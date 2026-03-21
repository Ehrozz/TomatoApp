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
        put("Victory F1", "Semi-determinate");
        put("HOPE F1", "Semi-determinate");
        put("Maganda F1", "Semi-determinate");
        put("Malakas F1", "Semi-determinate");
        put("Rocky 1 F1", "Semi-determinate");
        put("Improved KS Apollo", "Semi-determinate");
        put("Improved Pope", "Semi-determinate");
        put("Super Pope", "Semi-determinate");
        put("Maguilas", "Determinate");
        put("Maunlad", "Determinate");
        put("Mapalad", "Determinate");
        put("Abiona F1", "Semi-determinate");
        put("Akna F1", "Semi-determinate");
        put("Amari F1", "Semi-determinate");
        put("Anita F1", "Semi-determinate");
        put("Colette F1", "Determinate");
        put("Danica F1", "Semi-determinate");
        put("Granger F1", "Semi-determinate");
        put("Janet F1", "Semi-determinate");
        put("Platinum F1", "Semi-determinate");
        put("Reina F1", "Semi-determinate");
        put("Renata F1", "Semi-determinate");
        put("Rubellite F1", "Semi-determinate");
        put("TOM-055 F1", "Semi-determinate");
        put("TOM-262 OP", "Determinate");
        put("Dalwangan Tm1", "Determinate");
        put("Dalwangan Tm2", "Determinate");
        put("NSIC 1999 Tm09", "Determinate");
        put("Mara", "Determinate");
        put("AniMax 1", "Determinate");
        put("AniMax 2", "Semi-determinate");
        put("Golden Globe", "Semi-determinate");
        put("Maxxime", "Indeterminate");
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
