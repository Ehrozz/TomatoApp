package com.android.tomatoapp.core.ui;

import android.annotation.SuppressLint;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import com.android.tomatoapp.notifications.GeneralUpdateScheduler;
import com.android.tomatoapp.notifications.NotificationChannels;
import com.android.tomatoapp.notifications.NotificationPermissionHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import android.location.Geocoder;
import android.location.Address;
import java.util.Locale;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.json.JSONArray;

public class MainActivity extends BaseDrawerActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView textView;
    CardView workprogramselectionCard;
    CardView IPMCard;
    CardView projectedIncomeCard;

    // Weather UI
    private TextView weatherCondition;
    private TextView weatherTemp;
    private TextView weatherLocation;
    private ImageView weatherIcon;
    private CardView weatherCard;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQ_LOCATION = 2001;
    private static final String WEATHER_PREF = "WeatherPref";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_NAME = "name";

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        NotificationChannels.ensureCreated(this);
        NotificationPermissionHelper.ensurePermission(this);
        GeneralUpdateScheduler.ensureDailyTipScheduled(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        workprogramselectionCard = findViewById(R.id.wpsCard);
        IPMCard = findViewById(R.id.ipmCard);
        projectedIncomeCard = findViewById(R.id.projectedIncomeCard);


        // Weather views
        weatherCondition = findViewById(R.id.weatherCondition);
        weatherTemp = findViewById(R.id.weatherTemp);
        weatherLocation = findViewById(R.id.weatherLocation);
        weatherIcon = findViewById(R.id.weatherIcon);
        weatherCard = findViewById(R.id.weatherCard);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestWeather();
        
        // Setup bell icon click listener
        ImageView notificationBellIcon = findViewById(R.id.notificationBellIcon);
        if (notificationBellIcon != null) {
            notificationBellIcon.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(MainActivity.this, NotificationListActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Unable to open notifications", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Sync all data from Firebase to local database
        if (user != null) {
            LocalDataManager manager = LocalDataManager.getInstance(this);
            manager.syncWorkProgramsFromFirebase(user.getUid());
            manager.syncCalculationsFromFirebase(user.getUid());
            manager.syncDetectionHistoryFromFirebase(this, user.getUid());
            manager.syncSettingsToLocal(this, user.getUid());
            
            // Update weather data for all active work programs (runs in background)
            // This ensures weather data stays current for research purposes
            WeatherDataCollector.updateWeatherForAllActivePrograms(this);
        }

        if (weatherCard != null) {
            weatherCard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ForecastActivity.class)));
            weatherCard.setOnLongClickListener(v -> { openPhilippinesLocationPicker(); return true; });
        }

        if (workprogramselectionCard != null) {
            workprogramselectionCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WorkProgramSelection.class);
                startActivity(intent);
            });
        }

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
        } else {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            boolean isFirstLogin = prefs.getBoolean("isFirstLogin_" + user.getUid(), true);

            if (isFirstLogin) {
                // First-time login → show "Welcome"
                // Note: textView may not exist in layout, so check for null
                if (textView != null) {
                    textView.setText("Welcome " + user.getEmail());
                }

                // Mark as not first login anymore
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isFirstLogin_" + user.getUid(), false);
                editor.apply();
            } else {
                // Re-login → show random greeting
                // Note: textView may not exist in layout, so check for null
                if (textView != null) {
                    String[] greetings = {
                            "Good morning",
                            "Good day",
                            "Hello",
                            "Hi there",
                            "Glad to see you back"
                    };

                    java.util.Random random = new java.util.Random();
                    int index = random.nextInt(greetings.length);
                    textView.setText(greetings[index] + " " + user.getDisplayName());
                }
            }
        }



        // Right-side drawer toggle
        setupDrawer();

        if (getSupportActionBar() != null) {
            if (toggle != null) {
            toggle.getDrawerArrowDrawable().setDirection(
                    androidx.appcompat.graphics.drawable.DrawerArrowDrawable.ARROW_DIRECTION_END
            );
            getSupportActionBar().setHomeAsUpIndicator(toggle.getDrawerArrowDrawable());
        }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
            // Display saved name immediately
            if (weatherLocation != null && name != null && !name.isEmpty()) weatherLocation.setText(name);
            // Pass the saved name to use instead of reverse geocoding
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
                // Fallback: Lopez, Quezon (approx)
                Location fallback = new Location("");
                fallback.setLatitude(13.8840);
                fallback.setLongitude(122.2633);
                fetchAndDisplayWeather(fallback, null);
            }
        }).addOnFailureListener(e -> {
            // On failure, use fallback
            Location fallback = new Location("");
            fallback.setLatitude(13.8840);
            fallback.setLongitude(122.2633);
            fetchAndDisplayWeather(fallback, null);
        });
    }

    private void fetchAndDisplayWeather(Location location, String savedName) {
        final double lat = location.getLatitude();
        final double lon = location.getLongitude();

        // If we have a saved name, use it instead of reverse geocoding
        if (savedName != null && !savedName.isEmpty()) {
            // Still need to run network operation on background thread
            new Thread(() -> {
                fetchWeatherFromOpenMeteo(lat, lon, savedName);
            }).start();
            return;
        }

        // Resolve locality name via reverse geocoding only if no saved name
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
                
                // Get weather unit setting
                String weatherUnit = SettingsPreferences.getWeatherUnit(MainActivity.this);
                boolean useFahrenheit = weatherUnit.equals(SettingsPreferences.WEATHER_UNIT_FAHRENHEIT);
                
                // Convert temperature if needed
                double displayTemp = tempC;
                if (useFahrenheit && !Double.isNaN(tempC)) {
                    displayTemp = (tempC * 9.0 / 5.0) + 32.0;
                }
                String tempUnit = useFahrenheit ? "°F" : "°C";
                
                // Daily min/max
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

                String tempText = (Double.isNaN(displayTemp) ? "--" : Math.round(displayTemp) + tempUnit) + extra;

                runOnUiThread(() -> {
                    if (weatherCondition != null) weatherCondition.setText(condition);
                    if (weatherTemp != null) weatherTemp.setText(tempText);
                    if (weatherLocation != null) weatherLocation.setText(locality == null || locality.isEmpty() ? (lat + ", " + lon) : locality);
                    if (weatherIcon != null) weatherIcon.setImageResource(selectIconForCode(wcode));
                });
            } else {
                // Handle non-200 response codes
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
        // First show region selection
        String[] regions = {"Region 1 (Ilocos)", "Region 2 (Cagayan Valley)", "Region 3 (Central Luzon)", "Region 4A (CALABARZON)", "Region 4B (MIMAROPA)", "Region 5 (Bicol)", "Region 6 (Western Visayas)", "Region 7 (Central Visayas)", "Region 8 (Eastern Visayas)", "Region 9 (Zamboanga Peninsula)", "Region 10 (Northern Mindanao)", "Region 11 (Davao)", "Region 12 (Soccsksargen)", "Region 13 (Caraga)", "Region 14 (NCR - Metro Manila)", "Region 15 (CAR)", "Region 16 (BARMM)", "Region 17"};
        
        new AlertDialog.Builder(this)
                .setTitle("Select Region")
                .setItems(regions, (dialog, regionIndex) -> {
                    String[] labels;
                    double[] lats;
                    double[] lons;
                    String regionTitle;
                    
                    if (regionIndex == 0) {
                        // Region 1
                        labels = PhilippineLocations.getRegion1Labels();
                        lats = PhilippineLocations.getRegion1Lats();
                        lons = PhilippineLocations.getRegion1Lons();
                        regionTitle = "Region 1 (Ilocos)";
                    } else if (regionIndex == 1) {
                        // Region 2
                        labels = PhilippineLocations.getRegion2Labels();
                        lats = PhilippineLocations.getRegion2Lats();
                        lons = PhilippineLocations.getRegion2Lons();
                        regionTitle = "Region 2 (Cagayan Valley)";
                    } else if (regionIndex == 2) {
                        // Region 3
                        labels = PhilippineLocations.getRegion3Labels();
                        lats = PhilippineLocations.getRegion3Lats();
                        lons = PhilippineLocations.getRegion3Lons();
                        regionTitle = "Region 3 (Central Luzon)";
                    } else if (regionIndex == 3) {
                        // Region 4A
                        labels = PhilippineLocations.getRegion4Labels();
                        lats = PhilippineLocations.getRegion4Lats();
                        lons = PhilippineLocations.getRegion4Lons();
                        regionTitle = "Region 4A (CALABARZON)";
                    } else if (regionIndex == 4) {
                        // Region 4B
                        labels = PhilippineLocations.getRegion4BLabels();
                        lats = PhilippineLocations.getRegion4BLats();
                        lons = PhilippineLocations.getRegion4BLons();
                        regionTitle = "Region 4B (MIMAROPA)";
                    } else if (regionIndex == 5) {
                        // Region 5
                        labels = PhilippineLocations.getRegion5Labels();
                        lats = PhilippineLocations.getRegion5Lats();
                        lons = PhilippineLocations.getRegion5Lons();
                        regionTitle = "Region 5 (Bicol)";
                    } else if (regionIndex == 6) {
                        // Region 6
                        labels = PhilippineLocations.getRegion6Labels();
                        lats = PhilippineLocations.getRegion6Lats();
                        lons = PhilippineLocations.getRegion6Lons();
                        regionTitle = "Region 6 (Western Visayas)";
                    } else if (regionIndex == 7) {
                        // Region 7
                        labels = PhilippineLocations.getRegion7Labels();
                        lats = PhilippineLocations.getRegion7Lats();
                        lons = PhilippineLocations.getRegion7Lons();
                        regionTitle = "Region 7 (Central Visayas)";
                    } else if (regionIndex == 8) {
                        // Region 8
                        labels = PhilippineLocations.getRegion8Labels();
                        lats = PhilippineLocations.getRegion8Lats();
                        lons = PhilippineLocations.getRegion8Lons();
                        regionTitle = "Region 8 (Eastern Visayas)";
                    } else if (regionIndex == 9) {
                        // Region 9
                        labels = PhilippineLocations.getRegion9Labels();
                        lats = PhilippineLocations.getRegion9Lats();
                        lons = PhilippineLocations.getRegion9Lons();
                        regionTitle = "Region 9 (Zamboanga Peninsula)";
                    } else if (regionIndex == 10) {
                        // Region 10
                        labels = PhilippineLocations.getRegion10Labels();
                        lats = PhilippineLocations.getRegion10Lats();
                        lons = PhilippineLocations.getRegion10Lons();
                        regionTitle = "Region 10 (Northern Mindanao)";
                    } else if (regionIndex == 11) {
                        // Region 11
                        labels = PhilippineLocations.getRegion11Labels();
                        lats = PhilippineLocations.getRegion11Lats();
                        lons = PhilippineLocations.getRegion11Lons();
                        regionTitle = "Region 11 (Davao)";
                    } else if (regionIndex == 12) {
                        // Region 12
                        labels = PhilippineLocations.getRegion12Labels();
                        lats = PhilippineLocations.getRegion12Lats();
                        lons = PhilippineLocations.getRegion12Lons();
                        regionTitle = "Region 12 (Soccsksargen)";
                    } else if (regionIndex == 13) {
                        // Region 13
                        labels = PhilippineLocations.getRegion13Labels();
                        lats = PhilippineLocations.getRegion13Lats();
                        lons = PhilippineLocations.getRegion13Lons();
                        regionTitle = "Region 13 (Caraga)";
                    } else if (regionIndex == 14) {
                        // Region 14
                        labels = PhilippineLocations.getRegion14Labels();
                        lats = PhilippineLocations.getRegion14Lats();
                        lons = PhilippineLocations.getRegion14Lons();
                        regionTitle = "Region 14 (NCR - Metro Manila)";
                    } else if (regionIndex == 15) {
                        // Region 15
                        labels = PhilippineLocations.getRegion15Labels();
                        lats = PhilippineLocations.getRegion15Lats();
                        lons = PhilippineLocations.getRegion15Lons();
                        regionTitle = "Region 15 (CAR)";
                    } else if (regionIndex == 16) {
                        // Region 16
                        labels = PhilippineLocations.getRegion16Labels();
                        lats = PhilippineLocations.getRegion16Lats();
                        lons = PhilippineLocations.getRegion16Lons();
                        regionTitle = "Region 16 (BARMM)";
                    } else {
                        // Region 17
                        labels = PhilippineLocations.getRegion17Labels();
                        lats = PhilippineLocations.getRegion17Lats();
                        lons = PhilippineLocations.getRegion17Lons();
                        regionTitle = "Region 17";
                    }
                    
                    // Show location picker for selected region
                    if (labels == null || labels.length == 0 || lats == null || lats.length == 0 || lons == null || lons.length == 0) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("No Locations Available")
                                .setMessage("No locations are available for " + regionTitle + " at this time.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }
                    
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Select Location - " + regionTitle)
                            .setItems(labels, (dialog2, which) -> {
                                if (which < 0 || which >= labels.length || which >= lats.length || which >= lons.length) {
                                    Toast.makeText(MainActivity.this, "Invalid selection", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                double lat = lats[which];
                                double lon = lons[which];
                                String label = labels[which];
                                SharedPreferences wp = getSharedPreferences(WEATHER_PREF, MODE_PRIVATE);
                                wp.edit()
                                        .putLong(KEY_LAT, Double.doubleToLongBits(lat))
                                        .putLong(KEY_LON, Double.doubleToLongBits(lon))
                                        .putString(KEY_NAME, label)
                                        .apply();
                                if (weatherLocation != null) weatherLocation.setText(label);
                                Location loc = new Location("");
                                loc.setLatitude(lat);
                                loc.setLongitude(lon);
                                // Pass the selected label to preserve the exact location name
                                fetchAndDisplayWeather(loc, label);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String mapWeatherCode(int code) {
        // Minimal mapping per Open-Meteo weather codes
        if (code == 0) return "Clear";
        if (code == 1 || code == 2) return "Partly cloudy";
        if (code == 3) return "Overcast";
        if (code >= 45 && code <= 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain showers";
        if (code >= 85 && code <= 86) return "Snow showers";
        if (code >= 95) return "Thunderstorm";
        return "Unknown";
    }

    private int selectIconForCode(int code) {
        // Use cloud icons based on weather code
        if (code == 0) return R.drawable.ic_weather_clear;
        if (code == 1 || code == 2) return R.drawable.ic_weather_partly_cloudy;
        if (code == 3) return R.drawable.ic_weather_overcast;
        if (code >= 45 && code <= 48) return R.drawable.ic_weather_fog;
        if (code >= 51 && code <= 57) return R.drawable.ic_weather_drizzle;
        if (code >= 61 && code <= 67) return R.drawable.ic_weather_rain;
        if (code >= 71 && code <= 77) return R.drawable.ic_weather_overcast; // Snow - use overcast
        if (code >= 80 && code <= 82) return R.drawable.ic_weather_rain_showers;
        if (code >= 85 && code <= 86) return R.drawable.ic_weather_overcast; // Snow showers - use overcast
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
                // Use fallback location
                Location fallback = new Location("");
                fallback.setLatitude(13.8840);
                fallback.setLongitude(122.2633);
                fetchAndDisplayWeather(fallback, null);
            }
        }
    }

    // Show the Agreement Popup
    private void showUserAgreementDialog(SharedPreferences prefs) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_agreement, null);

        com.google.android.material.checkbox.MaterialCheckBox chkAgree = dialogView.findViewById(R.id.chkAgree);
        com.google.android.material.button.MaterialButton btnAccept = dialogView.findViewById(R.id.btnAccept);

        if (chkAgree == null || btnAccept == null) {
            // If views are not found, the dialog layout might have changed
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false) // must accept
                .create();

        chkAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAccept.setEnabled(isChecked);
        });

        btnAccept.setOnClickListener(v -> {
            if (chkAgree.isChecked()) {
                prefs.edit().putBoolean("UserAgreementAccepted", true).apply();
                dialog.dismiss(); // allow user to continue
            }
        });

        dialog.show();
    }
    
    // Show Terms Dialog on First Login
    private void showTermsDialogOnFirstLogin(String userId) {
        TermsDialog dialog = new TermsDialog(this, userId, new TermsDialog.OnTermsAcceptedListener() {
            @Override
            public void onTermsAccepted() {
                // Terms accepted, user can continue using the app
                Toast.makeText(MainActivity.this, getString(R.string.success_login), Toast.LENGTH_SHORT).show();
                
                // Show tutorial after terms acceptance
                if (TutorialManager.shouldShowTutorial(MainActivity.this, userId)) {
                    // Use post to ensure terms dialog is fully dismissed first
                    findViewById(android.R.id.content).post(() -> {
                        TutorialManager.startTutorial(MainActivity.this, userId);
                    });
                }
            }
            
            @Override
            public void onTermsDeclined() {
                // User must accept terms - sign them out
                Toast.makeText(MainActivity.this, getString(R.string.terms_must_accept), Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize Financial Overview with donut chart
     * Fetches data from Firebase and calculates income, expenses, and net income
     */
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Save all data before app closes
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            LocalDataManager manager = LocalDataManager.getInstance(this);
            manager.syncSettingsToLocal(this, currentUser.getUid());
        }
    }
}
