package com.android.tomatoapp.weather.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.tomatoapp.core.database.AppDatabase;
import com.android.tomatoapp.workprogram.data.WorkProgramDao;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Collects and stores weather data for work programs.
 * This enables correlation between weather conditions and crop performance.
 */
public class WeatherDataCollector {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    /**
     * Update weather data for a work program.
     * Should be called periodically during the growing season.
     */
    public static void updateWeatherData(Context context, String programId, String plantingDate,
                                        double temperature, double minTemp, double maxTemp,
                                        double precipitation, double humidity) {
        if (context == null || programId == null || plantingDate == null) {
            return;
        }
        
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            WeatherDataDao weatherDao = db.weatherDataDao();
            
            WeatherData existing = weatherDao.getByProgramId(programId);
            
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String userId = user != null ? user.getUid() : null;
            
            if (existing == null) {
                // Create new weather data entry
                WeatherData weatherData = new WeatherData(
                        programId,
                        userId,
                        plantingDate,
                        temperature,
                        minTemp,
                        maxTemp,
                        precipitation,
                        humidity,
                        minTemp,
                        maxTemp,
                        precipitation,
                        sdf.format(new Date()),
                        sdf.format(new Date()),
                        1,
                        System.currentTimeMillis()
                );
                weatherDao.upsert(weatherData);
            } else {
                // Update existing entry with running averages
                int daysTracked = existing.daysTracked + 1;
                double newAvgTemp = ((existing.avgTemperature * existing.daysTracked) + temperature) / daysTracked;
                double newAvgMinTemp = ((existing.avgMinTemperature * existing.daysTracked) + minTemp) / daysTracked;
                double newAvgMaxTemp = ((existing.avgMaxTemperature * existing.daysTracked) + maxTemp) / daysTracked;
                double newTotalPrecip = existing.totalPrecipitation + precipitation;
                double newAvgHumidity = existing.avgHumidity > 0 ? 
                    ((existing.avgHumidity * existing.daysTracked) + humidity) / daysTracked : humidity;
                
                double newMinTemp = Math.min(existing.minTemperature, minTemp);
                double newMaxTemp = Math.max(existing.maxTemperature, maxTemp);
                double newMaxDailyPrecip = Math.max(existing.maxDailyPrecipitation, precipitation);
                
                WeatherData updated = new WeatherData(
                        programId,
                        userId,
                        plantingDate,
                        newAvgTemp,
                        newAvgMinTemp,
                        newAvgMaxTemp,
                        newTotalPrecip,
                        newAvgHumidity,
                        newMinTemp,
                        newMaxTemp,
                        newMaxDailyPrecip,
                        existing.weatherStartDate,
                        sdf.format(new Date()),
                        daysTracked,
                        System.currentTimeMillis()
                );
                weatherDao.upsert(updated);
            }
        });
    }
    
    /**
     * Collect weather data from Open-Meteo API and store it.
     * This should be called daily or periodically for active programs.
     * Fetches current weather and updates the weather data entry.
     */
    public static void collectCurrentWeather(Context context, String programId, String plantingDate,
                                            double lat, double lon) {
        if (context == null || programId == null || plantingDate == null) {
            return;
        }
        
        executor.execute(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                // Fetch current weather and today's daily data
                String urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon +
                        "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min,precipitation_sum&forecast_days=1&timezone=auto";
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    JSONObject root = new JSONObject(sb.toString());
                    JSONObject current = root.optJSONObject("current");
                    JSONObject daily = root.optJSONObject("daily");
                    
                    double temperature = Double.NaN;
                    double minTemp = Double.NaN;
                    double maxTemp = Double.NaN;
                    double precipitation = 0.0;
                    double humidity = 0.0; // Open-Meteo free API doesn't provide humidity in forecast
                    
                    // Get current temperature
                    if (current != null) {
                        temperature = current.optDouble("temperature_2m", Double.NaN);
                    }
                    
                    // Get daily min/max and precipitation
                    if (daily != null) {
                        JSONArray tmax = daily.optJSONArray("temperature_2m_max");
                        JSONArray tmin = daily.optJSONArray("temperature_2m_min");
                        JSONArray precip = daily.optJSONArray("precipitation_sum");
                        
                        if (tmax != null && tmax.length() > 0) {
                            maxTemp = tmax.optDouble(0, Double.NaN);
                        }
                        if (tmin != null && tmin.length() > 0) {
                            minTemp = tmin.optDouble(0, Double.NaN);
                        }
                        if (precip != null && precip.length() > 0) {
                            precipitation = precip.optDouble(0, 0.0);
                        }
                    }
                    
                    // Use current temp as average if daily data not available
                    if (Double.isNaN(temperature) && !Double.isNaN(minTemp) && !Double.isNaN(maxTemp)) {
                        temperature = (minTemp + maxTemp) / 2.0;
                    } else if (Double.isNaN(temperature)) {
                        // No temperature data available
                        Log.w("WeatherDataCollector", "No temperature data available for program " + programId);
                        return;
                    }
                    
                    // Use min/max from current if daily not available
                    if (Double.isNaN(minTemp)) minTemp = temperature - 5; // Estimate
                    if (Double.isNaN(maxTemp)) maxTemp = temperature + 5; // Estimate
                    
                    // Update weather data with fetched values
                    updateWeatherData(context, programId, plantingDate, 
                                    temperature, minTemp, maxTemp, precipitation, humidity);
                    
                    Log.d("WeatherDataCollector", "Successfully collected weather for program " + programId);
                } else {
                    Log.e("WeatherDataCollector", "Failed to fetch weather: HTTP " + code);
                }
            } catch (Exception e) {
                Log.e("WeatherDataCollector", "Error fetching weather data: " + e.getMessage(), e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception ignored) {}
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }
    
    /**
     * Collect weather data for a program using location from SharedPreferences.
     * This is a convenience method that gets location and calls collectCurrentWeather.
     */
    public static void collectWeatherForProgram(Context context, String programId, String plantingDate) {
        if (context == null || programId == null || plantingDate == null) {
            return;
        }
        
        SharedPreferences wp = context.getSharedPreferences("WeatherPref", Context.MODE_PRIVATE);
        if (wp.contains("lat") && wp.contains("lon")) {
            double lat = Double.longBitsToDouble(wp.getLong("lat", Double.doubleToLongBits(0)));
            double lon = Double.longBitsToDouble(wp.getLong("lon", Double.doubleToLongBits(0)));
            collectCurrentWeather(context, programId, plantingDate, lat, lon);
        } else {
            // Use default Philippines location (Lopez, Quezon)
            collectCurrentWeather(context, programId, plantingDate, 13.8840, 122.2633);
        }
    }
    
    /**
     * Update weather data for all active work programs.
     * This should be called periodically (e.g., when app starts, or daily).
     * Only updates programs that are still in their growing season.
     */
    public static void updateWeatherForAllActivePrograms(Context context) {
        if (context == null) {
            return;
        }
        
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                WorkProgramDao workProgramDao = db.workProgramDao();
                
                // Get all work programs for current user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    return;
                }
                
                List<WorkProgramEntity> programs = workProgramDao.getAllForUser(user.getUid());
                if (programs == null || programs.isEmpty()) {
                    return;
                }
                
                // Get location from SharedPreferences
                SharedPreferences wp = context.getSharedPreferences("WeatherPref", Context.MODE_PRIVATE);
                double lat, lon;
                if (wp.contains("lat") && wp.contains("lon")) {
                    lat = Double.longBitsToDouble(wp.getLong("lat", Double.doubleToLongBits(0)));
                    lon = Double.longBitsToDouble(wp.getLong("lon", Double.doubleToLongBits(0)));
                } else {
                    // Use default Philippines location
                    lat = 13.8840;
                    lon = 122.2633;
                }
                
                // Check if programs are still active (within growing season)
                Date today = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(today);
                
                for (WorkProgramEntity program : programs) {
                    if (program.startingDate == null || program.startingDate.isEmpty()) {
                        continue;
                    }
                    
                    try {
                        Date startDate = sdf.parse(program.startingDate);
                        if (startDate == null) continue;
                        
                        // Estimate end date (assuming max 140 days growing season)
                        cal.setTime(startDate);
                        cal.add(Calendar.DAY_OF_YEAR, 140);
                        Date estimatedEndDate = cal.getTime();
                        
                        // Only update weather if program is still active
                        if (today.before(estimatedEndDate) || today.equals(estimatedEndDate)) {
                            collectCurrentWeather(context, program.id, program.startingDate, lat, lon);
                            // Small delay to avoid overwhelming the API
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        Log.e("WeatherDataCollector", "Error processing program " + program.id + ": " + e.getMessage());
                    }
                }
                
                Log.d("WeatherDataCollector", "Updated weather for " + programs.size() + " active programs");
            } catch (Exception e) {
                Log.e("WeatherDataCollector", "Error updating weather for all programs: " + e.getMessage(), e);
            }
        });
    }
}

