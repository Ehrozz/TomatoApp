package com.android.tomatoapp.notifications;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.tomatoapp.monitoring.ui.PlantMonitoringActivity;

import java.util.Calendar;

/**
 * Schedules reminders for plant monitoring check-ins.
 */
public final class MonitoringReminderScheduler {

    private MonitoringReminderScheduler() {
    }

    public static void scheduleFollowUp(Context context,
                                        String programId,
                                        String cultivar,
                                        int phase) {
        if (TextUtils.isEmpty(programId)) return;
        if (!NotificationPreferences.areMonitoringNotificationsEnabled(context)) return;

        int intervalDays = determineIntervalDays(phase);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, intervalDays);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Bundle extras = new Bundle();
        extras.putString(PlantMonitoringActivity.EXTRA_PROGRAM_ID, programId);
        extras.putString(PlantMonitoringActivity.EXTRA_CULTIVAR, cultivar);
        extras.putInt(PlantMonitoringActivity.EXTRA_PHASE, phase);

        String title = "Monitoring reminder";
        String message = "Time to check " + cultivar + " (Phase " + phase + ").";

        NotificationScheduler.scheduleExact(
                context,
                NotificationReceiver.TYPE_MONITORING,
                NotificationChannels.CHANNEL_MONITORING,
                title,
                message,
                PlantMonitoringActivity.class,
                extras,
                calendar.getTimeInMillis(),
                buildRequestCode(programId, phase)
        );
    }

    public static void cancelForProgram(Context context, String programId) {
        if (TextUtils.isEmpty(programId)) return;
        for (int phase = 1; phase <= 5; phase++) {
            NotificationScheduler.cancel(context, buildRequestCode(programId, phase));
        }
    }

    private static int determineIntervalDays(int phase) {
        switch (phase) {
            case 1:
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
            case 5:
            default:
                return 4;
        }
    }

    private static int buildRequestCode(String programId, int phase) {
        return ("MONITOR_" + programId + "_" + phase).hashCode();
    }
}

