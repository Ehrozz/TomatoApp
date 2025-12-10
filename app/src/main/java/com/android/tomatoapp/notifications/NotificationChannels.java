package com.android.tomatoapp.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

/**
 * Central place to define and create notification channels.
 */
public final class NotificationChannels {

    public static final String CHANNEL_TASKS = "tomato_tasks";
    public static final String CHANNEL_MONITORING = "tomato_monitoring";
    public static final String CHANNEL_GENERAL = "tomato_general";

    private NotificationChannels() {
        // no-op
    }

    public static void ensureCreated(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        NotificationChannel tasksChannel = new NotificationChannel(
                CHANNEL_TASKS,
                "Work Program Tasks",
                NotificationManager.IMPORTANCE_HIGH
        );
        tasksChannel.setDescription("Reminders for daily work program tasks and missed-task alerts.");

        NotificationChannel monitoringChannel = new NotificationChannel(
                CHANNEL_MONITORING,
                "Monitoring & Disease Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        monitoringChannel.setDescription("Plant monitoring reminders and detection alerts.");

        NotificationChannel generalChannel = new NotificationChannel(
                CHANNEL_GENERAL,
                "General Updates",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        generalChannel.setDescription("Tips, warnings, and general TomatoApp updates.");

        manager.createNotificationChannel(tasksChannel);
        manager.createNotificationChannel(monitoringChannel);
        manager.createNotificationChannel(generalChannel);
    }
}

