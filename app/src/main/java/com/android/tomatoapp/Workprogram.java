package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class Workprogram extends AppCompatActivity {

    private CardView cultivarCard;
    private Spinner cultivarSpinner;
    private Button btnSelectCultivar;
    private TextView wkTitle;
    private MaterialCalendarView calendarView;

    private String selectedCultivar = "";
    private String selectedDate = "";
    private String programStartDate = "";
    private String programId = "";

    private DatabaseReference dbRef;
    private DatabaseReference logsRef;
    private String userId;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private HashSet<CalendarDay> completedDates = new HashSet<>();
    private HashSet<CalendarDay> missedDates = new HashSet<>();

    // Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workprogram);

        // --- ActionBar + Drawer setup ---
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tomato App");

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // --- Existing Workprogram ---
        cultivarCard = findViewById(R.id.cultivarCard);
        cultivarSpinner = findViewById(R.id.cultivarSpinner);
        btnSelectCultivar = findViewById(R.id.btnSelectCultivar);
        wkTitle = findViewById(R.id.wkTitle);
        calendarView = findViewById(R.id.CalendarView);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("workPrograms");

        Intent intent = getIntent();
        String cultivar = intent.getStringExtra("cultivar");
        String startDate = intent.getStringExtra("startDate");
        programId = intent.getStringExtra("programId");

        if (cultivar != null && startDate != null && programId != null) {
            cultivarCard.setVisibility(CardView.GONE);
            wkTitle.setText("Work Program\nCultivar: " + cultivar + "\nStart Date: " + startDate);
            selectedCultivar = cultivar;
            programStartDate = startDate;

            logsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("routineLogs")
                    .child(programId)
                    .child("tasks");

            attachLogsListener();

            setCalendarClickListener();

        } else {
            cultivarCard.setVisibility(CardView.VISIBLE);
            wkTitle.setText("Add New Work Program");

            List<String> cultivars = Arrays.asList("Diamante Max", "Athena", "Ilocos Red", "Apollo", "Ruby");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, cultivars);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cultivarSpinner.setAdapter(adapter);

            calendarView.setOnDateChangedListener((widget, date, selected) -> {
                selectedDate = formatDate(date);
            });

            btnSelectCultivar.setOnClickListener(v -> {
                selectedCultivar = cultivarSpinner.getSelectedItem().toString();

                if (selectedCultivar.isEmpty() || selectedDate.isEmpty()) {
                    Toast.makeText(this, "Please select cultivar and start date", Toast.LENGTH_SHORT).show();
                    return;
                }

                String id = dbRef.push().getKey();
                if (id == null) {
                    Toast.makeText(this, "Error generating ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                programId = id;

                WorkProgramModel model = new WorkProgramModel(selectedCultivar, selectedDate);
                dbRef.child(id).setValue(model).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Work Program saved successfully!", Toast.LENGTH_LONG).show();

                    // Initialize logsRef for new program
                    logsRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(userId)
                            .child("routineLogs")
                            .child(programId)
                            .child("tasks");

                    logsRef.child(selectedDate).setValue("pending");

                    attachLogsListener();

                    programStartDate = selectedDate;
                    setCalendarClickListener();

                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

                wkTitle.setText("Work Program\nCultivar: " + selectedCultivar + "\nStart Date: " + selectedDate);
                cultivarCard.setVisibility(CardView.GONE);
            });
        }
    }

    // --- Set click listener for calendar ---
    private void setCalendarClickListener() {
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String clickedDate = formatDate(date);
            if (isBeforeStartDate(clickedDate, programStartDate)) {
                Toast.makeText(this, "No tasks available before start date!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent taskIntent = new Intent(Workprogram.this, DailyTask.class);
            taskIntent.putExtra("cultivar", selectedCultivar);
            taskIntent.putExtra("date", clickedDate);
            taskIntent.putExtra("programId", programId);
            startActivity(taskIntent);
        });
    }

    // --- Attach ValueEventListener for logsRef ---
    private void attachLogsListener() {
        if (logsRef == null) return;

        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                completedDates.clear();
                missedDates.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String dateKey = child.getKey();
                    String status = child.getValue(String.class);
                    if (status == null) continue;

                    CalendarDay cd = parseCalendarDay(dateKey);
                    if (cd == null) continue;

                    if ("completed".equals(status)) {
                        completedDates.add(cd);
                    } else if ("missed".equals(status)) {
                        missedDates.add(cd);
                    }
                }

                calendarView.removeDecorators();
                calendarView.addDecorator(new CompletedDecorator(completedDates, Workprogram.this));
                calendarView.addDecorator(new MissedDecorator(missedDates, Workprogram.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // --- Format CalendarDay to yyyy-MM-dd ---
    private String formatDate(CalendarDay date) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d",
                date.getYear(), date.getMonth() + 1, date.getDay());
    }

    private boolean isBeforeStartDate(String clickedDate, String startDate) {
        try {
            Date d1 = sdf.parse(clickedDate);
            Date d2 = sdf.parse(startDate);
            return d1 != null && d2 != null && d1.before(d2);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private CalendarDay parseCalendarDay(String dateStr) {
        try {
            Date d = sdf.parse(dateStr);
            if (d != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(d);
                return CalendarDay.from(cal);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Back button menu ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.action_back) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Data model ---
    public static class WorkProgramModel {
        public String cultivar;
        public String startDate;

        public WorkProgramModel() { }

        public WorkProgramModel(String cultivar, String startDate) {
            this.cultivar = cultivar;
            this.startDate = startDate;
        }
    }
}
