package com.android.tomatoapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Utility class for managing app settings stored in SharedPreferences.
 */
public final class SettingsPreferences {
    
    private static final String PREFS_NAME = "app_settings";
    
    // Keys for preferences
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME = "theme";
    private static final String KEY_WEATHER_UNIT = "weather_unit";
    private static final String KEY_DEFAULT_CULTIVAR = "default_cultivar";
    private static final String KEY_MEASUREMENT_UNIT = "measurement_unit";
    private static final String KEY_DATE_FORMAT = "date_format";
    private static final String KEY_NOTIFICATION_SOUND = "notification_sound";
    private static final String KEY_NOTIFICATION_HOUR = "notification_hour";
    private static final String KEY_NOTIFICATION_MINUTE = "notification_minute";
    private static final String KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled";
    private static final String KEY_QUIET_HOURS_START_HOUR = "quiet_hours_start_hour";
    private static final String KEY_QUIET_HOURS_START_MINUTE = "quiet_hours_start_minute";
    private static final String KEY_QUIET_HOURS_END_HOUR = "quiet_hours_end_hour";
    private static final String KEY_QUIET_HOURS_END_MINUTE = "quiet_hours_end_minute";
    
    // Default values
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_FILIPINO = "fil";
    
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";
    
    public static final String WEATHER_UNIT_CELSIUS = "celsius";
    public static final String WEATHER_UNIT_FAHRENHEIT = "fahrenheit";
    
    public static final String MEASUREMENT_UNIT_METRIC = "metric";
    public static final String MEASUREMENT_UNIT_IMPERIAL = "imperial";
    public static final String MEASUREMENT_UNIT_HECTARE = "hectare";
    
    public static final String DATE_FORMAT_DDMMYYYY = "dd/MM/yyyy";
    public static final String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";
    public static final String DATE_FORMAT_YYYYMMDD = "yyyy-MM-dd";
    
    private SettingsPreferences() {
    }
    
    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Language
    public static String getLanguage(Context context) {
        return prefs(context).getString(KEY_LANGUAGE, LANGUAGE_ENGLISH);
    }
    
    public static void setLanguage(Context context, String language) {
        prefs(context).edit().putString(KEY_LANGUAGE, language).apply();
    }
    
    // Theme
    public static String getTheme(Context context) {
        return prefs(context).getString(KEY_THEME, THEME_SYSTEM);
    }
    
    public static void setTheme(Context context, String theme) {
        prefs(context).edit().putString(KEY_THEME, theme).apply();
    }
    
    // Weather Unit
    public static String getWeatherUnit(Context context) {
        return prefs(context).getString(KEY_WEATHER_UNIT, WEATHER_UNIT_CELSIUS);
    }
    
    public static void setWeatherUnit(Context context, String unit) {
        prefs(context).edit().putString(KEY_WEATHER_UNIT, unit).apply();
    }
    
    // Default Cultivar
    public static String getDefaultCultivar(Context context) {
        return prefs(context).getString(KEY_DEFAULT_CULTIVAR, "");
    }
    
    public static void setDefaultCultivar(Context context, String cultivar) {
        prefs(context).edit().putString(KEY_DEFAULT_CULTIVAR, cultivar).apply();
    }
    
    // Measurement Unit
    public static String getMeasurementUnit(Context context) {
        return prefs(context).getString(KEY_MEASUREMENT_UNIT, MEASUREMENT_UNIT_METRIC);
    }
    
    public static void setMeasurementUnit(Context context, String unit) {
        prefs(context).edit().putString(KEY_MEASUREMENT_UNIT, unit).apply();
    }
    
    // Date Format
    public static String getDateFormat(Context context) {
        return prefs(context).getString(KEY_DATE_FORMAT, DATE_FORMAT_DDMMYYYY);
    }
    
    public static void setDateFormat(Context context, String format) {
        prefs(context).edit().putString(KEY_DATE_FORMAT, format).apply();
    }
    
    // Notification Sound
    public static String getNotificationSound(Context context) {
        return prefs(context).getString(KEY_NOTIFICATION_SOUND, "default");
    }
    
    public static void setNotificationSound(Context context, String sound) {
        prefs(context).edit().putString(KEY_NOTIFICATION_SOUND, sound).apply();
    }
    
    // Notification Time
    public static int getNotificationHour(Context context) {
        return prefs(context).getInt(KEY_NOTIFICATION_HOUR, 8); // Default 8 AM
    }
    
    public static void setNotificationHour(Context context, int hour) {
        prefs(context).edit().putInt(KEY_NOTIFICATION_HOUR, hour).apply();
    }
    
    public static int getNotificationMinute(Context context) {
        return prefs(context).getInt(KEY_NOTIFICATION_MINUTE, 0);
    }
    
    public static void setNotificationMinute(Context context, int minute) {
        prefs(context).edit().putInt(KEY_NOTIFICATION_MINUTE, minute).apply();
    }
    
    // Quiet Hours
    public static boolean isQuietHoursEnabled(Context context) {
        return prefs(context).getBoolean(KEY_QUIET_HOURS_ENABLED, false);
    }
    
    public static void setQuietHoursEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).apply();
    }
    
    public static int getQuietHoursStartHour(Context context) {
        return prefs(context).getInt(KEY_QUIET_HOURS_START_HOUR, 22); // Default 10 PM
    }
    
    public static void setQuietHoursStartHour(Context context, int hour) {
        prefs(context).edit().putInt(KEY_QUIET_HOURS_START_HOUR, hour).apply();
    }
    
    public static int getQuietHoursStartMinute(Context context) {
        return prefs(context).getInt(KEY_QUIET_HOURS_START_MINUTE, 0);
    }
    
    public static void setQuietHoursStartMinute(Context context, int minute) {
        prefs(context).edit().putInt(KEY_QUIET_HOURS_START_MINUTE, minute).apply();
    }
    
    public static int getQuietHoursEndHour(Context context) {
        return prefs(context).getInt(KEY_QUIET_HOURS_END_HOUR, 7); // Default 7 AM
    }
    
    public static void setQuietHoursEndHour(Context context, int hour) {
        prefs(context).edit().putInt(KEY_QUIET_HOURS_END_HOUR, hour).apply();
    }
    
    public static int getQuietHoursEndMinute(Context context) {
        return prefs(context).getInt(KEY_QUIET_HOURS_END_MINUTE, 0);
    }
    
    public static void setQuietHoursEndMinute(Context context, int minute) {
        prefs(context).edit().putInt(KEY_QUIET_HOURS_END_MINUTE, minute).apply();
    }
    
    /**
     * Gets a SimpleDateFormat instance based on user's date format preference.
     * @param context The context
     * @return SimpleDateFormat instance with user's preferred format
     */
    public static SimpleDateFormat getDateFormatInstance(Context context) {
        String format = getDateFormat(context);
        return new SimpleDateFormat(format, Locale.getDefault());
    }
    
    /**
     * Gets a SimpleDateFormat instance for parsing dates (always uses yyyy-MM-dd for parsing).
     * @return SimpleDateFormat instance for parsing
     */
    public static SimpleDateFormat getDateParseFormat() {
        return new SimpleDateFormat(DATE_FORMAT_YYYYMMDD, Locale.US);
    }
}

