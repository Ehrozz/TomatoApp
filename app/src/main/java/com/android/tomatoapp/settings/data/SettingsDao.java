package com.android.tomatoapp.settings.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SettingsEntity settings);

    @Query("SELECT * FROM settings WHERE userId = :userId LIMIT 1")
    SettingsEntity getByUser(String userId);

    @Delete
    void delete(SettingsEntity settings);
}

