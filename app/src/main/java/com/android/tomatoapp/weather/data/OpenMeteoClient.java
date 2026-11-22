package com.android.tomatoapp.weather.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class OpenMeteoClient {

    // Leave empty for Open-Meteo's default model mix. For testing, use ONE model, e.g. "ecmwf_ifs025"
    // or "icon_seamless". Don't list several models: the response keys get suffixed and parsing breaks.
    // Some models don't return precipitation_probability; see Phase 4.
    private static final String MODELS = "";

    private OpenMeteoClient() {}

    public static class Day {
        public String date;
        public double tMax, tMin;
        public double precipMm;       // 24h total
        public int rainProbMax;       // daytime (06-18h) max, %
        public int rainProbAvg;       // daytime average, %
        public int cloudAvg;          // daytime average, %
        public boolean thunder;
        public int iconCode;          // WMO-style code used for icon/tip
        public String condition;
    }

    public static class Result {
        public double nowTemp, nowPrecip;
        public int nowCloud, nowCode, nowIconCode;
        public String nowCondition;
        public List<Day> days = new ArrayList<>();
    }

    public static Result fetch(double lat, double lon) throws Exception {
        String urlStr = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat + "&longitude=" + lon
                + "&current=temperature_2m,precipitation,cloud_cover,weather_code"
                + "&hourly=precipitation_probability,precipitation,cloud_cover,weather_code"
                + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum"
                + "&forecast_days=7&timezone=auto"
                + (MODELS.isEmpty() ? "" : "&models=" + MODELS);

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            int code = conn.getResponseCode();
            if (code != 200) throw new Exception("HTTP " + code);

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return parse(sb.toString());
        } finally {
            if (reader != null) try { reader.close(); } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }

    static Result parse(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        Result r = new Result();

        JSONObject cur = root.getJSONObject("current");
        r.nowTemp = cur.optDouble("temperature_2m", Double.NaN);
        r.nowPrecip = cur.optDouble("precipitation", 0);
        r.nowCloud = cur.optInt("cloud_cover", 0);
        r.nowCode = cur.optInt("weather_code", 0);
        r.nowIconCode = nowCode(r.nowCode, r.nowPrecip, r.nowCloud);
        r.nowCondition = label(r.nowIconCode);

        JSONObject daily = root.getJSONObject("daily");
        JSONObject hourly = root.getJSONObject("hourly");
        JSONArray dTime = daily.getJSONArray("time");
        JSONArray dMax = daily.getJSONArray("temperature_2m_max");
        JSONArray dMin = daily.getJSONArray("temperature_2m_min");
        JSONArray dPrec = daily.optJSONArray("precipitation_sum");
        JSONArray hProb = hourly.optJSONArray("precipitation_probability");
        JSONArray hCloud = hourly.optJSONArray("cloud_cover");
        JSONArray hCode = hourly.optJSONArray("weather_code");
        int hourCount = hourly.getJSONArray("time").length();

        for (int d = 0; d < dTime.length(); d++) {
            Day f = new Day();
            f.date = dTime.getString(d);
            f.tMax = dMax.optDouble(d, Double.NaN);
            f.tMin = dMin.optDouble(d, Double.NaN);
            f.precipMm = dPrec != null ? dPrec.optDouble(d, 0) : 0;

            int probMax = 0, probSum = 0, cloudSum = 0, n = 0;
            for (int h = 6; h <= 18; h++) {            // daytime hours
                int idx = d * 24 + h;                   // timezone=auto starts at local midnight
                if (idx >= hourCount) break;
                int p = hProb != null ? hProb.optInt(idx, 0) : 0;
                probMax = Math.max(probMax, p);
                probSum += p;
                cloudSum += hCloud != null ? hCloud.optInt(idx, 0) : 0;
                if (hCode != null && hCode.optInt(idx, 0) >= 95) f.thunder = true;
                n++;
            }
            f.rainProbMax = probMax;
            f.rainProbAvg = n > 0 ? probSum / n : 0;
            f.cloudAvg = n > 0 ? cloudSum / n : 0;
            f.iconCode = dayCode(f);
            f.condition = label(f.iconCode);
            r.days.add(f);
        }
        return r;
    }

    // ---- Classification (thresholds are starting points; tune after comparing with reality) ----

    static int dayCode(Day f) {
        if (f.thunder && f.precipMm >= 1.0) return 95;                        // thunderstorms
        if (f.precipMm >= 10 && f.rainProbAvg >= 50) return 63;               // rain
        if (f.precipMm >= 2 && f.rainProbMax >= 50) return 80;                // showers
        if (f.precipMm >= 0.5 && f.rainProbMax >= 40) return 51;              // light showers possible
        return cloudCode(f.cloudAvg);
    }

    static int nowCode(int apiCode, double precipNow, int cloud) {
        if (apiCode >= 45 && apiCode <= 48) return apiCode;                   // fog
        if (apiCode >= 95 && precipNow >= 0.1) return 95;
        if (precipNow >= 2.5) return 63;
        if (precipNow >= 0.2) return 80;
        return cloudCode(cloud);                                              // ignore "rain" code if nothing is falling
    }

    static int cloudCode(int cloud) {
        if (cloud < 15) return 0;
        if (cloud < 45) return 1;
        if (cloud < 75) return 2;
        return 3;
    }

    public static String label(int code) {
        switch (code) {
            case 0: return "Clear";
            case 1: return "Mostly clear";
            case 2: return "Partly cloudy";
            case 3: return "Cloudy";
            case 51: return "Chance of light showers";
            case 63: return "Rain";
            case 80: return "Showers";
            case 95: return "Thunderstorms";
            default:
                if (code >= 45 && code <= 48) return "Fog";
                return "Partly cloudy";
        }
    }
}