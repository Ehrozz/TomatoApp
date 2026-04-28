package com.android.tomatoapp.common.models;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.tomatoapp.R;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.detection.ui.DiseaseView;
import com.android.tomatoapp.common.models.DiseaseData;
import com.android.tomatoapp.common.models.DiseaseInfo;
import com.google.android.material.card.MaterialCardView;

public class InformationInterface extends BaseBottomNavActivity {


    private RecyclerView diseaseRecyclerView;
    private DiseaseAdapter adapter;
    private List<DiseaseItem> diseaseList = new ArrayList<>();

    // Search and filter
    private EditText searchField;
    private MaterialCardView catAll, catFungal, catBacterial, catPest, catViral;
    private String activeCategory = null; // null = all
    private String searchQuery = "";

    
    // Map short names to full names in DiseaseData
    private static final HashMap<String, String> diseaseNameMap = new HashMap<>();
    static {
        diseaseNameMap.put("Tomato Leaf Curl Virus", "Tomato Leaf Curl Virus (TLCV)");
        diseaseNameMap.put("Early Blight", "Early Blight (Alternaria solani)");
        diseaseNameMap.put("Late Blight", "Late Blight (Phytophthora infestans)");
        diseaseNameMap.put("Anthracnose ", "Anthracnose (Colletotrichum spp.)");
        diseaseNameMap.put("Black Leaf Mold", "Black Leaf Mold (Pseudocercospora fuligena)");
    }
    
    // Map disease names to image resources
    private static final HashMap<String, Integer> diseaseImageMap = new HashMap<>();
    static {
        diseaseImageMap.put("Anthracnose", R.drawable.disease_anthracnose);
        diseaseImageMap.put("Anthracnose ", R.drawable.disease_anthracnose);
        diseaseImageMap.put("Black Leaf Mold", R.drawable.disease_black_leaf_mold);
        diseaseImageMap.put("Early Blight", R.drawable.disease_early_blight);
        diseaseImageMap.put("Late Blight", R.drawable.disease_late_blight);
        diseaseImageMap.put("Yellow Leaf Curl", R.drawable.disease_yellow_leaf_curl);
        diseaseImageMap.put("Tomato Leaf Curl Virus", R.drawable.disease_yellow_leaf_curl);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_interface);

        diseaseRecyclerView = findViewById(R.id.diseaseRecyclerView);
        diseaseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        prepareDiseaseList();

        adapter = new DiseaseAdapter(diseaseList);
        diseaseRecyclerView.setAdapter(adapter);

