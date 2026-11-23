package com.android.tomatoapp;

import android.app.Application;
import android.util.Log;

import com.android.tomatoapp.core.database.AppDatabase;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class TomatoAppApplication extends Application {
    private static final String TAG = "TomatoAppApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate");

        // Enable Firebase Realtime Database offline persistence.
        // Must execute before other Realtime Database operations.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Initialize local Room database
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

