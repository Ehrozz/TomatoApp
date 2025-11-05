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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CostSelection extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    private CultivarAdapter adapter;
    private List<Cultivar> cultivarList = new ArrayList<>();

    private DatabaseReference dbRef;
    private String userId;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_selection);

        recyclerView = findViewById(R.id.costRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CultivarAdapter(cultivarList);
        recyclerView.setAdapter(adapter);

        btnAdd = findViewById(R.id.addButton);

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }
        
        userId = currentUser.getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("workPrograms");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cultivarList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String programId = child.getKey();
                    String cultivar = child.child("cultivar").getValue(String.class);
                    String startDate = child.child("startDate").getValue(String.class);

                    if (programId != null && cultivar != null && startDate != null) {
                        cultivarList.add(new Cultivar(programId, cultivar, startDate, R.drawable.ic_launcher_foreground));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnAdd.setOnClickListener(v -> {
            // You can add functionality for adding new cost entries here
            Toast.makeText(CostSelection.this, "Add new cost entry", Toast.LENGTH_SHORT).show();
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Projected Income/Expenses");
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

    // ---- Cultivar Model ----
    static class Cultivar {
        String programId, name, date;
        int imageRes;

        Cultivar(String programId, String name, String date, int imageRes) {
            this.programId = programId;
            this.name = name;
            this.date = date;
            this.imageRes = imageRes;
        }
    }

    // ---- RecyclerView Adapter ----
    class CultivarAdapter extends RecyclerView.Adapter<CultivarAdapter.CultivarViewHolder> {
        private final List<Cultivar> items;

        CultivarAdapter(List<Cultivar> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public CultivarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cultivar, parent, false);
            return new CultivarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CultivarViewHolder holder, int position) {
            Cultivar item = items.get(position);
            holder.name.setText(item.name);
            holder.date.setText(item.date);
            holder.image.setImageResource(item.imageRes);

            // 🍅 Rotate through tomato-themed colors
            int[] bgColors = {
                    R.color.tomato_red,
                    R.color.ripe_orange,
                    R.color.fresh_green,
                    R.color.golden_yellow,
                    R.color.soft_cream
            };

            int colorIndex = position % bgColors.length;
            int bgColor = ContextCompat.getColor(holder.itemView.getContext(), bgColors[colorIndex]);

            holder.card.setCardBackgroundColor(bgColor);

            // Adjust text color for readability
            if (colorIndex == 0 || colorIndex == 1 || colorIndex == 2) {
                // Dark backgrounds → white text
                holder.name.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                holder.date.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            } else {
                // Light backgrounds → dark text
                holder.name.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
                holder.date.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
            }

            // 🎯 Click → open Calculator with cultivar info
            holder.card.setOnClickListener(v -> {
                String cultivarName = item.name;
                String dateSaved = item.date;

                if (cultivarName != null) {
                    // 🌱 Fetch growth habit and NP
                    String growthHabit = CultivarNPData.getGrowthHabit(cultivarName);
                    int NP = CultivarNPData.getNP(cultivarName);

                    // 🔄 Pass data to Calculator.java
                    Intent intent = new Intent(CostSelection.this, Calculator.class);
                    intent.putExtra("cultivar_name", cultivarName);
                    intent.putExtra("growth_habit", growthHabit);
                    intent.putExtra("NP_VALUE", (double) NP);
                    intent.putExtra("date_saved", dateSaved);
                    startActivity(intent);
                } else {
                    Toast.makeText(CostSelection.this, "Error loading cultivar details", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class CultivarViewHolder extends RecyclerView.ViewHolder {
            TextView name, date;
            ImageView image;
            CardView card;

            CultivarViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.cultivarName);
                date = itemView.findViewById(R.id.cultivarDate);
                image = itemView.findViewById(R.id.cultivarImage);
                card = itemView.findViewById(R.id.cultivarCard);
            }
        }
    }
}
