package com.android.tomatoapp;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskSchedule: Generates daily tasks based on cultivar type,
 * maturity duration, and current day number.
 *
 * - Divides maturity days into 5 phases
 * - Expands broad phase tasks into daily or recurring activities
 * - Adjusts tasks depending on growth habit (Determinate, Semi, Indeterminate)
 */
public class TaskSchedule {

    public static List<String> getTasksForDay(String growthHabit, int maturityDays, int dayNumber) {
        List<String> tasks = new ArrayList<>();

        int phaseDuration = maturityDays / 5;
        int phase = Math.min((dayNumber - 1) / phaseDuration + 1, 5);

        switch (phase) {
            case 1: // Nursery / Land Prep
                if (dayNumber == 1) {
                    tasks.add("Plow the land (first pass), prepare nursery seedbeds.");
                } else if (dayNumber <= 7) {
                    tasks.add("Water seedlings daily to maintain soil moisture.");
                } else if (dayNumber == 8) {
                    tasks.add("Second plowing and harrowing.");
                } else if (dayNumber == 15) {
                    tasks.add("Final plowing + construct furrows.");
                } else if (dayNumber >= phaseDuration - 3) {
                    tasks.add("Harden seedlings (reduce water, increase sun exposure).");
                } else {
                    tasks.add("Maintain seedlings and monitor for pests.");
                }
                break;

            case 2: // Transplant & Establishment
                if (dayNumber == phaseDuration + 1) {
                    tasks.add("Transplant seedlings and apply basal fertilizer.");
                }
                if (dayNumber % 7 == 0) {
                    tasks.add("Weekly weeding.");
                }
                if (dayNumber % 2 == 0) {
                    tasks.add("Water plants (3–4x per week).");
                }
                if (growthHabit.contains("Semi") || growthHabit.contains("Indeterminate")) {
                    if (dayNumber >= (phaseDuration + 10)) {
                        tasks.add("Install trellis/staking supports.");
                    }
                }
                break;

            case 3: // Vegetative Growth
                if (dayNumber % 14 == 0) {
                    tasks.add("Apply side-dressing fertilizer (Urea + Potash).");
                }
                if (dayNumber % 7 == 0) {
                    tasks.add("Weeding or hilling-up soil at plant base.");
                }
                tasks.add("Monitor pests and diseases this week.");
                if (growthHabit.contains("Indeterminate")) {
                    tasks.add("Prune side shoots and manage trellis.");
                }
                break;

            case 4: // Flowering & Fruit Setting
                if (dayNumber % 10 == 0) {
                    tasks.add("Fertilization during flowering/fruit set.");
                }
                tasks.add("Pest monitoring (twice per week).");
                if (growthHabit.contains("Indeterminate")) {
                    tasks.add("Adjust trellis and prune overcrowded leaves.");
                }
                break;

            case 5: // Harvest Phase
                if (dayNumber % 7 == 0) {
                    tasks.add("Harvest mature fruits.");
                }
                tasks.add("Continue watering 2–3x per week.");
                tasks.add("Remove diseased or senescent leaves.");
                tasks.add("Pest monitoring (twice per week).");
                break;
        }

        return tasks;
    }
}
