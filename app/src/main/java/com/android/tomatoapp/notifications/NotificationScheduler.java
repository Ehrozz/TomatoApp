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
                context,
                type,
                channelId,
                title,
                message,
                targetActivity,
                targetExtras,
                requestCode,
                false
        );
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

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
                context,
                type,
                channelId,
                title,
                message,
                targetActivity,
                targetExtras,
                requestCode,
                randomMessage
        );
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        manager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                intervalMillis,
                pendingIntent
        );
    }

    public static void cancel(Context context, int requestCode) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }

    private static PendingIntent buildPendingIntent(Context context,
                                                    String type,
                                                    String channelId,
                                                    String title,
                                                    String message,
                                                    Class<?> targetActivity,
                                                    @Nullable Bundle targetExtras,
                                                    int requestCode,
                                                    boolean randomMessage) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_RANDOM_MESSAGE, randomMessage);
        intent.putExtra(EXTRA_NOTIFICATION_ID, requestCode);
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

