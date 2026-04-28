package com.android.tomatoapp.core.sync;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SyncActionDao {
    @Insert
    void insert(SyncActionEntity action);

    @Query("SELECT * FROM sync_queue WHERE userId = :userId ORDER BY timestamp ASC")
    List<SyncActionEntity> getAllPending(String userId);

    @Delete
    void delete(SyncActionEntity action);

    @Query("DELETE FROM sync_queue WHERE entityId = :entityId AND entityType = :entityType")
    void deleteForEntity(String entityId, String entityType);

    @Query("SELECT COUNT(*) FROM sync_queue WHERE userId = :userId")
    int getPendingCount(String userId);
}
