package com.android.tomatoapp.task.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.android.tomatoapp.detection.data.DetectionHistoryManager;
import com.android.tomatoapp.financial.ui.DailyExpensesActivity;
import com.android.tomatoapp.monitoring.ui.PlantMonitoringActivity;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.task.data.ScheduledTask;
import com.android.tomatoapp.task.data.TaskEntity;
import com.android.tomatoapp.task.data.TaskModel;
import com.android.tomatoapp.task.data.TaskSchedule;
import com.android.tomatoapp.task.data.TaskTemplate;
import com.android.tomatoapp.task.data.TomatoWorkProgramScheduler;
import com.android.tomatoapp.common.utils.AppClock;
import com.android.tomatoapp.workprogram.data.ProgramHistory;

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
    private boolean programHasPlantCount;
    private String stopDate;
    private String phase3Date, phase4Date, phase5Date;
    private String placementDate;
    private int intakeStartPhase = 1;
    private DatabaseReference taskRef;
    private boolean dayCompleted;
    private boolean previewMode;
    private TextView missedDayBanner;
    private String dayStatus;
    private volatile Double fetchedPrecip;
    private boolean scanLinked;
    private final List<OverdueTask> overdueTasks = new ArrayList<>();

    private static final String[] SKIP_REASONS = {
            "Rain / bad weather",
            "Saw signs of disease or pests",
            "No labor",
            "No materials or money",
            "Other"
    };


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
        missedDayBanner = findViewById(R.id.missedDayBanner);

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

    @Override
    protected void onResume() {
        super.onResume();

        com.google.firebase.auth.FirebaseUser u =
                com.google.firebase.auth.FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (u != null) {
            com.android.tomatoapp.core.network.LocalDataManager
                    .getInstance(this)
                    .processSyncQueue(this, u.getUid());
            refreshScanLinked();
        }
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
                    programHasPlantCount = hasPositivePlantCount(snapshot.child("plantCount").getValue())
                            || hasPositivePlantCount(intake.child("plantCount").getValue());
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
                        saveLateMeta();

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
        if (hasUnrecordedMilestone()) {
            Toast.makeText(this, "Record the milestone first", Toast.LENGTH_SHORT).show();
            return;
        }
        showSkipDialog();
    }

    private boolean hasUnrecordedMilestone() {
        for (TaskModel task : taskList) {
            if ("MILESTONE".equals(task.type) && !isMilestoneHistory(task)) return true;
        }
        return false;
    }

    private void showSkipDialog() {
        fetchedPrecip = null;
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        final android.widget.RadioGroup reasons = new android.widget.RadioGroup(this);
        reasons.setOrientation(android.widget.RadioGroup.VERTICAL);
        for (String reason : SKIP_REASONS) {
            android.widget.RadioButton button = new android.widget.RadioButton(this);
            button.setId(View.generateViewId());
            button.setText(reason);
            button.setTextColor(android.graphics.Color.BLACK);
            reasons.addView(button);
        }
        layout.addView(reasons);

        final TextView rainHint = new TextView(this);
        rainHint.setTextColor(android.graphics.Color.BLACK);
        rainHint.setVisibility(View.GONE);
        rainHint.setPadding(0, pad / 2, 0, 0);
        layout.addView(rainHint);

        final EditText noteInput = new EditText(this);
        noteInput.setHint("Note (optional)");
        noteInput.setTextColor(android.graphics.Color.BLACK);
        noteInput.setVisibility(View.GONE);
        layout.addView(noteInput);
        reasons.setOnCheckedChangeListener((group, checkedId) -> {
            int index = group.indexOfChild(group.findViewById(checkedId));
            noteInput.setVisibility(index == SKIP_REASONS.length - 1 ? View.VISIBLE : View.GONE);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Skip tasks")
                .setView(layout)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(shown -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                int checkedId = reasons.getCheckedRadioButtonId();
                if (checkedId == -1) {
                    Toast.makeText(this, "Select a reason", Toast.LENGTH_SHORT).show();
                    return;
                }
                int index = reasons.indexOfChild(reasons.findViewById(checkedId));
                String reason = SKIP_REASONS[Math.max(0, Math.min(index, SKIP_REASONS.length - 1))];
                String note = index == SKIP_REASONS.length - 1
                        ? noteInput.getText().toString().trim() : "";
                dialog.dismiss();
                commitSkip(reason, note);
            });
            fetchSkipWeather(reasons, rainHint, dialog);
        });
        dialog.show();
    }

    private void fetchSkipWeather(android.widget.RadioGroup reasons, TextView rainHint, AlertDialog dialog) {
        if (!LocalDataManager.isOnline(this) || date == null) return;
        final String viewed = date;
        new Thread(() -> {
            Double mm = fetchPrecipitationMm(viewed);
            if (mm == null) return;
            fetchedPrecip = mm;
            if (mm < 1.0) return;
            final String hint = "Rain recorded: " + formatMm(mm) + " mm";
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || !dialog.isShowing()) return;
                if (reasons.getCheckedRadioButtonId() == -1 && reasons.getChildCount() > 0) {
                    reasons.check(reasons.getChildAt(0).getId());
                }
                rainHint.setText(hint);
                rainHint.setVisibility(View.VISIBLE);
            });
        }).start();
    }

    private Double fetchPrecipitationMm(String day) {
        android.content.SharedPreferences wp = getSharedPreferences("WeatherPref", MODE_PRIVATE);
        double lat = Double.longBitsToDouble(wp.getLong("lat", Double.doubleToRawLongBits(13.8855)));
        double lon = Double.longBitsToDouble(wp.getLong("lon", Double.doubleToRawLongBits(122.2909)));
        boolean past;
        try {
            past = java.time.LocalDate.parse(day).isBefore(AppClock.today(this));
        } catch (RuntimeException error) {
            return null;
        }
        String base = past
                ? "https://archive-api.open-meteo.com/v1/archive"
                : "https://api.open-meteo.com/v1/forecast";
        String url = String.format(Locale.US,
                "%s?latitude=%f&longitude=%f&daily=precipitation_sum&timezone=auto&start_date=%s&end_date=%s",
                base, lat, lon, day, day);
        java.net.HttpURLConnection conn = null;
        try {
            conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != 200) return null;
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
            reader.close();
            org.json.JSONArray sums = new org.json.JSONObject(body.toString())
                    .getJSONObject("daily").getJSONArray("precipitation_sum");
            if (sums.length() == 0 || sums.isNull(0)) return null;
            return sums.getDouble(0);
        } catch (Exception error) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String formatMm(double mm) {
        if (Math.abs(mm - Math.rint(mm)) < 0.05) return String.valueOf((int) Math.rint(mm));
        return String.format(Locale.US, "%.1f", mm);
    }

    private void commitSkip(String reason, String note) {
        if (taskRef == null || date == null) return;
        taskRef.child(date).setValue("skipped")
                .addOnSuccessListener(unused -> {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null && programId != null && !taskList.isEmpty()) {
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
                    saveSkipMeta(reason, note);
                    Toast.makeText(this, "Tasks marked as skipped.", Toast.LENGTH_SHORT).show();
                    if ("Saw signs of disease or pests".equals(reason)) {
                        new AlertDialog.Builder(this)
                                .setMessage("Scan a plant now?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    openMonitoring();
                                    finish();
                                })
                                .setNegativeButton("Later", (dialog, which) -> finish())
                                .setCancelable(false)
                                .show();
                    } else {
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveSkipMeta(String reason, String note) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || programId == null || date == null) return;
        final String uid = currentUser.getUid();
        final Double precip = fetchedPrecip;
        final String weatherNote = precip == null ? null : "rain " + formatMm(precip) + " mm";
        final boolean linked = scanLinked;
        new Thread(() -> {
            TaskEntity existing = findLocalTask(uid);
            LocalDataManager.getInstance(DailyTask.this).saveTaskMeta(
                    uid,
                    programId,
                    date,
                    reason,
                    note,
                    false,
                    weatherNote != null ? weatherNote : (existing == null ? null : existing.weatherNote),
                    linked || (existing != null && existing.scanLinked));
        }).start();
    }

    private void saveLateMeta() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || programId == null || date == null) return;
        try {
            if (!java.time.LocalDate.parse(date).isBefore(AppClock.today(this))) return;
        } catch (RuntimeException error) {
            return;
        }
        final String uid = currentUser.getUid();
        final boolean linked = scanLinked;
        new Thread(() -> {
            TaskEntity existing = findLocalTask(uid);
            LocalDataManager.getInstance(DailyTask.this).saveTaskMeta(
                    uid,
                    programId,
                    date,
                    existing == null ? null : existing.skipReason,
                    existing == null ? null : existing.skipNote,
                    true,
                    existing == null ? null : existing.weatherNote,
                    linked || (existing != null && existing.scanLinked));
        }).start();
    }

    private TaskEntity findLocalTask(String uid) {
        if (uid == null || programId == null || date == null) return null;
        for (TaskEntity task : LocalDataManager.getInstance(this).getTasksFromLocal(uid, programId)) {
            if (date.equals(task.dateKey)) return task;
        }
        return null;
    }

    private void refreshScanLinked() {
        if (previewMode || isNewProgram || programId == null || date == null) return;
        final String pid = programId;
        final String viewed = date;
        final android.content.Context app = getApplicationContext();
        new Thread(() -> {
            boolean linked = false;
            try {
                SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                for (org.json.JSONObject entry : DetectionHistoryManager.getHistory(app)) {
                    if (!pid.equals(entry.optString("programId", ""))) continue;
                    if (viewed.equals(day.format(new Date(entry.optLong("timestamp", 0))))) {
                        linked = true;
                        break;
                    }
                }
            } catch (Exception ignored) { }
            if (!linked) return;
            scanLinked = true;
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;
            TaskEntity existing = findLocalTask(currentUser.getUid());
            LocalDataManager.getInstance(DailyTask.this).saveTaskMeta(
                    currentUser.getUid(),
                    pid,
                    viewed,
                    existing == null ? null : existing.skipReason,
                    existing == null ? null : existing.skipNote,
                    existing != null && existing.late,
                    existing == null ? null : existing.weatherNote,
                    true);
        }).start();
    }

    private void saveMilestone(String field, String value) {
        if (previewMode) return;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || programId == null || programId.isEmpty()) return;

        java.util.Map<String, Object> update = new java.util.HashMap<>();
        update.put(field, value);

        LocalDataManager.getInstance(this)
        .updateProgramFields(currentUser.getUid(), programId, update)
        .addOnSuccessListener(unused -> {
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
            java.time.LocalDate current = java.time.LocalDate.parse(date);
            // Ongoing planting uses the same scheduler as a normal program. Earlier
            // work is represented by onboarding completion state, not a second mode.
            return TomatoWorkProgramScheduler.getTasksForDate(schedulerInput(current), current);
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

        LocalDataManager.getInstance(this)
        .updateProgramFields(currentUser.getUid(), programId, update)
        .addOnSuccessListener(unused -> {
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
                chosen -> {
                    saveMilestone("transplantDate", chosen);
                    askPlantCount();
                },
                null);
    }

    private void askPlantCount() {
        if (previewMode) return;
        if (programHasPlantCount) return;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || programId == null || programId.isEmpty()) return;

        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setTextColor(android.graphics.Color.BLACK);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("How many plants did you transplant?")
                .setMessage("An estimate is fine. You can change it later in Milestones.")
                .setView(input)
                .setPositiveButton("Save", null)
                .setNegativeButton("Later", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            long count;
            try {
                count = Long.parseLong(input.getText().toString().trim());
                if (count <= 0) throw new NumberFormatException();
            } catch (NumberFormatException error) {
                Toast.makeText(this, "Enter a whole number greater than 0.", Toast.LENGTH_SHORT).show();
                return;
            }
            java.util.Map<String, Object> update = new java.util.HashMap<>();
            update.put("plantCount", count);
            LocalDataManager.getInstance(this)
                    .updateProgramFields(currentUser.getUid(), programId, update)
                    .addOnSuccessListener(unused -> {
                        programHasPlantCount = true;
                        dialog.dismiss();
                    })
                    .addOnFailureListener(error -> Toast.makeText(this,
                            "Could not save plant count: " + error.getMessage(),
                            Toast.LENGTH_LONG).show());
        });
    }

    private boolean hasPositivePlantCount(Object value) {
        if (value instanceof Number) return ((Number) value).longValue() > 0;
        if (value == null) return false;
        try {
            return Long.parseLong(String.valueOf(value).trim()) > 0;
        } catch (NumberFormatException error) {
            return false;
        }
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
            dayStatus = taskSnapshot.getValue(String.class);
            dayCompleted = isOnboardingCompletedDate() || "completed".equals(dayStatus);
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
        loadOverdueTasks();
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
        updateMissedBanner();
    }

    private void updateMissedBanner() {
        if (missedDayBanner == null) return;
        boolean show = !previewMode && isViewedDateMissed();
        missedDayBanner.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            missedDayBanner.setText("This day was missed. Complete it late or skip it with a reason.");
        }
    }

    private boolean isViewedDateMissed() {
        if (previewMode || date == null || dayCompleted) return false;
        if ("completed".equals(dayStatus) || "skipped".equals(dayStatus)) return false;
        try {
            java.time.LocalDate viewed = java.time.LocalDate.parse(date);
            java.time.LocalDate today = AppClock.today(this);
            int phase = phaseNumber(viewed);
            return ProgramHistory.isEligibleForMissed(
                    viewed, parseDateOrNull(placementDate), today, phase, intakeStartPhase,
                    getActionTaskCount() > 0);
        } catch (RuntimeException error) {
            return false;
        }
    }

    private int phaseNumber(java.time.LocalDate day) {
        if (day == null || startDate == null) return 0;
        try {
            return TaskSchedule.getPhaseNumber(
                    java.time.LocalDate.parse(startDate),
                    effectiveTransplant(),
                    parseDateOrNull(stopDate),
                    day,
                    parseDateOrNull(phase3Date),
                    parseDateOrNull(phase4Date),
                    parseDateOrNull(phase5Date));
        } catch (RuntimeException error) {
            return 0;
        }
    }

    private java.time.LocalDate effectiveTransplant() {
        return ProgramHistory.effectiveTransplantDate(
                parseDateOrNull(transplantDate), parseDateOrNull(placementDate), intakeStartPhase);
    }

    private void loadOverdueTasks() {
        overdueTasks.clear();
        if (taskAdapter != null) taskAdapter.setOverdue(overdueTasks);
        if (previewMode || isNewProgram || programId == null || date == null || startDate == null) return;
        java.time.LocalDate today = AppClock.today(this);
        java.time.LocalDate viewed;
        try {
            viewed = java.time.LocalDate.parse(date);
        } catch (RuntimeException error) {
            return;
        }
        if (!viewed.equals(today)) return;
        final String uid = FirebaseAuth.getInstance().getCurrentUser() == null
                ? null : FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String pid = programId;
        final DatabaseReference tasks = taskRef;
        new Thread(() -> {
            java.util.Map<String, String> statuses = new java.util.HashMap<>();
            if (uid != null) {
                for (TaskEntity task : LocalDataManager.getInstance(DailyTask.this).getTasksFromLocal(uid, pid)) {
                    if (task.dateKey != null && task.status != null) {
                        statuses.put(task.dateKey, task.status);
                    }
                }
            }
            if (LocalDataManager.isOnline(DailyTask.this) && tasks != null) {
                try {
                    DataSnapshot snapshot = com.google.android.gms.tasks.Tasks.await(tasks.get());
                    java.util.Map<String, String> remote = new java.util.HashMap<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String key = child.getKey();
                        String status = child.getValue(String.class);
                        if (key != null && status != null) remote.put(key, status);
                    }
                    for (java.util.Map.Entry<String, String> entry : remote.entrySet()) {
                        statuses.put(entry.getKey(), entry.getValue());
                    }
                } catch (Exception ignored) { }
            }
            List<OverdueTask> late = collectOverdue(today, statuses);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                overdueTasks.clear();
                overdueTasks.addAll(late);
                if (taskAdapter != null) taskAdapter.setOverdue(overdueTasks);
            });
        }).start();
    }

    private List<OverdueTask> collectOverdue(java.time.LocalDate today,
                                             java.util.Map<String, String> statuses) {
        List<OverdueTask> late = new ArrayList<>();
        java.time.LocalDate start;
        try {
            start = java.time.LocalDate.parse(startDate);
        } catch (RuntimeException error) {
            return late;
        }
        java.time.LocalDate placement = parseDateOrNull(placementDate);
        java.util.LinkedHashMap<String, ScheduledTask> carry = new java.util.LinkedHashMap<>();
        for (java.time.LocalDate day = start; day.isBefore(today); day = day.plusDays(1)) {
            String status = statuses.get(day.toString());
            if ("completed".equals(status) || "skipped".equals(status)) {
                if (!"skipped".equals(status)) continue;
            }
            boolean skipped = "skipped".equals(status);
            boolean missed = !skipped && !"completed".equals(status)
                    && ProgramHistory.isEligibleForMissed(
                    day, placement, today, phaseNumber(day), intakeStartPhase,
                    scheduledCountable(day) > 0);
            if (!missed && !skipped) continue;
            for (ScheduledTask task : oneTimeMainTasks(day)) {
                if (task.id != null && !carry.containsKey(task.id)) carry.put(task.id, task);
            }
        }
        java.time.LocalDate transplant = effectiveTransplant();
        int todayPhase = phaseNumber(today);
        for (ScheduledTask task : carry.values()) {
            if (task.endDate == null || !today.isAfter(task.endDate)) continue;
            if (windowCompleted(task, statuses, placement)) continue;
            if (todayPhase <= 0 || todayPhase > task.phase) continue;
            if (todayPhase != task.phase) continue;
            String detail = "Late";
            if (transplant != null && task.startDate != null) {
                long from = java.time.temporal.ChronoUnit.DAYS.between(transplant, task.startDate);
                long to = java.time.temporal.ChronoUnit.DAYS.between(transplant, task.endDate);
                long now = java.time.temporal.ChronoUnit.DAYS.between(transplant, today);
                detail = detail + "\nWas due DAT " + from + "-" + to + ", today is DAT " + now;
            }
            late.add(new OverdueTask(task.title, detail));
        }
        return late;
    }

    private boolean windowCompleted(ScheduledTask task, java.util.Map<String, String> statuses,
                                    java.time.LocalDate placement) {
        if (task.startDate == null || task.endDate == null) return false;
        for (java.time.LocalDate day = task.startDate; !day.isAfter(task.endDate); day = day.plusDays(1)) {
            if (ProgramHistory.shouldTreatAsCompleted(day, placement)) return true;
            if ("completed".equals(statuses.get(day.toString()))) return true;
        }
        return false;
    }

    private int scheduledCountable(java.time.LocalDate day) {
        int count = 0;
        try {
            TomatoWorkProgramScheduler.Input input = schedulerInput(day);
            for (TaskModel task : TomatoWorkProgramScheduler.getTasksForDate(input, day)) {
                if ("MAIN".equals(task.type) || "MAINTENANCE".equals(task.type)) count++;
            }
        } catch (RuntimeException ignored) { }
        return count;
    }

    private List<ScheduledTask> oneTimeMainTasks(java.time.LocalDate day) {
        List<ScheduledTask> found = new ArrayList<>();
        try {
            for (ScheduledTask task : TomatoWorkProgramScheduler.generate(schedulerInput(day), day)) {
                if (!task.occursOn(day)) continue;
                if (task.type != TaskTemplate.Type.MAIN) continue;
                if (task.frequencyDays != 0) continue;
                found.add(task);
            }
        } catch (RuntimeException ignored) { }
        return found;
    }

    private TomatoWorkProgramScheduler.Input schedulerInput(java.time.LocalDate current) {
        java.time.LocalDate planting = java.time.LocalDate.parse(startDate);
        java.time.LocalDate sowing = parseDateOrNull(sowingDate);
        java.time.LocalDate seeds = parseDateOrNull(seedsConfirmedDate);
        java.time.LocalDate stop = parseDateOrNull(stopDate);
        double area = getIntent().getDoubleExtra("farmAreaHectares", 1.0);
        return new TomatoWorkProgramScheduler.Input(
                planting, area, growthHabit, seeds, sowing, effectiveTransplant(), stop, current,
                TomatoWorkProgramScheduler.DEFAULT_HECTARES_PER_PLOWING_DAY, 0,
                parseDateOrNull(phase3Date), parseDateOrNull(phase4Date), parseDateOrNull(phase5Date));
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
        private final List<OverdueTask> overdue = new ArrayList<>();
        private final java.util.Set<Integer> expandedTasks = new java.util.HashSet<>();

        TaskAdapter(List<TaskModel> tasks) {
            this.tasks = tasks;
            rebuildRows();
        }

        void setOverdue(List<OverdueTask> items) {
            overdue.clear();
            if (items != null) overdue.addAll(items);
            rebuildRows();
            notifyDataSetChanged();
        }

        private void rebuildRows() {
            rows.clear();
            if (!overdue.isEmpty()) {
                rows.add("Overdue (" + overdue.size() + ")");
                rows.addAll(overdue);
            }
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
            if (rows.get(position) instanceof OverdueTask) {
                OverdueTask item = (OverdueTask) rows.get(position);
                holder.taskName.setText(item.name);
                holder.taskSubtext.setText(item.detail);
                holder.taskIcon.setBackground(ContextCompat.getDrawable(
                        holder.itemView.getContext(), R.drawable.circle_green));
                holder.milestoneAction.setVisibility(View.GONE);
                holder.taskCheckbox.setVisibility(View.GONE);
                holder.taskCheckbox.setOnCheckedChangeListener(null);
                holder.itemView.setOnClickListener(null);
                holder.taskName.setAlpha(1.0f);
                holder.taskSubtext.setAlpha(1.0f);
                return;
            }
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

    private static final class OverdueTask {
        final String name;
        final String detail;

        OverdueTask(String name, String detail) {
            this.name = name;
            this.detail = detail;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
