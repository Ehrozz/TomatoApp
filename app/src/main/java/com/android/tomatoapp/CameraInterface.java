package com.android.tomatoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.tomatoapp.notifications.NotificationUseCases;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CameraInterface extends AppCompatActivity {

    public static final String EXTRA_LINKED_PROGRAM_ID = "extra_linked_program_id";
    public static final String EXTRA_LINKED_CULTIVAR = "extra_linked_cultivar";
    public static final String EXTRA_LINKED_PHASE = "extra_linked_phase";
    public static final String EXTRA_DETECTION_TYPE = "extra_detection_type";

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private Interpreter tflite;
    private ArrayList<String> labels;
    private View modelSelectorBtn; // Can be Button or Chip
    private ModelType loadedModelType = null; // Track which model is currently loaded
    private Spinner cultivarSpinner;
    private Spinner phaseSpinner;
    private ArrayAdapter<String> cultivarAdapter;
    private ArrayAdapter<String> phaseAdapter;
    private final ArrayList<String> cultivarOptions = new ArrayList<>();
    private final ArrayList<String> phaseOptions = new ArrayList<>();
    private final ArrayList<WorkProgramEntity> programOptions = new ArrayList<>();
    private String selectedCultivarLabel;
    private int selectedPhase = 1;
    private WorkProgramRepository workProgramRepository;
    private PlantMonitoringRepository plantMonitoringRepository;
    private String linkedProgramId;
    private String linkedCultivarName;
    private int linkedPhase = -1;
    private static final SimpleDateFormat START_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    // Model types
    private enum ModelType {
        FRUITS,
        LEAVES,
        PEST
    }
    private ModelType currentModelType = ModelType.FRUITS;

    private void setupPhaseSpinner() {
        phaseOptions.clear();
        for (int i = 1; i <= 5; i++) {
            phaseOptions.add(getString(R.string.detection_phase_placeholder, i));
        }
        phaseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, phaseOptions);
        phaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        phaseSpinner.setAdapter(phaseAdapter);
        phaseSpinner.setSelection(Math.max(0, selectedPhase - 1));
        phaseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPhase = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupCultivarSpinner() {
        cultivarOptions.clear();
        cultivarOptions.add(getString(R.string.detection_cultivar_unspecified));
        cultivarAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cultivarOptions);
        cultivarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cultivarSpinner.setAdapter(cultivarAdapter);
        cultivarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedCultivarLabel = getString(R.string.detection_cultivar_unspecified);
                    return;
                }
                int index = position - 1;
                if (index >= 0 && index < programOptions.size()) {
                    WorkProgramEntity entity = programOptions.get(index);
                    selectedCultivarLabel = entity.cultivarName != null
                            ? entity.cultivarName
                            : getString(R.string.detection_cultivar_unspecified);
                    int suggestion = suggestPhase(entity);
                    if (suggestion >= 1 && suggestion <= 5) {
                        selectedPhase = suggestion;
                        phaseSpinner.setSelection(suggestion - 1);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadCultivarOptions() {
        if (workProgramRepository == null) return;
        workProgramRepository.loadAllForCurrentUser(items -> runOnUiThread(() -> {
            programOptions.clear();
            if (items != null) {
                programOptions.addAll(items);
            }
            cultivarOptions.clear();
            cultivarOptions.add(getString(R.string.detection_cultivar_unspecified));
            for (WorkProgramEntity entity : programOptions) {
                cultivarOptions.add(buildCultivarLabel(entity));
            }
            cultivarAdapter.notifyDataSetChanged();
            applyLinkedProgramSelection();
        }));
    }

    private String buildCultivarLabel(WorkProgramEntity entity) {
        String cultivar = entity.cultivarName != null ? entity.cultivarName : getString(R.string.detection_cultivar_unspecified);
        String start = entity.startingDate != null ? entity.startingDate : "N/A";
        return cultivar + " (" + start + ")";
    }

    private int suggestPhase(WorkProgramEntity entity) {
        int maturityDays = WorkProgramDataHelper.getMaturityDays(entity.cultivarName);
        if (maturityDays <= 0) maturityDays = 90;
        int dayNumber = calculateDayNumber(entity.startingDate);
        return TaskSchedule.getPhaseNumber(maturityDays, dayNumber);
    }

    private int calculateDayNumber(String startDate) {
        if (startDate == null) return 1;
        try {
            Date start = START_DATE_FORMAT.parse(startDate);
            if (start == null) return 1;
            long diff = System.currentTimeMillis() - start.getTime();
            return Math.max(1, (int) (diff / (1000 * 60 * 60 * 24)) + 1);
        } catch (ParseException e) {
            return 1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_interface);

        // Hide action bar for full-screen camera experience
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        plantMonitoringRepository = new PlantMonitoringRepository(this);
        previewView = findViewById(R.id.previewView);
        cultivarSpinner = findViewById(R.id.spinnerCultivar);
        phaseSpinner = findViewById(R.id.spinnerPhase);
        selectedCultivarLabel = getString(R.string.detection_cultivar_unspecified);
        Intent launchIntent = getIntent();
        if (launchIntent != null) {
            linkedProgramId = launchIntent.getStringExtra(EXTRA_LINKED_PROGRAM_ID);
            linkedCultivarName = launchIntent.getStringExtra(EXTRA_LINKED_CULTIVAR);
            linkedPhase = launchIntent.getIntExtra(EXTRA_LINKED_PHASE, -1);
            
            // Read detection type from Intent
            String detectionTypeStr = launchIntent.getStringExtra(EXTRA_DETECTION_TYPE);
            if (detectionTypeStr != null) {
                try {
                    DetectionTypeDialog.DetectionType detectionType = DetectionTypeDialog.DetectionType.valueOf(detectionTypeStr);
                    switch (detectionType) {
                        case FRUIT:
                            currentModelType = ModelType.FRUITS;
                            break;
                        case LEAVES:
                            currentModelType = ModelType.LEAVES;
                            break;
                        case PEST:
                            currentModelType = ModelType.PEST;
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid detection type, use default
                    currentModelType = ModelType.FRUITS;
                }
            }
            
            if (!TextUtils.isEmpty(linkedCultivarName)) {
                selectedCultivarLabel = linkedCultivarName;
            }
            if (linkedPhase > 0) {
                selectedPhase = linkedPhase;
            }
        }
        workProgramRepository = new WorkProgramRepository(this);
        setupPhaseSpinner();
        setupCultivarSpinner();
        loadCultivarOptions();
        
        // Capture button (now MaterialCardView)
        View captureBtn = findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(v -> capturePhoto());
        
        // Gallery button (hidden by default, can be accessed via menu)
        com.google.android.material.floatingactionbutton.FloatingActionButton openGalleryBtn = findViewById(R.id.openGalleryButton);
        openGalleryBtn.setOnClickListener(v -> openGallery());
        
        // Model selector button (now MaterialCardView) - hide it since selection happens before
        modelSelectorBtn = findViewById(R.id.modelSelectorBtn);
        if (modelSelectorBtn != null) {
            modelSelectorBtn.setVisibility(View.GONE);
        }
        
        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        
        // Menu button
        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> {
            // Show menu options (gallery, settings, etc.)
            openGalleryBtn.setVisibility(openGalleryBtn.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        
        // Header title
        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(R.string.scan_section);

        // Load labels from assets
        loadLabels();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Set preview scale type to ensure camera feed is visible
                previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
                
                // Build preview use case
                Preview preview = new Preview.Builder()
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Build image capture use case
                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Select back camera (or front if back is not available)
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind all use cases before rebinding
                cameraProvider.unbindAll();
                
                // Bind use cases to lifecycle - this ensures camera automatically starts/stops with activity lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to start camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(
                getExternalFilesDir(null),
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(System.currentTimeMillis()) + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri photoUri = FileProvider.getUriForFile(
                                CameraInterface.this,
                                getPackageName() + ".provider",
                                photoFile
                        );

                        MediaScannerConnection.scanFile(
                                CameraInterface.this,
                                new String[]{photoFile.getAbsolutePath()},
                                null,
                                null
                        );

                        Toast.makeText(CameraInterface.this,
                                "Photo saved: " + photoFile.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();

                        HashMap<String, String> detectionResults = runTeachableMachineDetection(photoUri);

                        // Save detection with all details to history
                        DetectionHistoryManager.addHistory(
                                CameraInterface.this,
                                photoUri.toString(),
                                detectionResults.get("title"),
                                detectionResults.get("accuracy"),
                                detectionResults.get("description"),
                                detectionResults.get("symptoms"),
                                detectionResults.get("cause"),
                                detectionResults.get("cure"),
                                detectionResults.getOrDefault("prevention", ""),   // add prevention if available
                                detectionResults.get("pestTitle"),
                                detectionResults.get("pestDescription"),
                                detectionResults.getOrDefault("pestImageUri", ""),  // add pest image if available
                                selectedCultivarLabel,
                                selectedPhase,
                                linkedProgramId
                        );
                        persistDetectionForProgram(detectionResults, photoUri);

                        NotificationUseCases.notifyDiseaseDetection(
                                CameraInterface.this,
                                photoUri.toString(),
                                detectionResults,
                                selectedCultivarLabel != null ? selectedCultivarLabel : "Tomato",
                                selectedPhase
                        );

                        // Pass results to DetectionResults activity
                        Intent intent = new Intent(CameraInterface.this, DetectionResults.class);
                        intent.putExtra("imageUri", photoUri.toString());
                        for (String key : detectionResults.keySet()) {
                            intent.putExtra(key, detectionResults.get(key));
                        }
                        intent.putExtra("detectionCultivar", selectedCultivarLabel);
                        intent.putExtra("detectionPhase", selectedPhase);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                        Toast.makeText(CameraInterface.this,
                                "Failed to save photo: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            
            // Check if image was selected
            if (selectedImage == null) {
                Toast.makeText(this, "Failed to load image. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            HashMap<String, String> detectionResults = runTeachableMachineDetection(selectedImage);
            
            // Check if detection was successful
            if (detectionResults == null || detectionResults.isEmpty()) {
                Toast.makeText(this, "Detection failed. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            String imageUriString = selectedImage.toString();
            
            DetectionHistoryManager.addHistory(
                    CameraInterface.this,
                    imageUriString,
                    detectionResults.get("title"),
                    detectionResults.get("accuracy"),
                    detectionResults.get("description"),
                    detectionResults.get("symptoms"),
                    detectionResults.get("cause"),
                    detectionResults.get("cure"),
                    detectionResults.getOrDefault("prevention", ""),
                    detectionResults.get("pestTitle"),
                    detectionResults.get("pestDescription"),
                    detectionResults.getOrDefault("pestImageUri", ""),
                    selectedCultivarLabel,
                    selectedPhase,
                    linkedProgramId
            );
            persistDetectionForProgram(detectionResults, selectedImage);

            NotificationUseCases.notifyDiseaseDetection(
                    CameraInterface.this,
                    imageUriString,
                    detectionResults,
                    selectedCultivarLabel != null ? selectedCultivarLabel : "Tomato",
                    selectedPhase
            );

            Intent intent = new Intent(CameraInterface.this, DetectionResults.class);
            intent.putExtra("imageUri", imageUriString);
            for (String key : detectionResults.keySet()) {
                intent.putExtra(key, detectionResults.get(key));
            }
            intent.putExtra("detectionCultivar", selectedCultivarLabel);
            intent.putExtra("detectionPhase", selectedPhase);
            startActivity(intent);

        }
    }

    private HashMap<String, String> runTeachableMachineDetection(Uri imageUri) {
        HashMap<String, String> results = new HashMap<>();
        try {
            // Load and preprocess image with better quality
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            if (bitmap == null) {
                results.put("title", "Error");
                results.put("accuracy", "0%");
                results.put("description", "Failed to load image.");
                return results;
            }
            
            // Better image preprocessing: use high-quality scaling and maintain aspect ratio
            Bitmap resized = preprocessImage(bitmap, 224, 224);

            // Improved pixel normalization - normalize to [0, 1] range
            ByteBuffer input = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).order(ByteOrder.nativeOrder());
            int[] pixels = new int[224 * 224];
            resized.getPixels(pixels, 0, 224, 0, 0, 224, 224);
            for (int pixel : pixels) {
                // Normalize RGB values to [0, 1] range
                float r = ((pixel >> 16) & 0xFF) / 255.0f;
                float g = ((pixel >> 8) & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;
                input.putFloat(r);
                input.putFloat(g);
                input.putFloat(b);
            }
            input.rewind();

            // Load model if not loaded or if model type changed
            if (tflite == null || loadedModelType != currentModelType) {
                if (tflite != null) {
                    tflite.close();
                    tflite = null;
                }
                String modelName;
                if (currentModelType == ModelType.FRUITS) {
                    modelName = "model_fruits.tflite";
                } else if (currentModelType == ModelType.LEAVES) {
                    modelName = "model_leaves.tflite";
                } else {
                    modelName = "model_pest.tflite";
                }
                tflite = new Interpreter(loadModelFile(modelName));
                loadedModelType = currentModelType;
            }

            float[][] output = new float[1][labels.size()];
            tflite.run(input, output);

            // Apply softmax to get proper probabilities (if model doesn't output probabilities)
            float[] probabilities = applySoftmax(output[0]);
            
            // Get top 3 predictions for better accuracy
            int[] topIndices = getTopKIndices(probabilities, 3);
            float[] topProbs = new float[3];
            for (int i = 0; i < 3; i++) {
                topProbs[i] = probabilities[topIndices[i]];
            }
            
            int maxIdx = topIndices[0];
            float maxProb = topProbs[0];
            float secondProb = topProbs.length > 1 ? topProbs[1] : 0f;
            float thirdProb = topProbs.length > 2 ? topProbs[2] : 0f;

            // Adaptive threshold based on model type and dataset quality
            // Lower threshold for imbalanced datasets (0.30-0.40 instead of 0.50)
            float threshold = 0.30f; // Lower threshold to handle imbalanced datasets
            float highConfidenceThreshold = 0.70f; // High confidence threshold
            
            String rawLabel = labels.get(maxIdx).trim().toLowerCase();
            
            // Store top 3 predictions for user reference
            StringBuilder topPredictions = new StringBuilder();
            topPredictions.append(labels.get(topIndices[0]).trim()).append(" (").append(String.format("%.1f%%", topProbs[0] * 100)).append(")");
            if (topProbs.length > 1 && topProbs[1] > 0.1f) {
                topPredictions.append(", ").append(labels.get(topIndices[1]).trim()).append(" (").append(String.format("%.1f%%", topProbs[1] * 100)).append(")");
            }
            if (topProbs.length > 2 && topProbs[2] > 0.1f) {
                topPredictions.append(", ").append(labels.get(topIndices[2]).trim()).append(" (").append(String.format("%.1f%%", topProbs[2] * 100)).append(")");
            }
            results.put("topPredictions", topPredictions.toString());

            HashMap<String, String> labelMapping = new HashMap<>();
            
            // Map labels based on current model type
            if (currentModelType == ModelType.FRUITS) {
                // Fruits model labels
                labelMapping.put("anthracnose", "Anthracnose (Colletotrichum spp.)");
                labelMapping.put("black leaf mold", "Black Leaf Mold (Pseudocercospora fuligena)");
                labelMapping.put("early blight", "Early Blight (Alternaria solani)");
                labelMapping.put("fusarium wilt", "Fusarium Wilt (Fusarium oxysporum)");
                labelMapping.put("late blight", "Late Blight (Phytophthora infestans)");
                labelMapping.put("yellow leaf curl", "Tomato Yellow Leaf Curl Virus (TYLCV)");
                labelMapping.put("healty", "Healthy Tomato");
                labelMapping.put("healthy", "Healthy Tomato");
            } else {
                // Leaves model labels
                labelMapping.put("early blight", "Early Blight (Alternaria solani)");
                labelMapping.put("healty", "Healthy Tomato");
                labelMapping.put("healthy", "Healthy Tomato");
                labelMapping.put("late blight", "Late Blight (Phytophthora infestans)");
                labelMapping.put("leaf mold", "Leaf Mold (Passalora fulva)");
                labelMapping.put("leaf curl", "Tomato Leaf Curl Virus (TLCV)");
            }

            String mappedLabel = labelMapping.getOrDefault(rawLabel, rawLabel);
            // If not found in mapping, capitalize first letter of each word
            if (mappedLabel.equals(rawLabel)) {
                String[] words = rawLabel.split("\\s+");
                StringBuilder sb = new StringBuilder();
                for (String word : words) {
                    if (sb.length() > 0) sb.append(" ");
                    if (word.length() > 0) {
                        sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
                    }
                }
                mappedLabel = sb.toString();
            }

            // Determine confidence level and add warnings
            String confidenceWarning = "";
            if (maxProb < threshold) {
                confidenceWarning = "⚠️ Very Low Confidence - Consider retaking photo with better lighting/angle";
            } else if (maxProb < highConfidenceThreshold) {
                confidenceWarning = "⚠️ Low Confidence - Top predictions: " + topPredictions.toString();
            } else if (secondProb > 0.25f && (maxProb - secondProb) < 0.15f) {
                confidenceWarning = "⚠️ Ambiguous Detection - Multiple similar predictions. Top: " + topPredictions.toString();
            }
            
            if (maxProb < threshold) {
                results.put("title", "Unknown / Low Confidence");
                results.put("accuracy", String.format("%.1f%%", maxProb * 100));
                results.put("description", "Low confidence detection. The model could not reliably identify the condition.\n\n" +
                        "Top predictions: " + topPredictions.toString() + "\n\n" +
                        "Recommendations:\n" +
                        "• Ensure good lighting\n" +
                        "• Focus clearly on the affected area\n" +
                        "• Try different angles\n" +
                        "• Use the appropriate model (Fruits vs Leaves)");
                results.put("symptoms", "No reliable symptoms detected.");
                results.put("cause", "Uncertain cause.");
                results.put("cure", "No reliable cure information.");
                results.put("prevention", "No reliable prevention information.");
                results.put("pestTitle", "Unknown");
                results.put("pestDescription", "No pest information.");
                results.put("confidenceWarning", confidenceWarning);
            } else {
                DiseaseInfo info = DiseaseData.getDiseaseInfo(mappedLabel);
                if (info != null) {
                    results.put("title", mappedLabel);
                    results.put("accuracy", String.format("%.1f%%", maxProb * 100));
                    
                    // Add confidence warning to description if needed
                    String description = info.getDescription();
                    if (!confidenceWarning.isEmpty()) {
                        description = confidenceWarning + "\n\n" + description;
                    }
                    results.put("description", description);
                    
                    results.put("symptoms", info.getSymptoms());
                    results.put("cause", info.getCause());
                    results.put("cure", info.getCure());
                    results.put("prevention", info.getPrevention());
                    results.put("pestTitle", info.getPest());
                    results.put("pestDescription", info.getPestDescription());
                    results.put("confidenceWarning", confidenceWarning);
                } else {
                    results.put("title", mappedLabel + " (Database Info Missing)");
                    results.put("accuracy", String.format("%.1f%%", maxProb * 100));
                    results.put("description", "Detection: " + mappedLabel + "\n" +
                            "Top predictions: " + topPredictions.toString() + "\n\n" +
                            (!confidenceWarning.isEmpty() ? confidenceWarning + "\n\n" : "") +
                            "No detailed information available in database for this condition.");
                    results.put("symptoms", "No data available.");
                    results.put("cause", "No data available.");
                    results.put("cure", "No data available.");
                    results.put("prevention", "No data available.");
                    results.put("pestTitle", "Unknown");
                    results.put("pestDescription", "No data available.");
                    results.put("confidenceWarning", confidenceWarning);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    private void applyLinkedProgramSelection() {
        if (linkedProgramId == null || cultivarSpinner == null || programOptions.isEmpty()) {
            return;
        }
        for (int i = 0; i < programOptions.size(); i++) {
            WorkProgramEntity entity = programOptions.get(i);
            if (linkedProgramId.equals(entity.id)) {
                int spinnerIndex = i + 1; // account for "unspecified" entry
                if (spinnerIndex < cultivarSpinner.getCount()) {
                    cultivarSpinner.setSelection(spinnerIndex);
                }
                return;
            }
        }
    }

    private void persistDetectionForProgram(HashMap<String, String> detectionResults, Uri imageUri) {
        if (linkedProgramId == null) {
            return;
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        if (plantMonitoringRepository == null) {
            plantMonitoringRepository = new PlantMonitoringRepository(this);
        }
        String entryId = UUID.randomUUID().toString();
        int phaseForEntry = linkedPhase > 0 ? linkedPhase : selectedPhase;
        String shortDescription = detectionResults.get("title");
        if (TextUtils.isEmpty(shortDescription)) {
            shortDescription = getString(R.string.scan_section);
        }
        String issues = detectionResults.get("symptoms");
        if (TextUtils.isEmpty(issues)) {
            issues = detectionResults.get("cause");
        }
        String warnings = detectionResults.get("confidenceWarning");
        if (TextUtils.isEmpty(warnings)) {
            warnings = detectionResults.get("prevention");
        }
        String description = detectionResults.get("description");
        String accuracy = detectionResults.get("accuracy");
        StringBuilder notesBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(accuracy)) {
            notesBuilder.append("Accuracy: ").append(accuracy);
        }
        if (!TextUtils.isEmpty(description)) {
            if (notesBuilder.length() > 0) {
                notesBuilder.append("\n");
            }
            notesBuilder.append(description);
        }
        if (notesBuilder.length() == 0) {
            notesBuilder.append(getString(R.string.monitor_detection_auto_note));
        }

        PlantMonitoringEntity entity = new PlantMonitoringEntity(
                entryId,
                currentUser.getUid(),
                linkedProgramId,
                phaseForEntry,
                System.currentTimeMillis(),
                shortDescription,
                issues,
                warnings,
                notesBuilder.toString(),
                entryId,
                imageUri != null ? imageUri.toString() : null
        );
        plantMonitoringRepository.saveEntry(entity);
        runOnUiThread(() ->
                Toast.makeText(this, R.string.monitor_detection_saved_to_program, Toast.LENGTH_SHORT).show()
        );
    }

    private MappedByteBuffer loadModelFile(String modelName) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(getAssets().openFd(modelName).getFileDescriptor())) {
            FileChannel fileChannel = fileInputStream.getChannel();
            long startOffset = getAssets().openFd(modelName).getStartOffset();
            long declaredLength = getAssets().openFd(modelName).getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private void loadLabels() {
        labels = new ArrayList<>();
        String labelsFile;
        if (currentModelType == ModelType.FRUITS) {
            labelsFile = "fruit_labels.txt";
        } else if (currentModelType == ModelType.LEAVES) {
            labelsFile = "leaves_labels.txt";
        } else {
            labelsFile = "pest_labels.txt";
        }
        try (InputStream is = getAssets().open(labelsFile)) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String[] lines = new String(buffer).split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // Parse format like "0 anthracnose" or "1 black leaf mold"
                    // Remove the index number and keep only the label name
                    String[] parts = line.split("\\s+", 2);
                    if (parts.length >= 2) {
                        labels.add(parts[1].trim());
                    } else {
                        labels.add(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Preprocess image with better quality scaling
     */
    private Bitmap preprocessImage(Bitmap bitmap, int targetWidth, int targetHeight) {
        // Use high-quality scaling
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
    }
    
    /**
     * Apply softmax to convert logits to probabilities
     */
    private float[] applySoftmax(float[] logits) {
        float[] probabilities = new float[logits.length];
        float maxLogit = logits[0];
        
        // Find max for numerical stability
        for (float logit : logits) {
            if (logit > maxLogit) {
                maxLogit = logit;
            }
        }
        
        // Compute sum of exp(logits - maxLogit)
        float sum = 0f;
        for (int i = 0; i < logits.length; i++) {
            probabilities[i] = (float) Math.exp(logits[i] - maxLogit);
            sum += probabilities[i];
        }
        
        // Normalize
        if (sum > 0) {
            for (int i = 0; i < probabilities.length; i++) {
                probabilities[i] /= sum;
            }
        }
        
        return probabilities;
    }
    
    /**
     * Get top K indices sorted by probability (descending)
     */
    private int[] getTopKIndices(float[] probabilities, int k) {
        int[] indices = new int[probabilities.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        
        // Sort indices by probability (descending)
        for (int i = 0; i < Math.min(k, probabilities.length); i++) {
            int maxIdx = i;
            for (int j = i + 1; j < probabilities.length; j++) {
                if (probabilities[indices[j]] > probabilities[indices[maxIdx]]) {
                    maxIdx = j;
                }
            }
            // Swap
            int temp = indices[i];
            indices[i] = indices[maxIdx];
            indices[maxIdx] = temp;
        }
        
        return indices;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // do nothing (no back action)
            return true;
        } else if (id == R.id.nav_home) {
            finish();
            return true;
        } else if (id == R.id.nav_profile) {
            return true;
        } else if (id == R.id.nav_settings) {
            return true;
        } else if (id == R.id.nav_logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // CameraX automatically handles camera lifecycle when bound with bindToLifecycle()
        // No need to manually restart camera here
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Camera will be automatically paused by lifecycle binding
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up TensorFlow Lite interpreter
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        // Unbind camera use cases
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
