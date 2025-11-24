package com.android.tomatoapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import android.content.SharedPreferences;
import android.location.Location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class Workprogram extends BaseDrawerActivity {

    private CardView cultivarCard;
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
    private LinearLayout calendarHeader;
    private ImageView headerCultivarImage;
    private TextView headerCultivarName;
    private TextView headerStartDate;
    private TextView taskWarningBanner;

    private String selectedCultivar = "";
    private String selectedDate = "";
    private String landArea = "";
    private String programStartDate = "";
    private String programId = "";

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workprogram);

        // Drawer setup
        setupDrawer();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Work Program");
        }

        // Views
        cultivarCard = findViewById(R.id.floatingFormCard);
        cultivarSpinner = findViewById(R.id.cultivarSpinner);
        landAreaInput = findViewById(R.id.landAreaInput);
        startDatePicker = findViewById(R.id.startDatePicker);
        btnSubmitForm = findViewById(R.id.btnSelectCultivar);
        calendarView = findViewById(R.id.CalendarView);
        
        // New UI elements
        headerCard = findViewById(R.id.headerCard);
        cultivarImage = findViewById(R.id.cultivarImage);
        cultivarNameText = findViewById(R.id.cultivarNameText);
        startDateText = findViewById(R.id.startDateText);
        calendarHeader = findViewById(R.id.calendarHeader);
        headerCultivarImage = findViewById(R.id.headerCultivarImage);
        headerCultivarName = findViewById(R.id.headerCultivarName);
        headerStartDate = findViewById(R.id.headerStartDate);
        taskWarningBanner = findViewById(R.id.taskWarningBanner);

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
            cultivarCard.setVisibility(CardView.GONE);
            headerCard.setVisibility(View.VISIBLE);
            calendarHeader.setVisibility(View.GONE);
            
            selectedCultivar = cultivar;
            programStartDate = startDate;
            programId = passedProgramId;
            
            // Update header card with cultivar info
            updateCultivarInfo(cultivar, startDate);

            logsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("routineLogs")
                    .child(programId)
                    .child("tasks");

            attachLogsListener();
            addPhaseDecorators();
            setCalendarClickListener();

        } else {
            // New program form
            cultivarCard.setVisibility(CardView.VISIBLE);
            headerCard.setVisibility(View.GONE);
            calendarHeader.setVisibility(View.GONE);

            String[] cultivarNames = new String[cultivarsData.length];
            for (int i = 0; i < cultivarsData.length; i++) {
                cultivarNames[i] = cultivarsData[i][0]; // cultivar name is column 0
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, cultivarNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cultivarSpinner.setAdapter(adapter);
            
            // Pre-select default cultivar if set
            String defaultCultivar = SettingsPreferences.getDefaultCultivar(this);
            if (!defaultCultivar.isEmpty()) {
                for (int i = 0; i < cultivarNames.length; i++) {
                    if (cultivarNames[i].equals(defaultCultivar)) {
                        cultivarSpinner.setSelection(i);
                        selectedCultivar = defaultCultivar;
                        break;
                    }
                }
            }

            // Initialize selectedDate
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    startDatePicker.getYear(),
                    startDatePicker.getMonth() + 1,
                    startDatePicker.getDayOfMonth());

            // Update selectedDate when DatePicker changes
            startDatePicker.init(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth(),
                    (view, year, month, dayOfMonth) -> selectedDate = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", year, month + 1, dayOfMonth));

            btnSubmitForm.setOnClickListener(v -> {
                selectedCultivar = cultivarSpinner.getSelectedItem().toString();
                landArea = landAreaInput.getText().toString().trim();

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
                
                // Save all data at once
                DatabaseReference programRef = dbRef.child(id);
                programRef.setValue(programData).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Work Program saved successfully!", Toast.LENGTH_LONG).show();

                    // Sync to local database
                    LocalDataManager.getInstance(Workprogram.this).syncWorkProgramsFromFirebase(userId);

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
                            e.printStackTrace();
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

                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show());

                cultivarCard.setVisibility(CardView.GONE);
            });
        }
    }

    private void addPhaseDecorators() {
        if (programStartDate.isEmpty() || selectedCultivar.isEmpty()) return;
        int maturityDays = getMaturityDays(selectedCultivar);
        if (maturityDays <= 0) return;

        // Phase 1 fixed = 30 days
        int phase1Days = 30;

        // Remaining days split across Phases 2–5
        int remaining = maturityDays - phase1Days;
        int eachPhase = remaining / 4;
        int extra = remaining % 4; // distribute remainder

        Drawable p1 = ContextCompat.getDrawable(this, R.drawable.phase1);
        Drawable p2 = ContextCompat.getDrawable(this, R.drawable.phase2);
        Drawable p3 = ContextCompat.getDrawable(this, R.drawable.phase3);
        Drawable p4 = ContextCompat.getDrawable(this, R.drawable.phase4);
        Drawable p5 = ContextCompat.getDrawable(this, R.drawable.phase5);

        try {
            Date start = sdf.parse(programStartDate);
            if (start == null) return;
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);

            HashSet<CalendarDay> phase1 = new HashSet<>();
            HashSet<CalendarDay> phase2 = new HashSet<>();
            HashSet<CalendarDay> phase3 = new HashSet<>();
            HashSet<CalendarDay> phase4 = new HashSet<>();
            HashSet<CalendarDay> phase5 = new HashSet<>();

            // Fill Phase 1
            for (int d = 0; d < phase1Days; d++) {
                phase1.add(CalendarDay.from(cal));
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Fill Phases 2–5
            for (int phase = 2; phase <= 5; phase++) {
                int duration = eachPhase + (extra-- > 0 ? 1 : 0);
                for (int d = 0; d < duration; d++) {
                    CalendarDay cd = CalendarDay.from(cal);
                    switch (phase) {
                        case 2: phase2.add(cd); break;
                        case 3: phase3.add(cd); break;
                        case 4: phase4.add(cd); break;
                        case 5: phase5.add(cd); break;
                    }
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }
            }

            calendarView.addDecorator(new PhaseRangeDecorator(phase1, p1));
            calendarView.addDecorator(new PhaseRangeDecorator(phase2, p2));
            calendarView.addDecorator(new PhaseRangeDecorator(phase3, p3));
            calendarView.addDecorator(new PhaseRangeDecorator(phase4, p4));
            calendarView.addDecorator(new PhaseRangeDecorator(phase5, p5));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static class PhaseRangeDecorator implements DayViewDecorator {
        private final HashSet<CalendarDay> dates;
        private final Drawable drawable;
        public PhaseRangeDecorator(HashSet<CalendarDay> dates, Drawable drawable) {
            this.dates = dates; this.drawable = drawable;
        }
        @Override public boolean shouldDecorate(CalendarDay day) { return dates.contains(day); }
        @Override public void decorate(DayViewFacade view) { view.setBackgroundDrawable(drawable); }
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
                    e.printStackTrace();
                    return 0;
                }
            }
        }
        return 0;
    }

    private void updateTaskWarningBanner() {
        if (taskWarningBanner == null) return;
        int missedCount = missedDates.size();
        int skippedCount = skippedDates.size();
        if (missedCount == 0 && skippedCount == 0) {
            taskWarningBanner.setVisibility(View.GONE);
            return;
        }

        StringBuilder message = new StringBuilder("Heads up: ");
        if (missedCount > 0) {
            message.append(missedCount).append(missedCount == 1 ? " day missed" : " days missed");
        }
        if (skippedCount > 0) {
            if (missedCount > 0) {
                message.append(" · ");
            }
            message.append(skippedCount).append(skippedCount == 1 ? " day skipped" : " days skipped");
        }

        String hint = buildPhaseCatchUpHint();
        if (!hint.isEmpty()) {
            message.append("\n").append(hint);
        } else {
            message.append("\nLog today's activity or plan a quick catch-up.");
        }

        taskWarningBanner.setText(message.toString());
        taskWarningBanner.setVisibility(View.VISIBLE);
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
        int dayNumber = calculateDayNumber(programStartDate, targetDate);
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
                return "Phase 1 (Nursery & Land Prep)";
            case 2:
                return "Phase 2 (Transplant & Establishment)";
            case 3:
                return "Phase 3 (Vegetative Growth)";
            case 4:
                return "Phase 4 (Flowering & Fruit Set)";
            case 5:
                return "Phase 5 (Harvest)";
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

            // Always allow start date (day 1) to be accessible
            if (clickedDate.equals(programStartDate)) {
                openDailyTask(clickedDate);
                return;
            }

            // Check if date is accessible (all previous days completed)
            // Use parseCalendarDay to ensure consistent CalendarDay creation
            CalendarDay clickedDay = parseCalendarDay(clickedDate);
            if (clickedDay == null || !accessibleDates.contains(clickedDay)) {
                // Also check if it's the start date (should always be accessible)
                if (!clickedDate.equals(programStartDate)) {
                    Toast.makeText(this, "Please complete previous day tasks first", Toast.LENGTH_LONG).show();
                    // Deselect the date to prevent selection
                    widget.setDateSelected(date, false);
                    return;
                }
            }

            // Date is accessible, open DailyTask
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
        startActivity(taskIntent);
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
                
                if (!LocalDataManager.isOnline(this)) {
                    Toast.makeText(this, "Showing offline task data", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void processTaskStatusesMap(java.util.HashMap<String, String> taskStatuses, Date today) {
        // Process dates and determine accessible dates
        try {
            Date start = sdf.parse(programStartDate);
            if (start == null) return;
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            
            // Calculate accessible dates: a date is accessible if all previous dates are completed or missed
            Calendar checkCal = Calendar.getInstance();
            checkCal.setTime(start);
            Date endDate = today;
            // Add some future dates to check
            Calendar futureCal = Calendar.getInstance();
            futureCal.setTime(today);
            futureCal.add(Calendar.DAY_OF_YEAR, 90); // Check up to 90 days ahead
            if (futureCal.getTime().after(endDate)) {
                endDate = futureCal.getTime();
            }
            
            while (!checkCal.getTime().after(endDate)) {
                String dateKey = sdf.format(checkCal.getTime());
                CalendarDay cd = parseCalendarDay(dateKey);
                if (cd == null) {
                    checkCal.add(Calendar.DAY_OF_YEAR, 1);
                    continue;
                }
                
                // Always allow start date (day 1) to be accessible
                if (dateKey.equals(programStartDate)) {
                    accessibleDates.add(cd);
                    // Process status for display
                    String status = taskStatuses.get(dateKey);
                    if ("completed".equals(status)) {
                        completedDates.add(cd);
                        // When Day 1 is completed, make Day 2 accessible
                        Calendar nextCal = Calendar.getInstance();
                        nextCal.setTime(checkCal.getTime());
                        nextCal.add(Calendar.DAY_OF_YEAR, 1);
                        String nextDateKey = sdf.format(nextCal.getTime());
                        CalendarDay nextDay = parseCalendarDay(nextDateKey);
                        if (nextDay != null) {
                            accessibleDates.add(nextDay);
                        }
                    } else if ("missed".equals(status)) {
                        missedDates.add(cd);
                        // When Day 1 is missed, make Day 2 accessible
                        Calendar nextCal = Calendar.getInstance();
                        nextCal.setTime(checkCal.getTime());
                        nextCal.add(Calendar.DAY_OF_YEAR, 1);
                        String nextDateKey = sdf.format(nextCal.getTime());
                        CalendarDay nextDay = parseCalendarDay(nextDateKey);
                        if (nextDay != null) {
                            accessibleDates.add(nextDay);
                        }
                    } else if ("skipped".equals(status)) {
                        skippedDates.add(cd);
                        Calendar nextCal = Calendar.getInstance();
                        nextCal.setTime(checkCal.getTime());
                        nextCal.add(Calendar.DAY_OF_YEAR, 1);
                        String nextDateKey = sdf.format(nextCal.getTime());
                        CalendarDay nextDay = parseCalendarDay(nextDateKey);
                        if (nextDay != null) {
                            accessibleDates.add(nextDay);
                        }
                    } else if ("pending".equals(status)) {
                        Date taskDate = sdf.parse(dateKey);
                        if (taskDate != null && taskDate.before(today)) {
                            // Mark as missed if past due (only if online)
                            if (logsRef != null && LocalDataManager.isOnline(this)) {
                                logsRef.child(dateKey).setValue("missed");
                            }
                            missedDates.add(cd);
                        }
                    }
                    checkCal.add(Calendar.DAY_OF_YEAR, 1);
                    continue; // Skip to next date
                }
                
                // Check if this date is accessible
                        // A date is accessible if ALL previous dates are completed or missed
                        boolean isAccessible = true;
                        Calendar prevCal = Calendar.getInstance();
                        prevCal.setTime(start);
                        
                        // Check all previous dates - if ANY previous date is pending, current date is NOT accessible
                        while (prevCal.getTime().before(checkCal.getTime())) {
                            String prevDateKey = sdf.format(prevCal.getTime());
                            String prevStatus = taskStatuses.get(prevDateKey);
                            Date prevDate = prevCal.getTime();
                            
                            // Check previous date status
                            if ("pending".equals(prevStatus)) {
                                // Previous date is pending - current date is NOT accessible
                                isAccessible = false;
                                break;
                            } else if ("completed".equals(prevStatus) || "missed".equals(prevStatus) || "skipped".equals(prevStatus)) {
                                // Previous date is completed or missed - this is good, continue checking
                                // No action needed, continue to next previous date
                            } else if (prevStatus == null) {
                                // Status doesn't exist in Firebase
                                if (prevDate.before(today)) {
                                    // Should have been created but wasn't - not accessible
                                    isAccessible = false;
                                    break;
                                }
                                // If it's today or future and status is null, that's okay (not created yet)
                            } else {
                                // Unknown status - treat as not accessible if before today
                                if (prevDate.before(today)) {
                                    isAccessible = false;
                                    break;
                                }
                            }
                            
                            prevCal.add(Calendar.DAY_OF_YEAR, 1);
                        }
                        
                        // If all previous dates passed the check, the current date is accessible
                        
                        if (isAccessible) {
                            accessibleDates.add(cd);
                            
                            // Process status for display
                            String status = taskStatuses.get(dateKey);
                            if ("completed".equals(status)) {
                                completedDates.add(cd);
                                // When a date is completed, make the next day accessible
                                Calendar nextCal = Calendar.getInstance();
                                nextCal.setTime(checkCal.getTime());
                                nextCal.add(Calendar.DAY_OF_YEAR, 1);
                                String nextDateKey = sdf.format(nextCal.getTime());
                                CalendarDay nextDay = parseCalendarDay(nextDateKey);
                                if (nextDay != null) {
                                    accessibleDates.add(nextDay);
                                }
                            } else if ("missed".equals(status)) {
                                missedDates.add(cd);
                                // When a date is missed, make the next day accessible (user can continue)
                                Calendar nextCal = Calendar.getInstance();
                                nextCal.setTime(checkCal.getTime());
                                nextCal.add(Calendar.DAY_OF_YEAR, 1);
                                String nextDateKey = sdf.format(nextCal.getTime());
                                CalendarDay nextDay = parseCalendarDay(nextDateKey);
                                if (nextDay != null) {
                                    accessibleDates.add(nextDay);
                                }
                            } else if ("skipped".equals(status)) {
                                skippedDates.add(cd);
                                Calendar nextCal = Calendar.getInstance();
                                nextCal.setTime(checkCal.getTime());
                                nextCal.add(Calendar.DAY_OF_YEAR, 1);
                                String nextDateKey = sdf.format(nextCal.getTime());
                                CalendarDay nextDay = parseCalendarDay(nextDateKey);
                                if (nextDay != null) {
                                    accessibleDates.add(nextDay);
                                }
                            } else if ("pending".equals(status)) {
                                Date taskDate = sdf.parse(dateKey);
                                if (taskDate != null && taskDate.before(today)) {
                                    // Mark as missed if past due
                                    logsRef.child(dateKey).setValue("missed");
                                    missedDates.add(cd);
                                }
                            }
                        } else {
                            // Date is not accessible - ensure it's not in accessibleDates
                            accessibleDates.remove(cd);
                        }
                        
                        checkCal.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    
                    // Post-process: Explicitly make next day accessible for all completed/missed dates
                    // This ensures that when a date is completed, the next day is always accessible
                    Calendar postCal = Calendar.getInstance();
                    postCal.setTime(start);
                    while (!postCal.getTime().after(endDate)) {
                        String dateKey = sdf.format(postCal.getTime());
                        String status = taskStatuses.get(dateKey);
                        
                        // If this date is completed, missed, or skipped, make the next day accessible
                        if ("completed".equals(status) || "missed".equals(status) || "skipped".equals(status)) {
                            Calendar nextCal = Calendar.getInstance();
                            nextCal.setTime(postCal.getTime());
                            nextCal.add(Calendar.DAY_OF_YEAR, 1);
                            String nextDateKey = sdf.format(nextCal.getTime());
                            CalendarDay nextDay = parseCalendarDay(nextDateKey);
                            if (nextDay != null) {
                                accessibleDates.add(nextDay);
                            }
                        }
                        
                        postCal.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                updateTaskWarningBanner();

                // Clear all decorators first
                calendarView.removeDecorators();

                // Re-add phase backgrounds
                addPhaseDecorators();

                // ✅ Add decorator to disable non-accessible dates FIRST (before other decorators)
                calendarView.addDecorator(new DisabledDateDecorator(new HashSet<>(accessibleDates), Workprogram.this));
                
                // ✅ Add dots under numbers
                calendarView.addDecorator(new CompletedDecorator(new HashSet<>(completedDates), Workprogram.this));
                calendarView.addDecorator(new MissedDecorator(new HashSet<>(missedDates), Workprogram.this));
                calendarView.addDecorator(new SkippedDecorator(new HashSet<>(skippedDates), Workprogram.this));
    }



    private boolean isBeforeStartDate(String clickedDate, String startDate) {
        try {
            Date d1 = sdf.parse(clickedDate);
            Date d2 = sdf.parse(startDate);
            return d1 != null && d2 != null && d1.before(d2);
        } catch (ParseException e) { e.printStackTrace(); return false; }
    }

    private CalendarDay parseCalendarDay(String dateStr) {
        try {
            Date d = sdf.parse(dateStr);
            if (d != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                return CalendarDay.from(cal);
            }
        } catch (ParseException e) { e.printStackTrace(); }
        return null;
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
    }


    /**
     * Updates the cultivar information in the header card
     */
    private void updateCultivarInfo(String cultivar, String startDate) {
        // Format date according to user preference for display
        String displayDate = startDate;
        try {
            Date dateObj = sdf.parse(startDate);
            SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(this);
            displayDate = displayFormat.format(dateObj);
        } catch (ParseException e) {
            // Keep original format if parsing fails
        }
        if (cultivarNameText != null) {
            cultivarNameText.setText(cultivar);
        }
        if (startDateText != null) {
            startDateText.setText("Start Date: " + displayDate);
        }
        if (headerStartDate != null) {
            headerStartDate.setText("Start Date: " + displayDate);
        }
        if (cultivarImage != null) {
            // Set cultivar-specific image (for now using default, can be extended)
            cultivarImage.setImageResource(getCultivarImageResource(cultivar));
        }
    }

    /**
     * Gets the image resource for a specific cultivar
     * Currently returns default logo, but can be extended to map specific cultivars to images
     */
    private int getCultivarImageResource(String cultivar) {
        // For now, use default logo for all cultivars
        // This can be extended to map specific cultivars to specific images
        // Example:
        // if (cultivar.contains("Victory")) return R.mipmap.victory_tomato;
        // if (cultivar.contains("HOPE")) return R.mipmap.hope_tomato;
        // etc.
        return R.mipmap.ic_logo;
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
