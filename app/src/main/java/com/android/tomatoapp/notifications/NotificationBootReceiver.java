package com.android.tomatoapp.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Reschedules notifications after device reboot.
 */
public class NotificationBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            GeneralUpdateScheduler.ensureDailyTipScheduled(context);
        }
    }
}

