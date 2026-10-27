package com.android.tomatoapp.workprogram.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.android.tomatoapp.R;
import com.android.tomatoapp.auth.ui.Login;
import com.android.tomatoapp.common.managers.CompletedDecorator;
import com.android.tomatoapp.common.managers.MissedDecorator;
import com.android.tomatoapp.common.managers.SkippedDecorator;
import com.android.tomatoapp.common.utils.CultivarImageHelper;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.financial.ui.CurrentExpensesActivity;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.task.data.TaskEntity;
import com.android.tomatoapp.task.data.TaskModel;
import com.android.tomatoapp.task.data.TomatoWorkProgramScheduler;
import com.android.tomatoapp.task.ui.DailyTask;
import com.android.tomatoapp.weather.data.WeatherDataCollector;
import com.android.tomatoapp.workprogram.data.WorkProgramDataHelper;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;

public class Workprogram extends BaseBottomNavActivity {

    private static final String TAG = "Workprogram";
    private static final int DAILY_TASK_REQUEST = 4101;

    private View cultivarCard;
    private Spinner cultivarSpinner;
    private EditText landAreaInput;
    private DatePicker startDatePicker;
    private Button btnSubmitForm;
    private MaterialCalendarView calendarView;
    
    // New UI elements
    private com.google.android.material.card.MaterialCardView headerCard;
    private ImageView cultivarImage;
    private TextView cultivarNameText;
    private TextView startDateText;
    private View calendarHeader;       
    private ImageView headerCultivarImage;
    private TextView headerCultivarName;
    private TextView headerStartDate;
    private View taskWarningBanner;     
    private MaterialButton btnCurrentExpenses;
    private MaterialButton btnMilestones;
    private TextView summaryDaysActive;
    private TextView summaryExpenses;
    private TextView summaryTasksDue;
    private TextView seasonTagText;
    private TextView cultivarEmoji;

    // Progress card views
    private com.google.android.material.card.MaterialCardView progressCard;
    private android.widget.ProgressBar progressBarOverall;
    private android.widget.ProgressBar progressPhase1, progressPhase2, progressPhase3, progressPhase4, progressPhase5;
    private TextView progressPercent, progressDayLabel;
    private TextView progressPhase1Pct, progressPhase2Pct, progressPhase3Pct, progressPhase4Pct, progressPhase5Pct;
    private TextView progressStatCompleted, progressStatMissed, progressStatSkipped;
    private android.widget.LinearLayout progressDetailsSection;
    private android.widget.ImageView progressToggleBtn;
    private boolean progressExpanded = false;

    private String selectedCultivar = "";
    private String selectedDate = "";
    private String landArea = "";
    private String programStartDate = "";
    private String programId = "";
    private String seedsConfirmedDate;
    private String sowingDate;
    private String transplantDate;
    private String stopDate;
    private double farmAreaHectares = 1.0;

    private DatabaseReference dbRef;
    private DatabaseReference logsRef;
    private String userId;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    // Weather data collection
    private static final String WEATHER_PREF = "WeatherPref";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";

    private HashSet<CalendarDay> completedDates = new HashSet<>();
    private HashSet<CalendarDay> missedDates = new HashSet<>();
    private HashSet<CalendarDay> skippedDates = new HashSet<>();
    private HashSet<CalendarDay> accessibleDates = new HashSet<>(); // Dates that can be clicked


    private final String[][] cultivarsData = {
            {"Improved KS Apollo", "Semi-determinate", "85", "105"},
            {"Diamante max", "Semi-determinate", "85", "105"},
            {"Jewel f1", "Determinate", "85", "105"},
            {"Marimar", "Determinate", "85", "105"},
            {"Apollo", "Semi-determinate", "85", "105"}
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workprogram);

        // Bottom Navigation setup
        setupBottomNavigation();

        // Views - only bind views that actually exist in the redesigned layout
        cultivarCard   = findViewById(R.id.floatingFormCard); // dummy <View>
        calendarView   = findViewById(R.id.CalendarView);

        // New UI elements (real views in redesigned layout)
        headerCard         = findViewById(R.id.headerCard);
        cultivarImage      = findViewById(R.id.cultivarImage);
        cultivarNameText   = findViewById(R.id.cultivarNameText);
        startDateText      = findViewById(R.id.startDateText);
        calendarHeader     = findViewById(R.id.calendarHeader);      // dummy <TextView>
        headerCultivarImage = findViewById(R.id.headerCultivarImage); // dummy <ImageView>
        headerCultivarName  = findViewById(R.id.headerCultivarName);  // dummy <TextView>
        headerStartDate     = findViewById(R.id.headerStartDate);     // dummy <TextView>
        taskWarningBanner   = findViewById(R.id.taskWarningBanner);  // dummy <LinearLayout>
        btnCurrentExpenses  = findViewById(R.id.btnCurrentExpenses);
        btnMilestones = findViewById(R.id.btnMilestones);
        summaryDaysActive   = findViewById(R.id.summaryDaysActive);
        summaryExpenses     = findViewById(R.id.summaryExpenses);
        summaryTasksDue      = findViewById(R.id.summaryTasksDue);
        seasonTagText       = findViewById(R.id.seasonTagText);
        cultivarEmoji       = findViewById(R.id.cultivarEmoji);

        // Progress card
        progressCard         = findViewById(R.id.progressCard);
        progressBarOverall   = findViewById(R.id.progressBarOverall);
        progressPhase1       = findViewById(R.id.progressPhase1);
        progressPhase2       = findViewById(R.id.progressPhase2);
        progressPhase3       = findViewById(R.id.progressPhase3);
        progressPhase4       = findViewById(R.id.progressPhase4);
        progressPhase5       = findViewById(R.id.progressPhase5);
        progressPercent      = findViewById(R.id.progressPercent);
        progressDayLabel     = findViewById(R.id.progressDayLabel);
        progressPhase1Pct    = findViewById(R.id.progressPhase1Pct);
        progressPhase2Pct    = findViewById(R.id.progressPhase2Pct);
        progressPhase3Pct    = findViewById(R.id.progressPhase3Pct);
        progressPhase4Pct    = findViewById(R.id.progressPhase4Pct);
        progressPhase5Pct    = findViewById(R.id.progressPhase5Pct);
        progressStatCompleted = findViewById(R.id.progressStatCompleted);
        progressStatMissed    = findViewById(R.id.progressStatMissed);
        progressStatSkipped   = findViewById(R.id.progressStatSkipped);
        progressDetailsSection = findViewById(R.id.progressDetailsSection);
        progressToggleBtn      = findViewById(R.id.progressToggleBtn);

