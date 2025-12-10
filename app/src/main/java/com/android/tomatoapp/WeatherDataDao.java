package com.android.tomatoapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WeatherDataDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(WeatherData weatherData);
    
    @Query("SELECT * FROM weather_data WHERE programId = :programId")
    WeatherData getByProgramId(String programId);
    
    @Query("SELECT * FROM weather_data WHERE userId = :userId")
    List<WeatherData> getAllForUser(String userId);
    
    @Query("DELETE FROM weather_data WHERE programId = :programId")
    void deleteByProgramId(String programId);
}

