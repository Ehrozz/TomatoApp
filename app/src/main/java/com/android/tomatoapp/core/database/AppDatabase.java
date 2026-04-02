package com.android.tomatoapp.core.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.tomatoapp.detection.data.DetectionHistoryDao;
import com.android.tomatoapp.detection.data.DetectionHistoryEntity;
import com.android.tomatoapp.financial.data.CalculationDao;
import com.android.tomatoapp.financial.data.CalculationEntity;
import com.android.tomatoapp.monitoring.data.PlantMonitoringDao;
import com.android.tomatoapp.monitoring.data.PlantMonitoringEntity;
import com.android.tomatoapp.settings.data.SettingsDao;
import com.android.tomatoapp.settings.data.SettingsEntity;
import com.android.tomatoapp.task.data.TaskDao;
import com.android.tomatoapp.task.data.TaskEntity;
import com.android.tomatoapp.weather.data.WeatherData;
import com.android.tomatoapp.weather.data.WeatherDataDao;
import com.android.tomatoapp.workprogram.data.WorkProgramDao;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;

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
