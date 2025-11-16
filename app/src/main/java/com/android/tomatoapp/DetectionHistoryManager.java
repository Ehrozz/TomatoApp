package com.android.tomatoapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DetectionHistoryManager {
    private static final String PREF_NAME = "detection_history";
    private static final String KEY_HISTORY = "history";

    public static void addHistory(Context context,
                                  String imageUri,
                                  String title,
                                  String accuracy,
                                  String description,
                                  String symptoms,
                                  String cause,
                                  String cure,
                                  String prevention,
                                  String pestTitle,
                                  String pestDescription,
                                  String pestImageUri,
                                  String cultivar,
                                  int phase) {

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyJson = prefs.getString(KEY_HISTORY, "[]");

        try {
            JSONArray historyArray = new JSONArray(historyJson);
            JSONObject entry = new JSONObject();
            entry.put("imageUri", imageUri);
            entry.put("disease", title);
            entry.put("accuracy", accuracy);
            entry.put("description", description);
            entry.put("symptoms", symptoms);
            entry.put("cause", cause);
            entry.put("cure", cure);
            entry.put("prevention", prevention);
            entry.put("pestTitle", pestTitle);
            entry.put("pestDescription", pestDescription);
            entry.put("pestImageUri", pestImageUri);
            entry.put("timestamp", System.currentTimeMillis());
            entry.put("cultivar", cultivar);
            entry.put("phase", phase);

            historyArray.put(entry);
            prefs.edit().putString(KEY_HISTORY, historyArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<JSONObject> getHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyJson = prefs.getString(KEY_HISTORY, "[]");
        ArrayList<JSONObject> historyList = new ArrayList<>();

        try {
            JSONArray historyArray = new JSONArray(historyJson);
            for (int i = 0; i < historyArray.length(); i++) {
                historyList.add(historyArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return historyList;
    }

    public static void clearHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_HISTORY).apply();
    }

    public static void removeDetection(Context context, JSONObject entryToRemove) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyJson = prefs.getString(KEY_HISTORY, "[]");

        try {
            JSONArray historyArray = new JSONArray(historyJson);
            JSONArray newArray = new JSONArray();

            // Find matching entry by comparing key fields
            String targetImageUri = entryToRemove.optString("imageUri", "");
            String targetDisease = entryToRemove.optString("disease", "");
            long targetTimestamp = entryToRemove.optLong("timestamp", 0);

            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject entry = historyArray.getJSONObject(i);
                String imageUri = entry.optString("imageUri", "");
                String disease = entry.optString("disease", "");
                long timestamp = entry.optLong("timestamp", 0);

                // Only keep entries that don't match
                if (!(imageUri.equals(targetImageUri) && 
                      disease.equals(targetDisease) && 
                      timestamp == targetTimestamp)) {
                    newArray.put(entry);
                }
            }

            prefs.edit().putString(KEY_HISTORY, newArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
