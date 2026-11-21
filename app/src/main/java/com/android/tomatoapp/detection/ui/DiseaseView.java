package com.android.tomatoapp.detection.ui;

import java.util.HashMap;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.android.tomatoapp.R;
import com.android.tomatoapp.common.models.DiseaseData;
import com.android.tomatoapp.common.models.DiseaseImages;
import com.android.tomatoapp.common.models.DiseaseInfo;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.tomatoapp.core.ui.BaseBottomNavActivity;

public class DiseaseView extends BaseBottomNavActivity {

    TextView diseaseTitle, diseaseDescription, diseaseSymptoms, diseaseCause, diseaseCure, diseasePrevention, pestDescription, pestCommonName, pestScientificName, scientificName;
    ImageView pestImage;
    ViewPager2 diseasePager;
    TextView galleryCaption, tvDisclaimer;
    View noTreatmentBox;
    com.google.android.material.button.MaterialButton btnGoPrevention;
    LinearLayout[] tabViews;
    View[] sections;
    com.google.android.material.card.MaterialCardView pestImageCard;
    ScrollView scrollView;

    // Map short names to full names in DiseaseData
    private static final HashMap<String, String> diseaseNameMap = new HashMap<>();
    static {
        diseaseNameMap.put("Tomato Leaf Curl Virus", "Tomato Leaf Curl Virus (TLCV)");
        diseaseNameMap.put("Early Blight", "Early Blight (Alternaria solani)");
        diseaseNameMap.put("Late Blight", "Late Blight (Phytophthora infestans)");
        diseaseNameMap.put("Anthracnose ", "Anthracnose (Colletotrichum spp.)");
        diseaseNameMap.put("Black Leaf Mold", "Black Leaf Mold (Pseudocercospora fuligena)");
        diseaseNameMap.put("Yellow Leaf Curl", "Tomato Leaf Curl Virus (TLCV)"); // Yellow Leaf Curl is a variant
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
        diseasePager = findViewById(R.id.diseasePager);
        galleryCaption = findViewById(R.id.galleryCaption);
        tvDisclaimer = findViewById(R.id.tvDisclaimer);
        noTreatmentBox = findViewById(R.id.noTreatmentBox);
        btnGoPrevention = findViewById(R.id.btnGoPrevention);
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

        // The severity badge is hardcoded in the layout and is not real data yet, so hide it
        View severityBadge = findViewById(R.id.diseaseSeverityBadge);
        if (severityBadge != null) severityBadge.setVisibility(View.GONE);

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Tabs: Symptoms / Treatment / Prevention
        tabViews = new LinearLayout[]{
                findViewById(R.id.tabSymptoms),
                findViewById(R.id.tabTreatment),
                findViewById(R.id.tabPrevention)};
        sections = new View[]{
                findViewById(R.id.sectionSymptoms),
                findViewById(R.id.sectionTreatment),
                findViewById(R.id.sectionPrevention)};
        for (int i = 0; i < tabViews.length; i++) {
            final int position = i;
            tabViews[i].setOnClickListener(v -> showTab(position));
        }
        btnGoPrevention.setOnClickListener(v -> showTab(2));
        showTab(0);

        // Load disease data
        Intent intent = getIntent();
        if (intent != null) {
            String diseaseName = intent.getStringExtra("disease_name");
            if (diseaseName != null) {
                loadDiseaseData(diseaseName);
            }
        }

        setupBottomNavigation();
    }

