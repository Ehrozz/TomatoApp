package com.android.tomatoapp.core.ui;

import android.annotation.SuppressLint;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tomatoapp.common.models.UserLocation;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.GravityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.android.tomatoapp.R;
import com.android.tomatoapp.auth.ui.Login;
import com.android.tomatoapp.auth.ui.ProfileActivity;
import com.android.tomatoapp.common.managers.TutorialManager;
import com.android.tomatoapp.common.models.IPM;
import com.android.tomatoapp.common.ui.dialogs.TermsDialog;
import com.android.tomatoapp.common.utils.PhilippineLocations;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.android.tomatoapp.financial.ui.CostSelection;
import com.android.tomatoapp.notifications.GeneralUpdateScheduler;
import com.android.tomatoapp.notifications.NotificationChannels;
import com.android.tomatoapp.notifications.NotificationListActivity;
import com.android.tomatoapp.notifications.NotificationPermissionHelper;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.settings.ui.SettingsActivity;
import com.android.tomatoapp.weather.data.WeatherDataCollector;
import com.android.tomatoapp.weather.ui.ForecastActivity;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;
import com.android.tomatoapp.workprogram.ui.WorkProgramSelection;
import com.android.tomatoapp.workprogram.ui.Workprogram;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.widget.AutoCompleteTextView;
import android.widget.PopupMenu;
import android.location.Location;
import android.location.Geocoder;
import android.location.Address;
import android.os.Handler;
import android.os.Looper;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.json.JSONArray;
import com.android.tomatoapp.weather.ui.CityManagementActivity;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MainActivity extends BaseBottomNavActivity {

    private static final String TAG = "MainActivity";

    FirebaseAuth mAuth;
    FirebaseUser user;
    View workprogramselectionCard;
    View IPMCard;
    View projectedIncomeCard;
    com.google.android.material.button.MaterialButton btnViewWorkprogram;
    private ImageView userAvatarImage;
    private TextView userInitials;
    private boolean tutorialRequested;

    // Weather UI
    private TextView weatherCondition;
    private TextView weatherTemp;
    private TextView weatherLocation;
    private ImageView weatherIcon;
    private View weatherCard;

    // Charts
    private com.github.mikephil.charting.charts.HorizontalBarChart expensesBarChart;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQ_LOCATION = 2001;
    private static final String WEATHER_PREF = "WeatherPref";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_NAME = "name";

    private TextView activeCultivarName, activeStageName, activeDayCount;
    private android.widget.ProgressBar stageProgress;
    private List<com.android.tomatoapp.workprogram.data.WorkProgramEntity> workProgramList = new ArrayList<>();
    private String activeProgramId;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        NotificationChannels.ensureCreated(this);
        NotificationPermissionHelper.ensurePermission(this);
        GeneralUpdateScheduler.ensureDailyTipScheduled(this);
        com.android.tomatoapp.notifications.TaskNotificationScheduler.ensureTaskNotificationsScheduled(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        
        // Setup Bottom Navigation
        setupBottomNavigation();

        workprogramselectionCard = findViewById(R.id.monitorCard);
        IPMCard = findViewById(R.id.ipmCard);
        projectedIncomeCard = findViewById(R.id.projectedIncomeCard);
        btnViewWorkprogram = findViewById(R.id.btnViewWorkprogram);

        // Weather views
        weatherCondition = findViewById(R.id.weatherCondition);
        weatherTemp = findViewById(R.id.weatherTemp);
        weatherLocation = findViewById(R.id.weatherLocation);
        weatherIcon = findViewById(R.id.weatherIcon);
        weatherCard = findViewById(R.id.weatherCard);

        // Active Program Monitoring
        activeCultivarName = findViewById(R.id.activeCultivarName);
        activeStageName = findViewById(R.id.activeStageName);
        activeDayCount = findViewById(R.id.activeDayCount);
        stageProgress = findViewById(R.id.stageProgress);
        View programSelector = findViewById(R.id.programSelector);
        if (programSelector != null) {
            programSelector.setOnClickListener(v -> showProgramSelectionDialog());
        }
        setupActiveProgramMonitoring();
        
        // Add a manage locations button/icon in code if not in layout, 
        // but for now we'll use long-press on weather card to offer choice

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestWeather();
        
        // Setup bell icon click listener
        View bell = findViewById(R.id.notificationBellIcon);
        if (bell != null) {
            bell.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(MainActivity.this, com.android.tomatoapp.notifications.NotificationListActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open notifications", e);
                    Toast.makeText(MainActivity.this, "Unable to open notifications", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Setup profile click listener
        View profileCard = findViewById(R.id.profileCard);
        if (profileCard != null) profileCard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        if (user != null) {
            // Update UI with user info
            TextView userNameText = findViewById(R.id.userName);
            userInitials = findViewById(R.id.userInitials);
            userAvatarImage = findViewById(R.id.userAvatarImage);
            
            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                userNameText.setText(name);
                userInitials.setText(getInitials(name));
            } else {
                // Fetch from Database if DisplayName is not set
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String dbName = snapshot.child("fullName").getValue(String.class);
                            String photoUri = snapshot.child("photoUri").getValue(String.class);
                            if (dbName != null) {
                                userNameText.setText(dbName);
                                userInitials.setText(getInitials(dbName));
                            }
                            loadProfilePhoto(photoUri);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            
            // Load profile photo if stored
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userRef.child("photoUri").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String photoUri = snapshot.getValue(String.class);
                    loadProfilePhoto(photoUri);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

            LocalDataManager manager = LocalDataManager.getInstance(this);
            manager.syncWorkProgramsFromFirebase(user.getUid());
            manager.syncCalculationsFromFirebase(user.getUid());
            manager.syncDetectionHistoryFromFirebase(this, user.getUid());
            manager.syncSettingsToLocal(this, user.getUid());
            WeatherDataCollector.updateWeatherForAllActivePrograms(this);
            maybeStartTutorial();
            
            expensesBarChart = findViewById(R.id.expensesBarChart);
            setupExpensesOverviewChart();
        }

        if (weatherCard != null) {
            weatherCard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ForecastActivity.class)));
            weatherCard.setOnLongClickListener(v -> {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                popup.getMenu().add("Manage Cities");
                popup.getMenu().add("Change Location");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Manage Cities")) {
                        startActivity(new Intent(MainActivity.this, CityManagementActivity.class));
                    } else {
                        openPhilippinesLocationPicker();
                    }
                    return true;
                });
                popup.show();
                return true;
            });
        }

        if (workprogramselectionCard != null) workprogramselectionCard.setOnClickListener(v -> navigateToWorkProgram());

        if (btnViewWorkprogram != null) btnViewWorkprogram.setOnClickListener(v -> navigateToWorkProgram());

        if (IPMCard != null) {
            IPMCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, IPM.class);
                startActivity(intent);
            });
        }

        if (projectedIncomeCard != null) {
            projectedIncomeCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CostSelection.class);
                startActivity(intent);
            });
        }

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Show User Agreement if not accepted yet
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean accepted = prefs.getBoolean("UserAgreementAccepted", false);
        if (!accepted) {
            showUserAgreementDialog(prefs);
        }
    }

    private void requestWeather() {
        // Prefer saved PH location if available
        SharedPreferences wp = getSharedPreferences(WEATHER_PREF, MODE_PRIVATE);
        if (wp.contains(KEY_LAT) && wp.contains(KEY_LON)) {
            double lat = Double.longBitsToDouble(wp.getLong(KEY_LAT, Double.doubleToLongBits(0)));
            double lon = Double.longBitsToDouble(wp.getLong(KEY_LON, Double.doubleToLongBits(0)));
            String name = wp.getString(KEY_NAME, "");
            Location saved = new Location("");
            saved.setLatitude(lat);
            saved.setLongitude(lon);
            if (weatherLocation != null && name != null && !name.isEmpty()) weatherLocation.setText(name);
            fetchAndDisplayWeather(saved, name);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                fetchAndDisplayWeather(location, null);
            } else {
                Location fallback = new Location("");
                fallback.setLatitude(14.5995);
                fallback.setLongitude(120.9842);
                fetchAndDisplayWeather(fallback, "Manila, Philippines");
            }
        }).addOnFailureListener(e -> {
            Location fallback = new Location("");
            fallback.setLatitude(14.5995);
            fallback.setLongitude(120.9842);
            fetchAndDisplayWeather(fallback, "Manila, Philippines");
        });
    }

    private void fetchAndDisplayWeather(Location location, String savedName) {
        final double lat = location.getLatitude();
        final double lon = location.getLongitude();

        if (savedName != null && !savedName.isEmpty()) {
            new Thread(() -> {
                fetchWeatherFromOpenMeteo(lat, lon, savedName);
            }).start();
            return;
        }

        new Thread(() -> {
            String locality = "";
            try {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address a = addresses.get(0);
                    String city = a.getLocality();
                    String admin = a.getAdminArea();
                    String country = a.getCountryName();
                    StringBuilder sb = new StringBuilder();
                    if (city != null && !city.isEmpty()) sb.append(city);
                    if (admin != null && !admin.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(admin);
                    if (country != null && !country.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(country);
                    locality = sb.toString();
                }
            } catch (Exception ignored) {}

            String finalLocality = locality;
            fetchWeatherFromOpenMeteo(lat, lon, finalLocality);
        }).start();
    }

    private void fetchWeatherFromOpenMeteo(double lat, double lon, String locality) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            String urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon +
                    "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max&forecast_days=1&timezone=auto";
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int code = conn.getResponseCode();
            if (code == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);

                JSONObject root = new JSONObject(sb.toString());
                JSONObject current = root.getJSONObject("current");
                double tempC = current.optDouble("temperature_2m", Double.NaN);
                int wcode = current.optInt("weather_code", -1);

                String condition = mapWeatherCode(wcode);
                String weatherUnit = SettingsPreferences.getWeatherUnit(MainActivity.this);
                boolean useFahrenheit = weatherUnit.equals(SettingsPreferences.WEATHER_UNIT_FAHRENHEIT);
                
                double displayTemp = tempC;
                if (useFahrenheit && !Double.isNaN(tempC)) {
                    displayTemp = (tempC * 9.0 / 5.0) + 32.0;
                }
                String tempUnit = useFahrenheit ? "°F" : "°C";
                
                String extra = "";
                try {
                    JSONObject daily = root.getJSONObject("daily");
                    JSONArray tmax = daily.optJSONArray("temperature_2m_max");
                    JSONArray tmin = daily.optJSONArray("temperature_2m_min");
                    JSONArray pr = daily.optJSONArray("precipitation_probability_max");
                    if (tmax != null && tmax.length() > 0 && tmin != null && tmin.length() > 0) {
                        double mxC = tmax.optDouble(0);
                        double mnC = tmin.optDouble(0);
                        if (useFahrenheit) {
                            mxC = (mxC * 9.0 / 5.0) + 32.0;
                            mnC = (mnC * 9.0 / 5.0) + 32.0;
                        }
                        int mx = (int) Math.round(mxC);
                        int mn = (int) Math.round(mnC);
                        String prp = (pr != null && pr.length() > 0) ? (" · Rain " + pr.optInt(0) + "%") : "";
                        extra = " (" + mn + "°/" + mx + "°" + ")" + prp;
                    }
                } catch (Exception ignored) {}

                String tempText = (Double.isNaN(displayTemp) ? "--" : Math.round(displayTemp) + tempUnit);
                String subText = condition + extra;

                runOnUiThread(() -> {
                    if (weatherCondition != null) weatherCondition.setText(subText);
                    if (weatherTemp != null) weatherTemp.setText(tempText);
                    if (weatherLocation != null) {
                        weatherLocation.setText(locality == null || locality.isEmpty() ? (lat + ", " + lon) : locality);
                        weatherLocation.setSelected(true); // For marquee if needed
                    }
                    if (weatherIcon != null) weatherIcon.setImageResource(selectIconForCode(wcode));
                });
            } else {
                runOnUiThread(() -> {
                    if (weatherCondition != null) weatherCondition.setText("Weather unavailable");
                    if (weatherTemp != null) weatherTemp.setText("--");
                });
            }
        } catch (Exception e) {
            runOnUiThread(() -> {
                if (weatherCondition != null) weatherCondition.setText("Weather unavailable");
            });
        } finally {
            if (reader != null) try { reader.close(); } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }

    private void openPhilippinesLocationPicker() {
        showStructuredLocationDialog();
    }

    private void showStructuredLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_picker, null);
        AlertDialog dialog = builder.setView(dialogView).create();

        AutoCompleteTextView provinceSpinner = dialogView.findViewById(R.id.provinceAutoComplete);
        AutoCompleteTextView citySpinner = dialogView.findViewById(R.id.cityAutoComplete);
        AutoCompleteTextView brgySpinner = dialogView.findViewById(R.id.brgyAutoComplete);
        
        com.google.android.material.textfield.TextInputLayout cityLayout = dialogView.findViewById(R.id.cityLayout);
        com.google.android.material.textfield.TextInputLayout brgyLayout = dialogView.findViewById(R.id.brgyLayout);
        
        android.widget.Button btnApply = dialogView.findViewById(R.id.btnApply);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // State variables
        final String[] provinceSelected = {""};
        final String[] citySelected = {""};
        final String[] brgySelected = {""};
        final int[] finalRegionIndex = {-1};

        // Populate Provinces
        List<String> allProvincesList = new ArrayList<>();
        for (int i = 0; i <= 17; i++) {
            String[] labels = getLabelsForRegion(i);
            for (String label : labels) {
                String[] parts = label.split(", ");
                if (parts.length > 1) {
                    if (!allProvincesList.contains(parts[1])) {
                        allProvincesList.add(parts[1]);
                    }
                }
            }
        }
        Collections.sort(allProvincesList);
        android.widget.ArrayAdapter<String> provinceAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allProvincesList);
        provinceSpinner.setAdapter(provinceAdapter);

        // Province Listener
        provinceSpinner.setOnItemClickListener((parent, view, position, id) -> {
            provinceSelected[0] = (String) parent.getItemAtPosition(position);
            citySelected[0] = "";
            brgySelected[0] = "";
            
            citySpinner.setText("");
            brgySpinner.setText("");
            cityLayout.setEnabled(true);
            brgyLayout.setEnabled(false);
            btnApply.setEnabled(false);

            // Find cities for this province
            List<String> citiesInProvince = new ArrayList<>();
            for (int i = 0; i <= 17; i++) {
                String[] labels = getLabelsForRegion(i);
                for (String label : labels) {
                    if (label.contains(provinceSelected[0])) {
                        citiesInProvince.add(label.split(", ")[0]);
                        finalRegionIndex[0] = i; // Store region for coordinate lookup
                    }
                }
            }
            Collections.sort(citiesInProvince);
            android.widget.ArrayAdapter<String> cityAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, citiesInProvince);
            citySpinner.setAdapter(cityAdapter);
        });

        // City Listener
        citySpinner.setOnItemClickListener((parent, view, position, id) -> {
            citySelected[0] = (String) parent.getItemAtPosition(position);
            brgySelected[0] = "";
            brgySpinner.setText("");
            brgyLayout.setEnabled(true);
            btnApply.setEnabled(false);

            // Populate mock Barangays
            String[] barangays = getMockBarangays(citySelected[0]);
            android.widget.ArrayAdapter<String> brgyAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, barangays);
            brgySpinner.setAdapter(brgyAdapter);
        });

        // Barangay Listener
        brgySpinner.setOnItemClickListener((parent, view, position, id) -> {
            brgySelected[0] = (String) parent.getItemAtPosition(position);
            btnApply.setEnabled(true);
        });

        btnApply.setOnClickListener(v -> {
            String fullLocation = brgySelected[0] + ", " + citySelected[0] + ", " + provinceSelected[0];
            
            // Calculate coordinates (simplified lookup)
            double lat = 14.5995;
            double lon = 120.9842;
            String[] labels = getLabelsForRegion(finalRegionIndex[0]);
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].startsWith(citySelected[0])) {
                    lat = getLatsForRegion(finalRegionIndex[0])[i];
                    lon = getLonsForRegion(finalRegionIndex[0])[i];
                    break;
                }
            }

            // Save to Firebase for persistence
            if (user != null) {
                String locId = FirebaseDatabase.getInstance().getReference().push().getKey();
                if (locId != null) {
                    UserLocation newLoc = new UserLocation(locId, provinceSelected[0], citySelected[0], brgySelected[0], lat, lon, true);
                    FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("locations").child(locId).setValue(newLoc);
                    // Set others to not default
                    FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("locations").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String key = ds.getKey();
                                if (key != null && !key.equals(locId)) {
                                    ds.getRef().child("isDefault").setValue(false);
                                }
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            saveAndApplyLocation(finalRegionIndex[0], citySelected[0], fullLocation);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String[] getMockBarangays(String city) {
        return new String[]{"Poblacion", "San Jose", "Santa Maria", "San Pedro", "Santo Niño", "San Juan", "Santa Cruz", "San Roque", "Maligaya", "Bagong Pag-asa"};
    }

    private String[] getLabelsForRegion(int index) {
        switch (index) {
            case 0: return PhilippineLocations.getRegion1Labels();
            case 1: return PhilippineLocations.getRegion2Labels();
            case 2: return PhilippineLocations.getRegion3Labels();
            case 3: return PhilippineLocations.getRegion4Labels();
            case 4: return PhilippineLocations.getRegion4BLabels();
            case 5: return PhilippineLocations.getRegion5Labels();
            case 6: return PhilippineLocations.getRegion6Labels();
            case 7: return PhilippineLocations.getRegion7Labels();
            case 8: return PhilippineLocations.getRegion8Labels();
            case 9: return PhilippineLocations.getRegion9Labels();
            case 10: return PhilippineLocations.getRegion10Labels();
            case 11: return PhilippineLocations.getRegion11Labels();
            case 12: return PhilippineLocations.getRegion12Labels();
            case 13: return PhilippineLocations.getRegion13Labels();
            case 14: return PhilippineLocations.getRegion14Labels();
            case 15: return PhilippineLocations.getRegion15Labels();
            case 16: return PhilippineLocations.getRegion16Labels();
            default: return PhilippineLocations.getRegion17Labels();
        }
    }

    private void saveAndApplyLocation(int regionIndex, String city, String fullLabel) {
        String[] labels = getLabelsForRegion(regionIndex);
        double lat = 14.5995;
        double lon = 120.9842;
        
        // Try to find original coordinates from city
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].startsWith(city)) {
                double[] lats = getLatsForRegion(regionIndex);
                double[] lons = getLonsForRegion(regionIndex);
                lat = lats[i];
                lon = lons[i];
                break;
            }
        }

        SharedPreferences wp = getSharedPreferences(WEATHER_PREF, MODE_PRIVATE);
        wp.edit()
                .putLong(KEY_LAT, Double.doubleToLongBits(lat))
                .putLong(KEY_LON, Double.doubleToLongBits(lon))
                .putString(KEY_NAME, fullLabel)
                .apply();
        if (weatherLocation != null) weatherLocation.setText(fullLabel);
        Location loc = new Location("");
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        fetchAndDisplayWeather(loc, fullLabel);
    }

    private void loadProfilePhoto(String photoUri) {
        if (userAvatarImage == null || userInitials == null) {
            return;
        }

        if (photoUri == null || photoUri.trim().isEmpty()) {
            userAvatarImage.setImageResource(R.drawable.ic_person);
            userAvatarImage.setVisibility(View.VISIBLE);
            userInitials.setVisibility(View.VISIBLE);
            return;
        }

        try {
            android.net.Uri uri = android.net.Uri.parse(photoUri);
            try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    userAvatarImage.setImageBitmap(android.graphics.BitmapFactory.decodeStream(inputStream));
                    userInitials.setVisibility(View.GONE);
                    userAvatarImage.setVisibility(View.VISIBLE);
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        userAvatarImage.setImageResource(R.drawable.ic_person);
        userAvatarImage.setVisibility(View.VISIBLE);
        userInitials.setVisibility(View.VISIBLE);
    }

    private void maybeStartTutorial() {
        if (tutorialRequested || user == null) {
            return;
        }

        tutorialRequested = true;
        if (TutorialManager.shouldShowTutorial(this, user.getUid())) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing()) {
                    TutorialManager.startTutorial(this, user.getUid());
                }
            }, 900);
        }
    }

    private double[] getLatsForRegion(int index) {
        switch (index) {
            case 0: return PhilippineLocations.getRegion1Lats();
            case 1: return PhilippineLocations.getRegion2Lats();
            case 2: return PhilippineLocations.getRegion3Lats();
            case 3: return PhilippineLocations.getRegion4Lats();
            case 4: return PhilippineLocations.getRegion4BLats();
            case 5: return PhilippineLocations.getRegion5Lats();
            case 6: return PhilippineLocations.getRegion6Lats();
            case 7: return PhilippineLocations.getRegion7Lats();
            case 8: return PhilippineLocations.getRegion8Lats();
            case 9: return PhilippineLocations.getRegion9Lats();
            case 10: return PhilippineLocations.getRegion10Lats();
            case 11: return PhilippineLocations.getRegion11Lats();
            case 12: return PhilippineLocations.getRegion12Lats();
            case 13: return PhilippineLocations.getRegion13Lats();
            case 14: return PhilippineLocations.getRegion14Lats();
            case 15: return PhilippineLocations.getRegion15Lats();
            case 16: return PhilippineLocations.getRegion16Lats();
            default: return PhilippineLocations.getRegion17Lats();
        }
    }

    private double[] getLonsForRegion(int index) {
        switch (index) {
            case 0: return PhilippineLocations.getRegion1Lons();
            case 1: return PhilippineLocations.getRegion2Lons();
            case 2: return PhilippineLocations.getRegion3Lons();
            case 3: return PhilippineLocations.getRegion4Lons();
            case 4: return PhilippineLocations.getRegion4BLons();
            case 5: return PhilippineLocations.getRegion5Lons();
            case 6: return PhilippineLocations.getRegion6Lons();
            case 7: return PhilippineLocations.getRegion7Lons();
            case 8: return PhilippineLocations.getRegion8Lons();
            case 9: return PhilippineLocations.getRegion9Lons();
            case 10: return PhilippineLocations.getRegion10Lons();
            case 11: return PhilippineLocations.getRegion11Lons();
            case 12: return PhilippineLocations.getRegion12Lons();
            case 13: return PhilippineLocations.getRegion13Lons();
            case 14: return PhilippineLocations.getRegion14Lons();
            case 15: return PhilippineLocations.getRegion15Lons();
            case 16: return PhilippineLocations.getRegion16Lons();
            default: return PhilippineLocations.getRegion17Lons();
        }
    }

    private String mapWeatherCode(int code) {
        if (code == 0) return "Fair Weather";
        if (code == 1 || code == 2) return "Partly Cloudy to Cloudy";
        if (code == 3) return "Cloudy Skies";
        if (code >= 45 && code <= 48) return "Foggy";
        if (code >= 51 && code <= 57) return "Rainshowers / Drizzle";
        if (code >= 61 && code <= 67) return "Scattered Rainshowers";
        if (code >= 71 && code <= 77) return "Light Snow (N/A)";
        if (code >= 80 && code <= 82) return "Monsoon Rains / Showers";
        if (code >= 85 && code <= 86) return "Occasional Rains";
        if (code >= 95) return "Severe Thunderstorms";
        return "Fair Weather";
    }

    private int selectIconForCode(int code) {
        if (code == 0) return R.drawable.ic_weather_clear;
        if (code == 1 || code == 2) return R.drawable.ic_weather_partly_cloudy;
        if (code == 3) return R.drawable.ic_weather_overcast;
        if (code >= 45 && code <= 48) return R.drawable.ic_weather_fog;
        if (code >= 51 && code <= 57) return R.drawable.ic_weather_drizzle;
        if (code >= 61 && code <= 67) return R.drawable.ic_weather_rain;
        if (code >= 71 && code <= 77) return R.drawable.ic_weather_overcast;
        if (code >= 80 && code <= 82) return R.drawable.ic_weather_rain_showers;
        if (code >= 85 && code <= 86) return R.drawable.ic_weather_overcast;
        if (code >= 95) return R.drawable.ic_weather_thunderstorm;
        return R.drawable.ic_weather_clear;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            boolean granted = false;
            if (grantResults.length > 0) {
                for (int r : grantResults) { if (r == PackageManager.PERMISSION_GRANTED) { granted = true; break; } }
            }
            if (granted) {
                requestWeather();
            } else {
                Location fallback = new Location("");
                fallback.setLatitude(14.5995);
                fallback.setLongitude(120.9842);
                fetchAndDisplayWeather(fallback, "Manila, Philippines");
            }
        }
    }

    private void showUserAgreementDialog(SharedPreferences prefs) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_agreement, null);
        com.google.android.material.checkbox.MaterialCheckBox chkAgree = dialogView.findViewById(R.id.chkAgree);
        com.google.android.material.button.MaterialButton btnAccept = dialogView.findViewById(R.id.btnAccept);

        if (chkAgree == null || btnAccept == null) return;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        chkAgree.setOnCheckedChangeListener((buttonView, isChecked) -> btnAccept.setEnabled(isChecked));
        btnAccept.setOnClickListener(v -> {
            if (chkAgree.isChecked()) {
                prefs.edit().putBoolean("UserAgreementAccepted", true).apply();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            LocalDataManager manager = LocalDataManager.getInstance(this);
            manager.syncSettingsToLocal(this, currentUser.getUid());
        }
    }

    private void setupExpensesOverviewChart() {
        if (expensesBarChart == null || user == null) return;

        expensesBarChart.setNoDataText("Loading expense data...");
        expensesBarChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.muted));

        DatabaseReference programsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("workPrograms");

        programsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Aggregate expenses by cultivar
                java.util.TreeMap<String, Double> cultivarTotals = new java.util.TreeMap<>();

                for (DataSnapshot programSnapshot : snapshot.getChildren()) {
                    String cultivar = programSnapshot.child("cultivarName").getValue(String.class);
                    if (cultivar == null) cultivar = programSnapshot.child("cultivar").getValue(String.class);
                    if (cultivar == null) continue;

                    double programDailyTotal = 0;
                    DataSnapshot dailyExpensesSnapshot = programSnapshot.child("dailyExpenses");
                    if (dailyExpensesSnapshot.exists()) {
                        for (DataSnapshot dateSnapshot : dailyExpensesSnapshot.getChildren()) {
                            programDailyTotal += sumCategoryCosts(dateSnapshot.child("labor"));
                            programDailyTotal += sumCategoryCosts(dateSnapshot.child("material"));
                            programDailyTotal += sumCategoryCosts(dateSnapshot.child("equipment"));
                            programDailyTotal += sumCategoryCosts(dateSnapshot.child("miscellaneous"));
                        }
                    }

                    Double existing = cultivarTotals.get(cultivar);
                    double existingVal = (existing != null) ? existing : 0.0;
                    cultivarTotals.put(cultivar, existingVal + programDailyTotal);
                }

                if (cultivarTotals.isEmpty()) {
                    expensesBarChart.setNoDataText("No daily expenses logged yet");
                    expensesBarChart.invalidate();
                    return;
                }

                renderExpensesBarChart(cultivarTotals);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private double sumCategoryCosts(DataSnapshot snapshot) {
        double total = 0;
        for (DataSnapshot item : snapshot.getChildren()) {
            Double cost = item.child("totalCost").getValue(Double.class);
            if (cost == null) cost = item.child("cost").getValue(Double.class);
            if (cost != null) total += cost;
        }
        return total;
    }

    private void renderExpensesBarChart(java.util.TreeMap<String, Double> cultivarTotals) {
        if (expensesBarChart == null) return;

        List<com.github.mikephil.charting.data.BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        int index = 0;
        // Reversed for better display in HorizontalBarChart (top to bottom)
        for (java.util.Map.Entry<String, Double> entry : cultivarTotals.entrySet()) {
            entries.add(new com.github.mikephil.charting.data.BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        com.github.mikephil.charting.data.BarDataSet dataSet = new com.github.mikephil.charting.data.BarDataSet(entries, "Total Expenses (₱)");
        
        // ApexCharts inspired styling: Clean solid color, no labels
        dataSet.setColor(ContextCompat.getColor(this, R.color.sidebar_dark_green));
        dataSet.setDrawValues(false); // Matches dataLabels: { enabled: false }

        com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(dataSet);
        barData.setBarWidth(0.6f);
        expensesBarChart.setData(barData);

        // Chart Configuration
        expensesBarChart.getDescription().setEnabled(false);
        expensesBarChart.getLegend().setEnabled(false);
        expensesBarChart.setExtraLeftOffset(50f); // More space for cultivar names
        expensesBarChart.setDrawGridBackground(false);
        expensesBarChart.animateY(800, Easing.EaseOutQuart);
        expensesBarChart.setFitBars(true);
        expensesBarChart.setTouchEnabled(true);
        expensesBarChart.setScaleEnabled(false);

        XAxis xAxis = expensesBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text));
        xAxis.setTextSize(11f);
        xAxis.setLabelCount(labels.size());

        YAxis leftAxis = expensesBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.border));
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.muted));
        leftAxis.setAxisMinimum(0f);
        leftAxis.setLabelCount(5);
        leftAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "₱" + Math.round(value);
            }
        });

        expensesBarChart.getAxisRight().setEnabled(false);
        expensesBarChart.invalidate();
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length > 1) {
            return (Character.toUpperCase(parts[0].charAt(0)) + "" + Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        }
        return String.valueOf(Character.toUpperCase(parts[0].charAt(0)));
    }

    private void navigateToWorkProgram() {
        if (user == null) return;

        // Prefer the currently selected "active program" from the Current Stage card.
        if (activeProgramId != null && !activeProgramId.isEmpty() && workProgramList != null && !workProgramList.isEmpty()) {
            WorkProgramEntity active = null;
            for (WorkProgramEntity wp : workProgramList) {
                if (wp != null && activeProgramId.equals(wp.id)) {
                    active = wp;
                    break;
                }
            }
            if (active != null) {
                Intent intent = new Intent(MainActivity.this, Workprogram.class);
                intent.putExtra("programId", active.id);
                intent.putExtra("cultivar", active.cultivarName);
                intent.putExtra("startDate", active.startingDate);
                startActivity(intent);
                return;
            }
        }

        // Fallback: open selection/creation.
        Intent intent = new Intent(MainActivity.this, WorkProgramSelection.class);
        startActivity(intent);
    }
    private void setupActiveProgramMonitoring() {
        if (user == null) return;
        DatabaseReference wpRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("workPrograms");
        wpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                workProgramList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    com.android.tomatoapp.workprogram.data.WorkProgramEntity wp = ds.getValue(com.android.tomatoapp.workprogram.data.WorkProgramEntity.class);
                    if (wp != null) {
                        wp.id = ds.getKey();
                        workProgramList.add(wp);
                    }
                }
                
                if (activeProgramId == null && !workProgramList.isEmpty()) {
                    activeProgramId = workProgramList.get(0).id;
                }
                updateMonitoringUI();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateMonitoringUI() {
        if (activeProgramId == null || workProgramList.isEmpty()) {
            if (activeCultivarName != null) activeCultivarName.setText("No Active Program");
            if (activeStageName != null) activeStageName.setText("Start a work program to monitor progress");
            if (activeDayCount != null) activeDayCount.setText("Day -- of --");
            if (stageProgress != null) stageProgress.setProgress(0);
            return;
        }

        com.android.tomatoapp.workprogram.data.WorkProgramEntity activeWP = null;
        for (com.android.tomatoapp.workprogram.data.WorkProgramEntity wp : workProgramList) {
            if (wp.id.equals(activeProgramId)) {
                activeWP = wp;
                break;
            }
        }

        if (activeWP != null) {
            if (activeCultivarName != null) activeCultivarName.setText(activeWP.cultivarName);
            
            // Calculate progress and stage
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date start = sdf.parse(activeWP.startingDate);
                if (start != null) {
                    long diff = new java.util.Date().getTime() - start.getTime();
                    int days = (int) (diff / (1000 * 60 * 60 * 24)) + 1;
                    int totalDays = 90; // Default or fetch from cultivar data
                    
                    days = Math.max(1, days);
                    if (activeDayCount != null) activeDayCount.setText(String.format(Locale.getDefault(), "Day %d of %d", days, totalDays));
                    
                    int progress = (int) ((days / (float) totalDays) * 100);
                    if (stageProgress != null) stageProgress.setProgress(Math.min(100, progress));
                    
                    // Determine stage name based on crop duration
                    if (activeStageName != null) {
                        if (days <= 7) activeStageName.setText("Land and Soil Preparation");
                        else if (days <= 40) activeStageName.setText("Vegetative Stage");
                        else if (days <= 60) activeStageName.setText("Flowering Stage");
                        else if (days <= 90) activeStageName.setText("Maturity Stage");
                        else activeStageName.setText("Post-Harvest");
                    }
                }
            } catch (Exception e) {
                if (activeStageName != null) activeStageName.setText("Stage tracking unavailable");
            }
        }
    }

    private void showProgramSelectionDialog() {
        if (workProgramList.isEmpty()) {
            Toast.makeText(this, "No work programs found", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] programNames = new String[workProgramList.size()];
        for (int i = 0; i < workProgramList.size(); i++) {
            programNames[i] = workProgramList.get(i).cultivarName + " (" + workProgramList.get(i).startingDate + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Switch Active Program")
                .setItems(programNames, (dialog, which) -> {
                    activeProgramId = workProgramList.get(which).id;
                    updateMonitoringUI();
                    Toast.makeText(this, "Switched to " + workProgramList.get(which).cultivarName, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
