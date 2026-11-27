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
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

public class LocalDataManager {
    private static final String TAG = "LocalDataManager";
    private static LocalDataManager instance;
    private final AppDatabase database;
    private final ExecutorService executorService;
    private final Context context;
    private final AtomicBoolean syncQueueRunning = new AtomicBoolean(false);

    private LocalDataManager(Context context) {
        this.context = context.getApplicationContext();
        database = AppDatabase.getInstance(this.context);
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
                                    if (database.syncActionDao().countForEntity(programId, "work_program") > 0) {
                                        continue;
                                    }

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
        if (!syncQueueRunning.compareAndSet(false, true)) return;
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
                    } else if ("task_meta".equals(action.entityType)) {
                        targetRef = taskMetaRef(rootRef, action.entityId);
                    }

                    if (targetRef == null) continue;
                    try {
                        if ("DELETE".equals(action.action)) {
                            Tasks.await(targetRef.removeValue());
                        } else if ("work_program".equals(action.entityType)) {
                            WorkProgramEntity entity = database.workProgramDao().getById(action.entityId);
                            if (entity == null) {
                                database.syncActionDao().delete(action);
                                continue;
                            }
                            java.util.Map<String, Object> data;
                            if ("UPDATE".equals(action.action) && action.dataJson != null) {
                                data = jsonObjectToMap(action.dataJson);
                            } else {
                                data = serializeWorkProgram(entity);
                            }
                            Tasks.await(targetRef.updateChildren(data));
                        } else if ("location".equals(action.entityType)) {
                            UserLocationEntity entity = database.userLocationDao().getById(action.entityId);
                            if (entity == null) {
                                database.syncActionDao().delete(action);
                                continue;
                            }
                            Tasks.await(targetRef.setValue(serializeUserLocation(entity)));
                        } else if ("task_meta".equals(action.entityType)) {
                            java.util.Map<String, Object> data = action.dataJson != null
                                    ? jsonObjectToMap(action.dataJson)
                                    : null;
                            if (data == null || data.isEmpty()) {
                                data = taskMetaPayloadFromRoom(action.userId, action.entityId);
                            }
                            if (data == null || data.isEmpty()) {
                                continue;
                            }
                            Tasks.await(targetRef.updateChildren(data));
                        }
                        database.syncActionDao().delete(action);
                    } catch (Exception e) {
                        Log.e(TAG, "Sync queue action failed: " + action.action + " " + action.entityId, e);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing sync queue", e);
            } finally {
                syncQueueRunning.set(false);
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
        data.put("completionRate", entity.completionRate);
        data.put("phase1Completion", entity.phase1Completion);
        data.put("phase2Completion", entity.phase2Completion);
        data.put("phase3Completion", entity.phase3Completion);
        data.put("phase4Completion", entity.phase4Completion);
        data.put("phase5Completion", entity.phase5Completion);
        if (entity.harvestDate != null) data.put("harvestDate", entity.harvestDate);
        if (entity.plantCount != 0) data.put("plantCount", entity.plantCount);
        if (entity.seedsConfirmedDate != null && !entity.seedsConfirmedDate.isEmpty()) data.put("seedsConfirmedDate", entity.seedsConfirmedDate);
        if (entity.sowingDate != null && !entity.sowingDate.isEmpty()) data.put("sowingDate", entity.sowingDate);
        if (entity.transplantDate != null && !entity.transplantDate.isEmpty()) data.put("transplantDate", entity.transplantDate);
        if (entity.stopDate != null && !entity.stopDate.isEmpty()) data.put("stopDate", entity.stopDate);
        if (entity.phase3Date != null && !entity.phase3Date.isEmpty()) data.put("phase3Date", entity.phase3Date);
        if (entity.phase4Date != null && !entity.phase4Date.isEmpty()) data.put("phase4Date", entity.phase4Date);
        if (entity.phase5Date != null && !entity.phase5Date.isEmpty()) data.put("phase5Date", entity.phase5Date);
        if (entity.loggedExpensesTotal != 0) data.put("loggedExpensesTotal", entity.loggedExpensesTotal);
        if (entity.awf != 0) data.put("farmMetrics/awf", entity.awf);
        if (entity.afp != 0) data.put("farmMetrics/afp", entity.afp);
        if (entity.marketValue != 0) data.put("farmMetrics/marketValue", entity.marketValue);
        if (entity.financeLossPct != 0) data.put("financeLossPct", entity.financeLossPct);
        if (entity.phaseBudget1 != 0) data.put("phaseBudget1", entity.phaseBudget1);
        if (entity.phaseBudget2 != 0) data.put("phaseBudget2", entity.phaseBudget2);
        if (entity.phaseBudget3 != 0) data.put("phaseBudget3", entity.phaseBudget3);
        if (entity.phaseBudget4 != 0) data.put("phaseBudget4", entity.phaseBudget4);
        if (entity.phaseBudget5 != 0) data.put("phaseBudget5", entity.phaseBudget5);
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
                ArrayList<JSONObject> historyList = DetectionHistoryManager.getPrefsHistory(context);
                for (JSONObject entry : historyList) {
                    try {
                        String id = entry.optString("imageUri", "") + "_" + entry.optLong("timestamp", 0);
                        DetectionHistoryEntity entity = new DetectionHistoryEntity(
                                id,
                                userId,
                                entry.optString("programId", ""),
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
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId);
                DataSnapshot logsSnapshot = Tasks.await(
                        userRef.child("workPrograms").child(programId).child("logs").get());
                DataSnapshot metaSnapshot = Tasks.await(
                        userRef.child("routineLogs").child(programId).child("taskMeta").get());

                java.util.Set<String> dateKeys = new java.util.LinkedHashSet<>();
                for (DataSnapshot child : logsSnapshot.getChildren()) {
                    if (child.getKey() != null) dateKeys.add(child.getKey());
                }
                for (DataSnapshot child : metaSnapshot.getChildren()) {
                    if (child.getKey() != null) dateKeys.add(child.getKey());
                }

                int updatedCount = 0;
                for (String dateKey : dateKeys) {
                    if (database.syncActionDao().countForEntity(programId + "|" + dateKey, "task_meta") > 0) {
                        continue;
                    }
                    String status = null;
                    DataSnapshot logChild = logsSnapshot.child(dateKey);
                    if (logChild.exists()) {
                        status = logChild.getValue(String.class);
                    }
                    DataSnapshot metaChild = metaSnapshot.child(dateKey);
                    if (mergeRemoteTask(userId, programId, dateKey, status, metaChild)) {
                        updatedCount++;
                    }
                }
                Log.d(TAG, "Synced " + updatedCount + " tasks for program " + programId);
            } catch (Exception e) {
                Log.e(TAG, "Error syncing tasks", e);
            }
        });
    }

    private boolean mergeRemoteTask(String userId, String programId, String dateKey,
                                    String remoteStatus, DataSnapshot meta) {
        boolean hasMeta = meta != null && meta.exists();
        if (remoteStatus == null && !hasMeta) return false;

        TaskEntity existing = database.taskDao().getByDate(userId, programId, dateKey);
        if (existing == null) {
            TaskEntity entity = new TaskEntity(
                    programId + "_" + dateKey,
                    userId,
                    programId,
                    dateKey,
                    "",
                    "",
                    "",
                    0,
                    "",
                    remoteStatus != null ? remoteStatus : "pending",
                    System.currentTimeMillis()
            );
            if (hasMeta) {
                entity.skipReason = snapshotString(meta, "reason");
                entity.skipNote = snapshotString(meta, "note");
                entity.weatherNote = snapshotString(meta, "weatherNote");
                entity.late = snapshotBoolean(meta, "late");
                entity.scanLinked = snapshotBoolean(meta, "scanLinked");
            }
            database.taskDao().insert(entity);
            return true;
        }

        if (remoteStatus != null) {
            database.taskDao().updateStatus(existing.id, remoteStatus);
        }
        if (hasMeta) {
            database.taskDao().updateMeta(
                    existing.id,
                    snapshotString(meta, "reason"),
                    snapshotString(meta, "note"),
                    snapshotString(meta, "weatherNote"),
                    snapshotBoolean(meta, "late"),
                    snapshotBoolean(meta, "scanLinked"));
        }
        return true;
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
                TaskEntity existing = database.taskDao().getByDate(userId, programId, dateKey);
                if (existing != null) {
                    entity.skipReason = existing.skipReason;
                    entity.skipNote = existing.skipNote;
                    entity.weatherNote = existing.weatherNote;
                    entity.late = existing.late;
                    entity.scanLinked = existing.scanLinked;
                }
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
                obj.put("programId", det.programId);
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
                settingsObj.put("weatherUnit", settings.weatherUnit);
                settingsObj.put("measurementUnit", settings.measurementUnit);
                settingsObj.put("dateFormat", settings.dateFormat);
                settingsObj.put("taskNotifications", settings.taskNotifications);
                settingsObj.put("monitoringNotifications", settings.monitoringNotifications);
                settingsObj.put("generalNotifications", settings.generalNotifications);
                settingsObj.put("notificationSound", settings.notificationSound);
                settingsObj.put("notificationTime", settings.notificationTime);
                settingsObj.put("quietHoursEnabled", settings.quietHoursEnabled);
                settingsObj.put("quietHoursStart", settings.quietHoursStart);
                settingsObj.put("quietHoursEnd", settings.quietHoursEnd);
                exportData.put("settings", settingsObj);
            }

            return exportData.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error exporting data", e);
            return null;
        }
    }

    public boolean importAllData(Context context, String userId, String jsonData) {
        if (context == null || userId == null || userId.isEmpty() || jsonData == null || jsonData.isEmpty()) {
            return false;
        }

        try {
            JSONObject root = new JSONObject(jsonData);

            database.calculationDao().deleteAllByUser(userId);
            database.detectionHistoryDao().deleteAllByUser(userId);
            SettingsEntity currentSettings = database.settingsDao().getByUser(userId);
            if (currentSettings != null) {
                database.settingsDao().delete(currentSettings);
            }

            JSONArray calculations = root.optJSONArray("calculations");
            if (calculations != null) {
                for (int i = 0; i < calculations.length(); i++) {
                    JSONObject obj = calculations.getJSONObject(i);
                    CalculationEntity entity = new CalculationEntity(
                            obj.optString("id", ""),
                            userId,
                            obj.optString("programId", null),
                            obj.optDouble("grossIncome", 0),
                            obj.optDouble("totalExpenses", 0),
                            obj.optDouble("netIncome", 0),
                            obj.optDouble("hectare", 0),
                            obj.optString("dateCreated", null),
                            obj.optString("dateSaved", null),
                            obj.optString("cultivarName", null),
                            System.currentTimeMillis()
                    );
                    if (!entity.id.isEmpty()) {
                        database.calculationDao().insert(entity);
                    }
                }
            }

            JSONArray detections = root.optJSONArray("detectionHistory");
            if (detections != null) {
                for (int i = 0; i < detections.length(); i++) {
                    JSONObject obj = detections.getJSONObject(i);
                    DetectionHistoryEntity entity = new DetectionHistoryEntity(
                            obj.optString("id", ""),
                            userId,
                            obj.optString("programId", null),
                            obj.optString("imageUri", null),
                            obj.optString("disease", null),
                            obj.optString("accuracy", null),
                            obj.optString("description", null),
                            obj.optString("symptoms", null),
                            obj.optString("cause", null),
                            obj.optString("cure", null),
                            obj.optString("prevention", null),
                            obj.optString("pestTitle", null),
                            obj.optString("pestDescription", null),
                            obj.optString("pestImageUri", null),
                            obj.optLong("timestamp", System.currentTimeMillis()),
                            obj.optString("cultivar", null),
                            obj.optInt("phase", 0),
                            System.currentTimeMillis()
                    );
                    if (!entity.id.isEmpty()) {
                        database.detectionHistoryDao().insert(entity);
                    }
                }
            }

            JSONObject settingsObj = root.optJSONObject("settings");
            if (settingsObj != null) {
                SettingsEntity settings = new SettingsEntity(
                        userId,
                        settingsObj.optString("language", null),
                        settingsObj.optString("theme", null),
                        settingsObj.optString("defaultCultivar", null),
                        settingsObj.optString("weatherUnit", null),
                        settingsObj.optString("measurementUnit", null),
                        settingsObj.optString("dateFormat", null),
                        settingsObj.optBoolean("taskNotifications", true),
                        settingsObj.optBoolean("monitoringNotifications", true),
                        settingsObj.optBoolean("generalNotifications", true),
                        settingsObj.optString("notificationSound", null),
                        settingsObj.optString("notificationTime", null),
                        settingsObj.optBoolean("quietHoursEnabled", false),
                        settingsObj.optString("quietHoursStart", null),
                        settingsObj.optString("quietHoursEnd", null),
                        System.currentTimeMillis()
                );
                database.settingsDao().insert(settings);

                if (settings.language != null) SettingsPreferences.setLanguage(context, settings.language);
                if (settings.theme != null) SettingsPreferences.setTheme(context, settings.theme);
                if (settings.defaultCultivar != null) SettingsPreferences.setDefaultCultivar(context, settings.defaultCultivar);
                if (settings.weatherUnit != null) SettingsPreferences.setWeatherUnit(context, settings.weatherUnit);
                if (settings.measurementUnit != null) SettingsPreferences.setMeasurementUnit(context, settings.measurementUnit);
                if (settings.dateFormat != null) SettingsPreferences.setDateFormat(context, settings.dateFormat);
                if (settings.notificationSound != null) SettingsPreferences.setNotificationSound(context, settings.notificationSound);
                if (settings.notificationTime != null && settings.notificationTime.contains(":")) {
                    String[] parts = settings.notificationTime.split(":");
                    if (parts.length == 2) {
                        try {
                            SettingsPreferences.setNotificationHour(context, Integer.parseInt(parts[0]));
                            SettingsPreferences.setNotificationMinute(context, Integer.parseInt(parts[1]));
                        } catch (NumberFormatException ignored) { }
                    }
                }
                SettingsPreferences.setQuietHoursEnabled(context, settings.quietHoursEnabled);
                if (settings.quietHoursStart != null && settings.quietHoursStart.contains(":")) {
                    String[] parts = settings.quietHoursStart.split(":");
                    if (parts.length == 2) {
                        try {
                            SettingsPreferences.setQuietHoursStartHour(context, Integer.parseInt(parts[0]));
                            SettingsPreferences.setQuietHoursStartMinute(context, Integer.parseInt(parts[1]));
                        } catch (NumberFormatException ignored) { }
                    }
                }
                if (settings.quietHoursEnd != null && settings.quietHoursEnd.contains(":")) {
                    String[] parts = settings.quietHoursEnd.split(":");
                    if (parts.length == 2) {
                        try {
                            SettingsPreferences.setQuietHoursEndHour(context, Integer.parseInt(parts[0]));
                            SettingsPreferences.setQuietHoursEndMinute(context, Integer.parseInt(parts[1]));
                        } catch (NumberFormatException ignored) { }
                    }
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error importing data", e);
            return false;
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

    public List<TaskEntity> getTaskMetaFromLocal(String userId, String programId) {
        return getTasksFromLocal(userId, programId);
    }

    public void saveTaskMeta(String userId, String programId, String dateKey,
                             String reason, String note, boolean late,
                             String weatherNote, boolean scanLinked) {
        executorService.execute(() -> {
            try {
                String taskId = programId + "_" + dateKey;
                String entityId = programId + "|" + dateKey;
                long updatedAt = System.currentTimeMillis();
                TaskEntity existing = database.taskDao().getByDate(userId, programId, dateKey);
                if (existing == null) {
                    TaskEntity entity = new TaskEntity(
                            taskId,
                            userId,
                            programId,
                            dateKey,
                            "",
                            "",
                            "",
                            0,
                            "",
                            "pending",
                            updatedAt
                    );
                    entity.skipReason = reason;
                    entity.skipNote = note;
                    entity.weatherNote = weatherNote;
                    entity.late = late;
                    entity.scanLinked = scanLinked;
                    database.taskDao().insert(entity);
                } else {
                    database.taskDao().updateMeta(
                            existing.id, reason, note, weatherNote, late, scanLinked);
                }

                java.util.Map<String, Object> data = taskMetaChildren(
                        reason, note, late, weatherNote, scanLinked, updatedAt);
                if (isOnline(context)) {
                    try {
                        Tasks.await(FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(userId)
                                .child("routineLogs")
                                .child(programId)
                                .child("taskMeta")
                                .child(dateKey)
                                .updateChildren(data));
                        database.syncActionDao().deleteForEntity(entityId, "task_meta");
                        return;
                    } catch (Exception e) {
                        Log.e(TAG, "Task meta upload failed, queued", e);
                    }
                }
                queueTaskMeta(userId, entityId, data);
            } catch (Exception e) {
                Log.e(TAG, "Error saving task meta", e);
            }
        });
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

                java.util.List<SyncActionEntity> existing =
                        database.syncActionDao().getForEntity(entity.id, "work_program");
                if ("DELETE".equals(actionType)) {
                    database.syncActionDao().deleteForEntity(entity.id, "work_program");
                    database.syncActionDao().insert(new SyncActionEntity(
                            entity.userId, "work_program", entity.id, "DELETE", null));
                } else if ("CREATE".equals(actionType)) {
                    for (SyncActionEntity action : existing) {
                        if ("CREATE".equals(action.action)) {
                            return;
                        }
                    }
                    database.syncActionDao().deleteForEntity(entity.id, "work_program");
                    database.syncActionDao().insert(new SyncActionEntity(
                            entity.userId, "work_program", entity.id, "CREATE", null));
                } else {
                    for (SyncActionEntity action : existing) {
                        if ("DELETE".equals(action.action) || "CREATE".equals(action.action)) {
                            return;
                        }
                    }
                    JSONObject merged = new JSONObject();
                    for (SyncActionEntity action : existing) {
                        if ("UPDATE".equals(action.action) && action.dataJson != null) {
                            JSONObject prior = new JSONObject(action.dataJson);
                            java.util.Iterator<String> keys = prior.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                merged.put(key, prior.get(key));
                            }
                        }
                    }
                    java.util.Map<String, Object> full = serializeWorkProgram(entity);
                    for (java.util.Map.Entry<String, Object> entry : full.entrySet()) {
                        merged.put(entry.getKey(), entry.getValue());
                    }
                    merged.put("lastUpdated", entity.lastUpdated);
                    database.syncActionDao().deleteForEntity(entity.id, "work_program");
                    database.syncActionDao().insert(new SyncActionEntity(
                            entity.userId, "work_program", entity.id, "UPDATE", merged.toString()));
                }
                
                Log.d(TAG, "Saved work program " + entity.id + " and queued " + actionType);
            } catch (Exception e) {
                Log.e(TAG, "Error saving work program to local", e);
            }
        });
    }

    public Task<Void> updateProgramFields(String userId, String programId, java.util.Map<String, Object> fields) {
        java.util.Map<String, Object> firebaseFields = new java.util.HashMap<>();
        firebaseFields.putAll(fields);
        long lastUpdated = System.currentTimeMillis();
        firebaseFields.put("lastUpdated", lastUpdated);
        TaskCompletionSource<Void> result = new TaskCompletionSource<>();

        executorService.execute(() -> {
            try {
                WorkProgramEntity entity = database.workProgramDao().getById(programId);
                if (entity != null) {
                    for (java.util.Map.Entry<String, Object> entry : fields.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        switch (key) {
                        case "plantCount":
                            entity.plantCount = numberAsInt(value);
                            break;
                        case "seedsConfirmedDate": entity.seedsConfirmedDate = stringValue(value); break;
                        case "sowingDate": entity.sowingDate = stringValue(value); break;
                        case "transplantDate": entity.transplantDate = stringValue(value); break;
                        case "stopDate": entity.stopDate = stringValue(value); break;
                        case "phase3Date": entity.phase3Date = stringValue(value); break;
                        case "phase4Date": entity.phase4Date = stringValue(value); break;
                        case "phase5Date": entity.phase5Date = stringValue(value); break;
                        case "loggedExpensesTotal": entity.loggedExpensesTotal = numberAsDouble(value); break;
                        case "farmMetrics/awf":
                        case "awf": entity.awf = numberAsDouble(value); break;
                        case "farmMetrics/afp":
                        case "afp": entity.afp = numberAsDouble(value); break;
                        case "farmMetrics/marketValue":
                        case "marketValue": entity.marketValue = numberAsDouble(value); break;
                        case "financeLossPct": entity.financeLossPct = numberAsDouble(value); break;
                        case "phaseBudget1": entity.phaseBudget1 = numberAsDouble(value); break;
                        case "phaseBudget2": entity.phaseBudget2 = numberAsDouble(value); break;
                        case "phaseBudget3": entity.phaseBudget3 = numberAsDouble(value); break;
                        case "phaseBudget4": entity.phaseBudget4 = numberAsDouble(value); break;
                        case "phaseBudget5": entity.phaseBudget5 = numberAsDouble(value); break;
                        case "cultivarName": entity.cultivarName = stringValue(value); break;
                        case "startingDate": entity.startingDate = stringValue(value); break;
                        case "areaSize": entity.areaSize = numberAsDouble(value); break;
                        case "projectedIncome": entity.projectedIncome = numberAsDouble(value); break;
                        case "projectedExpenses": entity.projectedExpenses = numberAsDouble(value); break;
                        case "adjustedIncome": entity.adjustedIncome = numberAsDouble(value); break;
                        case "adjustedExpenses": entity.adjustedExpenses = numberAsDouble(value); break;
                        case "completionRate": entity.completionRate = numberAsDouble(value); break;
                        case "phase1Completion": entity.phase1Completion = numberAsDouble(value); break;
                        case "phase2Completion": entity.phase2Completion = numberAsDouble(value); break;
                        case "phase3Completion": entity.phase3Completion = numberAsDouble(value); break;
                        case "phase4Completion": entity.phase4Completion = numberAsDouble(value); break;
                        case "phase5Completion": entity.phase5Completion = numberAsDouble(value); break;
                        case "season": entity.season = stringValue(value); break;
                        case "seasonMonth": entity.seasonMonth = numberAsInt(value); break;
                        case "isOffSeason": entity.isOffSeason = booleanValue(value); break;
                        case "actualYield": entity.actualYield = numberAsDouble(value); break;
                        case "totalYield": entity.totalYield = numberAsDouble(value); break;
                        case "harvestDate": entity.harvestDate = stringValue(value); break;
                            default: break;
                        }
                    }
                    entity.lastUpdated = lastUpdated;
                    database.workProgramDao().upsert(entity);

                    if (!isOnline(context)) {
                        java.util.List<SyncActionEntity> existing =
                                database.syncActionDao().getForEntity(programId, "work_program");
                        JSONObject merged = new JSONObject();
                        boolean protectedAction = false;
                        for (SyncActionEntity action : existing) {
                            if ("CREATE".equals(action.action) || "DELETE".equals(action.action)) {
                                protectedAction = true;
                                break;
                            }
                            if ("UPDATE".equals(action.action) && action.dataJson != null) {
                                JSONObject prior = new JSONObject(action.dataJson);
                                java.util.Iterator<String> keys = prior.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    merged.put(key, prior.get(key));
                                }
                            }
                        }
                        if (!protectedAction) {
                            for (java.util.Map.Entry<String, Object> entry : firebaseFields.entrySet()) {
                                merged.put(entry.getKey(), entry.getValue());
                            }
                            database.syncActionDao().deleteForEntity(programId, "work_program");
                            database.syncActionDao().insert(new SyncActionEntity(
                                    userId, "work_program", programId, "UPDATE", merged.toString()));
                        }
                    }
                }

                Task<Void> firebaseTask = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("workPrograms")
                        .child(programId)
                        .updateChildren(firebaseFields);
                firebaseTask.addOnSuccessListener(aVoid -> result.setResult(null));
                firebaseTask.addOnFailureListener(result::setException);
            } catch (Exception e) {
                Log.e(TAG, "Error updating work program fields", e);
                result.setException(e);
            }
        });

        return result.getTask();
    }

    public void deleteWorkProgramLocally(String userId, String programId) {
        executorService.execute(() -> {
            try {
                database.workProgramDao().deleteById(programId);
                database.taskDao().deleteByProgram(userId, programId);
                // Add to sync queue
                database.syncActionDao().deleteForEntity(programId, "work_program");
                SyncActionEntity syncAction = new SyncActionEntity(
                    userId, "work_program", programId, "DELETE", null);
                database.syncActionDao().insert(syncAction);
                Log.d(TAG, "Deleted work program " + programId + " locally and queued DELETE");
            } catch (Exception e) {
                Log.e(TAG, "Error deleting work program locally", e);
            }
        });
    }

    public void updateCompletionRate(String programId, double rate) {
        executorService.execute(() -> {
            try {
                database.workProgramDao().updateCompletionRate(programId, rate);
                Log.d(TAG, "Updated completion rate for " + programId + " to " + rate);
            } catch (Exception e) {
                Log.e(TAG, "Error updating completion rate locally", e);
            }
        });
    }

    private static double numberAsDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value != null) {
            try { return Double.parseDouble(value.toString()); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private static int numberAsInt(Object value) {
        return (int) numberAsDouble(value);
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean booleanValue(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value != null) return Boolean.parseBoolean(value.toString());
        return false;
    }

    private static double snapshotDouble(DataSnapshot snapshot, String key) {
        return numberAsDouble(snapshot.child(key).getValue());
    }

    private static int snapshotInt(DataSnapshot snapshot, String key) {
        return numberAsInt(snapshot.child(key).getValue());
    }

    private static long snapshotLong(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        if (value instanceof Number) return ((Number) value).longValue();
        if (value != null) {
            try { return Long.parseLong(value.toString()); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private static String snapshotString(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value == null ? null : value.toString();
    }

    private static boolean snapshotBoolean(DataSnapshot snapshot, String key) {
        return booleanValue(snapshot.child(key).getValue());
    }

    private DatabaseReference taskMetaRef(DatabaseReference userRef, String entityId) {
        if (userRef == null || entityId == null) return null;
        int sep = entityId.indexOf('|');
        if (sep <= 0 || sep >= entityId.length() - 1) return null;
        return userRef.child("routineLogs")
                .child(entityId.substring(0, sep))
                .child("taskMeta")
                .child(entityId.substring(sep + 1));
    }

    private java.util.Map<String, Object> taskMetaChildren(String reason, String note, boolean late,
                                                           String weatherNote, boolean scanLinked,
                                                           long updatedAt) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("reason", reason == null ? "" : reason);
        data.put("note", note == null ? "" : note);
        data.put("late", late);
        data.put("weatherNote", weatherNote == null ? "" : weatherNote);
        data.put("scanLinked", scanLinked);
        data.put("updatedAt", updatedAt);
        return data;
    }

    private void queueTaskMeta(String userId, String entityId, java.util.Map<String, Object> data) throws JSONException {
        JSONObject payload = new JSONObject();
        for (java.util.Map.Entry<String, Object> entry : data.entrySet()) {
            payload.put(entry.getKey(), entry.getValue());
        }
        database.syncActionDao().deleteForEntity(entityId, "task_meta");
        database.syncActionDao().insert(new SyncActionEntity(
                userId, "task_meta", entityId, "UPDATE", payload.toString()));
    }

    private java.util.Map<String, Object> taskMetaPayloadFromRoom(String userId, String entityId) {
        if (entityId == null) return null;
        int sep = entityId.indexOf('|');
        if (sep <= 0 || sep >= entityId.length() - 1) return null;
        String programId = entityId.substring(0, sep);
        String dateKey = entityId.substring(sep + 1);
        TaskEntity entity = database.taskDao().getByDate(userId, programId, dateKey);
        if (entity == null) return null;
        return taskMetaChildren(
                entity.skipReason,
                entity.skipNote,
                entity.late,
                entity.weatherNote,
                entity.scanLinked,
                entity.lastSynced);
    }

    private java.util.Map<String, Object> jsonObjectToMap(String json) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            JSONObject object = new JSONObject(json);
            java.util.Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                result.put(key, object.get(key));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing queued work program fields", e);
        }
        return result;
    }

    // Helper method to parse WorkProgram from Firebase snapshot
    private WorkProgramEntity parseWorkProgramFromSnapshot(DataSnapshot snapshot, String programId, String userId) {
        try {
            String cultivarName = snapshotString(snapshot, "cultivarName");
            if (cultivarName == null) cultivarName = snapshotString(snapshot, "cultivar");
            String startingDate = snapshotString(snapshot, "startingDate");
            if (startingDate == null) startingDate = snapshotString(snapshot, "startDate");

            double areaSize = snapshotDouble(snapshot, "areaSize");
            if (areaSize == 0) areaSize = snapshotDouble(snapshot, "landArea");
            double projectedIncome = snapshotDouble(snapshot, "projectedIncome");
            double projectedExpenses = snapshotDouble(snapshot, "projectedExpenses");
            double adjustedIncome = snapshotDouble(snapshot, "adjustedIncome");
            double adjustedExpenses = snapshotDouble(snapshot, "adjustedExpenses");

            // Calculate phases JSON if we have cultivar and start date
            String phasesJson = null;
            if (cultivarName != null && startingDate != null) {
                phasesJson = WorkProgramDataHelper.calculatePhasesJson(cultivarName, startingDate);
            }

            // Parse research fields
            String season = snapshotString(snapshot, "season");
            int seasonMonth = snapshotInt(snapshot, "seasonMonth");
            boolean isOffSeason = snapshotBoolean(snapshot, "isOffSeason");
            double actualYield = snapshotDouble(snapshot, "actualYield");
            double totalYield = snapshotDouble(snapshot, "totalYield");
            String harvestDate = snapshotString(snapshot, "harvestDate");
            long lastUpdated = snapshotLong(snapshot, "lastUpdated");

            // Auto-detect season if not set
            if (season == null && startingDate != null) {
                season = SeasonHelper.getSeason(startingDate);
            }
            if (seasonMonth == 0 && startingDate != null) {
                seasonMonth = SeasonHelper.getSeasonMonth(startingDate);
            }
            if (!snapshot.hasChild("isOffSeason") && startingDate != null) {
                isOffSeason = SeasonHelper.isOffSeason(startingDate);
            }

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
            entity.completionRate = snapshotDouble(snapshot, "completionRate");
            for (int i = 1; i <= 5; i++) {
                Object phaseCompletion = snapshot.child("phase" + i + "Completion").getValue();
                double value = phaseCompletion instanceof Number
                        ? ((Number) phaseCompletion).doubleValue() : 0;
                if (i == 1) entity.phase1Completion = value;
                else if (i == 2) entity.phase2Completion = value;
                else if (i == 3) entity.phase3Completion = value;
                else if (i == 4) entity.phase4Completion = value;
                else entity.phase5Completion = value;
            }
            entity.lastUpdated = lastUpdated;
            entity.plantCount = snapshotInt(snapshot, "plantCount");
            if (!snapshot.hasChild("plantCount")) {
                entity.plantCount = snapshotInt(snapshot.child("intake"), "plantCount");
            }
            entity.seedsConfirmedDate = snapshotString(snapshot, "seedsConfirmedDate");
            entity.sowingDate = snapshotString(snapshot, "sowingDate");
            entity.transplantDate = snapshotString(snapshot, "transplantDate");
            entity.stopDate = snapshotString(snapshot, "stopDate");
            entity.phase3Date = snapshotString(snapshot, "phase3Date");
            entity.phase4Date = snapshotString(snapshot, "phase4Date");
            entity.phase5Date = snapshotString(snapshot, "phase5Date");
            entity.loggedExpensesTotal = snapshotDouble(snapshot, "loggedExpensesTotal");
            entity.awf = snapshotDouble(snapshot.child("farmMetrics"), "awf");
            entity.afp = snapshotDouble(snapshot.child("farmMetrics"), "afp");
            entity.marketValue = snapshotDouble(snapshot.child("farmMetrics"), "marketValue");
            entity.financeLossPct = snapshotDouble(snapshot, "financeLossPct");
            entity.phaseBudget1 = snapshotDouble(snapshot, "phaseBudget1");
            entity.phaseBudget2 = snapshotDouble(snapshot, "phaseBudget2");
            entity.phaseBudget3 = snapshotDouble(snapshot, "phaseBudget3");
            entity.phaseBudget4 = snapshotDouble(snapshot, "phaseBudget4");
            entity.phaseBudget5 = snapshotDouble(snapshot, "phaseBudget5");
            return entity;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing work program", e);
            return null;
        }
    }
}
