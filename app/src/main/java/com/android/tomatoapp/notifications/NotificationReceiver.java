package com.android.tomatoapp.notifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.TaskStackBuilder;

import com.android.tomatoapp.common.managers.AppNotificationManager;
import com.android.tomatoapp.core.ui.MainActivity;
import com.android.tomatoapp.settings.data.SettingsPreferences;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Receives scheduled alarms and displays notifications.
 */
public class NotificationReceiver extends BroadcastReceiver {

    public static final String TYPE_TASK = "task";
    public static final String TYPE_MONITORING = "monitoring";
    public static final String TYPE_GENERAL = "general";

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra(NotificationScheduler.EXTRA_TYPE);
        if (!isTypeEnabled(context, type)) {
            return; // disabled: do not re-arm, chain ends
        }

        // Arm tomorrow's alarm first so the chain never breaks.
        NotificationScheduler.rescheduleNext(context, intent);

        String channelId = intent.getStringExtra(NotificationScheduler.EXTRA_CHANNEL_ID);
        String title = intent.getStringExtra(NotificationScheduler.EXTRA_TITLE);
        String message = intent.getStringExtra(NotificationScheduler.EXTRA_MESSAGE);
        boolean randomMessage = intent.getBooleanExtra(NotificationScheduler.EXTRA_RANDOM_MESSAGE, false);
        int notificationId = intent.getIntExtra(NotificationScheduler.EXTRA_NOTIFICATION_ID, (int) System.currentTimeMillis());

        if (randomMessage && TYPE_GENERAL.equals(type)) {
            message = GeneralUpdateScheduler.pickRandomTip(context);
        }

        // In-app list always gets the entry
        createInAppNotification(context, title, message, intent);

        // Tray notification is skipped during quiet hours or without permission
        if (isQuietNow(context) || !NotificationHelper.hasPermission(context)) {
            return;
        }

        PendingIntent contentIntent = buildContentIntent(context, intent);
        NotificationHelper.showNotification(
                context, channelId, notificationId, title, message, contentIntent);
    }

    private void createInAppNotification(Context context, String title, String message, Intent intent) {
        String targetActivity = intent.getStringExtra(NotificationScheduler.EXTRA_TARGET_ACTIVITY);
        Bundle extras = intent.getBundleExtra(NotificationScheduler.EXTRA_TARGET_EXTRAS);

        JSONObject json = new JSONObject();
        if (extras != null) {
            for (String key : extras.keySet()) {
                try {
                    json.put(key, String.valueOf(extras.get(key)));
                } catch (Exception ignored) {
                }
            }
        }

        // Keep the FULL class name so navigation can resolve it
        String target = TextUtils.isEmpty(targetActivity) ? MainActivity.class.getName() : targetActivity;

        AppNotificationManager.addSimpleNotification(
                context,
                title != null ? title : "TomatoApp",
                message != null ? message : "",
                target,
                json.toString()
        );
    }

    private boolean isQuietNow(Context context) {
        if (!SettingsPreferences.isQuietHoursEnabled(context)) return false;
        Calendar now = Calendar.getInstance();
        int cur = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int start = SettingsPreferences.getQuietHoursStartHour(context) * 60
                + SettingsPreferences.getQuietHoursStartMinute(context);
        int end = SettingsPreferences.getQuietHoursEndHour(context) * 60
                + SettingsPreferences.getQuietHoursEndMinute(context);
        if (start == end) return false;
        return start < end ? (cur >= start && cur < end) : (cur >= start || cur < end);
    }

    private boolean isTypeEnabled(Context context, String type) {
        if (TYPE_TASK.equals(type)) {
            return NotificationPreferences.areTaskNotificationsEnabled(context);
        } else if (TYPE_MONITORING.equals(type)) {
            return NotificationPreferences.areMonitoringNotificationsEnabled(context);
        } else if (TYPE_GENERAL.equals(type)) {
            return NotificationPreferences.areGeneralNotificationsEnabled(context);
        }
        return true;
    }

    private PendingIntent buildContentIntent(Context context, Intent alarmIntent) {
        String targetName = alarmIntent.getStringExtra(NotificationScheduler.EXTRA_TARGET_ACTIVITY);
        Bundle extras = alarmIntent.getBundleExtra(NotificationScheduler.EXTRA_TARGET_EXTRAS);
        Intent targetIntent;
        if (!TextUtils.isEmpty(targetName)) {
            try {
                targetIntent = new Intent(context, Class.forName(targetName));
            } catch (ClassNotFoundException e) {
                targetIntent = new Intent(context, MainActivity.class);
            }
        } else {
            targetIntent = new Intent(context, MainActivity.class);
        }

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            targetIntent.putExtras(extras);
        }

        return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(targetIntent)
                .getPendingIntent(
                        (int) System.currentTimeMillis(),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
    }
}