package com.android.tomatoapp.weather.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Stores weather data for a work program to enable research analysis.
 * Weather data is collected during the growing period.
 */
@Entity(tableName = "weather_data")
public class WeatherData {
    
    @PrimaryKey
    @NonNull
    public String programId; // Links to WorkProgramEntity.id
    
    public String userId;
    public String plantingDate;
    
    // Average weather metrics during growing period
    public double avgTemperature; // Celsius
    public double avgMinTemperature; // Celsius
    public double avgMaxTemperature; // Celsius
    public double totalPrecipitation; // mm
    public double avgHumidity; // Percentage (if available)
    
    // Weather extremes
    public double minTemperature; // Lowest temperature recorded
    public double maxTemperature; // Highest temperature recorded
    public double maxDailyPrecipitation; // Highest daily precipitation
    
    // Date range
    public String weatherStartDate; // First weather reading date
    public String weatherEndDate; // Last weather reading date
    public int daysTracked; // Number of days weather was tracked
    
    // Timestamp
    public long lastUpdated; // Timestamp of last update
    
    public WeatherData(@NonNull String programId,
                      String userId,
                      String plantingDate,
                      double avgTemperature,
                      double avgMinTemperature,
                      double avgMaxTemperature,
                      double totalPrecipitation,
                      double avgHumidity,
                      double minTemperature,
                      double maxTemperature,
                      double maxDailyPrecipitation,
                      String weatherStartDate,
                      String weatherEndDate,
                      int daysTracked,
                      long lastUpdated) {
        this.programId = programId;
        this.userId = userId;
        this.plantingDate = plantingDate;
        this.avgTemperature = avgTemperature;
        this.avgMinTemperature = avgMinTemperature;
        this.avgMaxTemperature = avgMaxTemperature;
        this.totalPrecipitation = totalPrecipitation;
        this.avgHumidity = avgHumidity;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.maxDailyPrecipitation = maxDailyPrecipitation;
        this.weatherStartDate = weatherStartDate;
        this.weatherEndDate = weatherEndDate;
        this.daysTracked = daysTracked;
        this.lastUpdated = lastUpdated;
    }
}

