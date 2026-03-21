package com.android.tomatoapp;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyTask extends BaseDrawerActivity {

    private TextView cultivarNameHeader, cultivarDescription, dateHeader, taskSectionTitle, taskCountText;
    private ImageView cultivarImageHeader;
    private MaterialButton btnComplete;
    private MaterialButton btnSkip;
    private MaterialButton btnMonitor;
    private MaterialButton btnDailyExpenses;
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskModel> taskList = new ArrayList<>();
    private boolean[] checkedTasks; // Track which tasks are checked

    private boolean isNewProgram = false;
    private String cultivar, date, programId, startDate;
    private String growthHabit;
    private int dayNumber;
    private int maturityDays;
    private DatabaseReference taskRef;


    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_task);

        // Initialize views
        cultivarNameHeader = findViewById(R.id.cultivarNameHeader);
        cultivarDescription = findViewById(R.id.cultivarDescription);
        dateHeader = findViewById(R.id.dateHeader);
        cultivarImageHeader = findViewById(R.id.cultivarImageHeader);
        taskSectionTitle = findViewById(R.id.taskSectionTitle);
        taskCountText = findViewById(R.id.taskCountText);
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

        // Fallback if startDate is missing
        if (startDate == null || startDate.isEmpty()) {
            startDate = sdf.format(new Date());
        }

        // Detect whether existing or new workprogram
        if (programId == null || programId.isEmpty()) {
            isNewProgram = true;
        }

        // Update header
        if (cultivar != null) {
            cultivarNameHeader.setText(cultivar);
            cultivarDescription.setText("Off-season planting tasks for optimal growth");
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

        // Calculate day number and get tasks
        dayNumber = calculateDayNumber(startDate, date);
        if (dayNumber > 0 && growthHabit != null && maturityDays > 0) {
            taskList = TaskSchedule.getTasksForDay(growthHabit, maturityDays, dayNumber);
        }

        // Initialize checked tasks array
        checkedTasks = new boolean[taskList.size()];

        // Setup RecyclerView
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setAdapter(taskAdapter);

        // Update task count
        updateTaskCount();

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

        setupDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daily Tasks");
        }
    }

    private void updateTaskCount() {
        int count = taskList.size();
        int checkedCount = getCheckedCount();
        if (taskCountText != null) {
            taskCountText.setText(checkedCount + "/" + count + " completed");
        }
        
        // Update button text
        if (btnComplete != null) {
            if (checkedCount == count && count > 0) {
                btnComplete.setText("All Tasks Completed!");
                btnComplete.setEnabled(false);
            } else {
                btnComplete.setText("Mark All Tasks Complete");
                btnComplete.setEnabled(true);
            }
        }
    }

    private int getCheckedCount() {
        int count = 0;
        if (checkedTasks != null) {
            for (boolean checked : checkedTasks) {
                if (checked) count++;
            }
        }
        return count;
    }

    private boolean areAllTasksChecked() {
        if (checkedTasks == null || checkedTasks.length == 0) return false;
        for (boolean checked : checkedTasks) {
            if (!checked) return false;
        }
        return true;
    }

    private void markAllTasksComplete() {
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

    private void openMonitoring() {
        if (isNewProgram || programId == null) {
            Toast.makeText(this, "Save the work program to log monitoring entries.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dayNumber <= 0 || maturityDays <= 0) {
            Toast.makeText(this, "Monitoring unavailable for this date.", Toast.LENGTH_SHORT).show();
            return;
        }
        int phaseNumber = TaskSchedule.getPhaseNumber(maturityDays, dayNumber);
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

        TaskAdapter(List<TaskModel> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            TaskModel task = tasks.get(position);
            holder.taskName.setText(task.taskName);
            holder.taskSubtext.setText(task.category + " • " + task.phase);
            
            // Set icon based on task type
            int iconDrawable = getIconDrawable(task.iconType);
            holder.taskIcon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), iconDrawable));
            
            // Set checkbox state
            holder.taskCheckbox.setChecked(checkedTasks[position]);
            
            // Set checkbox listener
            holder.taskCheckbox.setOnCheckedChangeListener(null); // Prevent triggering during binding
            holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkedTasks[position] = isChecked;
                updateTaskCount();
                
                // Auto-complete when all tasks are checked
                if (areAllTasksChecked() && !isNewProgram && taskRef != null) {
                    markAllTasksComplete();
                }
            });
            
            // Update text style if checked
            if (checkedTasks[position]) {
                holder.taskName.setAlpha(0.6f);
                holder.taskSubtext.setAlpha(0.6f);
            } else {
                holder.taskName.setAlpha(1.0f);
                holder.taskSubtext.setAlpha(1.0f);
            }
        }

        @Override
        public int getItemCount() {
            return tasks.size();
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
            MaterialCheckBox taskCheckbox;

            TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                taskIcon = itemView.findViewById(R.id.taskIcon);
                taskName = itemView.findViewById(R.id.taskName);
                taskSubtext = itemView.findViewById(R.id.taskSubtext);
                taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
