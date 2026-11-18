package com.android.tomatoapp.notifications;

import android.content.Context;
import android.os.Bundle;

import com.android.tomatoapp.core.ui.MainActivity;

import java.util.Calendar;

/**
 * Morning reminders 6:00-8:00 AM every 30 min. Request codes 9300-9304.
 */
public final class MorningRemindersScheduler {

    private static final int[] MORNING_TIMES = {
            6 * 60, 6 * 60 + 30, 7 * 60, 7 * 60 + 30, 8 * 60
    };

    private static final String[] MORNING_MESSAGES = {
            "Good morning! Time to check your daily tasks.",
            "Don't forget to log today's farming activities.",
            "Morning check-in: Review your scheduled tasks for today.",
            "Time for plant monitoring. Capture updated photos today.",
            "Check weather forecast and plan your day ahead."
    };

    private static final int BASE_REQUEST_CODE = 9300; // was 9200 (collided with tasks)
    private static final long INTERVAL_DAY = 24 * 60 * 60 * 1000L;

    private MorningRemindersScheduler() {
    }

    public static void ensureMorningRemindersScheduled(Context context) {
        if (!NotificationPreferences.areGeneralNotificationsEnabled(context)) {
            cancelMorningReminders(context);
            return;
        }

        for (int i = 0; i < MORNING_TIMES.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, MORNING_TIMES[i] / 60);
            calendar.set(Calendar.MINUTE, MORNING_TIMES[i] % 60);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            NotificationScheduler.scheduleRepeating(
                    context,
                    NotificationReceiver.TYPE_GENERAL,
                    NotificationChannels.CHANNEL_GENERAL,
                    "Daily farming reminder",
                    MORNING_MESSAGES[i],
                    MainActivity.class,
                    new Bundle(),
                    calendar.getTimeInMillis(),
                    INTERVAL_DAY,
                    BASE_REQUEST_CODE + i,
                    false
            );
        }
    }

    public static void cancelMorningReminders(Context context) {
        for (int i = 0; i < MORNING_TIMES.length; i++) {
            NotificationScheduler.cancel(context, BASE_REQUEST_CODE + i);
        }
    }
}