package com.android.tomatoapp.common.utils;

import android.util.Log;

import com.android.tomatoapp.BuildConfig;
import com.android.tomatoapp.task.data.TaskModel;
import com.android.tomatoapp.task.data.TomatoWorkProgramScheduler;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlacementDebugDump {
    private static final String TAG = "PlacementDebug";

    private PlacementDebugDump() {}

    public static void log(String programId, String plantingStartDate,
                            String seedsConfirmedDate, String sowingDate,
                            String transplantDate, String stopDate,
                            int placementPhase, String variety, double area,
                            LocalDate appToday) {
        log(programId, plantingStartDate, seedsConfirmedDate, sowingDate,
                transplantDate, stopDate, placementPhase, null, variety, area, appToday);
    }

    public static void log(String programId, String plantingStartDate,
                            String seedsConfirmedDate, String sowingDate,
                            String transplantDate, String stopDate,
                            int placementPhase, Object placementRecord,
                            String variety, double area, LocalDate appToday) {
        if (!BuildConfig.DEBUG) return;
        try {
            LocalDate planting = LocalDate.parse(plantingStartDate);
            LocalDate seeds = parse(seedsConfirmedDate);
            LocalDate sowing = parse(sowingDate);
            LocalDate transplant = parse(transplantDate);
            LocalDate stop = parse(stopDate);
            int phase = com.android.tomatoapp.task.data.TaskSchedule.getPhaseNumber(
                    planting, transplant, stop, appToday);
            int dat = transplant == null ? -1 : (int) ChronoUnit.DAYS.between(transplant, appToday);
            TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                    planting, area, variety, seeds, sowing, transplant, stop,
                    appToday, TomatoWorkProgramScheduler.DEFAULT_HECTARES_PER_PLOWING_DAY);
            List<TaskModel> tasks = TomatoWorkProgramScheduler.getTasksForDate(input, appToday);
            Map<String, Integer> counts = new HashMap<>();
            for (TaskModel task : tasks) {
                String key = task.phase + "/" + task.type;
                counts.put(key, counts.containsKey(key) ? counts.get(key) + 1 : 1);
            }
            int completed = 0;
            for (TaskModel task : tasks) {
                int taskPhase = Integer.parseInt(task.phase.replaceAll("[^0-9]", ""));
                if (taskPhase < placementPhase
                        || (taskPhase == placementPhase && "MAIN".equals(task.type)
                        && task.startDate != null && task.startDate.compareTo(appToday.toString()) < 0)) {
                    completed++;
                }
            }
            int progress = tasks.isEmpty() ? 0 : completed * 100 / tasks.size();
            Log.d(TAG, "programId=" + programId
                    + " plantingStartDate=" + plantingStartDate
                    + " seedsConfirmedDate=" + seedsConfirmedDate
                    + " sowingDate=" + sowingDate
                    + " transplantDate=" + transplantDate
                    + " stopDate=" + stopDate
                    + " placementRecord=" + placementRecord
                    + " placementPhase=" + placementPhase
                    + " appToday=" + appToday
                    + " DAT=" + dat
                    + " phase=" + phase
                    + " tasksByPhaseType=" + counts
                    + " completedByOnboarding=" + completed
                    + " progress=" + progress + "%");
        } catch (RuntimeException error) {
            Log.w(TAG, "Could not dump placement state", error);
        }
    }

    private static LocalDate parse(String value) {
        return value == null || value.isEmpty() ? null : LocalDate.parse(value);
    }
}
