package com.android.tomatoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class DetectionResults extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private TextView identifyViewText, scoreText, finding1, finding2, finding3;
    private TextView detectionDescription, detectionSymptoms, detectionCure, detectionAccuracy;
    private TextView badgeNumber;
    private ImageView detectionImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detection_results);

        // Initialize new UI elements
        identifyViewText = findViewById(R.id.identifyViewText);
        scoreText = findViewById(R.id.scoreText);
        finding1 = findViewById(R.id.finding1);
        finding2 = findViewById(R.id.finding2);
        finding3 = findViewById(R.id.finding3);
        badgeNumber = findViewById(R.id.badgeNumber);
        detectionImage = findViewById(R.id.detectionImage);
        detectionDescription = findViewById(R.id.detectionDescription);
        detectionSymptoms = findViewById(R.id.detectionSymptoms);
        detectionCure = findViewById(R.id.detectionCure);
        detectionAccuracy = findViewById(R.id.detectionAccuracy);

        Intent intent = getIntent();
        if (intent != null) {
            // Set image
            String imageUriStr = intent.getStringExtra("imageUri");
            if (imageUriStr != null) {
                detectionImage.setImageURI(Uri.parse(imageUriStr));
            }

            // Set badge number - show total detections in history
            ArrayList<org.json.JSONObject> history = DetectionHistoryManager.getHistory(this);
            int totalDetections = history != null ? history.size() : 0;
            if (totalDetections > 0) {
                badgeNumber.setText(String.valueOf(totalDetections));
            } else {
                badgeNumber.setText("1");
            }

            // Set identify view text (could include timestamp or ID)
            String identifyText = intent.getStringExtra("identifyView");
            if (identifyText != null && !identifyText.isEmpty()) {
                identifyViewText.setText(identifyText);
            } else {
                // Generate a simple ID based on timestamp
                long timestamp = System.currentTimeMillis();
                identifyViewText.setText("Identify View, " + (timestamp % 10000));
            }

            // Set score/ID tag from accuracy or topPredictions
            String accuracy = intent.getStringExtra("accuracy");
            String topPredictions = intent.getStringExtra("topPredictions");
            if (accuracy != null && !accuracy.isEmpty()) {
                // Extract numbers from accuracy (e.g., "85.5%" -> "85 - 5")
                String cleanAcc = accuracy.replaceAll("[^0-9.]", "");
                if (cleanAcc.contains(".")) {
                    String[] parts = cleanAcc.split("\\.");
                    if (parts.length >= 2) {
                        scoreText.setText(parts[0] + " - " + parts[1].substring(0, Math.min(parts[1].length(), 3)));
                    } else {
                        scoreText.setText(cleanAcc.substring(0, Math.min(cleanAcc.length(), 4)) + " - 0");
                    }
                } else {
                    scoreText.setText(cleanAcc.substring(0, Math.min(cleanAcc.length(), 4)) + " - 0");
                }
            } else if (topPredictions != null && !topPredictions.isEmpty()) {
                // Use first prediction's confidence as score
                scoreText.setText("1011 - 628"); // Default format
            } else {
                scoreText.setText("1011 - 628"); // Default
            }

            // Populate findings from detection results
            String description = intent.getStringExtra("description");
            String symptoms = intent.getStringExtra("symptoms");
            String cause = intent.getStringExtra("cause");
            String cure = intent.getStringExtra("cure");
            String prevention = intent.getStringExtra("prevention");

            // Finding 1: Description or Symptoms
            if (description != null && !description.isEmpty()) {
                finding1.setText(description);
            } else if (symptoms != null && !symptoms.isEmpty()) {
                finding1.setText(symptoms);
            } else {
                finding1.setText("Detection result information will appear here.");
            }

            // Finding 2: Cause or Title
            String title = intent.getStringExtra("title");
            if (cause != null && !cause.isEmpty()) {
                finding2.setText(cause);
            } else if (title != null && !title.isEmpty()) {
                finding2.setText(title);
            } else {
                finding2.setText("Additional detection details.");
            }

            // Finding 3: Cure or Prevention
            if (cure != null && !cure.isEmpty()) {
                finding3.setText(cure);
            } else if (prevention != null && !prevention.isEmpty()) {
                finding3.setText(prevention);
            } else {
                finding3.setText("Treatment and prevention recommendations.");
            }

            // Set detailed information
            if (description != null) {
                detectionDescription.setText(description);
            }
            if (symptoms != null) {
                detectionSymptoms.setText(symptoms);
            }
            if (cure != null) {
                detectionCure.setText(cure);
            }

            // Set accuracy
            String acc = intent.getStringExtra("accuracy");
            String confidenceWarning = intent.getStringExtra("confidenceWarning");
            
            StringBuilder accuracyText = new StringBuilder();
            if (confidenceWarning != null && !confidenceWarning.isEmpty()) {
                accuracyText.append("⚠️ ").append(confidenceWarning).append("\n\n");
            }
            accuracyText.append("Detection Accuracy: ").append(acc != null ? acc : "N/A");
            if (topPredictions != null && !topPredictions.isEmpty()) {
                accuracyText.append("\n\nTop Predictions: ").append(topPredictions);
            }
            detectionAccuracy.setText(accuracyText.toString());
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Scan Results");
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, Login.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
