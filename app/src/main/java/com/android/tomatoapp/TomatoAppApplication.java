package com.android.tomatoapp;

import android.app.Application;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TomatoAppApplication extends Application {
    private static final String TAG = "TomatoAppApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate");
        
        // Initialize database
        AppDatabase.getInstance(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application onTerminate - saving data");
        
        // Save all data before app closes
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            LocalDataManager manager = LocalDataManager.getInstance(this);
            // Sync settings
            manager.syncSettingsToLocal(this, currentUser.getUid());
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "Low memory warning");
    }
}

