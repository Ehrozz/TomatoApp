package com.android.tomatoapp.settings.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.android.tomatoapp.notifications.GeneralUpdateScheduler;
import com.android.tomatoapp.notifications.NotificationHelper;
import com.android.tomatoapp.notifications.NotificationPermissionHelper;
import com.android.tomatoapp.notifications.NotificationPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for managing app settings and preferences.
 */
public class SettingsActivity extends BaseDrawerActivity {

    private TextInputEditText editTextLanguage;
    private TextInputEditText editTextTheme;
    private TextInputEditText editTextDefaultCultivar;
    private TextInputEditText editTextWeatherUnit;
    private TextInputEditText editTextMeasurementUnit;
    private TextInputEditText editTextDateFormat;
    
    private TextInputLayout layoutLanguage;
    private TextInputLayout layoutTheme;
    private TextInputLayout layoutDefaultCultivar;
    private TextInputLayout layoutWeatherUnit;
    private TextInputLayout layoutMeasurementUnit;
    private TextInputLayout layoutDateFormat;
    
    private MaterialSwitch switchTaskNotifications;
    private MaterialSwitch switchMonitoringNotifications;
    private MaterialSwitch switchGeneralNotifications;
    private MaterialSwitch switchQuietHours;
    
    private TextInputEditText editTextNotificationSound;
    private TextInputEditText editTextNotificationTime;
    private TextInputEditText editTextQuietHoursStart;
    private TextInputEditText editTextQuietHoursEnd;
    
    private TextInputLayout layoutNotificationSound;
    private TextInputLayout layoutNotificationTime;
    private TextInputLayout layoutQuietHoursStart;
    private TextInputLayout layoutQuietHoursEnd;
    
    private com.google.android.material.button.MaterialButton btnShowTutorial;
    private com.google.android.material.button.MaterialButton btnExportData;
    private com.google.android.material.button.MaterialButton btnImportData;
    private com.google.android.material.button.MaterialButton btnClearLocalData;

    // Cultivar list (from Workprogram.java)
    private final String[] cultivars = {
        "Victory F1", "HOPE F1", "Maganda F1", "Malakas F1", "Rocky 1 F1",
        "Improved KS Apollo", "Improved Pope", "Super Pope", "Maguilas", "Maunlad",
        "Mapalad", "Abiona F1", "Akna F1", "Amari F1", "Anita F1",
        "Colette F1", "Danica F1", "Granger F1", "Janet F1", "Platinum F1",
        "Reina F1", "Renata F1", "Rubellite F1", "TOM-055 F1", "TOM-262 OP",
        "Dalwangan Tm1", "Dalwangan Tm2", "NSIC 1999 Tm09", "Mara", "AniMax 1",
        "AniMax 2", "Golden Globe", "Maxxime"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        setupDrawer();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }
        
