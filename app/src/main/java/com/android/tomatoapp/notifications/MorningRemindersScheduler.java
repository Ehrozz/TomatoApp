package com.android.tomatoapp.notifications;

import android.content.Context;
import android.os.Bundle;

import com.android.tomatoapp.core.ui.MainActivity;

import java.util.Calendar;

/**
 * Handles morning reminder notifications scheduled from 6:00 AM to 8:00 AM every 30 minutes.
 * Helps farmers stay on top of daily tasks and monitoring.
 */
public final class MorningRemindersScheduler {

    private static final int[] MORNING_TIMES = {
            6 * 60,    // 6:00 AM
            6 * 60 + 30,  // 6:30 AM
            7 * 60,    // 7:00 AM
            7 * 60 + 30,  // 7:30 AM
            8 * 60     // 8:00 AM
    };

    private static final String[] MORNING_MESSAGES = {
            "Good morning! Time to check your daily tasks.",
            "Don't forget to log today's farming activities.",
            "Morning check-in: Review your scheduled tasks for today.",
            "Time for plant monitoring. Capture updated photos today.",
            "Check weather forecast and plan your day ahead."
    };

    private static final int BASE_REQUEST_CODE = 9200;
    private static final long INTERVAL_DAY = 24 * 60 * 60 * 1000L;

    private MorningRemindersScheduler() {
    }

    /**
     * Schedule morning reminders (6:00 AM to 8:00 AM every 30 minutes)
     * @param context The application context
     */
    public static void ensureMorningRemindersScheduled(Context context) {
        if (!NotificationPreferences.areGeneralNotificationsEnabled(context)) {
            cancelMorningReminders(context);
            return;
        }

        // Schedule each morning reminder time
        for (int i = 0; i < MORNING_TIMES.length; i++) {
            int minutesFromMidnight = MORNING_TIMES[i];
            int hour = minutesFromMidnight / 60;
            int minute = minutesFromMidnight % 60;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // If time has already passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Bundle extras = new Bundle();
            int requestCode = BASE_REQUEST_CODE + i;

            NotificationScheduler.scheduleRepeating(
                    context,
                    NotificationReceiver.TYPE_GENERAL,
                    NotificationChannels.CHANNEL_GENERAL,
                    "Daily farming reminder",
                    MORNING_MESSAGES[i],
                    MainActivity.class,
                    extras,
                    calendar.getTimeInMillis(),
                    INTERVAL_DAY,
                    requestCode,
                    false
            );
        }
    }

    /**
     * Cancel all morning reminder notifications
     * @param context The application context
     */
    public static void cancelMorningReminders(Context context) {
        for (int i = 0; i < MORNING_TIMES.length; i++) {
            NotificationScheduler.cancel(context, BASE_REQUEST_CODE + i);
        }
    }
}
