package com.android.tomatoapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkProgramDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(WorkProgramEntity entity);

    @Query("DELETE FROM work_programs WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM work_programs WHERE userId = :userId")
    List<WorkProgramEntity> getAllForUser(String userId);

    @Query("SELECT * FROM work_programs WHERE userId = :userId AND cultivarName = :cultivarName")
    List<WorkProgramEntity> getByCultivar(String userId, String cultivarName);
}


