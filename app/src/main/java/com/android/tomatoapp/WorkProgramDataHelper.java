package com.android.tomatoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class to calculate and load phase and detection history data for work programs.
 */
public class WorkProgramDataHelper {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final Map<String, Integer> DAY_TASK_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, int[]> PHASE_TOTAL_CACHE = new ConcurrentHashMap<>();

    // Cultivar maturity days data (same as in Workprogram.java)
    private static final String[][] cultivarsData = {
            {"Victory F1", "Semi-determinate", "90", "110"},
            {"HOPE F1", "Semi-determinate", "90", "110"},
            {"Maganda F1", "Semi-determinate", "80", "100"},
            {"Malakas F1", "Semi-determinate", "95", "115"},
            {"Rocky 1 F1", "Semi-determinate", "90", "110"},
            {"Improved KS Apollo", "Semi-determinate", "85", "105"},
            {"Improved Pope", "Semi-determinate", "85", "105"},
            {"Super Pope", "Semi-determinate", "85", "105"},
            {"Maguilas", "Determinate", "85", "105"},
            {"Maunlad", "Determinate", "80", "100"},
            {"Mapalad", "Determinate", "80", "100"},
            {"Abiona F1", "Semi-determinate", "95", "115"},
            {"Akna F1", "Semi-determinate", "105", "125"},
            {"Amari F1", "Semi-determinate", "110", "130"},
            {"Anita F1", "Semi-determinate", "110", "130"},
            {"Colette F1", "Determinate", "105", "125"},
            {"Danica F1", "Semi-determinate", "105", "125"},
            {"Granger F1", "Semi-determinate", "105", "125"},
            {"Janet F1", "Semi-determinate", "120", "140"},
            {"Platinum F1", "Semi-determinate", "100", "120"},
            {"Reina F1", "Semi-determinate", "105", "125"},
            {"Renata F1", "Semi-determinate", "105", "125"},
            {"Rubellite F1", "Semi-determinate", "90", "110"},
            {"TOM-055 F1", "Semi-determinate", "60", "75"},
            {"TOM-262 OP", "Determinate", "60", "75"},
            {"Dalwangan Tm1", "Determinate", "90", "110"},
            {"Dalwangan Tm2", "Determinate", "90", "110"},
            {"NSIC 1999 Tm09", "Determinate", "100", "120"},
            {"Mara", "Determinate", "78", "95"},
            {"AniMax 1", "Determinate", "87", "105"},
            {"AniMax 2", "Semi-determinate", "87", "105"},
            {"Golden Globe", "Semi-determinate", "92", "112"},
            {"Maxxime", "Indeterminate", "105", "125"}
    };

