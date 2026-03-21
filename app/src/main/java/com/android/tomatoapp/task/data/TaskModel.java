package com.android.tomatoapp.task.data;

public class TaskModel {
    public String taskName;
    public String category;
    public String iconType; // "land", "water", "fertilizer", "pest", "harvest", "maintenance"
    public int dayNumber;
    public String phase;

    public TaskModel(String taskName, String category, String iconType, int dayNumber, String phase) {
        this.taskName = taskName;
        this.category = category;
        this.iconType = iconType;
        this.dayNumber = dayNumber;
        this.phase = phase;
    }
}

