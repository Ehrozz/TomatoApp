package com.android.tomatoapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class to determine if a planting date falls in on-season or off-season.
 * 
 * For Philippines:
 * - On-season: October to February (dry season, cooler temperatures)
 * - Off-season: March to September (wet season, warmer/humid)
 */
public class SeasonHelper {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    /**
     * Determines if a planting date is off-season.
     * @param plantingDate Date string in format "yyyy-MM-dd"
     * @return true if off-season, false if on-season
     */
    public static boolean isOffSeason(String plantingDate) {
        if (plantingDate == null || plantingDate.isEmpty()) {
            return false;
        }
        
        try {
            Date date = sdf.parse(plantingDate);
            if (date == null) {
                return false;
            }
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
            
            // Off-season: March (3) to September (9)
            // On-season: October (10) to February (2)
            return month >= 3 && month <= 9;
        } catch (ParseException e) {
            return false;
        }
    }
    
    /**
     * Gets the season classification for a planting date.
     * @param plantingDate Date string in format "yyyy-MM-dd"
     * @return "off-season" or "on-season"
     */
    public static String getSeason(String plantingDate) {
        return isOffSeason(plantingDate) ? "off-season" : "on-season";
    }
    
    /**
     * Gets the month number (1-12) from a planting date.
     * @param plantingDate Date string in format "yyyy-MM-dd"
     * @return Month number (1-12), or 0 if invalid
     */
    public static int getSeasonMonth(String plantingDate) {
        if (plantingDate == null || plantingDate.isEmpty()) {
            return 0;
        }
        
        try {
            Date date = sdf.parse(plantingDate);
            if (date == null) {
                return 0;
            }
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        } catch (ParseException e) {
            return 0;
        }
    }
    
    /**
     * Gets the season name (e.g., "Dry Season", "Wet Season").
     * @param plantingDate Date string in format "yyyy-MM-dd"
     * @return Season name
     */
    public static String getSeasonName(String plantingDate) {
        if (isOffSeason(plantingDate)) {
            return "Wet Season (Off-Season)";
        } else {
            return "Dry Season (On-Season)";
        }
    }
    
    /**
     * Gets month name from month number.
     * @param month Month number (1-12)
     * @return Month name
     */
    public static String getMonthName(int month) {
        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        if (month >= 1 && month <= 12) {
            return months[month - 1];
        }
        return "Unknown";
    }
    
    /**
     * Gets price multiplier based on season.
     * Off-season typically has higher prices due to lower supply.
     * @param plantingDate Date string in format "yyyy-MM-dd"
     * @return Price multiplier (typically 1.0 for on-season, 1.15-1.25 for off-season)
     */
    public static double getPriceMultiplier(String plantingDate) {
        if (isOffSeason(plantingDate)) {
            // Off-season: 15-25% price premium due to lower supply
            return 1.20; // 20% premium
        } else {
            // On-season: standard prices
            return 1.0;
        }
    }
}

