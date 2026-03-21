package com.android.tomatoapp.task.data;

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
            case 1: // Nursery / Land Prep (Off-season: Focus on protection and moisture)
                if (dayNumber == 1) {
                    tasks.add(new TaskModel(
                        "Plow the land (first pass), prepare nursery seedbeds with raised beds",
                        "Land Preparation",
                        "land",
                        dayNumber,
                        "Phase 1"
                    ));
                } else if (dayNumber <= 7) {
                    tasks.add(new TaskModel(
                        "Water seedlings daily (morning only) to maintain soil moisture",
                        "Watering",
                        "water",
                        dayNumber,
                        "Phase 1"
                    ));
                } else if (dayNumber == 8) {
                    tasks.add(new TaskModel(
                        "Second plowing and harrowing. Apply organic matter",
                        "Land Preparation",
                        "land",
                        dayNumber,
                        "Phase 1"
                    ));
                } else if (dayNumber == 15) {
                    tasks.add(new TaskModel(
                        "Final plowing + construct furrows with proper drainage",
                        "Land Preparation",
                        "land",
                        dayNumber,
                        "Phase 1"
                    ));
                } else if (dayNumber >= phaseDuration - 3) {
                    tasks.add(new TaskModel(
                        "Harden seedlings (reduce water, increase sun exposure gradually)",
                        "Seedling Management",
                        "maintenance",
                        dayNumber,
                        "Phase 1"
                    ));
                } else {
                    tasks.add(new TaskModel(
                        "Maintain seedlings, monitor for damping-off and pests",
                        "Pest Management",
                        "pest",
                        dayNumber,
                        "Phase 1"
                    ));
                }
                break;

            case 2: // Transplant & Establishment (Off-season: Protect from cold, ensure proper spacing)
                if (dayNumber == phaseDuration + 1) {
                    tasks.add(new TaskModel(
                        "Transplant seedlings (late afternoon) and apply basal fertilizer",
                        "Transplanting",
                        "land",
                        dayNumber,
                        "Phase 2"
                    ));
                }
                if (dayNumber % 7 == 0) {
                    tasks.add(new TaskModel(
                        "Weekly weeding and mulching to retain soil warmth",
                        "Weeding",
                        "maintenance",
                        dayNumber,
                        "Phase 2"
                    ));
                }
                if (dayNumber % 2 == 0) {
                    tasks.add(new TaskModel(
                        "Water plants (morning, avoid evening to prevent disease)",
                        "Watering",
                        "water",
                        dayNumber,
                        "Phase 2"
                    ));
                }
                if (growthHabit.contains("Semi") || growthHabit.contains("Indeterminate")) {
                    if (dayNumber >= (phaseDuration + 10)) {
                        tasks.add(new TaskModel(
                            "Install trellis/staking supports for vertical growth",
                            "Support Installation",
                            "maintenance",
                            dayNumber,
                            "Phase 2"
                        ));
                    }
                }
                // Off-season specific: Cold protection
                if (dayNumber >= (phaseDuration + 3) && dayNumber <= (phaseDuration + 10)) {
                    tasks.add(new TaskModel(
                        "Monitor temperature, provide protection if needed",
                        "Climate Management",
                        "maintenance",
                        dayNumber,
                        "Phase 2"
                    ));
                }
                break;

            case 3: // Vegetative Growth (Off-season: Focus on disease prevention, proper nutrition)
                if (dayNumber % 14 == 0) {
                    tasks.add(new TaskModel(
                        "Apply side-dressing fertilizer (Urea + Potash) with proper spacing",
                        "Fertilization",
                        "fertilizer",
                        dayNumber,
                        "Phase 3"
                    ));
                }
                if (dayNumber % 7 == 0) {
                    tasks.add(new TaskModel(
                        "Weeding or hilling-up soil at plant base for support",
                        "Weeding",
                        "maintenance",
                        dayNumber,
                        "Phase 3"
                    ));
                }
                // Off-season: More frequent pest monitoring
                if (dayNumber % 3 == 0) {
                    tasks.add(new TaskModel(
                        "Monitor pests and diseases (off-season: higher humidity risk)",
                        "Pest Management",
                        "pest",
                        dayNumber,
                        "Phase 3"
                    ));
                }
                if (growthHabit.contains("Indeterminate")) {
                    tasks.add(new TaskModel(
                        "Prune side shoots and manage trellis for better air circulation",
                        "Pruning",
                        "maintenance",
                        dayNumber,
                        "Phase 3"
                    ));
                }
                break;

            case 4: // Flowering & Fruit Setting (Off-season: Pollination support, disease control)
                if (dayNumber % 10 == 0) {
                    tasks.add(new TaskModel(
                        "Fertilization during flowering/fruit set (balanced NPK)",
                        "Fertilization",
                        "fertilizer",
                        dayNumber,
                        "Phase 4"
                    ));
                }
                // Off-season: More frequent pest monitoring due to higher disease pressure
                if (dayNumber % 2 == 0) {
                    tasks.add(new TaskModel(
                        "Pest and disease monitoring (off-season: critical period)",
                        "Pest Management",
                        "pest",
                        dayNumber,
                        "Phase 4"
                    ));
                }
                if (growthHabit.contains("Indeterminate")) {
                    tasks.add(new TaskModel(
                        "Adjust trellis and prune overcrowded leaves for better light",
                        "Pruning",
                        "maintenance",
                        dayNumber,
                        "Phase 4"
                    ));
                }
                // Off-season: Support pollination
                if (dayNumber % 5 == 0) {
                    tasks.add(new TaskModel(
                        "Check flower development, ensure proper pollination conditions",
                        "Flower Management",
                        "maintenance",
                        dayNumber,
                        "Phase 4"
                    ));
                }
                break;

            case 5: // Harvest Phase (Off-season: Extended harvest, quality management)
                if (dayNumber % 7 == 0) {
                    tasks.add(new TaskModel(
                        "Harvest mature fruits (harvest in morning for better quality)",
                        "Harvesting",
                        "harvest",
                        dayNumber,
                        "Phase 5"
                    ));
                }
                tasks.add(new TaskModel(
                    "Continue watering 2–3x per week (morning only)",
                    "Watering",
                    "water",
                    dayNumber,
                    "Phase 5"
                ));
                tasks.add(new TaskModel(
                    "Remove diseased or senescent leaves to prevent spread",
                    "Disease Management",
                    "pest",
                    dayNumber,
                    "Phase 5"
                ));
                // Off-season: More frequent monitoring
                if (dayNumber % 3 == 0) {
                    tasks.add(new TaskModel(
                        "Pest monitoring (off-season: continued vigilance needed)",
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
}
