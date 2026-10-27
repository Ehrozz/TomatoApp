package com.android.tomatoapp.task.ui;

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

    private boolean isNewProgram = false;
    private String cultivar, date, programId, startDate;
    private String growthHabit;
    private int dayNumber;
    private int maturityDays;
    private String seedsConfirmedDate;
    private String sowingDate;
    private String transplantDate;
    private String stopDate;
    private DatabaseReference taskRef;
    private boolean dayCompleted;


    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_task);

        // Initialize views
        cultivarNameHeader = findViewById(R.id.cultivarNameHeader);
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
        seedsConfirmedDate = getIntent().getStringExtra("seedsConfirmedDate");
        sowingDate = getIntent().getStringExtra("sowingDate");
        transplantDate = getIntent().getStringExtra("transplantDate");
        stopDate = getIntent().getStringExtra("stopDate");
        int adjustedDayNumber = getIntent().getIntExtra("adjustedDayNumber", 0);

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
        dayNumber = adjustedDayNumber > 0
                ? adjustedDayNumber
                : calculateDayNumber(startDate, date);
        if (dayNumber > 0 && maturityDays > 0) {
            taskList = getScheduledTasksForDate();
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

        setupBottomNavigation();
        loadPersistedDailyState(currentUser);
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

    private void saveMilestone(String field, String value) {
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
            double area = getIntent().getDoubleExtra("farmAreaHectares", 1.0);
            TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                    planting, area, growthHabit, seeds, sowing, transplant, stop, current,
                    TomatoWorkProgramScheduler.DEFAULT_HECTARES_PER_PLOWING_DAY);
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
            if (viewed.isBefore(start) || viewed.isAfter(java.time.LocalDate.now())) return false;
            long day = java.time.temporal.ChronoUnit.DAYS.between(start, viewed);
            return day >= 0 && day % 3 == 0;
        } catch (RuntimeException error) {
            return false;
        }
    }

    private void maybeAskAboutSeeds() {
        if (!isSeedPromptDate() || isNewProgram || programId == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Do you already have seeds?")
                .setMessage("Need help finding seeds? Ask your local agri-supply or municipal agriculture office.")
                .setPositiveButton("Yes", (dialog, which) -> saveMilestone("seedsConfirmedDate", date))
                .setNegativeButton("No, not yet", null)
                .show();
    }

    private void loadPersistedDailyState(FirebaseUser currentUser) {
        if (isNewProgram || currentUser == null || programId == null) {
            maybeAskAboutSeeds();
            return;
        }
        DatabaseReference programRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("workPrograms").child(programId);
        programRef.get().addOnSuccessListener(snapshot -> {
            String savedSeeds = readOptionalString(snapshot, "seedsConfirmedDate");
            if (savedSeeds != null) seedsConfirmedDate = savedSeeds;
            String savedSowing = readOptionalString(snapshot, "sowingDate");
            if (savedSowing != null) sowingDate = savedSowing;
            String savedTransplant = readOptionalString(snapshot, "transplantDate");
            if (savedTransplant != null) transplantDate = savedTransplant;
            String savedStop = readOptionalString(snapshot, "stopDate");
            if (savedStop != null) stopDate = savedStop;

            taskRef.child(date).get().addOnSuccessListener(taskSnapshot -> {
                dayCompleted = "completed".equals(taskSnapshot.getValue(String.class));
                if (dayCompleted) {
                    for (int i = 0; i < taskList.size(); i++) {
                        if (isCountedTask(taskList.get(i))) checkedTasks[i] = true;
                    }
                }
                refreshTaskList();
                updateTaskCount();
                updateEmptyTaskState();
                maybeAskAboutSeeds();
            }).addOnFailureListener(error -> maybeAskAboutSeeds());
        }).addOnFailureListener(error -> maybeAskAboutSeeds());
    }

    private String readOptionalString(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value == null ? null : String.valueOf(value);
    }

    private void refreshTaskList() {
        boolean wasEmpty = taskList.isEmpty();
        taskList = getScheduledTasksForDate();
        boolean wasCompleted = dayCompleted;
        checkedTasks = new boolean[taskList.size()];
        if (wasCompleted) {
        for (int i = 0; i < taskList.size(); i++) {
            checkedTasks[i] = isCountedTask(taskList.get(i));
        }
        }
        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setAdapter(taskAdapter);
        updateTaskCount();
        updateEmptyTaskState();
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
                    java.time.LocalDate.parse(date));
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

    private boolean isMilestoneHistory(TaskModel task) {
        if (task.details != null && (task.details.contains("confirmed on this date")
                || task.details.contains("recorded on this date"))) {
            return true;
        }
        return "stop-indicator".equals(task.id) && date != null && date.equals(stopDate);
    }

    private void confirmMilestone(TaskModel task) {
        if (date == null) {
            Toast.makeText(this, "No date selected.", Toast.LENGTH_SHORT).show();
            return;
        }
        String field;
        if ("milestone-seeds".equals(task.id)) {
            field = "seedsConfirmedDate";
        } else if ("milestone-sowing".equals(task.id)) {
            field = "sowingDate";
        } else if ("milestone-transplant".equals(task.id)) {
            field = "transplantDate";
        } else if ("stop-indicator".equals(task.id)) {
            field = "stopDate";
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
                boolean isMainSection = title.startsWith("Main tasks");
                holder.sectionCount.setVisibility(isMainSection ? View.VISIBLE : View.GONE);
                if (isMainSection) {
                    holder.sectionCount.setText(getCheckedCount() + "/"
                            + getActionTaskCount() + " completed");
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
            holder.taskSubtext.setText(subtext);
            
            // Set icon based on task type
            int iconDrawable = getIconDrawable(task.iconType);
            holder.taskIcon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), iconDrawable));
            
            boolean isNote = "NOTE".equals(task.type);
            boolean isMilestone = "MILESTONE".equals(task.type);
            holder.milestoneAction.setVisibility(isMilestone && !isMilestoneHistory(task)
                    ? View.VISIBLE : View.GONE);
            holder.milestoneAction.setOnClickListener(v -> confirmMilestone(task));
            holder.taskCheckbox.setEnabled(!isNote && !isMilestone && !dayCompleted);
            holder.taskCheckbox.setVisibility(isNote || isMilestone ? View.GONE : View.VISIBLE);
            holder.taskCheckbox.setChecked(!isNote && !isMilestone && checkedTasks[taskIndex]);
            
            // Set checkbox listener
            holder.taskCheckbox.setOnCheckedChangeListener(null); // Prevent triggering during binding
            holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isNote || isMilestone || dayCompleted) return;
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
