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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
     */
    public void loadAllForCurrentUser(LoadCallback callback) {
        if (workProgramsRef == null || userId == null) {
            // No logged-in user, just return local (likely empty)
            ioExecutor.execute(() -> {
                List<WorkProgramEntity> local = workProgramDao.getAllForUser(userId);
                if (callback != null) callback.onLoaded(local);
            });
            return;
        }

        workProgramsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<WorkProgramEntity> buffer = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
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
                    Integer seasonMonthObj = child.child("seasonMonth").getValue(Integer.class);
                    Boolean isOffSeasonObj = child.child("isOffSeason").getValue(Boolean.class);
                    Double actualYieldObj = child.child("actualYield").getValue(Double.class);
                    Double totalYieldObj = child.child("totalYield").getValue(Double.class);
                    String harvestDate = child.child("harvestDate").getValue(String.class);
                    
                    // Auto-detect season if not set
                    if (season == null && startingDate != null) {
                        season = SeasonHelper.getSeason(startingDate);
                    }
                    int seasonMonth = seasonMonthObj != null ? seasonMonthObj : 
                                     (startingDate != null ? SeasonHelper.getSeasonMonth(startingDate) : 0);
                    boolean isOffSeason = isOffSeasonObj != null ? isOffSeasonObj : 
                                         (startingDate != null ? SeasonHelper.isOffSeason(startingDate) : false);
                    double actualYield = actualYieldObj != null ? actualYieldObj : 0.0;
                    double totalYield = totalYieldObj != null ? totalYieldObj : 0.0;

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
                    buffer.add(entity);
                }

                if (buffer.isEmpty()) {
                    persistBuffer(callback, buffer);
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
                                    entity.adjustedExpenses = projection.adjustedExpenses;
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
                ioExecutor.execute(() -> {
                    List<WorkProgramEntity> local = workProgramDao.getAllForUser(userId);
                    if (callback != null) callback.onLoaded(local);
                });
            }
        });
    }

    private void persistBuffer(LoadCallback callback, List<WorkProgramEntity> buffer) {
        ioExecutor.execute(() -> {
            for (WorkProgramEntity e : buffer) {
                workProgramDao.upsert(e);
            }
            List<WorkProgramEntity> local = workProgramDao.getAllForUser(userId);
            if (callback != null) callback.onLoaded(local);
        });
    }
}


