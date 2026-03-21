package com.android.tomatoapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DetectionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DetectionHistoryEntity detection);

    @Query("SELECT * FROM detection_history WHERE userId = :userId ORDER BY timestamp DESC")
    List<DetectionHistoryEntity> getAllByUser(String userId);

    @Query("SELECT * FROM detection_history WHERE programId = :programId")
    List<DetectionHistoryEntity> getByProgramId(String programId);

    @Delete
    void delete(DetectionHistoryEntity detection);

    @Query("DELETE FROM detection_history WHERE userId = :userId")
    void deleteAllByUser(String userId);
}

