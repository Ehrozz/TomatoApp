package com.android.tomatoapp.monitoring.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.tomatoapp.notifications.MonitoringReminderScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import com.android.tomatoapp.R;
import com.android.tomatoapp.common.utils.ReferenceImageProvider;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.android.tomatoapp.core.ui.BaseDrawerActivity;
import com.android.tomatoapp.detection.ui.CameraInterface;
import com.android.tomatoapp.detection.ui.DetectionTypeDialog;
import com.android.tomatoapp.detection.ui.SimpleCaptureActivity;
import com.android.tomatoapp.monitoring.data.PlantMonitoringEntity;
import com.android.tomatoapp.monitoring.data.PlantMonitoringRepository;

public class PlantMonitoringActivity extends BaseDrawerActivity {

    public static final String EXTRA_PROGRAM_ID = "programId";
    public static final String EXTRA_CULTIVAR = "cultivar";
    public static final String EXTRA_PHASE = "phase";
    public static final String EXTRA_SELECTED_DATE = "selectedDate";

    private ImageView referenceImage;
    private TextView referencePhaseLabel;
    private TextView referenceDescription;
    private MaterialButton btnSave;
    private MaterialButton btnScan;
    private MaterialButton btnCapture;
    private MaterialButton btnDeleteImage;
    private TextInputEditText inputNotes;
    private ImageView capturedImageView;
    private MaterialCardView capturedImageCard;

    private PlantMonitoringRepository repository;
    private ReferenceImageProvider.ReferenceImage referenceInfo;

    private String programId;
    private String cultivarName;
    private int phase = 1;
    private String selectedDate;
    private Uri capturedImageUri;
    private static final int REQUEST_CAPTURE_IMAGE = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_monitoring);

        setupDrawer();

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
        btnSave = findViewById(R.id.btnSave);
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
            selectedDate = intent.getStringExtra(EXTRA_SELECTED_DATE);
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
        
        // Check if offline and warn user
        if (!LocalDataManager.isOnline(this)) {
            Toast.makeText(this, "Saving offline. Data will sync when online.", Toast.LENGTH_SHORT).show();
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
                capturedImageUri != null ? capturedImageUri.toString() : null
        );

        try {
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
        } catch (Exception e) {
            Toast.makeText(this, "Error saving monitoring entry: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void launchDiseaseScanner() {
        if (programId == null || programId.isEmpty()) {
            Toast.makeText(this, R.string.monitor_scan_requires_program, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show detection type selection dialog
        DetectionTypeDialog dialog = new DetectionTypeDialog(this, type -> {
            Intent intent = new Intent(this, CameraInterface.class);
            intent.putExtra(CameraInterface.EXTRA_DETECTION_TYPE, type.name());
            intent.putExtra(CameraInterface.EXTRA_LINKED_PROGRAM_ID, programId);
            if (cultivarName != null) {
                intent.putExtra(CameraInterface.EXTRA_LINKED_CULTIVAR, cultivarName);
            }
            intent.putExtra(CameraInterface.EXTRA_LINKED_PHASE, phase);
            startActivity(intent);
        });
        dialog.show();
    }

    private String getTextValue(@Nullable TextInputEditText editText) {
        if (editText == null) return "";
        CharSequence text = editText.getText();
        return text != null ? text.toString().trim() : "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK && data != null) {
            String imageUriString = data.getStringExtra(SimpleCaptureActivity.EXTRA_CAPTURED_IMAGE_URI);
            if (imageUriString != null) {
                capturedImageUri = Uri.parse(imageUriString);
                displayCapturedImage(capturedImageUri);
            }
        }
    }
    
    private void displayCapturedImage(Uri imageUri) {
        if (capturedImageView == null || capturedImageCard == null) return;
        
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                capturedImageView.setImageBitmap(bitmap);
                capturedImageCard.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void deleteCapturedImage() {
        if (capturedImageUri != null) {
            try {
                // Delete the file if it exists
                String uriString = capturedImageUri.toString();
                if (uriString.startsWith("file://")) {
                    File file = new File(uriString.replace("file://", ""));
                    if (file.exists()) {
                        file.delete();
                    }
                } else {
                    // For content URIs, try to delete via content resolver
                    int deleted = getContentResolver().delete(capturedImageUri, null, null);
                    if (deleted > 0) {
                        Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                // Ignore deletion errors
            }
        }
        
        // Clear UI
        capturedImageUri = null;
        if (capturedImageView != null) {
            capturedImageView.setImageDrawable(null);
        }
        if (capturedImageCard != null) {
            capturedImageCard.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

