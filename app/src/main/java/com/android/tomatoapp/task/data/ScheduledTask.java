package com.android.tomatoapp.task.data;

import java.time.LocalDate;

/**
 * A task occurrence generated from a template.
 */
public final class ScheduledTask {
    public final String id;
    public final int phase;
    public final TaskTemplate.Type type;
    public final String title;
    public final LocalDate startDate;
    public final LocalDate endDate;
    public final String trigger;
    public final int frequencyDays;
    public final String variety;
    public final String details;

    public ScheduledTask(String id, int phase, TaskTemplate.Type type, String title,
                         LocalDate startDate, LocalDate endDate, String trigger,
                         int frequencyDays, String variety, String details) {
        this.id = id;
        this.phase = phase;
        this.type = type;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.trigger = trigger;
        this.frequencyDays = frequencyDays;
        this.variety = variety;
        this.details = details;
    }

    public boolean occursOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public TaskModel toTaskModel(int dayNumber) {
        String category = type == TaskTemplate.Type.NOTE ? "Note"
                : type == TaskTemplate.Type.MAINTENANCE ? "Maintenance" : "Main task";
        String icon = type == TaskTemplate.Type.MAINTENANCE ? "maintenance" : "land";
        return new TaskModel(id, title, category, icon, dayNumber, "Phase " + phase,
                startDate.toString(), endDate.toString(), type.name(), details);
    }
}
