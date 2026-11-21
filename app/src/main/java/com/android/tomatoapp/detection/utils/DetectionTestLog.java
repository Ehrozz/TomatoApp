package com.android.tomatoapp.detection.utils;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/** One CSV row per scan (accepted or rejected): Android/data/<app>/files/test_logs/detection_log.csv */
public final class DetectionTestLog {
    private static final String HEADER = "timestamp,model,image,status,accepted,top1,top1_prob,"
            + "top2,top2_prob,lead,min_conf,min_lead,app_message";

    private DetectionTestLog() { }

    public static synchronized void append(Context context, String model, String image,
                                           Map<String, String> r) {
        File dir = context.getExternalFilesDir("test_logs");
        if (dir == null) return;
        File file = new File(dir, "detection_log.csv");
        boolean isNew = !file.exists();
        try (FileWriter writer = new FileWriter(file, true)) {
            if (isNew) writer.write(HEADER + "\n");
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            String[] cols = {
                    time, model, image, r.get("status"), r.get("accepted"),
                    r.get("top1Name"), r.get("top1Prob"), r.get("top2Name"), r.get("top2Prob"),
                    r.get("lead"), r.get("minConfidence"), r.get("minLead"), r.get("rejectionReason")
            };
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < cols.length; i++) {
                if (i > 0) row.append(',');
                row.append(cols[i] == null ? "" : cols[i].replace(',', ';').replace('\n', ' '));
            }
            writer.write(row.append("\n").toString());
        } catch (IOException ignored) { }
    }
}