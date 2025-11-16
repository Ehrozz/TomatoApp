package com.android.tomatoapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles local/remote persistence of plant monitoring entries.
 */
public class PlantMonitoringRepository {

    public interface LoadCallback {
        void onLoaded(List<PlantMonitoringEntity> entries);
    }

    private final PlantMonitoringDao monitoringDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final String userId;
    private final DatabaseReference monitoringRef;

    public PlantMonitoringRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        monitoringDao = db.plantMonitoringDao();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            monitoringRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("plantMonitoring");
        } else {
            userId = null;
            monitoringRef = null;
        }
    }

    public void saveEntry(@NonNull PlantMonitoringEntity entity) {
        ioExecutor.execute(() -> monitoringDao.upsert(entity));

        if (monitoringRef != null && userId != null && entity.programId != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("phase", entity.phase);
            payload.put("timestamp", entity.timestamp);
            payload.put("shortDescription", entity.shortDescription);
            if (entity.issues != null) payload.put("issues", entity.issues);
            if (entity.warnings != null) payload.put("warnings", entity.warnings);
            if (entity.notes != null) payload.put("notes", entity.notes);
            if (entity.detectionId != null) payload.put("detectionId", entity.detectionId);

            monitoringRef.child(entity.programId)
                    .child(entity.id)
                    .setValue(payload);
        }
    }

    public void deleteEntry(@NonNull String entryId, @Nullable String programId) {
        ioExecutor.execute(() -> monitoringDao.deleteById(entryId));

        if (monitoringRef != null && userId != null && programId != null) {
            monitoringRef.child(programId).child(entryId).removeValue();
        }
    }

    public void loadForProgram(@NonNull String programId, @Nullable LoadCallback callback) {
        if (monitoringRef == null || userId == null) {
            ioExecutor.execute(() -> {
                List<PlantMonitoringEntity> local = monitoringDao.getByProgram(userId, programId);
                if (callback != null) callback.onLoaded(local);
            });
            return;
        }

        monitoringRef.child(programId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<RemoteEntry> remoteEntries = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String id = child.getKey();
                    if (id == null) continue;
                    RemoteEntry entry = new RemoteEntry();
                    entry.id = id;
                    entry.programId = programId;
                    entry.phase = safeInt(child.child("phase").getValue());
                    entry.timestamp = safeLong(child.child("timestamp").getValue());
                    entry.shortDescription = child.child("shortDescription").getValue(String.class);
                    entry.issues = child.child("issues").getValue(String.class);
                    entry.warnings = child.child("warnings").getValue(String.class);
                    entry.notes = child.child("notes").getValue(String.class);
                    entry.detectionId = child.child("detectionId").getValue(String.class);
                    remoteEntries.add(entry);
                }

                ioExecutor.execute(() -> {
                    for (RemoteEntry remote : remoteEntries) {
                        String existingPhoto = null;
                        PlantMonitoringEntity existing = monitoringDao.findById(remote.id);
                        if (existing != null) {
                            existingPhoto = existing.photoPath;
                        }

                        PlantMonitoringEntity entity = new PlantMonitoringEntity(
                                remote.id,
                                userId,
                                remote.programId,
                                remote.phase,
                                remote.timestamp,
                                remote.shortDescription,
                                remote.issues,
                                remote.warnings,
                                remote.notes,
                                remote.detectionId,
                                existingPhoto
                        );
                        monitoringDao.upsert(entity);
                    }

                    List<PlantMonitoringEntity> local = monitoringDao.getByProgram(userId, programId);
                    if (callback != null) callback.onLoaded(local);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ioExecutor.execute(() -> {
                    List<PlantMonitoringEntity> local = monitoringDao.getByProgram(userId, programId);
                    if (callback != null) callback.onLoaded(local);
                });
            }
        });
    }

    private static int safeInt(Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) { }
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return 0;
    }

    private static long safeLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) { }
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        return System.currentTimeMillis();
    }

    private static class RemoteEntry {
        String id;
        String programId;
        int phase;
        long timestamp;
        String shortDescription;
        String issues;
        String warnings;
        String notes;
        String detectionId;
    }
}

