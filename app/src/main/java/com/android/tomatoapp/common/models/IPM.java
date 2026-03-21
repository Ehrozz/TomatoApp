package com.android.tomatoapp.common.models;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class IPM extends BaseDrawerActivity {

    CardView ScanCard;
    CardView HistoryCard;
    CardView InformationCard;
    private WorkProgramRepository workProgramRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ipm);

        ScanCard = findViewById(R.id.scanCard);
        HistoryCard = findViewById(R.id.historyCard);
        InformationCard = findViewById(R.id.infoCard);

        workProgramRepository = new WorkProgramRepository(this);

        ScanCard.setOnClickListener(v -> showDetectionTypeDialog());

        HistoryCard.setOnClickListener(v -> {
            Intent intent = new Intent(IPM.this, DetectionHistoryActivity.class);
            startActivity(intent);
        });

        InformationCard.setOnClickListener(v -> {
            Intent intent = new Intent(IPM.this, InformationInterface.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.integrated_pest_management);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
    }

    private void showDetectionTypeDialog() {
        DetectionTypeDialog dialog = new DetectionTypeDialog(this, type -> {
            // After detection type is selected, show work program selection if available
            workProgramRepository.loadAllForCurrentUser(programs -> {
                runOnUiThread(() -> {
                    if (programs != null && !programs.isEmpty()) {
                        showWorkProgramSelectionDialog(programs, type);
                    } else {
                        // No work programs, launch scanner without program info
                        launchCameraInterface(type, null, null, -1);
                    }
                });
            });
        });
        dialog.show();
    }

    private void showWorkProgramSelectionDialog(List<WorkProgramEntity> programs, DetectionTypeDialog.DetectionType detectionType) {
        WorkProgramSelectionDialog dialog = new WorkProgramSelectionDialog(
            this,
            programs,
            new WorkProgramSelectionDialog.OnWorkProgramSelectedListener() {
                @Override
                public void onWorkProgramSelected(WorkProgramEntity program) {
                    // Calculate phase based on start date
                    int phase = calculatePhase(program.startingDate, program.cultivarName);
                    launchCameraInterface(detectionType, program.id, 
                            program.cultivarName, phase);
                }
                
                @Override
                public void onContinueWithoutProgram() {
                    launchCameraInterface(detectionType, null, null, -1);
                }
            }
        );
        dialog.show();
    }

    private int calculatePhase(String startDate, String cultivarName) {
        if (startDate == null) return 1;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date start = sdf.parse(startDate);
            if (start == null) return 1;
            long diff = System.currentTimeMillis() - start.getTime();
            int dayNumber = Math.max(1, (int) (diff / (1000 * 60 * 60 * 24)) + 1);
            int maturityDays = WorkProgramDataHelper.getMaturityDays(cultivarName);
            if (maturityDays <= 0) maturityDays = 90;
            return TaskSchedule.getPhaseNumber(maturityDays, dayNumber);
        } catch (Exception e) {
            return 1;
        }
    }

    private void launchCameraInterface(DetectionTypeDialog.DetectionType detectionType, 
                                      String programId, String cultivar, int phase) {
        Intent intent = new Intent(IPM.this, CameraInterface.class);
        intent.putExtra(CameraInterface.EXTRA_DETECTION_TYPE, detectionType.name());
        if (programId != null) {
            intent.putExtra(CameraInterface.EXTRA_LINKED_PROGRAM_ID, programId);
        }
        if (cultivar != null) {
            intent.putExtra(CameraInterface.EXTRA_LINKED_CULTIVAR, cultivar);
        }
        if (phase > 0) {
            intent.putExtra(CameraInterface.EXTRA_LINKED_PHASE, phase);
        }
        startActivity(intent);
    }

}
