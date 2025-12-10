package com.android.tomatoapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.media.MediaScannerConnection;

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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class SimpleCaptureActivity extends AppCompatActivity {

    public static final String EXTRA_CAPTURED_IMAGE_URI = "captured_image_uri";

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private static final int CAMERA_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_capture);

        // Hide action bar for full-screen camera experience
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        previewView = findViewById(R.id.previewView);
        
        // Capture button
        View captureBtn = findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(v -> capturePhoto());
        
        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
                
                Preview preview = new Preview.Builder()
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to start camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "TomatoApp_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg";
        
        // Save to app's external files directory first (always accessible)
        File photoFile = new File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                fileName
        );
        
        // Ensure directory exists
        File parentDir = photoFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Save to public Pictures directory if possible
                        saveToPublicPictures(photoFile, fileName);
                        
                        // Scan the file so it appears in gallery
                        MediaScannerConnection.scanFile(
                                SimpleCaptureActivity.this,
                                new String[]{photoFile.getAbsolutePath()},
                                new String[]{"image/jpeg"},
                                null
                        );
                        
                        Uri photoUri = FileProvider.getUriForFile(
                                SimpleCaptureActivity.this,
                                getPackageName() + ".provider",
                                photoFile
                        );
                        
                        // Return the URI to the calling activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_CAPTURED_IMAGE_URI, photoUri.toString());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                        Toast.makeText(SimpleCaptureActivity.this,
                                "Failed to capture photo: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    
    private void saveToPublicPictures(File sourceFile, String fileName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - use MediaStore
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TomatoApp");
                
                Uri publicUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (publicUri != null) {
                    // Copy file content to public location
                    try (java.io.InputStream in = new java.io.FileInputStream(sourceFile);
                         java.io.OutputStream out = getContentResolver().openOutputStream(publicUri)) {
                        if (out != null) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                }
            } else {
                // Older Android - copy to public Pictures directory
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File tomatoAppDir = new File(picturesDir, "TomatoApp");
                if (!tomatoAppDir.exists()) {
                    tomatoAppDir.mkdirs();
                }
                
                File destFile = new File(tomatoAppDir, fileName);
                java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            // Silently fail - we still have the image in app directory
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}

