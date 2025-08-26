package com.android.tomatoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class DetectionResults extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private TextView detectionTitle, detectionDescription, detectionSymptoms, detectionCause,
            detectionCure, detectionPrevention, pestTitle, pestDescription, detectionAccuracy;
    private ImageView detectionImage, pestImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detection_results);

        detectionTitle = findViewById(R.id.detectionTitle);
        detectionImage = findViewById(R.id.detectionImage);
        detectionDescription = findViewById(R.id.detectionDescription);
        detectionSymptoms = findViewById(R.id.detectionSymptoms);
        detectionCause = findViewById(R.id.detectionCause);
        detectionCure = findViewById(R.id.detectionCure);
        detectionPrevention = findViewById(R.id.detectionPrevention);
        pestTitle = findViewById(R.id.pestTitle);
        pestImage = findViewById(R.id.pestImage);
        pestDescription = findViewById(R.id.pestDescription);
        detectionAccuracy = findViewById(R.id.detectionAccuracy);

        Intent intent = getIntent();
        if (intent != null) {
            detectionTitle.setText(intent.getStringExtra("title"));
            detectionDescription.setText(intent.getStringExtra("description"));
            detectionSymptoms.setText(intent.getStringExtra("symptoms"));
            detectionCause.setText(intent.getStringExtra("cause"));
            detectionCure.setText(intent.getStringExtra("cure"));
            detectionPrevention.setText(intent.getStringExtra("prevention"));
            pestTitle.setText(intent.getStringExtra("pestTitle"));
            pestDescription.setText(intent.getStringExtra("pestDescription"));

            String acc = intent.getStringExtra("accuracy");
            detectionAccuracy.setText("Detection Accuracy: " + (acc != null ? acc : ""));

            String imageUriStr = intent.getStringExtra("imageUri");
            if (imageUriStr != null) {
                detectionImage.setImageURI(Uri.parse(imageUriStr));
            }

            String pestUriStr = intent.getStringExtra("pestImageUri");
            if (pestUriStr != null) {
                pestImage.setImageURI(Uri.parse(pestUriStr));
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tomato App");

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, Login.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_back) {
            finish(); // go back to previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
