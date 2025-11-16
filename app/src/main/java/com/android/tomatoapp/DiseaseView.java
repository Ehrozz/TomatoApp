package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class DiseaseView extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    TextView diseaseTitle, diseaseDescription, diseaseSymptoms, diseaseCause, diseaseCure, diseasePrevention, pestDescription, scientificName;
    ImageView diseaseImage;
    ScrollView scrollView;
    
    // Map short names to full names in DiseaseData
    private static final HashMap<String, String> diseaseNameMap = new HashMap<>();
    static {
        diseaseNameMap.put("Tomato Leaf Curl Virus", "Tomato Leaf Curl Virus (TLCV)");
        diseaseNameMap.put("Early Blight", "Early Blight (Alternaria solani)");
        diseaseNameMap.put("Late Blight", "Late Blight (Phytophthora infestans)");
        diseaseNameMap.put("Bacterial Wilt", "Bacterial Wilt (Ralstonia solanacearum)");
        diseaseNameMap.put("Fusarium Wilt", "Fusarium Wilt (Fusarium oxysporum)");
        diseaseNameMap.put("Anthracnose ", "Anthracnose (Colletotrichum spp.)");
        diseaseNameMap.put("Black Leaf Mold", "Black Leaf Mold (Pseudocercospora fuligena)");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_disease_view);

        // Hide action bar for custom header
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        diseaseTitle = findViewById(R.id.diseaseTitle);
        diseaseImage = findViewById(R.id.diseaseImage);
        diseaseDescription = findViewById(R.id.diseaseDescription);
        diseaseSymptoms = findViewById(R.id.diseaseSymptoms);
        diseaseCause = findViewById(R.id.diseaseCause);
        diseaseCure = findViewById(R.id.diseaseCure);
        diseasePrevention = findViewById(R.id.diseasePrevention);
        pestDescription = findViewById(R.id.pestDescription);
        scientificName = findViewById(R.id.scientificName);
        scrollView = findViewById(R.id.scrollView);

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Action buttons - scroll to sections
        LinearLayout symptomsButton = findViewById(R.id.symptomsButton);
        LinearLayout treatmentButton = findViewById(R.id.treatmentButton);
        LinearLayout preventionButton = findViewById(R.id.preventionButton);

        symptomsButton.setOnClickListener(v -> scrollToView(diseaseSymptoms));
        treatmentButton.setOnClickListener(v -> scrollToView(diseaseCure));
        preventionButton.setOnClickListener(v -> scrollToView(diseasePrevention));

        // Load disease data
        Intent intent = getIntent();
        if (intent != null) {
            String diseaseName = intent.getStringExtra("disease_name");
            if (diseaseName != null) {
                loadDiseaseData(diseaseName);
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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

    private void loadDiseaseData(String shortName) {
        // Get full name from map
        String fullName = diseaseNameMap.get(shortName);
        if (fullName == null) {
            fullName = shortName; // Use as-is if not in map
        }

        // Get disease info from DiseaseData
        DiseaseInfo info = DiseaseData.getDiseaseInfo(fullName);
        
        if (info != null) {
            // Set title (use short name for display)
            diseaseTitle.setText(shortName.trim());
            
            // Extract scientific name from full name (text in parentheses)
            String sciName = extractScientificName(fullName);
            if (sciName != null && !sciName.isEmpty()) {
                scientificName.setText(sciName);
                scientificName.setVisibility(View.VISIBLE);
            } else {
                scientificName.setVisibility(View.GONE);
            }
            
            // Set description
            diseaseDescription.setText(info.getDescription());
            
            // Set symptoms
            diseaseSymptoms.setText(info.getSymptoms());
            
            // Set cause
            diseaseCause.setText(info.getCause());
            
            // Set treatment/cure
            diseaseCure.setText(info.getCure());
            
            // Set prevention
            diseasePrevention.setText(info.getPrevention());
            
            // Set pest description
            String pestDesc = info.getPestDescription();
            if (pestDesc != null && !pestDesc.isEmpty() && !pestDesc.equals("No pests present.")) {
                pestDescription.setText(pestDesc);
                pestDescription.setVisibility(View.VISIBLE);
            } else {
                pestDescription.setVisibility(View.GONE);
            }
            
            // Set image (default to logo for now)
            diseaseImage.setImageResource(R.mipmap.ic_logo);
        } else {
            // Fallback if disease not found
            diseaseTitle.setText(shortName);
            diseaseDescription.setText("Information not available for this disease.");
            diseaseSymptoms.setText("Symptoms information not available.");
            diseaseCause.setText("Cause information not available.");
            diseaseCure.setText("Treatment information not available.");
            diseasePrevention.setText("Prevention information not available.");
            pestDescription.setVisibility(View.GONE);
        }
    }

    private String extractScientificName(String fullName) {
        // Extract text in parentheses (scientific name)
        int start = fullName.indexOf('(');
        int end = fullName.indexOf(')');
        if (start != -1 && end != -1 && end > start) {
            return fullName.substring(start + 1, end);
        }
        return null;
    }

    private void scrollToView(View view) {
        scrollView.post(() -> {
            int scrollTo = view.getTop();
            scrollView.smoothScrollTo(0, scrollTo - 100); // Offset for better visibility
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
