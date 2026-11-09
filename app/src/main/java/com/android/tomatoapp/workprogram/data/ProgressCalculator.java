package com.android.tomatoapp.workprogram.data;

import com.android.tomatoapp.task.data.ScheduledTask;
import com.android.tomatoapp.task.data.TaskTemplate;
import com.android.tomatoapp.task.data.TaskSchedule;
import com.android.tomatoapp.task.data.TomatoWorkProgramScheduler;

import android.util.Log;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProgressCalculator {
    public static final class Result {
        public final int overall;
        public final int[] stages;
        public final int completed;
        public final int total;

        private Result(int overall, int[] stages, int completed, int total) {
            this.overall = overall;
            this.stages = stages;
            this.completed = completed;
            this.total = total;
        }
    }

    private ProgressCalculator() {}

    public static Result calculate(LocalDate planting, LocalDate seeds, LocalDate sowing,
                                   LocalDate transplant, LocalDate stop, LocalDate placement,
                                   int startPhase, String variety, double area,
                                   LocalDate today, Set<LocalDate> completedDays) {
        if (planting == null || today == null) return new Result(0, new int[5], 0, 0);
        LocalDate templateTransplant = transplant == null ? planting.plusDays(30) : transplant;
        LocalDate through = stop == null ? planting.plusDays(365) : stop;
        TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                planting, Math.max(0.01, area), variety, seeds, sowing,
                templateTransplant, stop, today,
                TomatoWorkProgramScheduler.DEFAULT_HECTARES_PER_PLOWING_DAY);
        List<ScheduledTask> tasks = TomatoWorkProgramScheduler.generate(input, through);
                Set<LocalDate> completed = completedDays == null
                        ? new HashSet<>() : completedDays;
                List<Set<LocalDate>> phaseDates = new java.util.ArrayList<>();
                for (int i = 0; i < 5; i++) phaseDates.add(new HashSet<>());
                for (LocalDate date = planting; !date.isAfter(through); date = date.plusDays(1)) {
                    boolean working = false;
                    for (ScheduledTask task : tasks) {
                        if ((task.type == TaskTemplate.Type.MAIN
                                || task.type == TaskTemplate.Type.MAINTENANCE)
                                && task.occursOn(date)) {
                            working = true;
                            break;
                        }
                    }
                    if (!working) continue;
                    int phase = TaskSchedule.getPhaseNumber(planting, templateTransplant, stop, date);
                    if (phase >= 1 && phase <= 5) phaseDates.get(phase - 1).add(date);
                }
                if (stop == null && phaseDates.get(4).size() > 28) {
                    Set<LocalDate> limited = new HashSet<>();
                    int count = 0;
                    for (LocalDate date : new java.util.TreeSet<>(phaseDates.get(4))) {
                        if (count++ == 28) break;
                        limited.add(date);
                    }
                    phaseDates.set(4, limited);
                }
                int completedTotal = 0;
                int total = 0;
                double[] percentages = new double[5];
                int[] stages = new int[5];
                for (int i = 0; i < phaseDates.size(); i++) {
                    int totalDays = phaseDates.get(i).size();
                    int doneDays = 0;
                    int phase = i + 1;
                    if (phase < startPhase
                            || (transplant != null && phase == 5 && stop != null && !stop.isAfter(today))) {
                        doneDays = totalDays;
                    } else if (transplant != null) {
                        for (LocalDate date : phaseDates.get(i)) {
                            if (completed.contains(date)) doneDays++;
                        }
                    }
                    total += totalDays;
                    completedTotal += doneDays;
                    percentages[i] = totalDays == 0 ? 0 : doneDays * 100.0 / totalDays;
                    stages[i] = (int) Math.round(percentages[i]);
                }
                double average = 0;
                for (double percentage : percentages) average += percentage;
                int overall = (int) Math.round(average / percentages.length);
                Log.d("Progress", "pcts=" + Arrays.toString(percentages) + " completedDays="
                        + completed.size() + " startPhase=" + startPhase + " transplant=" + transplant);
                return new Result(overall, stages, completedTotal, total);
    }

}
