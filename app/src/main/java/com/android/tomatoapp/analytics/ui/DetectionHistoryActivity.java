package com.android.tomatoapp.analytics.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class DetectionHistoryActivity extends BaseDrawerActivity {

    private RecyclerView historyRecyclerView;
    private View emptyState;
    private ArrayList<JSONObject> historyData;
    private HistoryAdapter adapter;
    private boolean isResumingFromOtherActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_history);

        setupDrawer();

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        emptyState = findViewById(R.id.emptyState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detection History");
        }

        // Setup RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        historyRecyclerView.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        historyData = DetectionHistoryManager.getHistory(this);
        
        // Reverse to show newest first
        if (historyData != null && !historyData.isEmpty()) {
            Collections.reverse(historyData);
        }

        if (historyData == null || historyData.isEmpty()) {
            historyRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            historyRecyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            adapter.updateData(historyData);
        }
    }

    // RecyclerView Adapter
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
        private ArrayList<JSONObject> items = new ArrayList<>();

        void updateData(ArrayList<JSONObject> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_detection_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryViewHolder holder, int position) {
            JSONObject entry = items.get(position);
            try {
                String title = entry.getString("disease");
                String description = entry.optString("description", "");
                String symptoms = entry.optString("symptoms", "");
                String imageUri = entry.optString("imageUri", "");
                String cultivar = entry.optString("cultivar", getString(R.string.detection_cultivar_unspecified));
                int phase = entry.optInt("phase", 0);
                
                // Set title
                holder.title.setText(title);
                
                // Set description (use description, symptoms, or default)
                if (!description.isEmpty()) {
                    holder.description.setText(description);
                } else if (!symptoms.isEmpty()) {
                    holder.description.setText(symptoms);
                } else {
                    holder.description.setText("Detection details not available.");
                }
                
                // Context info
                if (holder.context != null) {
                    if (phase > 0) {
                        holder.context.setText(getString(R.string.detection_context_format, cultivar, phase));
                    } else {
                        holder.context.setText(cultivar);
                    }
                }

                // Load image if available
                if (!imageUri.isEmpty()) {
                    try {
                        holder.image.setImageURI(Uri.parse(imageUri));
                    } catch (Exception e) {
                        holder.image.setImageResource(R.mipmap.ic_logo);
                    }
                } else {
                    holder.image.setImageResource(R.mipmap.ic_logo);
                }
                
                // Make image circular
                holder.image.setClipToOutline(true);
                holder.image.setOutlineProvider(new android.view.ViewOutlineProvider() {
                    @Override
                    public void getOutline(android.view.View view, android.graphics.Outline outline) {
                        outline.setOval(0, 0, view.getWidth(), view.getHeight());
                    }
                });
                
                // Delete button click listener - prevent event propagation
                holder.deleteButton.setOnClickListener(v -> {
                    holder.isDeleteButtonClicked = true;
                    v.setClickable(false); // Prevent multiple clicks
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        v.setClickable(true);
                        holder.isDeleteButtonClicked = false;
                        return;
                    }
                    new AlertDialog.Builder(DetectionHistoryActivity.this)
                            .setTitle("Delete Detection")
                            .setMessage("Are you sure you want to delete this detection?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                // Use adapter position to ensure correct item
                                int pos = holder.getAdapterPosition();
                                if (pos != RecyclerView.NO_POSITION && pos < items.size()) {
                                    deleteDetection(pos);
                                }
                                holder.isDeleteButtonClicked = false;
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                v.setClickable(true);
                                holder.isDeleteButtonClicked = false;
                            })
                            .setOnDismissListener(dialog -> {
                                v.setClickable(true);
                                // Reset flag after a delay to prevent item click
                                holder.itemView.postDelayed(() -> holder.isDeleteButtonClicked = false, 300);
                            })
                            .show();
                });
                
                // Set click listener for item - use proper click handling
                holder.itemView.setOnClickListener(v -> {
                    // Don't open if delete button was clicked
                    if (holder.isDeleteButtonClicked) {
                        return;
                    }
                    
                    // Check if click was directly on delete button
                    if (v == holder.deleteButton) {
                        return;
                    }
                    
                    // Get current adapter position to ensure we have the right item
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION || adapterPosition >= items.size()) {
                        return;
                    }
                    
                    try {
                        // Get the entry at current position
                        JSONObject currentEntry = items.get(adapterPosition);
                        
                        Intent intent = new Intent(DetectionHistoryActivity.this, DetectionResults.class);
                        intent.putExtra("title", currentEntry.optString("disease", "Unknown"));
                        intent.putExtra("imageUri", currentEntry.optString("imageUri", ""));
                        intent.putExtra("description", currentEntry.optString("description", ""));
                        intent.putExtra("symptoms", currentEntry.optString("symptoms", ""));
                        intent.putExtra("cause", currentEntry.optString("cause", ""));
                        intent.putExtra("cure", currentEntry.optString("cure", ""));
                        intent.putExtra("prevention", currentEntry.optString("prevention", ""));
                        intent.putExtra("pestTitle", currentEntry.optString("pestTitle", ""));
                        intent.putExtra("pestDescription", currentEntry.optString("pestDescription", ""));
                        intent.putExtra("pestImageUri", currentEntry.optString("pestImageUri", ""));
                        intent.putExtra("accuracy", currentEntry.optString("accuracy", "0%"));
                        intent.putExtra("topPredictions", currentEntry.optString("topPredictions", ""));
                        intent.putExtra("confidenceWarning", currentEntry.optString("confidenceWarning", ""));
                        intent.putExtra("detectionCultivar", currentEntry.optString("cultivar", getString(R.string.detection_cultivar_unspecified)));
                        intent.putExtra("detectionPhase", currentEntry.optInt("phase", 0));
                        
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(DetectionHistoryActivity.this, "Error opening detection details", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title, description, context;
            ImageButton deleteButton;
            private boolean isDeleteButtonClicked = false;

            HistoryViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.historyItemImage);
                title = itemView.findViewById(R.id.historyItemTitle);
                description = itemView.findViewById(R.id.historyItemDescription);
                context = itemView.findViewById(R.id.historyItemContext);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }

    private void deleteDetection(int position) {
        if (historyData == null || position < 0 || position >= historyData.size()) {
            return;
        }

        try {
            // Get the entry to delete
            JSONObject entryToDelete = historyData.get(position);
            
            // Remove from local list
            historyData.remove(position);
            
            // Update history in SharedPreferences
            DetectionHistoryManager.removeDetection(this, entryToDelete);
            
            // Refresh the list
            loadHistory();
            
            Toast.makeText(this, "Detection deleted", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to delete detection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only reload if we're returning from another activity (not initial creation)
        if (isResumingFromOtherActivity) {
            loadHistory();
            isResumingFromOtherActivity = false;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Mark that we're pausing (likely going to another activity)
        isResumingFromOtherActivity = true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
