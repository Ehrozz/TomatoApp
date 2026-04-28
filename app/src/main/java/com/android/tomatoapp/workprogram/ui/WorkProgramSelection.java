package com.android.tomatoapp.workprogram.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;
import android.util.Log;

import com.android.tomatoapp.R;
import com.android.tomatoapp.auth.ui.Login;
import com.android.tomatoapp.common.utils.CultivarImageHelper;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;

public class WorkProgramSelection extends BaseBottomNavActivity {

    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton btnAdd;
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

        btnAdd = findViewById(R.id.fabAddProgram);
        programCountText = findViewById(R.id.programCountText);
        emptyState = findViewById(R.id.emptyState);
        headerMenuButton = findViewById(R.id.headerMenuButton);

        // Setup Bottom Navigation
        setupBottomNavigation();

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

        btnAdd.setOnClickListener(v -> showAddProgramDialog());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Work Program");
        }

        // Header menu button - Sort options
        headerMenuButton.setOnClickListener(v -> showSortMenu());
    }

    private void showAddProgramDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_work_program, null);
        
        
        android.widget.AutoCompleteTextView cultivarSpinner = dialogView.findViewById(R.id.cultivarSpinner);
        com.google.android.material.textfield.TextInputEditText landAreaInput = dialogView.findViewById(R.id.landAreaInput);
        com.google.android.material.textfield.TextInputEditText startDateInput = dialogView.findViewById(R.id.startDateInput);
        com.google.android.material.button.MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        // Cultivar data
        final String[][] cultivarsData = {
                {"Victory F1", "Semi-determinate", "90", "110"},
                {"HOPE F1", "Semi-determinate", "90", "110"},
                {"Maganda F1", "Semi-determinate", "80", "100"},
                {"Malakas F1", "Semi-determinate", "95", "115"},
                {"Rocky 1 F1", "Semi-determinate", "90", "110"},
                {"Improved KS Apollo", "Semi-determinate", "85", "105"},
                {"Improved Pope", "Semi-determinate", "85", "105"},
                {"Super Pope", "Semi-determinate", "85", "105"},
                {"Maguilas", "Determinate", "85", "105"},
                {"Maunlad", "Determinate", "80", "100"},
                {"Mapalad", "Determinate", "80", "100"},
                {"Abiona F1", "Semi-determinate", "95", "115"},
                {"Akna F1", "Semi-determinate", "105", "125"},
                {"Amari F1", "Semi-determinate", "110", "130"},
                {"Anita F1", "Semi-determinate", "110", "130"},
                {"Colette F1", "Determinate", "105", "125"},
                {"Danica F1", "Semi-determinate", "105", "125"},
                {"Granger F1", "Semi-determinate", "105", "125"},
                {"Janet F1", "Semi-determinate", "120", "140"},
                {"Platinum F1", "Semi-determinate", "100", "120"},
                {"Reina F1", "Semi-determinate", "105", "125"},
                {"Renata F1", "Semi-determinate", "105", "125"},
                {"Rubellite F1", "Semi-determinate", "90", "110"},
                {"TOM-055 F1", "Semi-determinate", "60", "75"},
                {"TOM-262 OP", "Determinate", "60", "75"},
                {"Dalwangan Tm1", "Determinate", "90", "110"},
                {"Dalwangan Tm2", "Determinate", "90", "110"},
                {"NSIC 1999 Tm09", "Determinate", "100", "120"},
                {"Mara", "Determinate", "78", "95"},
                {"AniMax 1", "Determinate", "87", "105"},
                {"AniMax 2", "Semi-determinate", "87", "105"},
                {"Golden Globe", "Semi-determinate", "92", "112"},
                {"Maxxime", "Indeterminate", "105", "125"}
        };

        String[] cultivarNames = new String[cultivarsData.length];
        for (int i = 0; i < cultivarsData.length; i++) {
            cultivarNames[i] = cultivarsData[i][0];
        }

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, cultivarNames);
        cultivarSpinner.setAdapter(adapter);

        // Date Picker logic
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        startDateInput.setText(sdf.format(calendar.getTime()));

        startDateInput.setOnClickListener(v -> {
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                startDateInput.setText(sdf.format(calendar.getTime()));
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSubmit.setOnClickListener(v -> {
            String cultivar = cultivarSpinner.getText() != null ? cultivarSpinner.getText().toString() : "";
            String areaStr = landAreaInput.getText() != null ? landAreaInput.getText().toString().trim() : "";
            String date = startDateInput.getText() != null ? startDateInput.getText().toString() : "";

            if (cultivar.isEmpty() || areaStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double area;
            try {
                area = Double.parseDouble(areaStr);
                if (area <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid land area", Toast.LENGTH_SHORT).show();
                return;
            }

            saveNewWorkProgram(cultivar, area, date, cultivarsData, dialog);
        });

        dialog.show();
    }

    private void saveNewWorkProgram(String cultivar, double area, String startDate, String[][] cultivarsData, AlertDialog dialog) {
        if (dbRef == null || userId == null) return;

        String id = dbRef.push().getKey();
        if (id == null) return;

        int tempMaturity = 90;
        for (String[] c : cultivarsData) {
            if (c[0].equals(cultivar)) {
                tempMaturity = Integer.parseInt(c[3]);
                break;
            }
        }
        final int finalMaturity = tempMaturity;

        // Prepare Firebase data
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("cultivarName", cultivar);
        data.put("startingDate", startDate);
        data.put("areaSize", area);
        data.put("projectedIncome", 0.0);
        data.put("projectedExpenses", 0.0);
        data.put("adjustedIncome", 0.0);
        data.put("adjustedExpenses", 0.0);

        // Calculate phases JSON
        String phasesJson = com.android.tomatoapp.workprogram.data.WorkProgramDataHelper.calculatePhasesJson(cultivar, startDate);

        // Local Entity
        WorkProgramEntity entity = new WorkProgramEntity(
                id, userId, cultivar, area, startDate, phasesJson, null,
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0,
                0, 0, 0, 0, 0.0
        );

        LocalDataManager.getInstance(this).saveWorkProgramToLocal(entity, "CREATE");
        LocalDataManager.getInstance(this).processSyncQueue(this, userId);

        dbRef.child(id).setValue(data).addOnSuccessListener(aVoid -> {
            // Initialize task logs
            DatabaseReference logs = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("routineLogs")
                    .child(id)
                    .child("tasks");

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Calendar cal = java.util.Calendar.getInstance();
                java.util.Date parsedDate = sdf.parse(startDate);
                if (parsedDate != null) {
                    cal.setTime(parsedDate);
                    
                    final java.util.Calendar workerCal = (java.util.Calendar) cal.clone();
                    new Thread(() -> {
                        for (int i = 0; i < finalMaturity; i++) {
                            String dayKey = sdf.format(workerCal.getTime());
                            logs.child(dayKey).setValue("pending");
                            workerCal.add(java.util.Calendar.DAY_OF_YEAR, 1);
                        }
                    }).start();
                }
            } catch (Exception e) {
                Log.e("WorkProgramSelection", "Error initializing logs", e);
            }

            Toast.makeText(this, "Program Initialized!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Saved locally. Will sync when online.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // When resuming, check if we're online and sync
        if (LocalDataManager.isOnline(this)) {
            // Upload any queued offline changes (work programs, locations, etc.)
            LocalDataManager.getInstance(this).processSyncQueue(this, userId);
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
                if (!LocalDataManager.isOnline(this) && !cultivarList.isEmpty()) {
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
                programCountText.setText(String.format(java.util.Locale.getDefault(), "%d programs", count));
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
                cultivarList.sort(java.util.Comparator.comparing(a -> a.date));
                break;
            case "name_asc": // A-Z
                cultivarList.sort(java.util.Comparator.comparing(a -> a.name, String.CASE_INSENSITIVE_ORDER));
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
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            
            Cultivar item = items.get(adapterPosition);
            holder.name.setText(item.name);
            
            // Format date according to user preference
            try {
                SimpleDateFormat parseFormat = SettingsPreferences.getDateParseFormat();
                SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(holder.itemView.getContext());
                java.util.Date dateObj = parseFormat.parse(item.date);
                if (dateObj != null) {
                    String formattedDate = displayFormat.format(dateObj);
                    holder.date.setText(String.format(java.util.Locale.getDefault(), "Started: %s", formattedDate));
                } else {
                    holder.date.setText(String.format(java.util.Locale.getDefault(), "Started: %s", item.date));
                }
            } catch (Exception e) {
                holder.date.setText(String.format(java.util.Locale.getDefault(), "Started: %s", item.date));
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

            int colorIndex = adapterPosition % bgColors.length;
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
                            .setMessage(String.format(java.util.Locale.getDefault(), "Are you sure you want to delete '%s'?\n\nThis will also delete all associated tasks and cannot be undone.", item.name))
                            .setPositiveButton("Delete", (dialog, which) -> {
                                deleteWorkProgram(item.programId, holder.getAdapterPosition());
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
            
            LocalDataManager.getInstance(activity).deleteWorkProgramLocally(activity.userId, programId);
            LocalDataManager.getInstance(activity).processSyncQueue(activity, activity.userId);

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
