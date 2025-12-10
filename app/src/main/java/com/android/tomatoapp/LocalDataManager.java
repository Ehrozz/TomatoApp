package com.android.tomatoapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.tomatoapp.notifications.NotificationPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalDataManager {
    private static final String TAG = "LocalDataManager";
    private static LocalDataManager instance;
    private final AppDatabase database;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private LocalDataManager(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized LocalDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalDataManager(context.getApplicationContext());
        }
        return instance;
    }

    // Sync Work Programs from Firebase
    public void syncWorkProgramsFromFirebase(String userId) {
        executorService.execute(() -> {
            try {
                DatabaseReference dbRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("workPrograms");

                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        executorService.execute(() -> {
                            try {
                                List<WorkProgramEntity> entities = new ArrayList<>();
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    String programId = child.getKey();
                                    if (programId == null) continue;

                                    WorkProgramEntity entity = parseWorkProgramFromSnapshot(child, programId, userId);
                                    if (entity != null) {
                                        entities.add(entity);
                                    }
                                }

                                // Save to local database
                                for (WorkProgramEntity entity : entities) {
                                    database.workProgramDao().upsert(entity);
                                }
                                Log.d(TAG, "Synced " + entities.size() + " work programs");
                            } catch (Exception e) {
                                Log.e(TAG, "Error syncing work programs", e);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error reading work programs", error.toException());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error syncing work programs", e);
            }
        });
    }

    // Sync Calculations from Firebase
    public void syncCalculationsFromFirebase(String userId) {
        executorService.execute(() -> {
            try {
                DatabaseReference dbRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("calculations");

                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        executorService.execute(() -> {
                            try {
                                List<CalculationEntity> entities = new ArrayList<>();
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    String calculationId = child.getKey();
                                    if (calculationId == null) continue;

                                    CalculationModel model = child.getValue(CalculationModel.class);
                                    if (model != null) {
                                        CalculationEntity entity = new CalculationEntity(
                                                calculationId,
                                                userId,
                                                null, // programId
                                                model.grossIncome,
                                                model.totalExpenses,
                                                model.netIncome,
                                                model.hectare,
                                                model.dateCreated,
                                                null, // dateSaved
                                                null, // cultivarName
                                                System.currentTimeMillis()
                                        );
                                        entities.add(entity);
                                    }
                                }

                                // Save to local database
                                for (CalculationEntity entity : entities) {
                                    database.calculationDao().insert(entity);
                                }
                                Log.d(TAG, "Synced " + entities.size() + " calculations");
                            } catch (Exception e) {
                                Log.e(TAG, "Error syncing calculations", e);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error reading calculations", error.toException());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error syncing calculations", e);
            }
        });
    }

    // Sync Detection History from SharedPreferences and Firebase
    public void syncDetectionHistoryFromFirebase(Context context, String userId) {
        executorService.execute(() -> {
            try {
                // First, migrate from SharedPreferences
                ArrayList<JSONObject> historyList = DetectionHistoryManager.getHistory(context);
                for (JSONObject entry : historyList) {
                    try {
                        String id = entry.optString("imageUri", "") + "_" + entry.optLong("timestamp", 0);
                        DetectionHistoryEntity entity = new DetectionHistoryEntity(
                                id,
                                userId,
                                null, // programId
                                entry.optString("imageUri", ""),
                                entry.optString("disease", ""),
                                entry.optString("accuracy", ""),
                                entry.optString("description", ""),
                                entry.optString("symptoms", ""),
                                entry.optString("cause", ""),
                                entry.optString("cure", ""),
                                entry.optString("prevention", ""),
                                entry.optString("pestTitle", ""),
                                entry.optString("pestDescription", ""),
                                entry.optString("pestImageUri", ""),
                                entry.optLong("timestamp", System.currentTimeMillis()),
                                entry.optString("cultivar", ""),
                                entry.optInt("phase", 0),
                                System.currentTimeMillis()
                        );
                        database.detectionHistoryDao().insert(entity);
                    } catch (Exception e) {
                        Log.e(TAG, "Error migrating detection history entry", e);
                    }
                }
                Log.d(TAG, "Migrated " + historyList.size() + " detection history entries");
            } catch (Exception e) {
                Log.e(TAG, "Error syncing detection history", e);
            }
        });
    }

    // Sync Tasks from Firebase
    public void syncTasksFromFirebase(String userId, String programId) {
        executorService.execute(() -> {
            try {
                DatabaseReference dbRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("workPrograms")
                        .child(programId)
                        .child("logs");

                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        executorService.execute(() -> {
                            try {
                                List<TaskEntity> entities = new ArrayList<>();
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    String dateKey = child.getKey();
                                    if (dateKey == null) continue;

                                    String status = child.getValue(String.class);
                                    if (status != null) {
                                        String taskId = programId + "_" + dateKey;
                                        // Note: We need task details from the work program phases
                                        // For now, create a basic entity
                                        TaskEntity entity = new TaskEntity(
                                                taskId,
                                                userId,
                                                programId,
                                                dateKey,
                                                "", // taskName - would need to fetch from phases
                                                "", // category
                                                "", // iconType
                                                0,  // dayNumber
                                                "", // phase
                                                status,
                                                System.currentTimeMillis()
                                        );
                                        entities.add(entity);
                                    }
                                }

                                // Save to local database
                                if (!entities.isEmpty()) {
                                    database.taskDao().insertAll(entities);
                                }
                                Log.d(TAG, "Synced " + entities.size() + " tasks for program " + programId);
                            } catch (Exception e) {
                                Log.e(TAG, "Error syncing tasks", e);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error reading tasks", error.toException());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error syncing tasks", e);
            }
        });
    }

    // Sync Settings to Local Database
    public void syncSettingsToLocal(Context context, String userId) {
        executorService.execute(() -> {
            try {
                SettingsEntity entity = new SettingsEntity(
                        userId,
                        SettingsPreferences.getLanguage(context),
                        SettingsPreferences.getTheme(context),
                        SettingsPreferences.getDefaultCultivar(context),
                        SettingsPreferences.getWeatherUnit(context),
                        SettingsPreferences.getMeasurementUnit(context),
                        SettingsPreferences.getDateFormat(context),
                        NotificationPreferences.areTaskNotificationsEnabled(context),
                        NotificationPreferences.areMonitoringNotificationsEnabled(context),
                        NotificationPreferences.areGeneralNotificationsEnabled(context),
                        SettingsPreferences.getNotificationSound(context),
                        String.format("%02d:%02d", SettingsPreferences.getNotificationHour(context), SettingsPreferences.getNotificationMinute(context)),
                        SettingsPreferences.isQuietHoursEnabled(context),
                        String.format("%02d:%02d", SettingsPreferences.getQuietHoursStartHour(context), SettingsPreferences.getQuietHoursStartMinute(context)),
                        String.format("%02d:%02d", SettingsPreferences.getQuietHoursEndHour(context), SettingsPreferences.getQuietHoursEndMinute(context)),
                        System.currentTimeMillis()
                );
                database.settingsDao().insert(entity);
                Log.d(TAG, "Synced settings for user " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error syncing settings", e);
            }
        });
    }

    // Save Calculation to Local Database
    public void saveCalculation(String calculationId, String userId, String programId,
                                double grossIncome, double totalExpenses, double netIncome,
                                double hectare, String dateCreated, String dateSaved,
                                String cultivarName) {
        executorService.execute(() -> {
            try {
                CalculationEntity entity = new CalculationEntity(
                        calculationId,
                        userId,
                        programId,
                        grossIncome,
                        totalExpenses,
                        netIncome,
                        hectare,
                        dateCreated,
                        dateSaved,
                        cultivarName,
                        System.currentTimeMillis()
                );
                database.calculationDao().insert(entity);
                Log.d(TAG, "Saved calculation to local database");
            } catch (Exception e) {
                Log.e(TAG, "Error saving calculation", e);
            }
        });
    }

    // Save Detection History Entry
    public void saveDetectionHistory(String userId, String programId, String imageUri,
                                     String disease, String accuracy, String description,
                                     String symptoms, String cause, String cure,
                                     String prevention, String pestTitle, String pestDescription,
                                     String pestImageUri, long timestamp, String cultivar, int phase) {
        executorService.execute(() -> {
            try {
                String id = imageUri + "_" + timestamp;
                DetectionHistoryEntity entity = new DetectionHistoryEntity(
                        id,
                        userId,
                        programId,
                        imageUri,
                        disease,
                        accuracy,
                        description,
                        symptoms,
                        cause,
                        cure,
                        prevention,
                        pestTitle,
                        pestDescription,
                        pestImageUri,
                        timestamp,
                        cultivar,
                        phase,
                        System.currentTimeMillis()
                );
                database.detectionHistoryDao().insert(entity);
                Log.d(TAG, "Saved detection history to local database");
            } catch (Exception e) {
                Log.e(TAG, "Error saving detection history", e);
            }
        });
    }

    // Save Task Status
    public void saveTaskStatus(String userId, String programId, String dateKey,
                              String taskName, String category, String iconType,
                              int dayNumber, String phase, String status) {
        executorService.execute(() -> {
            try {
                String taskId = programId + "_" + dateKey;
                TaskEntity entity = new TaskEntity(
                        taskId,
                        userId,
                        programId,
                        dateKey,
                        taskName,
                        category,
                        iconType,
                        dayNumber,
                        phase,
                        status,
                        System.currentTimeMillis()
                );
                database.taskDao().insert(entity);
                Log.d(TAG, "Saved task status to local database");
            } catch (Exception e) {
                Log.e(TAG, "Error saving task status", e);
            }
        });
    }

    // Export All Data as JSON
    public String exportAllData(String userId) {
        try {
            JSONObject exportData = new JSONObject();
            exportData.put("userId", userId);
            exportData.put("exportDate", System.currentTimeMillis());

            // Export calculations
            List<CalculationEntity> calculations = database.calculationDao().getAllByUser(userId);
            JSONArray calculationsArray = new JSONArray();
            for (CalculationEntity calc : calculations) {
                JSONObject obj = new JSONObject();
                obj.put("id", calc.id);
                obj.put("programId", calc.programId);
                obj.put("grossIncome", calc.grossIncome);
                obj.put("totalExpenses", calc.totalExpenses);
                obj.put("netIncome", calc.netIncome);
                obj.put("hectare", calc.hectare);
                obj.put("dateCreated", calc.dateCreated);
                obj.put("dateSaved", calc.dateSaved);
                obj.put("cultivarName", calc.cultivarName);
                calculationsArray.put(obj);
            }
            exportData.put("calculations", calculationsArray);

            // Export detection history
            List<DetectionHistoryEntity> detections = database.detectionHistoryDao().getAllByUser(userId);
            JSONArray detectionsArray = new JSONArray();
            for (DetectionHistoryEntity det : detections) {
                JSONObject obj = new JSONObject();
                obj.put("id", det.id);
                obj.put("imageUri", det.imageUri);
                obj.put("disease", det.disease);
                obj.put("accuracy", det.accuracy);
                obj.put("timestamp", det.timestamp);
                obj.put("cultivar", det.cultivar);
                obj.put("phase", det.phase);
                detectionsArray.put(obj);
            }
            exportData.put("detectionHistory", detectionsArray);

            // Export settings
            SettingsEntity settings = database.settingsDao().getByUser(userId);
            if (settings != null) {
                JSONObject settingsObj = new JSONObject();
                settingsObj.put("language", settings.language);
                settingsObj.put("theme", settings.theme);
                settingsObj.put("defaultCultivar", settings.defaultCultivar);
                exportData.put("settings", settingsObj);
            }

            return exportData.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error exporting data", e);
            return null;
        }
    }

    // Clear All Local Data
    public void clearAllLocalData(String userId) {
        executorService.execute(() -> {
            try {
                database.calculationDao().deleteAllByUser(userId);
                database.detectionHistoryDao().deleteAllByUser(userId);
                database.taskDao().deleteByProgram(userId, ""); // Note: This might need adjustment
                database.settingsDao().delete(database.settingsDao().getByUser(userId));
                Log.d(TAG, "Cleared all local data for user " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing local data", e);
            }
        });
    }

    // Read Work Programs from Local Database
    public List<WorkProgramEntity> getWorkProgramsFromLocal(String userId) {
        try {
            return database.workProgramDao().getAllForUser(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error reading work programs from local database", e);
            return new ArrayList<>();
        }
    }

    // Read Calculations from Local Database
    public List<CalculationEntity> getCalculationsFromLocal(String userId) {
        try {
            return database.calculationDao().getAllByUser(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error reading calculations from local database", e);
            return new ArrayList<>();
        }
    }

    // Read Detection History from Local Database
    public List<DetectionHistoryEntity> getDetectionHistoryFromLocal(String userId) {
        try {
            return database.detectionHistoryDao().getAllByUser(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error reading detection history from local database", e);
            return new ArrayList<>();
        }
    }

    // Read Tasks from Local Database
    public List<TaskEntity> getTasksFromLocal(String userId, String programId) {
        try {
            return database.taskDao().getByProgram(userId, programId);
        } catch (Exception e) {
            Log.e(TAG, "Error reading tasks from local database", e);
            return new ArrayList<>();
        }
    }

    // Check if device is online
    public static boolean isOnline(Context context) {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to parse WorkProgram from Firebase snapshot
    private WorkProgramEntity parseWorkProgramFromSnapshot(DataSnapshot snapshot, String programId, String userId) {
        try {
            // This is a simplified version - you may need to adjust based on actual Firebase structure
            String cultivarName = snapshot.child("cultivarName").getValue(String.class);
            String startingDate = snapshot.child("startingDate").getValue(String.class);
            Object areaSizeObj = snapshot.child("areaSize").getValue();
            double areaSize = 0;
            if (areaSizeObj != null) {
                if (areaSizeObj instanceof Double) {
                    areaSize = (Double) areaSizeObj;
                } else if (areaSizeObj instanceof Long) {
                    areaSize = ((Long) areaSizeObj).doubleValue();
                }
            }

            // Create a basic entity - you may need to add more fields
            return new WorkProgramEntity(
                    programId,
                    userId,
                    cultivarName != null ? cultivarName : "",
                    areaSize,
                    startingDate != null ? startingDate : "",
                    "", // phasesJson
                    "", // detectionHistoriesJson
                    0, 0, 0, 0, // projected/adjusted income/expenses
                    0, 0, 0, 0, 0, // phase completions
                    0, 0, 0, 0, 0.0 // task metrics
            );
        } catch (Exception e) {
            Log.e(TAG, "Error parsing work program", e);
            return null;
        }
    }
}

