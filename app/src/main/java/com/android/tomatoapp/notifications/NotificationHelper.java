package com.android.tomatoapp.notifications;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.tomatoapp.R;

/**
 * Helper for showing notifications and handling runtime permission.
 */
public final class NotificationHelper {

    public static final int REQUEST_POST_NOTIFICATIONS = 4010;

    private NotificationHelper() {
    }

    public static boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(activity)) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS
                );
            }
        }
    }

    public static void showNotification(Context context,
                                        String channelId,
                                        int notificationId,
                                        String title,
                                        String message,
                                        PendingIntent contentIntent) {
        NotificationChannels.ensureCreated(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (contentIntent != null) {
            builder.setContentIntent(contentIntent);
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(notificationId);
        }
    }
}

