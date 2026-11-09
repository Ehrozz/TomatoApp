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
import com.android.tomatoapp.common.utils.PhaseHelper;
import com.android.tomatoapp.common.utils.AppClock;
import com.android.tomatoapp.common.utils.PlacementDebugDump;
import com.android.tomatoapp.task.data.TaskModel;
import com.android.tomatoapp.task.data.TomatoWorkProgramScheduler;
import com.android.tomatoapp.task.data.ScheduledTask;
import com.android.tomatoapp.task.data.TaskTemplate;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.financial.ui.DailyExpensesActivity;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;
import com.android.tomatoapp.workprogram.data.IntakeAnswers;

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
                    new Thread(() -> {
                        java.util.Map<String, Integer> localRates = new java.util.HashMap<>();
                        try {
                            List<WorkProgramEntity> entities = LocalDataManager.getInstance(WorkProgramSelection.this).getWorkProgramsFromLocal(userId);
                            for (WorkProgramEntity e : entities) {
                                if (e != null && e.id != null) {
                                    localRates.put(e.id, (int) Math.round(e.completionRate));
                                }
                            }
                        } catch (Exception ignored) {}

                        runOnUiThread(() -> {
                            cultivarList.clear();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String programId = child.getKey();
                                String cultivar = child.child("cultivarName").getValue(String.class);
                                String startDate = child.child("startingDate").getValue(String.class);

                                if (programId != null && cultivar != null && startDate != null) {
                                    int completionRate = 0;
                                    if (child.hasChild("completionRate")) {
                                        Object val = child.child("completionRate").getValue();
                                        if (val instanceof Number) {
                                            completionRate = ((Number) val).intValue();
                                        }
                                    } else if (localRates.containsKey(programId)) {
                                        Integer lr = localRates.get(programId);
                                        if (lr != null) completionRate = lr;
                                    }
                                    cultivarList.add(new Cultivar(programId, cultivar, startDate,
                                            CultivarImageHelper.getCultivarImageResource(cultivar), completionRate));
                                }
                            }
                            updateUI();
                            // Sync from Firebase to local to ensure consistency
                            LocalDataManager.getInstance(WorkProgramSelection.this).syncWorkProgramsFromFirebase(userId);
                        });
                    }).start();
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

        btnAdd.setOnClickListener(v -> showAddProgramChoice());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Work Program");
        }

        // Header menu button - Sort options
        headerMenuButton.setOnClickListener(v -> showSortMenu());
    }

    private void showAddProgramChoice() {
        new AlertDialog.Builder(this)
                .setTitle("Add work program")
                .setItems(new String[]{"Start new program", "Add ongoing planting"}, (dialog, which) -> {
                    showAddProgramDialog(which == 1);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddProgramDialog() {
        showAddProgramDialog(false);
    }

    private void showAddProgramDialog(boolean ongoing) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_work_program, null);
        
        
        android.widget.AutoCompleteTextView cultivarSpinner = dialogView.findViewById(R.id.cultivarSpinner);
        android.widget.AutoCompleteTextView landAreaInput = dialogView.findViewById(R.id.landAreaInput);
        com.google.android.material.textfield.TextInputEditText startDateInput = dialogView.findViewById(R.id.startDateInput);
        com.google.android.material.button.MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        TextView addProgramTitle = dialogView.findViewById(R.id.addProgramTitle);
        View ongoingBanner = dialogView.findViewById(R.id.ongoingBanner);
        if (ongoing) {
            addProgramTitle.setText("Add ongoing planting");
            ongoingBanner.setVisibility(View.VISIBLE);
        }

        // Cultivar data from centralized WorkProgramDataHelper
        final String[][] cultivarsData = com.android.tomatoapp.workprogram.data.WorkProgramDataHelper.cultivarsData;

        String[] cultivarNames = new String[cultivarsData.length];
        for (int i = 0; i < cultivarsData.length; i++) {
            cultivarNames[i] = cultivarsData[i][0];
        }

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, cultivarNames) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                android.widget.TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) textView.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                android.widget.TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) textView.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }
        };
        cultivarSpinner.setAdapter(adapter);

        // Land Area Dropdown options
        final String[] landAreaOptions = {
                "0.25 hectare",
                "0.5 hectare",
                "0.75 hectare",
                "1 hectare"
        };
        android.widget.ArrayAdapter<String> landAreaAdapter = new android.widget.ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, landAreaOptions) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                android.widget.TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) textView.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                android.widget.TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) textView.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }
        };
        landAreaInput.setAdapter(landAreaAdapter);
        landAreaInput.setKeyListener(null);
        landAreaInput.setThreshold(0);
        landAreaInput.setOnClickListener(v -> landAreaInput.showDropDown());
        landAreaInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) landAreaInput.showDropDown();
        });

        // Date Picker logic
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            calendar.setTime(sdf.parse(java.time.LocalDate.now().toString()));
        } catch (Exception ignored) {
            Log.w("WorkProgramSelection", "Using device date because app date is invalid");
        }
        startDateInput.setText(sdf.format(calendar.getTime()));

        startDateInput.setOnClickListener(v -> {
            android.app.DatePickerDialog picker = new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                startDateInput.setText(sdf.format(calendar.getTime()));
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH));
            picker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            picker.show();
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

            double area = parseSelectedLandAreaHectares(areaStr);
            if (area <= 0) {
                Toast.makeText(this, "Please select a land area", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!ongoing) {
                saveNewWorkProgram(cultivar, area, date, cultivarsData, dialog, null, null);
                return;
            }
            new WorkProgramIntakeDialog(this, cultivar, new WorkProgramIntakeDialog.Callback() {
                @Override
                public void onCompleted(com.android.tomatoapp.workprogram.data.IntakeAnswers answers,
                                        com.android.tomatoapp.workprogram.data.IntakeAnswers.Evaluation evaluation) {
                    String dateError = answers.validateDates(evaluation.startPhase, AppClock.today(WorkProgramSelection.this));
                    if (dateError != null) {
                        Toast.makeText(WorkProgramSelection.this,
                                dateError,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    new AlertDialog.Builder(WorkProgramSelection.this)
                            .setTitle("Ongoing planting summary")
                            .setMessage("We will place you in Phase " + evaluation.startPhase
                                    + ". Earlier tasks, milestones and progress will be marked done.")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Continue", (summary, which) ->
                                    saveNewWorkProgram(cultivar, area, date, cultivarsData, dialog, answers, evaluation))
                            .show();
                }

                @Override
                public void onCancelled() { }
            }).show();
        });

        dialog.show();
    }

    private boolean datesMatchPlacement(IntakeAnswers answers,
                                        IntakeAnswers.Evaluation evaluation,
                                        String cultivar) {
        if (answers == null || evaluation == null) return true;
        try {
            java.time.LocalDate appToday = AppClock.today(this);
            java.time.LocalDate planting = java.time.LocalDate.parse(
                    earliestDate(appToday.toString(), answers.seedsConfirmedDate, answers.sowingDate));
            java.time.LocalDate seeds = parseDateOrNull(answers.seedsConfirmedDate);
            java.time.LocalDate sowing = parseDateOrNull(answers.sowingDate);
            java.time.LocalDate transplant = parseDateOrNull(answers.transplantDate);
            java.time.LocalDate stop = parseDateOrNull(answers.stopDate);
            if (seeds != null && sowing != null && seeds.isAfter(sowing)) return false;
            if (sowing != null && transplant != null && sowing.isAfter(transplant)) return false;
            if (transplant != null && transplant.isAfter(appToday)) return false;
            int phase = com.android.tomatoapp.task.data.TaskSchedule.getPhaseNumber(
                    planting, transplant, stop, appToday);
            return phase == evaluation.startPhase;
        } catch (RuntimeException error) {
            Log.w("WorkProgramSelection", "Could not validate placement dates", error);
            return false;
        }
    }

    private static java.time.LocalDate parseDateOrNull(String value) {
        return value == null || value.trim().isEmpty()
                ? null : java.time.LocalDate.parse(value);
    }

    /** Maps the land-area dropdown labels to the hectare values used by scheduling and cost formulas. */
    private static double parseSelectedLandAreaHectares(String selected) {
        if (selected == null) return -1;
        switch (selected.trim()) {
            case "0.25 hectare":
                return 0.25;
            case "0.5 hectare":
                return 0.5;
            case "0.75 hectare":
                return 0.75;
            case "1 hectare":
                return 1.0;
            default:
                return -1;
        }
    }

    private void saveNewWorkProgram(String cultivar, double area, String startDate, String[][] cultivarsData,
                                    AlertDialog dialog,
                                    com.android.tomatoapp.workprogram.data.IntakeAnswers answers,
                                    com.android.tomatoapp.workprogram.data.IntakeAnswers.Evaluation evaluation) {
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
        String plantingStartDate = answers == null
                ? (startDate == null || startDate.isEmpty() ? AppClock.today(this).toString() : startDate)
                : earliestDate(AppClock.today(this).toString(), answers.seedsConfirmedDate, answers.sowingDate);

        // Prepare Firebase data
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("cultivarName", cultivar);
        data.put("startingDate", plantingStartDate);
        data.put("areaSize", area);
        data.put("projectedIncome", 0.0);
        data.put("projectedExpenses", 0.0);
        data.put("adjustedIncome", 0.0);
        data.put("adjustedExpenses", 0.0);
        int startPhase = evaluation != null ? evaluation.startPhase : 1;
        data.put("intakeStartPhase", startPhase);
        if (answers != null) {
            data.put("intake", answers.toMap(startPhase));
        }

        if (answers != null) {
            if (!answers.seedsConfirmedDate.isEmpty()) data.put("seedsConfirmedDate", answers.seedsConfirmedDate);
            if (!answers.sowingDate.isEmpty()) data.put("sowingDate", answers.sowingDate);
            if (!answers.transplantDate.isEmpty()) data.put("transplantDate", answers.transplantDate);
            if (!answers.stopDate.isEmpty()) data.put("stopDate", answers.stopDate);
            data.put("plantingStartDate", plantingStartDate);
            data.put("placementDate", AppClock.today(WorkProgramSelection.this).toString());
            data.put("placementStartPhase", startPhase);
            data.put("placement", new java.util.HashMap<String, Object>() {{
                put("answers", answers.toMap(startPhase));
                put("startPhase", startPhase);
                put("createdAt", System.currentTimeMillis());
                put("placementDate", AppClock.today(WorkProgramSelection.this).toString());
            }});
            data.put("placementSource", "onboarding");
        }

        // Calculate phases JSON
        String phasesJson = com.android.tomatoapp.workprogram.data.WorkProgramDataHelper.calculatePhasesJson(cultivar, plantingStartDate);

        // Local Entity
        WorkProgramEntity entity = new WorkProgramEntity(
                id, userId, cultivar, area, plantingStartDate, phasesJson, null,
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0,
                0, 0, 0, 0, 0.0
        );

        LocalDataManager.getInstance(this).saveWorkProgramToLocal(entity, "CREATE");
        LocalDataManager.getInstance(this).processSyncQueue(this, userId);

        dbRef.child(id).setValue(data).addOnSuccessListener(aVoid -> {
            PlacementDebugDump.log(id, plantingStartDate,
                    answers == null ? null : answers.seedsConfirmedDate,
                    answers == null ? null : answers.sowingDate,
                    answers == null ? null : answers.transplantDate,
                    answers == null ? null : answers.stopDate,
                    startPhase, answers == null ? null : answers.toMap(startPhase),
                    cultivar, area, AppClock.today(this));
            Toast.makeText(this, "Program starts at Phase " + startPhase, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            Intent openProgram = new Intent(this, Workprogram.class);
            openProgram.putExtra("programId", id);
            if (evaluation != null && evaluation.catchUpPhases != null
                    && !evaluation.catchUpPhases.isEmpty()) {
                openProgram.putIntegerArrayListExtra("catchUpQueue",
                        new ArrayList<>(evaluation.catchUpPhases));
                openProgram.putExtra("catchUpExpenseDate",
                        catchUpExpenseDate(plantingStartDate, answers, evaluation));
            }
            startActivity(openProgram);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Saved locally. Will sync when online.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    private String catchUpExpenseDate(String startDate,
                                      com.android.tomatoapp.workprogram.data.IntakeAnswers answers,
                                      com.android.tomatoapp.workprogram.data.IntakeAnswers.Evaluation evaluation) {
        if (answers == null || evaluation == null || evaluation.catchUpPhases == null
                || evaluation.catchUpPhases.isEmpty()) return startDate;
        int first = evaluation.catchUpPhases.get(0);
        if (first == 1 && answers.sowingDate != null && !answers.sowingDate.isEmpty()) {
            return answers.sowingDate;
        }
        if (first >= 2 && answers.transplantDate != null && !answers.transplantDate.isEmpty()) {
            return answers.transplantDate;
        }
        return startDate;
    }

    private static String earliestDate(String fallback, String... dates) {
        String earliest = fallback;
        for (String value : dates) {
            if (value == null || value.trim().isEmpty()) continue;
            if (earliest == null || earliest.isEmpty() || value.compareTo(earliest) < 0) earliest = value;
        }
        return earliest;
    }

    private void showCatchUpExpensesDialog(String programId, String cultivar, String startDate,
                                           com.android.tomatoapp.workprogram.data.IntakeAnswers answers,
                                           com.android.tomatoapp.workprogram.data.IntakeAnswers.Evaluation evaluation) {
        if (evaluation == null || evaluation.catchUpPhases == null || evaluation.catchUpPhases.isEmpty()) {
            return;
        }

        List<String> labels = new ArrayList<>();
        for (Integer phase : evaluation.catchUpPhases) {
            labels.add(PhaseHelper.getPhaseNameWithDescription(phase));
        }
        boolean[] checked = new boolean[labels.size()];
        for (int i = 0; i < checked.length; i++) checked[i] = true;

        new AlertDialog.Builder(this)
                .setTitle("Log expenses already spent")
                .setMessage("Earlier phases were marked completed via setup intake. Log money already spent on the program start date. This uses the same daily expense table.")
                .setMultiChoiceItems(labels.toArray(new String[0]), checked, (d, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Log selected", (d, which) -> {
                    ArrayList<Integer> selectedPhases = new ArrayList<>();
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) {
                            selectedPhases.add(evaluation.catchUpPhases.get(i));
                        }
                    }
                    if (selectedPhases.isEmpty()) return;
                    String expenseDate = startDate;
                    if (answers != null) {
                        Integer first = selectedPhases.get(0);
                        if (first == 1 && !answers.sowingDate.isEmpty()) expenseDate = answers.sowingDate;
                        if (first == 2 && !answers.transplantDate.isEmpty()) expenseDate = answers.transplantDate;
                        if (first >= 3 && !answers.transplantDate.isEmpty()) expenseDate = answers.transplantDate;
                    }
                    Intent intent = new Intent(this, DailyExpensesActivity.class);
                    intent.putExtra("programId", programId);
                    intent.putExtra("cultivar", cultivar);
                    intent.putExtra("date", expenseDate);
                    intent.putExtra("programStartDate", startDate);
                    intent.putExtra("catchUpPhase", selectedPhases.get(0));
                    intent.putIntegerArrayListExtra("catchUpQueue", selectedPhases);
                    startActivity(intent);
                    Toast.makeText(this, "Log costs for past work. You can add more dates from Daily Task later.", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Later", null)
                .show();
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
        }
        // Reload to show latest progress rates and data
        loadWorkPrograms();
    }

    private void loadWorkPrograms() {
        if (LocalDataManager.isOnline(this)) {
            // Try Firebase first if online
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    new Thread(() -> {
                        java.util.Map<String, Integer> localRates = new java.util.HashMap<>();
                        try {
                            List<WorkProgramEntity> entities = LocalDataManager.getInstance(WorkProgramSelection.this).getWorkProgramsFromLocal(userId);
                            for (WorkProgramEntity e : entities) {
                                if (e != null && e.id != null) {
                                    localRates.put(e.id, (int) Math.round(e.completionRate));
                                }
                            }
                        } catch (Exception ignored) {}

                        runOnUiThread(() -> {
                            cultivarList.clear();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String programId = child.getKey();
                                String cultivar = child.child("cultivarName").getValue(String.class);
                                String startDate = child.child("startingDate").getValue(String.class);

                                if (programId != null && cultivar != null && startDate != null) {
                                    int completionRate = 0;
                                    if (child.hasChild("completionRate")) {
                                        Object val = child.child("completionRate").getValue();
                                        if (val instanceof Number) {
                                            completionRate = ((Number) val).intValue();
                                        }
                                    } else if (localRates.containsKey(programId)) {
                                        Integer lr = localRates.get(programId);
                                        if (lr != null) completionRate = lr;
                                    }
                                    cultivarList.add(new Cultivar(programId, cultivar, startDate,
                                            CultivarImageHelper.getCultivarImageResource(cultivar), completionRate));
                                }
                            }
                            updateUI();
                        });
                    }).start();
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
                        int rate = (int) Math.round(entity.completionRate);
                        cultivarList.add(new Cultivar(entity.id, entity.cultivarName, entity.startingDate,
                                CultivarImageHelper.getCultivarImageResource(entity.cultivarName), rate));
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
        final SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final Date epoch = new Date(0); // fallback for null/unparseable dates

        switch (sortOrder) {
            case "date_desc": // Newest first
                cultivarList.sort((a, b) -> {
                    Date da = parseDate(dateSdf, a.date, epoch);
                    Date db = parseDate(dateSdf, b.date, epoch);
                    return db.compareTo(da);
                });
                break;
            case "date_asc": // Oldest first
                cultivarList.sort((a, b) -> {
                    Date da = parseDate(dateSdf, a.date, epoch);
                    Date db = parseDate(dateSdf, b.date, epoch);
                    return da.compareTo(db);
                });
                break;
            case "name_asc": // A-Z
                cultivarList.sort((a, b) -> {
                    String na = a.name != null ? a.name : "";
                    String nb = b.name != null ? b.name : "";
                    return na.compareToIgnoreCase(nb);
                });
                break;
            case "name_desc": // Z-A
                cultivarList.sort((a, b) -> {
                    String na = a.name != null ? a.name : "";
                    String nb = b.name != null ? b.name : "";
                    return nb.compareToIgnoreCase(na);
                });
                break;
        }

        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Sorted", Toast.LENGTH_SHORT).show();
    }

    private Date parseDate(SimpleDateFormat sdf, String dateStr, Date fallback) {
        if (dateStr == null || dateStr.isEmpty()) return fallback;
        try {
            Date d = sdf.parse(dateStr);
            return d != null ? d : fallback;
        } catch (java.text.ParseException e) {
            return fallback;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
    }



    // ---- Cultivar Model ----
    static class Cultivar {
        String programId, name, date;
        int imageRes;
        int completionRate; // 0-100

        Cultivar(String programId, String name, String date, int imageRes) {
            this.programId = programId;
            this.name = name;
            this.date = date;
            this.imageRes = imageRes;
            this.completionRate = 0;
        }

        Cultivar(String programId, String name, String date, int imageRes, int completionRate) {
            this.programId = programId;
            this.name = name;
            this.date = date;
            this.imageRes = imageRes;
            this.completionRate = completionRate;
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

            // Mini progress bar
            if (holder.cardProgressBar != null) {
                holder.cardProgressBar.setProgress(item.completionRate);
            }
            if (holder.cardProgressText != null) {
                holder.cardProgressText.setText(item.completionRate + "% complete");
            }
            
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
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(activity.userId).child("plantMonitoring").child(programId).removeValue();
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(activity.userId).child("detectionLogs").child(programId).removeValue();
                        
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
            android.widget.ProgressBar cardProgressBar;
            TextView cardProgressText;

            CultivarViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.cultivarName);
                date = itemView.findViewById(R.id.cultivarDate);
                image = itemView.findViewById(R.id.cultivarImage);
                card = itemView.findViewById(R.id.cultivarCard);
                statusText = itemView.findViewById(R.id.statusText);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
                deleteButton = itemView.findViewById(R.id.deleteButton);
                cardProgressBar  = itemView.findViewById(R.id.cardProgressBar);
                cardProgressText = itemView.findViewById(R.id.cardProgressText);
            }
        }
    }
}
