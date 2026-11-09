package com.android.tomatoapp.workprogram.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.android.tomatoapp.core.database.AppDatabase;
import com.android.tomatoapp.financial.data.DailyExpenseMath;
import com.android.tomatoapp.season.data.SeasonHelper;

/**
 * Repository that keeps WorkProgram data in local Room and Firebase Realtime Database.
 */
public class WorkProgramRepository {

    private final WorkProgramDao workProgramDao;
    private final DatabaseReference workProgramsRef;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final String userId;

    public interface LoadCallback {
        void onLoaded(List<WorkProgramEntity> items);
    }

    public WorkProgramRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        workProgramDao = db.workProgramDao();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            workProgramsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("workPrograms");
        } else {
            userId = null;
            workProgramsRef = null;
        }
    }

    /**
     * Push a single work program entity to local DB and Firebase.
     */
    public void upsert(final WorkProgramEntity entity) {
        if (entity == null) return;

        ioExecutor.execute(() -> workProgramDao.upsert(entity));

        if (workProgramsRef != null && userId != null) {
            workProgramsRef.child(entity.id).child("cultivarName").setValue(entity.cultivarName);
            workProgramsRef.child(entity.id).child("startingDate").setValue(entity.startingDate);
            workProgramsRef.child(entity.id).child("areaSize").setValue(entity.areaSize);
            workProgramsRef.child(entity.id).child("projectedIncome").setValue(entity.projectedIncome);
            workProgramsRef.child(entity.id).child("projectedExpenses").setValue(entity.projectedExpenses);
            // Research fields
            workProgramsRef.child(entity.id).child("season").setValue(entity.season);
            workProgramsRef.child(entity.id).child("seasonMonth").setValue(entity.seasonMonth);
            workProgramsRef.child(entity.id).child("isOffSeason").setValue(entity.isOffSeason);
            workProgramsRef.child(entity.id).child("actualYield").setValue(entity.actualYield);
            workProgramsRef.child(entity.id).child("totalYield").setValue(entity.totalYield);
            workProgramsRef.child(entity.id).child("harvestDate").setValue(entity.harvestDate);
            // phases and detection histories can be synced later when structured
        }
    }

    /**
     * Load all work programs for current user, refreshing from Firebase into Room first.
     * Local Room rows are delivered immediately so newly created programs appear even
     * before Firebase and completion-stat fetches finish.
     */
    public void loadAllForCurrentUser(LoadCallback callback) {
        deliverLocalPrograms(callback);

        if (workProgramsRef == null || userId == null) {
            return;
        }

        workProgramsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<WorkProgramEntity> buffer = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                    String id = child.getKey();
                    if (id == null) continue;

                    String cultivarName = child.child("cultivarName").getValue(String.class);
                    if (cultivarName == null) {
                        cultivarName = child.child("cultivar").getValue(String.class);
                    }
                    String startingDate = child.child("startingDate").getValue(String.class);
                    if (startingDate == null) {
                        startingDate = child.child("startDate").getValue(String.class);
                    }
                    if ((cultivarName == null || cultivarName.isEmpty())
                            && (startingDate == null || startingDate.isEmpty())) {
                        child.getRef().removeValue();
                        continue;
                    }
                    if (cultivarName == null || cultivarName.isEmpty()
                            || startingDate == null || startingDate.isEmpty()) {
                        continue;
                    }

                    // Safely parse numeric fields that might be stored as Double, Long, or String in Firebase
                    double areaSize = 0;
                    Object areaValue = child.child("areaSize").getValue();
                    if (areaValue instanceof Double) {
                        areaSize = (Double) areaValue;
                    } else if (areaValue instanceof Long) {
                        areaSize = ((Long) areaValue).doubleValue();
                    } else if (areaValue instanceof String) {
                        try {
                            areaSize = Double.parseDouble((String) areaValue);
                        } catch (NumberFormatException ignored) { }
                    } else {
                        // fallback to legacy landArea
                        Object legacyAreaValue = child.child("landArea").getValue();
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
                    Object incomeValue = child.child("projectedIncome").getValue();
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
                    Object expensesValue = child.child("projectedExpenses").getValue();
                    if (expensesValue instanceof Double) {
                        projectedExpenses = (Double) expensesValue;
                    } else if (expensesValue instanceof Long) {
                        projectedExpenses = ((Long) expensesValue).doubleValue();
                    } else if (expensesValue instanceof String) {
                        try {
                            projectedExpenses = Double.parseDouble((String) expensesValue);
                        } catch (NumberFormatException ignored) { }
                    }

                    // Calculate phases JSON if we have cultivar and start date
                    String phasesJson = null;
                    if (cultivarName != null && startingDate != null) {
                        phasesJson = WorkProgramDataHelper.calculatePhasesJson(cultivarName, startingDate);
                    }

                    // Detection histories will be loaded on-demand in AnalyticsActivity
                    // For now, we'll leave it null and load it when needed
                    
                    // Parse research fields from Firebase
                    String season = child.child("season").getValue(String.class);
                    String harvestDate = child.child("harvestDate").getValue(String.class);
                    
                    // Auto-detect season if not set
                    if (season == null && startingDate != null) {
                        season = SeasonHelper.getSeason(startingDate);
                    }
                    int seasonMonth = toInt(child.child("seasonMonth").getValue(),
                            startingDate != null ? SeasonHelper.getSeasonMonth(startingDate) : 0);
                    boolean isOffSeason = toBoolean(child.child("isOffSeason").getValue(),
                            startingDate != null && SeasonHelper.isOffSeason(startingDate));
                    double actualYield = toDouble(child.child("actualYield").getValue(), 0.0);
                    double totalYield = toDouble(child.child("totalYield").getValue(), 0.0);

                    WorkProgramEntity entity = new WorkProgramEntity(
                            id,
                            userId,
                            cultivarName,
                            areaSize,
                            startingDate,
                            phasesJson,
                            null, // detection histories loaded on-demand
                            projectedIncome,
                            projectedExpenses,
                            projectedIncome,
                            projectedExpenses,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            season != null ? season : (startingDate != null ? SeasonHelper.getSeason(startingDate) : "unknown"),
                            seasonMonth,
                            isOffSeason,
                            actualYield,
                            totalYield,
                            harvestDate
                    );
                    entity.loggedExpenses = DailyExpenseMath.sumAllDates(child.child("dailyExpenses"));
                    if (entity.loggedExpenses <= 0) {
                        entity.loggedExpenses = DailyExpenseMath.asDouble(child.child("loggedExpensesTotal").getValue());
                    }
                    if (entity.loggedExpenses > 0) {
                        entity.adjustedExpenses = entity.loggedExpenses;
                    }
                    buffer.add(entity);
                    } catch (Exception ignored) {
                        // Skip a malformed child so the rest of the programs still load.
                    }
                }

                persistBuffer(callback, buffer);

                if (buffer.isEmpty()) {
                    return;
                }

                AtomicInteger remaining = new AtomicInteger(buffer.size());
                for (WorkProgramEntity entity : buffer) {
                    WorkProgramDataHelper.fetchCompletionStats(
                            userId,
                            entity.id,
                            entity.cultivarName,
                            entity.startingDate,
                            stats -> {
                                if (stats != null) {
                                    entity.totalTasks = stats.totalTasks;
                                    entity.completedTasks = stats.completedTasks;
                                    entity.missedTasks = stats.missedTasks;
                                    entity.skippedTasks = stats.skippedTasks;
                                    entity.completionRate = stats.completionRate;
                                    WorkProgramDataHelper.AdjustedProjection projection =
                                            WorkProgramDataHelper.adjustProjectionsByCompletionRate(
                                                    entity.projectedIncome,
                                                    entity.projectedExpenses,
                                                    stats
                                            );
                                    entity.adjustedIncome = projection.adjustedIncome;
                                    if (entity.loggedExpenses > 0) {
                                        entity.adjustedExpenses = entity.loggedExpenses;
                                    } else {
                                        entity.adjustedExpenses = projection.adjustedExpenses;
                                    }
                                    double[] phaseRates = WorkProgramDataHelper.getPhaseCompletionRates(stats);
                                    if (phaseRates.length >= 5) {
                                        entity.phase1Completion = phaseRates[0];
                                        entity.phase2Completion = phaseRates[1];
                                        entity.phase3Completion = phaseRates[2];
                                        entity.phase4Completion = phaseRates[3];
                                        entity.phase5Completion = phaseRates[4];
                                    }
                                }

                                if (remaining.decrementAndGet() == 0) {
                                    persistBuffer(callback, buffer);
                                }
                            }
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                deliverLocalPrograms(callback);
            }
        });
    }

    private void deliverLocalPrograms(LoadCallback callback) {
        ioExecutor.execute(() -> {
            List<WorkProgramEntity> local = workProgramDao.getAllForUser(userId);
            List<WorkProgramEntity> filtered = new ArrayList<>();
            if (local != null) {
                for (WorkProgramEntity entity : local) {
                    if (entity != null && entity.cultivarName != null && !entity.cultivarName.isEmpty()) {
                        filtered.add(entity);
                    }
                }
            }
            if (callback != null) callback.onLoaded(sortedCopy(filtered));
        });
    }

    private void persistBuffer(LoadCallback callback, List<WorkProgramEntity> buffer) {
        ioExecutor.execute(() -> {
            Map<String, WorkProgramEntity> remoteById = new HashMap<>();
            if (buffer != null) {
                for (WorkProgramEntity e : buffer) {
                    workProgramDao.upsert(e);
                    if (e != null && e.id != null) {
                        remoteById.put(e.id, e);
                    }
                }
            }
            List<WorkProgramEntity> local = workProgramDao.getAllForUser(userId);
            List<WorkProgramEntity> merged = new ArrayList<>();
            if (local != null) {
                for (WorkProgramEntity e : local) {
                    if (e != null && e.id != null && !remoteById.containsKey(e.id)
                            && System.currentTimeMillis() - e.lastUpdated > 120000) {
                        workProgramDao.deleteById(e.id);
                        continue;
                    }
                    WorkProgramEntity remote = e.id != null ? remoteById.get(e.id) : null;
                    if (remote != null) {
                        e.loggedExpenses = remote.loggedExpenses;
                        e.completionRate = remote.completionRate;
                        e.adjustedIncome = remote.adjustedIncome;
                        e.adjustedExpenses = remote.loggedExpenses > 0
                                ? remote.loggedExpenses
                                : remote.adjustedExpenses;
                        e.totalTasks = remote.totalTasks;
                        e.completedTasks = remote.completedTasks;
                    }
                    merged.add(e);
                }
            }
            if (callback != null) callback.onLoaded(sortedCopy(merged));
        });
    }

    private List<WorkProgramEntity> sortedCopy(List<WorkProgramEntity> source) {
        List<WorkProgramEntity> copy = source != null ? new ArrayList<>(source) : new ArrayList<>();
        Collections.sort(copy, (a, b) -> {
            int dateCmp = compareNullableDates(b != null ? b.startingDate : null,
                    a != null ? a.startingDate : null);
            if (dateCmp != 0) return dateCmp;
            long aUpdated = a != null ? a.lastUpdated : 0;
            long bUpdated = b != null ? b.lastUpdated : 0;
            return Long.compare(bUpdated, aUpdated);
        });
        return copy;
    }

    private static int compareNullableDates(String left, String right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;
        return left.compareTo(right);
    }

    private static double toDouble(Object value, double fallback) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static int toInt(Object value, int fallback) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static boolean toBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        if (value instanceof String) {
            return "true".equalsIgnoreCase((String) value) || "1".equals(value);
        }
        return fallback;
    }
}

