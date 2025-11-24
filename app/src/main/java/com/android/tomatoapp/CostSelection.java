package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

public class CostSelection extends BaseDrawerActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    private CultivarAdapter adapter;
    private List<Cultivar> cultivarList = new ArrayList<>();
    private TextView programCountText;
    private View emptyState;
    private ImageView headerMenuButton;

    private DatabaseReference dbRef;
    private String userId;
    private String currentSortOrder = "date_desc"; // Default: newest first


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_selection);

        recyclerView = findViewById(R.id.costRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CultivarAdapter(cultivarList);
        recyclerView.setAdapter(adapter);

        btnAdd = findViewById(R.id.addButton);
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

        // Set up Firebase listener for real-time updates (when online)
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Only update if online (to avoid conflicts with offline data)
                if (LocalDataManager.isOnline(CostSelection.this)) {
                    cultivarList.clear();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        String programId = child.getKey();
                        String cultivar = child.child("cultivarName").getValue(String.class);
                        String startDate = child.child("startingDate").getValue(String.class);

                        if (programId != null && cultivar != null && startDate != null) {
                            cultivarList.add(new Cultivar(programId, cultivar, startDate, CultivarImageHelper.getCultivarImageResource(cultivar)));
                        }
                    }
                    
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // If Firebase fails, try loading from local database
                if (!LocalDataManager.isOnline(CostSelection.this)) {
                    loadWorkProgramsFromLocal();
                }
            }
        });

        btnAdd.setOnClickListener(v -> showAddCalculationDialog());

        setupDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Projected Income/Expenses");
        }

        // Header menu button - Sort options
        headerMenuButton.setOnClickListener(v -> showSortMenu());
    }

    private void loadWorkPrograms() {
        if (LocalDataManager.isOnline(this)) {
            // Try Firebase first if online
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    cultivarList.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String programId = child.getKey();
                        String cultivar = child.child("cultivarName").getValue(String.class);
                        String startDate = child.child("startingDate").getValue(String.class);

                        if (programId != null && cultivar != null && startDate != null) {
                            cultivarList.add(new Cultivar(programId, cultivar, startDate, CultivarImageHelper.getCultivarImageResource(cultivar)));
                        }
                    }
                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Fallback to local database
                    loadWorkProgramsFromLocal();
                }
            });
        } else {
            // Offline - load from local database
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
                        cultivarList.add(new Cultivar(entity.id, entity.cultivarName, entity.startingDate, CultivarImageHelper.getCultivarImageResource(entity.cultivarName)));
                    }
                }
                updateUI();
                if (!LocalDataManager.isOnline(this) && cultivarList.size() > 0) {
                    Toast.makeText(this, "Showing offline data", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateUI() {
        // Update program count
        int count = cultivarList.size();
        if (programCountText != null) {
            if (count == 1) {
                programCountText.setText("1 program");
            } else {
                programCountText.setText(String.format("%d programs", count));
            }
        }
        
        // Show/hide empty state
        if (emptyState != null && recyclerView != null) {
            if (count == 0) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
        
        // Apply current sort order
        sortPrograms(currentSortOrder);
        adapter.notifyDataSetChanged();
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
                    switch (which) {
                        case 0: // Newest First
                            sortPrograms("date_desc");
                            break;
                        case 1: // Oldest First
                            sortPrograms("date_asc");
                            break;
                        case 2: // Name A-Z
                            sortPrograms("name_asc");
                            break;
                        case 3: // Name Z-A
                            sortPrograms("name_desc");
                            break;
                    }
                })
                .show();
    }

    private void showAddCalculationDialog() {
        // Create dialog view
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_calculation, null);
        
        TextInputLayout hectareLayout = dialogView.findViewById(R.id.hectareInputLayout);
        AutoCompleteTextView cultivarSpinner = dialogView.findViewById(R.id.cultivarSpinner);
        TextInputEditText hectareEditText = dialogView.findViewById(R.id.hectareEditText);
        
        // Cultivar data (same as Workprogram.java)
        final String[][] cultivarsData = {
                {"Victory F1", "Semi-determinate", "90", "110"},
                {"HOPE F1", "Semi-determinate", "90", "110"},
                {"Maganda F1", "Semi-determinate", "80", "100"},
                {"Malakas F1", "Semi-determinate", "95", "115"},
                {"Rocky 1 F1", "Semi-determinate", "90", "110"},
                {"Improved KS Apollo", "Semi-determinate", "85", "105"},
                {"Improved Pope", "Semi-determinate", "85", "105"},
                {"Super Pope", "Semi-determinate", "85", "105"},
                {"Maguilas", "Determinate", "85", "105"},
                {"Maunlad", "Determinate", "80", "100"},
                {"Mapalad", "Determinate", "80", "100"},
                {"Abiona F1", "Semi-determinate", "95", "115"},
                {"Akna F1", "Semi-determinate", "105", "125"},
                {"Amari F1", "Semi-determinate", "110", "130"},
                {"Anita F1", "Semi-determinate", "110", "130"},
                {"Colette F1", "Determinate", "105", "125"},
                {"Danica F1", "Semi-determinate", "105", "125"},
                {"Granger F1", "Semi-determinate", "105", "125"},
                {"Janet F1", "Semi-determinate", "120", "140"},
                {"Platinum F1", "Semi-determinate", "100", "120"},
                {"Reina F1", "Semi-determinate", "105", "125"},
                {"Renata F1", "Semi-determinate", "105", "125"},
                {"Rubellite F1", "Semi-determinate", "90", "110"},
                {"TOM-055 F1", "Semi-determinate", "60", "75"},
                {"TOM-262 OP", "Determinate", "60", "75"},
                {"Dalwangan Tm1", "Determinate", "90", "110"},
                {"Dalwangan Tm2", "Determinate", "90", "110"},
                {"NSIC 1999 Tm09", "Determinate", "100", "120"},
                {"Mara", "Determinate", "78", "95"},
                {"AniMax 1", "Determinate", "87", "105"},
                {"AniMax 2", "Semi-determinate", "87", "105"},
                {"Golden Globe", "Semi-determinate", "92", "112"},
                {"Maxxime", "Indeterminate", "105", "125"}
        };
        
        // Create array of cultivar names for spinner
        String[] cultivarNames = new String[cultivarsData.length];
        for (int i = 0; i < cultivarsData.length; i++) {
            cultivarNames[i] = cultivarsData[i][0]; // cultivar name is column 0
        }
        
        // Set up spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, cultivarNames);
        cultivarSpinner.setAdapter(adapter);
        
        // Set input type for hectare
        hectareEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New Calculation")
                .setView(dialogView)
                .setPositiveButton("Calculate", null)
                .setNegativeButton("Cancel", null)
                .create();
        
        // Override positive button to handle validation and prevent auto-close
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String cultivarName = cultivarSpinner.getText() != null ? 
                        cultivarSpinner.getText().toString().trim() : "";
                String hectareStr = hectareEditText.getText() != null ? 
                        hectareEditText.getText().toString().trim() : "";
                
                if (cultivarName.isEmpty()) {
                    Toast.makeText(this, "Please select a cultivar", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (hectareStr.isEmpty()) {
                    Toast.makeText(this, "Please enter the number of hectares", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double hectare;
                try {
                    hectare = Double.parseDouble(hectareStr);
                    if (hectare <= 0) {
                        Toast.makeText(this, "Hectare must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number for hectares", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // All validation passed - close dialog and proceed
                dialog.dismiss();
                
                // Get cultivar data
                String growthHabit = CultivarNPData.getGrowthHabit(cultivarName);
                int NP = CultivarNPData.getNP(cultivarName);
                
                // Generate current date
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                String currentDate = sdf.format(new java.util.Date());
                
                // Navigate to Calculator
                Intent intent = new Intent(CostSelection.this, Calculator.class);
                intent.putExtra("cultivar_name", cultivarName);
                intent.putExtra("growth_habit", growthHabit);
                intent.putExtra("NP_VALUE", (double) NP);
                intent.putExtra("date_saved", currentDate);
                intent.putExtra("hectare_prefilled", hectare);
                startActivity(intent);
            });
        });
        
        dialog.show();
    }

    private void fetchSavedHectare(String cultivarName, String growthHabit, int NP, String dateSaved) {
        // Fetch saved hectare from the work program
        // First, find the work program ID for this cultivar
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double savedHectare = 0;
                String programId = null;
                
                // Find the work program matching this cultivar and date
                for (DataSnapshot child : snapshot.getChildren()) {
                    String programCultivar = child.child("cultivarName").getValue(String.class);
                    String programDate = child.child("startingDate").getValue(String.class);
                    
                    if (cultivarName.equals(programCultivar) && dateSaved.equals(programDate)) {
                        programId = child.getKey();
                        // Check if hectare is stored in the work program as "areaSize"
                        // Try different data types (Double, Long, String)
                        Object hectareObj = child.child("areaSize").getValue();
                        if (hectareObj != null) {
                            if (hectareObj instanceof Double) {
                                savedHectare = (Double) hectareObj;
                            } else if (hectareObj instanceof Long) {
                                savedHectare = ((Long) hectareObj).doubleValue();
                            } else if (hectareObj instanceof String) {
                                try {
                                    savedHectare = Double.parseDouble((String) hectareObj);
                                } catch (NumberFormatException e) {
                                    // Ignore
                                }
                            }
                        }
                        break;
                    }
                }
                
                // If hectare not found in work program, try to fetch from calculations
                if (savedHectare <= 0 && programId != null) {
                    fetchHectareFromCalculations(programId, cultivarName, growthHabit, NP, dateSaved);
                    return;
                }
                
                // Navigate to Calculator with all data including saved hectare
                // Even if savedHectare is 0, we still pass programId so Calculator can fetch it
                navigateToCalculator(cultivarName, growthHabit, NP, dateSaved, savedHectare, programId);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // If fetch fails, proceed without saved hectare
                navigateToCalculator(cultivarName, growthHabit, NP, dateSaved, 0, null);
            }
        });
    }
    
    private void fetchHectareFromCalculations(String programId, String cultivarName, String growthHabit, int NP, String dateSaved) {
        // Try to fetch hectare from the most recent calculation
        DatabaseReference calculationsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("calculations");
        
        calculationsRef.orderByChild("dateCreated")
                .limitToLast(10) // Check last 10 calculations
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double savedHectare = 0;
                        
                        // Find the most recent calculation with hectare > 0
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                CalculationModel calculation = child.getValue(CalculationModel.class);
                                if (calculation != null && calculation.hectare > 0) {
                                    savedHectare = calculation.hectare;
                                    break; // Use the most recent one
                                }
                            }
                        }
                        
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
        // Always pass hectare if we have it (even if 0, Calculator will fetch from Firebase)
        if (savedHectare > 0) {
            intent.putExtra("hectare_prefilled", savedHectare);
        }
        // Always pass programId so Calculator can fetch hectare from Firebase if needed
        if (programId != null) {
            intent.putExtra("program_id", programId);
        }
        startActivity(intent);
    }

    private void sortPrograms(String sortOrder) {
        currentSortOrder = sortOrder;
        
        switch (sortOrder) {
            case "date_desc": // Newest first
                cultivarList.sort((a, b) -> b.date.compareTo(a.date));
                break;
            case "date_asc": // Oldest first
                cultivarList.sort((a, b) -> a.date.compareTo(b.date));
                break;
            case "name_asc": // A-Z
                cultivarList.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
                break;
            case "name_desc": // Z-A
                cultivarList.sort((a, b) -> b.name.compareToIgnoreCase(a.name));
                break;
        }
        
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Sorted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
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
            
            // Format date as "Started: YYYY-MM-DD"
            holder.date.setText(String.format("Started: %s", item.date));
            holder.image.setImageResource(item.imageRes);
            
            // Ensure circular clipping is applied after layout
            holder.image.post(() -> {
                holder.image.setClipToOutline(true);
                holder.image.setOutlineProvider(new android.view.ViewOutlineProvider() {
                    @Override
                    public void getOutline(android.view.View view, android.graphics.Outline outline) {
                        int size = Math.min(view.getWidth(), view.getHeight());
                        outline.setOval(0, 0, size, size);
                    }
                });
            });

            // 🍅 Rotate through tomato-themed colors with better contrast
            int[] bgColors = {
                    R.color.tomato_red,
                    R.color.fresh_green,
                    R.color.warm_orange,
                    R.color.sidebar_dark_green,
                    R.color.ripe_orange
            };

            int colorIndex = position % bgColors.length;
            int bgColor = ContextCompat.getColor(holder.itemView.getContext(), bgColors[colorIndex]);
            holder.card.setCardBackgroundColor(bgColor);

            // All backgrounds are vibrant, so use white text for better contrast
            holder.name.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.date.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            
            // Update status indicator and text
            if (holder.statusIndicator != null) {
                // Green for active programs
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

            // 🎯 Click → open Calculator with cultivar info
            holder.card.setOnClickListener(v -> {
                String cultivarName = item.name;
                String dateSaved = item.date;

                if (cultivarName != null) {
                    // 🌱 Fetch growth habit and NP
                    String growthHabit = CultivarNPData.getGrowthHabit(cultivarName);
                    int NP = CultivarNPData.getNP(cultivarName);

                    // 🔍 Fetch saved hectare from Firebase calculations
                    fetchSavedHectare(cultivarName, growthHabit, NP, dateSaved);
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
            TextView name, date, statusText;
            ImageView image, deleteButton;
            CardView card;
            View statusIndicator;

            CultivarViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.cultivarName);
                date = itemView.findViewById(R.id.cultivarDate);
                image = itemView.findViewById(R.id.cultivarImage);
                card = itemView.findViewById(R.id.cultivarCard);
                statusText = itemView.findViewById(R.id.statusText);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
                deleteButton = itemView.findViewById(R.id.deleteButton);
                
                // Circular clipping will be applied in onBindViewHolder after layout
            }
        }
    }
}
