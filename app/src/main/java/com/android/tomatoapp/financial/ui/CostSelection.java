package com.android.tomatoapp.financial.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.android.tomatoapp.R;
import com.android.tomatoapp.auth.ui.Login;
import com.android.tomatoapp.common.models.CultivarNPData;
import com.android.tomatoapp.common.utils.CultivarImageHelper;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.financial.data.DailyExpenseMath;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;

public class CostSelection extends BaseBottomNavActivity {

    private RecyclerView recyclerView;
    private CultivarAdapter adapter;
    private List<Cultivar> cultivarList = new ArrayList<>();
    private TextView programCountText;
    private View emptyState;
    private ImageView headerMenuButton;

    private DatabaseReference dbRef;
    private String userId;
    private String currentSortOrder = "date_desc"; // Default: newest first
    private ValueEventListener programsListener;
    private boolean offlineToastShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_selection);

        recyclerView = findViewById(R.id.costRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CultivarAdapter(cultivarList);
        recyclerView.setAdapter(adapter);

        programCountText = findViewById(R.id.programCountText);
        emptyState = findViewById(R.id.emptyState);
        headerMenuButton = findViewById(R.id.headerMenuButton);

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

        // Load data with offline fallback
        loadWorkPrograms();

        // Real-time updates (when online)
        programsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (LocalDataManager.isOnline(CostSelection.this)) {
                    readPrograms(snapshot);
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!LocalDataManager.isOnline(CostSelection.this)) {
                    loadWorkProgramsFromLocal();
                }
            }
        };
        dbRef.addValueEventListener(programsListener);

        setupBottomNavigation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Projected Income/Expenses");
        }

        // Header menu button - Sort options
        headerMenuButton.setOnClickListener(v -> showSortMenu());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbRef != null && programsListener != null) {
            dbRef.removeEventListener(programsListener);
        }
    }

    private void readPrograms(DataSnapshot snapshot) {
        cultivarList.clear();
        for (DataSnapshot child : snapshot.getChildren()) {
            String programId = child.getKey();
            String cultivar = child.child("cultivarName").getValue(String.class);
            String startDate = child.child("startingDate").getValue(String.class);

            if (programId != null && cultivar != null && startDate != null) {
                cultivarList.add(new Cultivar(programId, cultivar, startDate,
                        CultivarImageHelper.getCultivarImageResource(cultivar),
                        DailyExpenseMath.asInt(child.child("completionRate").getValue())));
            }
        }
    }

    private void loadWorkPrograms() {
        if (LocalDataManager.isOnline(this)) {
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    readPrograms(snapshot);
                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadWorkProgramsFromLocal();
                }
            });
        } else {
            loadWorkProgramsFromLocal();
        }
    }

    private void loadWorkProgramsFromLocal() {
        new Thread(() -> {
            List<WorkProgramEntity> entities = LocalDataManager.getInstance(this).getWorkProgramsFromLocal(userId);
            runOnUiThread(() -> {
                cultivarList.clear();
                for (WorkProgramEntity entity : entities) {
                    if (entity.cultivarName != null && entity.startingDate != null) {
                        cultivarList.add(new Cultivar(entity.id, entity.cultivarName, entity.startingDate,
                                CultivarImageHelper.getCultivarImageResource(entity.cultivarName),
                                (int) Math.round(entity.completionRate)));
                    }
                }
                updateUI();
                if (!LocalDataManager.isOnline(this) && !cultivarList.isEmpty() && !offlineToastShown) {
                    offlineToastShown = true;
                    Toast.makeText(this, "Showing offline data", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateUI() {
        int count = cultivarList.size();
        if (programCountText != null) {
            if (count == 1) {
                programCountText.setText("1 program");
            } else {
                programCountText.setText(String.format(java.util.Locale.getDefault(), "%d programs", count));
            }
        }

        if (emptyState != null && recyclerView != null) {
            if (count == 0) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        // Apply current sort order (silent)
        sortPrograms(currentSortOrder);
    }

    private void showSortMenu() {
        String[] sortOptions = {
                "Newest First",
                "Oldest First",
                "Name (A-Z)",
                "Name (Z-A)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Sort Work Programs")
                .setItems(sortOptions, (dialog, which) -> {
                    String[] orders = {"date_desc", "date_asc", "name_asc", "name_desc"};
                    sortPrograms(orders[which]);
                    Toast.makeText(this, "Sorted: " + sortOptions[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void sortPrograms(String sortOrder) {
        currentSortOrder = sortOrder;

        switch (sortOrder) {
            case "date_desc": // Newest first
                cultivarList.sort((a, b) -> b.date.compareTo(a.date));
                break;
            case "date_asc": // Oldest first
                cultivarList.sort(java.util.Comparator.comparing(a -> a.date));
                break;
            case "name_asc": // A-Z
                cultivarList.sort(java.util.Comparator.comparing(a -> a.name, String.CASE_INSENSITIVE_ORDER));
                break;
            case "name_desc": // Z-A
                cultivarList.sort((a, b) -> b.name.compareToIgnoreCase(a.name));
                break;
        }

        adapter.notifyDataSetChanged();
    }

    private void openLinkedProgramFinance(String programId, String cultivarName, String dateSaved,
                                          String growthHabit, int NP) {
        dbRef.child(programId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot child) {
                Object hectareObj = child.child("areaSize").getValue();
                if (hectareObj == null) hectareObj = child.child("landArea").getValue();
                double savedHectare = DailyExpenseMath.asDouble(hectareObj);
                navigateToCalculator(cultivarName, growthHabit, NP, dateSaved, savedHectare, programId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                navigateToCalculator(cultivarName, growthHabit, NP, dateSaved, 0, programId);
            }
        });
    }

    private void navigateToCalculator(String cultivarName, String growthHabit, int NP, String dateSaved,
                                      double savedHectare, String programId) {
        Intent intent = new Intent(CostSelection.this, Calculator.class);
        intent.putExtra("cultivar_name", cultivarName);
        intent.putExtra("growth_habit", growthHabit);
        intent.putExtra("NP_VALUE", (double) NP);
        intent.putExtra("date_saved", dateSaved);
        if (savedHectare > 0) {
            intent.putExtra("hectare_prefilled", savedHectare);
        }
        if (programId != null) {
            intent.putExtra("program_id", programId);
        }
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
    }

    // ---- Cultivar Model ----
    static class Cultivar {
        String programId, name, date;
        int imageRes;
        int completionRate;

        Cultivar(String programId, String name, String date, int imageRes, int completionRate) {
            this.programId = programId;
            this.name = name;
            this.date = date;
            this.imageRes = imageRes;
            this.completionRate = completionRate;
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
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            Cultivar item = items.get(adapterPosition);
            holder.name.setText(item.name);
            holder.date.setText(String.format(java.util.Locale.getDefault(), "Started: %s", item.date));
            holder.image.setImageResource(item.imageRes);

            // Make image circular
            holder.image.setClipToOutline(true);
            holder.image.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(android.view.View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            holder.image.setBackgroundResource(R.drawable.circular_image_border);

            int[] bgColors = {
                    R.color.tomato_red,
                    R.color.fresh_green,
                    R.color.warm_orange,
                    R.color.sidebar_dark_green,
                    R.color.ripe_orange
            };
            int colorIndex = adapterPosition % bgColors.length;
            int bgColor = ContextCompat.getColor(holder.itemView.getContext(), bgColors[colorIndex]);
            holder.card.setCardBackgroundColor(bgColor);

            holder.name.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.date.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));

            if (holder.statusIndicator != null) {
                holder.statusIndicator.setBackground(ContextCompat.getDrawable(
                        holder.itemView.getContext(), R.drawable.circle_green));
            }
            if (holder.statusText != null) {
                holder.statusText.setText("Active");
                holder.statusText.setTextColor(ContextCompat.getColor(
                        holder.itemView.getContext(), android.R.color.white));
            }

            // Hide delete button for cost selection (not needed here)
            if (holder.deleteButton != null) {
                holder.deleteButton.setVisibility(View.GONE);
            }

            if (holder.cardProgressBar != null) {
                holder.cardProgressBar.setProgress(item.completionRate);
            }
            if (holder.cardProgressText != null) {
                holder.cardProgressText.setText(item.completionRate + "% complete");
            }

            // Click -> open Calculator for THIS program
            holder.card.setOnClickListener(v -> {
                if (item.programId == null || item.name == null) {
                    Toast.makeText(CostSelection.this, "Error loading cultivar details", Toast.LENGTH_SHORT).show();
                    return;
                }
                String growthHabit = CultivarNPData.getGrowthHabit(item.name);
                int NP = CultivarNPData.getNP(item.name);
                openLinkedProgramFinance(item.programId, item.name, item.date, growthHabit, NP);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class CultivarViewHolder extends RecyclerView.ViewHolder {
            TextView name, date, statusText, cardProgressText;
            ImageView image, deleteButton;
            CardView card;
            View statusIndicator;
            android.widget.ProgressBar cardProgressBar;

            CultivarViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.cultivarName);
                date = itemView.findViewById(R.id.cultivarDate);
                image = itemView.findViewById(R.id.cultivarImage);
                card = itemView.findViewById(R.id.cultivarCard);
                statusText = itemView.findViewById(R.id.statusText);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
                deleteButton = itemView.findViewById(R.id.deleteButton);
                cardProgressBar = itemView.findViewById(R.id.cardProgressBar);
                cardProgressText = itemView.findViewById(R.id.cardProgressText);
            }
        }
    }
}