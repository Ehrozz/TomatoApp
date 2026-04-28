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
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.detection.ui.CameraInterface;
import com.android.tomatoapp.detection.ui.DetectionTypeDialog;
import com.android.tomatoapp.detection.ui.SimpleCaptureActivity;
import com.android.tomatoapp.monitoring.data.PlantMonitoringEntity;
import com.android.tomatoapp.monitoring.data.PlantMonitoringRepository;

public class PlantMonitoringActivity extends BaseBottomNavActivity {

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
    private ImageView capturedImageView; // Keep as reference to referenceImage for logic consistency
    private MaterialCardView capturedImageCard; // Not used but kept for field compatibility if needed
    private ImageView cameraHintIcon;
    private TextView phaseBadge;

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

        setupBottomNavigation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
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
        // Map capturedImageView to referenceImage since we've merged them
        capturedImageView = findViewById(R.id.referenceImage);
        cameraHintIcon = findViewById(R.id.cameraHintIcon);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        phaseBadge = findViewById(R.id.phaseBadge);
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
        
        // Remove "Phase X: " or "Phase X · " prefix from the title to avoid redundancy with the badge
        String title = referenceInfo.phaseLabel;
        if (title != null && title.toLowerCase().contains("phase")) {
            title = title.replaceAll("(?i)Phase\\s*\\d+\\s*[:·\\-]\\s*", "");
        }
        referencePhaseLabel.setText(title);
        referenceDescription.setText(referenceInfo.description);
        if (phaseBadge != null) {
            phaseBadge.setText("PHASE " + phase);
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        // If user scanned a disease (CameraInterface -> DetectionResults) and comes back here,
        // show the latest scan image for this program in the white card.
        if (capturedImageUri == null && programId != null && !programId.isEmpty()) {
            try {
                java.util.ArrayList<org.json.JSONObject> history =
                        com.android.tomatoapp.detection.data.DetectionHistoryManager.getHistory(this);
                long latestTs = -1L;
                String latestUri = null;
                for (org.json.JSONObject entry : history) {
                    String entryProgramId = entry.optString("programId", "");
                    if (!programId.equals(entryProgramId)) continue;
                    long ts = entry.optLong("timestamp", 0L);
                    if (ts > latestTs) {
                        latestTs = ts;
                        latestUri = entry.optString("imageUri", null);
                    }
                }
                if (latestUri != null && !latestUri.isEmpty()) {
                    capturedImageUri = Uri.parse(latestUri);
                    displayCapturedImage(capturedImageUri);
                }
            } catch (Exception ignored) {
                // keep silent; monitoring can still be used without an image
            }
        }
    }
    
    private void displayCapturedImage(Uri imageUri) {
        if (capturedImageView == null) return;
        
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                capturedImageView.setImageBitmap(bitmap);
                
                // Dynamic UI updates for "Result" mode
                if (cameraHintIcon != null) cameraHintIcon.setVisibility(View.GONE);
                if (btnDeleteImage != null) btnDeleteImage.setVisibility(View.VISIBLE);
                
                if (referencePhaseLabel != null) {
                    referencePhaseLabel.setText("Monitoring Photo");
                }
                if (referenceDescription != null) {
                    referenceDescription.setText("Captured at " + new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date()));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void deleteCapturedImage() {
        if (capturedImageUri != null) {
            try {
                String uriString = capturedImageUri.toString();
                if (uriString.startsWith("file://")) {
                    File file = new File(uriString.replace("file://", ""));
                    if (file.exists()) file.delete();
                } else {
                    getContentResolver().delete(capturedImageUri, null, null);
                }
            } catch (Exception ignored) {}
        }
        
        // Restore initial state
        capturedImageUri = null;
        if (cameraHintIcon != null) cameraHintIcon.setVisibility(View.VISIBLE);
        if (btnDeleteImage != null) btnDeleteImage.setVisibility(View.GONE);
        setupReferenceCard(); // Restore original phase hints
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

