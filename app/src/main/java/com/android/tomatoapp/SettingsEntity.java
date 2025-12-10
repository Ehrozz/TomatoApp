package com.android.tomatoapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings")
public class SettingsEntity {
    @PrimaryKey
    @NonNull
    public String userId;

    public String language;
    public String theme;
    public String defaultCultivar;
    public String weatherUnit;
    public String measurementUnit;
    public String dateFormat;
    public boolean taskNotifications;
    public boolean monitoringNotifications;
    public boolean generalNotifications;
    public String notificationSound;
    public String notificationTime;
    public boolean quietHoursEnabled;
    public String quietHoursStart;
    public String quietHoursEnd;
    public long lastSynced;

    public SettingsEntity() {
        // Default constructor required for Room
        this.userId = "";
    }

    @Ignore
    public SettingsEntity(@NonNull String userId, String language, String theme,
                         String defaultCultivar, String weatherUnit, String measurementUnit,
                         String dateFormat, boolean taskNotifications,
                         boolean monitoringNotifications, boolean generalNotifications,
                         String notificationSound, String notificationTime,
                         boolean quietHoursEnabled, String quietHoursStart,
                         String quietHoursEnd, long lastSynced) {
        this.userId = userId;
        this.language = language;
        this.theme = theme;
        this.defaultCultivar = defaultCultivar;
        this.weatherUnit = weatherUnit;
        this.measurementUnit = measurementUnit;
        this.dateFormat = dateFormat;
        this.taskNotifications = taskNotifications;
        this.monitoringNotifications = monitoringNotifications;
        this.generalNotifications = generalNotifications;
        this.notificationSound = notificationSound;
        this.notificationTime = notificationTime;
        this.quietHoursEnabled = quietHoursEnabled;
        this.quietHoursStart = quietHoursStart;
        this.quietHoursEnd = quietHoursEnd;
        this.lastSynced = lastSynced;
    }
}

