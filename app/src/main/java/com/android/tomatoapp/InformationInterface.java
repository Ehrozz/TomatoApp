package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InformationInterface extends AppCompatActivity {

    private RecyclerView diseaseRecyclerView;
    private DiseaseAdapter adapter;
    private List<DiseaseItem> diseaseList = new ArrayList<>();
    
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

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

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

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.information_section);
        }

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

    private void prepareDiseaseList() {
        List<String> diseaseNames = Arrays.asList(
                "Tomato Leaf Curl Virus",
                "Early Blight",
                "Late Blight",
                "Bacterial Wilt",
                "Fusarium Wilt",
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            
            // Set image based on disease type (you can customize this)
            holder.image.setImageResource(R.mipmap.ic_logo);
            
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
