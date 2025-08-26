package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyTask extends AppCompatActivity {

    private TextView dailyTaskTitle, taskList;
    private Button btnComplete;

    private boolean isNewProgram = false;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_task);

        dailyTaskTitle = findViewById(R.id.dailyTaskTitle);
        taskList = findViewById(R.id.taskList);
        btnComplete = findViewById(R.id.btnComplete);

        String cultivar = getIntent().getStringExtra("cultivar");
        String date = getIntent().getStringExtra("date");
        String programId = getIntent().getStringExtra("programId");

        String startDate = getIntent().getStringExtra("programStartDate");

        String growthHabit = getIntent().getStringExtra("growthHabit");
        int maturityDays = getIntent().getIntExtra("maturityDays", 0);

        // Fallback if startDate is missing
        if (startDate == null || startDate.isEmpty()) {
            startDate = sdf.format(new Date());
        }

        // Detect whether existing or new workprogram
        if (programId == null || programId.isEmpty()) {
            isNewProgram = true;
        }

        dailyTaskTitle.setText("Tasks for " + cultivar + " on " + date);

        int dayNumber = calculateDayNumber(startDate, date);

        List<String> tasks;
        if (dayNumber > 0) {
            tasks = TaskSchedule.getTasksForDay(growthHabit, maturityDays, dayNumber);
        } else {
            tasks = null;
        }

        if (tasks == null || tasks.isEmpty()) {
            taskList.setText("No tasks scheduled for today.");
        } else {
            taskList.setText("- " + TextUtils.join("\n- ", tasks));
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference taskRef;
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

        DatabaseReference finalTaskRef = taskRef;
        String finalStartDate = startDate;
        btnComplete.setOnClickListener(v -> {
            if (isNewProgram) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("date", date);
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(this, "Task marked complete (new program).", Toast.LENGTH_SHORT).show();
                finish();
            } else if (finalTaskRef != null) {
                finalTaskRef.child(date).setValue("completed")
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Marked as complete!", Toast.LENGTH_SHORT).show();

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("date", date);
                            resultIntent.putExtra("programId", programId);
                            resultIntent.putExtra("programStartDate", finalStartDate);
                            setResult(RESULT_OK, resultIntent);

                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tomato App");

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, Login.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });
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

    // back button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    // Handle toggle & back button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_back) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
