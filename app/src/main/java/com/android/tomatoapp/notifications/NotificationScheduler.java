package com.android.tomatoapp.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * Utility for scheduling and cancelling notification alarms.
 */
public final class NotificationScheduler {

    public static final String EXTRA_TYPE = "extra_notification_type";
    public static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_TARGET_ACTIVITY = "extra_target_activity";
    public static final String EXTRA_TARGET_EXTRAS = "extra_target_extras";
    public static final String EXTRA_RANDOM_MESSAGE = "extra_random_message";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    public static final String EXTRA_INTERVAL = "extra_interval";
    public static final String EXTRA_TRIGGER_AT = "extra_trigger_at";

    // Must be >= 10 minutes, otherwise Android 12+ treats it as an "exact" alarm
    // and requires the SCHEDULE_EXACT_ALARM permission.
    private static final long WINDOW_MS = 15 * 60 * 1000L;

    private NotificationScheduler() {
    }

    public static void scheduleExact(Context context,
                                     String type,
                                     String channelId,
                                     String title,
                                     String message,
                                     Class<?> targetActivity,
                                     @Nullable Bundle targetExtras,
                                     long triggerAtMillis,
                                     int requestCode) {
        PendingIntent pendingIntent = buildPendingIntent(
                context, type, channelId, title, message, targetActivity,
                targetExtras, requestCode, false, 0L, triggerAtMillis);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    /** Fires within a 15 min window at triggerAtMillis, then repeats every intervalMillis. */
    public static void scheduleRepeating(Context context,
                                         String type,
                                         String channelId,
                                         String title,
                                         String message,
                                         Class<?> targetActivity,
                                         @Nullable Bundle targetExtras,
                                         long triggerAtMillis,
                                         long intervalMillis,
                                         int requestCode,
                                         boolean randomMessage) {
        PendingIntent pendingIntent = buildPendingIntent(
                context, type, channelId, title, message, targetActivity,
                targetExtras, requestCode, randomMessage, intervalMillis, triggerAtMillis);
        arm(context, triggerAtMillis, pendingIntent);
    }

    /** Called by NotificationReceiver after an alarm fires to arm the next day's alarm. */
    public static void rescheduleNext(Context context, Intent fired) {
        long interval = fired.getLongExtra(EXTRA_INTERVAL, 0L);
        int requestCode = fired.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        if (interval <= 0 || requestCode < 0 || fired.getExtras() == null) return;

        long now = System.currentTimeMillis();
        long nominal = fired.getLongExtra(EXTRA_TRIGGER_AT, now);
        do {
            nominal += interval;
        } while (nominal <= now);

        Intent next = new Intent(context, NotificationReceiver.class);
        next.putExtras(fired.getExtras());
        next.putExtra(EXTRA_TRIGGER_AT, nominal);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, requestCode, next,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        arm(context, nominal, pi);
    }

    public static void cancel(Context context, int requestCode) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent == null) return; // nothing scheduled
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pendingIntent);
        }
        pendingIntent.cancel();
    }

    private static void arm(Context context, long triggerAtMillis, PendingIntent pendingIntent) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        manager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, WINDOW_MS, pendingIntent);
    }

    private static PendingIntent buildPendingIntent(Context context,
                                                    String type,
                                                    String channelId,
                                                    String title,
                                                    String message,
                                                    Class<?> targetActivity,
                                                    @Nullable Bundle targetExtras,
                                                    int requestCode,
                                                    boolean randomMessage,
                                                    long intervalMillis,
                                                    long triggerAtMillis) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_RANDOM_MESSAGE, randomMessage);
        intent.putExtra(EXTRA_NOTIFICATION_ID, requestCode);
        intent.putExtra(EXTRA_INTERVAL, intervalMillis);
        intent.putExtra(EXTRA_TRIGGER_AT, triggerAtMillis);
        if (targetActivity != null) {
            intent.putExtra(EXTRA_TARGET_ACTIVITY, targetActivity.getName());
        }
        if (targetExtras != null) {
            intent.putExtra(EXTRA_TARGET_EXTRAS, targetExtras);
        }
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}