package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
    private TextView referencePhaseLabel;
    private TextView referenceDescription;
    private MaterialButton btnSave;
    private MaterialButton btnScan;
    private TextInputEditText inputNotes;

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
        inputNotes = findViewById(R.id.inputNotes);
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
                null
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

