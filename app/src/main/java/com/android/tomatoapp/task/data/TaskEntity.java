package com.android.tomatoapp.task.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskEntity {
    @PrimaryKey
    @NonNull
    public String id; // Composite: programId + "_" + dateKey

    public String userId;
    public String programId;
    public String dateKey; // Format: "yyyy-MM-dd"
    public String taskName;
    public String category;
    public String iconType;
    public int dayNumber;
    public String phase;
    public String status; // "pending", "completed", "missed", "skipped"
    public long lastSynced;

    public TaskEntity() {
        // Default constructor required for Room
        this.id = "";
    }

    @Ignore
    public TaskEntity(@NonNull String id, String userId, String programId, String dateKey,
                     String taskName, String category, String iconType, int dayNumber,
                     String phase, String status, long lastSynced) {
        this.id = id;
        this.userId = userId;
        this.programId = programId;
        this.dateKey = dateKey;
        this.taskName = taskName;
        this.category = category;
        this.iconType = iconType;
        this.dayNumber = dayNumber;
        this.phase = phase;
        this.status = status;
        this.lastSynced = lastSynced;
    }
}

