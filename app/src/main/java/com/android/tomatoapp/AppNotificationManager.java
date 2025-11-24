package com.android.tomatoapp;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AppNotificationManager {
    private static final String PREF_NAME = "notifications";
    private static final String KEY_NOTIFICATIONS = "notifications";
    
    public static class AppNotification {
        public String id;
        public String title;
        public String message;
        public long timestamp;
        public String type; // "detection", "work_program", "expense", etc.
        public String targetActivity; // Activity class name to navigate to
        public String extraData; // JSON string with extra data for navigation
        
        public AppNotification() {
            this.id = String.valueOf(System.currentTimeMillis());
            this.timestamp = System.currentTimeMillis();
        }
        
        public AppNotification(String title, String message, String type, String targetActivity, String extraData) {
            this();
            this.title = title;
            this.message = message;
            this.type = type;
            this.targetActivity = targetActivity;
            this.extraData = extraData;
        }
        
        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("title", title);
            json.put("message", message);
            json.put("timestamp", timestamp);
            json.put("type", type);
            json.put("targetActivity", targetActivity);
            json.put("extraData", extraData != null ? extraData : "");
            return json;
        }
        
        public static AppNotification fromJson(JSONObject json) throws JSONException {
            AppNotification notification = new AppNotification();
            notification.id = json.optString("id", String.valueOf(System.currentTimeMillis()));
            notification.title = json.optString("title", "");
            notification.message = json.optString("message", "");
            notification.timestamp = json.optLong("timestamp", System.currentTimeMillis());
            notification.type = json.optString("type", "");
            notification.targetActivity = json.optString("targetActivity", "");
            notification.extraData = json.optString("extraData", "");
            return notification;
        }
    }
    
    /**
     * Add a new notification with simple, user-friendly messages
     */
    public static void addNotification(Context context, AppNotification notification) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String notificationsJson = prefs.getString(KEY_NOTIFICATIONS, "[]");
        
        try {
            JSONArray notificationsArray = new JSONArray(notificationsJson);
            notificationsArray.put(notification.toJson());
            prefs.edit().putString(KEY_NOTIFICATIONS, notificationsArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to create simple notification messages
     */
    public static void addSimpleNotification(Context context, String title, String message, String targetActivity, String extraData) {
        AppNotification notification = new AppNotification(title, message, "general", targetActivity, extraData);
        addNotification(context, notification);
    }
    
    /**
     * Create notification for disease detection
     */
    public static void addDetectionNotification(Context context, String diseaseName, String programId) {
        String title = "Disease Detected";
        String message = "Found " + diseaseName + " in your plants. Tap to view details.";
        String extraData = "{\"programId\":\"" + programId + "\"}";
        AppNotification notification = new AppNotification(title, message, "detection", "DetectionResults", extraData);
        addNotification(context, notification);
    }
    
    /**
     * Create notification for work program tasks
     */
    public static void addTaskNotification(Context context, String cultivar, String date, String programId) {
        String title = "Daily Tasks Ready";
        String message = "Tasks for " + cultivar + " on " + formatDate(date) + ". Tap to view.";
        String extraData = "{\"programId\":\"" + programId + "\",\"date\":\"" + date + "\",\"cultivar\":\"" + cultivar + "\"}";
        AppNotification notification = new AppNotification(title, message, "work_program", "DailyTask", extraData);
        addNotification(context, notification);
    }
    
    /**
     * Create notification for expense reminders
     */
    public static void addExpenseNotification(Context context, String programId, String cultivar) {
        String title = "Expense Reminder";
        String message = "Don't forget to log expenses for " + cultivar + ". Tap to add.";
        String extraData = "{\"programId\":\"" + programId + "\",\"cultivar\":\"" + cultivar + "\"}";
        AppNotification notification = new AppNotification(title, message, "expense", "DailyExpensesActivity", extraData);
        addNotification(context, notification);
    }
    
    /**
     * Format date for display (simple format)
     */
    private static String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "today";
        }
        try {
            // If date is in yyyy-MM-dd format, convert to readable format
            if (dateStr.length() >= 10) {
                String[] parts = dateStr.substring(0, 10).split("-");
                if (parts.length == 3) {
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    if (month >= 1 && month <= 12) {
                        return monthNames[month - 1] + " " + day;
                    }
                }
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    /**
     * Get all notifications
     */
    public static List<AppNotification> getNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String notificationsJson = prefs.getString(KEY_NOTIFICATIONS, "[]");
        List<AppNotification> notifications = new ArrayList<>();
        
        try {
            JSONArray notificationsArray = new JSONArray(notificationsJson);
            for (int i = 0; i < notificationsArray.length(); i++) {
                JSONObject json = notificationsArray.getJSONObject(i);
                notifications.add(AppNotification.fromJson(json));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return notifications;
    }
    
    /**
     * Remove a notification by ID
     */
    public static void removeNotification(Context context, String notificationId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String notificationsJson = prefs.getString(KEY_NOTIFICATIONS, "[]");
        
        try {
            JSONArray notificationsArray = new JSONArray(notificationsJson);
            JSONArray newArray = new JSONArray();
            
            for (int i = 0; i < notificationsArray.length(); i++) {
                JSONObject json = notificationsArray.getJSONObject(i);
                if (!json.optString("id", "").equals(notificationId)) {
                    newArray.put(json);
                }
            }
            
            prefs.edit().putString(KEY_NOTIFICATIONS, newArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Clear all notifications
     */
    public static void clearAllNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_NOTIFICATIONS, "[]").apply();
    }
    
    /**
     * Get notification count
     */
    public static int getNotificationCount(Context context) {
        return getNotifications(context).size();
    }
    
    /**
     * Check if there are unread notifications
     */
    public static boolean hasNotifications(Context context) {
        return getNotificationCount(context) > 0;
    }
}