    private void loadDiseaseData(String shortName) {
        // Photo gallery (never fails: shows a placeholder when no photo exists yet)
        setupGallery(shortName);

        // Get full name from map
        String fullName = diseaseNameMap.get(shortName);
        if (fullName == null) {
            String found = DiseaseData.findFullName(shortName);
            fullName = found != null ? found : shortName;
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

            // Set treatment, or the "no known treatment" message
            diseaseCure.setText(info.getCure());
            boolean noTreatment = info.hasNoTreatment();
            noTreatmentBox.setVisibility(noTreatment ? View.VISIBLE : View.GONE);
            diseaseCure.setVisibility(noTreatment ? View.GONE : View.VISIBLE);
            tvDisclaimer.setText(DiseaseData.TREATMENT_DISCLAIMER);
            tvDisclaimer.setVisibility("Healthy Tomato".equals(fullName) ? View.GONE : View.VISIBLE);

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
                String pestScientific = pestInfo[1]; // Scientific name
                String pestDescText = pestInfo[2]; // Description text

                // Check if we have a mapped common name (indicating it's a pest, not a pathogen)
                boolean hasMappedCommonName = pestCommonNameMap.containsKey(shortName.trim()) || pestCommonNameMap.containsKey(fullName);

                if (hasMappedCommonName && pestCommon != null && !pestCommon.isEmpty()) {
                    if (pestCommonName != null && pestScientificName != null) {
                        pestCommonName.setText(pestCommon);
                        pestCommonName.setVisibility(View.VISIBLE);

                        if (pestScientific != null && !pestScientific.isEmpty() && !pestScientific.equalsIgnoreCase(pestCommon)) {
                            pestScientificName.setText("(" + pestScientific + ")");
                            pestScientificName.setVisibility(View.VISIBLE);
                        } else {
                            pestScientificName.setVisibility(View.GONE);
                        }
                    }

                    if (pestDescText != null && !pestDescText.isEmpty()) {
                        pestDescription.setText(pestDescText);
                    } else {
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
                    Integer pestImageResId = pestImageMap.get(shortName.trim());
                    if (pestImageResId == null) {
                        pestImageResId = pestImageMap.get(fullName);
                    }
                    if (pestImageResId != null) {
                        try {
                            pestImage.setImageResource(pestImageResId);
                        } catch (Exception e) {
                            pestImage.setImageResource(R.mipmap.ic_logo);
                        }
                    } else {
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
        } else {
            // Fallback if disease not found
            diseaseTitle.setText(shortName);
            diseaseDescription.setText("Information not available for this disease.");
            diseaseSymptoms.setText("Symptoms information not available.");
            diseaseCause.setText("Cause information not available.");
            diseaseCure.setText("Treatment information not available.");
            diseasePrevention.setText("Prevention information not available.");
            noTreatmentBox.setVisibility(View.GONE);
            tvDisclaimer.setVisibility(View.GONE);
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
            String namePart = pestInfo.substring(0, arrowIndex).trim();
            description = pestInfo.substring(arrowIndex + 1).trim();

            int parenStart = namePart.indexOf('(');
            int parenEnd = namePart.indexOf(')');

            if (parenStart != -1 && parenEnd != -1 && parenEnd > parenStart) {
                commonName = namePart.substring(0, parenStart).trim();
                scientificName = namePart.substring(parenStart + 1, parenEnd).trim();
                if (commonName.isEmpty()) {
                    commonName = scientificName;
                    scientificName = "";
                }
            } else {
                commonName = namePart;
            }
        } else {
            int parenStart = pestInfo.indexOf('(');
            int parenEnd = pestInfo.indexOf(')');

            if (parenStart != -1 && parenEnd != -1 && parenEnd > parenStart) {
                commonName = pestInfo.substring(0, parenStart).trim();
                scientificName = pestInfo.substring(parenStart + 1, parenEnd).trim();
                if (parenEnd + 1 < pestInfo.length()) {
                    description = pestInfo.substring(parenEnd + 1).trim();
                }
            } else {
                description = pestInfo;
            }
        }

        return new String[]{commonName, scientificName, description};
    }

    // ===== Tabs =====
    private void showTab(int position) {
        for (int i = 0; i < tabViews.length; i++) {
            boolean selected = i == position;
            sections[i].setVisibility(selected ? View.VISIBLE : View.GONE);
            tabViews[i].setBackgroundTintList(ColorStateList.valueOf(
                    selected ? ContextCompat.getColor(this, R.color.green_light) : Color.TRANSPARENT));
            TextView label = (TextView) tabViews[i].getChildAt(1);
            label.setTextColor(ContextCompat.getColor(this, selected ? R.color.green : R.color.muted));
        }
        if (scrollView != null) scrollView.post(() -> scrollView.smoothScrollTo(0, 0));
    }

    // ===== Photo gallery =====
    private void setupGallery(String shortName) {
        List<DiseaseImages.Slide> slides = DiseaseImages.slidesFor(this, shortName);
        diseasePager.setAdapter(new GalleryAdapter(slides));
        diseasePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCaption(slides, position);
            }
        });
        updateCaption(slides, 0);
    }

    private void updateCaption(List<DiseaseImages.Slide> slides, int pos) {
        DiseaseImages.Slide s = slides.get(pos);
        if (s.placeholder) {
            galleryCaption.setText(s.label);
            return;
        }
        String count = slides.size() > 1 ? "  ·  " + (pos + 1) + "/" + slides.size() + "  ·  swipe" : "";
        galleryCaption.setText(s.label + count + "  ·  tap to enlarge");
    }

    private void showFullImage(int res) {
        Dialog d = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(this);
        iv.setImageResource(res);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setBackgroundColor(Color.BLACK);
        iv.setOnClickListener(v -> d.dismiss());
        d.setContentView(iv);
        d.show();
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.Holder> {
        private final List<DiseaseImages.Slide> slides;

        GalleryAdapter(List<DiseaseImages.Slide> slides) {
            this.slides = slides;
        }

        class Holder extends RecyclerView.ViewHolder {
            final ImageView image;
            Holder(ImageView v) {
                super(v);
                image = v;
            }
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new Holder(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            DiseaseImages.Slide s = slides.get(position);
            h.image.setImageResource(s.res);
            h.image.setContentDescription(s.label);
            h.image.setScaleType(s.placeholder ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
            if (s.placeholder) {
                h.image.setOnClickListener(null);
                h.image.setClickable(false);
            } else {
                h.image.setOnClickListener(v -> showFullImage(s.res));
            }
        }

        @Override
        public int getItemCount() {
            return slides.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}