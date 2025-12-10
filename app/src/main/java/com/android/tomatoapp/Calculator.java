package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class Calculator extends AppCompatActivity {

    EditText etHectare, etAWF, etAFP, etMarketValue;
    TextView tvCultivarName, tvDateSaved, tvNP, tvSubTotalHarvest;
    Spinner spinnerHarvestUnit;
    TextView tvHarvestPredictionYieldPerHa, tvHarvestPredictionTotalYield, tvHarvestPredictionDate;
    TextView tvNetIncomeCard, tvSummarySubtitle, tvCompletionRate, tvAdjustedNetIncome, tvAdjustedSubtitle, tvAdjustedExpenses, tvCompletionWarning;
    TextView tvTotalExpenses;
    com.google.android.material.button.MaterialButton btnDailyExpensesHistory;
    // Expense category cards
    TextView tvLaborTotalCost, tvLaborTotalWorkers;
    TextView tvMaterialTotalCost;
    TextView tvEquipmentTotalCost, tvEquipmentTotalUsage;
    TextView tvMiscellaneousTotalCost;
    // Expenses card aggregated totals
    TextView tvExpensesLaborTotal, tvExpensesEquipmentTotal, tvExpensesMaterialTotal, tvExpensesMiscellaneousTotal;
    LinearLayout laborItemsContainer, materialItemsContainer, equipmentItemsContainer, miscItemsContainer;

    double hectare = 0, AWF = 0, AFP = 0, baseNP = 0, currentNP = 0, marketValue = 0;
    double manpower = 0;
    double grossIncome = 0, totalExpenses = 0, netIncome = 0;
    double totalHarvestGrams = 0, totalHarvestKg = 0; // Store harvest values for unit conversion
    int maturityDays = 0; // Maturity days for harvest prediction
    String growthHabit = "";
    DecimalFormat df = new DecimalFormat("#,###");
    DecimalFormat df2 = new DecimalFormat("#,###.##");
    
    // Firebase
    private DatabaseReference calculationsRef;
    private DatabaseReference dailyExpensesRef;
    private FirebaseUser currentUser;
    private double lastSavedGrossIncome = 0;
    private double lastSavedTotalExpenses = 0;
    private String programId; // Store program ID for saving hectare and analytics
    private String cultivarName; // For analytics/work program record
    private String dateSaved;    // For analytics/work program record (starting date)
    private WorkProgramDataHelper.CompletionStats completionStats;
    
    // Daily expenses aggregation
    private double aggregatedLaborCost = 0;
    private double aggregatedMaterialCost = 0;
    private double aggregatedEquipmentCost = 0;
    private double aggregatedMiscCost = 0;
    private double aggregatedEquipmentUsageHours = 0; // Track total equipment usage in hours
    private int aggregatedLaborWorkerCount = 0; // Track total number of workers
    private List<Double> dailyExpenseTotals = new ArrayList<>(); // For min/max/avg calculation
    
    // Individual expense items for display
    private List<LaborItem> laborItems = new ArrayList<>();
    private List<MaterialItem> materialItems = new ArrayList<>();
    private List<EquipmentItem> equipmentItems = new ArrayList<>();
    private List<MiscItem> miscItems = new ArrayList<>();
    
    // Helper classes for expense items
    private static class LaborItem {
        String activityName;
        double dailyWage;
        int numWorkers;
        double totalCost;
    }
    
    private static class MaterialItem {
        String materialName;
        double quantity;
        String quantityUnit;
        double totalCost;
    }
    
    private static class EquipmentItem {
        String equipmentName;
        double usageValue;
        String usageUnit;
        double cost;
        double totalCost;
    }
    
    private static class MiscItem {
        String expenseName;
        double cost;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Projected Income/Expenses");
        }

        // Bind UI elements
        etHectare = findViewById(R.id.etHectare);
        etAWF = findViewById(R.id.etAWF);
        etAFP = findViewById(R.id.etAFP);
        etMarketValue = findViewById(R.id.etMarketValue);
        
        tvCultivarName = findViewById(R.id.tvCultivarName);
        tvDateSaved = findViewById(R.id.tvDateSaved);
        tvNP = findViewById(R.id.tvNP);
        tvSubTotalHarvest = findViewById(R.id.tvSubTotalHarvest);
        spinnerHarvestUnit = findViewById(R.id.spinnerHarvestUnit);
        tvHarvestPredictionYieldPerHa = findViewById(R.id.tvHarvestPredictionYieldPerHa);
        tvHarvestPredictionTotalYield = findViewById(R.id.tvHarvestPredictionTotalYield);
        tvHarvestPredictionDate = findViewById(R.id.tvHarvestPredictionDate);
        tvNetIncomeCard = findViewById(R.id.tvNetIncomeCard);
        tvSummarySubtitle = findViewById(R.id.tvSummarySubtitle);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvAdjustedNetIncome = findViewById(R.id.tvAdjustedNetIncome);
        tvAdjustedSubtitle = findViewById(R.id.tvAdjustedSubtitle);
        tvCompletionWarning = findViewById(R.id.tvCompletionWarning);
        btnDailyExpensesHistory = findViewById(R.id.btnDailyExpensesHistory);
        tvAdjustedExpenses = findViewById(R.id.tvAdjustedExpenses);
        
        // Expense category cards
        tvLaborTotalCost = findViewById(R.id.tvLaborTotalCost);
        tvLaborTotalWorkers = findViewById(R.id.tvLaborTotalWorkers);
        tvMaterialTotalCost = findViewById(R.id.tvMaterialTotalCost);
        tvEquipmentTotalCost = findViewById(R.id.tvEquipmentTotalCost);
        tvEquipmentTotalUsage = findViewById(R.id.tvEquipmentTotalUsage);
        tvMiscellaneousTotalCost = findViewById(R.id.tvMiscellaneousTotalCost);
        
        // Item containers for displaying individual expense items
        laborItemsContainer = findViewById(R.id.laborItemsContainer);
        materialItemsContainer = findViewById(R.id.materialItemsContainer);
        equipmentItemsContainer = findViewById(R.id.equipmentItemsContainer);
        miscItemsContainer = findViewById(R.id.miscItemsContainer);
        
        // Expenses card aggregated totals
        tvExpensesLaborTotal = findViewById(R.id.tvExpensesLaborTotal);
        tvExpensesEquipmentTotal = findViewById(R.id.tvExpensesEquipmentTotal);
        tvExpensesMaterialTotal = findViewById(R.id.tvExpensesMaterialTotal);
        tvExpensesMiscellaneousTotal = findViewById(R.id.tvExpensesMiscellaneousTotal);

        // Get values from intent
        cultivarName = getIntent().getStringExtra("cultivar_name");
        dateSaved = getIntent().getStringExtra("date_saved");
        baseNP = getIntent().getDoubleExtra("NP_VALUE", 0); // base NP per hectare
        growthHabit = getIntent().getStringExtra("growth_habit"); // Growth habit
        double prefilledHectare = getIntent().getDoubleExtra("hectare_prefilled", 0);
        programId = getIntent().getStringExtra("program_id");

        // Display cultivar info
        tvCultivarName.setText("Cultivar: " + (cultivarName != null ? cultivarName : "N/A"));
        tvDateSaved.setText("Date Saved: " + (dateSaved != null ? dateSaved : "N/A"));
        tvNP.setText("Number of Plants Per Hectare (NP): " + df.format(baseNP));
        
        // Get maturity days for harvest prediction
        if (cultivarName != null) {
            maturityDays = WorkProgramDataHelper.getMaturityDays(cultivarName);
            if (maturityDays <= 0) {
                maturityDays = 90; // Fallback default
            }
        }
        
        // Initialize Firebase first (needed for fetching hectare)
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            calculationsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("calculations");
            
            // Initialize daily expenses reference if programId is available
            if (programId != null) {
                dailyExpensesRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(currentUser.getUid())
                        .child("workPrograms")
                        .child(programId)
                        .child("dailyExpenses");
                // Load daily expenses after UI is initialized
                loadDailyExpenses();
            }
        }
        
        // Prefill hectare if provided
        if (prefilledHectare > 0) {
            etHectare.setText(df2.format(prefilledHectare));
            // Trigger computation with prefilled hectare
            hectare = prefilledHectare;
        } else if (programId != null && currentUser != null) {
            // Also try to fetch hectare from Firebase if programId is available and hectare not prefilled
            fetchHectareFromFirebase();
        }
        
        // TextWatcher to trigger computation
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                compute();
            }
        };

        etHectare.addTextChangedListener(watcher);
        etAWF.addTextChangedListener(watcher);
        etAFP.addTextChangedListener(watcher);
        etMarketValue.addTextChangedListener(watcher);
        
        // Initial calculations
        computeExpenses();
        
        // Auto-save when calculation is complete
        setupAutoSave();

        // Load completion stats for adjusted projections
        loadCompletionStats();
        
        // Wire up Daily Expenses History button
        setupDailyExpensesHistoryButton();
        
        // Initialize expense category cards (show empty state if no data yet)
        updateExpenseCategoryCards();
        updateExpensesCardTotals();
        
        // Set default net income to zero
        if (tvNetIncomeCard != null) {
            tvNetIncomeCard.setText("₱0.00");
        }
        
        // Setup harvest unit spinner
        setupHarvestUnitSpinner();
    }
    
    /**
     * Sets up the harvest unit spinner with kg and grams options
     */
    private void setupHarvestUnitSpinner() {
        if (spinnerHarvestUnit == null) return;
        
        String[] units = {"kg", "grams"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHarvestUnit.setAdapter(adapter);
        
        // Set default to kg
        spinnerHarvestUnit.setSelection(0);
        
        // Update display when unit changes
        spinnerHarvestUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateHarvestDisplay();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    /**
     * Updates the harvest display based on selected unit
     */
    private void updateHarvestDisplay() {
        if (tvSubTotalHarvest == null || spinnerHarvestUnit == null) return;
        
        String selectedUnit = (String) spinnerHarvestUnit.getSelectedItem();
        if (selectedUnit == null) selectedUnit = "kg";
        
        if (totalHarvestKg > 0 || totalHarvestGrams > 0) {
            if (selectedUnit.equals("kg")) {
                tvSubTotalHarvest.setText("₱" + df2.format(totalHarvestKg));
            } else {
                tvSubTotalHarvest.setText("₱" + df2.format(totalHarvestGrams));
            }
        } else {
            tvSubTotalHarvest.setText("₱—");
        }
    }
    
    /**
     * Sets up the Daily Expenses History button to navigate to DailyExpensesHistoryActivity
     */
    private void setupDailyExpensesHistoryButton() {
        if (btnDailyExpensesHistory == null) return;
        
        // Hide button by default if programId is not available (new calculations)
        if (programId == null || cultivarName == null || dateSaved == null) {
            btnDailyExpensesHistory.setVisibility(View.GONE);
            return;
        }
        
        btnDailyExpensesHistory.setVisibility(View.VISIBLE);
        
        // Set up click listener with validation
        btnDailyExpensesHistory.setOnClickListener(v -> {
            // Double-check that all required data is available before navigating
            if (programId == null || programId.isEmpty()) {
                Toast.makeText(Calculator.this, "Daily expenses history is only available for saved work programs", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (cultivarName == null || dateSaved == null) {
                Toast.makeText(Calculator.this, "Missing required information", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                Intent intent = new Intent(Calculator.this, DailyExpensesHistoryActivity.class);
                intent.putExtra("programId", programId);
                intent.putExtra("cultivar", cultivarName);
                intent.putExtra("startDate", dateSaved);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(Calculator.this, "Unable to open daily expenses history", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Loads and aggregates daily expenses from Firebase
     */
    private void loadDailyExpenses() {
        if (dailyExpensesRef == null || currentUser == null) return;
        
        dailyExpensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Reset aggregated values
                aggregatedLaborCost = 0;
                aggregatedMaterialCost = 0;
                aggregatedEquipmentCost = 0;
                aggregatedMiscCost = 0;
                aggregatedEquipmentUsageHours = 0;
                aggregatedLaborWorkerCount = 0;
                dailyExpenseTotals.clear();
                
                // Clear individual items
                laborItems.clear();
                materialItems.clear();
                equipmentItems.clear();
                miscItems.clear();
                
                // Iterate through all dates with expenses
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    double dailyTotal = 0;
                    
                    // Process labor expenses
                    if (dateSnapshot.hasChild("labor")) {
                        for (DataSnapshot laborSnapshot : dateSnapshot.child("labor").getChildren()) {
                            Double totalCost = laborSnapshot.child("totalCost").getValue(Double.class);
                            Integer numWorkers = laborSnapshot.child("numWorkers").getValue(Integer.class);
                            Double dailyWage = laborSnapshot.child("dailyWage").getValue(Double.class);
                            String notes = laborSnapshot.child("notes").getValue(String.class);
                            
                            if (totalCost != null) {
                                aggregatedLaborCost += totalCost;
                                dailyTotal += totalCost;
                                
                                // Store individual item
                                LaborItem item = new LaborItem();
                                item.activityName = (notes != null && !notes.isEmpty()) ? notes : "Labor";
                                item.dailyWage = dailyWage != null ? dailyWage : 0.0;
                                item.numWorkers = numWorkers != null ? numWorkers : 0;
                                item.totalCost = totalCost;
                                laborItems.add(item);
                            }
                            
                            // Track worker count
                            if (numWorkers != null && numWorkers > 0) {
                                aggregatedLaborWorkerCount += numWorkers;
                            }
                        }
                    }
                    
                    // Process material expenses
                    if (dateSnapshot.hasChild("material")) {
                        for (DataSnapshot materialSnapshot : dateSnapshot.child("material").getChildren()) {
                            Double totalCost = materialSnapshot.child("totalCost").getValue(Double.class);
                            String materialName = materialSnapshot.child("materialName").getValue(String.class);
                            Double quantity = materialSnapshot.child("quantity").getValue(Double.class);
                            String quantityUnit = materialSnapshot.child("quantityUnit").getValue(String.class);
                            
                            if (totalCost != null) {
                                aggregatedMaterialCost += totalCost;
                                dailyTotal += totalCost;
                                
                                // Store individual item
                                MaterialItem item = new MaterialItem();
                                item.materialName = materialName != null ? materialName : "Material";
                                item.quantity = quantity != null ? quantity : 0.0;
                                item.quantityUnit = quantityUnit != null ? quantityUnit : "";
                                item.totalCost = totalCost;
                                materialItems.add(item);
                            }
                        }
                    }
                    
                    // Process equipment expenses (only non-owned equipment)
                    if (dateSnapshot.hasChild("equipment")) {
                        for (DataSnapshot equipmentSnapshot : dateSnapshot.child("equipment").getChildren()) {
                            Boolean isOwned = equipmentSnapshot.child("isOwned").getValue(Boolean.class);
                            Double totalCost = equipmentSnapshot.child("totalCost").getValue(Double.class);
                            String equipmentName = equipmentSnapshot.child("equipmentName").getValue(String.class);
                            Double usageValue = equipmentSnapshot.child("usageValue").getValue(Double.class);
                            String usageUnit = equipmentSnapshot.child("usageUnit").getValue(String.class);
                            Double cost = equipmentSnapshot.child("cost").getValue(Double.class);
                            
                            // Track total usage hours for all equipment (owned and non-owned)
                            if (usageValue != null && usageUnit != null) {
                                double hours = usageUnit.equals("hours") ? usageValue : (usageValue / 60.0);
                                aggregatedEquipmentUsageHours += hours;
                            }
                            
                            // Only count non-owned equipment expenses
                            if (isOwned == null || !isOwned) {
                                if (totalCost != null) {
                                    aggregatedEquipmentCost += totalCost;
                                    dailyTotal += totalCost;
                                    
                                    // Store individual item
                                    EquipmentItem item = new EquipmentItem();
                                    item.equipmentName = equipmentName != null ? equipmentName : "Equipment";
                                    item.usageValue = usageValue != null ? usageValue : 0.0;
                                    item.usageUnit = usageUnit != null ? usageUnit : "hours";
                                    item.cost = cost != null ? cost : 0.0;
                                    item.totalCost = totalCost;
                                    equipmentItems.add(item);
                                }
                            }
                        }
                    }
                    
                    // Process miscellaneous expenses
                    if (dateSnapshot.hasChild("miscellaneous")) {
                        for (DataSnapshot miscSnapshot : dateSnapshot.child("miscellaneous").getChildren()) {
                            Double cost = miscSnapshot.child("cost").getValue(Double.class);
                            String expenseName = miscSnapshot.child("expenseName").getValue(String.class);
                            
                            if (cost != null) {
                                aggregatedMiscCost += cost;
                                dailyTotal += cost;
                                
                                // Store individual item
                                MiscItem item = new MiscItem();
                                item.expenseName = expenseName != null ? expenseName : "Miscellaneous";
                                item.cost = cost;
                                miscItems.add(item);
                            }
                        }
                    }
                    
                    // Store daily total for min/max/avg calculation
                    if (dailyTotal > 0) {
                        dailyExpenseTotals.add(dailyTotal);
                    }
                }
                
                // Display individual items
                displayExpenseItems();
                
                // Auto-fill expense fields with aggregated values
                populateExpenseFields();
                
                // Update cost range display if we have expense data
                if (!dailyExpenseTotals.isEmpty()) {
                    updateCostRangeDisplay();
                }
                
                // Update expense category cards
                updateExpenseCategoryCards();
                
                // Update Expenses card totals
                updateExpensesCardTotals();
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silently fail - user can still enter expenses manually
            }
        });
    }
    
    /**
     * Displays individual expense items in their respective containers
     */
    private void displayExpenseItems() {
        // Clear existing items
        if (laborItemsContainer != null) laborItemsContainer.removeAllViews();
        if (materialItemsContainer != null) materialItemsContainer.removeAllViews();
        if (equipmentItemsContainer != null) equipmentItemsContainer.removeAllViews();
        if (miscItemsContainer != null) miscItemsContainer.removeAllViews();
        
        // Display labor items
        for (LaborItem item : laborItems) {
            View itemView = createLaborItemView(item);
            if (laborItemsContainer != null && itemView != null) {
                laborItemsContainer.addView(itemView);
            }
        }
        
        // Display material items
        for (MaterialItem item : materialItems) {
            View itemView = createMaterialItemView(item);
            if (materialItemsContainer != null && itemView != null) {
                materialItemsContainer.addView(itemView);
            }
        }
        
        // Display equipment items
        for (EquipmentItem item : equipmentItems) {
            View itemView = createEquipmentItemView(item);
            if (equipmentItemsContainer != null && itemView != null) {
                equipmentItemsContainer.addView(itemView);
            }
        }
        
        // Display miscellaneous items
        for (MiscItem item : miscItems) {
            View itemView = createMiscItemView(item);
            if (miscItemsContainer != null && itemView != null) {
                miscItemsContainer.addView(itemView);
            }
        }
    }
    
    /**
     * Creates a view for a labor item
     */
    private View createLaborItemView(LaborItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 12, 0, 12);
        
        TextView activityName = new TextView(this);
        activityName.setText(item.activityName);
        activityName.setTextSize(16);
        activityName.setTypeface(null, android.graphics.Typeface.BOLD);
        activityName.setTextColor(getResources().getColor(android.R.color.black));
        layout.addView(activityName);
        
        TextView details = new TextView(this);
        String detailsText = String.format(Locale.getDefault(), 
            "Daily Wage: ₱%,.2f\nNumber of Workers: %d\nTotal Cost: ₱%,.2f",
            item.dailyWage, item.numWorkers, item.totalCost);
        details.setText(detailsText);
        details.setTextSize(14);
        details.setTextColor(getResources().getColor(android.R.color.darker_gray));
        details.setPadding(0, 4, 0, 0);
        layout.addView(details);
        
        return layout;
    }
    
    /**
     * Creates a view for a material item
     */
    private View createMaterialItemView(MaterialItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 12, 0, 12);
        
        TextView materialName = new TextView(this);
        materialName.setText(item.materialName);
        materialName.setTextSize(16);
        materialName.setTypeface(null, android.graphics.Typeface.BOLD);
        materialName.setTextColor(getResources().getColor(android.R.color.black));
        layout.addView(materialName);
        
        TextView details = new TextView(this);
        String quantityText = "";
        if (item.quantity > 0 && item.quantityUnit != null && !item.quantityUnit.isEmpty()) {
            quantityText = String.format(Locale.getDefault(), "Quantity: %s %s\n", 
                df2.format(item.quantity), item.quantityUnit);
        }
        String detailsText = quantityText + String.format(Locale.getDefault(), "Total Cost: ₱%,.2f", item.totalCost);
        details.setText(detailsText);
        details.setTextSize(14);
        details.setTextColor(getResources().getColor(android.R.color.darker_gray));
        details.setPadding(0, 4, 0, 0);
        layout.addView(details);
        
        return layout;
    }
    
    /**
     * Creates a view for an equipment item
     */
    private View createEquipmentItemView(EquipmentItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 12, 0, 12);
        
        TextView equipmentName = new TextView(this);
        equipmentName.setText(item.equipmentName);
        equipmentName.setTextSize(16);
        equipmentName.setTypeface(null, android.graphics.Typeface.BOLD);
        equipmentName.setTextColor(getResources().getColor(android.R.color.black));
        layout.addView(equipmentName);
        
        TextView details = new TextView(this);
        String usageText = "";
        if (item.usageValue > 0) {
            usageText = String.format(Locale.getDefault(), "Usage: %s %s\n", 
                df2.format(item.usageValue), item.usageUnit);
        }
        String costText = "";
        if (item.cost > 0) {
            costText = String.format(Locale.getDefault(), "Rental Cost: ₱%,.2f/%s\n", 
                item.cost, item.usageUnit);
        }
        String detailsText = usageText + costText + String.format(Locale.getDefault(), "Total Cost: ₱%,.2f", item.totalCost);
        details.setText(detailsText);
        details.setTextSize(14);
        details.setTextColor(getResources().getColor(android.R.color.darker_gray));
        details.setPadding(0, 4, 0, 0);
        layout.addView(details);
        
        return layout;
    }
    
    /**
     * Creates a view for a miscellaneous item
     */
    private View createMiscItemView(MiscItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 12, 0, 12);
        
        TextView expenseName = new TextView(this);
        expenseName.setText(item.expenseName);
        expenseName.setTextSize(16);
        expenseName.setTypeface(null, android.graphics.Typeface.BOLD);
        expenseName.setTextColor(getResources().getColor(android.R.color.black));
        layout.addView(expenseName);
        
        TextView details = new TextView(this);
        String detailsText = String.format(Locale.getDefault(), "Cost: ₱%,.2f", item.cost);
        details.setText(detailsText);
        details.setTextSize(14);
        details.setTextColor(getResources().getColor(android.R.color.darker_gray));
        details.setPadding(0, 4, 0, 0);
        layout.addView(details);
        
        return layout;
    }
    
    /**
     * Auto-fills expense fields with aggregated daily expense values
     */
    private void populateExpenseFields() {
        // Set manpower from aggregated labor cost
        manpower = aggregatedLaborCost;
        
        // Trigger expense computation to update totals
        computeExpenses();
    }
    
    /**
     * Updates expense category cards with aggregated expense data from daily expenses
     */
    private void updateExpenseCategoryCards() {
        // Update Labor card
        if (tvLaborTotalCost != null) {
            tvLaborTotalCost.setText(String.format(Locale.getDefault(), "Labor Total Cost: ₱%,.2f", aggregatedLaborCost));
            if (tvLaborTotalWorkers != null) {
                tvLaborTotalWorkers.setText(String.format(Locale.getDefault(), "Total Number of Workers: %d", aggregatedLaborWorkerCount));
            }
        }
        
        // Update Material card
        if (tvMaterialTotalCost != null) {
                tvMaterialTotalCost.setText(String.format(Locale.getDefault(), "Material Total Cost: ₱%,.2f", aggregatedMaterialCost));
        }
        
        // Update Equipment/Tools card
        if (tvEquipmentTotalCost != null) {
                tvEquipmentTotalCost.setText(String.format(Locale.getDefault(), "Equipment/Tools Total Cost: ₱%,.2f", aggregatedEquipmentCost));
                if (tvEquipmentTotalUsage != null) {
                    tvEquipmentTotalUsage.setText(String.format(Locale.getDefault(), "Total Usage: %.2f hours", aggregatedEquipmentUsageHours));
            }
        }
        
        // Update Miscellaneous card
        if (tvMiscellaneousTotalCost != null) {
                tvMiscellaneousTotalCost.setText(String.format(Locale.getDefault(), "Miscellaneous Total Cost: ₱%,.2f", aggregatedMiscCost));
        }
    }
    
    /**
     * Updates the Expenses card to show aggregated totals from daily expenses
     */
    private void updateExpensesCardTotals() {
        // Update Labor Total in Expenses card
        if (tvExpensesLaborTotal != null) {
            tvExpensesLaborTotal.setText(String.format(Locale.getDefault(), "Labor Total Cost: ₱%,.2f", aggregatedLaborCost));
        }
        
        // Update Equipment/Tools Total in Expenses card
        if (tvExpensesEquipmentTotal != null) {
            tvExpensesEquipmentTotal.setText(String.format(Locale.getDefault(), "Equipment/Tools Total Cost: ₱%,.2f", aggregatedEquipmentCost));
        }
        
        // Update Material Total in Expenses card
        if (tvExpensesMaterialTotal != null) {
            tvExpensesMaterialTotal.setText(String.format(Locale.getDefault(), "Material Total Cost: ₱%,.2f", aggregatedMaterialCost));
        }
        
        // Update Miscellaneous Total in Expenses card
        if (tvExpensesMiscellaneousTotal != null) {
            tvExpensesMiscellaneousTotal.setText(String.format(Locale.getDefault(), "Miscellaneous Total Cost: ₱%,.2f", aggregatedMiscCost));
        }
    }
    
    /**
     * Updates Harvest Prediction card with calculated harvest predictions
     */
    private void updateHarvestPrediction(double totalHarvestKg, double hectareValue) {
        if (hectareValue > 0 && totalHarvestKg > 0) {
            // Calculate yield per hectare
            double yieldPerHa = totalHarvestKg / hectareValue;
            
            if (tvHarvestPredictionYieldPerHa != null) {
                tvHarvestPredictionYieldPerHa.setText(String.format(Locale.getDefault(), "%.2f kg/hectare", yieldPerHa));
            }
            
            if (tvHarvestPredictionTotalYield != null) {
                tvHarvestPredictionTotalYield.setText(String.format(Locale.getDefault(), "%.2f kg", totalHarvestKg));
            }
            
            // Calculate predicted harvest date based on start date and maturity days
            if (dateSaved != null && maturityDays > 0 && tvHarvestPredictionDate != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date startDate = sdf.parse(dateSaved);
                    if (startDate != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(startDate);
                        cal.add(Calendar.DAY_OF_YEAR, maturityDays);
                        Date harvestDate = cal.getTime();
                        String formattedDate = sdf.format(harvestDate);
                        tvHarvestPredictionDate.setText(formattedDate);
                    }
                } catch (Exception e) {
                    tvHarvestPredictionDate.setText("—");
                }
            } else if (tvHarvestPredictionDate != null) {
                tvHarvestPredictionDate.setText("—");
            }
        } else {
            if (tvHarvestPredictionYieldPerHa != null) {
                tvHarvestPredictionYieldPerHa.setText("— kg/hectare");
            }
            if (tvHarvestPredictionTotalYield != null) {
                tvHarvestPredictionTotalYield.setText("— kg");
            }
            if (tvHarvestPredictionDate != null) {
                tvHarvestPredictionDate.setText("—");
            }
        }
    }
    
    /**
     * Updates cost range display showing min, max, average, and total expenses
     */
    private void updateCostRangeDisplay() {
        if (dailyExpenseTotals.isEmpty()) return;
        
        // Calculate min, max, average, and total
        double minExpense = Collections.min(dailyExpenseTotals);
        double maxExpense = Collections.max(dailyExpenseTotals);
        double avgExpense = 0.0;
        double totalExpense = 0.0;
        
        for (Double expense : dailyExpenseTotals) {
            totalExpense += expense;
        }
        avgExpense = totalExpense / dailyExpenseTotals.size();
        
        // Display cost range information - append to summary subtitle
        // The cost range shows actual expense patterns from daily entries
        String rangeText = String.format(Locale.getDefault(),
                "\nDaily Expense Range: ₱%,.2f - ₱%,.2f | Avg: ₱%,.2f/day | Total Accumulated: ₱%,.2f",
                minExpense, maxExpense, avgExpense, totalExpense);
        
        // Append to summary subtitle for visibility
        if (tvSummarySubtitle != null) {
            String currentText = tvSummarySubtitle.getText().toString();
            // Only append if not already present (to avoid duplicates)
            if (!currentText.contains("Daily Expense Range")) {
                tvSummarySubtitle.setText(currentText + rangeText);
            }
        }
    }
    
    /**
     * Fetches hectare from Firebase work program if not already prefilled
     */
    private void fetchHectareFromFirebase() {
        if (programId == null || currentUser == null) return;
        
        DatabaseReference workProgramRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("workPrograms")
                .child(programId);
        
        workProgramRef.child("landArea").addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Object hectareObj = snapshot.getValue();
                            if (hectareObj != null) {
                                double fetchedHectare = 0;
                                if (hectareObj instanceof Double) {
                                    fetchedHectare = (Double) hectareObj;
                                } else if (hectareObj instanceof Long) {
                                    fetchedHectare = ((Long) hectareObj).doubleValue();
                                } else if (hectareObj instanceof String) {
                                    try {
                                        fetchedHectare = Double.parseDouble((String) hectareObj);
                                    } catch (NumberFormatException e) {
                                        return;
                                    }
                                }
                                
                                // Update hectare field if we have a valid value and field is empty or zero
                                if (fetchedHectare > 0) {
                                    String currentText = etHectare.getText().toString().trim();
                                    if (currentText.isEmpty() || currentText.equals("0") || currentText.equals("0.0")) {
                                        etHectare.setText(df2.format(fetchedHectare));
                                        hectare = fetchedHectare;
                                        compute(); // Trigger computation
                                    }
                                }
                            }
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        // Silently fail - user can still enter hectare manually
                    }
                });
    }
    
    /**
     * Sets up auto-save functionality - saves to Firebase when all required fields are filled
     */
    private void setupAutoSave() {
        // Save when gross income and total expenses are calculated
        // This will be called from compute() and computeExpenses()
    }
    
    /**
     * Saves calculation to Firebase
     * Only saves if values have changed significantly to avoid duplicate entries
     */
    private void saveCalculation() {
        if (currentUser == null || calculationsRef == null) return;
        
        // Only save if we have meaningful data and values have changed
        if ((grossIncome > 0 || totalExpenses > 0) && 
            (Math.abs(grossIncome - lastSavedGrossIncome) > 0.01 || 
             Math.abs(totalExpenses - lastSavedTotalExpenses) > 0.01)) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String dateCreated = sdf.format(new Date());
            
            String calculationId = calculationsRef.push().getKey();
            if (calculationId != null) {
                CalculationModel calculation = new CalculationModel(
                        grossIncome,
                        totalExpenses,
                        netIncome,
                        hectare,
                        dateCreated
                );
                
                calculationsRef.child(calculationId).setValue(calculation)
                        .addOnSuccessListener(aVoid -> {
                            // Update last saved values
                            lastSavedGrossIncome = grossIncome;
                            lastSavedTotalExpenses = totalExpenses;
                            
                            // Also enrich the related work program record if programId is available
                            if (programId != null && hectare > 0 && currentUser != null) {
                                DatabaseReference workProgramRef = FirebaseDatabase.getInstance()
                                        .getReference("users")
                                        .child(currentUser.getUid())
                                        .child("workPrograms")
                                        .child(programId);

                                Map<String, Object> updates = new HashMap<>();
                                // Legacy fields (already used in the app)
                                updates.put("cultivar", cultivarName);
                                updates.put("startDate", dateSaved);
                                updates.put("landArea", hectare);
                                // New analytics-friendly fields
                                updates.put("cultivarName", cultivarName);
                                updates.put("startingDate", dateSaved);
                                updates.put("areaSize", hectare);
                                updates.put("projectedIncome", grossIncome);
                                updates.put("projectedExpenses", totalExpenses);

                                workProgramRef.updateChildren(updates);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save calculation", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void compute() {
        try {
            hectare = parse(etHectare);
            AWF = parse(etAWF);
            AFP = parse(etAFP);
            marketValue = parse(etMarketValue);

            // 🔹 Update NP immediately when hectare changes
            currentNP = baseNP * hectare;
            if (hectare > 0) {
                tvNP.setText("Number of Plants (NP): " + df.format(currentNP));
            } else {
                tvNP.setText("Number of Plants (NP): " + df.format(baseNP));
            }

            // 🔹 Compute totals only when all fields are filled
            if (hectare > 0 && AWF > 0 && AFP > 0 && marketValue > 0) {
                double AWP = AWF * AFP;           // Average Weight per Plant
                totalHarvestGrams = AWP * currentNP;      // Total Harvest (grams)
                totalHarvestKg = totalHarvestGrams / 1000;          // Total Harvest (kilograms)
                grossIncome = totalHarvestKg * marketValue; // ₱

                // Update harvest display based on selected unit
                updateHarvestDisplay();
                
                // Update Harvest Prediction card
                updateHarvestPrediction(totalHarvestKg, hectare);
            } else {
                grossIncome = 0;
                totalHarvestGrams = 0;
                totalHarvestKg = 0;
                
                // Update harvest display
                updateHarvestDisplay();
                
                // Clear Harvest Prediction card
                if (tvHarvestPredictionYieldPerHa != null) {
                    tvHarvestPredictionYieldPerHa.setText("— kg/hectare");
                }
                if (tvHarvestPredictionTotalYield != null) {
                    tvHarvestPredictionTotalYield.setText("— kg");
                }
                if (tvHarvestPredictionDate != null) {
                    tvHarvestPredictionDate.setText("—");
                }
            }
            
            // 🔹 Compute expenses and net income
            computeExpenses();
            
            // Auto-save to Firebase when calculation is complete
            if (grossIncome > 0 && totalExpenses >= 0) {
                saveCalculation();
            }
            
            // Ensure net income defaults to zero if no calculation
            if (grossIncome == 0 && totalExpenses == 0 && tvNetIncomeCard != null) {
                tvNetIncomeCard.setText("₱0.00");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void computeExpenses() {
        try {
            // Use aggregated values from daily expenses
            // manpower is already set from aggregatedLaborCost in populateExpenseFields()
            double equipmentCost = aggregatedEquipmentCost;
            double materialCost = aggregatedMaterialCost;
            double miscCost = aggregatedMiscCost;
            
            // Calculate total expenses from all categories
            totalExpenses = manpower + equipmentCost + materialCost + miscCost;
            
            // Calculate net income = Gross Income - Total Expenses
            netIncome = grossIncome - totalExpenses;
            
            // Display results
            if (tvTotalExpenses != null) {
                tvTotalExpenses.setText("Total Expenses: ₱" + df2.format(totalExpenses));
            }

            // Update card net income (default to zero if no calculation)
            if (tvNetIncomeCard != null) {
                if (grossIncome > 0 || totalExpenses > 0) {
                tvNetIncomeCard.setText("₱" + df2.format(netIncome));
                } else {
                    tvNetIncomeCard.setText("₱0.00");
                }
            }

            applyAdjustedProjection();
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values on error
            if (tvTotalExpenses != null) {
                tvTotalExpenses.setText("Total Expenses: ₱0.00");
            }
            if (tvNetIncomeCard != null) {
                tvNetIncomeCard.setText("₱0.00");
            }
            if (tvAdjustedNetIncome != null) {
                tvAdjustedNetIncome.setText("₱—");
            }
            if (tvAdjustedExpenses != null) {
                tvAdjustedExpenses.setText("Adjusted Expenses: ₱0.00");
            }
            if (tvCompletionWarning != null) {
                tvCompletionWarning.setVisibility(View.GONE);
            }
        }
    }

    private double parse(EditText et) {
        String value = et.getText().toString().trim();
        try {
            return value.isEmpty() ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    private void updateCompletionWarning() {
        if (tvCompletionWarning == null) return;
        if (completionStats == null || completionStats.totalTasks <= 0) {
            tvCompletionWarning.setVisibility(View.GONE);
            return;
        }
        double rate = completionStats.completionRate;
        if (rate >= 85 && completionStats.missedTasks == 0 && completionStats.skippedTasks == 0) {
            tvCompletionWarning.setVisibility(View.GONE);
            return;
        }
        StringBuilder warning = new StringBuilder();
        warning.append(String.format(Locale.getDefault(), "Completion logged at %.0f%%.", rate));
        if (completionStats.missedTasks > 0) {
            warning.append(" Missed days are lowering projected harvest.");
        } else if (completionStats.skippedTasks > 0) {
            warning.append(" Skipped days may defer income.");
        }
        String phaseHint = getWeakPhaseHint();
        if (!phaseHint.isEmpty()) {
            warning.append("\n").append(phaseHint);
        }
        tvCompletionWarning.setText(warning.toString());
        tvCompletionWarning.setVisibility(View.VISIBLE);
    }

    private String getWeakPhaseHint() {
        if (completionStats == null) return "";
        double weakest = 101;
        int weakestPhase = -1;
        for (int i = 0; i < completionStats.phaseTotals.length; i++) {
            if (completionStats.phaseTotals[i] == 0) continue;
            double phaseRate = (double) completionStats.phaseCompleted[i] / completionStats.phaseTotals[i] * 100;
            if (phaseRate < weakest) {
                weakest = phaseRate;
                weakestPhase = i + 1;
            }
        }
        if (weakestPhase == -1) {
            return "";
        }
        return "Focus on " + phaseLabelFromIndex(weakestPhase) + " tasks to recover momentum.";
    }

    private String phaseLabelFromIndex(int phase) {
        switch (phase) {
            case 1:
                return "Phase 1 (Nursery & Land Prep)";
            case 2:
                return "Phase 2 (Transplant & Establishment)";
            case 3:
                return "Phase 3 (Vegetative Growth)";
            case 4:
                return "Phase 4 (Flowering & Fruit Set)";
            case 5:
                return "Phase 5 (Harvest)";
            default:
                return "current phase";
        }
    }

    private void loadCompletionStats() {
        if (tvCompletionRate != null) {
            tvCompletionRate.setText("Completion rate: syncing...");
        }
        if (programId == null || currentUser == null) {
            if (tvCompletionRate != null) {
                tvCompletionRate.setText("Completion rate: N/A");
            }
            return;
        }

        WorkProgramDataHelper.fetchCompletionStats(
                currentUser.getUid(),
                programId,
                cultivarName,
                dateSaved,
                stats -> runOnUiThread(() -> {
                    completionStats = stats;
                    applyAdjustedProjection();
                })
        );
    }

    private void applyAdjustedProjection() {
        if (tvCompletionRate != null) {
            if (completionStats != null && completionStats.totalTasks > 0) {
                tvCompletionRate.setText(String.format(Locale.getDefault(),
                        "Completion rate: %.0f%%", completionStats.completionRate));
            } else {
                tvCompletionRate.setText("Completion rate: N/A");
            }
        }

        if (tvAdjustedNetIncome == null || tvAdjustedExpenses == null) {
            return;
        }

        if (completionStats == null || completionStats.totalTasks <= 0 || grossIncome <= 0) {
            tvAdjustedNetIncome.setText("₱—");
            if (tvAdjustedExpenses != null) {
                tvAdjustedExpenses.setText("Adjusted Expenses (₱): —");
            }
            if (tvCompletionWarning != null) {
                tvCompletionWarning.setVisibility(View.GONE);
            }
            return;
        }

        WorkProgramDataHelper.AdjustedProjection projection =
                WorkProgramDataHelper.adjustProjectionsByCompletionRate(
                        grossIncome,
                        totalExpenses,
                        completionStats
                );
        double adjustedNetIncome = projection.adjustedIncome - projection.adjustedExpenses;
        tvAdjustedNetIncome.setText("₱" + df2.format(adjustedNetIncome));
        if (tvAdjustedSubtitle != null) {
            tvAdjustedSubtitle.setText("Adjusted net income (based on completion)");
        }
        tvAdjustedExpenses.setText("Adjusted Expenses: ₱" + df2.format(projection.adjustedExpenses));
        updateCompletionWarning();
    }
}
