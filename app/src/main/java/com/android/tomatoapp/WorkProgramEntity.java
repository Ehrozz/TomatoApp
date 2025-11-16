package com.android.tomatoapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Local representation of a work program for analytics.
 * Mirrors the Firebase structure and adds local-only fields if needed.
 */
@Entity(tableName = "work_programs")
public class WorkProgramEntity {

    @PrimaryKey
    @NonNull
    public String id; // Firebase programId

    public String userId;
    public String cultivarName;
    public double areaSize;
    public String startingDate;

    // Serialized JSON for phases and detection histories to keep it simple
    public String phasesJson;
    public String detectionHistoriesJson;

    public double projectedIncome;
    public double projectedExpenses;
    public double adjustedIncome;
    public double adjustedExpenses;
    public double phase1Completion;
    public double phase2Completion;
    public double phase3Completion;
    public double phase4Completion;
    public double phase5Completion;

    // Task tracking metrics
    public int totalTasks;
    public int completedTasks;
    public int missedTasks;
    public int skippedTasks;
    public double completionRate;

    public WorkProgramEntity(@NonNull String id,
                             String userId,
                             String cultivarName,
                             double areaSize,
                             String startingDate,
                             String phasesJson,
                             String detectionHistoriesJson,
                             double projectedIncome,
                             double projectedExpenses,
                             double adjustedIncome,
                             double adjustedExpenses,
                             double phase1Completion,
                             double phase2Completion,
                             double phase3Completion,
                             double phase4Completion,
                             double phase5Completion,
                             int totalTasks,
                             int completedTasks,
                             int missedTasks,
                             int skippedTasks,
                             double completionRate) {
        this.id = id;
        this.userId = userId;
        this.cultivarName = cultivarName;
        this.areaSize = areaSize;
        this.startingDate = startingDate;
        this.phasesJson = phasesJson;
        this.detectionHistoriesJson = detectionHistoriesJson;
        this.projectedIncome = projectedIncome;
        this.projectedExpenses = projectedExpenses;
        this.adjustedIncome = adjustedIncome;
        this.adjustedExpenses = adjustedExpenses;
        this.phase1Completion = phase1Completion;
        this.phase2Completion = phase2Completion;
        this.phase3Completion = phase3Completion;
        this.phase4Completion = phase4Completion;
        this.phase5Completion = phase5Completion;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.missedTasks = missedTasks;
        this.skippedTasks = skippedTasks;
        this.completionRate = completionRate;
    }
}


