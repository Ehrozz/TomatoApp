package com.android.tomatoapp.task.data;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskSchedule: Generates daily tasks based on cultivar type,
 * maturity duration, and current day number.
 * Enhanced for off-season planting of tomatoes.
 *
 * - Divides maturity days into 5 phases
 * - Expands broad phase tasks into daily or recurring activities
 * - Adjusts tasks depending on growth habit (Determinate, Semi, Indeterminate)
 * - Optimized for off-season conditions (cooler temperatures, higher humidity, pest management)
 */
public class TaskSchedule {

    public static List<TaskModel> getTasksForDay(String growthHabit, int maturityDays, int dayNumber) {
        List<TaskModel> tasks = new ArrayList<>();

        int phaseDuration = Math.max(1, maturityDays / 5);
        int phase = getPhaseNumber(maturityDays, dayNumber);

        switch (phase) {
            case 1: // Phase 1: Land and Seedling Preparation
                if (dayNumber == 1) {
                    tasks.add(new TaskModel(
                        "Plow the land, prepare 180-200 g seeds per ha, and prepare growing medium",
                        "Land Preparation",
                        "land",
                        dayNumber,
                        "Phase 1"
                    ));
                } else if (dayNumber <= 7) {
                    tasks.add(new TaskModel(
                        "Water seedlings daily or as needed to maintain moisture",
                        "Watering",
                        "water",
                        dayNumber,
                        "Phase 1"
                    ));
                } else if (dayNumber <= 14) {
                    tasks.add(new TaskModel(
                        "Plow the land and water seedlings daily",
                        "Land Preparation",
                        "land",
                        dayNumber,
                        "Phase 1"
                    ));
                } else if (dayNumber <= 21) {
                    tasks.add(new TaskModel(
                        "Plowing & harrowing on last plowing date; seedling hardening 5-7 days before transplanting",
                        "Land Preparation",
                        "land",
                        dayNumber,
                        "Phase 1"
                    ));
                } else {
                    tasks.add(new TaskModel(
                        "Construct furrows (75-100 cm det/semi-det, 150 cm indet; beds 1 m wide x 20 cm high)",
                        "Land Preparation",
                        "land",
                        dayNumber,
                        "Phase 1"
                    ));
                }
                break;

            case 2: // Phase 2: Transplant & Establishment
                if (dayNumber == phaseDuration + 1) {
                    tasks.add(new TaskModel(
                        "Transplanting (3-5 true leaves), mulching (3-5 days), and basal fertilizer (14-14-14 10 g/plant)",
                        "Transplanting",
                        "land",
                        dayNumber,
                        "Phase 2"
                    ));
                }
                if (dayNumber % 2 == 0) {
                    tasks.add(new TaskModel(
                        "Watering: every other day, adjust based on weather",
                        "Watering",
                        "water",
                        dayNumber,
                        "Phase 2"
                    ));
                }
                if (dayNumber % 7 == 0) {
                    tasks.add(new TaskModel(
                        "Weeding (1x/week), pest/disease monitoring (1x/week), pruning if overcrowded",
                        "Maintenance",
                        "maintenance",
                        dayNumber,
                        "Phase 2"
                    ));
                }
                if (dayNumber == phaseDuration + 12) {
                    tasks.add(new TaskModel(
                        "First side-dressing (10-15 DAT): Urea 10 g + MOP 10 g per plant",
                        "Fertilization",
                        "fertilizer",
                        dayNumber,
                        "Phase 2"
                    ));
                }
                if ((growthHabit.contains("Semi") || growthHabit.contains("Indeterminate"))
                        && dayNumber >= (phaseDuration + 28) && dayNumber <= (phaseDuration + 30)) {
                    tasks.add(new TaskModel(
                        "Trellising / staking: install supports 1 month after transplanting and tie gently",
                        "Support Installation",
                        "maintenance",
                        dayNumber,
                        "Phase 2"
                    ));
                }
                break;

            case 3: // Phase 3: Vegetative Growth
                if (dayNumber % 14 == 0) {
                    tasks.add(new TaskModel(
                        "Side-dressing (Urea 10 g + MOP 10 g per plant; optional organic 0.5-1 kg/plant)",
                        "Fertilization",
                        "fertilizer",
                        dayNumber,
                        "Phase 3"
                    ));
                }
                if (dayNumber % 7 == 0) {
                    tasks.add(new TaskModel(
                        "Weeding / hilling-up (25-30 DAT), pest monitoring (1x/week), pruning for airflow",
                        "Maintenance",
                        "maintenance",
                        dayNumber,
                        "Phase 3"
                    ));
                }
                if (dayNumber % 2 == 0) {
                    tasks.add(new TaskModel(
                        "Watering: every other day, more if hot/dry",
                        "Watering",
                        "water",
                        dayNumber,
                        "Phase 3"
                    ));
                }
                break;

            case 4: // Phase 4: Flowering & Fruit Setting
                if (dayNumber % 10 == 0) {
                    tasks.add(new TaskModel(
                        "Fourth side-dressing at peak flowering/fruit set (Urea 10 g + MOP 10 g) and trellis adjustment",
                        "Fertilization",
                        "fertilizer",
                        dayNumber,
                        "Phase 4"
                    ));
                }
                if (dayNumber % 2 == 0) {
                    tasks.add(new TaskModel(
                        "Watering: every other day, more frequent if hot/dry",
                        "Watering",
                        "water",
                        dayNumber,
                        "Phase 4"
                    ));
                }
                if (dayNumber % 3 == 0) {
                    tasks.add(new TaskModel(
                        "Pest/disease monitoring: every 3 days",
                        "Pest Management",
                        "pest",
                        dayNumber,
                        "Phase 4"
                    ));
                }
                if (dayNumber % 7 == 0) {
                    tasks.add(new TaskModel(
                        "Mulching/soil care (1x/week) and pruning (on 5th or 6th day only)",
                        "Maintenance",
                        "maintenance",
                        dayNumber,
                        "Phase 4"
                    ));
                }
                break;

            case 5: // Phase 5: Peak Fruit / Harvest
                if (dayNumber % 7 == 0) {
                    tasks.add(new TaskModel(
                        "Harvesting weekly (Mature green, Breaker/pink, Red ripe). Pruning/cleanup senescent leaves.",
                        "Harvesting",
                        "harvest",
                        dayNumber,
                        "Phase 5"
                    ));
                }
                if (dayNumber % 2 == 0) {
                    tasks.add(new TaskModel(
                        "Watering: every 2 days (avoid excess to prevent fruit splitting)",
                        "Watering",
                        "water",
                        dayNumber,
                        "Phase 5"
                    ));
                }
                if (dayNumber % 3 == 0) {
                    tasks.add(new TaskModel(
                        "Pest/disease monitoring: every 3 days (treat as needed)",
                        "Pest Management",
                        "pest",
                        dayNumber,
                        "Phase 5"
                    ));
                }
                break;
        }

        return tasks;
    }

    // Legacy method for backward compatibility
    public static List<String> getTasksForDayLegacy(String growthHabit, int maturityDays, int dayNumber) {
        List<TaskModel> taskModels = getTasksForDay(growthHabit, maturityDays, dayNumber);
        List<String> tasks = new ArrayList<>();
        for (TaskModel task : taskModels) {
            tasks.add(task.taskName);
        }
        return tasks;
    }

    public static int getPhaseNumber(int maturityDays, int dayNumber) {
        if (dayNumber <= 0) return 1;
        int phaseDuration = Math.max(1, maturityDays / 5);
        return Math.min(((dayNumber - 1) / phaseDuration) + 1, 5);
    }

    public static int getPhaseNumber(LocalDate plantingStart, LocalDate transplantDate,
                                     LocalDate stopDate, LocalDate date) {
        if (plantingStart == null || date == null || date.isBefore(plantingStart)) return 0;
        if (stopDate != null && date.isAfter(stopDate)) return 0;
        if (transplantDate == null || date.isBefore(transplantDate)) return 1;
        long dat = ChronoUnit.DAYS.between(transplantDate, date);
        if (dat <= 27) return 2;
        if (dat <= 55) return 3;
        if (dat <= 83) return 4;
        return 5;
    }
}
