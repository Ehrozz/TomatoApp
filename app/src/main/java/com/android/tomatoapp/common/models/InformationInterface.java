package com.android.tomatoapp.common.models;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tomatoapp.R;
import com.android.tomatoapp.core.ui.BaseDrawerActivity;
import com.android.tomatoapp.detection.ui.DiseaseView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InformationInterface extends BaseDrawerActivity {

    private RecyclerView diseaseRecyclerView;
    private DiseaseAdapter adapter;
    private List<DiseaseItem> diseaseList = new ArrayList<>();
    
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
        
        // Prepare disease list with descriptions
        prepareDiseaseList();
        
        adapter = new DiseaseAdapter(diseaseList);
        diseaseRecyclerView.setAdapter(adapter);

        setupDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.information_section);
        }
    }

    private void prepareDiseaseList() {
        List<String> diseaseNames = Arrays.asList(
                "Tomato Leaf Curl Virus",
                "Early Blight",
                "Late Blight",
                "Anthracnose ",
                "Black Leaf Mold"
        );

        for (String shortName : diseaseNames) {
            String fullName = diseaseNameMap.get(shortName);
            DiseaseInfo info = DiseaseData.getDiseaseInfo(fullName);
            
            String title = shortName.trim();
            String description = "Information not available.";
            
            if (info != null) {
                // Use description, or fallback to symptoms if description is too long
                description = info.getDescription();
                if (description.length() > 150) {
                    description = description.substring(0, 147) + "...";
                }
            }
            
            diseaseList.add(new DiseaseItem(title, description, shortName));
        }
    }

    // Disease Item Model
    private static class DiseaseItem {
        String title;
        String description;
        String originalName; // For passing to DiseaseView

        DiseaseItem(String title, String description, String originalName) {
            this.title = title;
            this.description = description;
            this.originalName = originalName;
        }
    }

    // RecyclerView Adapter
    private class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder> {
        private List<DiseaseItem> items;

        DiseaseAdapter(List<DiseaseItem> items) {
            this.items = items;
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
