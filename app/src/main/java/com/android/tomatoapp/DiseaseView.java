package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class DiseaseView extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    TextView diseaseTitle, diseaseDescription, diseaseSymptoms, diseaseCause, diseaseCure, diseasePrevention, pestDescription;
    ImageView diseaseImage, pestImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_disease_view);

        diseaseTitle = findViewById(R.id.diseaseTitle);
        diseaseImage = findViewById(R.id.diseaseImage);
        diseaseDescription = findViewById(R.id.diseaseDescription);
        diseaseSymptoms = findViewById(R.id.diseaseSymptoms);
        diseaseCause = findViewById(R.id.diseaseCause);
        diseaseCure = findViewById(R.id.diseaseCure);
        pestImage = findViewById(R.id.pestImage);
        pestDescription = findViewById(R.id.pestDescription);

        Intent intent = getIntent();
        if (intent != null) {
            diseaseTitle.setText(intent.getStringExtra("diseaseTitle"));
            diseaseDescription.setText(intent.getStringExtra("diseaseDescription"));
            diseaseSymptoms.setText(intent.getStringExtra("diseaseSymptoms"));
            diseaseCause.setText(intent.getStringExtra("diseaseCause"));
            diseaseCure.setText(intent.getStringExtra("diseaseCure"));
            pestDescription.setText(intent.getStringExtra("pestDescription"));

            int diseaseImgRes = intent.getIntExtra("diseaseImage", 0);
            if (diseaseImgRes != 0) {
                diseaseImage.setImageResource(diseaseImgRes);
            }
            int pestImgRes = intent.getIntExtra("pestImage", 0);
            if (pestImgRes != 0) {
                pestImage.setImageResource(pestImgRes);
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tomato App"); // same as other activities

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_back) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
