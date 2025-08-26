package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DetectionHistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private Button btnClearHistory;
    private ArrayList<JSONObject> historyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_history);

        historyListView = findViewById(R.id.historyListView);
        btnClearHistory = findViewById(R.id.btnClearHistory);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detection History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadHistory();

        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            if (historyData == null || historyData.isEmpty()) return;
            JSONObject selectedEntry = historyData.get(position);
            try {
                Intent intent = new Intent(DetectionHistoryActivity.this, DetectionResults.class);
                intent.putExtra("title", selectedEntry.getString("disease"));
                intent.putExtra("imageUri", selectedEntry.getString("imageUri"));
                intent.putExtra("description", selectedEntry.optString("description", ""));
                intent.putExtra("symptoms", selectedEntry.optString("symptoms", ""));
                intent.putExtra("cause", selectedEntry.optString("cause", ""));
                intent.putExtra("cure", selectedEntry.optString("cure", ""));
                intent.putExtra("prevention", selectedEntry.optString("prevention", ""));
                intent.putExtra("pestTitle", selectedEntry.optString("pestTitle", ""));
                intent.putExtra("pestDescription", selectedEntry.optString("pestDescription", ""));
                intent.putExtra("pestImageUri", selectedEntry.optString("pestImageUri", ""));
                intent.putExtra("accuracy", selectedEntry.getString("accuracy"));

                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Clear history button
        btnClearHistory.setOnClickListener(v -> {
            DetectionHistoryManager.clearHistory(this);
            loadHistory();
        });
    }

    private void loadHistory() {
        historyData = DetectionHistoryManager.getHistory(this);
        ArrayList<String> displayList = new ArrayList<>();

        for (JSONObject entry : historyData) {
            try {
                String disease = entry.getString("disease");
                long timestamp = entry.getLong("timestamp");

                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(new Date(timestamp));

                displayList.add(disease + " - " + date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (displayList.isEmpty()) {
            displayList.add("No history found.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                displayList
        );

        historyListView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
