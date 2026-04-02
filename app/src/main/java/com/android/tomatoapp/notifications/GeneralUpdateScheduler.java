package com.android.tomatoapp.notifications;

import android.content.Context;
import android.os.Bundle;

import com.android.tomatoapp.R;
import com.android.tomatoapp.core.ui.MainActivity;

import java.util.Calendar;
import java.util.Random;

/**
 * Handles general tips / update notifications.
 */
public final class GeneralUpdateScheduler {

    private static final int GENERAL_REQUEST_CODE = 9100;
    private static final long INTERVAL_DAY = 24 * 60 * 60 * 1000L;
    private static final String[] DEFAULT_TIPS = new String[]{
            "Remember to log skipped tasks so analytics stay accurate.",
            "Humidity spikes invite pests. Inspect leaves today.",
            "Capture monitoring notes weekly to build stronger baselines.",
            "Fertilizer works best in the morning—plan it into your tasks.",
            "Review detection history to spot recurring disease patterns."
    };

    private static final Random RANDOM = new Random();

    private GeneralUpdateScheduler() {
    }

    public static void ensureDailyTipScheduled(Context context) {
        if (!NotificationPreferences.areGeneralNotificationsEnabled(context)) {
            cancelDailyTip(context);
            return;
        }
        if (NotificationPreferences.isGeneralReminderScheduled(context)) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Bundle extras = new Bundle();

        NotificationScheduler.scheduleRepeating(
                context,
                NotificationReceiver.TYPE_GENERAL,
                NotificationChannels.CHANNEL_GENERAL,
                "TomatoApp tip",
                "Tap to view today's tip",
                MainActivity.class,
                extras,
                calendar.getTimeInMillis(),
                INTERVAL_DAY,
                GENERAL_REQUEST_CODE,
                true
        );
        NotificationPreferences.setGeneralReminderScheduled(context, true);
    }

    public static void cancelDailyTip(Context context) {
        NotificationScheduler.cancel(context, GENERAL_REQUEST_CODE);
        NotificationPreferences.setGeneralReminderScheduled(context, false);
    }

    public static String pickRandomTip(Context context) {
        String[] tips = DEFAULT_TIPS;
        try {
            tips = context.getResources().getStringArray(R.array.general_tip_messages);
        } catch (Exception ignored) {
        }
        if (tips == null || tips.length == 0) {
            tips = DEFAULT_TIPS;
        }
        return tips[RANDOM.nextInt(tips.length)];
    }
}