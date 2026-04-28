package com.android.tomatoapp.analytics.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tomatoapp.R;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.detection.data.DetectionHistoryManager;
import com.android.tomatoapp.detection.ui.DetectionResults;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class DetectionHistoryActivity extends BaseBottomNavActivity {

    private static final String TAG = "DetectionHistoryActivity";
    
    private RecyclerView historyRecyclerView;
    private View emptyState;
    private ArrayList<JSONObject> historyData;
    private HistoryAdapter adapter;
    private boolean isResumingFromOtherActivity = false;

    // Filter state: null = all, otherwise keyword to match in disease name
    private String activeFilter = null;
    private boolean filterHealthy = false;
    private boolean filterUnhealthy = false;

    // UI refs
    private TextView scanCountText;
    private MaterialCardView chipAll, chipDisease, chipRipeness, chipHealthy, chipUnhealthy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_history);

        setupBottomNavigation();

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        scanCountText = findViewById(R.id.scanCountText);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detection History");
        }

        // Setup RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        historyRecyclerView.setAdapter(adapter);

        // Bind filter chips
        chipAll       = findViewById(R.id.filterAll);
        chipDisease   = findViewById(R.id.filterDisease);
        chipRipeness  = findViewById(R.id.filterRipeness);
        chipHealthy   = findViewById(R.id.filterHealthy);
        chipUnhealthy = findViewById(R.id.filterUnhealthy);
        setupFilterChips();

        loadHistory();
    }

    private void setupFilterChips() {
        View.OnClickListener chipListener = v -> {
            // Reset all styling
            setChipActive(chipAll,       false);
            setChipActive(chipDisease,   false);
            setChipActive(chipRipeness,  false);
            setChipActive(chipHealthy,   false);
            setChipActive(chipUnhealthy, false);

            activeFilter   = null;
            filterHealthy  = false;
            filterUnhealthy = false;

            if (v == chipAll) {
                setChipActive(chipAll, true);
            } else if (v == chipDisease) {
                setChipActive(chipDisease, true);
                activeFilter = "blight";
            } else if (v == chipRipeness) {
                setChipActive(chipRipeness, true);
                activeFilter = "ripe";
            } else if (v == chipHealthy) {
                setChipActive(chipHealthy, true);
                filterHealthy = true;
            } else if (v == chipUnhealthy) {
                setChipActive(chipUnhealthy, true);
                filterUnhealthy = true;
            }
            applyFilter();
        };

        if (chipAll       != null) chipAll.setOnClickListener(chipListener);
        if (chipDisease   != null) chipDisease.setOnClickListener(chipListener);
        if (chipRipeness  != null) chipRipeness.setOnClickListener(chipListener);
        if (chipHealthy   != null) chipHealthy.setOnClickListener(chipListener);
        if (chipUnhealthy != null) chipUnhealthy.setOnClickListener(chipListener);

        // Default: All active
        setChipActive(chipAll, true);
    }

    private void setChipActive(MaterialCardView chip, boolean active) {
        if (chip == null) return;
        if (active) {
            chip.setCardBackgroundColor(getResources().getColor(R.color.tomato_red, getTheme()));
            chip.setStrokeWidth(0);
        } else {
            chip.setCardBackgroundColor(getResources().getColor(R.color.white, getTheme()));
            chip.setStrokeWidth(1);
        }
    }

    private void applyFilter() {
        if (historyData == null) return;
        ArrayList<JSONObject> filtered = new ArrayList<>();
        for (JSONObject entry : historyData) {
            String disease = entry.optString("disease", "").toLowerCase();
            boolean healthy = disease.contains("healthy");

            if (filterHealthy) {
                if (healthy) filtered.add(entry);
            } else if (filterUnhealthy) {
                if (!healthy) filtered.add(entry);
            } else if (activeFilter != null) {
                if (disease.contains(activeFilter)) filtered.add(entry);
            } else {
                filtered.add(entry);
            }
        }
        boolean hasItems = !filtered.isEmpty();
        historyRecyclerView.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(hasItems ? View.GONE : View.VISIBLE);
        adapter.updateData(filtered);
    }

    private void loadHistory() {
        historyData = DetectionHistoryManager.getHistory(this);

        // Reverse to show newest first
        if (historyData != null && !historyData.isEmpty()) {
            Collections.reverse(historyData);
        }

        // Update count badge
        int count = historyData != null ? historyData.size() : 0;
        if (scanCountText != null) {
            scanCountText.setText(count + (count == 1 ? " record" : " records"));
        }

        applyFilter();
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
                String disease    = entry.getString("disease");
                String description = entry.optString("description", "");
                String symptoms   = entry.optString("symptoms", "");
                String accuracy   = entry.optString("accuracy", "");
                String imageUri   = entry.optString("imageUri", "");
                String cultivar   = entry.optString("cultivar", getString(R.string.detection_cultivar_unspecified));
                int phase         = entry.optInt("phase", 0);

                boolean isHealthy = disease.toLowerCase().contains("healthy");

                // Title
                holder.title.setText(disease);

                // Description snippet
                if (!description.isEmpty()) {
                    holder.description.setText(description);
                } else if (!symptoms.isEmpty()) {
                    holder.description.setText(symptoms);
                } else {
                    holder.description.setText("Detection details not available.");
                }

                // Context / date footer
                if (holder.context != null) {
                    String timestamp = entry.optString("timestamp", "");
                    String ctx = timestamp.isEmpty()
                            ? (phase > 0 ? getString(R.string.detection_context_format, cultivar, phase) : cultivar)
                            : timestamp + (phase > 0 ? "  •  Phase " + phase : "");
                    holder.context.setText(ctx);
                }

                // Confidence footer
                if (holder.confidenceText != null) {
                    holder.confidenceText.setText(accuracy.isEmpty() ? "" : accuracy + " confident");
                }

                // Scan-type tag (infer from disease name)
                if (holder.scanTypeText != null) {
                    String lower = disease.toLowerCase();
                    if (lower.contains("ripe") || lower.contains("fruit")) {
                        holder.scanTypeText.setText("Ripeness");
                    } else if (lower.contains("pest") || lower.contains("mite") || lower.contains("fly")) {
                        holder.scanTypeText.setText("Pest");
                    } else {
                        holder.scanTypeText.setText("Disease");
                    }
                }

                // Result-status tag + accent bar
                int accentColor = isHealthy
                        ? getResources().getColor(R.color.fresh_green, getTheme())
                        : getResources().getColor(R.color.tomato_red, getTheme());
                if (holder.statusAccentBar != null) holder.statusAccentBar.setBackgroundColor(accentColor);
                if (holder.resultStatusText != null) {
                    holder.resultStatusText.setText(isHealthy ? "✅ Healthy" : "⚠ Infected");
                }
                if (holder.resultStatusTag != null) {
                    holder.resultStatusTag.setCardBackgroundColor(
                            isHealthy
                                    ? getResources().getColor(R.color.green_light, getTheme())
                                    : getResources().getColor(R.color.red_light, getTheme()));
                }
                if (holder.resultStatusText != null) {
                    holder.resultStatusText.setTextColor(
                            isHealthy
                                    ? getResources().getColor(R.color.sidebar_dark_green, getTheme())
                                    : getResources().getColor(R.color.tomato_red, getTheme()));
                }

                // Load image
                if (!imageUri.isEmpty()) {
                    try { holder.image.setImageURI(Uri.parse(imageUri)); }
                    catch (Exception e) { holder.image.setImageResource(R.mipmap.ic_logo); }
                } else {
                    holder.image.setImageResource(R.mipmap.ic_logo);
                }

                // Rounded image
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
                        Log.e(TAG, "Error opening detection details", e);
                        Toast.makeText(DetectionHistoryActivity.this, "Error opening detection details", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error setting up detection item click listener", e);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title, description, context, confidenceText;
            View statusAccentBar;
            MaterialCardView scanTypeTag, resultStatusTag;
            TextView scanTypeText, resultStatusText;
            ImageButton deleteButton;
            private boolean isDeleteButtonClicked = false;

            HistoryViewHolder(View itemView) {
                super(itemView);
                image            = itemView.findViewById(R.id.historyItemImage);
                title            = itemView.findViewById(R.id.historyItemTitle);
                description      = itemView.findViewById(R.id.historyItemDescription);
                context          = itemView.findViewById(R.id.historyItemContext);
                confidenceText   = itemView.findViewById(R.id.confidenceText);
                statusAccentBar  = itemView.findViewById(R.id.statusAccentBar);
                scanTypeTag      = itemView.findViewById(R.id.scanTypeTag);
                resultStatusTag  = itemView.findViewById(R.id.resultStatusTag);
                scanTypeText     = itemView.findViewById(R.id.scanTypeText);
                resultStatusText = itemView.findViewById(R.id.resultStatusText);
                deleteButton     = itemView.findViewById(R.id.deleteButton);
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
            Log.e(TAG, "Error deleting detection", e);
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