    /**
     * Calculate phase information for a work program based on cultivar and start date.
     * Returns a JSON string with phase ranges.
     */
    public static String calculatePhasesJson(String cultivarName, String startDate) {
        if (cultivarName == null || startDate == null) return null;

        int maturityDays = getMaturityDays(cultivarName);
        if (maturityDays <= 0) return null;

        try {
            Date start = sdf.parse(startDate);
            if (start == null) return null;

            Calendar cal = Calendar.getInstance();
            cal.setTime(start);

            // Phase 1 fixed = 30 days
            int phase1Days = 30;
            int remaining = maturityDays - phase1Days;
            int eachPhase = remaining / 4;
            int extra = remaining % 4;

            JSONObject phasesJson = new JSONObject();
            phasesJson.put("totalDays", maturityDays);
            phasesJson.put("phase1Days", phase1Days);
            phasesJson.put("phase2Days", eachPhase + (extra > 0 ? 1 : 0));
            phasesJson.put("phase3Days", eachPhase + (extra > 1 ? 1 : 0));
            phasesJson.put("phase4Days", eachPhase + (extra > 2 ? 1 : 0));
            phasesJson.put("phase5Days", eachPhase + (extra > 3 ? 1 : 0));

            // Calculate phase date ranges
            cal.setTime(start);
            cal.add(Calendar.DAY_OF_YEAR, phase1Days - 1);
            phasesJson.put("phase1End", sdf.format(cal.getTime()));

            int phase2Days = eachPhase + (extra > 0 ? 1 : 0);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            phasesJson.put("phase2Start", sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, phase2Days - 1);
            phasesJson.put("phase2End", sdf.format(cal.getTime()));

            int phase3Days = eachPhase + (extra > 1 ? 1 : 0);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            phasesJson.put("phase3Start", sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, phase3Days - 1);
            phasesJson.put("phase3End", sdf.format(cal.getTime()));

            int phase4Days = eachPhase + (extra > 2 ? 1 : 0);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            phasesJson.put("phase4Start", sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, phase4Days - 1);
            phasesJson.put("phase4End", sdf.format(cal.getTime()));

            int phase5Days = eachPhase + (extra > 3 ? 1 : 0);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            phasesJson.put("phase5Start", sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, phase5Days - 1);
            phasesJson.put("phase5End", sdf.format(cal.getTime()));

            return phasesJson.toString();
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get maturity days for a cultivar.
     */
    public static int getMaturityDays(String cultivar) {
        for (String[] c : cultivarsData) {
            if (c[0].equals(cultivar)) {
                try {
                    return Integer.parseInt(c[3]); // use max maturity days
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    /**
     * Fetch completion statistics for a specific work program.
     */
    public static void fetchCompletionStats(@Nullable String userId,
                                            @Nullable String programId,
                                            @Nullable String cultivarName,
                                            @Nullable String startDate,
                                            @NonNull CompletionStatsCallback callback) {
        CompletionStats stats = new CompletionStats();

        int maturityDays = getMaturityDays(cultivarName);
        if (maturityDays <= 0) {
            maturityDays = 90;
        }
        final int finalMaturityDays = maturityDays;

        int[] phaseTotals = getPhaseTotals(cultivarName, finalMaturityDays);
        int total = 0;
        for (int i = 0; i < phaseTotals.length; i++) {
            stats.phaseTotals[i] = phaseTotals[i];
            total += phaseTotals[i];
        }
        stats.totalTasks = total;
        stats.completionRate = stats.totalTasks > 0 ? (stats.completedTasks * 100.0) / stats.totalTasks : 0;

        if (userId == null || programId == null || startDate == null) {
            callback.onResult(stats);
            return;
        }

        DatabaseReference tasksRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("routineLogs")
                .child(programId)
                .child("tasks");

        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    String dateKey = taskSnapshot.getKey();
                    String status = taskSnapshot.getValue(String.class);
                    if (dateKey == null || status == null) continue;

                    int dayNumber = calculateDayNumber(startDate, dateKey);
                    if (dayNumber <= 0) continue;

                    int tasksForDay = getTaskCountForDay(cultivarName, finalMaturityDays, dayNumber);
                    int phaseIndex = getPhaseIndex(finalMaturityDays, dayNumber);
                    if ("completed".equals(status)) {
                        stats.completedTasks += tasksForDay;
                        if (phaseIndex >= 1) {
                            stats.phaseCompleted[phaseIndex - 1] += tasksForDay;
                        }
                    } else if ("missed".equals(status)) {
                        stats.missedTasks += tasksForDay;
                        if (phaseIndex >= 1) {
                            stats.phaseMissed[phaseIndex - 1] += tasksForDay;
                        }
                    } else if ("skipped".equals(status)) {
                        stats.skippedTasks += tasksForDay;
                        if (phaseIndex >= 1) {
                            stats.phaseSkipped[phaseIndex - 1] += tasksForDay;
                        }
                    }
                }

                stats.completionRate = stats.totalTasks > 0
                        ? (stats.completedTasks * 100.0) / stats.totalTasks
                        : 0;
                callback.onResult(stats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(stats);
            }
        });
    }

    /**
     * Holder for completion statistics.
     */
    public static class CompletionStats {
        public int totalTasks;
        public int completedTasks;
        public int missedTasks;
        public int skippedTasks;
        public double completionRate;
        public final int[] phaseTotals = new int[5];
        public final int[] phaseCompleted = new int[5];
        public final int[] phaseMissed = new int[5];
        public final int[] phaseSkipped = new int[5];
    }

    public interface CompletionStatsCallback {
        void onResult(CompletionStats stats);
    }

    /**
     * Adjust projected income and expenses based on completion data.
     */
    public static AdjustedProjection adjustProjectionsByCompletionRate(double projectedIncome,
                                                                       double projectedExpenses,
                                                                       @Nullable CompletionStats stats) {
        if (stats == null || stats.totalTasks <= 0) {
            return new AdjustedProjection(projectedIncome, projectedExpenses);
        }

        double completionFactor = Math.max(0, Math.min(1, stats.completionRate / 100.0));
        double skippedRatio = stats.totalTasks > 0 ? (double) stats.skippedTasks / stats.totalTasks : 0;
        double harvestCompletion = stats.phaseTotals[4] > 0
                ? (double) stats.phaseCompleted[4] / stats.phaseTotals[4]
                : completionFactor;
        harvestCompletion = Math.max(0, Math.min(1, harvestCompletion));

        // Weight harvest completion higher since it impacts income the most
        double phaseWeight = 0.6 + (harvestCompletion * 0.4);

        double skipFactor = Math.max(0.6, 1 - (skippedRatio * 0.2));
        double adjustedIncome = projectedIncome * completionFactor * phaseWeight * skipFactor;
        adjustedIncome = Math.min(projectedIncome, adjustedIncome);

        double missedRatio = stats.totalTasks > 0 ? (double) stats.missedTasks / stats.totalTasks : 0;
        double expenseReductionFactor = 1 - (missedRatio * 0.3) - (skippedRatio * 0.15);
        expenseReductionFactor = Math.max(0.4, expenseReductionFactor);
        double adjustedExpenses = projectedExpenses * expenseReductionFactor;

        return new AdjustedProjection(adjustedIncome, adjustedExpenses);
    }

    public static class AdjustedProjection {
        public final double adjustedIncome;
        public final double adjustedExpenses;

        public AdjustedProjection(double adjustedIncome, double adjustedExpenses) {
            this.adjustedIncome = adjustedIncome;
            this.adjustedExpenses = adjustedExpenses;
        }
    }

    public static double[] getPhaseCompletionRates(@Nullable CompletionStats stats) {
        double[] rates = new double[5];
        if (stats == null) {
            return rates;
        }
        for (int i = 0; i < 5; i++) {
            if (stats.phaseTotals[i] > 0) {
                rates[i] = (double) stats.phaseCompleted[i] / stats.phaseTotals[i] * 100.0;
            } else {
                rates[i] = 0;
            }
        }
        return rates;
    }

    /**
     * Load phase information from routineLogs tasks and calculate completion status.
     * Returns a summary string like "Phase 1: 15/30, Phase 2: 5/20, ..."
     */
    public static String getPhasesSummaryFromTasks(String userId, String programId, String cultivarName, String startDate) {
        if (userId == null || programId == null || cultivarName == null || startDate == null) {
            return "N/A";
        }

        String phasesJson = calculatePhasesJson(cultivarName, startDate);
        if (phasesJson == null) return "N/A";

        try {
            JSONObject phases = new JSONObject(phasesJson);
            int totalDays = phases.optInt("totalDays", 0);
            if (totalDays == 0) return "N/A";

            // Count completed tasks from routineLogs
            DatabaseReference tasksRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("routineLogs")
                    .child(programId)
                    .child("tasks");

            // For now, return a simple summary. Can be enhanced to count actual completed tasks per phase
            return "5 phases (" + totalDays + " days)";
        } catch (JSONException e) {
            return "5 phases";
        }
    }

    /**
     * Load detection history count for a work program.
     * Since detection histories are stored globally in SharedPreferences,
     * we'll count all detections that occurred during the work program period.
     */
    public static String getDetectionsSummary(android.content.Context context, String startDate) {
        if (context == null || startDate == null) return "None";

        try {
            ArrayList<org.json.JSONObject> allHistory = DetectionHistoryManager.getHistory(context);
            if (allHistory == null || allHistory.isEmpty()) return "None";

            // Count detections that occurred after the work program start date
            Date programStart = sdf.parse(startDate);
            if (programStart == null) return "None";

            int count = 0;
            for (org.json.JSONObject entry : allHistory) {
                long timestamp = entry.optLong("timestamp", 0);
                if (timestamp > 0) {
                    Date detectionDate = new Date(timestamp);
                    if (detectionDate.after(programStart) || detectionDate.equals(programStart)) {
                        count++;
                    }
                }
            }

            return count > 0 ? String.valueOf(count) + " detections" : "None";
        } catch (ParseException e) {
            return "None";
        }
    }

    /**
     * Get a summary of activities done in each phase.
     * Returns a concise string describing what happens in each phase.
     */
    public static String getPhasesActivitySummary(org.json.JSONObject phases) {
        if (phases == null) return "N/A";
        
        // Phase descriptions based on TaskSchedule.java
        // Phase 1: Nursery / Land Prep (land preparation, seedling management, watering)
        // Phase 2: Transplant & Establishment (transplanting, weeding, mulching, staking)
        // Phase 3: Vegetative Growth (fertilization, weeding, pest monitoring, pruning)
        // Phase 4: Flowering & Fruit Setting (fertilization, pest monitoring, flower management)
        // Phase 5: Harvest Phase (harvesting, watering, disease management)
        return "1:Land Prep & Nursery, 2:Transplant & Establishment, 3:Vegetative Growth, 4:Flowering & Fruit Set, 5:Harvest";
    }

    private static int getTaskCountForDay(@Nullable String cultivarName,
                                          int maturityDays,
                                          int dayNumber) {
        if (dayNumber <= 0 || dayNumber > maturityDays) {
            return 0;
        }
        String growthHabit = getGrowthHabit(cultivarName);
        String cacheKey = (cultivarName == null ? "unknown" : cultivarName) + "#" + dayNumber + "#" + maturityDays;
        Integer cached = DAY_TASK_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<TaskModel> dailyTasks = TaskSchedule.getTasksForDay(growthHabit, maturityDays, dayNumber);
        int size = dailyTasks != null ? dailyTasks.size() : 0;
        DAY_TASK_CACHE.put(cacheKey, size);
        return size;
    }

    private static int calculateDayNumber(String startDate, String currentDate) {
        if (startDate == null || currentDate == null) {
            return -1;
        }
        try {
            Date start = sdf.parse(startDate);
            Date current = sdf.parse(currentDate);
            if (start == null || current == null) return -1;
            long diff = current.getTime() - start.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        } catch (ParseException e) {
            return -1;
        }
    }

    private static String getGrowthHabit(@Nullable String cultivarName) {
        if (cultivarName == null) {
            return "Semi-determinate";
        }
        for (String[] c : cultivarsData) {
            if (c[0].equals(cultivarName) && c.length > 1) {
                return c[1];
            }
        }
        return "Semi-determinate";
    }

    private static int[] getPhaseTotals(@Nullable String cultivarName, int maturityDays) {
        if (cultivarName == null) {
            return new int[5];
        }
        String cacheKey = cultivarName + "#" + maturityDays;
        int[] cached = PHASE_TOTAL_CACHE.get(cacheKey);
        if (cached != null) {
            return cached.clone();
        }

        int[] totals = new int[5];
        for (int day = 1; day <= maturityDays; day++) {
            int phaseIndex = getPhaseIndex(maturityDays, day);
            if (phaseIndex < 1) continue;
            totals[phaseIndex - 1] += getTaskCountForDay(cultivarName, maturityDays, day);
        }
        int[] stored = totals.clone();
        PHASE_TOTAL_CACHE.put(cacheKey, stored);
        return stored.clone();
    }

    private static int getPhaseIndex(int maturityDays, int dayNumber) {
        if (dayNumber <= 0) return -1;
        int phaseDuration = maturityDays / 5;
        if (phaseDuration <= 0) {
            phaseDuration = 1;
        }
        return Math.min(((dayNumber - 1) / phaseDuration) + 1, 5);
    }
}