        // Search field
        searchField = findViewById(R.id.searchField);
        if (searchField != null) {
            searchField.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    searchQuery = s.toString().toLowerCase().trim();
                    filterList();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Category chips
        catAll       = findViewById(R.id.catAll);
        catFungal    = findViewById(R.id.catFungal);
        catBacterial = findViewById(R.id.catBacterial);
        catPest      = findViewById(R.id.catPest);
        catViral     = findViewById(R.id.catViral);
        setupCategoryChips();

        setupBottomNavigation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.information_section);
        }
    }

    private void setupCategoryChips() {
        View.OnClickListener cl = v -> {
            setCatActive(catAll,       false);
            setCatActive(catFungal,    false);
            setCatActive(catBacterial, false);
            setCatActive(catPest,      false);
            setCatActive(catViral,     false);
            if      (v == catAll)       { activeCategory = null;       setCatActive(catAll,       true); }
            else if (v == catFungal)    { activeCategory = "fungal";    setCatActive(catFungal,    true); }
            else if (v == catBacterial) { activeCategory = "bacterial"; setCatActive(catBacterial, true); }
            else if (v == catPest)      { activeCategory = "pest";      setCatActive(catPest,      true); }
            else if (v == catViral)     { activeCategory = "viral";     setCatActive(catViral,     true); }
            filterList();
        };
        if (catAll       != null) catAll.setOnClickListener(cl);
        if (catFungal    != null) catFungal.setOnClickListener(cl);
        if (catBacterial != null) catBacterial.setOnClickListener(cl);
        if (catPest      != null) catPest.setOnClickListener(cl);
        if (catViral     != null) catViral.setOnClickListener(cl);
        setCatActive(catAll, true);
    }

    private void setCatActive(MaterialCardView chip, boolean active) {
        if (chip == null) return;
        if (active) {
            chip.setCardBackgroundColor(getResources().getColor(R.color.warm_orange, getTheme()));
            chip.setStrokeWidth(0);
        } else {
            chip.setCardBackgroundColor(getResources().getColor(R.color.white, getTheme()));
            chip.setStrokeWidth(1);
        }
    }

    private void filterList() {
        List<DiseaseItem> filtered = new ArrayList<>();
        for (DiseaseItem item : diseaseList) {
            boolean matchSearch = searchQuery.isEmpty()
                    || item.title.toLowerCase().contains(searchQuery)
                    || item.description.toLowerCase().contains(searchQuery);
            boolean matchCat = activeCategory == null
                    || item.category.equals(activeCategory);
            if (matchSearch && matchCat) filtered.add(item);
        }
        adapter.updateItems(filtered);
    }

    private void prepareDiseaseList() {
        // { displayName, dataKey, category }
        String[][] entries = {
                {"Early Blight",           "Early Blight",          "fungal"},
                {"Late Blight",            "Late Blight",           "fungal"},
                {"Anthracnose",            "Anthracnose ",          "fungal"},
                {"Black Leaf Mold",        "Black Leaf Mold",       "fungal"},
                {"Tomato Leaf Curl Virus", "Tomato Leaf Curl Virus","viral"},
        };
        for (String[] row : entries) {
            String shortName = row[0];
            String dataKey   = row[1];
            String category  = row[2];
            String fullName  = diseaseNameMap.containsKey(dataKey) ? diseaseNameMap.get(dataKey) : dataKey;
            DiseaseInfo info = DiseaseData.getDiseaseInfo(fullName);
            String description = "Information not available.";
            if (info != null) {
                description = info.getDescription();
                if (description.length() > 150) description = description.substring(0, 147) + "...";
            }
            diseaseList.add(new DiseaseItem(shortName, description, dataKey, category));
        }
    }

    // Disease Item Model
    private static class DiseaseItem {
        String title;
        String description;
        String originalName;
        String category;

        DiseaseItem(String title, String description, String originalName, String category) {
            this.title        = title;
            this.description  = description;
            this.originalName = originalName;
            this.category     = category;
        }
    }

    // RecyclerView Adapter
    private class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder> {
        private List<DiseaseItem> items;

        DiseaseAdapter(List<DiseaseItem> items) {
            this.items = new ArrayList<>(items);
        }

        void updateItems(List<DiseaseItem> newItems) {
            this.items = new ArrayList<>(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DiseaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_disease_info, parent, false);
            return new DiseaseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
            DiseaseItem item = items.get(position);
            holder.title.setText(item.title);
            holder.description.setText(item.description);
            
            // Set image based on disease type
            Integer imageResId = diseaseImageMap.get(item.originalName.trim());
            if (imageResId != null) {
                try {
                    holder.image.setImageResource(imageResId);
                } catch (Exception e) {
                    // Fallback to logo if image resource not found
                    holder.image.setImageResource(R.mipmap.ic_logo);
                }
            } else {
                // Default to logo if no image mapping found
                holder.image.setImageResource(R.mipmap.ic_logo);
            }
            
            // Make image circular
            holder.image.setClipToOutline(true);
            holder.image.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(android.view.View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            
            // Click listener
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(InformationInterface.this, DiseaseView.class);
                intent.putExtra("disease_name", item.originalName);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class DiseaseViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title, description;

            DiseaseViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.diseaseImage);
                title = itemView.findViewById(R.id.diseaseTitle);
                description = itemView.findViewById(R.id.diseaseDescription);
            }
        }
    }
}

