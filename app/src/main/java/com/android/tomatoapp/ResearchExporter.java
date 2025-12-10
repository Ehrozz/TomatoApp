package com.android.tomatoapp;

import android.content.Context;
import android.os.Environment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Exports research data to CSV format for analysis.
 * Includes all variables needed for proving off-season tomato planting viability.
 */
public class ResearchExporter {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat exportSdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    
    /**
     * Export all work programs to CSV with research variables.
     * @param context Application context
     * @param programs List of work programs to export
     * @param includeWeather Whether to include weather data (requires additional queries)
     * @return File path of created CSV, or null if failed
     */
    public static String exportToCsv(Context context, List<WorkProgramEntity> programs, boolean includeWeather) {
        if (context == null || programs == null || programs.isEmpty()) {
            return null;
        }
        
        try {
            String timestamp = exportSdf.format(new Date());
            String fileName = "Tomato_Research_Data_" + timestamp + ".csv";
            
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            File csvFile = new File(downloadsDir, fileName);
            FileWriter writer = new FileWriter(csvFile);
            
            // Write CSV header
            writer.append("Program ID,Cultivar,Planting Date,Harvest Date,Area (hectare),Season,Season Month,Is Off-Season,");
            writer.append("Projected Income (PHP),Projected Expenses (PHP),Actual Income (PHP),Actual Expenses (PHP),");
            writer.append("Net Profit (PHP),Adjusted Income (PHP),Adjusted Expenses (PHP),Adjusted Profit (PHP),");
            writer.append("Actual Yield (kg/hectare),Total Yield (kg),Completion Rate (%),");
            writer.append("Total Tasks,Completed Tasks,Missed Tasks,Skipped Tasks,");
            writer.append("Phase 1 Completion (%),Phase 2 Completion (%),Phase 3 Completion (%),");
            writer.append("Phase 4 Completion (%),Phase 5 Completion (%)\n");
            
            // Write data rows
            for (WorkProgramEntity program : programs) {
                writer.append(escapeCsv(program.id)).append(",");
                writer.append(escapeCsv(program.cultivarName)).append(",");
                writer.append(escapeCsv(program.startingDate)).append(",");
                writer.append(escapeCsv(program.harvestDate)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.areaSize)).append(",");
                writer.append(escapeCsv(program.season != null ? program.season : "")).append(",");
                writer.append(String.valueOf(program.seasonMonth)).append(",");
                writer.append(program.isOffSeason ? "Yes" : "No").append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.projectedIncome)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.projectedExpenses)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.projectedIncome)).append(","); // Actual = projected for now
                writer.append(String.format(Locale.getDefault(), "%.2f", program.projectedExpenses)).append(","); // Actual = projected for now
                double netProfit = program.projectedIncome - program.projectedExpenses;
                writer.append(String.format(Locale.getDefault(), "%.2f", netProfit)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.adjustedIncome)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.adjustedExpenses)).append(",");
                double adjustedProfit = program.adjustedIncome - program.adjustedExpenses;
                writer.append(String.format(Locale.getDefault(), "%.2f", adjustedProfit)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.actualYield)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.totalYield)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.completionRate)).append(",");
                writer.append(String.valueOf(program.totalTasks)).append(",");
                writer.append(String.valueOf(program.completedTasks)).append(",");
                writer.append(String.valueOf(program.missedTasks)).append(",");
                writer.append(String.valueOf(program.skippedTasks)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.phase1Completion)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.phase2Completion)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.phase3Completion)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.phase4Completion)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.phase5Completion)).append("\n");
            }
            
            writer.flush();
            writer.close();
            
            return csvFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Export with weather data included.
     * This requires additional database queries.
     */
    public static String exportToCsvWithWeather(Context context, List<WorkProgramEntity> programs) {
        if (context == null || programs == null || programs.isEmpty()) {
            return null;
        }
        
        AppDatabase db = AppDatabase.getInstance(context);
        WeatherDataDao weatherDao = db.weatherDataDao();
        
        try {
            String timestamp = exportSdf.format(new Date());
            String fileName = "Tomato_Research_Data_With_Weather_" + timestamp + ".csv";
            
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            File csvFile = new File(downloadsDir, fileName);
            FileWriter writer = new FileWriter(csvFile);
            
            // Write CSV header with weather columns
            writer.append("Program ID,Cultivar,Planting Date,Harvest Date,Area (hectare),Season,Season Month,Is Off-Season,");
            writer.append("Projected Income (PHP),Projected Expenses (PHP),Net Profit (PHP),");
            writer.append("Actual Yield (kg/hectare),Total Yield (kg),Completion Rate (%),");
            writer.append("Avg Temperature (°C),Avg Min Temp (°C),Avg Max Temp (°C),");
            writer.append("Total Precipitation (mm),Avg Humidity (%),");
            writer.append("Min Temperature (°C),Max Temperature (°C),Max Daily Precipitation (mm),");
            writer.append("Days Tracked\n");
            
            // Write data rows
            for (WorkProgramEntity program : programs) {
                WeatherData weather = weatherDao.getByProgramId(program.id);
                
                writer.append(escapeCsv(program.id)).append(",");
                writer.append(escapeCsv(program.cultivarName)).append(",");
                writer.append(escapeCsv(program.startingDate)).append(",");
                writer.append(escapeCsv(program.harvestDate)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.areaSize)).append(",");
                writer.append(escapeCsv(program.season != null ? program.season : "")).append(",");
                writer.append(String.valueOf(program.seasonMonth)).append(",");
                writer.append(program.isOffSeason ? "Yes" : "No").append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.projectedIncome)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.projectedExpenses)).append(",");
                double netProfit = program.projectedIncome - program.projectedExpenses;
                writer.append(String.format(Locale.getDefault(), "%.2f", netProfit)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.actualYield)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.totalYield)).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", program.completionRate)).append(",");
                
                if (weather != null) {
                    writer.append(String.format(Locale.getDefault(), "%.2f", weather.avgTemperature)).append(",");
                    writer.append(String.format(Locale.getDefault(), "%.2f", weather.avgMinTemperature)).append(",");
                    writer.append(String.format(Locale.getDefault(), "%.2f", weather.avgMaxTemperature)).append(",");
                    writer.append(String.format(Locale.getDefault(), "%.2f", weather.totalPrecipitation)).append(",");
                    writer.append(String.format(Locale.getDefault(), "%.2f", weather.avgHumidity)).append(",");
                    writer.append(String.format(Locale.getDefault(), "%.2f", weather.minTemperature)).append(",");
                    writer.append(String.format(Locale.getDefault(), "%.2f", weather.maxTemperature)).append(",");
                    writer.append(String.format(Locale.getDefault(), "%.2f", weather.maxDailyPrecipitation)).append(",");
                    writer.append(String.valueOf(weather.daysTracked)).append("\n");
                } else {
                    writer.append(",,,,\n"); // Empty weather data
                }
            }
            
            writer.flush();
            writer.close();
            
            return csvFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

