package com.android.tomatoapp.notifications;

import android.app.Activity;
import android.content.Context;

/**
 * Convenience wrapper for notification permission handling.
 */
public final class NotificationPermissionHelper {

    private NotificationPermissionHelper() {
    }

    public static boolean ensurePermission(Activity activity) {
        if (NotificationHelper.hasPermission(activity)) {
            return true;
        }
        NotificationHelper.requestPermission(activity);
        return false;
    }

    public static boolean hasPermission(Context context) {
        return NotificationHelper.hasPermission(context);
    }
}

