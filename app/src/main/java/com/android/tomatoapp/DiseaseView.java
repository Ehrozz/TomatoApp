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

    TextView diseaseTitle, diseaseDescription, diseaseSymptoms, diseaseCause, diseaseCure, diseasePrevention, pestDescription, pestCommonName, pestScientificName, scientificName;
    ImageView diseaseImage, pestImage;
    com.google.android.material.card.MaterialCardView pestImageCard;
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
        diseaseNameMap.put("Yellow Leaf Curl", "Tomato Leaf Curl Virus (TLCV)"); // Yellow Leaf Curl is a variant
    }
    
    // Map disease names to image resources
    private static final HashMap<String, Integer> diseaseImageMap = new HashMap<>();
    static {
        diseaseImageMap.put("Anthracnose", R.drawable.disease_anthracnose);
        diseaseImageMap.put("Anthracnose ", R.drawable.disease_anthracnose);
        diseaseImageMap.put("Black Leaf Mold", R.drawable.disease_black_leaf_mold);
        diseaseImageMap.put("Early Blight", R.drawable.disease_early_blight);
        diseaseImageMap.put("Fusarium Wilt", R.drawable.disease_fusarium_wilt);
        diseaseImageMap.put("Late Blight", R.drawable.disease_late_blight);
        diseaseImageMap.put("Yellow Leaf Curl", R.drawable.disease_yellow_leaf_curl);
        diseaseImageMap.put("Tomato Leaf Curl Virus", R.drawable.disease_yellow_leaf_curl);
    }
    
    // Map disease names to pest image resources
    private static final HashMap<String, Integer> pestImageMap = new HashMap<>();
    static {
        // Tomato Leaf Curl Virus - Whitefly
        pestImageMap.put("Tomato Leaf Curl Virus", R.drawable.pest_whitefly);
        pestImageMap.put("Yellow Leaf Curl", R.drawable.pest_whitefly);
        
        // Early Blight - Spider Mite (common pest)
        pestImageMap.put("Early Blight", R.drawable.pest_spider_mite);
        
        // Late Blight - Leaf Beetle (common pest)
        pestImageMap.put("Late Blight", R.drawable.pest_leaf_beetle);
        
        // Fusarium Wilt - Root Weevil (root-related pest)
        pestImageMap.put("Fusarium Wilt", R.drawable.pest_root_weevil);
        
        // Anthracnose - Generic pest image
        pestImageMap.put("Anthracnose", R.drawable.pest_1);
        pestImageMap.put("Anthracnose ", R.drawable.pest_1);
        
        // Black Leaf Mold - Generic pest image
        pestImageMap.put("Black Leaf Mold", R.drawable.pest_2);
    }
    
    // Map disease names to common pest names
    private static final HashMap<String, String> pestCommonNameMap = new HashMap<>();
    static {
        // Tomato Leaf Curl Virus - Whiteflies
        pestCommonNameMap.put("Tomato Leaf Curl Virus", "Whiteflies");
        pestCommonNameMap.put("Yellow Leaf Curl", "Whiteflies");
        
        // Early Blight - Spider mites
        pestCommonNameMap.put("Early Blight", "Spider mites");
        
        // Late Blight - Leaf beetles
        pestCommonNameMap.put("Late Blight", "Leaf beetles");
        
        // Fusarium Wilt - Root weevils
        pestCommonNameMap.put("Fusarium Wilt", "Root weevils");
        
        // Anthracnose - Fruitworm
        pestCommonNameMap.put("Anthracnose", "Fruitworm");
        pestCommonNameMap.put("Anthracnose ", "Fruitworm");
        
        // Black Leaf Mold - Aphids
        pestCommonNameMap.put("Black Leaf Mold", "Aphids");
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
        pestCommonName = findViewById(R.id.pestCommonName);
        pestScientificName = findViewById(R.id.pestScientificName);
        pestImage = findViewById(R.id.pestImage);
        pestImageCard = findViewById(R.id.pestImageCard);
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
                // Get common name from mapping (user-provided common names)
                String pestCommon = pestCommonNameMap.get(shortName.trim());
                if (pestCommon == null) {
                    pestCommon = pestCommonNameMap.get(fullName);
                }
                
                // Parse pest information to extract scientific name and description
                String[] pestInfo = parsePestInfo(pestDesc);
                String parsedCommon = pestInfo[0]; // Parsed common name from description
                String pestScientific = pestInfo[1]; // Scientific name
                String pestDescText = pestInfo[2]; // Description text
                
                // Check if we have a mapped common name (indicating it's a pest, not a pathogen)
                // The mapping contains user-provided common names like "Whiteflies", "Spider mites", etc.
                boolean hasMappedCommonName = pestCommonNameMap.containsKey(shortName.trim()) || pestCommonNameMap.containsKey(fullName);
                
                if (hasMappedCommonName && pestCommon != null && !pestCommon.isEmpty()) {
                    // Set common name and scientific name
                    if (pestCommonName != null && pestScientificName != null) {
                        // Show user-provided common name from mapping
                        pestCommonName.setText(pestCommon);
                        pestCommonName.setVisibility(View.VISIBLE);
                        
                        // Show scientific name in parentheses if available and different from common name
                        if (pestScientific != null && !pestScientific.isEmpty() && !pestScientific.equalsIgnoreCase(pestCommon)) {
                            pestScientificName.setText("(" + pestScientific + ")");
                            pestScientificName.setVisibility(View.VISIBLE);
                        } else {
                            pestScientificName.setVisibility(View.GONE);
                        }
                    }
                    
                    // Set description (without the name part)
                    if (pestDescText != null && !pestDescText.isEmpty()) {
                        pestDescription.setText(pestDescText);
                    } else {
                        // Fallback to full description if parsing fails
                        pestDescription.setText(pestDesc);
                    }
                    pestDescription.setVisibility(View.VISIBLE);
                } else {
                    // This is a pathogen, not a pest - hide pest section
                    pestDescription.setVisibility(View.GONE);
                    if (pestCommonName != null) {
                        pestCommonName.setVisibility(View.GONE);
                    }
                    if (pestScientificName != null) {
                        pestScientificName.setVisibility(View.GONE);
                    }
                    if (pestImageCard != null) {
                        pestImageCard.setVisibility(View.GONE);
                    }
                }
                
                // Show pest image only if we have a mapped common name (indicating it's a pest)
                if (hasMappedCommonName && pestImageCard != null && pestImage != null) {
                    pestImageCard.setVisibility(View.VISIBLE);
                    // Get pest image based on disease name
                    Integer pestImageResId = pestImageMap.get(shortName.trim());
                    if (pestImageResId == null) {
                        // Try with full name if short name not found
                        pestImageResId = pestImageMap.get(fullName);
                    }
                    if (pestImageResId != null) {
                        try {
                            pestImage.setImageResource(pestImageResId);
                        } catch (Exception e) {
                            // Fallback to logo if image resource not found
                            pestImage.setImageResource(R.mipmap.ic_logo);
                        }
                    } else {
                        // Default to logo if no pest image mapping found
                        pestImage.setImageResource(R.mipmap.ic_logo);
                    }
                } else if (pestImageCard != null) {
                    pestImageCard.setVisibility(View.GONE);
                }
            } else {
                pestDescription.setVisibility(View.GONE);
                if (pestCommonName != null) {
                    pestCommonName.setVisibility(View.GONE);
                }
                if (pestScientificName != null) {
                    pestScientificName.setVisibility(View.GONE);
                }
                if (pestImageCard != null) {
                    pestImageCard.setVisibility(View.GONE);
                }
            }
            
            // Set image based on disease name
            Integer imageResId = diseaseImageMap.get(shortName.trim());
            if (imageResId == null) {
                // Try with full name if short name not found
                imageResId = diseaseImageMap.get(fullName);
            }
            if (imageResId != null) {
                try {
                    diseaseImage.setImageResource(imageResId);
                } catch (Exception e) {
                    // Fallback to logo if image resource not found
                    diseaseImage.setImageResource(R.mipmap.ic_logo);
                }
            } else {
                // Default to logo if no image mapping found
                diseaseImage.setImageResource(R.mipmap.ic_logo);
            }
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
    
    /**
     * Parses pest information string to extract common name, scientific name, and description.
     * Format: "Common Name (Scientific Name) → Description"
     * @param pestInfo The full pest information string
     * @return String array [commonName, scientificName, description]
     */
    private String[] parsePestInfo(String pestInfo) {
        String commonName = "";
        String scientificName = "";
        String description = "";
        
        if (pestInfo == null || pestInfo.isEmpty()) {
            return new String[]{"", "", ""};
        }
        
        // Look for pattern: "Common Name (Scientific Name) → Description"
        int arrowIndex = pestInfo.indexOf("→");
        if (arrowIndex != -1) {
            // Split at arrow
            String namePart = pestInfo.substring(0, arrowIndex).trim();
            description = pestInfo.substring(arrowIndex + 1).trim();
            
            // Extract common name and scientific name from name part
            int parenStart = namePart.indexOf('(');
            int parenEnd = namePart.indexOf(')');
            
            if (parenStart != -1 && parenEnd != -1 && parenEnd > parenStart) {
                commonName = namePart.substring(0, parenStart).trim();
                scientificName = namePart.substring(parenStart + 1, parenEnd).trim();
                // If common name is empty, use scientific name as common name
                if (commonName.isEmpty()) {
                    commonName = scientificName;
                    scientificName = ""; // Don't show scientific name twice
                }
            } else {
                // No parentheses, check if it looks like a scientific name (has spaces or is capitalized)
                // If it's just a scientific name, use it as common name
                commonName = namePart;
            }
        } else {
            // No arrow found, try to extract from parentheses
            int parenStart = pestInfo.indexOf('(');
            int parenEnd = pestInfo.indexOf(')');
            
            if (parenStart != -1 && parenEnd != -1 && parenEnd > parenStart) {
                commonName = pestInfo.substring(0, parenStart).trim();
                scientificName = pestInfo.substring(parenStart + 1, parenEnd).trim();
                // Rest is description
                if (parenEnd + 1 < pestInfo.length()) {
                    description = pestInfo.substring(parenEnd + 1).trim();
                }
            } else {
                // No structure found, use entire string as description
                description = pestInfo;
            }
        }
        
        return new String[]{commonName, scientificName, description};
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
