package com.android.tomatoapp.notifications;

import android.content.Context;

/** Single entry point that (re)schedules every alarm according to the current toggles. */
public final class NotificationScheduleManager {

    private NotificationScheduleManager() {
    }

    public static void rescheduleAll(Context context) {
        Context app = context.getApplicationContext();
        GeneralUpdateScheduler.ensureDailyTipScheduled(app);
        MorningRemindersScheduler.ensureMorningRemindersScheduled(app);
        TaskNotificationScheduler.ensureTaskNotificationsScheduled(app);
    }
}