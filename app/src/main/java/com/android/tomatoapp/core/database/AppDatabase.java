package com.android.tomatoapp.core.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {
        WorkProgramEntity.class,
        PlantMonitoringEntity.class,
        WeatherData.class,
        CalculationEntity.class,
        DetectionHistoryEntity.class,
        TaskEntity.class,
        SettingsEntity.class
}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract WorkProgramDao workProgramDao();
    public abstract PlantMonitoringDao plantMonitoringDao();
    public abstract WeatherDataDao weatherDataDao();
    public abstract CalculationDao calculationDao();
    public abstract DetectionHistoryDao detectionHistoryDao();
    public abstract TaskDao taskDao();
    public abstract SettingsDao settingsDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "tomatoapp_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}


