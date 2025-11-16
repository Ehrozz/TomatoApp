package com.android.tomatoapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure analytics calculations over WorkProgramEntity data.
 */
public class AnalyticsManager {

    public static class CultivarSummary {
        public final String cultivarName;
        public double totalArea;
        public double totalIncome;
        public double totalExpenses;
        public double totalAdjustedIncome;
        public double totalAdjustedExpenses;
        public double totalCompletionRate;
        public int programCount;

        public CultivarSummary(String cultivarName) {
            this.cultivarName = cultivarName;
        }

        public double getProfit() {
            return totalIncome - totalExpenses;
        }

        public double getIncomePerArea() {
            return totalArea > 0 ? totalIncome / totalArea : 0;
        }

        public double getProfitPerArea() {
            return totalArea > 0 ? getProfit() / totalArea : 0;
        }

        public double getAdjustedProfit() {
            return totalAdjustedIncome - totalAdjustedExpenses;
        }

        public double getAdjustedProfitPerArea() {
            return totalArea > 0 ? getAdjustedProfit() / totalArea : 0;
        }

        public double getAverageCompletionRate() {
            return programCount > 0 ? totalCompletionRate / programCount : 0;
        }
    }

    /**
     * Group work programs by cultivar and aggregate metrics.
     */
    public List<CultivarSummary> summarizeByCultivar(List<WorkProgramEntity> items) {
        Map<String, CultivarSummary> map = new HashMap<>();
        if (items == null) return new ArrayList<>();

        for (WorkProgramEntity e : items) {
            if (e == null || e.cultivarName == null) continue;
            CultivarSummary s = map.get(e.cultivarName);
            if (s == null) {
                s = new CultivarSummary(e.cultivarName);
                map.put(e.cultivarName, s);
            }
            s.totalArea += e.areaSize;
            s.totalIncome += e.projectedIncome;
            s.totalExpenses += e.projectedExpenses;
            s.totalAdjustedIncome += e.adjustedIncome;
            s.totalAdjustedExpenses += e.adjustedExpenses;
            if (e.completionRate > 0) {
                s.totalCompletionRate += e.completionRate;
            }
            s.programCount++;
        }

        return new ArrayList<>(map.values());
    }
}


