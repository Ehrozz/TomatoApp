package com.android.tomatoapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages tutorial state and progression for new users.
 * Handles showing tutorial after terms acceptance and allows replay from Settings.
 */
public class TutorialManager {
    
    private static final String PREFS_NAME = "TutorialPrefs";
    private static final String KEY_TUTORIAL_COMPLETED = "tutorial_completed";
    private static final String KEY_TUTORIAL_VERSION = "tutorial_version";
    private static final int CURRENT_TUTORIAL_VERSION = 1;
    
    /**
     * Check if tutorial should be shown for a user.
     * @param context The application context
     * @param userId The user ID
     * @return true if tutorial should be shown, false otherwise
     */
    public static boolean shouldShowTutorial(Context context, String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String completedKey = KEY_TUTORIAL_COMPLETED + "_" + userId;
        String versionKey = KEY_TUTORIAL_VERSION + "_" + userId;
        
        boolean completed = prefs.getBoolean(completedKey, false);
        int version = prefs.getInt(versionKey, 0);
        
        // If tutorial version has changed, show again
        if (version < CURRENT_TUTORIAL_VERSION) {
            return true;
        }
        
        return !completed;
    }
    
    /**
     * Check if tutorial has been completed for a user.
     * @param context The application context
     * @param userId The user ID
     * @return true if tutorial is completed, false otherwise
     */
    public static boolean isTutorialCompleted(Context context, String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String completedKey = KEY_TUTORIAL_COMPLETED + "_" + userId;
        String versionKey = KEY_TUTORIAL_VERSION + "_" + userId;
        
        boolean completed = prefs.getBoolean(completedKey, false);
        int version = prefs.getInt(versionKey, 0);
        
        return completed && version >= CURRENT_TUTORIAL_VERSION;
    }
    
    /**
     * Start the tutorial for a user.
     * @param context The application context
     * @param userId The user ID
     */
    public static void startTutorial(Context context, String userId) {
        if (context == null || userId == null || userId.isEmpty()) {
            return;
        }
        
        TutorialDialog dialog = new TutorialDialog(context, userId);
        dialog.show();
    }
    
    /**
     * Mark tutorial as completed for a user.
     * @param context The application context
     * @param userId The user ID
     */
    public static void markTutorialCompleted(Context context, String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        String completedKey = KEY_TUTORIAL_COMPLETED + "_" + userId;
        String versionKey = KEY_TUTORIAL_VERSION + "_" + userId;
        
        editor.putBoolean(completedKey, true);
        editor.putInt(versionKey, CURRENT_TUTORIAL_VERSION);
        editor.apply();
    }
    
    /**
     * Reset tutorial for a user (allows replay).
     * @param context The application context
     * @param userId The user ID
     */
    public static void resetTutorial(Context context, String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        String completedKey = KEY_TUTORIAL_COMPLETED + "_" + userId;
        String versionKey = KEY_TUTORIAL_VERSION + "_" + userId;
        
        editor.remove(completedKey);
        editor.remove(versionKey);
        editor.apply();
    }
}

