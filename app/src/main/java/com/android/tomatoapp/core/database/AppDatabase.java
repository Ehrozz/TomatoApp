package com.android.tomatoapp.core.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.android.tomatoapp.detection.data.DetectionHistoryDao;
import com.android.tomatoapp.detection.data.DetectionHistoryEntity;
import com.android.tomatoapp.common.models.UserLocationDao;
import com.android.tomatoapp.common.models.UserLocationEntity;
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
import com.android.tomatoapp.core.sync.SyncActionDao;
import com.android.tomatoapp.core.sync.SyncActionEntity;

@Database(entities = {
        WorkProgramEntity.class,
        PlantMonitoringEntity.class,
        UserLocationEntity.class,
        WeatherData.class,
        CalculationEntity.class,
        DetectionHistoryEntity.class,
        TaskEntity.class,
        SettingsEntity.class,
        SyncActionEntity.class
}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE work_programs ADD COLUMN plantCount INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN seedsConfirmedDate TEXT");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN sowingDate TEXT");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN transplantDate TEXT");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN stopDate TEXT");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN phase3Date TEXT");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN phase4Date TEXT");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN phase5Date TEXT");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN loggedExpensesTotal REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN awf REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN afp REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN marketValue REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN financeLossPct REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN phaseBudget1 REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN phaseBudget2 REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN phaseBudget3 REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN phaseBudget4 REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE work_programs ADD COLUMN phaseBudget5 REAL NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN skipReason TEXT");
            database.execSQL("ALTER TABLE tasks ADD COLUMN skipNote TEXT");
            database.execSQL("ALTER TABLE tasks ADD COLUMN weatherNote TEXT");
            database.execSQL("ALTER TABLE tasks ADD COLUMN late INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tasks ADD COLUMN scanLinked INTEGER NOT NULL DEFAULT 0");
        }
    };

    public abstract WorkProgramDao workProgramDao();
    public abstract PlantMonitoringDao plantMonitoringDao();
    public abstract WeatherDataDao weatherDataDao();
    public abstract CalculationDao calculationDao();
    public abstract DetectionHistoryDao detectionHistoryDao();
    public abstract TaskDao taskDao();
    public abstract SettingsDao settingsDao();
    public abstract SyncActionDao syncActionDao();
    public abstract UserLocationDao userLocationDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "tomatoapp_db"
                            )
                            .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
