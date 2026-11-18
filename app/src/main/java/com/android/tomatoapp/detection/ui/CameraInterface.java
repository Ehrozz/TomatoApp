package com.android.tomatoapp.detection.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import com.android.tomatoapp.R;
import com.android.tomatoapp.common.models.DiseaseData;
import com.android.tomatoapp.common.models.DiseaseInfo;
import com.android.tomatoapp.detection.data.DetectionHistoryManager;
import com.android.tomatoapp.monitoring.data.PlantMonitoringEntity;
import com.android.tomatoapp.monitoring.data.PlantMonitoringRepository;
import com.android.tomatoapp.notifications.NotificationUseCases;
import com.android.tomatoapp.task.data.TaskSchedule;
import com.android.tomatoapp.workprogram.data.WorkProgramDataHelper;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;
import com.android.tomatoapp.workprogram.data.WorkProgramRepository;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.detection.utils.RipenessClassifier;

public class CameraInterface extends BaseBottomNavActivity {

    private static final String TAG = "CameraInterface";
    private static final float MIN_ACCEPTED_CONFIDENCE = 0.80f;
    private static final float MIN_PREDICTION_MARGIN = 0.15f;
    private static final float FRUIT_MIN_CONFIDENCE = 0.70f;
    private static final float FRUIT_MIN_MARGIN = 0.15f;
    private static final float FRUIT_NON_TOMATO_P = 0.40f;
    private static final float PEST_MIN_CONFIDENCE = 0.70f;
    private static final float PEST_MIN_MARGIN = 0.15f;
    private static final float PEST_NOT_PEST_P = 0.40f;
    private static final String DETECTION_DISCLAIMER =
            "AI-assisted screening only. Results are estimates and should be verified by observing the plant or consulting an agricultural specialist.";
    public static final String EXTRA_LINKED_PROGRAM_ID = "extra_linked_program_id";
    public static final String EXTRA_LINKED_CULTIVAR = "extra_linked_cultivar";
    public static final String EXTRA_LINKED_PHASE = "extra_linked_phase";
    public static final String EXTRA_DETECTION_TYPE = "extra_detection_type";
    public static final String EXTRA_LINKED_DATE = "extra_linked_date";

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private Interpreter tflite;
    private final ExecutorService detectionExecutor = Executors.newSingleThreadExecutor();
    private ArrayList<String> labels;
    private View captureButton;
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
    private String launchProgramId;
    private String linkedCultivarName;
    private int linkedPhase = -1;
    private String linkedDate;