        // Form fields (bound to dummies if not in form path)
        cultivarSpinner = findViewById(R.id.cultivarSpinner);
        landAreaInput   = findViewById(R.id.landAreaInput);
        startDatePicker = findViewById(R.id.startDatePicker);
        btnSubmitForm   = findViewById(R.id.btnSubmitForm);

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        userId = currentUser.getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("workPrograms");

        Intent intent = getIntent();
        String cultivar = intent.getStringExtra("cultivar");
        String startDate = intent.getStringExtra("startDate");
        String passedProgramId = intent.getStringExtra("programId");

        if (cultivar != null && startDate != null && passedProgramId != null) {
            // Existing program - show header card with cultivar info
            if (cultivarCard != null) cultivarCard.setVisibility(View.GONE);
            if (headerCard != null) headerCard.setVisibility(View.VISIBLE);
            if (calendarHeader != null) calendarHeader.setVisibility(View.GONE);
            
            selectedCultivar = cultivar;
            programStartDate = startDate;
            programId = passedProgramId;
            loadProgramMilestones();
            
            // Show progress card
            if (progressCard != null) progressCard.setVisibility(View.VISIBLE);

            // Set up collapse toggle
            if (progressToggleBtn != null) {
                progressToggleBtn.setOnClickListener(v -> {
                    progressExpanded = !progressExpanded;
                    if (progressDetailsSection != null) {
                        progressDetailsSection.setVisibility(progressExpanded ? View.VISIBLE : View.GONE);
                    }
                    progressToggleBtn.setImageResource(progressExpanded
                            ? android.R.drawable.arrow_up_float
                            : android.R.drawable.arrow_down_float);
                });
            }

            // Show Current Expenses button
            if (btnCurrentExpenses != null) {
                btnCurrentExpenses.setVisibility(View.VISIBLE);
                btnCurrentExpenses.setOnClickListener(v -> openCurrentExpenses());
            }
            if (btnMilestones != null) {
                btnMilestones.setOnClickListener(v -> openMilestones());
            }
            
            // Update header card with cultivar info
            updateCultivarInfo(cultivar, startDate);

            logsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("routineLogs")
                    .child(programId)
                    .child("tasks");

            attachLogsListener();
            calendarView.removeDecorators();
            addPhaseDecorators();
            setCalendarClickListener();
            loadSummaryStats();

        } else {
            // New program form
            if (cultivarCard != null) cultivarCard.setVisibility(View.VISIBLE);
            if (headerCard != null) headerCard.setVisibility(View.GONE);
            if (calendarHeader != null) calendarHeader.setVisibility(View.GONE);

            String[] cultivarNames = new String[cultivarsData.length];
            for (int i = 0; i < cultivarsData.length; i++) {
                cultivarNames[i] = cultivarsData[i][0]; // cultivar name is column 0
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, cultivarNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (cultivarSpinner != null) {
                cultivarSpinner.setAdapter(adapter);
            }
            
            // Pre-select default cultivar if set
            String defaultCultivar = SettingsPreferences.getDefaultCultivar(this);
            if (!defaultCultivar.isEmpty()) {
                for (int i = 0; i < cultivarNames.length; i++) {
                    if (cultivarNames[i].equals(defaultCultivar)) {
                        if (cultivarSpinner != null) cultivarSpinner.setSelection(i);
                        selectedCultivar = defaultCultivar;
                        break;
                    }
                }
            }

            // Initialize selectedDate
            if (startDatePicker != null) {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                        startDatePicker.getYear(),
                        startDatePicker.getMonth() + 1,
                        startDatePicker.getDayOfMonth());
            }

            // Update selectedDate when DatePicker changes
            if (startDatePicker != null) {
                startDatePicker.init(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth(),
                        (view, year, month, dayOfMonth) -> selectedDate = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", year, month + 1, dayOfMonth));
            }

            if (btnSubmitForm != null) btnSubmitForm.setOnClickListener(v -> {
                if (cultivarSpinner == null || landAreaInput == null || selectedDate == null) {
                    Toast.makeText(this, "Setup error: form inputs not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                Object selected = cultivarSpinner.getSelectedItem();
                selectedCultivar = selected != null ? selected.toString() : "";
                landArea = landAreaInput.getText() != null ? landAreaInput.getText().toString().trim() : "";

                if (selectedCultivar.isEmpty() || selectedDate.isEmpty() || landArea.isEmpty()) {
                    Toast.makeText(this, "Please fill land area, cultivar, and start date", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Validate land area is a valid number (integer or decimal)
                double areaSizeValue;
                try {
                    areaSizeValue = Double.parseDouble(landArea);
                    if (areaSizeValue <= 0) {
                        Toast.makeText(this, "Land area must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Land area must be a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                String id = dbRef.push().getKey();
                if (id == null) {
                    Toast.makeText(this, "Error generating ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                programId = id;

                // Prepare data map with correct field names that match WorkProgramEntity structure
                // Firebase rules require: cultivarName, startingDate, areaSize
                // Optional but recommended: projectedIncome, projectedExpenses
                java.util.Map<String, Object> programData = new java.util.HashMap<>();
                programData.put("cultivarName", selectedCultivar);
                programData.put("startingDate", selectedDate);
                programData.put("areaSize", areaSizeValue);
                programData.put("projectedIncome", 0.0);
                programData.put("projectedExpenses", 0.0);
                programData.put("adjustedIncome", 0.0);
                programData.put("adjustedExpenses", 0.0);
                programData.put("detectionHistories", new java.util.HashMap<String, Object>());
                farmAreaHectares = areaSizeValue;
                
                // Calculate phases JSON for local storage
                String phasesJson = WorkProgramDataHelper.calculatePhasesJson(selectedCultivar, selectedDate);
                
                // Create WorkProgramEntity for local database
                WorkProgramEntity localEntity = new WorkProgramEntity(
                        id,
                        userId,
                        selectedCultivar,
                        areaSizeValue,
                        selectedDate,
                        phasesJson,
                        null, // detectionHistoriesJson
                        0.0, // projectedIncome
                        0.0, // projectedExpenses
                        0.0, // adjustedIncome
                        0.0, // adjustedExpenses
                        0.0, 0.0, 0.0, 0.0, 0.0, // phase completions
                        0, 0, 0, 0, 0.0 // task metrics
                );
                
                // Save to local database immediately (works offline)
                LocalDataManager.getInstance(Workprogram.this).saveWorkProgramToLocal(localEntity, "CREATE");
                
                // Save all data at once to Firebase (will queue if offline)
                DatabaseReference programRef = dbRef.child(id);
                programRef.setValue(programData).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Work Program saved successfully!", Toast.LENGTH_LONG).show();

                    // Sync from Firebase to local (to ensure consistency, handles online case)
                    if (LocalDataManager.isOnline(Workprogram.this)) {
                        LocalDataManager.getInstance(Workprogram.this).syncWorkProgramsFromFirebase(userId);
                    }

                    // Initialize weather data collection for this program
                    initializeWeatherDataCollection(programId, selectedDate, selectedCultivar);

                    logsRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(userId)
                            .child("routineLogs")
                            .child(programId)
                            .child("tasks");

                    int maturityDays = getMaturityDays(selectedCultivar);
                    if (maturityDays > 0) {
                        try {
                            Date start = sdf.parse(selectedDate);
                            if (start != null) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(start);
                                for (int i = 0; i < maturityDays; i++) {
                                    String dayKey = sdf.format(cal.getTime());
                                    logsRef.child(dayKey).setValue("pending");
                                    cal.add(Calendar.DAY_OF_YEAR, 1);
                                }
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "Error initializing work program dates", e);
                        }
                    }

                    attachLogsListener();
                    programStartDate = selectedDate;
                    addPhaseDecorators();
                    setCalendarClickListener();

                    // Show header card with cultivar info
                    headerCard.setVisibility(View.VISIBLE);
                    calendarHeader.setVisibility(View.GONE);
                    updateCultivarInfo(selectedCultivar, selectedDate);
                    
                    // Show Current Expenses button
                    if (btnCurrentExpenses != null) {
                        btnCurrentExpenses.setVisibility(View.VISIBLE);
                        btnCurrentExpenses.setOnClickListener(view -> openCurrentExpenses());
                    }

                }).addOnFailureListener(e -> {
                    // Even if Firebase save fails, local save succeeded
                    // Show appropriate message based on online status
                    if (LocalDataManager.isOnline(this)) {
                        Toast.makeText(this, "Failed to save to Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Work Program saved locally. Will sync when online.", Toast.LENGTH_LONG).show();
                    }
                });

                cultivarCard.setVisibility(View.GONE);
            });
        }
    }

    private void addPhaseDecorators() {
        if (programStartDate == null || programStartDate.isEmpty()) return;
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(programStartDate);
            java.time.LocalDate transplant = transplantDate == null ? null : java.time.LocalDate.parse(transplantDate);
            java.time.LocalDate stop = stopDate == null ? null : java.time.LocalDate.parse(stopDate);
            java.time.LocalDate phaseProjectionTransplant = transplant != null
                    ? transplant : getProjectedTransplantDate(start);
            HashSet<CalendarDay> phase1 = new HashSet<>();
            HashSet<CalendarDay> phase2 = new HashSet<>();
            HashSet<CalendarDay> phase3 = new HashSet<>();
            HashSet<CalendarDay> phase4 = new HashSet<>();
            HashSet<CalendarDay> phase5 = new HashSet<>();
            java.time.LocalDate end = stop != null ? stop : start.plusDays(365);
            for (java.time.LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
                int phase = com.android.tomatoapp.task.data.TaskSchedule.getPhaseNumber(
                        start, phaseProjectionTransplant, stop, current);
                CalendarDay currentDate = CalendarDay.from(current.getYear(),
                        current.getMonthValue() - 1, current.getDayOfMonth());
                switch (phase) {
                    case 1: phase1.add(currentDate); break;
                    case 2: phase2.add(currentDate); break;
                    case 3: phase3.add(currentDate); break;
                    case 4: phase4.add(currentDate); break;
                    case 5: phase5.add(currentDate); break;
                }
            }

            // Use the new drawables for the 5 stages
            Drawable p1 = ContextCompat.getDrawable(this, R.drawable.bg_calendar_day_landprep);
            Drawable p2 = ContextCompat.getDrawable(this, R.drawable.bg_calendar_day_vegetative);
            Drawable p3 = ContextCompat.getDrawable(this, R.drawable.bg_calendar_day_flowering);
            Drawable p4 = ContextCompat.getDrawable(this, R.drawable.bg_calendar_day_maturity);
            Drawable p5 = ContextCompat.getDrawable(this, R.drawable.bg_calendar_day_harvest);

            calendarView.addDecorator(new PhaseRangeDecorator(phase1, p1));
            calendarView.addDecorator(new PhaseRangeDecorator(phase2, p2));
            calendarView.addDecorator(new PhaseRangeDecorator(phase3, p3));
            calendarView.addDecorator(new PhaseRangeDecorator(phase4, p4));
            calendarView.addDecorator(new PhaseRangeDecorator(phase5, p5));
            addMilestoneDecorators();
            
            // Highlight today without replacing its phase background.
            calendarView.addDecorator(new TodayDecorator(this));
            calendarView.invalidateDecorators();
            
        } catch (java.time.format.DateTimeParseException e) {
            Log.e(TAG, "Error parsing phases for calendar display", e);
        }
    }

    /**
     * Supplies calendar-only phase colors before transplanting is recorded.
     * Projected dates never enter the scheduler, so future tasks remain locked.
     */
    private java.time.LocalDate getProjectedTransplantDate(java.time.LocalDate start) {
        if (sowingDate != null && !sowingDate.isEmpty()) {
            try {
                return java.time.LocalDate.parse(sowingDate).plusDays(25);
            } catch (java.time.format.DateTimeParseException error) {
                Log.w(TAG, "Ignoring invalid sowing date for phase projection", error);
            }
        }
        if (seedsConfirmedDate != null && !seedsConfirmedDate.isEmpty()) {
            try {
                java.time.LocalDate seeds = java.time.LocalDate.parse(seedsConfirmedDate);
                return seeds.plusDays(26);
            } catch (java.time.format.DateTimeParseException error) {
                Log.w(TAG, "Ignoring invalid seed confirmation date for phase projection", error);
            }
        }
        return start.plusDays(27);
    }

    public static class PhaseRangeDecorator implements DayViewDecorator {
        private final HashSet<CalendarDay> dates;
        private final Drawable drawable;
        public PhaseRangeDecorator(HashSet<CalendarDay> dates, Drawable drawable) {
            this.dates = dates; this.drawable = drawable;
        }
        @Override public boolean shouldDecorate(CalendarDay day) { return dates.contains(day); }
        @Override public void decorate(DayViewFacade view) { 
            view.setBackgroundDrawable(drawable); 
            view.addSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.BLACK));
        }
    }

    public static class TodayDecorator implements DayViewDecorator {
        private final CalendarDay today;

        public TodayDecorator(android.content.Context context) {
            this.today = CalendarDay.today();
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(today);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.WHITE));
            view.addSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD));
        }
    }

    private void addMilestoneDecorators() {
        HashSet<CalendarDay> seeds = milestoneDates(seedsConfirmedDate);
        HashSet<CalendarDay> sowing = milestoneDates(sowingDate);
        HashSet<CalendarDay> transplant = milestoneDates(transplantDate);
        HashSet<CalendarDay> stop = milestoneDates(stopDate);
        calendarView.addDecorator(new MilestoneDecorator(seeds, android.graphics.Color.rgb(33, 150, 243)));
        calendarView.addDecorator(new MilestoneDecorator(sowing, android.graphics.Color.rgb(76, 175, 80)));
        calendarView.addDecorator(new MilestoneDecorator(transplant, android.graphics.Color.rgb(255, 152, 0)));
        calendarView.addDecorator(new MilestoneDecorator(stop, android.graphics.Color.rgb(156, 39, 176)));
    }

    private HashSet<CalendarDay> milestoneDates(String value) {
        HashSet<CalendarDay> dates = new HashSet<>();
        if (value == null || value.isEmpty()) return dates;
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(value);
            dates.add(CalendarDay.from(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()));
        } catch (java.time.format.DateTimeParseException error) {
            Log.w(TAG, "Ignoring invalid milestone date: " + value, error);
        }
        return dates;
    }

    private static class MilestoneDecorator implements DayViewDecorator {
        private final HashSet<CalendarDay> dates;
        private final int color;

        MilestoneDecorator(HashSet<CalendarDay> dates, int color) {
            this.dates = dates;
            this.color = color;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new android.text.style.ForegroundColorSpan(color));
            view.addSpan(new android.text.style.UnderlineSpan());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (programId != null && !programId.isEmpty()) {
            loadProgramMilestones();
        }
    }

    private void loadSummaryStats() {
        if (programId == null || programStartDate == null) return;

        // 1. Days Active
        int daysActive = calculateDayNumber(programStartDate, new Date());
        if (summaryDaysActive != null) {
            summaryDaysActive.setText(String.valueOf(Math.max(1, daysActive)));
        }

        // 2. Expenses and Tasks Due
        // Avoid UI "flash" from any XML placeholder values
        if (summaryExpenses != null) summaryExpenses.setText("₱0");
        if (summaryTasksDue != null) summaryTasksDue.setText("0");

        WorkProgramDataHelper.fetchCompletionStats(userId, programId, selectedCultivar, programStartDate, stats -> {
            runOnUiThread(() -> {
                // Expenses: sum logged daily expenses from Firebase
                loadLoggedDailyExpensesTotal();
            });
        });
    }

    private void loadLoggedDailyExpensesTotal() {
        if (summaryExpenses == null) return;
        if (userId == null || programId == null) {
            summaryExpenses.setText("₱0");
            return;
        }

        if (!LocalDataManager.isOnline(this)) {
            // Offline: we can't reliably aggregate remote daily expenses
            summaryExpenses.setText("₱0");
            return;
        }

        DatabaseReference dailyExpensesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("workPrograms")
                .child(programId)
                .child("dailyExpenses");

        dailyExpensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double total = 0.0;
                for (DataSnapshot dateNode : snapshot.getChildren()) {
                    total += sumDailyExpenseNode(dateNode);
                }
                final double finalTotal = total;
                runOnUiThread(() -> summaryExpenses.setText(formatPeso(finalTotal)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> summaryExpenses.setText("₱0"));
            }
        });
    }

    private double sumDailyExpenseNode(DataSnapshot dateNode) {
        double total = 0.0;

        // labor[].totalCost
        DataSnapshot labor = dateNode.child("labor");
        for (DataSnapshot item : labor.getChildren()) {
            total += getDouble(item.child("totalCost").getValue());
        }

        // material[].totalCost
        DataSnapshot material = dateNode.child("material");
        for (DataSnapshot item : material.getChildren()) {
            total += getDouble(item.child("totalCost").getValue());
        }

        // equipment[].totalCost
        DataSnapshot equipment = dateNode.child("equipment");
        for (DataSnapshot item : equipment.getChildren()) {
            total += getDouble(item.child("totalCost").getValue());
        }

        // miscellaneous[].cost
        DataSnapshot misc = dateNode.child("miscellaneous");
        for (DataSnapshot item : misc.getChildren()) {
            total += getDouble(item.child("cost").getValue());
        }

        return total;
    }

    private double getDouble(Object value) {
        if (value instanceof Double) return (Double) value;
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private String formatPeso(double value) {
        if (value >= 1000) {
            return String.format(Locale.getDefault(), "₱%.1fk", value / 1000.0);
        }
        return String.format(Locale.getDefault(), "₱%.0f", value);
    }

    // Decorator to disable non-accessible dates
    public static class DisabledDateDecorator implements DayViewDecorator {
        private final HashSet<CalendarDay> accessibleDates;
        private final android.content.Context context;
        
        public DisabledDateDecorator(HashSet<CalendarDay> accessibleDates, android.content.Context context) {
            this.accessibleDates = accessibleDates;
            this.context = context;
        }
        
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            // Decorate dates that are NOT accessible (disable them)
            return !accessibleDates.contains(day);
        }
        
        @Override
        public void decorate(DayViewFacade view) {
            // Make non-accessible dates appear disabled and non-selectable
            view.setDaysDisabled(true);
            // Gray out the text to show it's disabled
            view.addSpan(new android.text.style.ForegroundColorSpan(
                    ContextCompat.getColor(context, android.R.color.darker_gray)));
        }
    }

    private int getMaturityDays(String cultivar) {
        for (String[] c : cultivarsData) {
            if (c[0].equals(cultivar)) {
                try {
                    return Integer.parseInt(c[3]); // use max maturity days
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing maturity days for cultivar " + cultivar, e);
                    return 0;
                }

            }
        }
        return 0;
    }

    private String getGrowthHabit(String cultivar) {
        if (cultivar == null) return null;
        for (String[] data : cultivarsData) {
            if (cultivar.equals(data[0])) return data[1];
        }
        return null;
    }

    /**
     * Returns the biological program day from the recorded planting date.
     * Missed or skipped work is shown separately and never shifts phases.
     */
    private int calculateAdjustedDayNumber(Date current) {
        int calendarDay = calculateDayNumber(programStartDate, current);
        if (calendarDay <= 0) {
            return calendarDay;
        }

        return calendarDay;
    }

    private int calculateAdjustedDayNumber(String date) {
        if (date == null || date.isEmpty()) {
            return -1;
        }
        try {
            Date parsedDate = sdf.parse(date);
            return calculateAdjustedDayNumber(parsedDate);
        } catch (ParseException e) {
            Log.e(TAG, "Error calculating adjusted program day for " + date, e);
            return -1;
        }
    }

    private void updateTaskWarningBanner() {
        if (taskWarningBanner == null) return;
        int missedCount = missedDates.size();
        int skippedCount = skippedDates.size();
        if (missedCount == 0 && skippedCount == 0) {
            taskWarningBanner.setVisibility(View.GONE);
            return;
        }
        // Banner is a dummy view in redesigned layout; just show it as a visual indicator
        taskWarningBanner.setVisibility(View.GONE); // hidden since dummy has no text UI
    }

    private String buildPhaseCatchUpHint() {
        CalendarDay target = findEarliestDate(missedDates);
        if (target == null) {
            target = findEarliestDate(skippedDates);
        }
        if (target == null || programStartDate == null || programStartDate.isEmpty()) {
            return "";
        }

        Date targetDate = target.getDate();
        int dayNumber = calculateAdjustedDayNumber(targetDate);
        if (dayNumber <= 0) {
            return "";
        }

        int maturityDays = getMaturityDays(selectedCultivar);
        if (maturityDays <= 0) {
            maturityDays = 90;
        }
        int phase = getPhaseFromDay(maturityDays, dayNumber);
        String phaseLabel = getPhaseLabel(phase);
        if (phaseLabel.isEmpty()) {
            return "";
        }

        return "Catch up on " + phaseLabel + " tasks to keep projections accurate.";
    }

    private CalendarDay findEarliestDate(HashSet<CalendarDay> dates) {
        CalendarDay earliest = null;
        for (CalendarDay day : dates) {
            if (earliest == null || day.getDate().before(earliest.getDate())) {
                earliest = day;
            }
        }
        return earliest;
    }

    private int calculateDayNumber(String startDate, Date current) {
        try {
            Date start = sdf.parse(startDate);
            if (start == null || current == null) return -1;
            long diff = current.getTime() - start.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        } catch (ParseException e) {
            return -1;
        }
    }

    private int getPhaseFromDay(int maturityDays, int dayNumber) {
        if (dayNumber <= 0) return 1;
        int phaseDuration = maturityDays / 5;
        if (phaseDuration <= 0) {
            phaseDuration = 1;
        }
        return Math.min(((dayNumber - 1) / phaseDuration) + 1, 5);
    }

    private String getPhaseLabel(int phase) {
        switch (phase) {
            case 1:
                return "Phase 1 (Land & Soil Prep)";
            case 2:
                return "Phase 2 (Vegetative)";
            case 3:
                return "Phase 3 (Flowering)";
            case 4:
                return "Phase 4 (Maturity)";
            case 5:
                return "Phase 5 (Post-Harvest)";
            default:
                return "";
        }
    }


    private void setCalendarClickListener() {
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            // Don't process if date is not selected (deselection)
            if (!selected) {
                return;
            }
            
            String clickedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    date.getYear(), date.getMonth() + 1, date.getDay());
            if (isBeforeStartDate(clickedDate, programStartDate)) {
                Toast.makeText(this, "No tasks available before start date!", Toast.LENGTH_SHORT).show();
                // Deselect the date
                widget.setDateSelected(date, false);
                return;
            }

            CalendarDay selectedDay = CalendarDay.from(date.getYear(), date.getMonth(), date.getDay());
            if (!accessibleDates.contains(selectedDay)) {
                Toast.makeText(this, "Complete the previous day before opening this date.",
                        Toast.LENGTH_SHORT).show();
                widget.setDateSelected(date, false);
                return;
            }

            openDailyTask(clickedDate);
        });
    }

    private void openDailyTask(String clickedDate) {
        // 🔹 Get cultivar details (growth habit & maturity days)
        String growthHabit = "";
        int maturityDays = 0;
        for (String[] c : cultivarsData) {
            if (c[0].equals(selectedCultivar)) {
                growthHabit = c[1];
                try {
                    maturityDays = Integer.parseInt(c[2]);
                } catch (NumberFormatException e) {
                    maturityDays = 90; // fallback default
                }
                break;
            }
        }

        // 🔹 Pass all required info to DailyTask
        Intent taskIntent = new Intent(Workprogram.this, DailyTask.class);
        taskIntent.putExtra("cultivar", selectedCultivar);
        taskIntent.putExtra("date", clickedDate);
        taskIntent.putExtra("programId", programId);
        taskIntent.putExtra("growthHabit", growthHabit);
        taskIntent.putExtra("maturityDays", maturityDays);
        taskIntent.putExtra("programStartDate", programStartDate);
        taskIntent.putExtra("adjustedDayNumber", calculateAdjustedDayNumber(clickedDate));
        taskIntent.putExtra("farmAreaHectares", getSelectedAreaHectares());
        taskIntent.putExtra("seedsConfirmedDate", seedsConfirmedDate);
        taskIntent.putExtra("sowingDate", sowingDate);
        taskIntent.putExtra("transplantDate", transplantDate);
        taskIntent.putExtra("stopDate", stopDate);
        startActivityForResult(taskIntent, DAILY_TASK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DAILY_TASK_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("milestoneChanged", false)) {
                loadProgramMilestones();
            }
            if (data != null && (data.getBooleanExtra("statusChanged", false)
                    || data.hasExtra("date"))) {
                // Immediately reflect the completed date on the calendar (optimistic update)
                String completedDate = data.getStringExtra("date");
                if (completedDate != null) {
                    applyCompletedDateImmediately(completedDate);
                }
                // Then sync with Firebase to catch any server-side differences
                reloadTaskStatuses();
            }
        }
    }

    /**
     * Immediately marks a date as completed in the local sets and redraws
     * calendar decorators so the indicator shows without waiting for Firebase.
     */
    private void applyCompletedDateImmediately(String dateStr) {
        CalendarDay cd = parseCalendarDay(dateStr);
        if (cd == null) return;
        completedDates.add(cd);
        missedDates.remove(cd);
        skippedDates.remove(cd);
        // Unlock the next day
        try {
            SimpleDateFormat localSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = localSdf.parse(dateStr);
            if (d != null) {
                Calendar nextCal = Calendar.getInstance();
                nextCal.setTime(d);
                nextCal.add(Calendar.DAY_OF_YEAR, 1);
                String nextDateStr = localSdf.format(nextCal.getTime());
                CalendarDay nextCd = parseCalendarDay(nextDateStr);
                if (nextCd != null) {
                    accessibleDates.add(nextCd);
                }
            }
        } catch (ParseException e) {
            Log.w(TAG, "Could not pre-unlock next day", e);
        }
        // Redraw decorators immediately
        calendarView.removeDecorators();
        addPhaseDecorators();
        calendarView.addDecorator(new CompletedDecorator(new HashSet<>(completedDates), Workprogram.this));
        calendarView.addDecorator(new MissedDecorator(new HashSet<>(missedDates), Workprogram.this));
        calendarView.addDecorator(new SkippedDecorator(new HashSet<>(skippedDates), Workprogram.this));
        // Also refresh progress card immediately
        updateProgressCard();
    }

    private void reloadTaskStatuses() {
        if (logsRef == null) return;
        logsRef.get().addOnSuccessListener(this::processTaskStatuses)
                .addOnFailureListener(error -> Log.w(TAG,
                        "Could not refresh calendar task indicators", error));
    }

    private double getSelectedAreaHectares() {
        if (farmAreaHectares > 0) return farmAreaHectares;
        try {
            return Double.parseDouble(landArea);
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }

    private void loadProgramMilestones() {
        if (programId == null || programId.isEmpty()) return;
        dbRef.child(programId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Object areaValue = snapshot.child("areaSize").getValue();
                    if (areaValue instanceof Number && ((Number) areaValue).doubleValue() > 0) {
                        farmAreaHectares = ((Number) areaValue).doubleValue();
                    } else if (areaValue instanceof String) {
                        try {
                            double parsedArea = Double.parseDouble((String) areaValue);
                            if (parsedArea > 0) farmAreaHectares = parsedArea;
                        } catch (NumberFormatException ignored) {
                            Log.w(TAG, "Ignoring invalid saved farm area for program " + programId);
                        }
                    }
                    seedsConfirmedDate = readOptionalString(snapshot, "seedsConfirmedDate");
                    sowingDate = readOptionalString(snapshot, "sowingDate");
                    transplantDate = readOptionalString(snapshot, "transplantDate");
                    stopDate = readOptionalString(snapshot, "stopDate");
                    calendarView.removeDecorators();
                    addPhaseDecorators();
                } catch (RuntimeException error) {
                    Log.e(TAG, "Could not read saved milestone data", error);
                    Toast.makeText(Workprogram.this,
                            "Work program opened, but milestone data could not be loaded.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Workprogram.this,
                        "Could not load milestone status: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    private String readOptionalString(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value == null ? null : String.valueOf(value);
    }

    private void openCurrentExpenses() {
        if (programId == null || selectedCultivar == null || programStartDate == null) {
            Toast.makeText(this, "Missing required information", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Workprogram.this, CurrentExpensesActivity.class);
        intent.putExtra("programId", programId);
        intent.putExtra("cultivar", selectedCultivar);
        intent.putExtra("startDate", programStartDate);
        startActivity(intent);
    }

    private void openMilestones() {
        Intent intent = new Intent(this, MilestonesActivity.class);
        intent.putExtra("programId", programId);
        intent.putExtra("seedsConfirmedDate", seedsConfirmedDate);
        intent.putExtra("sowingDate", sowingDate);
        intent.putExtra("transplantDate", transplantDate);
        intent.putExtra("stopDate", stopDate);
        startActivityForResult(intent, DAILY_TASK_REQUEST);
    }

    private void attachLogsListener() {
        if (logsRef == null) return;
        
        // Load task statuses with offline fallback
        if (LocalDataManager.isOnline(this) && programId != null && userId != null) {
            // Try Firebase first if online
            logsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    processTaskStatuses(snapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Fallback to local database
                    loadTaskStatusesFromLocal();
                }
            });
        } else {
            // Offline - load from local database
            loadTaskStatusesFromLocal();
        }
    }

    private void processTaskStatuses(DataSnapshot snapshot) {
        completedDates.clear();
        missedDates.clear();
        skippedDates.clear();
        accessibleDates.clear();
        Date today = new Date();

        // Store all task statuses
        java.util.HashMap<String, String> taskStatuses = new java.util.HashMap<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            String dateKey = child.getKey();
            String status = child.getValue(String.class);
            if (status != null) {
                taskStatuses.put(dateKey, status);
            }
        }
        
        processTaskStatusesMap(taskStatuses, today);
    }

    private void loadTaskStatusesFromLocal() {
        if (programId == null || userId == null) return;
        
        new Thread(() -> {
            List<TaskEntity> tasks = LocalDataManager.getInstance(Workprogram.this).getTasksFromLocal(userId, programId);
            runOnUiThread(() -> {
                java.util.HashMap<String, String> taskStatuses = new java.util.HashMap<>();
                for (TaskEntity task : tasks) {
                    taskStatuses.put(task.dateKey, task.status);
                }
                
                completedDates.clear();
                missedDates.clear();
                skippedDates.clear();
                accessibleDates.clear();
                Date today = new Date();
                processTaskStatusesMap(taskStatuses, today);
                
                if (!LocalDataManager.isOnline(Workprogram.this)) {
                    Toast.makeText(Workprogram.this, "Showing offline task data", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void processTaskStatusesMap(java.util.HashMap<String, String> taskStatuses, Date today) {
        try {
            Date start = sdf.parse(programStartDate);
            if (start == null) return;
            
            Calendar checkCal = Calendar.getInstance();
            checkCal.setTime(start);
            Calendar futureCal = Calendar.getInstance();
            futureCal.setTime(today);
            futureCal.add(Calendar.DAY_OF_YEAR, 365);
            Date endDate = futureCal.getTime();
            String previousDateKey = null;
            
            while (!checkCal.getTime().after(endDate)) {
                String dateKey = sdf.format(checkCal.getTime());
                CalendarDay cd = parseCalendarDay(dateKey);
                if (cd == null) {
                    checkCal.add(Calendar.DAY_OF_YEAR, 1);
                    continue;
                }

                if (previousDateKey == null || "completed".equals(taskStatuses.get(previousDateKey))) {
                    accessibleDates.add(cd);
                }
                String status = taskStatuses.get(dateKey);
                if ("completed".equals(status)) {
                    completedDates.add(cd);
                } else if ("missed".equals(status)) {
                    missedDates.add(cd);
                } else if ("skipped".equals(status)) {
                    skippedDates.add(cd);
                }
                previousDateKey = dateKey;
                checkCal.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error calculating accessible dates", e);
        }

        // Calculate tasks due for today
        int tasksDue = 0;
        String todayKey = sdf.format(today);
        String todayStatus = taskStatuses.get(todayKey);
        
        // If today is pending or not yet logged, show tasks count for today
        if (todayStatus == null || "pending".equals(todayStatus)) {
            int maturityDays = getMaturityDays(selectedCultivar);
            int dayNumber = calculateAdjustedDayNumber(today);
            if (dayNumber > 0 && dayNumber <= maturityDays) {
                // Fetch task count for the current day from the schedule
                tasksDue = getScheduledActionTaskCount(today);
            }

        }
        
        final int finalTasksDue = tasksDue;
        runOnUiThread(() -> {
            if (summaryTasksDue != null) {
                summaryTasksDue.setText(String.valueOf(finalTasksDue));
            }
        });

        updateTaskWarningBanner();
        calendarView.removeDecorators();
        addPhaseDecorators();
        calendarView.addDecorator(new CompletedDecorator(new HashSet<>(completedDates), Workprogram.this));
        calendarView.addDecorator(new MissedDecorator(new HashSet<>(missedDates), Workprogram.this));
        calendarView.addDecorator(new SkippedDecorator(new HashSet<>(skippedDates), Workprogram.this));
        updateProgressCard();
    }

    /**
     * Computes and renders the Program Progress card from already-loaded date sets.
     * Safe to call any time after processTaskStatusesMap() has run.
     *
     * FORMULA:
     *   overallPct  = completedDays / totalProgramDays × 100
     *   phasePct[i] = completedDaysInPhase / phaseDuration × 100
     *
     * Both denominators use the FIXED program length, not daysElapsed,
     * so phases don't jump to 100% just because few days have passed.
     */
    private void updateProgressCard() {
        if (progressCard == null || programStartDate == null || programStartDate.isEmpty()) return;

        int totalDays     = getMaturityDays(selectedCultivar);
        if (totalDays <= 0) totalDays = 105; // fallback

        int daysElapsed   = Math.max(1, calculateDayNumber(programStartDate, new Date()));
        int completed     = completedDates.size();
        int missed        = missedDates.size();
        int skipped       = skippedDates.size();

        // ── FIXED: divide by totalDays, NOT daysElapsed ──
        int overallPct = Math.min(100, totalDays > 0 ? (completed * 100 / totalDays) : 0);

        // Phase size = totalDays / 5 (fixed duration)
        int phaseDuration = Math.max(1, totalDays / 5);
        int[] phaseCompleted = new int[5];

        // Map each completed CalendarDay → its phase bucket
        try {
            SimpleDateFormat localSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startD = localSdf.parse(programStartDate);
            if (startD != null) {
                // Normalize start to midnight to avoid hour-offset errors
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startD);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                long startMs = startCal.getTimeInMillis();

                for (CalendarDay cd : completedDates) {
                    Calendar cdCal = Calendar.getInstance();
                    cdCal.set(cd.getYear(), cd.getMonth(), cd.getDay());
                    cdCal.set(Calendar.HOUR_OF_DAY, 0);
                    cdCal.set(Calendar.MINUTE, 0);
                    cdCal.set(Calendar.SECOND, 0);
                    cdCal.set(Calendar.MILLISECOND, 0);
                    long diffMs = cdCal.getTimeInMillis() - startMs;
                    int dayNum  = (int) (diffMs / (1000L * 60 * 60 * 24)) + 1;
                    if (dayNum >= 1 && dayNum <= totalDays) {
                        int phaseIdx = Math.min(4, (dayNum - 1) / phaseDuration);
                        phaseCompleted[phaseIdx]++;
                    }
                }
            }
        } catch (ParseException e) {
            Log.w(TAG, "Error computing phase progress", e);
        }

        // Phase % = completedInPhase / phaseDuration (not elapsed in phase)
        int[] phasePct = new int[5];
        for (int i = 0; i < 5; i++) {
            // Last phase may have extra days if totalDays not evenly divisible by 5
            int thisPhaseDuration = (i == 4) ? (totalDays - phaseDuration * 4) : phaseDuration;
            thisPhaseDuration = Math.max(1, thisPhaseDuration);
            phasePct[i] = Math.min(100, phaseCompleted[i] * 100 / thisPhaseDuration);
        }

        // ── Update UI ──
        if (progressBarOverall != null) progressBarOverall.setProgress(overallPct);
        if (progressPercent    != null) progressPercent.setText(overallPct + "%");
        if (progressDayLabel   != null)
            progressDayLabel.setText(String.format(Locale.getDefault(),
                    "Day %d of %d  ·  %d%% complete", daysElapsed, totalDays, overallPct));

        setPhaseBar(progressPhase1, progressPhase1Pct, phasePct[0]);
        setPhaseBar(progressPhase2, progressPhase2Pct, phasePct[1]);
        setPhaseBar(progressPhase3, progressPhase3Pct, phasePct[2]);
        setPhaseBar(progressPhase4, progressPhase4Pct, phasePct[3]);
        setPhaseBar(progressPhase5, progressPhase5Pct, phasePct[4]);

        if (progressStatCompleted != null) progressStatCompleted.setText(String.valueOf(completed));
        if (progressStatMissed    != null) progressStatMissed.setText(String.valueOf(missed));
        if (progressStatSkipped   != null) progressStatSkipped.setText(String.valueOf(skipped));

        // Persist completionRate to local entity so WorkProgramSelection can display it
        if (programId != null && !programId.isEmpty() && userId != null) {
            final int finalRate = overallPct;
            new Thread(() -> {
                try {
                    LocalDataManager.getInstance(Workprogram.this)
                            .updateCompletionRate(programId, finalRate);
                } catch (Exception ignored) {}
            }).start();
        }
    }

    private void setPhaseBar(android.widget.ProgressBar bar, TextView label, int pct) {
        if (bar   != null) bar.setProgress(pct);
        if (label != null) label.setText(pct + "%");
    }

    private int getScheduledActionTaskCount(Date day) {
        try {
            java.time.LocalDate current = java.time.LocalDate.parse(sdf.format(day));
            java.time.LocalDate planting = java.time.LocalDate.parse(programStartDate);
            java.time.LocalDate seeds = seedsConfirmedDate == null ? null : java.time.LocalDate.parse(seedsConfirmedDate);
            java.time.LocalDate sowing = sowingDate == null ? null : java.time.LocalDate.parse(sowingDate);
            java.time.LocalDate transplant = transplantDate == null ? null : java.time.LocalDate.parse(transplantDate);
            java.time.LocalDate stop = stopDate == null ? null : java.time.LocalDate.parse(stopDate);
            TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                    planting, getSelectedAreaHectares(), getGrowthHabit(selectedCultivar), seeds, sowing, transplant,
                    stop, current, TomatoWorkProgramScheduler.DEFAULT_HECTARES_PER_PLOWING_DAY);
            int count = 0;
            for (TaskModel task : TomatoWorkProgramScheduler.getTasksForDate(input, current)) {
                if ("MAIN".equals(task.type) || "MAINTENANCE".equals(task.type)) count++;
            }
            return count;
        } catch (RuntimeException error) {
            Log.w(TAG, "Could not calculate scheduled task count", error);
            return 0;
        }
    }



    private boolean isBeforeStartDate(String clickedDate, String startDate) {
        try {
            Date d1 = sdf.parse(clickedDate);
            Date d2 = sdf.parse(startDate);
            return d1 != null && d2 != null && d1.before(d2);
        } catch (ParseException e) { Log.e(TAG, "Error comparing dates", e); return false; }
    }

    private CalendarDay parseCalendarDay(String dateStr) {
        try {
            Date d = sdf.parse(dateStr);
            if (d != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                return CalendarDay.from(cal);
            }
        } catch (ParseException e) { Log.e(TAG, "Error parsing calendar day from " + dateStr, e); }
        return null;
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
    }


    /**
     * Updates the cultivar information in the header card
     */
    private void updateCultivarInfo(String cultivar, String startDate) {
        if (cultivarNameText != null) cultivarNameText.setText(cultivar);
        
        if (startDateText != null) {
            try {
                Date date = sdf.parse(startDate);
                if (date != null) {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                    startDateText.setText("Started: " + displayFormat.format(date));
                    
                    // Update Season tag: Wet (June-Nov) or Dry (Dec-May)
                    if (seasonTagText != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int month = cal.get(Calendar.MONTH); // 0-based
                        String season = (month >= 5 && month <= 10) ? "Wet Season" : "Dry Season";
                        seasonTagText.setText(season);
                    }
                }
            } catch (ParseException e) {
                startDateText.setText("Started: " + startDate);
            }
        }
        
        // Handle image vs emoji
        if (cultivarImage != null && cultivarEmoji != null) {
            // Use the correct method name from the project (getCultivarImageResource)
            int imageRes = CultivarImageHelper.getCultivarImageResource(cultivar);
            if (imageRes != 0) {
                cultivarImage.setImageResource(imageRes);
                cultivarImage.setVisibility(View.VISIBLE);
                cultivarEmoji.setVisibility(View.GONE);
            } else {
                cultivarImage.setVisibility(View.GONE);
                cultivarEmoji.setVisibility(View.VISIBLE);
                cultivarEmoji.setText("🍅");
            }
        }
    }

    /**
     * Initializes weather data collection for a newly created work program.
     * Fetches current weather data from Open-Meteo API and stores it.
     * Uses the location from SharedPreferences (same as MainActivity).
     */
    private void initializeWeatherDataCollection(String programId, String plantingDate, String cultivar) {
        // Fetch and collect current weather data for this program
        // This will create the weather data entry with actual current weather
        WeatherDataCollector.collectWeatherForProgram(this, programId, plantingDate);
        
        // Note: Weather data will be updated periodically as the program progresses.
        // The initial fetch happens here, and can be refreshed later.
    }

    public static class WorkProgramModel {
        public String cultivar;
        public String startDate;
        public String landArea;
        public WorkProgramModel() {}
        public WorkProgramModel(String cultivar, String startDate, String landArea) {
            this.cultivar = cultivar;
            this.startDate = startDate;
            this.landArea = landArea;
        }
    }
}
