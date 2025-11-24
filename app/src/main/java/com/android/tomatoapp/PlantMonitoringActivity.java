package com.android.tomatoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.tomatoapp.notifications.MonitoringReminderScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class PlantMonitoringActivity extends AppCompatActivity {

    public static final String EXTRA_PROGRAM_ID = "programId";
    public static final String EXTRA_CULTIVAR = "cultivar";
    public static final String EXTRA_PHASE = "phase";
    public static final String EXTRA_SELECTED_DATE = "selectedDate";

    private ImageView referenceImage;
    private ImageView capturedImageView;
    private com.google.android.material.card.MaterialCardView capturedImageCard;
    private TextView referencePhaseLabel;
    private TextView referenceDescription;
    private MaterialButton btnSave;
    private MaterialButton btnScan;
    private MaterialButton btnCapture;
    private MaterialButton btnDeleteImage;
    private TextInputEditText inputNotes;
    
    private static final int REQUEST_CAPTURE_IMAGE = 200;
    private String capturedImageUri;

    private PlantMonitoringRepository repository;
    private ReferenceImageProvider.ReferenceImage referenceInfo;

    private String programId;
    private String cultivarName;
    private int phase = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_monitoring);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.monitor_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = new PlantMonitoringRepository(this);
        bindViews();
        extractExtras();
        setupReferenceCard();
        setupActions();
    }

    private void bindViews() {
        referenceImage = findViewById(R.id.referenceImage);
        referencePhaseLabel = findViewById(R.id.referencePhaseLabel);
        referenceDescription = findViewById(R.id.referenceDescription);
        btnSave = findViewById(R.id.btnSaveMonitoring);
        btnScan = findViewById(R.id.btnScanDiseases);
        btnCapture = findViewById(R.id.btnCapture);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        inputNotes = findViewById(R.id.inputNotes);
        capturedImageView = findViewById(R.id.capturedImageView);
        capturedImageCard = findViewById(R.id.capturedImageCard);
    }

    private void extractExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            programId = intent.getStringExtra(EXTRA_PROGRAM_ID);
            cultivarName = intent.getStringExtra(EXTRA_CULTIVAR);
            phase = intent.getIntExtra(EXTRA_PHASE, 1);
        }
    }

    private void setupReferenceCard() {
        referenceInfo = ReferenceImageProvider.getReference(this, cultivarName, phase);
        referenceImage.setImageDrawable(ContextCompat.getDrawable(this, referenceInfo.imageRes));
        referencePhaseLabel.setText(referenceInfo.phaseLabel);
        referenceDescription.setText(referenceInfo.description);
    }

    private void setupActions() {
        btnSave.setOnClickListener(v -> saveEntry());
        if (btnScan != null) {
            btnScan.setOnClickListener(v -> launchDiseaseScanner());
        }
        if (btnCapture != null) {
            btnCapture.setOnClickListener(v -> launchCapture());
        }
        if (btnDeleteImage != null) {
            btnDeleteImage.setOnClickListener(v -> deleteCapturedImage());
        }
    }
    
    private void launchCapture() {
        Intent intent = new Intent(this, SimpleCaptureActivity.class);
        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
    }

    private void saveEntry() {
        String notes = getTextValue(inputNotes);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || programId == null) {
            Toast.makeText(this, "No active user/program.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (notes.isEmpty()) {
            inputNotes.setError(getString(R.string.monitor_missing_description));
            return;
        }
        String entryId = UUID.randomUUID().toString();
        PlantMonitoringEntity entity = new PlantMonitoringEntity(
                entryId,
                user.getUid(),
                programId,
                phase,
                System.currentTimeMillis(),
                referenceInfo.description,
                referenceInfo.issueHint,
                referenceInfo.warningHint,
                notes,
                null,
                capturedImageUri
        );

        repository.saveEntry(entity);
        MonitoringReminderScheduler.scheduleFollowUp(
                this,
                programId,
                cultivarName != null ? cultivarName : "Work program",
                phase
        );
        Toast.makeText(this, R.string.monitor_saved_success, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void launchDiseaseScanner() {
        if (programId == null || programId.isEmpty()) {
            Toast.makeText(this, R.string.monitor_scan_requires_program, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CameraInterface.class);
        intent.putExtra(CameraInterface.EXTRA_LINKED_PROGRAM_ID, programId);
        if (cultivarName != null) {
            intent.putExtra(CameraInterface.EXTRA_LINKED_CULTIVAR, cultivarName);
        }
        intent.putExtra(CameraInterface.EXTRA_LINKED_PHASE, phase);
        startActivity(intent);
    }

    private String getTextValue(@Nullable TextInputEditText editText) {
        if (editText == null) return "";
        CharSequence text = editText.getText();
        return text != null ? text.toString().trim() : "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK && data != null) {
            String imageUriString = data.getStringExtra(SimpleCaptureActivity.EXTRA_CAPTURED_IMAGE_URI);
            if (imageUriString != null) {
                capturedImageUri = imageUriString;
                displayCapturedImage(imageUriString);
                Toast.makeText(this, "Image captured successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void displayCapturedImage(String imageUriString) {
        if (capturedImageView == null || capturedImageCard == null) return;
        
        try {
            Uri imageUri = Uri.parse(imageUriString);
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                capturedImageView.setImageBitmap(bitmap);
                capturedImageCard.setVisibility(View.VISIBLE);
                inputStream.close();
                return;
            }
        } catch (FileNotFoundException e) {
            // Try as file path
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Try loading as file path
        try {
            File imageFile = new File(imageUriString);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                capturedImageView.setImageBitmap(bitmap);
                capturedImageCard.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteCapturedImage() {
        if (capturedImageUri == null || capturedImageUri.isEmpty()) {
            return;
        }
        
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this captured image?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the image file
                    try {
                        Uri imageUri = Uri.parse(capturedImageUri);
                        if (imageUri.getScheme() != null && imageUri.getScheme().startsWith("content")) {
                            // Try to delete via ContentResolver
                            getContentResolver().delete(imageUri, null, null);
                        } else {
                            // Try as file path
                            File imageFile = new File(capturedImageUri);
                            if (imageFile.exists()) {
                                imageFile.delete();
                            }
                        }
                        
                        // Clear the image and hide the card
                        capturedImageUri = null;
                        capturedImageView.setImageDrawable(null);
                        capturedImageCard.setVisibility(View.GONE);
                        Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

