package com.android.tomatoapp.task.data;

public class TaskModel {
    public String id;
    public String taskName;
    public String category;
    public String iconType; // "land", "water", "fertilizer", "pest", "harvest", "maintenance"
    public int dayNumber;
    public String phase;
    public String startDate;
    public String endDate;
    public String type;
    public String details;

    public TaskModel(String taskName, String category, String iconType, int dayNumber, String phase) {
        this.taskName = taskName;
        this.category = category;
        this.iconType = iconType;
        this.dayNumber = dayNumber;
        this.phase = phase;
    }

    public TaskModel(String taskName, String category, String iconType, int dayNumber, String phase,
                     String startDate, String endDate, String type, String details) {
        this(taskName, category, iconType, dayNumber, phase);
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.details = details;
    }

    public TaskModel(String id, String taskName, String category, String iconType, int dayNumber,
                     String phase, String startDate, String endDate, String type, String details) {
        this(taskName, category, iconType, dayNumber, phase, startDate, endDate, type, details);
        this.id = id;
    }
}
