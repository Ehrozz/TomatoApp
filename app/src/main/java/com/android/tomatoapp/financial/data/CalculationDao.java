package com.android.tomatoapp.financial.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CalculationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CalculationEntity calculation);

    @Query("SELECT * FROM calculations WHERE userId = :userId ORDER BY dateCreated DESC")
    List<CalculationEntity> getAllByUser(String userId);

    @Query("SELECT * FROM calculations WHERE programId = :programId")
    List<CalculationEntity> getByProgramId(String programId);

    @Delete
    void delete(CalculationEntity calculation);

    @Query("DELETE FROM calculations WHERE userId = :userId")
    void deleteAllByUser(String userId);
}

