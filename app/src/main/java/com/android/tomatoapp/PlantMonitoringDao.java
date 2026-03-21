package com.android.tomatoapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlantMonitoringDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(PlantMonitoringEntity entity);

    @Query("SELECT * FROM plant_monitoring WHERE userId = :userId AND programId = :programId ORDER BY timestamp DESC")
    List<PlantMonitoringEntity> getByProgram(String userId, String programId);

    @Query("SELECT * FROM plant_monitoring WHERE userId = :userId ORDER BY timestamp DESC")
    List<PlantMonitoringEntity> getAllForUser(String userId);

    @Query("SELECT * FROM plant_monitoring WHERE id = :id LIMIT 1")
    PlantMonitoringEntity findById(String id);

    @Query("DELETE FROM plant_monitoring WHERE id = :id")
    void deleteById(String id);
}

