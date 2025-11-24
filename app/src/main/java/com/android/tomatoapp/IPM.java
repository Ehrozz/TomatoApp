package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class IPM extends BaseDrawerActivity {

    CardView ScanCard;
    CardView HistoryCard;
    CardView InformationCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ipm);

        ScanCard = findViewById(R.id.scanCard);
        HistoryCard = findViewById(R.id.historyCard);
        InformationCard = findViewById(R.id.infoCard);

        ScanCard.setOnClickListener(v -> {
            Intent intent = new Intent(IPM.this, CameraInterface.class);
            startActivity(intent);
        });

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

}
