package com.android.tomatoapp;

import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * Activity displaying comprehensive user manual for TomatoApp.
 * Provides documentation for all major features and functionality.
 */
public class UserManualActivity extends BaseDrawerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);
        
        setupDrawer();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Manual");
        }
    }
}

