package com.android.tomatoapp;

import com.android.tomatoapp.task.data.ScheduledTask;
import com.android.tomatoapp.task.data.TomatoWorkProgramScheduler;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TomatoWorkProgramSchedulerTest {
    private static final LocalDate START = LocalDate.of(2026, 1, 1);

    @Test
    public void largeFarmPlowingSpansMultipleDaysAndHarrowingUsesLastDay() {
        TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                START, 1.0, "Determinate", START, null, null, null, 0.25);
        List<ScheduledTask> tasks = TomatoWorkProgramScheduler.generate(input, START.plusDays(30));

        ScheduledTask plow = find(tasks, "plow");
        ScheduledTask harrow = find(tasks, "harrow");
        assertEquals(4, plow.endDate.getDayOfYear() - plow.startDate.getDayOfYear() + 1);
        assertEquals(plow.endDate, harrow.startDate);
        assertTrue(plow.endDate.isAfter(plow.startDate));
    }

    @Test
    public void wateringDoesNotExistBeforeSowing() {
        TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                START, 1.0, "Determinate", START.plusDays(14), START.plusDays(14),
                null, null);
        List<ScheduledTask> tasks = TomatoWorkProgramScheduler.generate(input, START.plusDays(13));
        for (ScheduledTask task : tasks) {
            assertFalse(task.title.startsWith("Water seedlings"));
        }
    }

    @Test
    public void delayedSowingKeepsPhaseOneUntilRecordedTransplant() {
        LocalDate sowing = START.plusDays(14);
        TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                START, 1.0, "Determinate", sowing, sowing, null, null);
        List<ScheduledTask> tasks = TomatoWorkProgramScheduler.generate(input, sowing.plusDays(20));

        ScheduledTask hardening = find(tasks, "harden");
        assertEquals(sowing.plusDays(18), hardening.startDate);
        for (ScheduledTask task : tasks) {
            assertTrue(task.phase == 1);
        }
        assertEquals(sowing, find(tasks, "sow").startDate);
    }

    @Test
    public void determinateVarietyHasNoTrellising() {
        LocalDate transplant = START.plusDays(30);
        TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                START, 1.0, "Determinate", START, START, transplant, null);
        for (ScheduledTask task : TomatoWorkProgramScheduler.generate(input, transplant.plusDays(40))) {
            assertFalse(task.title.toLowerCase().contains("trellis"));
        }
    }

    @Test
    public void noTransplantDateProducesOnlyPhaseOneTasks() {
            LocalDate sowing = START.plusDays(2);
            TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                    START, 1.0, "Determinate", START, sowing, null, null);
            for (ScheduledTask task : TomatoWorkProgramScheduler.generate(input, START.plusDays(60))) {
                assertEquals(1, task.phase);
            }
    }

    @Test
    public void trellisAndThirdSideDressingUseCorrectDatWindows() {
            LocalDate transplant = START;
            TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                    START, 1.0, "sEmI-dEtErMiNaTe", START, START, transplant, null);
            List<ScheduledTask> tasks = TomatoWorkProgramScheduler.generate(input, transplant.plusDays(70));

            ScheduledTask install = find(tasks, "trellis-install");
            ScheduledTask tie = find(tasks, "trellis-tie");
            ScheduledTask third = find(tasks, "side-dress-3");
            assertEquals(transplant.plusDays(30), install.startDate);
            assertEquals(transplant.plusDays(36), install.endDate);
            assertEquals(transplant.plusDays(37), tie.startDate);
            assertEquals(transplant.plusDays(35), third.startDate);
            assertEquals(transplant.plusDays(41), third.endDate);
    }

    @Test
    public void stopDatePreventsTasksAfterStopDate() {
            LocalDate transplant = START;
            LocalDate stop = transplant.plusDays(90);
            TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                    START, 1.0, "Determinate", START, START, transplant, stop);
            assertTrue(TomatoWorkProgramScheduler.getTasksForDate(input, stop.plusDays(1)).isEmpty());
    }

    @Test
    public void phaseFiveMaintenanceContinuesUntilStopDate() {
        LocalDate transplant = START.plusDays(30);
        LocalDate stop = transplant.plusDays(100);
        TomatoWorkProgramScheduler.Input input = new TomatoWorkProgramScheduler.Input(
                START, 1.0, "Indeterminate", START, START, transplant, stop);
        List<ScheduledTask> tasks = TomatoWorkProgramScheduler.generate(input);
        ScheduledTask lastWater = null;
        for (ScheduledTask task : tasks) {
            if (task.id.startsWith("water-5-")) lastWater = task;
        }
        assertTrue(lastWater != null);
        assertTrue(!lastWater.startDate.isAfter(stop));
        assertEquals(2, lastWater.frequencyDays);
    }

    @Test
    public void missedDayDelayCannotMoveScheduledDates() {
        TomatoWorkProgramScheduler.Input delayed = new TomatoWorkProgramScheduler.Input(
                START, 1.0, "Determinate", START, START, START.plusDays(10), null,
                START.plusDays(20), 0.25, 5);
        assertEquals(START, find(TomatoWorkProgramScheduler.generate(delayed, START.plusDays(20)),
                "sow").startDate);
        assertEquals(2, com.android.tomatoapp.task.data.TaskSchedule.getPhaseNumber(
                START, START.plusDays(10), null, START.plusDays(10)));
    }

    private static ScheduledTask find(List<ScheduledTask> tasks, String prefix) {
        for (ScheduledTask task : tasks) {
            if (task.id.startsWith(prefix + "-")) return task;
        }
        throw new AssertionError("Missing task: " + prefix);
    }
}
