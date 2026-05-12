package com.android.tomatoapp.notifications;

import android.content.Context;
import android.os.Bundle;

import com.android.tomatoapp.R;
import com.android.tomatoapp.core.ui.MainActivity;

import java.util.Calendar;

/**
 * Handles task reminder notifications at 6:00 AM, 6:30 AM, 7:00 AM, 7:30 AM, 8:00 AM.
 */
public final class TaskNotificationScheduler {

    private static final int[] TASK_REQUEST_CODES = {9200, 9201, 9202, 9203, 9204};
    private static final int[] TASK_HOURS = {6, 6, 7, 7, 8};
    private static final int[] TASK_MINUTES = {0, 30, 0, 30, 0};
    private static final long INTERVAL_DAY = 24 * 60 * 60 * 1000L;

    private static final String[] TASK_MESSAGES = new String[]{
            "📋 Don't forget to log your tasks for today!",
            "⏰ Check your work program tasks.",
            "✅ Time to update task progress.",
            "🌾 Review and complete scheduled tasks.",
            "📝 Log today's farming activities."
    };

    private TaskNotificationScheduler() {
    }

    public static void ensureTaskNotificationsScheduled(Context context) {
        if (!NotificationPreferences.areTaskNotificationsEnabled(context)) {
            cancelTaskNotifications(context);
            return;
        }
        if (NotificationPreferences.areTaskRemindersScheduled(context)) {
            return;
        }

        // Schedule 5 notifications at different times
        for (int i = 0; i < TASK_REQUEST_CODES.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, TASK_HOURS[i]);
            calendar.set(Calendar.MINUTE, TASK_MINUTES[i]);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Bundle extras = new Bundle();

            NotificationScheduler.scheduleRepeating(
                    context,
                    NotificationReceiver.TYPE_TASK,
                    NotificationChannels.CHANNEL_TASKS,
                    "Task Reminder",
                    TASK_MESSAGES[i],
                    MainActivity.class,
                    extras,
                    calendar.getTimeInMillis(),
                    INTERVAL_DAY,
                    TASK_REQUEST_CODES[i],
                    false
            );
        }
        NotificationPreferences.setTaskRemindersScheduled(context, true);
    }

    public static void cancelTaskNotifications(Context context) {
        for (int requestCode : TASK_REQUEST_CODES) {
            NotificationScheduler.cancel(context, requestCode);
        }
        NotificationPreferences.setTaskRemindersScheduled(context, false);
    }
}
