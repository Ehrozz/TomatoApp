package com.android.tomatoapp.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.TaskStackBuilder;

import com.android.tomatoapp.AppNotificationManager;
import com.android.tomatoapp.DetectionResults;

import java.util.Map;

/**
 * High-level helpers for specific notification scenarios.
 */
public final class NotificationUseCases {

    private NotificationUseCases() {
    }

    public static void notifyDiseaseDetection(Context context,
                                              String imageUri,
                                              Map<String, String> detectionResults,
                                              String cultivar,
                                              int phase) {
        if (!NotificationPreferences.areMonitoringNotificationsEnabled(context)) return;
        if (!NotificationPermissionHelper.hasPermission(context)) return;

        String diseaseName = detectionResults != null
                ? detectionResults.getOrDefault("title", "Detection")
                : "Detection";

        Intent target = new Intent(context, DetectionResults.class);
        if (imageUri != null) {
            target.putExtra("imageUri", imageUri);
        }
        if (detectionResults != null) {
            for (Map.Entry<String, String> entry : detectionResults.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    target.putExtra(entry.getKey(), entry.getValue());
                }
            }
        }
        target.putExtra("detectionCultivar", cultivar);
        target.putExtra("detectionPhase", phase);

        PendingIntent pi = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(target)
                .getPendingIntent(
                        (int) System.currentTimeMillis(),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        String title = "Possible " + diseaseName;
        String message = "Detected on " + cultivar + " (Phase " + phase + "). Tap for details.";

        NotificationHelper.showNotification(
                context,
                NotificationChannels.CHANNEL_MONITORING,
                (int) (System.currentTimeMillis() & 0xFFFFFF),
                title,
                message,
                pi
        );
        
        // Also create in-app notification
        String programId = detectionResults != null ? detectionResults.get("programId") : null;
        if (programId == null || programId.isEmpty()) {
            programId = "unknown";
        }
        AppNotificationManager.addDetectionNotification(context, diseaseName, programId);
    }
}

