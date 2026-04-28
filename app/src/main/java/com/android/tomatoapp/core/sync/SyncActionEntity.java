package com.android.tomatoapp.core.sync;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_queue")
public class SyncActionEntity {
    @PrimaryKey(autoGenerate = true)
    public long queueId;

    public String userId;
    public String entityType; // e.g., "work_program", "calculation", "location"
    public String entityId;
    public String action; // "CREATE", "UPDATE", "DELETE"
    public long timestamp;
    public String dataJson; // Optional payload for the action

    public SyncActionEntity() {}

    public SyncActionEntity(String userId, String entityType, String entityId, String action, String dataJson) {
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.dataJson = dataJson;
        this.timestamp = System.currentTimeMillis();
    }
}
