package com.android.tomatoapp.weather.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.tomatoapp.R;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.weather.data.OpenMeteoClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastActivity extends BaseBottomNavActivity {

    private TextView locationTitle;
    private LinearLayout forecastContainer;
    
    // Today Card Views
    private TextView todayDateText;
    private TextView todayTempText;
    private TextView todayConditionText;
    private TextView todayRangeText;
    private TextView todayRainChanceText;
    private TextView weatherTipText;
    private ImageView todayWeatherIcon;

    private static final String WEATHER_PREF = "WeatherPref";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_NAME = "name";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Forecast");
        }

        locationTitle = findViewById(R.id.locationTitle);
        forecastContainer = findViewById(R.id.forecastContainer);
        
        // Initialize Today Card
        todayDateText = findViewById(R.id.todayDateText);
        todayTempText = findViewById(R.id.todayTempText);
        todayConditionText = findViewById(R.id.todayConditionText);
        todayRangeText = findViewById(R.id.todayRangeText);
        todayRainChanceText = findViewById(R.id.todayRainChanceText);
        weatherTipText = findViewById(R.id.weatherTipText);
        todayWeatherIcon = findViewById(R.id.todayWeatherIcon);

        SharedPreferences wp = getSharedPreferences(WEATHER_PREF, MODE_PRIVATE);
        double lat = Double.longBitsToDouble(wp.getLong(KEY_LAT, Double.doubleToLongBits(0)));
        double lon = Double.longBitsToDouble(wp.getLong(KEY_LON, Double.doubleToLongBits(0)));
        String name = wp.getString(KEY_NAME, "");

        if (name != null && !name.isEmpty()) {
            locationTitle.setText(name);
        } else {
            locationTitle.setText(String.format(Locale.getDefault(), "%.3f, %.3f", lat, lon));
        }

        fetchForecast(lat, lon);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void fetchForecast(double lat, double lon) {
        // Guard against the (0,0) default when no location was saved yet
        if (lat == 0 && lon == 0) { lat = 13.8855; lon = 122.2909; }
        final double fLat = lat, fLon = lon;
        android.util.Log.d("WeatherDebug", "Forecast lat=" + fLat + " lon=" + fLon);

        new Thread(() -> {
            try {
                OpenMeteoClient.Result w = OpenMeteoClient.fetch(fLat, fLon);
                boolean useF = SettingsPreferences.getWeatherUnit(ForecastActivity.this)
                        .equals(SettingsPreferences.WEATHER_UNIT_FAHRENHEIT);
                String unit = useF ? "°F" : "°C";

                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat out = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

                List<Row> rows = new ArrayList<>();
                for (OpenMeteoClient.Day d : w.days) {
                    double mxV = useF ? d.tMax * 9.0 / 5.0 + 32.0 : d.tMax;
                    double mnV = useF ? d.tMin * 9.0 / 5.0 + 32.0 : d.tMin;
                    Row r = new Row();
                    try { r.date = out.format(in.parse(d.date)); } catch (Exception e) { r.date = d.date; }
                    r.condition = d.condition;
                    r.tempRange = Math.round(mnV) + "°/" + Math.round(mxV) + "°";
                    r.rainChance = d.rainProbMax + "% Rain" + (d.precipMm >= 0.1
                            ? String.format(Locale.US, " · %.1f mm", d.precipMm) : "");
                    r.iconCode = d.iconCode;
                    r.iconRes = selectIconForCode(d.iconCode);
                    r.maxTempC = (int) Math.round(d.tMax);
                    rows.add(r);
                }
                // Today card shows the CURRENT temperature and condition, not the day's max
                double nowV = useF ? w.nowTemp * 9.0 / 5.0 + 32.0 : w.nowTemp;
                rows.get(0).nowTemp = Double.isNaN(nowV) ? "--" : Math.round(nowV) + unit;
                rows.get(0).condition = w.nowCondition;
                rows.get(0).iconRes = selectIconForCode(w.nowIconCode);

                runOnUiThread(() -> {
                    updateTodayCard(rows.get(0));
                    displayForecast(rows.subList(1, rows.size()));
                });
            } catch (Exception e) {
                android.util.Log.e("WeatherDebug", "Forecast failed", e);
                runOnUiThread(() -> {
                    List<Row> fallback = new ArrayList<>();
                    Row r = new Row();
                    r.date = "—"; r.condition = "Failed to load forecast";
                    r.tempRange = ""; r.rainChance = "";
                    r.iconRes = android.R.drawable.ic_dialog_alert;
                    fallback.add(r);
                    displayForecast(fallback);
                });
            }
        }).start();
    }

    private String mapWeatherCode(int code) {
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

    private static class Row {
        String date;
        String condition;
        String tempRange;
        String rainChance;

        int iconRes;
        int iconCode;
        int maxTempC;

        String nowTemp;
    }

    private void updateTodayCard(Row today) {
        if (todayDateText != null) todayDateText.setText(today.date);
        if (todayTempText != null) todayTempText.setText(today.nowTemp != null ? today.nowTemp : today.tempRange.split("/")[1]);
        if (todayConditionText != null) todayConditionText.setText(today.condition);
        if (todayRangeText != null) todayRangeText.setText(today.tempRange);
        if (todayRainChanceText != null) todayRainChanceText.setText(today.rainChance.split(" ")[0].replace("Rain", "").trim());
        if (todayWeatherIcon != null) todayWeatherIcon.setImageResource(today.iconRes);
        if (weatherTipText != null) weatherTipText.setText(getWeatherTip(today.iconCode, today.maxTempC));
    }

    private String getWeatherTip(int code, int maxTempC) {
        if (code >= 95) return "Thunderstorms expected. Secure loose equipment and avoid fieldwork.";
        if (code == 63) return "Rain expected. Good for soil moisture, but postpone spraying and fertilizing.";
        if (code == 80) return "Showers possible. Keep sprays and fertilizer for a dry window.";
        if (code == 51) return "Small chance of light showers. Normal work is fine; watch the sky.";
        if (code >= 45 && code <= 48) return "Foggy morning. Humid conditions favor fungal disease, so scout your plants.";
        if (maxTempC > 33) return "High heat. Irrigate early or late in the day to reduce tomato stress.";
        if (code <= 1) return "Clear and dry. Good day for harvesting, weeding or spraying.";
        return "Mild conditions. Continue your scheduled farm tasks.";
    }

    private void displayForecast(List<Row> rows) {
        if (forecastContainer == null) return;
        
        forecastContainer.removeAllViews();
        
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Row row : rows) {
            View itemView = inflater.inflate(R.layout.item_forecast_row, forecastContainer, false);
            
            ImageView icon = itemView.findViewById(R.id.icon);
            TextView date = itemView.findViewById(R.id.date);
            TextView cond = itemView.findViewById(R.id.condition);
            TextView det = itemView.findViewById(R.id.details);
            TextView rain = itemView.findViewById(R.id.rainLabel);
            
            if (icon != null) icon.setImageResource(row.iconRes);
            if (date != null) date.setText(row.date);
            if (cond != null) cond.setText(row.condition);
            if (det != null) det.setText(row.tempRange);
            if (rain != null) {
                if (row.rainChance != null && !row.rainChance.isEmpty()) {
                    rain.setText(row.rainChance);
                    rain.setVisibility(View.VISIBLE);
                } else {
                    rain.setVisibility(View.GONE);
                }
            }
            
            forecastContainer.addView(itemView);
        }
    }
}



