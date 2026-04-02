package com.android.tomatoapp.detection.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.android.tomatoapp.core.network.LocalDataManager;

public class DetectionHistoryManager {
    private static final String TAG = "DetectionHistoryManager";
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
                                  int phase,
                                  String programId) {
        // Overloaded method for backward compatibility
        addHistory(context, imageUri, title, accuracy, description, symptoms, cause, cure, 
                  prevention, pestTitle, pestDescription, pestImageUri, cultivar, phase, 
                  programId, "", "");
    }
    
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
                                  int phase,
                                  String programId,
                                  String topPredictions,
                                  String confidenceWarning) {

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
            entry.put("programId", programId != null ? programId : "");
            entry.put("topPredictions", topPredictions != null ? topPredictions : "");
            entry.put("confidenceWarning", confidenceWarning != null ? confidenceWarning : "");

            historyArray.put(entry);
            prefs.edit().putString(KEY_HISTORY, historyArray.toString()).apply();
            
            // Also save to local database
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                LocalDataManager.getInstance(context).saveDetectionHistory(
                        currentUser.getUid(),
                        programId,
                        imageUri,
                        title,
                        accuracy,
                        description,
                        symptoms,
                        cause,
                        cure,
                        prevention,
                        pestTitle,
                        pestDescription,
                        pestImageUri,
                        entry.optLong("timestamp", System.currentTimeMillis()),
                        cultivar,
                        phase
                );
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error adding detection history", e);
        }
    }

    public static ArrayList<JSONObject> getHistory(Context context) {
        ArrayList<JSONObject> historyList = new ArrayList<>();
        
        // Try to get from local database first (if user is logged in)
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            try {
                List<DetectionHistoryEntity> entities = LocalDataManager.getInstance(context).getDetectionHistoryFromLocal(currentUser.getUid());
                for (DetectionHistoryEntity entity : entities) {
                    JSONObject entry = new JSONObject();
                    entry.put("imageUri", entity.imageUri);
                    entry.put("disease", entity.disease);
                    entry.put("accuracy", entity.accuracy);
                    entry.put("description", entity.description);
                    entry.put("symptoms", entity.symptoms);
                    entry.put("cause", entity.cause);
                    entry.put("cure", entity.cure);
                    entry.put("prevention", entity.prevention);
                    entry.put("pestTitle", entity.pestTitle);
                    entry.put("pestDescription", entity.pestDescription);
                    entry.put("pestImageUri", entity.pestImageUri);
                    entry.put("timestamp", entity.timestamp);
                    entry.put("cultivar", entity.cultivar);
                    entry.put("phase", entity.phase);
                    historyList.add(entry);
                }
                
                // If we got data from local database, return it
                if (!historyList.isEmpty()) {
                    return historyList;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error retrieving detection history from local database", e);
            }
        }
        
        // Fallback to SharedPreferences (for backward compatibility)
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyJson = prefs.getString(KEY_HISTORY, "[]");

        try {
            JSONArray historyArray = new JSONArray(historyJson);
            for (int i = 0; i < historyArray.length(); i++) {
                historyList.add(historyArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing detection history JSON", e);
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
            Log.e(TAG, "Error removing detection from history", e);
        }
    }
}
