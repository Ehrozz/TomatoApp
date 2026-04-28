package com.android.tomatoapp.core.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.android.tomatoapp.R;
import com.android.tomatoapp.analytics.ui.AnalyticsActivity;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.settings.ui.SettingsActivity;
import com.android.tomatoapp.workprogram.ui.WorkProgramSelection;
import com.android.tomatoapp.workprogram.ui.Workprogram;
import com.android.tomatoapp.task.ui.DailyTask;
import com.android.tomatoapp.monitoring.ui.PlantMonitoringActivity;
import com.android.tomatoapp.detection.ui.CameraInterface;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

/**
 * Base activity that provides Bottom Navigation functionality.
 * Activities should extend this class and call setupBottomNavigation() in onCreate().
 */
public abstract class BaseBottomNavActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    private boolean wasOffline = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply language setting
        String language = SettingsPreferences.getLanguage(newBase);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme setting before super.onCreate
        applyTheme();
        super.onCreate(savedInstanceState);
        
        // Check initial connectivity state
        wasOffline = !LocalDataManager.isOnline(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if we just came back online
        boolean isOnline = LocalDataManager.isOnline(this);
        if (wasOffline && isOnline) {
            // We just came back online, sync local data to Firebase
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                LocalDataManager.getInstance(this).processSyncQueue(this, currentUser.getUid());
            }
        }
        wasOffline = !isOnline;
        
        // Ensure the correct item is selected in bottom nav
        updateBottomNavSelection();
    }

    private void applyTheme() {
        String theme = SettingsPreferences.getTheme(this);
        if (theme.equals(SettingsPreferences.THEME_DARK)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme.equals(SettingsPreferences.THEME_LIGHT)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    protected void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) return;

        // Set global item tint and text color to Black (#0a0a0a) for visibility
        android.content.res.ColorStateList blackColorStateList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0a0a0a"));
        bottomNavigationView.setItemIconTintList(blackColorStateList);
        bottomNavigationView.setItemTextColor(blackColorStateList);
        
        // Ensure all labels and icons are visible at all times
        bottomNavigationView.setLabelVisibilityMode(com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                if (!(this instanceof MainActivity)) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                return true;
            } else if (itemId == R.id.nav_scan) {
                if (!(this instanceof CameraInterface)) {
                    startActivity(new Intent(this, CameraInterface.class));
                    finish();
                }
                return true;
            } else if (itemId == R.id.nav_analytics) {
                if (!(this instanceof AnalyticsActivity)) {
                    startActivity(new Intent(this, AnalyticsActivity.class));
                    finish();
                }
                return true;
            } else if (itemId == R.id.nav_workprogram) {
                // Fix: Allow staying in Workprogram, DailyTask, or Monitoring screens
                if (!(this instanceof WorkProgramSelection || this instanceof Workprogram || this instanceof DailyTask || this instanceof PlantMonitoringActivity)) {
                    startActivity(new Intent(this, WorkProgramSelection.class));
                    finish();
                }
                return true;
            } else if (itemId == R.id.nav_settings) {
                if (!(this instanceof SettingsActivity)) {
                    startActivity(new Intent(this, SettingsActivity.class));
                    finish();
                }
                return true;
            }
            return false;
        });
    }

    private void updateBottomNavSelection() {
        if (bottomNavigationView == null) return;
        
        if (this instanceof MainActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (this instanceof CameraInterface) {
            bottomNavigationView.setSelectedItemId(R.id.nav_scan);
        } else if (this instanceof AnalyticsActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_analytics);
        } else if (this instanceof WorkProgramSelection || this instanceof Workprogram || this instanceof DailyTask || this instanceof PlantMonitoringActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_workprogram);
        } else if (this instanceof SettingsActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        }
    }
}
