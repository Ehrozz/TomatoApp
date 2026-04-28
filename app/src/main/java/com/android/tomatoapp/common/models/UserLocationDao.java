package com.android.tomatoapp.common.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserLocationEntity entity);

    @Query("DELETE FROM user_locations WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM user_locations WHERE userId = :userId")
    List<UserLocationEntity> getAllForUser(String userId);

    @Query("SELECT * FROM user_locations WHERE id = :id")
    UserLocationEntity getById(String id);

    @Query("UPDATE user_locations SET isDefault = 0 WHERE userId = :userId AND id != :defaultId")
    void clearOtherDefaults(String userId, String defaultId);
}
