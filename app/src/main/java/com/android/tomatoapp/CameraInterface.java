package com.android.tomatoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
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

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraInterface extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private Interpreter tflite;
    private ArrayList<String> labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_interface);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tomato App");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        previewView = findViewById(R.id.previewView);
        Button captureBtn = findViewById(R.id.captureBtn);
        ImageButton openGalleryBtn = findViewById(R.id.openGalleryButton);

        captureBtn.setOnClickListener(v -> capturePhoto());
        openGalleryBtn.setOnClickListener(v -> openGallery());

        loadLabels(); // Load labels from assets

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
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
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

                        // Run Teachable Machine detection
                        HashMap<String, String> detectionResults = runTeachableMachineDetection(photoUri);

                        // ✅ Save detection with all details to history
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
                                detectionResults.getOrDefault("pestImageUri", "")  // add pest image if available
                        );

                        // Pass results to DetectionResults activity
                        Intent intent = new Intent(CameraInterface.this, DetectionResults.class);
                        intent.putExtra("imageUri", photoUri.toString());
                        for (String key : detectionResults.keySet()) {
                            intent.putExtra(key, detectionResults.get(key));
                        }
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
            HashMap<String, String> detectionResults = runTeachableMachineDetection(selectedImage);

            DetectionHistoryManager.addHistory(
                    CameraInterface.this,
                    selectedImage.toString(),
                    detectionResults.get("title"),
                    detectionResults.get("accuracy"),
                    detectionResults.get("description"),
                    detectionResults.get("symptoms"),
                    detectionResults.get("cause"),
                    detectionResults.get("cure"),
                    detectionResults.getOrDefault("prevention", ""),
                    detectionResults.get("pestTitle"),
                    detectionResults.get("pestDescription"),
                    detectionResults.getOrDefault("pestImageUri", "")
            );

            Intent intent = new Intent(CameraInterface.this, DetectionResults.class);
            intent.putExtra("imageUri", selectedImage.toString());
            for (String key : detectionResults.keySet()) {
                intent.putExtra(key, detectionResults.get(key));
            }
            startActivity(intent);

        }
    }

    private HashMap<String, String> runTeachableMachineDetection(Uri imageUri) {
        HashMap<String, String> results = new HashMap<>();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

            ByteBuffer input = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).order(ByteOrder.nativeOrder());
            int[] pixels = new int[224 * 224];
            resized.getPixels(pixels, 0, 224, 0, 0, 224, 224);
            for (int pixel : pixels) {
                input.putFloat(((pixel >> 16) & 0xFF) / 255.f);
                input.putFloat(((pixel >> 8) & 0xFF) / 255.f);
                input.putFloat((pixel & 0xFF) / 255.f);
            }

            if (tflite == null) {
                tflite = new Interpreter(loadModelFile("model_unquant.tflite"));
            }

            float[][] output = new float[1][labels.size()];
            tflite.run(input, output);

            int maxIdx = 0;
            float maxProb = 0f;
            for (int i = 0; i < output[0].length; i++) {
                if (output[0][i] > maxProb) {
                    maxProb = output[0][i];
                    maxIdx = i;
                }
            }

            float threshold = 0.50f;
            String rawLabel = labels.get(maxIdx).trim();

            HashMap<String, String> labelMapping = new HashMap<>();
            labelMapping.put("Healthy Tomato", "Healthy Tomato");
            labelMapping.put("Black Leaf Mold", "Black Leaf Mold (Pseudocercospora fuligena)");
            labelMapping.put("Anthracnose", "Anthracnose (Colletotrichum spp.)");
            labelMapping.put("Fusarium Wilt", "Fusarium Wilt (Fusarium oxysporum)");
            labelMapping.put("Bacterial Wilt", "Bacterial Wilt (Ralstonia solanacearum)");
            labelMapping.put("Late Blight", "Late Blight (Phytophthora infestans)");
            labelMapping.put("Early Blight", "Early Blight (Alternaria solani)");
            labelMapping.put("Leaf Curl", "Tomato Leaf Curl Virus (TLCV)");

            String mappedLabel = labelMapping.getOrDefault(rawLabel, rawLabel);

            if (maxProb < threshold) {
                results.put("title", "Unknown");
                results.put("accuracy", String.format("%.2f%%", maxProb * 100));
                results.put("description", "Low confidence detection.");
                results.put("symptoms", "No reliable symptoms detected.");
                results.put("cause", "Uncertain cause.");
                results.put("cure", "No reliable cure information.");
                results.put("prevention", "No reliable prevention information.");
                results.put("pestTitle", "Unknown");
                results.put("pestDescription", "No pest information.");
            } else {
                DiseaseInfo info = DiseaseData.getDiseaseInfo(mappedLabel);
                if (info != null) {
                    results.put("title", mappedLabel);
                    results.put("accuracy", String.format("%.2f%%", maxProb * 100));
                    results.put("description", info.getDescription());
                    results.put("symptoms", info.getSymptoms());
                    results.put("cause", info.getCause());
                    results.put("cure", info.getCure());
                    results.put("prevention", info.getPrevention());
                    results.put("pestTitle", info.getPest());
                    results.put("pestDescription", info.getPestDescription());
                } else {
                    results.put("title", "Unknown");
                    results.put("accuracy", String.format("%.2f%%", maxProb * 100));
                    results.put("description", "No matching disease found in database.");
                    results.put("symptoms", "No data.");
                    results.put("cause", "Unknown.");
                    results.put("cure", "No data.");
                    results.put("prevention", "No data.");
                    results.put("pestTitle", "Unknown");
                    results.put("pestDescription", "No data.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
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
        try (InputStream is = getAssets().open("labels.txt")) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String[] lines = new String(buffer).split("\n");
            for (String line : lines) {
                labels.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            finish();
            return true;
        } else if (id == R.id.nav_home) {
            finish();
            return true;
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, DetectionHistoryActivity.class));
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
}
