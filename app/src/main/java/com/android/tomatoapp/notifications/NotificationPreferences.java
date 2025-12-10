package com.android.tomatoapp.notifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Handles notification user preferences and scheduling flags.
 */
public final class NotificationPreferences {

    private static final String PREFS_NAME = "notification_prefs";

    private static final String KEY_TASK_ENABLED = "task_enabled";
    private static final String KEY_MONITORING_ENABLED = "monitoring_enabled";
    private static final String KEY_GENERAL_ENABLED = "general_enabled";
    private static final String KEY_GENERAL_SCHEDULED = "general_scheduled";

    private NotificationPreferences() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean areTaskNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_TASK_ENABLED, true);
    }

    public static void setTaskNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_TASK_ENABLED, enabled).apply();
    }

    public static boolean areMonitoringNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_MONITORING_ENABLED, true);
    }

    public static void setMonitoringNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_MONITORING_ENABLED, enabled).apply();
    }

    public static boolean areGeneralNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_GENERAL_ENABLED, true);
    }

    public static void setGeneralNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_GENERAL_ENABLED, enabled).apply();
    }

    public static boolean isGeneralReminderScheduled(Context context) {
        return prefs(context).getBoolean(KEY_GENERAL_SCHEDULED, false);
    }

    public static void setGeneralReminderScheduled(Context context, boolean scheduled) {
        prefs(context).edit().putBoolean(KEY_GENERAL_SCHEDULED, scheduled).apply();
    }
}