    // New UI elements wired from redesigned layout
    private View connectivityBadge;
    private View connectivityDot;
    private TextView connectivityLabel;
    private TextView taskContextLabel;
    private View scanTypeBadge;
    private TextView scanTypeLabel;
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
        phaseAdapter = createBlackTextAdapter(phaseOptions);
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
        cultivarAdapter = createBlackTextAdapter(cultivarOptions);
        cultivarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cultivarSpinner.setAdapter(cultivarAdapter);
        cultivarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedCultivarLabel = getString(R.string.detection_cultivar_unspecified);
                    linkedProgramId = launchProgramId;
                    return;
                }
                int index = position - 1;
                if (index >= 0 && index < programOptions.size()) {
                    WorkProgramEntity entity = programOptions.get(index);
                    linkedProgramId = entity.id;
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

    private ArrayAdapter<String> createBlackTextAdapter(ArrayList<String> options) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(android.graphics.Color.BLACK);
                }
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(android.graphics.Color.BLACK);
                }
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }
        };
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

        setupBottomNavigation();

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
            launchProgramId = linkedProgramId;
            linkedCultivarName = launchIntent.getStringExtra(EXTRA_LINKED_CULTIVAR);
            linkedPhase = launchIntent.getIntExtra(EXTRA_LINKED_PHASE, -1);
            linkedDate = launchIntent.getStringExtra(EXTRA_LINKED_DATE);
            
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
        captureButton = findViewById(R.id.captureBtn);
        captureButton.setOnClickListener(v -> capturePhoto());
        
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

        // ── Connectivity badge ──────────────────────────────────────────────
        connectivityBadge = findViewById(R.id.connectivityBadge);
        connectivityDot   = findViewById(R.id.connectivityDot);
        connectivityLabel = findViewById(R.id.connectivityLabel);
        updateConnectivityBadge();

        // ── Task context label & scan-type chip ────────────────────────────
        taskContextLabel = findViewById(R.id.taskContextLabel);
        scanTypeBadge    = findViewById(R.id.scanTypeBadge);
        scanTypeLabel    = findViewById(R.id.scanTypeLabel);
        if (taskContextLabel != null && linkedCultivarName != null && !linkedCultivarName.isEmpty()) {
            taskContextLabel.setText(linkedCultivarName);
        }
        updateScanTypeLabel();

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

    private void showDetectionTypeDialog() {
        DetectionTypeDialog dialog = new DetectionTypeDialog(this, type -> {
            switch (type) {
                case LEAVES:
                    currentModelType = ModelType.LEAVES;
                    break;
                case PEST:
                    currentModelType = ModelType.PEST;
                    break;
                case FRUIT:
                default:
                    currentModelType = ModelType.FRUITS;
                    break;
            }

            // Force the next scan to load the model and labels for the new type.
            loadedModelType = null;
            loadLabels();
            updateScanTypeLabel();
        });
        dialog.show();
    }

    private void updateScanTypeLabel() {
        if (scanTypeLabel == null) return;
        switch (currentModelType) {
            case LEAVES:
                scanTypeLabel.setText("Leaves");
                break;
            case PEST:
                scanTypeLabel.setText("Pest");
                break;
            default:
                scanTypeLabel.setText("Fruits");
                break;
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
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(this, "Failed to start camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private String scanDate() {
        return !TextUtils.isEmpty(linkedDate) ? linkedDate
                : com.android.tomatoapp.common.utils.AppClock.today(this).toString();
    }

    private long scanTimestamp() {
        try {
            return java.time.LocalDate.parse(scanDate()).atTime(java.time.LocalTime.now())
                    .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) { return System.currentTimeMillis(); }
    }

    private void capturePhoto() {
        if (imageCapture == null) return;
        captureButton.setEnabled(false);

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

                        detectionExecutor.execute(() -> {
                            HashMap<String, String> detectionResults =
                                    runTeachableMachineDetection(photoUri);
                            runOnUiThread(() -> handleDetectionResult(detectionResults, photoUri));
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Error capturing image", exception);
                        Toast.makeText(CameraInterface.this,
                                "Failed to save photo: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        captureButton.setEnabled(true);
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
            Uri selectedImageUri = data.getData();
            
            // Check if image was selected
            if (selectedImageUri == null) {
                Toast.makeText(this, "Failed to load image. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Copy gallery image to app storage (same as camera photos) for consistent access
            Uri savedImageUri = copyGalleryImageToAppStorage(selectedImageUri);
            if (savedImageUri == null) {
                Toast.makeText(this, "Failed to save image. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            captureButton.setEnabled(false);
            detectionExecutor.execute(() -> {
                HashMap<String, String> detectionResults =
                        runTeachableMachineDetection(savedImageUri);
                runOnUiThread(() -> handleDetectionResult(detectionResults, savedImageUri));
            });
        }
    }
    
    /**
     * Copy gallery image to app's external files directory and return FileProvider URI
     * This ensures the image is accessible like camera photos
     */
    private Uri copyGalleryImageToAppStorage(Uri galleryUri) {
        try {
            // Create destination file in app's external files directory
            File destinationFile = new File(
                    getExternalFilesDir(null),
                    "gallery_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            .format(System.currentTimeMillis()) + ".jpg"
            );
            
            // Copy image from gallery to app storage
            try (InputStream inputStream = getContentResolver().openInputStream(galleryUri);
                 FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                
                if (inputStream == null) {
                    return null;
                }
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            // Scan file to make it visible in gallery
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{destinationFile.getAbsolutePath()},
                    null,
                    null
            );
            
            // Create FileProvider URI (same format as camera photos)
            Uri fileProviderUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    destinationFile
            );
            
            Toast.makeText(this,
                    "Image saved: " + destinationFile.getName(),
                    Toast.LENGTH_SHORT).show();
            
            return fileProviderUri;
        } catch (Exception e) {
            Log.e(TAG, "Error saving image", e);
            return null;
        }
    }

    private Bitmap decodeImage(Uri imageUri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream boundsStream = getContentResolver().openInputStream(imageUri)) {
            if (boundsStream == null) {
                return null;
            }
            BitmapFactory.decodeStream(boundsStream, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        int shorterSide = Math.min(bounds.outWidth, bounds.outHeight);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        while (shorterSide / (options.inSampleSize * 2) >= 448) {
            options.inSampleSize *= 2;
        }
        Bitmap bitmap;
        try (InputStream imageStream = getContentResolver().openInputStream(imageUri)) {
            if (imageStream == null) {
                return null;
            }
            bitmap = BitmapFactory.decodeStream(imageStream, null, options);
        }
        if (bitmap == null) {
            return null;
        }

        try (InputStream exifStream = getContentResolver().openInputStream(imageUri)) {
            if (exifStream == null) {
                return bitmap;
            }
            ExifInterface exif = new ExifInterface(exifStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            } else if (orientation == ExifInterface.ORIENTATION_FLIP_HORIZONTAL) {
                matrix.setScale(-1, 1);
            } else if (orientation == ExifInterface.ORIENTATION_FLIP_VERTICAL) {
                matrix.setScale(1, -1);
            } else if (orientation == ExifInterface.ORIENTATION_TRANSPOSE) {
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
            } else if (orientation == ExifInterface.ORIENTATION_TRANSVERSE) {
                matrix.setRotate(270);
                matrix.postScale(-1, 1);
            } else {
                return bitmap;
            }
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            if (rotated != bitmap) {
                bitmap.recycle();
            }
            return rotated;
        }
    }

    private ByteBuffer bitmapToInput(Bitmap bitmap, boolean rawPixels) {
        ByteBuffer input = ByteBuffer.allocateDirect(224 * 224 * 3 * 4)
                .order(ByteOrder.nativeOrder());
        int[] pixels = new int[224 * 224];
        bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224);
        for (int pixel : pixels) {
            input.putFloat(rawPixels ? (pixel >> 16) & 0xFF : ((pixel >> 16) & 0xFF) / 255.0f);
            input.putFloat(rawPixels ? (pixel >> 8) & 0xFF : ((pixel >> 8) & 0xFF) / 255.0f);
            input.putFloat(rawPixels ? pixel & 0xFF : (pixel & 0xFF) / 255.0f);
        }
        input.rewind();
        return input;
    }

    private String normalizeLabel(String raw) {
        return raw == null ? "" : raw.toLowerCase(Locale.ROOT)
                .replace('_', ' ').replace('-', ' ').replaceAll("\\s+", " ").trim();
    }

    private String friendlyLabel(String rawLabel, HashMap<String, String> labelMapping) {
        String normalized = normalizeLabel(rawLabel);
        String mapped = labelMapping.get(normalized);
        if (mapped != null) return mapped;
        if ("non tomato".equals(normalized)) return "Non-Tomato";
        if ("tomato leaf".equals(normalized)) return "Tomato Leaf";
        String[] words = normalized.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (result.length() > 0) result.append(" ");
            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        return result.toString();
    }

    private float meanLuminance(Bitmap bitmap) {
        long total = 0;
        int count = 0;
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);
                total += (299 * ((pixel >> 16) & 0xFF)
                        + 587 * ((pixel >> 8) & 0xFF)
                        + 114 * (pixel & 0xFF)) / 1000;
                count++;
            }
        }
        return count == 0 ? 0f : (float) total / count;
    }

    private float[] infer(ByteBuffer input) {
        float[][] output = new float[1][labels.size()];
        tflite.run(input, output);
        float sum = 0f;
        float maxVal = Float.NEGATIVE_INFINITY;
        float minVal = Float.POSITIVE_INFINITY;
        for (float val : output[0]) {
            sum += val;
            maxVal = Math.max(maxVal, val);
            minVal = Math.min(minVal, val);
        }
        if (minVal >= 0f && maxVal <= 1.0f && Math.abs(sum - 1.0f) < 0.01f) {
            return output[0].clone();
        }
        return applySoftmax(output[0]);
    }

    private HashMap<String, String> runTeachableMachineDetection(Uri imageUri) {
        HashMap<String, String> results = new HashMap<>();
        try {
            Bitmap bitmap = decodeImage(imageUri);
            if (bitmap == null) {
                results.put("title", "Image Error");
                results.put("accuracy", "0%");
                results.put("description", "Failed to decode image. Please ensure the image is valid.");
                return results;
            }

            // Extra signal derived from the image itself (non-static):
            // For FRUITS model runs, estimate ripeness + maturity stage (Green/Turning/Red).
            RipenessClassifier.Result ripenessResult = currentModelType == ModelType.FRUITS
                    ? RipenessClassifier.classify(bitmap) : null;
            
            Bitmap resized;
            if (currentModelType == ModelType.LEAVES) {
                int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
                int left = (bitmap.getWidth() - cropSize) / 2;
                int top = (bitmap.getHeight() - cropSize) / 2;
                Bitmap cropped = Bitmap.createBitmap(bitmap, left, top, cropSize, cropSize);
                resized = Bitmap.createScaledBitmap(cropped, 224, 224, true);
                if (cropped != bitmap) {
                    cropped.recycle();
                }
            } else if (currentModelType == ModelType.FRUITS || currentModelType == ModelType.PEST) {
                resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            } else {
                resized = preprocessImage(bitmap, 224, 224);
            }
            
            boolean shouldRecycleResized = resized != bitmap;
            if (shouldRecycleResized) {
                bitmap.recycle();
            }

            ByteBuffer input = bitmapToInput(resized,
                    currentModelType == ModelType.LEAVES || currentModelType == ModelType.FRUITS
                            || currentModelType == ModelType.PEST);
            Bitmap flipped = null;
            ByteBuffer flippedInput = null;
            if (currentModelType == ModelType.LEAVES || currentModelType == ModelType.FRUITS
                    || currentModelType == ModelType.PEST) {
                Matrix matrix = new Matrix();
                matrix.setScale(-1f, 1f);
                flipped = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(),
                        resized.getHeight(), matrix, true);
                flippedInput = bitmapToInput(flipped, true);
            }
            float imageLuminance = currentModelType == ModelType.FRUITS
                    ? meanLuminance(resized) : 0f;
            if (shouldRecycleResized) {
                resized.recycle();
            }
            if (flipped != null) {
                flipped.recycle();
            }

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
                
                // Enhanced model loading with error handling
                try {
                    MappedByteBuffer modelBuffer = loadModelFile(modelName);
                    Interpreter.Options options = new Interpreter.Options();
                    options.setNumThreads(4); // Use 4 threads for better performance
                    try {
                        // Try to enable XNNPACK for faster inference (may not be available on all devices)
                        options.setUseXNNPACK(true);
                    } catch (Exception e) {
                        // XNNPACK not available, continue without it
                    }
                    tflite = new Interpreter(modelBuffer, options);
                    if (tflite.getOutputTensor(0).shape().length < 2
                            || tflite.getOutputTensor(0).shape()[1] != labels.size()) {
                        tflite.close();
                        tflite = null;
                        results.put("title", "Model Configuration Error");
                        results.put("accuracy", "0%");
                        results.put("description", "Model output size does not match the loaded labels.");
                        return results;
                    }
                    loadedModelType = currentModelType;
                } catch (Exception e) {
                    Log.e(TAG, "Error loading TensorFlow model", e);
                    results.put("title", "Model Loading Error");
                    results.put("accuracy", "0%");
                    results.put("description", "Failed to load detection model: " + modelName + "\n\nError: " + e.getMessage() + "\n\nPlease ensure the model file exists in assets folder.");
                    return results;
                }
            }

            float[] probabilities;
            try {
                probabilities = infer(input);
                if (currentModelType == ModelType.LEAVES || currentModelType == ModelType.FRUITS
                        || currentModelType == ModelType.PEST) {
                    float[] flippedProbabilities = infer(flippedInput);
                    for (int i = 0; i < probabilities.length; i++) {
                        probabilities[i] = (probabilities[i] + flippedProbabilities[i]) / 2f;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error running ML inference", e);
                results.put("title", "Inference Error");
                results.put("accuracy", "0%");
                results.put("description", "Failed to run detection: " + e.getMessage());
                return results;
            }
            
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
            for (int i = 0; i < Math.min(3, topIndices.length); i++) {
                Log.d(TAG, "top3 " + labels.get(topIndices[i]).trim() + " "
                        + topProbs[i]);
            }

            // Adaptive threshold based on model type and dataset quality
            // Adjusted thresholds for better accuracy reporting
            // Minimum threshold for accepting a detection (was too low at 0.30)
            float threshold = 0.50f; // 50% minimum confidence for valid detection
            float highConfidenceThreshold = 0.75f; // 75% for high confidence
            float mediumConfidenceThreshold = 0.60f; // 60% for medium confidence
            
            String rawLabel = labels.get(maxIdx).trim();
            
            // Store top 3 predictions for user reference (always include for transparency)
            StringBuilder topPredictions = new StringBuilder();
            topPredictions.append("1. ").append(labels.get(topIndices[0]).trim())
                    .append(" (").append(String.format("%.1f%%", topProbs[0] * 100)).append(")");
            if (topProbs.length > 1 && topProbs[1] > 0.05f) {
                topPredictions.append("\n2. ").append(labels.get(topIndices[1]).trim())
                        .append(" (").append(String.format("%.1f%%", topProbs[1] * 100)).append(")");
            }
            if (topProbs.length > 2 && topProbs[2] > 0.05f) {
                topPredictions.append("\n3. ").append(labels.get(topIndices[2]).trim())
                        .append(" (").append(String.format("%.1f%%", topProbs[2] * 100)).append(")");
            }
            results.put("topPredictions", topPredictions.toString());
            
            // Store individual prediction scores for dynamic analysis
            results.put("primaryPrediction", labels.get(topIndices[0]).trim());
            results.put("primaryConfidence", String.format("%.1f%%", topProbs[0] * 100));
            if (topProbs.length > 1) {
                results.put("secondaryPrediction", labels.get(topIndices[1]).trim());
                results.put("secondaryConfidence", String.format("%.1f%%", topProbs[1] * 100));
            }

            HashMap<String, String> labelMapping = new HashMap<>();
            
            // Map labels based on current model type
            if (currentModelType == ModelType.FRUITS) {
                labelMapping.put("anthracnose", "Anthracnose (Colletotrichum spp.)");
                labelMapping.put("blossom end rot", "Blossom End Rot");
                labelMapping.put("blotchy ripening", "Blotchy Ripening");
                labelMapping.put("cracks", "Fruit Cracking");
                labelMapping.put("sun scald", "Sun Scald");
                labelMapping.put("worm", "Fruit Worm (Fruit Borer)");
                labelMapping.put("healthy", "Healthy Tomato");
            } else if (currentModelType == ModelType.LEAVES) {
                labelMapping.put("Tomato___Bacterial_spot", "Bacterial Spot (Xanthomonas spp.)");
                labelMapping.put("Tomato___Early_blight", "Early Blight (Alternaria solani)");
                labelMapping.put("Tomato___Late_blight", "Late Blight (Phytophthora infestans)");
                labelMapping.put("Tomato___Leaf_Mold", "Leaf Mold (Passalora fulva)");
                labelMapping.put("Tomato___Spider_mites Two-spotted_spider_mite",
                        "Spider Mites (Tetranychus urticae)");
                labelMapping.put("Tomato___Tomato_Yellow_Leaf_Curl_Virus",
                        "Tomato Leaf Curl Virus (TLCV)");
                labelMapping.put("Tomato___Tomato_mosaic_virus", "Tomato Mosaic Virus (ToMV)");
                labelMapping.put("Tomato___healthy", "Healthy Tomato");
            } else {
                labelMapping.put("fruites for pest", "No Pest (Fruit)");
                labelMapping.put("leaves for pest", "No Pest (Leaf)");
                labelMapping.put("lady beetle", "Lady Beetle");
                labelMapping.put("whiteflies leaf curl", "Whiteflies (Bemisia tabaci)");
                labelMapping.put("aphids", "Aphids");
                labelMapping.put("flea beetles", "Flea Beetles");
                labelMapping.put("leaf miner", "Leaf Miner");
                labelMapping.put("melon flies", "Melon Fly");
                labelMapping.put("not pest", "Not Pest");
                labelMapping.put("worms", "Worms (Caterpillars)");
            }

            String mappingKey = currentModelType == ModelType.LEAVES
                    ? rawLabel : normalizeLabel(rawLabel);
            String mappedLabel = labelMapping.getOrDefault(mappingKey, rawLabel);
            if (currentModelType == ModelType.LEAVES && "non-tomato".equals(rawLabel)) {
                results.put("accepted", "false");
                results.put("rejectionReason",
                        "This doesn't look like a tomato leaf. Please frame a single tomato leaf.");
            }
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

            boolean accepted;
            if (currentModelType == ModelType.FRUITS) {
                int nonTomatoIndex = -1;
                for (int i = 0; i < labels.size(); i++) {
                    if ("non tomato".equals(normalizeLabel(labels.get(i)))) {
                        nonTomatoIndex = i;
                        break;
                    }
                }
                String topLabel = normalizeLabel(rawLabel);
                float nonTomatoProbability = nonTomatoIndex >= 0
                        ? probabilities[nonTomatoIndex] : 0f;
                accepted = true;
                if ("non tomato".equals(topLabel) || nonTomatoProbability >= FRUIT_NON_TOMATO_P) {
                    accepted = false;
                    results.put("rejectionReason",
                            "No tomato fruit detected. Please frame a single tomato fruit.");
                } else if ("tomato leaf".equals(topLabel)) {
                    accepted = false;
                    results.put("rejectionReason",
                            "This looks like a leaf, not a fruit. Switch to the Leaves scan or frame a tomato fruit.");
                } else if (imageLuminance < 40f || imageLuminance > 220f) {
                    accepted = false;
                    results.put("rejectionReason",
                            "Image is too dark / overexposed. Retake in even daylight.");
                } else {
                    float minConfidence = "worm".equals(topLabel) ? 0.60f
                            : ("anthracnose".equals(topLabel) || "cracks".equals(topLabel)
                            ? 0.65f : FRUIT_MIN_CONFIDENCE);
                    if (maxProb < minConfidence) {
                        accepted = false;
                        results.put("rejectionReason", String.format(Locale.getDefault(),
                                "Confidence too low (%.1f%%, needs %.0f%%). Move closer, keep the fruit in focus, avoid glare and shadows.",
                                maxProb * 100f, minConfidence * 100f));
                    } else if (maxProb - secondProb < FRUIT_MIN_MARGIN) {
                        accepted = false;
                        results.put("rejectionReason", "Unsure between "
                                + friendlyLabel(labels.get(topIndices[0]), labelMapping) + " and "
                                + friendlyLabel(labels.get(topIndices[1]), labelMapping)
                                + ". Take a closer photo of the affected area.");
                    }
                }
            } else if (currentModelType == ModelType.PEST) {
                int notPestIndex = -1;
                for (int i = 0; i < labels.size(); i++) {
                    if ("not pest".equals(normalizeLabel(labels.get(i)))) {
                        notPestIndex = i;
                        break;
                    }
                }
                String pestTopLabel = normalizeLabel(rawLabel);
                float notPestProbability = notPestIndex >= 0
                        ? probabilities[notPestIndex] : 0f;
                accepted = true;
                if ("not pest".equals(pestTopLabel) || notPestProbability >= PEST_NOT_PEST_P) {
                    accepted = false;
                    results.put("rejectionReason",
                            "No pest detected. Please frame a close-up of the pest on a tomato plant.");
                } else if ("fruites for pest".equals(pestTopLabel)
                        || "leaves for pest".equals(pestTopLabel)) {
                    accepted = false;
                    results.put("rejectionReason",
                            "No pest found on this fruit/leaf. If you can see a pest, move closer and retake the photo.");
                } else if (maxProb < PEST_MIN_CONFIDENCE) {
                    accepted = false;
                    results.put("rejectionReason", String.format(Locale.getDefault(),
                            "Confidence too low (%.1f%%, needs %.0f%%). Move closer, keep the pest in focus, avoid glare and shadows.",
                            maxProb * 100f, PEST_MIN_CONFIDENCE * 100f));
                } else if (maxProb - secondProb < PEST_MIN_MARGIN) {
                    accepted = false;
                    results.put("rejectionReason", "Unsure between "
                            + friendlyLabel(labels.get(topIndices[0]), labelMapping) + " and "
                            + friendlyLabel(labels.get(topIndices[1]), labelMapping)
                            + ". Take a closer photo of the pest.");
                }
            } else {
                boolean nonTomatoLeaf = "non-tomato".equals(rawLabel);
                if (nonTomatoLeaf) {
                    accepted = false;
                    results.put("rejectionReason",
                            "This doesn't look like a tomato leaf. Please frame a single tomato leaf.");
                } else {
                    accepted = maxProb >= MIN_ACCEPTED_CONFIDENCE
                            && (maxProb - secondProb) >= MIN_PREDICTION_MARGIN;
                    if (!accepted) {
                        results.put("rejectionReason", String.format(Locale.getDefault(),
                                "Confidence must be at least %.0f%% and lead the next prediction by at least %.0f%%. Current result: %.1f%% confidence and %.1f%% lead.",
                                MIN_ACCEPTED_CONFIDENCE * 100f,
                                MIN_PREDICTION_MARGIN * 100f,
                                maxProb * 100f,
                                (maxProb - secondProb) * 100f));
                    }
                }
            }
            results.put("accepted", String.valueOf(accepted));

            // Generate dynamic results based on actual model predictions
            // Build comprehensive prediction analysis
            StringBuilder predictionAnalysis = new StringBuilder();
            predictionAnalysis.append("📊 Model Prediction Analysis:\n\n");
            predictionAnalysis.append("Primary Detection: ").append(labels.get(topIndices[0]).trim())
                    .append(" (").append(String.format("%.1f%%", topProbs[0] * 100)).append(" confidence)\n");
            
            if (topProbs.length > 1 && topProbs[1] > 0.05f) {
                predictionAnalysis.append("Secondary: ").append(labels.get(topIndices[1]).trim())
                        .append(" (").append(String.format("%.1f%%", topProbs[1] * 100)).append(")\n");
            }
            if (topProbs.length > 2 && topProbs[2] > 0.05f) {
                predictionAnalysis.append("Tertiary: ").append(labels.get(topIndices[2]).trim())
                        .append(" (").append(String.format("%.1f%%", topProbs[2] * 100)).append(")\n");
            }
            
            // Determine confidence level and generate dynamic warnings
            String confidenceWarning = "";
            String confidenceLevel = "";
            boolean isAmbiguous = secondProb > 0.20f && (maxProb - secondProb) < 0.25f;
            
            if (maxProb < threshold) {
                confidenceLevel = "Very Low";
                confidenceWarning = "⚠️ Very Low Confidence (" + String.format("%.1f%%", maxProb * 100) + 
                        ") - Model uncertainty is high. Consider retaking photo with better lighting/angle.";
            } else if (maxProb < mediumConfidenceThreshold) {
                confidenceLevel = "Low-Medium";
                confidenceWarning = "⚠️ Low-Medium Confidence (" + String.format("%.1f%%", maxProb * 100) + 
                        ") - Results should be interpreted with caution.";
            } else if (maxProb < highConfidenceThreshold) {
                confidenceLevel = "Medium";
                confidenceWarning = "⚠️ Medium Confidence (" + String.format("%.1f%%", maxProb * 100) + 
                        ") - Detection is reasonably reliable but consider additional verification.";
            } else {
                confidenceLevel = "High";
                if (isAmbiguous) {
                    confidenceWarning = "⚠️ High Confidence but Ambiguous - Multiple similar predictions detected.";
                }
            }
            
            if (isAmbiguous && maxProb >= threshold) {
                confidenceWarning = "⚠️ Ambiguous Detection - Multiple similar predictions. " +
                        "The model suggests " + labels.get(topIndices[0]).trim() + " (" + 
                        String.format("%.1f%%", topProbs[0] * 100) + ") but " + 
                        labels.get(topIndices[1]).trim() + " (" + 
                        String.format("%.1f%%", topProbs[1] * 100) + ") is also likely.";
            }
            
            // Generate dynamic description based on predictions
            String dynamicDescription = generateDynamicDescription(
                    mappedLabel, maxProb, topIndices, topProbs, labels, isAmbiguous, confidenceLevel
            );
            
            // Get static disease info as reference, but make it secondary to dynamic analysis
            DiseaseInfo staticInfo = DiseaseData.getDiseaseInfo(mappedLabel);
            
            // Build comprehensive results
            {
                // Valid detection - combine dynamic analysis with static info
                results.put("title", mappedLabel);
                results.put("accuracy", String.format("%.1f%%", maxProb * 100));
                
                // Build dynamic description with static info as supplementary
                StringBuilder fullDescription = new StringBuilder();
                fullDescription.append(dynamicDescription);
                
                if (isAmbiguous) {
                    fullDescription.append("\n\n").append(predictionAnalysis.toString());
                    fullDescription.append("\n⚠️ Note: Multiple conditions are possible. Please verify with additional images or expert consultation.");
                }
                
                if (staticInfo != null) {
                    // Add static info as reference, but mark it as supplementary
                    fullDescription.append("\n\n--- Reference Information ---\n");
                    if (!confidenceWarning.isEmpty()) {
                        fullDescription.append(confidenceWarning).append("\n\n");
                    }
                    fullDescription.append(staticInfo.getDescription());
                    if (accepted && currentModelType == ModelType.FRUITS
                            && "Healthy Tomato".equals(mappedLabel)
                            && ripenessResult != null
                            && !"Unknown".equals(ripenessResult.stage)) {
                        String stageDescription;
                        if ("Green".equals(ripenessResult.stage)) {
                            stageDescription = "Stage: Unripe (Green) - leave on the plant";
                        } else if ("Turning".equals(ripenessResult.stage)) {
                            stageDescription = "Stage: Turning - harvest in a few days";
                        } else {
                            stageDescription = "Stage: Ripe (Red) - ready to harvest";
                        }
                        fullDescription.append("\n").append(stageDescription);
                        results.put("ripeness", ripenessResult.ripeness);
                        results.put("ripenessStage", ripenessResult.stage);
                        results.put("ripenessConfidence", String.format(Locale.getDefault(),
                                "%.0f%%", ripenessResult.confidence01 * 100f));
                    }
                } else if (!confidenceWarning.isEmpty()) {
                    fullDescription.append("\n\n").append(confidenceWarning);
                }
                fullDescription.append("\n\n").append(DETECTION_DISCLAIMER);
                
                results.put("description", fullDescription.toString());
                
                // Use static info for symptoms/cause/cure, but add dynamic context
                if (staticInfo != null) {
                    results.put("symptoms", staticInfo.getSymptoms() + 
                            (isAmbiguous ? "\n\nNote: Symptoms may overlap with other conditions." : ""));
                    results.put("cause", staticInfo.getCause());
                    results.put("cure", staticInfo.getCure());
                    results.put("prevention", staticInfo.getPrevention());
                    results.put("pestTitle", staticInfo.getPest());
                    results.put("pestDescription", staticInfo.getPestDescription());
                } else {
                    // No static info available - use dynamic generation
                    results.put("symptoms", generateDynamicSymptoms(mappedLabel, maxProb));
                    results.put("cause", "Cause information not available in database. Model detected: " + mappedLabel);
                    results.put("cure", "Treatment information not available. Please consult agricultural expert.");
                    results.put("prevention", "Prevention strategies not available. Model confidence: " + 
                            String.format("%.1f%%", maxProb * 100));
                    results.put("pestTitle", "Unknown");
                    results.put("pestDescription", "Pest information not available in database.");
                }
                results.put("confidenceWarning", confidenceWarning);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error running detection", e);
        }
        return results;
    }

    private boolean isAcceptedDetection(HashMap<String, String> detectionResults) {
        return detectionResults != null
                && "true".equalsIgnoreCase(detectionResults.get("accepted"));
    }

    private void handleDetectionResult(HashMap<String, String> detectionResults, Uri imageUri) {
        captureButton.setEnabled(true);
        if (detectionResults == null || detectionResults.isEmpty()) {
            Toast.makeText(this, "Detection failed. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isAcceptedDetection(detectionResults)) {
            showRetakeMessage(detectionResults);
            return;
        }

        String imageUriString = imageUri.toString();
        DetectionHistoryManager.addHistory(
                this,
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
                linkedProgramId,
                detectionResults.getOrDefault("topPredictions", ""),
                detectionResults.getOrDefault("confidenceWarning", "")
        );
        DetectionHistoryManager.setScanDate(this, imageUriString, scanDate());
        persistDetectionForProgram(detectionResults, imageUri);
        NotificationUseCases.notifyDiseaseDetection(
                this,
                imageUriString,
                detectionResults,
                selectedCultivarLabel != null ? selectedCultivarLabel : "Tomato",
                selectedPhase
        );

        Intent intent = new Intent(this, DetectionResults.class);
        intent.putExtra("imageUri", imageUriString);
        for (String key : detectionResults.keySet()) {
            intent.putExtra(key, detectionResults.get(key));
        }
        intent.putExtra("detectionCultivar", selectedCultivarLabel);
        intent.putExtra("detectionPhase", selectedPhase);
        startActivity(intent);
    }

    private void showRetakeMessage(HashMap<String, String> detectionResults) {
        String message = detectionResults != null
                ? detectionResults.get("rejectionReason")
                : null;
        if (TextUtils.isEmpty(message)) {
            message = "The image could not be analyzed reliably.";
        }
        message += "\n\n" + (currentModelType == ModelType.FRUITS
                ? "Please retake a clear photo of one tomato fruit."
                : "Please retake a clear photo of one tomato leaf, fruit, or pest.");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Retake Photo")
                .setMessage(message)
                .setPositiveButton("Try Again", null)
                .create();
        dialog.setOnShowListener(dialogInterface -> {
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextSize(18);
                messageView.setLineSpacing(0f, 1.15f);
                messageView.setPadding(8, 8, 8, 8);
            }
            TextView titleView = dialog.findViewById(
                    getResources().getIdentifier("alertTitle", "id", getPackageName()));
            if (titleView != null) {
                titleView.setTextSize(21);
            }
            if (dialog.getWindow() != null) {
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90f);
                dialog.getWindow().setLayout(width, android.view.WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
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
                scanTimestamp(),
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
        try (android.content.res.AssetFileDescriptor assetFileDescriptor =
                     getAssets().openFd(modelName);
             FileInputStream fileInputStream =
                     new FileInputStream(assetFileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = fileInputStream.getChannel();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY,
                    assetFileDescriptor.getStartOffset(), assetFileDescriptor.getDeclaredLength());
        }
    }

    private void loadLabels() {
        labels = new ArrayList<>();
        String labelsFile;
        if (currentModelType == ModelType.FRUITS) {
            labelsFile = "fruits_labels.txt";
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
                    labels.add(line.replaceFirst("^\\d+[\\s:.\\-]+", "").trim());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading model labels", e);
        }
    }
    
    /**
     * Preprocess image with better quality scaling and normalization
     * Enhanced preprocessing for better detection accuracy
     */
    private Bitmap preprocessImage(Bitmap bitmap, int targetWidth, int targetHeight) {
        // Calculate scaling to maintain aspect ratio
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        
        // Calculate the scale factor to fit the image while maintaining aspect ratio
        float scaleWidth = (float) targetWidth / originalWidth;
        float scaleHeight = (float) targetHeight / originalHeight;
        float scale = Math.min(scaleWidth, scaleHeight);
        
        // Create scaled bitmap maintaining aspect ratio
        int scaledWidth = Math.round(originalWidth * scale);
        int scaledHeight = Math.round(originalHeight * scale);
        
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
        
        // Create final bitmap with target dimensions, centered
        Bitmap finalBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(finalBitmap);
        
        // Fill with black background (common for ML models)
        canvas.drawColor(android.graphics.Color.BLACK);
        
        // Center the scaled image
        float left = (targetWidth - scaledWidth) / 2f;
        float top = (targetHeight - scaledHeight) / 2f;
        canvas.drawBitmap(scaledBitmap, left, top, null);
        
        // Clean up
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle();
        }
        
        return finalBitmap;
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
     * Generate dynamic description based on actual model predictions
     */
    private String generateDynamicDescription(String mappedLabel, float maxProb, int[] topIndices, 
                                               float[] topProbs, ArrayList<String> labels, 
                                               boolean isAmbiguous, String confidenceLevel) {
        StringBuilder desc = new StringBuilder();
        
        desc.append("🔍 Detection Result:\n");
        desc.append("Detected Condition: ").append(mappedLabel).append("\n");
        desc.append("Confidence Level: ").append(confidenceLevel).append(" (").append(String.format(Locale.getDefault(), "%.1f%%", maxProb * 100)).append(")\n");
        
        if (isAmbiguous) {
            desc.append("\n⚠️ Ambiguous Detection:\n");
            desc.append("The model detected multiple possible conditions with similar confidence:\n");
            for (int i = 0; i < Math.min(3, topProbs.length); i++) {
                if (topProbs[i] > 0.15f) {
                    desc.append("• ").append(labels.get(topIndices[i]).trim())
                            .append(": ").append(String.format(Locale.getDefault(), "%.1f%%", topProbs[i] * 100)).append("\n");
                }
            }
            desc.append("\nRecommendation: Verify with additional images or consult an expert.");
        } else {
            desc.append("\n✅ Primary Detection:\n");
            desc.append("The model confidently identified this condition as: ").append(mappedLabel).append("\n");
            desc.append("Confidence Score: ").append(String.format(Locale.getDefault(), "%.1f%%", maxProb * 100));
            
            if (maxProb >= 0.75f) {
                desc.append("\n\nThis is a high-confidence detection. The model is very certain about this diagnosis.");
            } else if (maxProb >= 0.60f) {
                desc.append("\n\nThis is a medium-confidence detection. The result is reasonably reliable.");
            } else {
                desc.append("\n\nThis is a lower-confidence detection. Please verify with additional images.");
            }
        }
        
        // Add context about model performance
        if (maxProb < 0.50f) {
            desc.append("\n\n📸 Image Quality Recommendations:\n");
            desc.append("• Ensure good lighting conditions\n");
            desc.append("• Focus clearly on the affected area\n");
            desc.append("• Avoid shadows and reflections\n");
            desc.append("• Use appropriate model type (Fruits/Leaves/Pest)");
        }
        
        return desc.toString();
    }
    
    /**
     * Generate dynamic symptoms description based on detection
     */
    private String generateDynamicSymptoms(String mappedLabel, float confidence) {
        StringBuilder symptoms = new StringBuilder();
        symptoms.append("Based on model detection: ").append(mappedLabel).append("\n");
        symptoms.append("Detection Confidence: ").append(String.format(Locale.getDefault(), "%.1f%%", confidence * 100)).append("\n\n");
        
        if (confidence >= 0.75f) {
            symptoms.append("High confidence detection. The model identified specific symptoms associated with this condition.");
        } else if (confidence >= 0.50f) {
            symptoms.append("Medium confidence detection. Symptoms may be present but require verification.");
        } else {
            symptoms.append("Low confidence detection. Symptoms cannot be reliably determined from this image.");
        }
        
        symptoms.append("\n\nFor detailed symptom information, please refer to agricultural resources or consult an expert.");
        return symptoms.toString();
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

    /**
     * Updates the connectivity badge pill (Online / Offline) in the header.
     */
    private void updateConnectivityBadge() {
        if (connectivityLabel == null || connectivityDot == null) return;
        boolean online = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    android.net.Network net = cm.getActiveNetwork();
                    NetworkCapabilities caps = net != null ? cm.getNetworkCapabilities(net) : null;
                    online = caps != null
                            && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
                } else {
                    android.net.NetworkInfo info = cm.getActiveNetworkInfo();
                    online = info != null && info.isConnected();
                }
            }
        } catch (Exception ignored) { }

        if (online) {
            connectivityLabel.setText("Online");
            // Green background tint already set in XML; just ensure dot is green
            if (connectivityDot != null)
                connectivityDot.setBackgroundResource(R.drawable.circle_green);
            if (connectivityBadge != null)
                ((com.google.android.material.card.MaterialCardView) connectivityBadge)
                        .setCardBackgroundColor(0xCC2D7A3A); // dark-green semi-transparent
        } else {
            connectivityLabel.setText("Offline");
            if (connectivityDot != null)
                connectivityDot.setBackgroundResource(R.drawable.circle_orange);
            if (connectivityBadge != null)
                ((com.google.android.material.card.MaterialCardView) connectivityBadge)
                        .setCardBackgroundColor(0xCC6B7280); // muted grey semi-transparent
        }
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
                Toast.makeText(this, "Camera permission denied. Please open Phone Settings and allow camera permission to continue.", Toast.LENGTH_LONG).show();
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
        detectionExecutor.shutdownNow();
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
