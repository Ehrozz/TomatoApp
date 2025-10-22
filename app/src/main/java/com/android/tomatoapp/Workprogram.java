package com.android.tomatoapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

public class Workprogram extends AppCompatActivity {

    private CardView cultivarCard;
    private Spinner cultivarSpinner;
    private EditText landAreaInput;
    private DatePicker startDatePicker;
    private Button btnSubmitForm;
    private TextView wkTitle;
    private MaterialCalendarView calendarView;

    private String selectedCultivar = "";
    private String selectedDate = "";
    private String landArea = "";
    private String programStartDate = "";
    private String programId = "";

    private DatabaseReference dbRef;
    private DatabaseReference logsRef;
    private String userId;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private HashSet<CalendarDay> completedDates = new HashSet<>();
    private HashSet<CalendarDay> missedDates = new HashSet<>();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

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
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Work Program");
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) finish();
            drawerLayout.closeDrawers();
            return true;
        });

        // Views
        cultivarCard = findViewById(R.id.floatingFormCard);
        cultivarSpinner = findViewById(R.id.cultivarSpinner);
        landAreaInput = findViewById(R.id.landAreaInput);
        startDatePicker = findViewById(R.id.startDatePicker);
        btnSubmitForm = findViewById(R.id.btnSelectCultivar);
        wkTitle = findViewById(R.id.wkTitle);
        calendarView = findViewById(R.id.CalendarView);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("workPrograms");

        Intent intent = getIntent();
        String cultivar = intent.getStringExtra("cultivar");
        String startDate = intent.getStringExtra("startDate");
        String passedProgramId = intent.getStringExtra("programId");

        if (cultivar != null && startDate != null && passedProgramId != null) {
            // Existing program
            cultivarCard.setVisibility(CardView.GONE);
            wkTitle.setText("Work Program\nCultivar: " + cultivar + "\nDate Start : " + startDate);
            selectedCultivar = cultivar;
            programStartDate = startDate;
            programId = passedProgramId;

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
            wkTitle.setText("Add New Work Program");

            String[] cultivarNames = new String[cultivarsData.length];
            for (int i = 0; i < cultivarsData.length; i++) {
                cultivarNames[i] = cultivarsData[i][0]; // cultivar name is column 0
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, cultivarNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cultivarSpinner.setAdapter(adapter);

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
                if (!landArea.matches("\\d+")) {
                    Toast.makeText(this, "Land area must be numbers only", Toast.LENGTH_SHORT).show();
                    return;
                }

                String id = dbRef.push().getKey();
                if (id == null) {
                    Toast.makeText(this, "Error generating ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                programId = id;

                WorkProgramModel model = new WorkProgramModel(selectedCultivar, selectedDate, landArea);
                dbRef.child(id).setValue(model).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Work Program saved successfully!", Toast.LENGTH_LONG).show();

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

                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show());

                wkTitle.setText("Work Program\nCultivar: " + selectedCultivar +
                        "\nStart Date: " + selectedDate +
                        "\nLand Area: " + landArea);
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


    private void setCalendarClickListener() {
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String clickedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    date.getYear(), date.getMonth() + 1, date.getDay());
            if (isBeforeStartDate(clickedDate, programStartDate)) {
                Toast.makeText(this, "No tasks available before start date!", Toast.LENGTH_SHORT).show();
                return;
            }

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
        });
    }

    private void attachLogsListener() {
        if (logsRef == null) return;
        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                completedDates.clear();
                missedDates.clear();
                Date today = new Date();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String dateKey = child.getKey();
                    String status = child.getValue(String.class);
                    if (status == null) continue;
                    CalendarDay cd = parseCalendarDay(dateKey);
                    if (cd == null) continue;

                    try {
                        Date taskDate = sdf.parse(dateKey);
                        if ("completed".equals(status)) {
                            completedDates.add(cd);
                        } else if ("missed".equals(status)) {
                            missedDates.add(cd);
                        } else if ("pending".equals(status) && taskDate != null && taskDate.before(today)) {
                            logsRef.child(dateKey).setValue("missed");
                            missedDates.add(cd);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // Clear all decorators first
                calendarView.removeDecorators();

                // Re-add phase backgrounds
                addPhaseDecorators();

                // ✅ Add dots under numbers
                calendarView.addDecorator(new CompletedDecorator(new HashSet<>(completedDates), Workprogram.this));
                calendarView.addDecorator(new MissedDecorator(new HashSet<>(missedDates), Workprogram.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
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
        getMenuInflater().inflate(R.menu.menu_back, menu); return true;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) return true;
        else if (item.getItemId() == R.id.action_back) { finish(); return true; }
        return super.onOptionsItemSelected(item);
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
