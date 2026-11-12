package com.android.tomatoapp.analytics.data;

import java.util.ArrayList;
import java.util.List;

public class DiseaseExportGroup {
    public static class Row {
        public final String date, disease, status, confidence;
        public final int phase;
        public Row(String date, int phase, String disease, String status, String confidence) {
            this.date = date; this.phase = phase; this.disease = disease;
            this.status = status; this.confidence = confidence;
        }
    }

    public final String title, startDate;
    public final double area;
    public final List<Row> rows = new ArrayList<>();

    public DiseaseExportGroup(String title, String startDate, double area) {
        this.title = title; this.startDate = startDate; this.area = area;
    }

    public int healthyCount() {
        int n = 0;
        for (Row r : rows) if ("Healthy".equals(r.status)) n++;
        return n;
    }

    public double avgConfidence() {
        double sum = 0; int n = 0;
        for (Row r : rows) {
            try { sum += Double.parseDouble(r.confidence.replace("%", "").trim()); n++; }
            catch (Exception ignored) { }
        }
        return n == 0 ? 0 : sum / n;
    }
}