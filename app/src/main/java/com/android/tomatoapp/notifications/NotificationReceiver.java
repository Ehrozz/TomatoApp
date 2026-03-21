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
            return;
        }

        String channelId = intent.getStringExtra(NotificationScheduler.EXTRA_CHANNEL_ID);
        String title = intent.getStringExtra(NotificationScheduler.EXTRA_TITLE);
        String message = intent.getStringExtra(NotificationScheduler.EXTRA_MESSAGE);
        boolean randomMessage = intent.getBooleanExtra(NotificationScheduler.EXTRA_RANDOM_MESSAGE, false);
        int notificationId = intent.getIntExtra(NotificationScheduler.EXTRA_NOTIFICATION_ID, (int) System.currentTimeMillis());

        if (randomMessage && TYPE_GENERAL.equals(type)) {
            message = GeneralUpdateScheduler.pickRandomTip(context);
        }

        PendingIntent contentIntent = buildContentIntent(context, intent);
        if (!NotificationHelper.hasPermission(context)) {
            // Cannot show notification without permission
            return;
        }
        NotificationHelper.showNotification(
                context,
                channelId,
                notificationId,
                title,
                message,
                contentIntent
        );
        
        // Also create in-app notification
        createInAppNotification(context, type, title, message, intent);
    }
    
    private void createInAppNotification(Context context, String type, String title, String message, Intent intent) {
        String targetActivity = intent.getStringExtra(NotificationScheduler.EXTRA_TARGET_ACTIVITY);
        Bundle extras = intent.getBundleExtra(NotificationScheduler.EXTRA_TARGET_EXTRAS);
        
        // Build extra data JSON
        StringBuilder extraDataJson = new StringBuilder("{");
        if (extras != null) {
            boolean first = true;
            for (String key : extras.keySet()) {
                if (!first) extraDataJson.append(",");
                Object value = extras.get(key);
                extraDataJson.append("\"").append(key).append("\":\"");
                if (value != null) {
                    extraDataJson.append(value.toString().replace("\"", "\\\""));
                }
                extraDataJson.append("\"");
                first = false;
            }
        }
        extraDataJson.append("}");
        
        // Determine target activity class name
        String targetActivityName = "MainActivity";
        if (!TextUtils.isEmpty(targetActivity)) {
            try {
                // Extract class name from full path
                if (targetActivity.contains(".")) {
                    String[] parts = targetActivity.split("\\.");
                    targetActivityName = parts[parts.length - 1];
                } else {
                    targetActivityName = targetActivity;
                }
            } catch (Exception e) {
                targetActivityName = "MainActivity";
            }
        }
        
        // Create in-app notification
        AppNotificationManager.addSimpleNotification(
                context,
                title,
                message,
                targetActivityName,
                extraDataJson.toString()
        );
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
                Class<?> targetClass = Class.forName(targetName);
                targetIntent = new Intent(context, targetClass);
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

