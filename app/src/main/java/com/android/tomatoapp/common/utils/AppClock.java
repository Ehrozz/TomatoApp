package com.android.tomatoapp.common.utils;

import android.content.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class AppClock {
    private static final String PREFS = "app_clock";
    private static final String DATE_KEY = "selected_date";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private AppClock() {}

    public static LocalDate today(Context context) {
        String value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(DATE_KEY, null);
        if (value != null) {
            try {
                return LocalDate.parse(value, FORMATTER);
            } catch (RuntimeException ignored) {
                // Reset invalid persisted state.
            }
        }
        LocalDate current = LocalDate.now();
        setToday(context, current);
        return current;
    }

    public static void setToday(Context context, LocalDate date) {
        if (date != null) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString(DATE_KEY, date.format(FORMATTER))
                    .apply();
        }
    }
}