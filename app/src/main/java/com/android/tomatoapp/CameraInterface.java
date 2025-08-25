package com.android.tomatoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraInterface extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private static final int CAMERA_PERMISSION_CODE = 100;

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
                        .setTargetRotation(Surface.ROTATION_0)
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
                        // ✅ Use FileProvider for safe URI
                        Uri photoUri = FileProvider.getUriForFile(
                                CameraInterface.this,
                                getPackageName() + ".provider",
                                photoFile
                        );

                        // ✅ Notify system gallery
                        MediaScannerConnection.scanFile(
                                CameraInterface.this,
                                new String[]{photoFile.getAbsolutePath()},
                                null,
                                null
                        );

                        Toast.makeText(CameraInterface.this,
                                "Photo saved: " + photoFile.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();

                        // ✅ View the photo immediately
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        viewIntent.setDataAndType(photoUri, "image/*");
                        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
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
        // ✅ Open system gallery safely
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivity(intent);
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
