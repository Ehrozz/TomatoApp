package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkProgramSelection extends BaseDrawerActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    private CultivarAdapter adapter;
    private List<Cultivar> cultivarList = new ArrayList<>();
    private TextView programCountText;
    private View emptyState;
    private ImageView headerMenuButton;

    private DatabaseReference dbRef;
    private String userId;
    private String currentSortOrder = "date_desc"; // Default: newest first


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_program_selection);

        recyclerView = findViewById(R.id.workProgramRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CultivarAdapter(cultivarList);
        recyclerView.setAdapter(adapter);

        btnAdd = findViewById(R.id.addButton);
        programCountText = findViewById(R.id.programCountText);
        emptyState = findViewById(R.id.emptyState);
        headerMenuButton = findViewById(R.id.headerMenuButton);

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }
        
        userId = currentUser.getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("workPrograms");

        // Load data with offline fallback
        loadWorkPrograms();

        // Set up Firebase listener for real-time updates (when online)
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Only update if online (to avoid conflicts with offline data)
                if (LocalDataManager.isOnline(WorkProgramSelection.this)) {
                    cultivarList.clear();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        String programId = child.getKey();
                        String cultivar = child.child("cultivarName").getValue(String.class);
                        String startDate = child.child("startingDate").getValue(String.class);

                        if (programId != null && cultivar != null && startDate != null) {
                            cultivarList.add(new Cultivar(programId, cultivar, startDate, CultivarImageHelper.getCultivarImageResource(cultivar)));
                        }
                    }
                    
                    updateUI();
                    
                    // Sync from Firebase to local to ensure consistency
                    LocalDataManager.getInstance(WorkProgramSelection.this).syncWorkProgramsFromFirebase(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // If Firebase fails, try loading from local database
                if (!LocalDataManager.isOnline(WorkProgramSelection.this)) {
                    loadWorkProgramsFromLocal();
                }
            }
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(WorkProgramSelection.this, Workprogram.class);
            startActivity(intent);
        });

        setupDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Work Program");
        }

        // Header menu button - Sort options
        headerMenuButton.setOnClickListener(v -> showSortMenu());
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // When resuming, check if we're online and sync
        if (LocalDataManager.isOnline(this)) {
            // Sync local work programs to Firebase (for offline-created ones)
            LocalDataManager.getInstance(this).syncWorkProgramsToFirebase(this, userId);
            // Also sync from Firebase to local (to get any updates)
            LocalDataManager.getInstance(this).syncWorkProgramsFromFirebase(userId);
            // Reload to show synced data
            loadWorkPrograms();
        }
    }

    private void loadWorkPrograms() {
        if (LocalDataManager.isOnline(this)) {
            // Try Firebase first if online
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    cultivarList.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String programId = child.getKey();
                        String cultivar = child.child("cultivarName").getValue(String.class);
                        String startDate = child.child("startingDate").getValue(String.class);

                        if (programId != null && cultivar != null && startDate != null) {
                            cultivarList.add(new Cultivar(programId, cultivar, startDate, CultivarImageHelper.getCultivarImageResource(cultivar)));
                        }
                    }
                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Fallback to local database
                    loadWorkProgramsFromLocal();
                }
            });
        } else {
            // Offline - load from local database
            loadWorkProgramsFromLocal();
        }
    }

    private void loadWorkProgramsFromLocal() {
        new Thread(() -> {
            List<WorkProgramEntity> entities = LocalDataManager.getInstance(this).getWorkProgramsFromLocal(userId);
            runOnUiThread(() -> {
                cultivarList.clear();
                for (WorkProgramEntity entity : entities) {
                    if (entity.cultivarName != null && entity.startingDate != null) {
                        cultivarList.add(new Cultivar(entity.id, entity.cultivarName, entity.startingDate, CultivarImageHelper.getCultivarImageResource(entity.cultivarName)));
                    }
                }
                updateUI();
                if (!LocalDataManager.isOnline(this) && cultivarList.size() > 0) {
                    Toast.makeText(this, "Showing offline data", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateUI() {
        // Update program count
        int count = cultivarList.size();
        if (programCountText != null) {
            if (count == 1) {
                programCountText.setText("1 program");
            } else {
                programCountText.setText(String.format("%d programs", count));
            }
        }
        
        // Show/hide empty state
        if (emptyState != null && recyclerView != null) {
            if (count == 0) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
        
        // Apply current sort order
        sortPrograms(currentSortOrder);
        adapter.notifyDataSetChanged();
    }

    private void showSortMenu() {
        String[] sortOptions = {
            "Newest First",
            "Oldest First",
            "Name (A-Z)",
            "Name (Z-A)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Sort Work Programs")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Newest First
                            sortPrograms("date_desc");
                            break;
                        case 1: // Oldest First
                            sortPrograms("date_asc");
                            break;
                        case 2: // Name A-Z
                            sortPrograms("name_asc");
                            break;
                        case 3: // Name Z-A
                            sortPrograms("name_desc");
                            break;
                    }
                })
                .show();
    }

    private void sortPrograms(String sortOrder) {
        currentSortOrder = sortOrder;
        
        switch (sortOrder) {
            case "date_desc": // Newest first
                cultivarList.sort((a, b) -> b.date.compareTo(a.date));
                break;
            case "date_asc": // Oldest first
                cultivarList.sort((a, b) -> a.date.compareTo(b.date));
                break;
            case "name_asc": // A-Z
                cultivarList.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
                break;
            case "name_desc": // Z-A
                cultivarList.sort((a, b) -> b.name.compareToIgnoreCase(a.name));
                break;
        }
        
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Sorted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
    }



    // ---- Cultivar Model ----
    static class Cultivar {
        String programId, name, date;
        int imageRes;

        Cultivar(String programId, String name, String date, int imageRes) {
            this.programId = programId;
            this.name = name;
            this.date = date;
            this.imageRes = imageRes;
        }
    }

    // ---- RecyclerView Adapter ----
    class CultivarAdapter extends RecyclerView.Adapter<CultivarAdapter.CultivarViewHolder> {
        private final List<Cultivar> items;

        CultivarAdapter(List<Cultivar> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public CultivarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cultivar, parent, false);
            return new CultivarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CultivarViewHolder holder, int position) {
            Cultivar item = items.get(position);
            holder.name.setText(item.name);
            
            // Format date according to user preference
            try {
                SimpleDateFormat parseFormat = SettingsPreferences.getDateParseFormat();
                SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(holder.itemView.getContext());
                java.util.Date dateObj = parseFormat.parse(item.date);
                String formattedDate = displayFormat.format(dateObj);
                holder.date.setText(String.format("Started: %s", formattedDate));
            } catch (Exception e) {
            holder.date.setText(String.format("Started: %s", item.date));
            }
            holder.image.setImageResource(item.imageRes);
            
            // Make image circular
            holder.image.setClipToOutline(true);
            holder.image.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(android.view.View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            holder.image.setBackgroundResource(R.drawable.circular_image_border);

            // 🍅 Rotate through tomato-themed colors with better contrast
            int[] bgColors = {
                    R.color.tomato_red,
                    R.color.fresh_green,
                    R.color.warm_orange,
                    R.color.sidebar_dark_green,
                    R.color.ripe_orange
            };

            int colorIndex = position % bgColors.length;
            int bgColor = ContextCompat.getColor(holder.itemView.getContext(), bgColors[colorIndex]);
            holder.card.setCardBackgroundColor(bgColor);

            // All backgrounds are vibrant, so use white text for better contrast
                holder.name.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                holder.date.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            
            // Update status indicator and text
            if (holder.statusIndicator != null) {
                // Green for active programs
                holder.statusIndicator.setBackground(ContextCompat.getDrawable(
                    holder.itemView.getContext(), R.drawable.circle_green));
            }
            if (holder.statusText != null) {
                holder.statusText.setText("Active");
                holder.statusText.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), android.R.color.white));
            }

            // Delete button click → show confirmation and delete
            if (holder.deleteButton != null) {
                holder.deleteButton.setOnClickListener(v -> {
                    // Show confirmation dialog
                    new AlertDialog.Builder(WorkProgramSelection.this)
                            .setTitle("Delete Work Program")
                            .setMessage(String.format("Are you sure you want to delete '%s'?\n\nThis will also delete all associated tasks and cannot be undone.", item.name))
                            .setPositiveButton("Delete", (dialog, which) -> {
                                deleteWorkProgram(item.programId, position);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            }

            // Click → open Workprogram
            holder.card.setOnClickListener(v -> {
                Intent intent = new Intent(WorkProgramSelection.this, Workprogram.class);
                intent.putExtra("programId", item.programId);
                intent.putExtra("cultivar", item.name);
                intent.putExtra("startDate", item.date);
                startActivity(intent);
            });
        }
        
        private void deleteWorkProgram(String programId, int position) {
            WorkProgramSelection activity = WorkProgramSelection.this;
            if (activity.dbRef == null || programId == null) return;
            
            // Delete from Firebase
            activity.dbRef.child(programId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Also delete associated routine logs
                        DatabaseReference routineLogsRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(activity.userId)
                                .child("routineLogs")
                                .child(programId);
                        routineLogsRef.removeValue();
                        
                        Toast.makeText(activity, "Work program deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(activity, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        class CultivarViewHolder extends RecyclerView.ViewHolder {
            TextView name, date, statusText;
            ImageView image, deleteButton;
            CardView card;
            View statusIndicator;

            CultivarViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.cultivarName);
                date = itemView.findViewById(R.id.cultivarDate);
                image = itemView.findViewById(R.id.cultivarImage);
                card = itemView.findViewById(R.id.cultivarCard);
                statusText = itemView.findViewById(R.id.statusText);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}
