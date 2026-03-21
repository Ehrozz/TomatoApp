package com.android.tomatoapp.task.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskEntity task);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TaskEntity> tasks);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND programId = :programId ORDER BY dateKey ASC")
    List<TaskEntity> getByProgram(String userId, String programId);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND programId = :programId AND dateKey = :dateKey")
    TaskEntity getByDate(String userId, String programId, String dateKey);

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    void updateStatus(String id, String status);

    @Delete
    void delete(TaskEntity task);

    @Query("DELETE FROM tasks WHERE userId = :userId AND programId = :programId")
    void deleteByProgram(String userId, String programId);
}

