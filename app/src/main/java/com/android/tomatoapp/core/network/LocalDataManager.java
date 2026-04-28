package com.android.tomatoapp.core.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.tomatoapp.common.models.UserLocationEntity;
import com.android.tomatoapp.core.database.AppDatabase;
import com.android.tomatoapp.core.sync.SyncActionEntity;
import com.android.tomatoapp.detection.data.DetectionHistoryEntity;
import com.android.tomatoapp.detection.data.DetectionHistoryManager;
import com.android.tomatoapp.financial.data.CalculationEntity;
import com.android.tomatoapp.financial.data.CalculationModel;
import com.android.tomatoapp.notifications.NotificationPreferences;
import com.android.tomatoapp.season.data.SeasonHelper;
import com.android.tomatoapp.settings.data.SettingsEntity;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.task.data.TaskEntity;
import com.android.tomatoapp.workprogram.data.WorkProgramDataHelper;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalDataManager {
    private static final String TAG = "LocalDataManager";
    private static LocalDataManager instance;
    private final AppDatabase database;
    private final ExecutorService executorService;

    private LocalDataManager(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newFixedThreadPool(2);
    }

    public static synchronized LocalDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalDataManager(context.getApplicationContext());
        }
        return instance;
    }

    // Sync Work Programs from Firebase to Local (with conflict resolution)
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
                                List<WorkProgramEntity> localPrograms = database.workProgramDao().getAllForUser(userId);
                                java.util.Map<String, WorkProgramEntity> localMap = new java.util.HashMap<>();
                                for (WorkProgramEntity lp : localPrograms) {
                                    localMap.put(lp.id, lp);
                                }

                                int updatedCount = 0;
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    String programId = child.getKey();
                                    if (programId == null) continue;

                                    WorkProgramEntity remoteEntity = parseWorkProgramFromSnapshot(child, programId, userId);
                                    if (remoteEntity != null) {
                                        WorkProgramEntity local = localMap.get(programId);
                                        // Conflict Resolution: Latest update wins
                                        if (local == null || remoteEntity.lastUpdated > local.lastUpdated) {
                                            database.workProgramDao().upsert(remoteEntity);
                                            updatedCount++;
                                        }
                                    }
                                }
                                Log.d(TAG, "Synced " + updatedCount + " work programs from Firebase (Conflict Resolution Applied)");
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

    // Process Sync Queue (Upload offline changes to Firebase)
    public void processSyncQueue(Context context, String userId) {
        executorService.execute(() -> {
            try {
                if (!isOnline(context)) return;

                List<SyncActionEntity> pendingActions = database.syncActionDao().getAllPending(userId);
                if (pendingActions.isEmpty()) return;

                Log.d(TAG, "Processing " + pendingActions.size() + " pending actions in sync queue");
                
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                for (SyncActionEntity action : pendingActions) {
                    DatabaseReference targetRef = null;
                    if ("work_program".equals(action.entityType)) {
                        targetRef = rootRef.child("workPrograms").child(action.entityId);
                    } else if ("location".equals(action.entityType)) {
                        targetRef = rootRef.child("locations").child(action.entityId);
                    }

                    if (targetRef == null) continue;

                    if ("DELETE".equals(action.action)) {
                        targetRef.removeValue().addOnSuccessListener(
                                aVoid -> executorService.execute(() -> database.syncActionDao().delete(action))
                        );
                    } else {
                        // CREATE or UPDATE
                        if ("work_program".equals(action.entityType)) {
                            WorkProgramEntity entity = database.workProgramDao().getById(action.entityId);
                            if (entity != null) {
                                java.util.Map<String, Object> data = serializeWorkProgram(entity);
                                targetRef.setValue(data).addOnSuccessListener(
                                        aVoid -> executorService.execute(() -> database.syncActionDao().delete(action))
                                );
                            } else {
                                // Entity no longer exists locally, remove from queue
                                executorService.execute(() -> database.syncActionDao().delete(action));
                            }
                        } else if ("location".equals(action.entityType)) {
                            UserLocationEntity entity = database.userLocationDao().getById(action.entityId);
                            if (entity != null) {
                                targetRef.setValue(serializeUserLocation(entity)).addOnSuccessListener(
                                        aVoid -> executorService.execute(() -> database.syncActionDao().delete(action))
                                );
                            } else {
                                executorService.execute(() -> database.syncActionDao().delete(action));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing sync queue", e);
            }
        });
    }

    private java.util.Map<String, Object> serializeWorkProgram(WorkProgramEntity entity) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("cultivarName", entity.cultivarName);
        data.put("startingDate", entity.startingDate);
        data.put("areaSize", entity.areaSize);
        data.put("projectedIncome", entity.projectedIncome);
        data.put("projectedExpenses", entity.projectedExpenses);
        data.put("adjustedIncome", entity.adjustedIncome);
        data.put("adjustedExpenses", entity.adjustedExpenses);
        data.put("lastUpdated", entity.lastUpdated);
        data.put("season", entity.season);
        data.put("seasonMonth", entity.seasonMonth);
        data.put("isOffSeason", entity.isOffSeason);
        data.put("actualYield", entity.actualYield);
        data.put("totalYield", entity.totalYield);
        if (entity.harvestDate != null) data.put("harvestDate", entity.harvestDate);
        return data;
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
    @SuppressWarnings("unused")
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
                        String.format(Locale.getDefault(), "%02d:%02d", SettingsPreferences.getNotificationHour(context), SettingsPreferences.getNotificationMinute(context)),
                        SettingsPreferences.isQuietHoursEnabled(context),
                        String.format(Locale.getDefault(), "%02d:%02d", SettingsPreferences.getQuietHoursStartHour(context), SettingsPreferences.getQuietHoursStartMinute(context)),
                        String.format(Locale.getDefault(), "%02d:%02d", SettingsPreferences.getQuietHoursEndHour(context), SettingsPreferences.getQuietHoursEndMinute(context)),
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
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public List<com.android.tomatoapp.common.models.UserLocationEntity> getUserLocationsFromLocal(String userId) {
        try {
            return database.userLocationDao().getAllForUser(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error reading locations from local database", e);
            return new ArrayList<>();
        }
    }

    public void saveUserLocationToLocal(com.android.tomatoapp.common.models.UserLocationEntity entity, String actionType) {
        executorService.execute(() -> {
            try {
                entity.lastUpdated = System.currentTimeMillis();
                database.userLocationDao().upsert(entity);
                if (entity.isDefault) {
                    database.userLocationDao().clearOtherDefaults(entity.userId, entity.id);
                }
                
                // Add to sync queue
                SyncActionEntity syncAction = new SyncActionEntity(
                    entity.userId, "location", entity.id, actionType, null);
                database.syncActionDao().insert(syncAction);
                Log.d(TAG, "Saved location " + entity.id + " and queued " + actionType);
            } catch (Exception e) {
                Log.e(TAG, "Error saving location to local", e);
            }
        });
    }

    @SuppressWarnings("unused")
    public void deleteUserLocationLocally(String userId, String locationId) {
        executorService.execute(() -> {
            try {
                database.userLocationDao().deleteById(locationId);
                // Add to sync queue
                SyncActionEntity syncAction = new SyncActionEntity(
                    userId, "location", locationId, "DELETE", null);
                database.syncActionDao().insert(syncAction);
                Log.d(TAG, "Deleted location " + locationId + " locally and queued DELETE");
            } catch (Exception e) {
                Log.e(TAG, "Error deleting location locally", e);
            }
        });
    }

    private java.util.Map<String, Object> serializeUserLocation(com.android.tomatoapp.common.models.UserLocationEntity entity) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", entity.id);
        data.put("province", entity.province);
        data.put("city", entity.city);
        data.put("brgy", entity.brgy);
        data.put("lat", entity.lat);
        data.put("lon", entity.lon);
        data.put("isDefault", entity.isDefault);
        data.put("lastUpdated", entity.lastUpdated);
        return data;
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
            android.net.Network activeNetwork = cm.getActiveNetwork();
            if (activeNetwork == null) return false;

            android.net.NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
            return capabilities != null
                    && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            return false;
        }
    }

    // Save Work Program to Local Database and Queue for Sync
    public void saveWorkProgramToLocal(WorkProgramEntity entity, String actionType) {
        executorService.execute(() -> {
            try {
                entity.lastUpdated = System.currentTimeMillis();
                database.workProgramDao().upsert(entity);
                
                // Add to sync queue
                SyncActionEntity syncAction = new SyncActionEntity(
                    entity.userId, "work_program", entity.id, actionType, null);
                database.syncActionDao().insert(syncAction);
                
                Log.d(TAG, "Saved work program " + entity.id + " and queued " + actionType);
            } catch (Exception e) {
                Log.e(TAG, "Error saving work program to local", e);
            }
        });
    }

    public void deleteWorkProgramLocally(String userId, String programId) {
        executorService.execute(() -> {
            try {
                database.workProgramDao().deleteById(programId);
                // Add to sync queue
                SyncActionEntity syncAction = new SyncActionEntity(
                    userId, "work_program", programId, "DELETE", null);
                database.syncActionDao().insert(syncAction);
                Log.d(TAG, "Deleted work program " + programId + " locally and queued DELETE");
            } catch (Exception e) {
                Log.e(TAG, "Error deleting work program locally", e);
            }
        });
    }

    // Helper method to parse WorkProgram from Firebase snapshot
    private WorkProgramEntity parseWorkProgramFromSnapshot(DataSnapshot snapshot, String programId, String userId) {
        try {
            String cultivarName = snapshot.child("cultivarName").getValue(String.class);
            if (cultivarName == null) {
                cultivarName = snapshot.child("cultivar").getValue(String.class);
            }
            String startingDate = snapshot.child("startingDate").getValue(String.class);
            if (startingDate == null) {
                startingDate = snapshot.child("startDate").getValue(String.class);
            }

            // Safely parse numeric fields
            double areaSize = 0;
            Object areaSizeObj = snapshot.child("areaSize").getValue();
            if (areaSizeObj != null) {
                if (areaSizeObj instanceof Double) {
                    areaSize = (Double) areaSizeObj;
                } else if (areaSizeObj instanceof Long) {
                    areaSize = ((Long) areaSizeObj).doubleValue();
                } else if (areaSizeObj instanceof String) {
                    try {
                        areaSize = Double.parseDouble((String) areaSizeObj);
                    } catch (NumberFormatException ignored) { }
                }
            } else {
                // Fallback to legacy landArea
                Object legacyAreaValue = snapshot.child("landArea").getValue();
                if (legacyAreaValue instanceof Double) {
                    areaSize = (Double) legacyAreaValue;
                } else if (legacyAreaValue instanceof Long) {
                    areaSize = ((Long) legacyAreaValue).doubleValue();
                } else if (legacyAreaValue instanceof String) {
                    try {
                        areaSize = Double.parseDouble((String) legacyAreaValue);
                    } catch (NumberFormatException ignored) { }
                }
            }

            double projectedIncome = 0;
            Object incomeValue = snapshot.child("projectedIncome").getValue();
            if (incomeValue instanceof Double) {
                projectedIncome = (Double) incomeValue;
            } else if (incomeValue instanceof Long) {
                projectedIncome = ((Long) incomeValue).doubleValue();
            } else if (incomeValue instanceof String) {
                try {
                    projectedIncome = Double.parseDouble((String) incomeValue);
                } catch (NumberFormatException ignored) { }
            }

            double projectedExpenses = 0;
            Object expensesValue = snapshot.child("projectedExpenses").getValue();
            if (expensesValue instanceof Double) {
                projectedExpenses = (Double) expensesValue;
            } else if (expensesValue instanceof Long) {
                projectedExpenses = ((Long) expensesValue).doubleValue();
            } else if (expensesValue instanceof String) {
                try {
                    projectedExpenses = Double.parseDouble((String) expensesValue);
                } catch (NumberFormatException ignored) { }
            }

            double adjustedIncome = 0;
            Object adjustedIncomeValue = snapshot.child("adjustedIncome").getValue();
            if (adjustedIncomeValue instanceof Double) {
                adjustedIncome = (Double) adjustedIncomeValue;
            } else if (adjustedIncomeValue instanceof Long) {
                adjustedIncome = ((Long) adjustedIncomeValue).doubleValue();
            }

            double adjustedExpenses = 0;
            Object adjustedExpensesValue = snapshot.child("adjustedExpenses").getValue();
            if (adjustedExpensesValue instanceof Double) {
                adjustedExpenses = (Double) adjustedExpensesValue;
            } else if (adjustedExpensesValue instanceof Long) {
                adjustedExpenses = ((Long) adjustedExpensesValue).doubleValue();
            }

            // Calculate phases JSON if we have cultivar and start date
            String phasesJson = null;
            if (cultivarName != null && startingDate != null) {
                phasesJson = WorkProgramDataHelper.calculatePhasesJson(cultivarName, startingDate);
            }

            // Parse research fields
            String season = snapshot.child("season").getValue(String.class);
            Integer seasonMonthObj = snapshot.child("seasonMonth").getValue(Integer.class);
            Boolean isOffSeasonObj = snapshot.child("isOffSeason").getValue(Boolean.class);
            Double actualYieldObj = snapshot.child("actualYield").getValue(Double.class);
            Double totalYieldObj = snapshot.child("totalYield").getValue(Double.class);
            String harvestDate = snapshot.child("harvestDate").getValue(String.class);
            Long lastUpdated = snapshot.child("lastUpdated").getValue(Long.class);

            // Auto-detect season if not set
            if (season == null && startingDate != null) {
                season = SeasonHelper.getSeason(startingDate);
            }
            int seasonMonth = seasonMonthObj != null ? seasonMonthObj :
                             (startingDate != null ? SeasonHelper.getSeasonMonth(startingDate) : 0);
            boolean isOffSeason = isOffSeasonObj != null
                    ? isOffSeasonObj
                    : (startingDate != null && SeasonHelper.isOffSeason(startingDate));
            double actualYield = actualYieldObj != null ? actualYieldObj : 0.0;
            double totalYield = totalYieldObj != null ? totalYieldObj : 0.0;

            // Create entity with all fields
            WorkProgramEntity entity = new WorkProgramEntity(
                    programId,
                    userId,
                    cultivarName != null ? cultivarName : "",
                    areaSize,
                    startingDate != null ? startingDate : "",
                    phasesJson != null ? phasesJson : "",
                    null, // detectionHistoriesJson - loaded on-demand
                    projectedIncome,
                    projectedExpenses,
                    adjustedIncome,
                    adjustedExpenses,
                    0.0, 0.0, 0.0, 0.0, 0.0, // phase completions
                    0, 0, 0, 0, 0.0, // task metrics
                    season != null ? season : (startingDate != null ? SeasonHelper.getSeason(startingDate) : "unknown"),
                    seasonMonth,
                    isOffSeason,
                    actualYield,
                    totalYield,
                    harvestDate
            );
            if (lastUpdated != null) entity.lastUpdated = lastUpdated;
            return entity;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing work program", e);
            return null;
        }
    }
}