        initializeViews();
        loadSettings();
        setupClickListeners();
    }
    
    private void initializeViews() {
        editTextLanguage = findViewById(R.id.editTextLanguage);
        editTextTheme = findViewById(R.id.editTextTheme);
        editTextDefaultCultivar = findViewById(R.id.editTextDefaultCultivar);
        editTextWeatherUnit = findViewById(R.id.editTextWeatherUnit);
        editTextMeasurementUnit = findViewById(R.id.editTextMeasurementUnit);
        editTextDateFormat = findViewById(R.id.editTextDateFormat);
        
        layoutLanguage = findViewById(R.id.layoutLanguage);
        layoutTheme = findViewById(R.id.layoutTheme);
        layoutDefaultCultivar = findViewById(R.id.layoutDefaultCultivar);
        layoutWeatherUnit = findViewById(R.id.layoutWeatherUnit);
        layoutMeasurementUnit = findViewById(R.id.layoutMeasurementUnit);
        layoutDateFormat = findViewById(R.id.layoutDateFormat);
        
        switchTaskNotifications = findViewById(R.id.switchTaskNotifications);
        switchMonitoringNotifications = findViewById(R.id.switchMonitoringNotifications);
        switchGeneralNotifications = findViewById(R.id.switchGeneralNotifications);
        switchQuietHours = findViewById(R.id.switchQuietHours);
        
        editTextNotificationSound = findViewById(R.id.editTextNotificationSound);
        editTextNotificationTime = findViewById(R.id.editTextNotificationTime);
        editTextQuietHoursStart = findViewById(R.id.editTextQuietHoursStart);
        editTextQuietHoursEnd = findViewById(R.id.editTextQuietHoursEnd);
        
        layoutNotificationSound = findViewById(R.id.layoutNotificationSound);
        layoutNotificationTime = findViewById(R.id.layoutNotificationTime);
        layoutQuietHoursStart = findViewById(R.id.layoutQuietHoursStart);
        layoutQuietHoursEnd = findViewById(R.id.layoutQuietHoursEnd);
        
        btnShowTutorial = findViewById(R.id.btnShowTutorial);
        btnExportData = findViewById(R.id.btnExportData);
        btnImportData = findViewById(R.id.btnImportData);
        btnClearLocalData = findViewById(R.id.btnClearLocalData);
    }
    
    private void loadSettings() {
        // Load Language
        String language = SettingsPreferences.getLanguage(this);
        editTextLanguage.setText(language.equals(SettingsPreferences.LANGUAGE_ENGLISH) ? "English" : "Filipino");
        
        // Load Theme
        String theme = SettingsPreferences.getTheme(this);
        String themeDisplay = theme.equals(SettingsPreferences.THEME_LIGHT) ? "Light" :
                            theme.equals(SettingsPreferences.THEME_DARK) ? "Dark" : "System";
        editTextTheme.setText(themeDisplay);
        
        // Load Default Cultivar
        String defaultCultivar = SettingsPreferences.getDefaultCultivar(this);
        editTextDefaultCultivar.setText(defaultCultivar.isEmpty() ? "None" : defaultCultivar);
        
        // Load Weather Unit
        String weatherUnit = SettingsPreferences.getWeatherUnit(this);
        editTextWeatherUnit.setText(weatherUnit.equals(SettingsPreferences.WEATHER_UNIT_CELSIUS) ? "Celsius" : "Fahrenheit");
        
        // Load Measurement Unit
        String measurementUnit = SettingsPreferences.getMeasurementUnit(this);
        String unitDisplay = measurementUnit.equals(SettingsPreferences.MEASUREMENT_UNIT_HECTARE) ? "Hectare" : "Metric";
        editTextMeasurementUnit.setText(unitDisplay);
        
        // Load Date Format
        String dateFormat = SettingsPreferences.getDateFormat(this);
        editTextDateFormat.setText(dateFormat);
        
        // Load Notification Settings
        setupNotificationPreferences();
        
        // Load Advanced Settings
        loadAdvancedSettings();
    }
    
    private void loadAdvancedSettings() {
        // Load Notification Sound
        String sound = SettingsPreferences.getNotificationSound(this);
        editTextNotificationSound.setText(sound);
        
        // Load Notification Time
        int hour = SettingsPreferences.getNotificationHour(this);
        int minute = SettingsPreferences.getNotificationMinute(this);
        String timeStr = String.format("%02d:%02d", hour, minute);
        editTextNotificationTime.setText(timeStr);
        
        // Load Quiet Hours
        boolean quietHoursEnabled = SettingsPreferences.isQuietHoursEnabled(this);
        switchQuietHours.setChecked(quietHoursEnabled);
        updateQuietHoursVisibility(quietHoursEnabled);
        
        if (quietHoursEnabled) {
            int startHour = SettingsPreferences.getQuietHoursStartHour(this);
            int startMinute = SettingsPreferences.getQuietHoursStartMinute(this);
            int endHour = SettingsPreferences.getQuietHoursEndHour(this);
            int endMinute = SettingsPreferences.getQuietHoursEndMinute(this);
            
            editTextQuietHoursStart.setText(String.format("%02d:%02d", startHour, startMinute));
            editTextQuietHoursEnd.setText(String.format("%02d:%02d", endHour, endMinute));
        }
    }
    
    private void updateQuietHoursVisibility(boolean enabled) {
        int visibility = enabled ? View.VISIBLE : View.GONE;
        layoutQuietHoursStart.setVisibility(visibility);
        layoutQuietHoursEnd.setVisibility(visibility);
    }
    
    private void setupNotificationPreferences() {
        if (switchTaskNotifications != null) {
            switchTaskNotifications.setChecked(NotificationPreferences.areTaskNotificationsEnabled(this));
            switchTaskNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                NotificationPreferences.setTaskNotificationsEnabled(this, isChecked);
                backupSettings();
                if (isChecked) {
                    NotificationPermissionHelper.ensurePermission(this);
                }
            });
        }
        if (switchMonitoringNotifications != null) {
            switchMonitoringNotifications.setChecked(NotificationPreferences.areMonitoringNotificationsEnabled(this));
            switchMonitoringNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                NotificationPreferences.setMonitoringNotificationsEnabled(this, isChecked);
                backupSettings();
                if (isChecked) {
                    NotificationPermissionHelper.ensurePermission(this);
                }
            });
        }
        if (switchGeneralNotifications != null) {
            switchGeneralNotifications.setChecked(NotificationPreferences.areGeneralNotificationsEnabled(this));
            switchGeneralNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                NotificationPreferences.setGeneralNotificationsEnabled(this, isChecked);
                backupSettings();
                if (isChecked) {
                    if (NotificationPermissionHelper.ensurePermission(this)) {
                        GeneralUpdateScheduler.ensureDailyTipScheduled(this);
                    }
                } else {
                    GeneralUpdateScheduler.cancelDailyTip(this);
                }
            });
        }
    }
    
    private void setupClickListeners() {
        // Language selector
        editTextLanguage.setOnClickListener(v -> showLanguageDialog());
        
        // Theme selector
        editTextTheme.setOnClickListener(v -> showThemeDialog());
        
        // Cultivar selector
        editTextDefaultCultivar.setOnClickListener(v -> showCultivarDialog());
        
        // Weather Unit selector
        editTextWeatherUnit.setOnClickListener(v -> showWeatherUnitDialog());
        
        // Measurement Unit selector
        editTextMeasurementUnit.setOnClickListener(v -> showMeasurementUnitDialog());
        
        // Date Format selector
        editTextDateFormat.setOnClickListener(v -> showDateFormatDialog());
        
        // Notification Sound selector
        editTextNotificationSound.setOnClickListener(v -> showNotificationSoundDialog());
        
        // Notification Time selector
        editTextNotificationTime.setOnClickListener(v -> showNotificationTimeDialog());
        
        // Quiet Hours switch
        switchQuietHours.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsPreferences.setQuietHoursEnabled(this, isChecked);
            backupSettings();
            updateQuietHoursVisibility(isChecked);
        });
        
        // Quiet Hours time selectors
        editTextQuietHoursStart.setOnClickListener(v -> showQuietHoursStartDialog());
        editTextQuietHoursEnd.setOnClickListener(v -> showQuietHoursEndDialog());
        
        // Show Tutorial button
        if (btnShowTutorial != null) {
            btnShowTutorial.setOnClickListener(v -> {
                com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    // Reset tutorial to allow replay
                    TutorialManager.resetTutorial(this, currentUser.getUid());
                    TutorialManager.startTutorial(this, currentUser.getUid());
                } else {
                    android.widget.Toast.makeText(this, "Please log in to view tutorial", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Export Data button
        btnExportData = findViewById(R.id.btnExportData);
        if (btnExportData != null) {
            btnExportData.setOnClickListener(v -> {
                com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String jsonData = LocalDataManager.getInstance(this).exportAllData(currentUser.getUid());
                    if (jsonData != null) {
                        // For now, just show a toast. In a full implementation, you'd save to a file or share
                        android.widget.Toast.makeText(this, "Data exported successfully", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        android.widget.Toast.makeText(this, "Failed to export data", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.widget.Toast.makeText(this, "Please log in to export data", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Import Data button
        btnImportData = findViewById(R.id.btnImportData);
        if (btnImportData != null) {
            btnImportData.setOnClickListener(v -> {
                android.widget.Toast.makeText(this, "Import functionality coming soon", android.widget.Toast.LENGTH_SHORT).show();
            });
        }
        
        // Clear Local Data button
        btnClearLocalData = findViewById(R.id.btnClearLocalData);
        if (btnClearLocalData != null) {
            btnClearLocalData.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Clear Local Data")
                        .setMessage("Are you sure you want to clear all local data? This action cannot be undone.")
                        .setPositiveButton("Clear", (dialog, which) -> {
                            com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                            if (currentUser != null) {
                                LocalDataManager.getInstance(this).clearAllLocalData(currentUser.getUid());
                                android.widget.Toast.makeText(this, "Local data cleared", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }
    
    private void showLanguageDialog() {
        String[] languages = {"English", "Filipino"};
        String currentLanguage = SettingsPreferences.getLanguage(this);
        int selectedIndex = currentLanguage.equals(SettingsPreferences.LANGUAGE_ENGLISH) ? 0 : 1;
        
        new AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String selected = which == 0 ? SettingsPreferences.LANGUAGE_ENGLISH : SettingsPreferences.LANGUAGE_FILIPINO;
                    SettingsPreferences.setLanguage(this, selected);
                    editTextLanguage.setText(languages[which]);
                    backupSettings();
                    dialog.dismiss();
                    Toast.makeText(this, "Language changed. Restart app to apply.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System"};
        String currentTheme = SettingsPreferences.getTheme(this);
        int selectedIndex = 0;
        if (currentTheme.equals(SettingsPreferences.THEME_DARK)) selectedIndex = 1;
        else if (currentTheme.equals(SettingsPreferences.THEME_SYSTEM)) selectedIndex = 2;
        
        new AlertDialog.Builder(this)
                .setTitle("Select Theme")
                .setSingleChoiceItems(themes, selectedIndex, (dialog, which) -> {
                    String selected;
                    if (which == 0) selected = SettingsPreferences.THEME_LIGHT;
                    else if (which == 1) selected = SettingsPreferences.THEME_DARK;
                    else selected = SettingsPreferences.THEME_SYSTEM;
                    
                    SettingsPreferences.setTheme(this, selected);
                    editTextTheme.setText(themes[which]);
                    backupSettings();
                    dialog.dismiss();
                    Toast.makeText(this, "Theme changed. Restart app to apply.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showCultivarDialog() {
        List<String> cultivarList = new ArrayList<>();
        cultivarList.add("None");
        for (String cultivar : cultivars) {
            cultivarList.add(cultivar);
        }
        
        String currentCultivar = SettingsPreferences.getDefaultCultivar(this);
        int selectedIndex = 0;
        if (!currentCultivar.isEmpty()) {
            for (int i = 0; i < cultivarList.size(); i++) {
                if (cultivarList.get(i).equals(currentCultivar)) {
                    selectedIndex = i;
                    break;
                }
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Select Default Cultivar")
                .setSingleChoiceItems(cultivarList.toArray(new String[0]), selectedIndex, (dialog, which) -> {
                    String selected = which == 0 ? "" : cultivarList.get(which);
                    SettingsPreferences.setDefaultCultivar(this, selected);
                    editTextDefaultCultivar.setText(which == 0 ? "None" : selected);
                    backupSettings();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showWeatherUnitDialog() {
        String[] units = {"Celsius", "Fahrenheit"};
        String currentUnit = SettingsPreferences.getWeatherUnit(this);
        int selectedIndex = currentUnit.equals(SettingsPreferences.WEATHER_UNIT_CELSIUS) ? 0 : 1;
        
        new AlertDialog.Builder(this)
                .setTitle("Select Weather Unit")
                .setSingleChoiceItems(units, selectedIndex, (dialog, which) -> {
                    String selected = which == 0 ? SettingsPreferences.WEATHER_UNIT_CELSIUS : SettingsPreferences.WEATHER_UNIT_FAHRENHEIT;
                    SettingsPreferences.setWeatherUnit(this, selected);
                    editTextWeatherUnit.setText(units[which]);
                    backupSettings();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showMeasurementUnitDialog() {
        String[] units = {"Metric", "Hectare"};
        String currentUnit = SettingsPreferences.getMeasurementUnit(this);
        int selectedIndex = currentUnit.equals(SettingsPreferences.MEASUREMENT_UNIT_HECTARE) ? 1 : 0;
        
        new AlertDialog.Builder(this)
                .setTitle("Select Measurement Unit")
                .setSingleChoiceItems(units, selectedIndex, (dialog, which) -> {
                    String selected = which == 0 ? SettingsPreferences.MEASUREMENT_UNIT_METRIC : SettingsPreferences.MEASUREMENT_UNIT_HECTARE;
                    SettingsPreferences.setMeasurementUnit(this, selected);
                    editTextMeasurementUnit.setText(units[which]);
                    backupSettings();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showDateFormatDialog() {
        String[] formats = {
            SettingsPreferences.DATE_FORMAT_DDMMYYYY,
            SettingsPreferences.DATE_FORMAT_MMDDYYYY,
            SettingsPreferences.DATE_FORMAT_YYYYMMDD
        };
        String currentFormat = SettingsPreferences.getDateFormat(this);
        int selectedIndex = 0;
        for (int i = 0; i < formats.length; i++) {
            if (formats[i].equals(currentFormat)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Select Date Format")
                .setSingleChoiceItems(formats, selectedIndex, (dialog, which) -> {
                    SettingsPreferences.setDateFormat(this, formats[which]);
                    editTextDateFormat.setText(formats[which]);
                    backupSettings();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showNotificationSoundDialog() {
        String[] sounds = {"Default", "Chime", "Bell", "Alert"};
        String currentSound = SettingsPreferences.getNotificationSound(this);
        int selectedIndex = 0;
        for (int i = 0; i < sounds.length; i++) {
            if (sounds[i].equalsIgnoreCase(currentSound)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Select Notification Sound")
                .setSingleChoiceItems(sounds, selectedIndex, (dialog, which) -> {
                    String selected = sounds[which].toLowerCase();
                    SettingsPreferences.setNotificationSound(this, selected);
                    editTextNotificationSound.setText(sounds[which]);
                    backupSettings();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showNotificationTimeDialog() {
        int hour = SettingsPreferences.getNotificationHour(this);
        int minute = SettingsPreferences.getNotificationMinute(this);
        
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    SettingsPreferences.setNotificationHour(this, selectedHour);
                    SettingsPreferences.setNotificationMinute(this, selectedMinute);
                    editTextNotificationTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                },
                hour, minute, true);
        timePicker.setTitle("Select Notification Time");
        timePicker.show();
    }
    
    private void showQuietHoursStartDialog() {
        int hour = SettingsPreferences.getQuietHoursStartHour(this);
        int minute = SettingsPreferences.getQuietHoursStartMinute(this);
        
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    SettingsPreferences.setQuietHoursStartHour(this, selectedHour);
                    SettingsPreferences.setQuietHoursStartMinute(this, selectedMinute);
                    editTextQuietHoursStart.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                    backupSettings();
                },
                hour, minute, true);
        timePicker.setTitle("Select Quiet Hours Start");
        timePicker.show();
    }
    
    private void showQuietHoursEndDialog() {
        int hour = SettingsPreferences.getQuietHoursEndHour(this);
        int minute = SettingsPreferences.getQuietHoursEndMinute(this);
        
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    SettingsPreferences.setQuietHoursEndHour(this, selectedHour);
                    SettingsPreferences.setQuietHoursEndMinute(this, selectedMinute);
                    editTextQuietHoursEnd.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                    backupSettings();
                },
                hour, minute, true);
        timePicker.setTitle("Select Quiet Hours End");
        timePicker.show();
    }

    private void backupSettings() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            LocalDataManager.getInstance(this).syncSettingsToLocal(this, currentUser.getUid());
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @androidx.annotation.NonNull String[] permissions,
                                           @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NotificationHelper.REQUEST_POST_NOTIFICATIONS) {
            if (!NotificationHelper.hasPermission(this)) {
                Toast.makeText(this, R.string.notification_permission_message, Toast.LENGTH_LONG).show();
                if (switchTaskNotifications != null) {
                    switchTaskNotifications.setChecked(false);
                }
                if (switchMonitoringNotifications != null) {
                    switchMonitoringNotifications.setChecked(false);
                }
                if (switchGeneralNotifications != null) {
                    switchGeneralNotifications.setChecked(false);
                }
            } else {
                GeneralUpdateScheduler.ensureDailyTipScheduled(this);
            }
        }
    }
}

