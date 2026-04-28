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

public class ForecastActivity extends AppCompatActivity {

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
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                String urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon +
                        "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&forecast_days=7&timezone=auto";
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);

                    JSONObject root = new JSONObject(sb.toString());
                    JSONObject daily = root.getJSONObject("daily");
                    JSONArray dates = daily.getJSONArray("time");
                    JSONArray wcodes = daily.getJSONArray("weather_code");
                    JSONArray tmax = daily.getJSONArray("temperature_2m_max");
                    JSONArray tmin = daily.getJSONArray("temperature_2m_min");
                    JSONArray pr = daily.optJSONArray("precipitation_probability_max");

                    // Get weather unit setting
                    String weatherUnit = SettingsPreferences.getWeatherUnit(ForecastActivity.this);
                    boolean useFahrenheit = weatherUnit.equals(SettingsPreferences.WEATHER_UNIT_FAHRENHEIT);
                    String tempUnit = useFahrenheit ? "°F" : "°C";
                    
                    List<Row> rows = new ArrayList<>();
                    SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat out = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
                    for (int i = 0; i < dates.length(); i++) {
                        String d = dates.getString(i);
                        int code = wcodes.optInt(i, -1);
                        double mxC = tmax.optDouble(i);
                        double mnC = tmin.optDouble(i);
                        if (useFahrenheit) {
                            mxC = (mxC * 9.0 / 5.0) + 32.0;
                            mnC = (mnC * 9.0 / 5.0) + 32.0;
                        }
                        int mx = (int) Math.round(mxC);
                        int mn = (int) Math.round(mnC);
                        String prp = (pr != null && pr.length() > i) ? (" · Rain " + pr.optInt(i) + "%") : "";
                        String label;
                        try {
                            Date dd = in.parse(d);
                            label = out.format(dd);
                        } catch (Exception e) {
                            label = d;
                        }
                        Row row = new Row();
                        row.date = label;
                        row.condition = mapWeatherCode(code);
                        row.tempRange = mn + "°/" + mx + "°";
                        row.rainChance = (pr != null && pr.length() > i) ? pr.optInt(i) + "% Rain" : "";
                        row.iconRes = selectIconForCode(code);
                        row.weatherCode = code;
                        row.maxTemp = mx;
                        rows.add(row);
                    }

                    List<Row> finalRows = rows;
                    runOnUiThread(() -> {
                        updateTodayCard(finalRows.get(0));
                        displayForecast(finalRows.subList(1, finalRows.size()));
                    });
                }
            } catch (Exception ignored) {
                runOnUiThread(() -> {
                    List<Row> fallback = new ArrayList<>();
                    Row r = new Row();
                    r.date = "—";
                    r.condition = "Failed to load forecast";
                    r.tempRange = "";
                    r.rainChance = "";
                    r.iconRes = android.R.drawable.ic_dialog_alert;
                    fallback.add(r);
                    displayForecast(fallback);
                });
            } finally {
                if (reader != null) try { reader.close(); } catch (Exception ignored2) {}
                if (conn != null) conn.disconnect();
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
        int weatherCode;
        int maxTemp;
    }

    private void updateTodayCard(Row today) {
        if (todayDateText != null) todayDateText.setText(today.date);
        if (todayTempText != null) todayTempText.setText(today.tempRange.split("/")[1]);
        if (todayConditionText != null) todayConditionText.setText(today.condition);
        if (todayRangeText != null) todayRangeText.setText(today.tempRange);
        if (todayRainChanceText != null) todayRainChanceText.setText(today.rainChance.replace(" Rain", ""));
        if (todayWeatherIcon != null) todayWeatherIcon.setImageResource(today.iconRes);
        
        if (weatherTipText != null) {
            weatherTipText.setText(getWeatherTip(today.weatherCode, today.maxTemp));
        }
    }

    private String getWeatherTip(int code, int maxTemp) {
        if (code >= 95) return "Thunderstorms expected. Secure loose equipment and avoid fieldwork.";
        if (code >= 61) return "Heavy rain likely. Good for soil moisture, but avoid applying chemicals.";
        if (code >= 51) return "Light drizzle. Ideal for transplanting seedlings as they won't dry out.";
        if (code == 0) {
            if (maxTemp > 32) return "High heat. Ensure intensive irrigation today to prevent tomato stress.";
            return "Clear skies. Great day for harvesting or weeding.";
        }
        if (code <= 2) return "Mild weather. Perfect for general maintenance and monitoring.";
        return "Normal conditions. Continue with your scheduled farming tasks.";
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




