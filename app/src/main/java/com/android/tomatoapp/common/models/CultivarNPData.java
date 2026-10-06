package com.android.tomatoapp.common.models;

import java.util.HashMap;
import java.util.Map;

public class CultivarNPData {

    // Growth habit to NP mapping
    private static final Map<String, Integer> habitNP = new HashMap<String, Integer>() {{
        put("Determinate", 10000);
        put("Semi-determinate", 7000);
        put("Indeterminate", 5000);
    }};

    // Cultivar name → growth habit
    private static final Map<String, String> cultivarHabit = new HashMap<String, String>() {{
        put("Improved KS Apollo", "Semi-determinate");
        put("Diamante max", "Semi-determinate");
        put("Jewel f1", "Determinate");
        put("Marimar", "Determinate");
    }};

    /**
     * Returns the growth habit for a given cultivar name.
     */
    public static String getGrowthHabit(String cultivarName) {
        return cultivarHabit.getOrDefault(cultivarName, "Unknown");
    }

    /**
     * Returns the NP (number of plants per hectare) for a given cultivar.
     */
    public static int getNP(String cultivarName) {
        String habit = getGrowthHabit(cultivarName);
        return habitNP.getOrDefault(habit, 0);
    }

    /**
     * Returns the NP directly from a given growth habit.
     */
    public static int getNPFromHabit(String habit) {
        return habitNP.getOrDefault(habit, 0);
    }
}
