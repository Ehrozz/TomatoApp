package com.android.tomatoapp.detection.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.FileProvider;

import com.android.tomatoapp.R;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.detection.data.DetectionHistoryManager;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class DetectionResults extends BaseBottomNavActivity {
    private static final String TAG = "DetectionResults";

    private TextView identifyViewText, scoreText, finding1, finding2, finding3;
    private TextView detectionContextInfo;
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
        detectionContextInfo = findViewById(R.id.detectionContextInfo);
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
            // Set image with proper error handling
            String imageUriStr = intent.getStringExtra("imageUri");
            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                loadImageFromUri(imageUriStr);
            } else {
                // Set default image if no URI provided
                detectionImage.setImageResource(R.mipmap.ic_logo);
            }

            // Set badge number - show total detections in history
            ArrayList<org.json.JSONObject> history = DetectionHistoryManager.getHistory(this);
            int totalDetections = history.size();
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
                identifyViewText.setText(String.format(Locale.getDefault(), "Identify View, %d", (timestamp % 10000)));
            }

            String detectionCultivar = intent.getStringExtra("detectionCultivar");
            int detectionPhase = intent.getIntExtra("detectionPhase", 0);
            String ripeness = intent.getStringExtra("ripeness");
            String ripenessStage = intent.getStringExtra("ripenessStage");
            String ripenessConfidence = intent.getStringExtra("ripenessConfidence");
            if (detectionContextInfo != null) {
                StringBuilder ctx = new StringBuilder();
                if (detectionCultivar != null && detectionPhase > 0) {
                    ctx.append(getString(R.string.detection_context_format, detectionCultivar, detectionPhase));
                } else if (detectionCultivar != null) {
                    ctx.append(detectionCultivar);
                }

                // Append ripeness/stage if provided (fruit scans)
                if (ripeness != null && !ripeness.isEmpty()) {
                    if (ctx.length() > 0) ctx.append(" • ");
                    ctx.append("Ripeness: ").append(ripeness);
                    if (ripenessStage != null && !ripenessStage.isEmpty()
                            && !"Unknown".equalsIgnoreCase(ripenessStage)) {
                        ctx.append(" (").append(ripenessStage).append(")");
                    }
                    if (ripenessConfidence != null && !ripenessConfidence.isEmpty()) {
                        ctx.append(" • ").append(ripenessConfidence);
                    }
                }

                if (ctx.length() > 0) {
                    detectionContextInfo.setText(ctx.toString());
                    detectionContextInfo.setVisibility(View.VISIBLE);
                } else {
                    detectionContextInfo.setVisibility(View.GONE);
                }
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
                        scoreText.setText(String.format(Locale.getDefault(), "%s - %s", parts[0], parts[1].substring(0, Math.min(parts[1].length(), 3))));
                    } else {
                        scoreText.setText(String.format(Locale.getDefault(), "%s - 0", cleanAcc.substring(0, Math.min(cleanAcc.length(), 4))));
                    }
                } else {
                    scoreText.setText(String.format(Locale.getDefault(), "%s - 0", cleanAcc.substring(0, Math.min(cleanAcc.length(), 4))));
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

            // Set accuracy with enhanced display
            String acc = intent.getStringExtra("accuracy");
            String confidenceWarning = intent.getStringExtra("confidenceWarning");
            
            StringBuilder accuracyText = new StringBuilder();
            
            // Add confidence level indicator
            if (acc != null && !acc.isEmpty()) {
                try {
                    float accuracyValue = Float.parseFloat(acc.replace("%", "").trim());
                    String confidenceLevel;
                    if (accuracyValue >= 80) {
                        confidenceLevel = "🟢 High Confidence";
                    } else if (accuracyValue >= 50) {
                        confidenceLevel = "🟡 Medium Confidence";
                    } else {
                        confidenceLevel = "🔴 Low Confidence";
                    }
                    accuracyText.append(confidenceLevel).append("\n");
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
            
            if (confidenceWarning != null && !confidenceWarning.isEmpty()) {
                accuracyText.append("\n⚠️ ").append(confidenceWarning).append("\n");
            }
            
            accuracyText.append("\nDetection Accuracy: ").append(acc != null ? acc : "N/A");
            
            if (topPredictions != null && !topPredictions.isEmpty()) {
                accuracyText.append("\n\n📊 Top Predictions:\n").append(topPredictions);
            }
            
            detectionAccuracy.setText(accuracyText.toString());
        }

        setupBottomNavigation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scan Results");
        }
    }

    /**
     * Load image from URI with proper error handling for FileProvider and file:// URIs
     */
    private void loadImageFromUri(String imageUriStr) {
        try {
            // Handle plain file paths (no scheme)
            if (imageUriStr != null && !imageUriStr.contains("://")) {
                File file = new File(imageUriStr);
                if (file.exists()) {
                    try {
                        // Try to convert to FileProvider URI if it's in app's external files
                        File externalFilesDir = getExternalFilesDir(null);
                        if (externalFilesDir != null && imageUriStr.contains(externalFilesDir.getAbsolutePath())) {
                            Uri fileProviderUri = FileProvider.getUriForFile(
                                this,
                                getPackageName() + ".provider",
                                file
                            );
                            detectionImage.setImageURI(fileProviderUri);
                            return;
                        } else {
                            // Direct file path
                            Bitmap bitmap = BitmapFactory.decodeFile(imageUriStr);
                            if (bitmap != null) {
                                detectionImage.setImageBitmap(bitmap);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading image from file path", e);
                    }
                }
            }
            
            Uri imageUri = Uri.parse(imageUriStr);
            
            // Handle FileProvider URIs (content://)
            if ("content".equals(imageUri.getScheme())) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            detectionImage.setImageBitmap(bitmap);
                            inputStream.close();
                            return;
                        }
                        inputStream.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image from content URI", e);
                }
            }
            
            // Handle file:// URIs
            if ("file".equals(imageUri.getScheme())) {
                try {
                    String filePath = imageUri.getPath();
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            // Try to convert to FileProvider URI if it's in app's external files
                            File externalFilesDir = getExternalFilesDir(null);
                            if (externalFilesDir != null && filePath.contains(externalFilesDir.getAbsolutePath())) {
                                Uri fileProviderUri = FileProvider.getUriForFile(
                                    this,
                                    getPackageName() + ".provider",
                                    file
                                );
                                detectionImage.setImageURI(fileProviderUri);
                                return;
                            } else {
                                // Direct file path
                                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                if (bitmap != null) {
                                    detectionImage.setImageBitmap(bitmap);
                                    return;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image from file URI", e);
                }
            }
            
            // Try direct URI loading as fallback
            detectionImage.setImageURI(imageUri);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            // Set default image on error
            detectionImage.setImageResource(R.mipmap.ic_logo);
        }
    }

}
