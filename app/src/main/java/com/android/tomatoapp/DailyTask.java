package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.HashMap;

public class DailyTask extends AppCompatActivity {

    private TextView dailyTaskTitle, taskList;
    private Button btnComplete;

    private boolean isNewProgram = false;

    // 🔹 Drawer
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_task);

        dailyTaskTitle = findViewById(R.id.dailyTaskTitle);
        taskList = findViewById(R.id.taskList);
        btnComplete = findViewById(R.id.btnComplete);

        // 🔹 Get intent extras
        String cultivar = getIntent().getStringExtra("cultivar");
        String date = getIntent().getStringExtra("date");
        String programId = getIntent().getStringExtra("programId");

        // 🔹 Detect whether this came from a saved program or a new program
        if (programId == null || programId.isEmpty()) {
            isNewProgram = true;
        }

        dailyTaskTitle.setText("Tasks for " + cultivar + " on " + date);

        // 🔹 Example cultivar tasks
        HashMap<String, String> exampleTasks = new HashMap<>();
        exampleTasks.put("Day 1", "- Prepare soil\n- Irrigation\n- Seed sowing");
        exampleTasks.put("Day 5", "- Fertilizer application\n- Weeding");
        exampleTasks.put("Day 30", "- Staking\n- Pest check");

        taskList.setText("- Watering\n- Fertilizing\n- Pest monitoring");

        // 🔹 Firebase ref only if it's a saved program
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

        // 🔹 Handle completion
        btnComplete.setOnClickListener(v -> {
            if (isNewProgram) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("date", date);
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(this, "Task marked complete (new program).", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                taskRef.child(date).setValue("completed")
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Marked as complete!", Toast.LENGTH_SHORT).show();

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("date", date);
                            resultIntent.putExtra("programId", programId);
                            setResult(RESULT_OK, resultIntent);

                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        });

        // 🔹 Drawer setup
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

    // 🔹 Inflate the back button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    // 🔹 Handle toggle + back button
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
