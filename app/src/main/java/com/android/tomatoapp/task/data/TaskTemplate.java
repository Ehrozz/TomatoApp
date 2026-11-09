package com.android.tomatoapp.task.data;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * Rule for producing one or more dated work-program tasks.
 */
public final class TaskTemplate {
    public enum Type { MAIN, MAINTENANCE, NOTE, MILESTONE }

    public final String id;
    public final int phase;
    public final Type type;
    public final String title;
    public final int durationDays;
    @Nullable public final String trigger;
    public final int frequencyDays;
    public final Set<String> varietyFilter;
    public final String details;

    public TaskTemplate(String id, int phase, Type type, String title, int durationDays,
                        @Nullable String trigger, int frequencyDays,
                        @Nullable Set<String> varietyFilter, String details) {
        this.id = id;
        this.phase = phase;
        this.type = type;
        this.title = title;
        this.durationDays = Math.max(1, durationDays);
        this.trigger = trigger;
        this.frequencyDays = Math.max(0, frequencyDays);
        this.varietyFilter = varietyFilter == null
                ? Collections.<String>emptySet() : Collections.unmodifiableSet(varietyFilter);
        this.details = details == null ? "" : details;
    }
}
