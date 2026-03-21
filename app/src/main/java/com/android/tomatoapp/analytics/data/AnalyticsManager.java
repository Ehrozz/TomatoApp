package com.android.tomatoapp.analytics.data;

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
        public double totalYield; // Total kg harvested
        public double totalActualYield; // Total kg/hectare
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
        
        public double getAverageYield() {
            return programCount > 0 ? totalActualYield / programCount : 0;
        }
        
        public double getTotalYield() {
            return totalYield;
        }
    }
    
    /**
     * Summary by season for research comparison.
     */
    public static class SeasonSummary {
        public final String season; // "on-season" or "off-season"
        public int programCount;
        public double totalArea;
        public double totalIncome;
        public double totalExpenses;
        public double totalAdjustedIncome;
        public double totalAdjustedExpenses;
        public double totalCompletionRate;
        public double totalYield;
        public double totalActualYield;
        
        public SeasonSummary(String season) {
            this.season = season;
        }
        
        public double getProfit() {
            return totalIncome - totalExpenses;
        }
        
        public double getProfitPerArea() {
            return totalArea > 0 ? getProfit() / totalArea : 0;
        }
        
        public double getAverageCompletionRate() {
            return programCount > 0 ? totalCompletionRate / programCount : 0;
        }
        
        public double getAverageYield() {
            return programCount > 0 ? totalActualYield / programCount : 0;
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
            s.totalYield += e.totalYield;
            s.totalActualYield += e.actualYield;
            s.programCount++;
        }

        return new ArrayList<>(map.values());
    }
    
    /**
     * Group work programs by season and aggregate metrics for comparison.
     */
    public List<SeasonSummary> summarizeBySeason(List<WorkProgramEntity> items) {
        Map<String, SeasonSummary> map = new HashMap<>();
        if (items == null) return new ArrayList<>();
        
        for (WorkProgramEntity e : items) {
            if (e == null) continue;
            
            String season = e.season != null ? e.season : (e.isOffSeason ? "off-season" : "on-season");
            SeasonSummary s = map.get(season);
            if (s == null) {
                s = new SeasonSummary(season);
                map.put(season, s);
            }
            
            s.programCount++;
            s.totalArea += e.areaSize;
            s.totalIncome += e.projectedIncome;
            s.totalExpenses += e.projectedExpenses;
            s.totalAdjustedIncome += e.adjustedIncome;
            s.totalAdjustedExpenses += e.adjustedExpenses;
            if (e.completionRate > 0) {
                s.totalCompletionRate += e.completionRate;
            }
            s.totalYield += e.totalYield;
            s.totalActualYield += e.actualYield;
        }
        
        return new ArrayList<>(map.values());
    }
    
    /**
     * Compare on-season vs off-season performance.
     * Returns a comparison object with key metrics.
     */
    public static class SeasonComparison {
        public SeasonSummary onSeason;
        public SeasonSummary offSeason;
        
        public double getProfitDifference() {
            if (onSeason == null || offSeason == null) return 0;
            return offSeason.getProfit() - onSeason.getProfit();
        }
        
        public double getProfitDifferencePercent() {
            if (onSeason == null || onSeason.getProfit() == 0) return 0;
            return (getProfitDifference() / onSeason.getProfit()) * 100;
        }
        
        public double getYieldDifference() {
            if (onSeason == null || offSeason == null) return 0;
            return offSeason.getAverageYield() - onSeason.getAverageYield();
        }
        
        public double getYieldDifferencePercent() {
            if (onSeason == null || onSeason.getAverageYield() == 0) return 0;
            return (getYieldDifference() / onSeason.getAverageYield()) * 100;
        }
    }
    
    /**
     * Create a season comparison from summaries.
     */
    public SeasonComparison compareSeasons(List<SeasonSummary> summaries) {
        SeasonComparison comparison = new SeasonComparison();
        
        for (SeasonSummary summary : summaries) {
            if ("on-season".equals(summary.season)) {
                comparison.onSeason = summary;
            } else if ("off-season".equals(summary.season)) {
                comparison.offSeason = summary;
            }
        }
        
        return comparison;
    }
}


