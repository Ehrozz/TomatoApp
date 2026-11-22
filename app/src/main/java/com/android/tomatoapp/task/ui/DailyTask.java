package com.android.tomatoapp.task.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.android.tomatoapp.R;
import com.android.tomatoapp.auth.ui.Login;
import com.android.tomatoapp.common.utils.CultivarImageHelper;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.financial.ui.DailyExpensesActivity;
import com.android.tomatoapp.monitoring.ui.PlantMonitoringActivity;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.task.data.TaskModel;
import com.android.tomatoapp.task.data.TaskSchedule;
import com.android.tomatoapp.task.data.TomatoWorkProgramScheduler;
import com.android.tomatoapp.common.utils.AppClock;

public class DailyTask extends BaseBottomNavActivity {

    private TextView cultivarNameHeader, dateHeader, taskSectionTitle, taskCountText;
    private ImageView cultivarImageHeader;
    private MaterialButton btnComplete;
    private MaterialButton btnSkip;
    private MaterialButton btnMonitor;
    private MaterialButton btnDailyExpenses;
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskModel> taskList = new ArrayList<>();
    private boolean[] checkedTasks; // Track which tasks are checked
    private TextView programInsights;

    private boolean isNewProgram = false;
    private String cultivar, date, programId, startDate;
    private String growthHabit;
    private int dayNumber;
    private int maturityDays;
    private String seedsConfirmedDate;
    private String sowingDate;
    private String transplantDate;
    private String stopDate;
    private String phase3Date, phase4Date, phase5Date;
    private String placementDate;
    private int intakeStartPhase = 1;
    private DatabaseReference taskRef;
    private boolean dayCompleted;
    private boolean previewMode;


    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_task);

        // Initialize views
        cultivarNameHeader = findViewById(R.id.cultivarNameHeader);
        programInsights = findViewById(R.id.programInsights);
        dateHeader = findViewById(R.id.dateHeader);
        cultivarImageHeader = findViewById(R.id.cultivarImageHeader);
        taskSectionTitle = findViewById(R.id.taskSectionTitle);
        //taskCountText = findViewById(R.id.taskCountText);
        btnComplete = findViewById(R.id.btnComplete);
        btnSkip = findViewById(R.id.btnSkipTasks);
        btnMonitor = findViewById(R.id.btnMonitorPlant);
        btnDailyExpenses = findViewById(R.id.btnDailyExpenses);
        taskRecyclerView = findViewById(R.id.taskRecyclerView);

        // Get intent data
        cultivar = getIntent().getStringExtra("cultivar");
        date = getIntent().getStringExtra("date");
        programId = getIntent().getStringExtra("programId");
        startDate = getIntent().getStringExtra("programStartDate");
        growthHabit = getIntent().getStringExtra("growthHabit");
        maturityDays = getIntent().getIntExtra("maturityDays", 0);
        seedsConfirmedDate = getIntent().getStringExtra("seedsConfirmedDate");
        sowingDate = getIntent().getStringExtra("sowingDate");
        transplantDate = getIntent().getStringExtra("transplantDate");
        stopDate = getIntent().getStringExtra("stopDate");
        phase3Date = getIntent().getStringExtra("phase3Date");
        phase4Date = getIntent().getStringExtra("phase4Date");
        phase5Date = getIntent().getStringExtra("phase5Date");
        placementDate = getIntent().getStringExtra("placementDate");
        intakeStartPhase = getIntent().getIntExtra("intakeStartPhase", 1);
        int adjustedDayNumber = getIntent().getIntExtra("adjustedDayNumber", 0);
        previewMode = getIntent().getBooleanExtra("previewMode", false);

        // A new program has no persisted state yet. Existing programs must wait
        // for Firebase state instead of using today's date as their schedule anchor.
        if ((programId == null || programId.isEmpty())
                && (startDate == null || startDate.isEmpty())) {
            startDate = AppClock.today(this).toString();
        }

        // Detect whether existing or new workprogram
        if (programId == null || programId.isEmpty()) {
            isNewProgram = true;
        }

        // Ignore a saved transplant date that is impossible (before start / sowing)
        discardInvalidTransplantDate();

        // Update header
        if (cultivar != null) {
            cultivarNameHeader.setText(cultivar);
            cultivarImageHeader.setImageResource(CultivarImageHelper.getCultivarImageResource(cultivar));
            // Make image circular
            cultivarImageHeader.post(() -> {
                cultivarImageHeader.setClipToOutline(true);
                cultivarImageHeader.setOutlineProvider(new android.view.ViewOutlineProvider() {
                    @Override
                    public void getOutline(android.view.View view, android.graphics.Outline outline) {
                        outline.setOval(0, 0, view.getWidth(), view.getHeight());
                    }
                });
            });
        }
        if (date != null) {
            // Format date according to user preference
            try {
                Date dateObj = sdf.parse(date);
                SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(this);
                String formattedDate = displayFormat.format(dateObj);
                dateHeader.setText("Date: " + formattedDate);
            } catch (ParseException e) {
                dateHeader.setText("Date: " + date);
            }

        }

        if (isNewProgram) {
            dayNumber = adjustedDayNumber > 0
                    ? adjustedDayNumber
                    : calculateDayNumber(startDate, date);
            if (dayNumber > 0 && maturityDays > 0) {
                taskList = getScheduledTasksForDate();
            }
        }

        // Initialize checked tasks array
        checkedTasks = new boolean[taskList.size()];

        // Setup RecyclerView
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setAdapter(taskAdapter);

        // Update task count
        updateTaskCount();
        updateEmptyTaskState();
        applyIntakeSkipNotice();

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }
        
        String userId = currentUser.getUid();
        if (!isNewProgram) {
            taskRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("routineLogs")
                    .child(programId)
                    .child("tasks");
        } else {
            taskRef = null;
        }

        String finalStartDate = startDate;
        
        btnComplete.setOnClickListener(v -> markAllTasksComplete());
        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> markTasksSkipped());
        }
        if (btnMonitor != null) {
            btnMonitor.setOnClickListener(v -> openMonitoring());
            btnMonitor.setEnabled(!isNewProgram);
        }
        if (btnDailyExpenses != null) {
            btnDailyExpenses.setOnClickListener(v -> openDailyExpenses());
            btnDailyExpenses.setEnabled(!isNewProgram);
        }

        applyPreviewLock();
        showProgramInsights();
        setupBottomNavigation();
        loadProgramState(currentUser);
    }

    //Allow preview tasks
    private void applyPreviewLock() {
        if (!previewMode) return;
        if (taskSectionTitle != null) taskSectionTitle.setText(
                "Preview only. Complete the previous day's tasks to unlock this date.");
        View[] actions = {btnComplete, btnSkip, btnMonitor, btnDailyExpenses};
        for (View b : actions) if (b != null) b.setVisibility(View.GONE);
    }

    private void showProgramInsights() {
        if (isNewProgram || programId == null || programId.isEmpty() || dateHeader == null || programInsights == null) return;
        final android.content.Context app = getApplicationContext();
        final String pid = programId;
        final String viewed = date;
        new Thread(() -> {
            java.util.List<String> lines = new ArrayList<>();
            try {
                com.android.tomatoapp.weather.data.WeatherData w =
                        com.android.tomatoapp.core.database.AppDatabase.getInstance(app)
                                .weatherDataDao().getByProgramId(pid);
                if (w != null && w.daysTracked > 0) {
                    lines.add(String.format(Locale.US, "Weather (%d days): avg %.1f°C, rain %.1f mm",
                            w.daysTracked, w.avgTemperature, w.totalPrecipitation));
                }
            } catch (Exception ignored) { }
            try {
                SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                int total = 0, onDay = 0;
                for (org.json.JSONObject e : com.android.tomatoapp.detection.data
                        .DetectionHistoryManager.getHistory(app)) {
                    if (!pid.equals(e.optString("programId", ""))) continue;
                    total++;
                    if (viewed != null && viewed.equals(day.format(new Date(e.optLong("timestamp", 0))))) onDay++;
                }
                if (total > 0) lines.add(String.format(Locale.US,
                        "Disease scans: %d total (%d on this date)", total, onDay));
            } catch (Exception ignored) { }
            if (lines.isEmpty()) return;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                programInsights.setText(android.text.TextUtils.join("\n", lines));
                programInsights.setVisibility(View.VISIBLE);
            });
        }).start();
    }

    /**
     * A transplant date can never be on/before the program start date, or before the
     * sowing date. Such a value leaves Phase 1 with zero days (Land Prep 0%, phase
     * colours shifted). Ongoing/intake programs are exempt: their history is derived.
     */
    private void discardInvalidTransplantDate() {
        if (transplantDate == null || placementDate != null || intakeStartPhase > 1) return;
        try {
            java.time.LocalDate transplant = java.time.LocalDate.parse(transplantDate);
            boolean invalid = false;
            if (startDate != null && !startDate.isEmpty()
                    && !transplant.isAfter(java.time.LocalDate.parse(startDate))) {
                invalid = true;
            }
            if (sowingDate != null && !sowingDate.isEmpty()
                    && transplant.isBefore(java.time.LocalDate.parse(sowingDate))) {
                invalid = true;
            }
            if (invalid) transplantDate = null;
        } catch (RuntimeException ignored) {
            // Leave the value alone if it cannot be parsed.
        }
    }

    private void loadProgramState(FirebaseUser currentUser) {
        if (isNewProgram || programId == null) {
            loadPersistedDailyState(currentUser);
            return;
        }
        FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("workPrograms").child(programId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    cultivar = firstNonEmpty(
                            readNonEmptyString(snapshot, "cultivarName"), cultivar);
                    startDate = firstNonEmpty(
                            readNonEmptyString(snapshot, "startingDate"),
                            readNonEmptyString(snapshot, "plantingStartDate"),
                            startDate);
                    DataSnapshot intake = snapshot.child("intake");
                    DataSnapshot placement = snapshot.child("placement");
                    DataSnapshot placementAnswers = placement.child("answers");
                    seedsConfirmedDate = firstNonEmpty(
                            readNonEmptyString(snapshot, "seedsConfirmedDate"),
                            readNonEmptyString(intake, "seedsConfirmedDate"),
                            readNonEmptyString(placementAnswers, "seedsConfirmedDate"),
                            seedsConfirmedDate);
                    sowingDate = firstNonEmpty(
                            readNonEmptyString(snapshot, "sowingDate"),
                            readNonEmptyString(intake, "sowingDate"),
                            readNonEmptyString(placementAnswers, "sowingDate"),
                            sowingDate);
                    transplantDate = firstNonEmpty(
                            readNonEmptyString(snapshot, "transplantDate"),
                            readNonEmptyString(intake, "transplantDate"),
                            readNonEmptyString(placementAnswers, "transplantDate"),
                            transplantDate);
                    stopDate = firstNonEmpty(
                            readNonEmptyString(snapshot, "stopDate"),
                            readNonEmptyString(intake, "stopDate"),
                            readNonEmptyString(placementAnswers, "stopDate"),
                            stopDate);
                    phase3Date = firstNonEmpty(readNonEmptyString(snapshot, "phase3Date"), phase3Date);
                    phase4Date = firstNonEmpty(readNonEmptyString(snapshot, "phase4Date"), phase4Date);
                    phase5Date = firstNonEmpty(readNonEmptyString(snapshot, "phase5Date"), phase5Date);
                    placementDate = readNonEmptyString(placement, "placementDate");
                    placementDate = firstNonEmpty(
                            placementDate, readNonEmptyString(snapshot, "placementDate"));
                    if (placementDate == null) {
                        Object createdAt = snapshot.child("placement").child("createdAt").getValue();
                        if (createdAt instanceof Number) {
                            placementDate = new java.text.SimpleDateFormat("yyyy-MM-dd",
                                    java.util.Locale.US)
                                    .format(new java.util.Date(((Number) createdAt).longValue()));
                        }
                    }
                    Object phase = snapshot.child("intakeStartPhase").getValue();
                    if (!(phase instanceof Number)) {
                        phase = placement.child("startPhase").getValue();
                    }
                    if (!(phase instanceof Number)) {
                        phase = snapshot.child("placementStartPhase").getValue();
                    }
                    if (!(phase instanceof Number)) {
                        phase = intake.child("startPhase").getValue();
                    }
                    if (phase instanceof Number) intakeStartPhase = ((Number) phase).intValue();
                    discardInvalidTransplantDate();
                    growthHabit = findGrowthHabit(cultivar);
                    maturityDays = findMaturityDays(cultivar);
                    loadPersistedDailyState(currentUser);
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this, "Could not load saved program state: "
                            + error.getMessage(), Toast.LENGTH_LONG).show();
                    loadPersistedDailyState(currentUser);
                });
    }

    private String readOptionalString(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value == null ? null : String.valueOf(value);
    }

    private String readNonEmptyString(DataSnapshot snapshot, String key) {
        String value = readOptionalString(snapshot, key);
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value;
        }
        return null;
    }

    private String findGrowthHabit(String name) {
        if (name == null) return "";
        for (String[] cultivarData : com.android.tomatoapp.workprogram.data.WorkProgramDataHelper.cultivarsData) {
            if (name.equals(cultivarData[0])) return cultivarData[1];
        }
        return "";
    }

    private int findMaturityDays(String name) {
        if (name == null) return 0;
        for (String[] cultivarData : com.android.tomatoapp.workprogram.data.WorkProgramDataHelper.cultivarsData) {
            if (name.equals(cultivarData[0])) {
                try {
                    return Integer.parseInt(cultivarData[3]);
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private void updateTaskCount() {
        int count = getActionTaskCount();
        int checkedCount = getCheckedCount();
        if (taskAdapter != null) taskAdapter.notifyDataSetChanged();
        
        // Update button text
        if (btnComplete != null) {
            if (checkedCount == count && count > 0) {
                btnComplete.setText("Completed!");
                btnComplete.setEnabled(false);
            } else {
                btnComplete.setText("Complete All");
                btnComplete.setEnabled(true);
            }
        }
    }

    private int getCheckedCount() {
        int count = 0;
        if (checkedTasks != null) {
            for (int index = 0; index < checkedTasks.length; index++) {
                if (checkedTasks[index] && isCountedTask(taskList.get(index))) count++;
            }
        }
        return count;
    }

    private int getActionTaskCount() {
        return taskAdapter == null ? 0 : taskAdapter.getActionableTaskCount();
    }

    private boolean areAllTasksChecked() {
        if (checkedTasks == null || checkedTasks.length == 0 || getActionTaskCount() == 0) return false;
        for (int i = 0; i < checkedTasks.length; i++) {
            if (!isCountedTask(taskList.get(i))) continue;
            if (!checkedTasks[i]) return false;
        }
        return true;
    }

    private boolean isCountedTask(TaskModel task) {
        return "MAIN".equals(task.type) || "MAINTENANCE".equals(task.type);
    }

    private void markAllTasksComplete() {
        if (previewMode) return;
        if (isNewProgram) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("date", date);
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, "Task marked complete (new program).", Toast.LENGTH_SHORT).show();
            finish();
        } else if (taskRef != null) {
            taskRef.child(date).setValue("completed")
                    .addOnSuccessListener(unused -> {
                        // Save to local database
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null && programId != null && !taskList.isEmpty()) {
                            // Save first task as representative (all tasks share the same status)
                            TaskModel firstTask = taskList.get(0);
                            LocalDataManager.getInstance(DailyTask.this).saveTaskStatus(
                                    currentUser.getUid(),
                                    programId,
                                    date,
                                    firstTask.taskName,
                                    firstTask.category,
                                    firstTask.iconType,
                                    firstTask.dayNumber,
                                    firstTask.phase,
                                    "completed"
                            );
                        }
                        
                        Toast.makeText(this, "Marked as complete!", Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("date", date);
                        resultIntent.putExtra("programId", programId);
                        resultIntent.putExtra("programStartDate", startDate);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void markTasksSkipped() {
        if (previewMode) return;
        if (isNewProgram) {
            Toast.makeText(this, "Skipping is available after saving the work program.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (taskRef == null) {
            return;
        }

        taskRef.child(date).setValue("skipped")
                .addOnSuccessListener(unused -> {
                    // Save to local database
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null && programId != null && !taskList.isEmpty()) {
                        // Save first task as representative (all tasks share the same status)
                        TaskModel firstTask = taskList.get(0);
                        LocalDataManager.getInstance(DailyTask.this).saveTaskStatus(
                                currentUser.getUid(),
                                programId,
                                date,
                                firstTask.taskName,
                                firstTask.category,
                                firstTask.iconType,
                                firstTask.dayNumber,
                                firstTask.phase,
                                "skipped"
                        );
                    }
                    
                    Toast.makeText(this, "Tasks marked as skipped.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveMilestone(String field, String value) {
        if (previewMode) return;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || programId == null || programId.isEmpty()) return;
        DatabaseReference programRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("workPrograms").child(programId);
        java.util.Map<String, Object> update = new java.util.HashMap<>();
        update.put(field, value);
        programRef.updateChildren(update).addOnSuccessListener(unused -> {
            if ("seedsConfirmedDate".equals(field)) seedsConfirmedDate = value;
            if ("sowingDate".equals(field)) sowingDate = value;
            if ("transplantDate".equals(field)) transplantDate = value;
            if ("stopDate".equals(field)) stopDate = value;
            if (taskRef == null || date == null) {
                finishMilestoneSave();
                return;
            }
            taskRef.child(date).setValue("completed")
                    .addOnSuccessListener(ignored -> finishMilestoneSave())
                    .addOnFailureListener(error -> Toast.makeText(this,
                            "Milestone saved, but the day could not be unlocked: "
                                    + error.getMessage(), Toast.LENGTH_LONG).show());
        }).addOnFailureListener(error -> Toast.makeText(this,
                "Could not record milestone: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void finishMilestoneSave() {
        refreshTaskList();
        Intent result = new Intent();
        result.putExtra("milestoneChanged", true);
        result.putExtra("statusChanged", true);
        result.putExtra("date", date);
        setResult(RESULT_OK, result);
        Toast.makeText(this, "Milestone recorded. The next date is unlocked.",
                Toast.LENGTH_SHORT).show();
    }

    private List<TaskModel> getScheduledTasksForDate() {
        if (startDate == null || date == null || cultivar == null) {
            return new ArrayList<>();
        }
        try {
            java.time.LocalDate planting = java.time.LocalDate.parse(startDate);
            java.time.LocalDate current = java.time.LocalDate.parse(date);
            java.time.LocalDate sowing = sowingDate == null ? null : java.time.LocalDate.parse(sowingDate);
            java.time.LocalDate seeds = seedsConfirmedDate == null ? null : java.time.LocalDate.parse(seedsConfirmedDate);
            java.time.LocalDate transplant = transplantDate == null ? null : java.time.LocalDate.parse(transplantDate);
            java.time.LocalDate stop = stopDate == null ? null : java.time.LocalDate.parse(stopDate);
            java.time.LocalDate placement = placementDate == null ? null : java.time.LocalDate.parse(placementDate);
            java.time.LocalDate phase3 = phase3Date == null ? null : java.time.LocalDate.parse(phase3Date);
            java.time.LocalDate phase4 = phase4Date == null ? null : java.time.LocalDate.parse(phase4Date);
            java.time.LocalDate phase5 = phase5Date == null ? null : java.time.LocalDate.parse(phase5Date);
            transplant = com.android.tomatoapp.workprogram.data.ProgramHistory.effectiveTransplantDate(
                    transplant, placement, intakeStartPhase);
            double area = getIntent().getDoubleExtra("farmAreaHectares", 1.0);
            TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                    planting, area, growthHabit, seeds, sowing, transplant, stop, current,
                    TomatoWorkProgramScheduler.DEFAULT_HECTARES_PER_PLOWING_DAY, 0,
                    phase3, phase4, phase5);
            // Ongoing planting uses the same scheduler as a normal program. Earlier
            // work is represented by onboarding completion state, not a second mode.
            return TomatoWorkProgramScheduler.getTasksForDate(input, current);
        } catch (java.time.format.DateTimeParseException | IllegalArgumentException e) {
            Toast.makeText(this, "Unable to load scheduled tasks for this date.", Toast.LENGTH_SHORT).show();
            return new ArrayList<>();
        }
    }

    private boolean isSeedPromptDate() {
        if (seedsConfirmedDate != null || date == null || startDate == null) return false;
        try {
            java.time.LocalDate viewed = java.time.LocalDate.parse(date);
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            if (viewed.isBefore(start) || viewed.isAfter(AppClock.today(this))) return false;
            long day = java.time.temporal.ChronoUnit.DAYS.between(start, viewed);
            return day >= 0 && day % 3 == 0;
        } catch (RuntimeException error) {
            return false;
        }
    }

    private boolean maybeAskAboutSeeds() {
        if (previewMode || !isSeedPromptDate() || isNewProgram || programId == null) return false;
        new AlertDialog.Builder(this)
                .setTitle("Do you already have seeds?")
                .setMessage("Need help finding seeds? Ask your local agri-supply or municipal agriculture office.")
                .setPositiveButton("Yes", (dialog, which) -> saveMilestone("seedsConfirmedDate", date))
                .setNegativeButton("No, not yet", null)
                .show();
        return true;
    }

    private void maybeAskPhaseSurvey() {
        if (previewMode) return;
        if (isNewProgram || isFinishing() || isDestroyed()) return;
        if (programId == null || programId.isEmpty() || transplantDate == null) return;
        if (date == null) return;
        // final java.time.LocalDate today = AppClock.today(this);
        // final java.time.LocalDate transplant;
        // try {
        //     if (!today.equals(java.time.LocalDate.parse(date))) return;
        //     transplant = java.time.LocalDate.parse(transplantDate);
        // } catch (RuntimeException error) {
        //     return;
        // }
        final java.time.LocalDate realToday = AppClock.today(this);
        final java.time.LocalDate today;      // the date being viewed
        final java.time.LocalDate transplant;
        try {
            today = java.time.LocalDate.parse(date);
            transplant = java.time.LocalDate.parse(transplantDate);
        } catch (RuntimeException error) {
            return;
        }

        final String[] fields = {"phase3Date", "phase4Date", "phase5Date"};
        final int[] phases = {3, 4, 5};
        final int[] earliestDays = {25, 50, 77};
        final int[] estimatedDays = {28, 56, 84};
        final String[] questions = {
                "Have plants begun flowering?",
                "Are flowers present on most plants?",
                "Are fruits starting to mature?"
        };
        final String[] hints = {
                "Also check that one-time tasks are done.",
                null,
                null
        };
        final String[] values = {phase3Date, phase4Date, phase5Date};
        java.util.Map<String, Object> overdue = new java.util.HashMap<>();
        for (int i = 0; i < fields.length; i++) {
            if (hasPhaseDate(values[i])) continue;
            if (i > 0 && !hasPhaseDate(values[i - 1]) && !overdue.containsKey(fields[i - 1])) {
                break;
            }
            java.time.LocalDate estimated = transplant.plusDays(estimatedDays[i]);
            if (realToday.isAfter(estimated.plusDays(14))) {
                overdue.put(fields[i], estimated.toString());
                continue;
            }
            if (!overdue.isEmpty()) {
                savePhaseDate(overdue);
                return;
            }
            java.time.LocalDate earliest = transplant.plusDays(earliestDays[i]);
            if (!today.isBefore(earliest) && !isPhaseSurveySnoozed(phases[i])) {
                final String field = fields[i];
                final int phase = phases[i];
                showSurveyDialog("Field status check", questions[i], hints[i], false, null, null,
                        ignored -> {
                            java.util.Map<String, Object> update = new java.util.HashMap<>();
                            update.put(field, date);
                            savePhaseDate(update);
                        },
                        () -> snoozePhaseSurvey(phase));
            }
            return;
        }
        if (!overdue.isEmpty()) {
            savePhaseDate(overdue);
        }
    }

    private boolean hasPhaseDate(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isPhaseSurveySnoozed(int phase) {
        String stored = getSharedPreferences("phase_survey", MODE_PRIVATE)
                .getString(programId + "_p" + phase, null);
        return date.equals(stored);
    }

    private void snoozePhaseSurvey(int phase) {
        getSharedPreferences("phase_survey", MODE_PRIVATE)
                .edit()
                .putString(programId + "_p" + phase, AppClock.today(this).toString())
                .apply();
    }

    private void savePhaseDate(java.util.Map<String, Object> update) {
        if (previewMode) return;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || programId == null || programId.isEmpty()
                || update == null || update.isEmpty()) {
            return;
        }
        DatabaseReference programRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("workPrograms").child(programId);
        programRef.updateChildren(update).addOnSuccessListener(unused -> {
            if (update.containsKey("phase3Date")) phase3Date = String.valueOf(update.get("phase3Date"));
            if (update.containsKey("phase4Date")) phase4Date = String.valueOf(update.get("phase4Date"));
            if (update.containsKey("phase5Date")) phase5Date = String.valueOf(update.get("phase5Date"));
            Intent result = new Intent();
            result.putExtra("milestoneChanged", true);
            setResult(RESULT_OK, result);
            if (isFinishing() || isDestroyed()) return;
            refreshTaskList();
            maybeAskPhaseSurvey();
        }).addOnFailureListener(error -> Toast.makeText(this,
                "Could not record phase date: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private interface SurveyAnswer {
        void onAnswered(String chosenDate);
    }

    private void showSurveyDialog(String title, String question, String hint, boolean pickDate,
            java.time.LocalDate minDate, java.time.LocalDate maxDate,
            SurveyAnswer onYes, Runnable onNotYet) {
        if (isFinishing() || isDestroyed()) return;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_phase_survey, null);
        TextView titleView = view.findViewById(R.id.surveyTitle);
        TextView questionView = view.findViewById(R.id.surveyQuestion);
        TextView hintView = view.findViewById(R.id.surveyHint);
        TextView dateView = view.findViewById(R.id.surveyDate);
        MaterialButton btnNotYet = view.findViewById(R.id.btnSurveyNotYet);
        MaterialButton btnYes = view.findViewById(R.id.btnSurveyYes);
        titleView.setText(title);
        questionView.setText(question);
        if (hint != null && !hint.isEmpty()) {
            hintView.setText(hint);
            hintView.setVisibility(View.VISIBLE);
        }
        final String[] chosen = {maxDate == null ? null : maxDate.toString()};
        if (pickDate && maxDate != null) {
            dateView.setVisibility(View.VISIBLE);
            dateView.setText("Transplant date\n" + chosen[0]);
            dateView.setOnClickListener(v -> {
                DatePickerDialog picker = new DatePickerDialog(this, (pickerView, year, month, day) -> {
                    chosen[0] = java.time.LocalDate.of(year, month + 1, day).toString();
                    dateView.setText("Transplant date\n" + chosen[0]);
                }, maxDate.getYear(), maxDate.getMonthValue() - 1, maxDate.getDayOfMonth());
                Calendar max = Calendar.getInstance();
                max.set(maxDate.getYear(), maxDate.getMonthValue() - 1, maxDate.getDayOfMonth(), 23, 59, 59);
                max.set(Calendar.MILLISECOND, 999);
                try {
                    picker.getDatePicker().setMaxDate(max.getTimeInMillis());
                    if (minDate != null && !minDate.isAfter(maxDate)) {
                        Calendar min = Calendar.getInstance();
                        min.set(minDate.getYear(), minDate.getMonthValue() - 1, minDate.getDayOfMonth(), 0, 0, 0);
                        min.set(Calendar.MILLISECOND, 0);
                        picker.getDatePicker().setMinDate(min.getTimeInMillis());
                    }
                } catch (RuntimeException ignored) {
                    // Keep the picker usable if a bound is rejected.
                }
                picker.show();
            });
        }
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(view)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        btnNotYet.setOnClickListener(v -> {
            dialog.dismiss();
            if (onNotYet != null) onNotYet.run();
        });
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            if (onYes != null) onYes.onAnswered(chosen[0]);
        });
        dialog.show();
    }

    private void showTransplantLeafDialog() {
        java.time.LocalDate today;
        try {
            today = date != null ? java.time.LocalDate.parse(date) : AppClock.today(this);
        } catch (RuntimeException e) {
            today = AppClock.today(this);
        }

        // Earliest allowed transplant date: the day after the program start, and never
        // before the sowing date. This keeps Phase 1 (Land Prep) from ending up empty.
        java.time.LocalDate min = null;
        try {
            if (startDate != null) min = java.time.LocalDate.parse(startDate).plusDays(1);
            if (sowingDate != null) {
                java.time.LocalDate sow = java.time.LocalDate.parse(sowingDate);
                if (min == null || sow.isAfter(min)) min = sow;
            }
        } catch (RuntimeException ignored) {
            // Fall back to whatever bound was computed so far.
        }

        if (min != null && min.isAfter(today)) {
            Toast.makeText(this, "Transplant can only be recorded after the sowing date.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showSurveyDialog("Work Program Milestone", "Do seedlings have 3-5 true leaves?",
                null, true, min, today,
                chosen -> saveMilestone("transplantDate", chosen),
                null);
    }

    private void loadPersistedDailyState(FirebaseUser currentUser) {
        if (isNewProgram || currentUser == null || programId == null) {
            maybeAskAboutSeeds();
            return;
        }
        if (taskRef == null || date == null || date.isEmpty()) {
            Toast.makeText(this, "Could not restore saved task state.", Toast.LENGTH_LONG).show();
            return;
        }

        taskRef.child(date).get().addOnSuccessListener(taskSnapshot -> {
            dayCompleted = isOnboardingCompletedDate()
                    || "completed".equals(taskSnapshot.getValue(String.class));
            dayNumber = calculateDayNumber(startDate, date);
            if (dayNumber > 0 && maturityDays > 0) {
                refreshTaskList();
            } else {
                updateTaskCount();
                updateEmptyTaskState();
            }
            if (!maybeAskAboutSeeds()) {
                maybeAskPhaseSurvey();
            }
        }).addOnFailureListener(error -> {
            Toast.makeText(this, "Could not restore saved task state: "
                    + error.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private boolean isOnboardingCompletedDate() {
        if (startDate == null || date == null) return false;
        try {
            java.time.LocalDate viewedDate = java.time.LocalDate.parse(date);
            java.time.LocalDate placement = placementDate == null
                    ? null : java.time.LocalDate.parse(placementDate);
            if (com.android.tomatoapp.workprogram.data.ProgramHistory
                    .shouldTreatAsCompleted(viewedDate, placement)) {
                return true;
            }
            if (intakeStartPhase <= 1) return false;
            int phase = TaskSchedule.getPhaseNumber(
                    java.time.LocalDate.parse(startDate),
                    com.android.tomatoapp.workprogram.data.ProgramHistory.effectiveTransplantDate(
                            transplantDate == null ? null : java.time.LocalDate.parse(transplantDate),
                            placementDate == null ? null : java.time.LocalDate.parse(placementDate),
                            intakeStartPhase),
                    stopDate == null ? null : java.time.LocalDate.parse(stopDate),
                    viewedDate,
                    phase3Date == null ? null : java.time.LocalDate.parse(phase3Date),
                    phase4Date == null ? null : java.time.LocalDate.parse(phase4Date),
                    phase5Date == null ? null : java.time.LocalDate.parse(phase5Date));
            return phase > 0 && phase <= intakeStartPhase
                    && (placement == null || viewedDate.isBefore(placement));
        } catch (RuntimeException error) {
            return false;
        }
    }

    private void refreshTaskList() {
        taskList = getScheduledTasksForDate();
        boolean wasCompleted = dayCompleted;
        checkedTasks = new boolean[taskList.size()];
        if (wasCompleted) {
            for (int i = 0; i < taskList.size(); i++) {
                checkedTasks[i] = isCountedTask(taskList.get(i));
            }
        }
        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setItemAnimator(null);
        taskRecyclerView.setAdapter(taskAdapter);
        taskRecyclerView.post(() -> taskRecyclerView.requestLayout());
        updateTaskCount();
        updateEmptyTaskState();
        applyIntakeSkipNotice();
    }

    private void updateEmptyTaskState() {
        boolean empty = taskList.isEmpty() || getActionTaskCount() == 0;
        if (taskSectionTitle != null) {
            taskSectionTitle.setText(empty
                    ? "No tasks unlocked for this date. Record the next milestone when it is due."
                    : "Today's Growth Plan");
        }
        if (btnComplete != null) btnComplete.setEnabled(!empty && getActionTaskCount() > 0);
        if (btnSkip != null) btnSkip.setEnabled(!empty && getActionTaskCount() > 0);

        applyPreviewLock();
    }

    private void applyIntakeSkipNotice() {
        if (intakeStartPhase <= 1 || startDate == null || date == null) return;
        int phase;
        try {
            phase = TaskSchedule.getPhaseNumber(
                    java.time.LocalDate.parse(startDate),
                    transplantDate == null ? null : java.time.LocalDate.parse(transplantDate),
                    stopDate == null ? null : java.time.LocalDate.parse(stopDate),
                    java.time.LocalDate.parse(date),
                    phase3Date == null ? null : java.time.LocalDate.parse(phase3Date),
                    phase4Date == null ? null : java.time.LocalDate.parse(phase4Date),
                    phase5Date == null ? null : java.time.LocalDate.parse(phase5Date));
        } catch (RuntimeException error) {
            return;
        }
        if (phase >= intakeStartPhase) return;
        if (taskSectionTitle != null) {
            taskSectionTitle.setText("Tasks for earlier phases are already marked completed. You can still log expenses for work already done.");
        }
        if (btnComplete != null) btnComplete.setEnabled(false);
        if (btnSkip != null) btnSkip.setEnabled(false);
        if (btnDailyExpenses != null) btnDailyExpenses.setEnabled(!isNewProgram);
    }

    private void openMonitoring() {
        if (isNewProgram || programId == null) {
            Toast.makeText(this, "Save the work program to log monitoring entries.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dayNumber <= 0 || maturityDays <= 0) {
            Toast.makeText(this, "Monitoring unavailable for this date.", Toast.LENGTH_SHORT).show();
            return;
        }
        int phaseNumber;
        try {
            phaseNumber = TaskSchedule.getPhaseNumber(java.time.LocalDate.parse(startDate),
                    transplantDate == null ? null : java.time.LocalDate.parse(transplantDate),
                    stopDate == null ? null : java.time.LocalDate.parse(stopDate),
                    java.time.LocalDate.parse(date),
                    phase3Date == null ? null : java.time.LocalDate.parse(phase3Date),
                    phase4Date == null ? null : java.time.LocalDate.parse(phase4Date),
                    phase5Date == null ? null : java.time.LocalDate.parse(phase5Date));
        } catch (RuntimeException error) {
            Toast.makeText(this, "Monitoring unavailable for this date.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phaseNumber <= 0) return;
        Intent intent = new Intent(this, PlantMonitoringActivity.class);
        intent.putExtra(PlantMonitoringActivity.EXTRA_PROGRAM_ID, programId);
        intent.putExtra(PlantMonitoringActivity.EXTRA_CULTIVAR, cultivar);
        intent.putExtra(PlantMonitoringActivity.EXTRA_PHASE, phaseNumber);
        intent.putExtra(PlantMonitoringActivity.EXTRA_SELECTED_DATE, date);
        startActivity(intent);
    }

    private void openDailyExpenses() {
        if (isNewProgram || programId == null) {
            Toast.makeText(this, "Save the work program to log daily expenses.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cultivar == null || date == null || startDate == null) {
            Toast.makeText(this, "Missing required information.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, DailyExpensesActivity.class);
        intent.putExtra("programId", programId);
        intent.putExtra("cultivar", cultivar);
        intent.putExtra("date", date);
        intent.putExtra("programStartDate", startDate);
        startActivity(intent);
    }

    private java.time.LocalDate parseDateOrNull(String v) {
        try { return v == null || v.isEmpty() ? null : java.time.LocalDate.parse(v); }
        catch (RuntimeException e) { return null; }
    }

    private boolean isMilestoneHistory(TaskModel task) {
        if (task.details != null && (task.details.contains("confirmed on this date")
                || task.details.contains("recorded on this date")
                || task.details.contains("sown on this date"))) {
            return true;
        }
        return "stop-indicator".equals(task.id) && date != null && date.equals(stopDate);
    }

    private void confirmMilestone(TaskModel task) {
        if (previewMode) return;
        if (date == null) {
            Toast.makeText(this, "No date selected.", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("stop-indicator".equals(task.id)) {
            new AlertDialog.Builder(this)
                    .setTitle("Work Program Milestone")
                    .setMessage("Have you harvested or tomato plants dies?")
                    .setPositiveButton("Yes, all fruits are harvested", (dialog, which) -> saveMilestone("stopDate", date))
                    .setNegativeButton("No, not yet", null)
                    .show();
            return;
        }
        String field;
        if ("milestone-seeds".equals(task.id)) {
            field = "seedsConfirmedDate";
        } else if ("milestone-sowing".equals(task.id)) {
            field = "sowingDate";
        } else if ("milestone-transplant".equals(task.id)) {
            showTransplantLeafDialog();
            return;
        } else {
            Toast.makeText(this, "Unknown milestone.", Toast.LENGTH_SHORT).show();
            return;
        }
        saveMilestone(field, date);
    }


    private int calculateDayNumber(String startDate, String currentDate) {
        try {
            Date start = sdf.parse(startDate);
            Date current = sdf.parse(currentDate);
            if (start == null || current == null) return -1;

            long diff = current.getTime() - start.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // RecyclerView Adapter
    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
        private final List<TaskModel> tasks;
        private final List<Object> rows = new ArrayList<>();
        private final java.util.Set<Integer> expandedTasks = new java.util.HashSet<>();

        TaskAdapter(List<TaskModel> tasks) {
            this.tasks = tasks;
            addSection("Milestones", "MILESTONE");
            addSection("Main tasks", "MAIN");
            addSection("Notes", "NOTE");
            addSection("Maintenance tasks", "MAINTENANCE");
        }

        private void addSection(String title, String... types) {
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < tasks.size(); i++) {
                for (String type : types) {
                    if (type.equals(tasks.get(i).type)) {
                        indexes.add(i);
                        break;
                    }
                }
            }
            if (!indexes.isEmpty()) {
                int count = indexes.size();
                rows.add(title + " (" + count + ")");
                rows.addAll(indexes);
            }
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(viewType == 0 ? R.layout.item_task_section_header : R.layout.item_task,
                            parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            return rows.get(position) instanceof String ? 0 : 1;
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            if (rows.get(position) instanceof String) {
                String title = (String) rows.get(position);
                holder.sectionTitle.setText(title);
                boolean isAction = title.startsWith("Main tasks")
                        || title.startsWith("Maintenance tasks");
                holder.sectionCount.setVisibility(isAction ? View.VISIBLE : View.GONE);
                if (isAction) {
                    // FIX: "Maintenance tasks" also starts with "Main", so match the full prefix.
                    String sectionType = title.startsWith("Main tasks") ? "MAIN" : "MAINTENANCE";
                    int done = 0;
                    int all = 0;
                    for (int i = 0; i < tasks.size(); i++) {
                        if (sectionType.equals(tasks.get(i).type)) {
                            all++;
                            if (checkedTasks[i]) done++;
                        }
                    }
                    holder.sectionCount.setText(done + "/" + all + " completed");
                }
                return;
            }
            int taskIndex = (Integer) rows.get(position);
            TaskModel task = tasks.get(taskIndex);
            holder.taskName.setText(task.taskName);
            String subtext = task.category + " • " + task.phase;
            if (task.startDate != null && task.endDate != null
                    && !task.startDate.equals(task.endDate)) {
                subtext += " • " + task.startDate + " to " + task.endDate;
            }
            if (task.details != null && !task.details.isEmpty()) {
                subtext += "\n" + task.details;
            }
            final boolean hasGuide = com.android.tomatoapp.task.data.TaskKnowledgeBase.has(task.id);
        if (hasGuide) {
            if (expandedTasks.contains(taskIndex)) {
                String guide = com.android.tomatoapp.task.data.TaskKnowledgeBase.guide(task.id,
                        getIntent().getDoubleExtra("farmAreaHectares", 1.0), growthHabit,
                        parseDateOrNull(date), parseDateOrNull(transplantDate), parseDateOrNull(sowingDate));
                if (guide != null) subtext += "\n\n" + guide;
            } else {
                subtext += "\n▸ Tap for guide";
            }
        }
        holder.taskSubtext.setText(subtext);
        if (hasGuide) {
            holder.itemView.setOnClickListener(v -> {
                if (!expandedTasks.remove(taskIndex)) expandedTasks.add(taskIndex);
                notifyItemChanged(holder.getAdapterPosition());
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
            
            // Set icon based on task type
            int iconDrawable = getIconDrawable(task.iconType);
            holder.taskIcon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), iconDrawable));
            
            boolean isNote = "NOTE".equals(task.type);
            boolean isMilestone = "MILESTONE".equals(task.type);
            holder.milestoneAction.setVisibility(isMilestone && !previewMode && !isMilestoneHistory(task)
                    ? View.VISIBLE : View.GONE);
            holder.milestoneAction.setOnClickListener(v -> confirmMilestone(task));
            holder.taskCheckbox.setEnabled(!isNote && !isMilestone && !dayCompleted && !previewMode);
            holder.taskCheckbox.setVisibility(isNote || isMilestone ? View.GONE : View.VISIBLE);
            holder.taskCheckbox.setChecked(!isNote && !isMilestone && checkedTasks[taskIndex]);
            
            // Set checkbox listener
            holder.taskCheckbox.setOnCheckedChangeListener(null); // Prevent triggering during binding
            holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isNote || isMilestone || dayCompleted || previewMode) return;
                checkedTasks[taskIndex] = isChecked;
                updateTaskCount();
                
                // Auto-complete when all tasks are checked
                if (areAllTasksChecked() && !isNewProgram && taskRef != null) {
                    markAllTasksComplete();
                }
            });
            
            // Update text style if checked
            if (checkedTasks[taskIndex]) {
                holder.taskName.setAlpha(0.6f);
                holder.taskSubtext.setAlpha(0.6f);
            } else {
                holder.taskName.setAlpha(1.0f);
                holder.taskSubtext.setAlpha(1.0f);
            }
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        private int getIconDrawable(String iconType) {
            switch (iconType) {
                case "land":
                    return R.drawable.task_icon_land;
                case "water":
                    return R.drawable.task_icon_water;
                case "fertilizer":
                    return R.drawable.task_icon_fertilizer;
                case "pest":
                    return R.drawable.task_icon_pest;
                case "harvest":
                    return R.drawable.task_icon_harvest;
                default:
                    return R.drawable.circle_green;
            }
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            View taskIcon;
            TextView taskName, taskSubtext;
            TextView sectionTitle;
            TextView sectionCount;
            MaterialCheckBox taskCheckbox;
            MaterialButton milestoneAction;

            TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                taskIcon = itemView.findViewById(R.id.taskIcon);
                taskName = itemView.findViewById(R.id.taskName);
                taskSubtext = itemView.findViewById(R.id.taskSubtext);
                taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
                milestoneAction = itemView.findViewById(R.id.milestoneAction);
                sectionTitle = itemView.findViewById(R.id.sectionTitle);
                sectionCount = itemView.findViewById(R.id.taskCountText);
            }

        }

        int getActionableTaskCount() {
            int count = 0;
            for (Object row : rows) {
                if (row instanceof Integer && isCountedTask(tasks.get((Integer) row))) {
                    count++;
                }
            }
            return count;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
