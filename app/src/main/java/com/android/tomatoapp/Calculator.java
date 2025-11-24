package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Calculator extends BaseDrawerActivity {

    EditText etHectare, etAWF, etAFP, etMarketValue;
    TextView tvCultivarName, tvDateSaved, tvNP, tvTHGrams;
    android.widget.Spinner spinnerHarvestUnit;
    TextView tvYieldPerHectareRange, tvTotalYieldRange, tvPredictedHarvestDate;
    TextView tvNetIncomeCard, tvSummarySubtitle, tvCompletionRate, tvAdjustedNetIncome, tvAdjustedSubtitle, tvAdjustedExpenses, tvCompletionWarning;
    TextView tvTotalExpenses;
    com.google.android.material.button.MaterialButton btnDailyExpensesHistory;
    LinearLayout laborBreakdownContainer;
    TextView tvLaborTotalWorkers, tvLaborTotalCost;
    LinearLayout equipmentBreakdownContainer;
    TextView tvEquipmentTotalUsage, tvEquipmentTotalCost;
    LinearLayout materialBreakdownContainer;
    TextView tvMaterialTotalCost;
    LinearLayout miscellaneousBreakdownContainer;
    TextView tvMiscellaneousTotalCost;
    TextView tvCostBreakdownLabor, tvCostBreakdownEquipment, tvCostBreakdownMaterial, tvCostBreakdownMiscellaneous;

    double hectare = 0, AWF = 0, AFP = 0, baseNP = 0, currentNP = 0, marketValue = 0;
    double fertilizerCost = 0, manpower = 0, pesticide = 0, seedlings = 0, otherExpenses = 0;
    double grossIncome = 0, totalExpenses = 0, netIncome = 0;
    // Individual fertilizer costs (stored to calculate based on checkbox selection)
    double completeCostTotal = 0, ureaCostTotal = 0, mopCostTotal = 0;
    String growthHabit = "";
    DecimalFormat df = new DecimalFormat("#,###");
    DecimalFormat df2 = new DecimalFormat("#,###.##");
    
    // Firebase
    private DatabaseReference calculationsRef;
    private DatabaseReference expensesRef; // Daily expenses reference
    private FirebaseUser currentUser;
    private double lastSavedGrossIncome = 0;
    private double lastSavedTotalExpenses = 0;
    private String programId; // Store program ID for saving hectare and analytics
    private String cultivarName; // For analytics/work program record
    private String dateSaved;    // For analytics/work program record (starting date)
    private WorkProgramDataHelper.CompletionStats completionStats;
    
    // Expense data from daily expenses
    private ExpenseSummary laborSummary = new ExpenseSummary();
    private ExpenseSummary equipmentSummary = new ExpenseSummary();
    private ExpenseSummary materialSummary = new ExpenseSummary();
    private ExpenseSummary miscellaneousSummary = new ExpenseSummary();
    
    // Fertilizer prices (PHP per kg)
    private static final double PRICE_COMPLETE = 32.20; // PHP 32.20 / kg
    private static final double PRICE_UREA = 32.40;     // PHP 32.40 / kg
    private static final double PRICE_MOP = 40.70;       // PHP 40.70 / kg
    
    // Data classes for expense summaries
    static class ExpenseSummary {
        double totalCost = 0.0;
        double minCost = Double.MAX_VALUE;
        double maxCost = 0.0;
        double avgCost = 0.0;
        int itemCount = 0;
        int totalWorkers = 0; // For labor only
        double totalUsage = 0.0; // For equipment only (in hours)
        Map<String, CategoryBreakdown> categories = new HashMap<>();
    }
    
    static class CategoryBreakdown {
        String categoryName;
        double totalCost = 0.0;
        int itemCount = 0;
        List<ExpenseItem> items = new ArrayList<>();
    }
    
    static class ExpenseItem {
        String name;
        double cost;
        String details; // For notes/details
        int workers = 0; // For labor
        double usage = 0.0; // For equipment
        String usageUnit = "";
        double quantity = 0.0; // For material
        String quantityUnit = ""; // For material
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        setupDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Projected Income/Expenses");
        }

        // Bind UI elements
        etHectare = findViewById(R.id.etHectare);
        etAWF = findViewById(R.id.etAWF);
        etAFP = findViewById(R.id.etAFP);
        etMarketValue = findViewById(R.id.etMarketValue);
        
        // Expense inputs (removed - now using expense summaries from daily expenses)

        tvCultivarName = findViewById(R.id.tvCultivarName);
        tvDateSaved = findViewById(R.id.tvDateSaved);
        tvNP = findViewById(R.id.tvNP);
        tvTHGrams = findViewById(R.id.tvTHGrams);
        spinnerHarvestUnit = findViewById(R.id.spinnerHarvestUnit);
        
        // Setup harvest unit spinner
        if (spinnerHarvestUnit != null) {
            String[] units = {"kg", "grams"};
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, units);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHarvestUnit.setAdapter(adapter);
            spinnerHarvestUnit.setSelection(0); // Default to kg
            
            // Add listener to recompute when unit changes
            spinnerHarvestUnit.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    compute(); // Recompute when unit changes
                }
                
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }
        tvNetIncomeCard = findViewById(R.id.tvNetIncomeCard);
        tvSummarySubtitle = findViewById(R.id.tvSummarySubtitle);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvAdjustedNetIncome = findViewById(R.id.tvAdjustedNetIncome);
        tvAdjustedSubtitle = findViewById(R.id.tvAdjustedSubtitle);
        tvCompletionWarning = findViewById(R.id.tvCompletionWarning);
        btnDailyExpensesHistory = findViewById(R.id.btnDailyExpensesHistory);
        tvAdjustedExpenses = findViewById(R.id.tvAdjustedExpenses);
        
        // Labor card views
        laborBreakdownContainer = findViewById(R.id.laborBreakdownContainer);
        tvLaborTotalWorkers = findViewById(R.id.tvLaborTotalWorkers);
        tvLaborTotalCost = findViewById(R.id.tvLaborTotalCost);
        
        // Equipment/Tools card views
        equipmentBreakdownContainer = findViewById(R.id.equipmentBreakdownContainer);
        tvEquipmentTotalUsage = findViewById(R.id.tvEquipmentTotalUsage);
        tvEquipmentTotalCost = findViewById(R.id.tvEquipmentTotalCost);
        
        // Material card views
        materialBreakdownContainer = findViewById(R.id.materialBreakdownContainer);
        tvMaterialTotalCost = findViewById(R.id.tvMaterialTotalCost);
        
        // Miscellaneous card views
        miscellaneousBreakdownContainer = findViewById(R.id.miscellaneousBreakdownContainer);
        tvMiscellaneousTotalCost = findViewById(R.id.tvMiscellaneousTotalCost);
        
        // Cost breakdown TextViews
        tvCostBreakdownLabor = findViewById(R.id.tvCostBreakdownLabor);
        tvCostBreakdownEquipment = findViewById(R.id.tvCostBreakdownEquipment);
        tvCostBreakdownMaterial = findViewById(R.id.tvCostBreakdownMaterial);
        tvCostBreakdownMiscellaneous = findViewById(R.id.tvCostBreakdownMiscellaneous);

        // Expense total TextView
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        
        // Harvest prediction fields
        tvYieldPerHectareRange = findViewById(R.id.tvYieldPerHectareRange);
        tvTotalYieldRange = findViewById(R.id.tvTotalYieldRange);
        tvPredictedHarvestDate = findViewById(R.id.tvPredictedHarvestDate);

        // Get values from intent
        cultivarName = getIntent().getStringExtra("cultivar_name");
        dateSaved = getIntent().getStringExtra("date_saved");
        baseNP = getIntent().getDoubleExtra("NP_VALUE", 0); // base NP per hectare
        growthHabit = getIntent().getStringExtra("growth_habit"); // Growth habit
        double prefilledHectare = getIntent().getDoubleExtra("hectare_prefilled", 0);
        programId = getIntent().getStringExtra("program_id");

        // Setup Daily Expenses History button (after programId is loaded)
        setupDailyExpensesHistoryButton();

        // Display cultivar info
        tvCultivarName.setText("Cultivar: " + (cultivarName != null ? cultivarName : "N/A"));
        
        // Format date according to user preference
        String dateDisplay = "N/A";
        if (dateSaved != null && !dateSaved.isEmpty()) {
            try {
                SimpleDateFormat parseFormat = SettingsPreferences.getDateParseFormat();
                SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(this);
                Date dateObj = parseFormat.parse(dateSaved);
                dateDisplay = displayFormat.format(dateObj);
            } catch (Exception e) {
                dateDisplay = dateSaved;
            }
        }
        tvDateSaved.setText("Date Saved: " + dateDisplay);
        
        // Get measurement unit setting
        String measurementUnit = SettingsPreferences.getMeasurementUnit(this);
        String unitLabel = measurementUnit.equals(SettingsPreferences.MEASUREMENT_UNIT_HECTARE) ? "Hectare" : "Hectare"; // Both use hectare for now
        tvNP.setText("Number of Plants Per " + unitLabel + " (NP): " + df.format(baseNP));
        
        // Initialize Firebase first (needed for fetching hectare)
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            calculationsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("calculations");
            
            // Initialize expenses reference if programId is available
            if (programId != null) {
                expensesRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(currentUser.getUid())
                        .child("workPrograms")
                        .child(programId)
                        .child("dailyExpenses");
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

        // Make hectare field read-only (auto-filled) - don't add watcher since it's auto-filled
        etHectare.setFocusable(false);
        etHectare.setClickable(false);
        etHectare.setEnabled(false);
        etHectare.setAlpha(0.7f); // Dim to indicate read-only
        
        etAWF.addTextChangedListener(watcher);
        etAFP.addTextChangedListener(watcher);
        etMarketValue.addTextChangedListener(watcher);
        
        // Initial calculations
        computeFertilizer();
        computeExpenses();
        
        // Auto-save when calculation is complete
        setupAutoSave();

        // Load completion stats for adjusted projections
        loadCompletionStats();
        
        // Load expense data from daily expenses
        loadExpenseData();
    }
    
    /**
     * Sets up the Daily Expenses History button click listener
     */
    private void setupDailyExpensesHistoryButton() {
        if (btnDailyExpensesHistory == null) {
            return;
        }
        
        // Always set click listener - check programId inside
        btnDailyExpensesHistory.setOnClickListener(v -> {
            if (programId != null && !programId.isEmpty()) {
                // Navigate to Daily Expenses History
                Intent intent = new Intent(Calculator.this, DailyExpensesHistoryActivity.class);
                intent.putExtra("programId", programId);
                intent.putExtra("cultivar", cultivarName != null ? cultivarName : "");
                intent.putExtra("startDate", dateSaved != null ? dateSaved : "");
                startActivity(intent);
            } else {
                // Show message if no program ID available
                Toast.makeText(Calculator.this, 
                    "No work program selected. Please open this calculator from a work program.", 
                    Toast.LENGTH_SHORT).show();
            }
        });
        
        // Update button appearance based on programId availability
        if (programId != null && !programId.isEmpty()) {
            btnDailyExpensesHistory.setEnabled(true);
            btnDailyExpensesHistory.setAlpha(1.0f);
        } else {
            btnDailyExpensesHistory.setEnabled(true); // Still clickable to show message
            btnDailyExpensesHistory.setAlpha(0.7f); // Slightly dimmed to indicate limited functionality
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
        
        // Compute expenses from daily expenses data (only when saving)
        computeExpenses();
        
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
                            
                            // Save to local database
                            LocalDataManager.getInstance(Calculator.this).saveCalculation(
                                    calculationId,
                                    currentUser.getUid(),
                                    programId,
                                    grossIncome,
                                    totalExpenses,
                                    netIncome,
                                    hectare,
                                    dateCreated,
                                    dateSaved,
                                    cultivarName
                            );
                            
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
                                
                                // Research fields: Season (auto-detected)
                                if (dateSaved != null && !dateSaved.isEmpty()) {
                                    updates.put("season", SeasonHelper.getSeason(dateSaved));
                                    updates.put("seasonMonth", SeasonHelper.getSeasonMonth(dateSaved));
                                    updates.put("isOffSeason", SeasonHelper.isOffSeason(dateSaved));
                                }
                                
                                // Harvest predictions are calculated and displayed, not saved separately

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

            // 🔹 Validate inputs
            boolean hasValidationWarnings = validateInputs();

            // 🔹 Update NP immediately when hectare changes
            currentNP = baseNP * hectare;
            if (hectare > 0) {
                // Get measurement unit setting
                String measurementUnit = SettingsPreferences.getMeasurementUnit(this);
                String unitLabel = measurementUnit.equals(SettingsPreferences.MEASUREMENT_UNIT_HECTARE) ? "Hectare" : "Hectare";
                tvNP.setText("Number of Plants per Hectare: " + df.format(currentNP));
            } else {
                tvNP.setText("Number of Plants per Hectare: " + df.format(baseNP));
            }

            // 🔹 Compute totals only when all fields are filled
            if (hectare > 0 && AWF > 0 && AFP > 0 && marketValue > 0) {
                double AWP = AWF * AFP;           // Average Weight per Plant
                double TH = AWP * currentNP;      // Total Harvest (grams)
                double THKg = TH / 1000;          // Total Harvest (kilograms)
                
                // Apply harvest efficiency factor (90% - typical marketable yield)
                // Accounts for non-marketable fruits, damage, etc.
                double harvestEfficiency = 0.90;
                double marketableYield = THKg * harvestEfficiency;
                
                // Apply seasonal price multiplier
                double seasonalMultiplier = 1.0;
                if (dateSaved != null && !dateSaved.isEmpty()) {
                    seasonalMultiplier = SeasonHelper.getPriceMultiplier(dateSaved);
                }
                
                grossIncome = marketableYield * marketValue * seasonalMultiplier;

                // Update harvest display based on selected unit
                if (spinnerHarvestUnit != null && tvTHGrams != null) {
                    String selectedUnit = (String) spinnerHarvestUnit.getSelectedItem();
                    if ("kg".equals(selectedUnit)) {
                        tvTHGrams.setText(df2.format(THKg));
                    } else {
                        tvTHGrams.setText(df2.format(TH));
                    }
                }
                
                // Calculate and display harvest predictions with ranges
                calculateHarvestPredictions(THKg, hectare);
            } else {
                grossIncome = 0;
                if (tvTHGrams != null) {
                    tvTHGrams.setText("—");
                }
                // Clear predictions
                if (tvYieldPerHectareRange != null) tvYieldPerHectareRange.setText("— kg/hectare");
                if (tvTotalYieldRange != null) tvTotalYieldRange.setText("— kg");
                if (tvPredictedHarvestDate != null) tvPredictedHarvestDate.setText("—");
            }
            
            // 🔹 Compute fertilizer requirements
            computeFertilizer();

            // 🔹 Update pesticide breakdown with improved calculation
            updatePesticideBreakdown();
            
            // 🔹 Calculate net income based on gross income and fertilizer cost only (for UI updates)
            // Daily expenses calculation will only happen when save button is clicked
            double currentExpenses = fertilizerCost;
            netIncome = grossIncome - currentExpenses;
            
            // Update card net income (based on fertilizer cost only, not daily expenses)
            if (tvNetIncomeCard != null) {
                tvNetIncomeCard.setText("₱" + df2.format(netIncome));
            }
            
            // Auto-save to Firebase when calculation is complete
            if (grossIncome > 0 && currentExpenses >= 0) {
                saveCalculation();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Calculates and displays harvest predictions with ranges.
     * All predictions are shown as ranges (min-max) instead of exact values.
     */
    private void calculateHarvestPredictions(double totalHarvestKg, double hectare) {
        if (tvYieldPerHectareRange == null || tvTotalYieldRange == null || tvPredictedHarvestDate == null) {
            return;
        }
        
        try {
            // Calculate yield per hectare with ranges
            // Typical tomato yield ranges: 20-60 tons/hectare (20,000-60,000 kg/hectare)
            // We'll use efficiency factors to create a range around calculated value
            double baseYieldPerHectare = totalHarvestKg / hectare;
            
            // Apply efficiency factors: 0.85 (pessimistic) to 1.10 (optimistic)
            double minYieldPerHectare = baseYieldPerHectare * 0.85;
            double maxYieldPerHectare = baseYieldPerHectare * 1.10;
            double avgYieldPerHectare = baseYieldPerHectare * 0.95; // Average efficiency
            
            // Display yield per hectare range
            String yieldRange = String.format("%.0f - %.0f kg/hectare (avg: %.0f)", 
                minYieldPerHectare, maxYieldPerHectare, avgYieldPerHectare);
            tvYieldPerHectareRange.setText(yieldRange);
            
            // Calculate total yield range
            double minTotalYield = minYieldPerHectare * hectare;
            double maxTotalYield = maxYieldPerHectare * hectare;
            double avgTotalYield = avgYieldPerHectare * hectare;
            
            // Display total yield range
            String totalYieldRange = String.format("%.0f - %.0f kg (avg: %.0f)", 
                minTotalYield, maxTotalYield, avgTotalYield);
            tvTotalYieldRange.setText(totalYieldRange);
            
            // Calculate predicted harvest date based on maturity days
            if (dateSaved != null && !dateSaved.isEmpty() && cultivarName != null) {
                int maturityDays = WorkProgramDataHelper.getMaturityDays(cultivarName);
                if (maturityDays > 0) {
                    try {
                        SimpleDateFormat parseFormat = SettingsPreferences.getDateParseFormat();
                        SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(this);
                        Date startDate = parseFormat.parse(dateSaved);
                        if (startDate != null) {
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(startDate);
                            
                            // Add maturity days with a range: -5 to +10 days variation
                            int minDays = maturityDays - 5;
                            int maxDays = maturityDays + 10;
                            
                            // Calculate min harvest date
                            cal.setTime(startDate);
                            cal.add(java.util.Calendar.DAY_OF_YEAR, minDays);
                            Date minHarvestDate = cal.getTime();
                            
                            // Calculate max harvest date
                            cal.setTime(startDate);
                            cal.add(java.util.Calendar.DAY_OF_YEAR, maxDays);
                            Date maxHarvestDate = cal.getTime();
                            
                            // Calculate average harvest date
                            cal.setTime(startDate);
                            cal.add(java.util.Calendar.DAY_OF_YEAR, maturityDays);
                            Date avgHarvestDate = cal.getTime();
                            
                            // Display harvest date range
                            String minDateStr = displayFormat.format(minHarvestDate);
                            String maxDateStr = displayFormat.format(maxHarvestDate);
                            String avgDateStr = displayFormat.format(avgHarvestDate);
                            String dateRange = String.format("%s - %s (avg: %s)", 
                                minDateStr, maxDateStr, avgDateStr);
                            tvPredictedHarvestDate.setText(dateRange);
                        }
                    } catch (Exception e) {
                        tvPredictedHarvestDate.setText("—");
                        e.printStackTrace();
                    }
                } else {
                    tvPredictedHarvestDate.setText("—");
                }
            } else {
                tvPredictedHarvestDate.setText("—");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            tvYieldPerHectareRange.setText("— kg/hectare");
            tvTotalYieldRange.setText("— kg");
            tvPredictedHarvestDate.setText("—");
        }
    }
    
    /**
     * Validates input values and shows warnings if values seem unrealistic.
     * @return true if there are validation warnings
     */
    private boolean validateInputs() {
        boolean hasWarnings = false;
        
        // Validate hectare
        if (hectare > 0 && (hectare < 0.01 || hectare > 1000)) {
            // Silently allow but could show warning
            hasWarnings = true;
        }
        
        // Validate AWF (Average Weight per Fruit) - typical: 50-500g
        if (AWF > 0 && (AWF < 50 || AWF > 500)) {
            // Values outside typical range - could show warning
            hasWarnings = true;
        }
        
        // Validate AFP (Average Fruits per Plant) - typical: 10-100
        if (AFP > 0 && (AFP < 10 || AFP > 100)) {
            // Values outside typical range
            hasWarnings = true;
        }
        
        // Validate market value - typical: ₱20-₱150/kg
        if (marketValue > 0 && (marketValue < 20 || marketValue > 150)) {
            // Values outside typical range
            hasWarnings = true;
        }
        
        return hasWarnings;
    }
    
    private void computeFertilizer() {
        if (hectare <= 0 || baseNP <= 0) {
            // Reset fertilizer costs
            completeCostTotal = 0;
            ureaCostTotal = 0;
            mopCostTotal = 0;
            fertilizerCost = 0;
            return;
        }
        
        // P = plants per hectare (baseNP)
        double P = baseNP;
        
        // Calculate fertilizer requirements per hectare
        // Complete (14-14-14): 10 g per plant × 1 application = 10 g/plant
        double completeKgPerHa = (10.0 * 1.0 * P) / 1000.0;
        
        // Urea (46-0-0): 10 g per plant × (2/3) × 4 applications = 26.68 g/plant
        double ureaKgPerHa = (10.0 * (2.0/3.0) * 4.0 * P) / 1000.0;
        
        // MOP (0-0-60): 10 g per plant × (1/3) × 4 applications = 13.32 g/plant
        double mopKgPerHa = (10.0 * (1.0/3.0) * 4.0 * P) / 1000.0;
        
        // Calculate costs per hectare
        double completeCostPerHa = completeKgPerHa * PRICE_COMPLETE;
        double ureaCostPerHa = ureaKgPerHa * PRICE_UREA;
        double mopCostPerHa = mopKgPerHa * PRICE_MOP;
        double totalCostPerHa = completeCostPerHa + ureaCostPerHa + mopCostPerHa;
        
        // Scale by hectare
        double completeKgTotal = completeKgPerHa * hectare;
        double ureaKgTotal = ureaKgPerHa * hectare;
        double mopKgTotal = mopKgPerHa * hectare;
        // Store individual costs as instance variables
        completeCostTotal = completeCostPerHa * hectare;
        ureaCostTotal = ureaCostPerHa * hectare;
        mopCostTotal = mopCostPerHa * hectare;
        
        // Calculate total fertilizer cost (all fertilizers included by default)
        fertilizerCost = completeCostTotal + ureaCostTotal + mopCostTotal;
    }
    
    /**
     * Updates the fertilizer cost (all fertilizers included by default)
     */
    private void updateFertilizerCost() {
        // All fertilizers are included by default
        fertilizerCost = completeCostTotal + ureaCostTotal + mopCostTotal;
    }
    
    private void computeExpenses() {
        try {
            // Use expense summaries if available (from daily expenses), otherwise use input fields
            boolean hasExpenseData = (laborSummary != null && laborSummary.itemCount > 0) ||
                                    (equipmentSummary != null && equipmentSummary.itemCount > 0) ||
                                    (materialSummary != null && materialSummary.itemCount > 0) ||
                                    (miscellaneousSummary != null && miscellaneousSummary.itemCount > 0);
            
            if (hasExpenseData) {
                // Total expenses is already calculated in updateTotalExpensesWithRange()
                // Just recalculate net income
            } else {
                // No expense data available - use fertilizer cost only (if calculated)
                totalExpenses = fertilizerCost;
                
                // Display results
                if (tvTotalExpenses != null) {
                    tvTotalExpenses.setText("Total Expenses (₱): " + df2.format(totalExpenses));
                }
            }
            
            // Calculate net income = Gross Income - Total Expenses
            netIncome = grossIncome - totalExpenses;
            
            // Update card net income
            if (tvNetIncomeCard != null) {
                tvNetIncomeCard.setText("₱" + df2.format(netIncome));
            }

            applyAdjustedProjection();
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values on error
            if (tvTotalExpenses != null) {
                tvTotalExpenses.setText("Total Expenses (₱): —");
            }
            if (tvNetIncomeCard != null) {
                tvNetIncomeCard.setText("₱0.00");
            }
            if (tvAdjustedNetIncome != null) {
                tvAdjustedNetIncome.setText("₱—");
            }
            if (tvAdjustedExpenses != null) {
                tvAdjustedExpenses.setText("Adjusted Expenses (₱): —");
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

    /**
     * Simple helper to toggle visibility of a collapsible section.
     */
    private void setupSectionToggle(final View header, final View content, final ImageView icon) {
        if (header == null || content == null) return;
        header.setOnClickListener(v -> {
            if (content.getVisibility() == View.VISIBLE) {
                content.setVisibility(View.GONE);
                if (icon != null) icon.setRotation(180f);
            } else {
                content.setVisibility(View.VISIBLE);
                if (icon != null) icon.setRotation(0f);
            }
        });
    }

    /**
     * Calculates pesticide cost based on multiple factors:
     * - Base cost per hectare (literature-based)
     * - Cultivar disease resistance
     * - Season (off-season typically needs more pesticides)
     * - Detection history (if available)
     * Note: Pesticide card was removed, but calculation kept for potential future use
     */
    private void updatePesticideBreakdown() {
        // Pesticide breakdown UI was removed - calculation kept for reference
        if (hectare <= 0) {
            return;
        }

        // Base pesticide cost per hectare (PHP) - based on typical Philippine farming costs
        // Average: ₱5,000-₱7,000 per hectare for tomato production
        double basePesticidePerHa = 5500.0;
        
        // Factor 1: Cultivar disease resistance
        // Some cultivars are more resistant, requiring less pesticide
        double cultivarFactor = getCultivarDiseaseResistanceFactor(cultivarName);
        
        // Factor 2: Season
        // Off-season (wet season) typically requires 20-30% more pesticides
        double seasonFactor = 1.0;
        if (dateSaved != null && !dateSaved.isEmpty()) {
            if (SeasonHelper.isOffSeason(dateSaved)) {
                seasonFactor = 1.25; // 25% increase for off-season
            }
        }
        
        // Factor 3: Detection history (if programId is available)
        // More detections = higher pesticide needs
        double detectionFactor = getDetectionHistoryFactor();
        
        // Calculate total pesticide cost per hectare
        double pesticidePerHa = basePesticidePerHa * cultivarFactor * seasonFactor * detectionFactor;
        double suggestedTotal = pesticidePerHa * hectare;

        // Pesticide calculation kept for reference but not displayed
        // Breakdown: 40% preventive, 40% curative, 20% other (equipment, application costs)
        // Note: This is no longer used in the UI but kept for potential future use
    }
    
    /**
     * Gets disease resistance factor for a cultivar.
     * Lower factor = more resistant = less pesticide needed.
     * @param cultivarName Name of the cultivar
     * @return Factor (0.7-1.3, where 1.0 is average)
     */
    private double getCultivarDiseaseResistanceFactor(String cultivarName) {
        if (cultivarName == null) return 1.0;
        
        // Some cultivars are known for better disease resistance
        // This is a simplified model - can be enhanced with actual research data
        String lowerName = cultivarName.toLowerCase();
        
        // More resistant cultivars (require less pesticide)
        if (lowerName.contains("victory") || lowerName.contains("hope") || 
            lowerName.contains("maganda") || lowerName.contains("malakas")) {
            return 0.85; // 15% less pesticide
        }
        
        // Less resistant cultivars (require more pesticide)
        if (lowerName.contains("tom-055") || lowerName.contains("tom-262") ||
            lowerName.contains("dalwangan")) {
            return 1.15; // 15% more pesticide
        }
        
        // Average resistance
        return 1.0;
    }
    
    /**
     * Gets detection history factor based on past disease/pest detections.
     * More detections = higher pesticide needs.
     * @return Factor (1.0-1.3)
     */
    private double getDetectionHistoryFactor() {
        if (programId == null || currentUser == null || dateSaved == null) {
            return 1.0; // No data available, use base factor
        }
        
        try {
            // Count detections for this program
            ArrayList<org.json.JSONObject> history = DetectionHistoryManager.getHistory(this);
            if (history == null || history.isEmpty()) {
                return 1.0;
            }
            
            // Parse start date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date programStart = sdf.parse(dateSaved);
            if (programStart == null) return 1.0;
            
            // Count detections after program start
            int detectionCount = 0;
            for (org.json.JSONObject entry : history) {
                long timestamp = entry.optLong("timestamp", 0);
                if (timestamp > 0) {
                    Date detectionDate = new Date(timestamp);
                    if (detectionDate.after(programStart) || detectionDate.equals(programStart)) {
                        String entryCultivar = entry.optString("cultivar", "");
                        // Only count if it matches current cultivar or is unspecified
                        if (cultivarName == null || entryCultivar.isEmpty() || 
                            entryCultivar.equalsIgnoreCase(cultivarName)) {
                            detectionCount++;
                        }
                    }
                }
            }
            
            // Factor: 1.0 (no detections) to 1.3 (many detections)
            // Scale: 0 detections = 1.0, 5+ detections = 1.3
            if (detectionCount == 0) {
                return 1.0;
            } else if (detectionCount <= 2) {
                return 1.1; // 10% increase
            } else if (detectionCount <= 4) {
                return 1.2; // 20% increase
            } else {
                return 1.3; // 30% increase for 5+ detections
            }
        } catch (Exception e) {
            return 1.0; // Default on error
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
    
    /**
     * Loads expense data from daily expenses and aggregates by category
     */
    private void loadExpenseData() {
        if (expensesRef == null || programId == null || dateSaved == null) {
            return;
        }
        
        expensesRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                // Reset summaries
                laborSummary = new ExpenseSummary();
                equipmentSummary = new ExpenseSummary();
                materialSummary = new ExpenseSummary();
                miscellaneousSummary = new ExpenseSummary();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                
                for (com.google.firebase.database.DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();
                    if (dateKey == null) continue;
                    
                    // Process labor items
                    if (dateSnapshot.hasChild("labor")) {
                        for (com.google.firebase.database.DataSnapshot laborSnapshot : dateSnapshot.child("labor").getChildren()) {
                            Double totalCost = laborSnapshot.child("totalCost").getValue(Double.class);
                            Integer numWorkers = laborSnapshot.child("numWorkers").getValue(Integer.class);
                            String notes = laborSnapshot.child("notes").getValue(String.class);
                            Double dailyWage = laborSnapshot.child("dailyWage").getValue(Double.class);
                            
                            if (totalCost != null && totalCost > 0) {
                                ExpenseItem item = new ExpenseItem();
                                item.name = notes != null && !notes.isEmpty() ? notes : "Labor";
                                item.cost = totalCost;
                                item.workers = numWorkers != null ? numWorkers : 0;
                                item.details = "Daily Wage: ₱" + (dailyWage != null ? dailyWage : 0.0);
                                
                                String categoryKey = item.name;
                                if (!laborSummary.categories.containsKey(categoryKey)) {
                                    CategoryBreakdown breakdown = new CategoryBreakdown();
                                    breakdown.categoryName = categoryKey;
                                    laborSummary.categories.put(categoryKey, breakdown);
                                }
                                
                                CategoryBreakdown breakdown = laborSummary.categories.get(categoryKey);
                                breakdown.items.add(item);
                                breakdown.totalCost += totalCost;
                                breakdown.itemCount++;
                                
                                laborSummary.totalCost += totalCost;
                                laborSummary.totalWorkers += item.workers;
                                laborSummary.itemCount++;
                                if (totalCost < laborSummary.minCost) laborSummary.minCost = totalCost;
                                if (totalCost > laborSummary.maxCost) laborSummary.maxCost = totalCost;
                            }
                        }
                    }
                    
                    // Process equipment items
                    if (dateSnapshot.hasChild("equipment")) {
                        for (com.google.firebase.database.DataSnapshot equipmentSnapshot : dateSnapshot.child("equipment").getChildren()) {
                            Double totalCost = equipmentSnapshot.child("totalCost").getValue(Double.class);
                            String equipmentName = equipmentSnapshot.child("equipmentName").getValue(String.class);
                            Double usageValue = equipmentSnapshot.child("usageValue").getValue(Double.class);
                            String usageUnit = equipmentSnapshot.child("usageUnit").getValue(String.class);
                            Double cost = equipmentSnapshot.child("cost").getValue(Double.class);
                            Boolean isOwned = equipmentSnapshot.child("isOwned").getValue(Boolean.class);
                            
                            // Skip owned equipment
                            if (isOwned != null && isOwned) continue;
                            
                            if (totalCost != null && totalCost > 0 && equipmentName != null) {
                                ExpenseItem item = new ExpenseItem();
                                item.name = equipmentName;
                                item.cost = totalCost;
                                item.usage = usageValue != null ? usageValue : 0.0;
                                item.usageUnit = usageUnit != null ? usageUnit : "hours";
                                item.details = "Rental: ₱" + (cost != null ? cost : 0.0) + "/hour";
                                
                                // Convert usage to hours for aggregation
                                double usageInHours = item.usage;
                                if ("minutes".equals(item.usageUnit)) {
                                    usageInHours = item.usage / 60.0;
                                }
                                
                                String categoryKey = item.name;
                                if (!equipmentSummary.categories.containsKey(categoryKey)) {
                                    CategoryBreakdown breakdown = new CategoryBreakdown();
                                    breakdown.categoryName = categoryKey;
                                    equipmentSummary.categories.put(categoryKey, breakdown);
                                }
                                
                                CategoryBreakdown breakdown = equipmentSummary.categories.get(categoryKey);
                                breakdown.items.add(item);
                                breakdown.totalCost += totalCost;
                                breakdown.itemCount++;
                                
                                equipmentSummary.totalCost += totalCost;
                                equipmentSummary.totalUsage += usageInHours;
                                equipmentSummary.itemCount++;
                                if (totalCost < equipmentSummary.minCost) equipmentSummary.minCost = totalCost;
                                if (totalCost > equipmentSummary.maxCost) equipmentSummary.maxCost = totalCost;
                            }
                        }
                    }
                    
                    // Process material items
                    if (dateSnapshot.hasChild("material")) {
                        for (com.google.firebase.database.DataSnapshot materialSnapshot : dateSnapshot.child("material").getChildren()) {
                            Double totalCost = materialSnapshot.child("totalCost").getValue(Double.class);
                            String materialName = materialSnapshot.child("materialName").getValue(String.class);
                            Double quantity = materialSnapshot.child("quantity").getValue(Double.class);
                            String quantityUnit = materialSnapshot.child("quantityUnit").getValue(String.class);
                            
                            if (totalCost != null && totalCost > 0 && materialName != null) {
                                ExpenseItem item = new ExpenseItem();
                                item.name = materialName;
                                item.cost = totalCost;
                                item.quantity = quantity != null ? quantity : 0.0;
                                item.quantityUnit = quantityUnit != null ? quantityUnit : "";
                                
                                String categoryKey = item.name;
                                if (!materialSummary.categories.containsKey(categoryKey)) {
                                    CategoryBreakdown breakdown = new CategoryBreakdown();
                                    breakdown.categoryName = categoryKey;
                                    materialSummary.categories.put(categoryKey, breakdown);
                                }
                                
                                CategoryBreakdown breakdown = materialSummary.categories.get(categoryKey);
                                breakdown.items.add(item);
                                breakdown.totalCost += totalCost;
                                breakdown.itemCount++;
                                
                                materialSummary.totalCost += totalCost;
                                materialSummary.itemCount++;
                                if (totalCost < materialSummary.minCost) materialSummary.minCost = totalCost;
                                if (totalCost > materialSummary.maxCost) materialSummary.maxCost = totalCost;
                            }
                        }
                    }
                    
                    // Process miscellaneous items
                    if (dateSnapshot.hasChild("miscellaneous")) {
                        for (com.google.firebase.database.DataSnapshot miscSnapshot : dateSnapshot.child("miscellaneous").getChildren()) {
                            Double cost = miscSnapshot.child("cost").getValue(Double.class);
                            String expenseName = miscSnapshot.child("expenseName").getValue(String.class);
                            
                            if (cost != null && cost > 0 && expenseName != null) {
                                ExpenseItem item = new ExpenseItem();
                                item.name = expenseName;
                                item.cost = cost;
                                
                                String categoryKey = item.name;
                                if (!miscellaneousSummary.categories.containsKey(categoryKey)) {
                                    CategoryBreakdown breakdown = new CategoryBreakdown();
                                    breakdown.categoryName = categoryKey;
                                    miscellaneousSummary.categories.put(categoryKey, breakdown);
                                }
                                
                                CategoryBreakdown breakdown = miscellaneousSummary.categories.get(categoryKey);
                                breakdown.items.add(item);
                                breakdown.totalCost += cost;
                                breakdown.itemCount++;
                                
                                miscellaneousSummary.totalCost += cost;
                                miscellaneousSummary.itemCount++;
                                if (cost < miscellaneousSummary.minCost) miscellaneousSummary.minCost = cost;
                                if (cost > miscellaneousSummary.maxCost) miscellaneousSummary.maxCost = cost;
                            }
                        }
                    }
                }
                
                // Calculate averages
                if (laborSummary.itemCount > 0) {
                    laborSummary.avgCost = laborSummary.totalCost / laborSummary.itemCount;
                    if (laborSummary.minCost == Double.MAX_VALUE) laborSummary.minCost = 0.0;
                }
                if (equipmentSummary.itemCount > 0) {
                    equipmentSummary.avgCost = equipmentSummary.totalCost / equipmentSummary.itemCount;
                    if (equipmentSummary.minCost == Double.MAX_VALUE) equipmentSummary.minCost = 0.0;
                }
                if (materialSummary.itemCount > 0) {
                    materialSummary.avgCost = materialSummary.totalCost / materialSummary.itemCount;
                    if (materialSummary.minCost == Double.MAX_VALUE) materialSummary.minCost = 0.0;
                }
                if (miscellaneousSummary.itemCount > 0) {
                    miscellaneousSummary.avgCost = miscellaneousSummary.totalCost / miscellaneousSummary.itemCount;
                    if (miscellaneousSummary.minCost == Double.MAX_VALUE) miscellaneousSummary.minCost = 0.0;
                }
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    updateExpenseDisplay();
                });
            }
            
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                // Handle error silently
            }
        });
    }
    
    /**
     * Updates the expense display with fetched data
     */
    private void updateExpenseDisplay() {
        updateLaborCard();
        updateEquipmentCard();
        updateMaterialCard();
        updateMiscellaneousCard();
        updateCostBreakdown();
    }
    
    /**
     * Updates the Labor card with categorized expense data
     */
    private void updateLaborCard() {
        if (laborBreakdownContainer == null || tvLaborTotalWorkers == null || tvLaborTotalCost == null) {
            return;
        }
        
        // Clear existing breakdown
        laborBreakdownContainer.removeAllViews();
        
        if (laborSummary == null || laborSummary.categories.isEmpty()) {
            // Show "No data" message
            TextView noDataText = new TextView(this);
            noDataText.setText("No labor expenses recorded yet");
            noDataText.setTextSize(14f);
            noDataText.setTextColor(0xFF999999);
            noDataText.setPadding(0, 8, 0, 8);
            laborBreakdownContainer.addView(noDataText);
            
            tvLaborTotalWorkers.setText("0");
            tvLaborTotalCost.setText("₱0.00");
            return;
        }
        
        // Display categorized breakdown by activity
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormat dfInt = new DecimalFormat("#,##0");
        
        for (Map.Entry<String, CategoryBreakdown> entry : laborSummary.categories.entrySet()) {
            CategoryBreakdown breakdown = entry.getValue();
            
            // Calculate stats for this activity
            int activityTotalWorkers = 0;
            double minDailyWage = Double.MAX_VALUE;
            double maxDailyWage = 0.0;
            double totalDailyWage = 0.0;
            int wageCount = 0;
            
            for (ExpenseItem item : breakdown.items) {
                activityTotalWorkers += item.workers;
                // Extract daily wage from details if available
                if (item.details != null && item.details.contains("Daily Wage: ₱")) {
                    try {
                        String wageStr = item.details.replace("Daily Wage: ₱", "").trim();
                        double wage = Double.parseDouble(wageStr);
                        if (wage > 0) {
                            totalDailyWage += wage;
                            wageCount++;
                            if (wage < minDailyWage) minDailyWage = wage;
                            if (wage > maxDailyWage) maxDailyWage = wage;
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
            }
            
            // Create activity card
            androidx.cardview.widget.CardView activityCard = new androidx.cardview.widget.CardView(this);
            androidx.cardview.widget.CardView.LayoutParams cardParams = new androidx.cardview.widget.CardView.LayoutParams(
                androidx.cardview.widget.CardView.LayoutParams.MATCH_PARENT,
                androidx.cardview.widget.CardView.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 12);
            activityCard.setLayoutParams(cardParams);
            activityCard.setRadius(8f);
            activityCard.setCardElevation(2f);
            activityCard.setCardBackgroundColor(0xFFF5F5F5);
            activityCard.setUseCompatPadding(true);
            
            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(16, 16, 16, 16);
            activityCard.addView(cardContent);
            
            // Activity name
            TextView activityName = new TextView(this);
            activityName.setText(breakdown.categoryName);
            activityName.setTextSize(16f);
            activityName.setTextColor(0xFF333333);
            activityName.setTypeface(null, android.graphics.Typeface.BOLD);
            activityName.setPadding(0, 0, 0, 8);
            cardContent.addView(activityName);
            
            // Daily wage info
            if (wageCount > 0) {
                double avgDailyWage = totalDailyWage / wageCount;
                TextView wageText = new TextView(this);
                String wageRange;
                if (minDailyWage == maxDailyWage) {
                    wageRange = "Daily Wage: ₱" + df.format(avgDailyWage);
                } else {
                    wageRange = String.format(Locale.getDefault(), 
                        "Daily Wage: ₱%.0f - ₱%.0f (avg: ₱%.0f)", 
                        minDailyWage, maxDailyWage, avgDailyWage);
                }
                wageText.setText(wageRange);
                wageText.setTextSize(13f);
                wageText.setTextColor(0xFF666666);
                wageText.setPadding(0, 0, 0, 4);
                cardContent.addView(wageText);
            }
            
            // Number of workers
            TextView workersText = new TextView(this);
            workersText.setText("Number of Workers: " + activityTotalWorkers);
            workersText.setTextSize(13f);
            workersText.setTextColor(0xFF666666);
            workersText.setPadding(0, 0, 0, 4);
            cardContent.addView(workersText);
            
            // Activity cost
            TextView costText = new TextView(this);
            costText.setText("Total Cost: ₱" + df.format(breakdown.totalCost));
            costText.setTextSize(13f);
            costText.setTextColor(0xFF666666);
            cardContent.addView(costText);
            
            laborBreakdownContainer.addView(activityCard);
        }
        
        // Update totals
        tvLaborTotalWorkers.setText(dfInt.format(laborSummary.totalWorkers));
        
        // Display cost as range (showing min, max, and average per item)
        if (laborSummary.itemCount > 0) {
            String costRange;
            if (laborSummary.minCost == laborSummary.maxCost) {
                costRange = "₱" + df.format(laborSummary.totalCost);
            } else {
                // Calculate total range: min total (min cost × item count) to max total (max cost × item count)
                double minTotal = laborSummary.minCost * laborSummary.itemCount;
                double maxTotal = laborSummary.maxCost * laborSummary.itemCount;
                costRange = String.format(Locale.getDefault(), 
                    "₱%.0f - ₱%.0f (avg: ₱%.0f)", 
                    minTotal, maxTotal, laborSummary.totalCost);
            }
            tvLaborTotalCost.setText(costRange);
        } else {
            tvLaborTotalCost.setText("₱0.00");
        }
    }
    
    /**
     * Updates the Equipment/Tools card with categorized expense data
     */
    private void updateEquipmentCard() {
        if (equipmentBreakdownContainer == null || tvEquipmentTotalUsage == null || tvEquipmentTotalCost == null) {
            return;
        }
        
        // Clear existing breakdown
        equipmentBreakdownContainer.removeAllViews();
        
        if (equipmentSummary == null || equipmentSummary.categories.isEmpty()) {
            // Show "No data" message
            TextView noDataText = new TextView(this);
            noDataText.setText("No equipment/tools expenses recorded yet");
            noDataText.setTextSize(14f);
            noDataText.setTextColor(0xFF999999);
            noDataText.setPadding(0, 8, 0, 8);
            equipmentBreakdownContainer.addView(noDataText);
            
            tvEquipmentTotalUsage.setText("0 hours");
            tvEquipmentTotalCost.setText("₱0.00");
            return;
        }
        
        // Display categorized breakdown by equipment name
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormat dfInt = new DecimalFormat("#,##0");
        
        for (Map.Entry<String, CategoryBreakdown> entry : equipmentSummary.categories.entrySet()) {
            CategoryBreakdown breakdown = entry.getValue();
            
            // Calculate stats for this equipment
            double totalUsageHours = 0.0;
            double minRentalCost = Double.MAX_VALUE;
            double maxRentalCost = 0.0;
            double totalRentalCost = 0.0;
            int rentalCount = 0;
            
            for (ExpenseItem item : breakdown.items) {
                // Convert usage to hours if needed
                double usageInHours = item.usage;
                if ("minutes".equals(item.usageUnit)) {
                    usageInHours = item.usage / 60.0;
                }
                totalUsageHours += usageInHours;
                
                // Extract rental cost from details if available
                if (item.details != null && item.details.contains("Rental: ₱")) {
                    try {
                        String rentalStr = item.details.replace("Rental: ₱", "").replace("/hour", "").trim();
                        double rental = Double.parseDouble(rentalStr);
                        if (rental > 0) {
                            totalRentalCost += rental;
                            rentalCount++;
                            if (rental < minRentalCost) minRentalCost = rental;
                            if (rental > maxRentalCost) maxRentalCost = rental;
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
            }
            
            // Create equipment card
            androidx.cardview.widget.CardView equipmentCard = new androidx.cardview.widget.CardView(this);
            androidx.cardview.widget.CardView.LayoutParams cardParams = new androidx.cardview.widget.CardView.LayoutParams(
                androidx.cardview.widget.CardView.LayoutParams.MATCH_PARENT,
                androidx.cardview.widget.CardView.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 12);
            equipmentCard.setLayoutParams(cardParams);
            equipmentCard.setRadius(8f);
            equipmentCard.setCardElevation(2f);
            equipmentCard.setCardBackgroundColor(0xFFF5F5F5);
            equipmentCard.setUseCompatPadding(true);
            
            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(16, 16, 16, 16);
            equipmentCard.addView(cardContent);
            
            // Equipment name
            TextView equipmentName = new TextView(this);
            equipmentName.setText(breakdown.categoryName);
            equipmentName.setTextSize(16f);
            equipmentName.setTextColor(0xFF333333);
            equipmentName.setTypeface(null, android.graphics.Typeface.BOLD);
            equipmentName.setPadding(0, 0, 0, 8);
            cardContent.addView(equipmentName);
            
            // Usage info
            TextView usageText = new TextView(this);
            usageText.setText(String.format(Locale.getDefault(), "Usage: %.2f hours", totalUsageHours));
            usageText.setTextSize(13f);
            usageText.setTextColor(0xFF666666);
            usageText.setPadding(0, 0, 0, 4);
            cardContent.addView(usageText);
            
            // Rental cost info
            if (rentalCount > 0) {
                double avgRentalCost = totalRentalCost / rentalCount;
                TextView rentalText = new TextView(this);
                String rentalRange;
                if (minRentalCost == maxRentalCost) {
                    rentalRange = "Rental Cost: ₱" + df.format(avgRentalCost) + "/hour";
                } else {
                    rentalRange = String.format(Locale.getDefault(), 
                        "Rental Cost: ₱%.0f - ₱%.0f/hour (avg: ₱%.0f/hour)", 
                        minRentalCost, maxRentalCost, avgRentalCost);
                }
                rentalText.setText(rentalRange);
                rentalText.setTextSize(13f);
                rentalText.setTextColor(0xFF666666);
                rentalText.setPadding(0, 0, 0, 4);
                cardContent.addView(rentalText);
            }
            
            // Equipment cost
            TextView costText = new TextView(this);
            costText.setText("Total Cost: ₱" + df.format(breakdown.totalCost));
            costText.setTextSize(13f);
            costText.setTextColor(0xFF666666);
            cardContent.addView(costText);
            
            equipmentBreakdownContainer.addView(equipmentCard);
        }
        
        // Update totals
        // Format total usage in hours with decimals
        if (equipmentSummary.totalUsage > 0) {
            tvEquipmentTotalUsage.setText(String.format(Locale.getDefault(), "%.2f hours", equipmentSummary.totalUsage));
        } else {
            tvEquipmentTotalUsage.setText("0 hours");
        }
        
        // Display cost as range
        if (equipmentSummary.itemCount > 0) {
            String costRange;
            if (equipmentSummary.minCost == equipmentSummary.maxCost) {
                costRange = "₱" + df.format(equipmentSummary.totalCost);
            } else {
                // Calculate total range: min total (min cost × item count) to max total (max cost × item count)
                double minTotal = equipmentSummary.minCost * equipmentSummary.itemCount;
                double maxTotal = equipmentSummary.maxCost * equipmentSummary.itemCount;
                costRange = String.format(Locale.getDefault(), 
                    "₱%.0f - ₱%.0f (avg: ₱%.0f)", 
                    minTotal, maxTotal, equipmentSummary.totalCost);
            }
            tvEquipmentTotalCost.setText(costRange);
        } else {
            tvEquipmentTotalCost.setText("₱0.00");
        }
    }
    
    /**
     * Updates the Material card with categorized expense data
     */
    private void updateMaterialCard() {
        if (materialBreakdownContainer == null || tvMaterialTotalCost == null) {
            return;
        }
        
        // Clear existing breakdown
        materialBreakdownContainer.removeAllViews();
        
        if (materialSummary == null || materialSummary.categories.isEmpty()) {
            // Show "No data" message
            TextView noDataText = new TextView(this);
            noDataText.setText("No material expenses recorded yet");
            noDataText.setTextSize(14f);
            noDataText.setTextColor(0xFF999999);
            noDataText.setPadding(0, 8, 0, 8);
            materialBreakdownContainer.addView(noDataText);
            
            tvMaterialTotalCost.setText("₱0.00");
            return;
        }
        
        // Display categorized breakdown by material name
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        for (Map.Entry<String, CategoryBreakdown> entry : materialSummary.categories.entrySet()) {
            CategoryBreakdown breakdown = entry.getValue();
            
            // Calculate total quantity for this material
            Map<String, Double> quantityByUnit = new HashMap<>();
            
            for (ExpenseItem item : breakdown.items) {
                // Use quantity and quantityUnit fields directly
                if (item.quantity > 0 && item.quantityUnit != null && !item.quantityUnit.isEmpty()) {
                    String unit = item.quantityUnit;
                    if (!quantityByUnit.containsKey(unit)) {
                        quantityByUnit.put(unit, 0.0);
                    }
                    quantityByUnit.put(unit, quantityByUnit.get(unit) + item.quantity);
                }
            }
            
            // Create material card
            androidx.cardview.widget.CardView materialCard = new androidx.cardview.widget.CardView(this);
            androidx.cardview.widget.CardView.LayoutParams cardParams = new androidx.cardview.widget.CardView.LayoutParams(
                androidx.cardview.widget.CardView.LayoutParams.MATCH_PARENT,
                androidx.cardview.widget.CardView.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 12);
            materialCard.setLayoutParams(cardParams);
            materialCard.setRadius(8f);
            materialCard.setCardElevation(2f);
            materialCard.setCardBackgroundColor(0xFFF5F5F5);
            materialCard.setUseCompatPadding(true);
            
            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(16, 16, 16, 16);
            materialCard.addView(cardContent);
            
            // Material name
            TextView materialName = new TextView(this);
            materialName.setText(breakdown.categoryName);
            materialName.setTextSize(16f);
            materialName.setTextColor(0xFF333333);
            materialName.setTypeface(null, android.graphics.Typeface.BOLD);
            materialName.setPadding(0, 0, 0, 8);
            cardContent.addView(materialName);
            
            // Quantity info
            if (!quantityByUnit.isEmpty()) {
                TextView quantityText = new TextView(this);
                // Show quantities grouped by unit if multiple units exist
                StringBuilder qtyText = new StringBuilder("Quantity: ");
                boolean first = true;
                for (Map.Entry<String, Double> qtyEntry : quantityByUnit.entrySet()) {
                    if (!first) qtyText.append(", ");
                    qtyText.append(String.format(Locale.getDefault(), "%.2f %s", qtyEntry.getValue(), qtyEntry.getKey()));
                    first = false;
                }
                quantityText.setText(qtyText.toString());
                quantityText.setTextSize(13f);
                quantityText.setTextColor(0xFF666666);
                quantityText.setPadding(0, 0, 0, 4);
                cardContent.addView(quantityText);
            }
            
            // Material cost
            TextView costText = new TextView(this);
            costText.setText("Total Cost: ₱" + df.format(breakdown.totalCost));
            costText.setTextSize(13f);
            costText.setTextColor(0xFF666666);
            cardContent.addView(costText);
            
            materialBreakdownContainer.addView(materialCard);
        }
        
        // Update total cost
        if (materialSummary.itemCount > 0) {
            String costRange;
            if (materialSummary.minCost == materialSummary.maxCost) {
                costRange = "₱" + df.format(materialSummary.totalCost);
            } else {
                // Calculate total range: min total (min cost × item count) to max total (max cost × item count)
                double minTotal = materialSummary.minCost * materialSummary.itemCount;
                double maxTotal = materialSummary.maxCost * materialSummary.itemCount;
                costRange = String.format(Locale.getDefault(), 
                    "₱%.0f - ₱%.0f (avg: ₱%.0f)", 
                    minTotal, maxTotal, materialSummary.totalCost);
            }
            tvMaterialTotalCost.setText(costRange);
        } else {
            tvMaterialTotalCost.setText("₱0.00");
        }
    }
    
    /**
     * Updates the Miscellaneous card with categorized expense data
     */
    private void updateMiscellaneousCard() {
        if (miscellaneousBreakdownContainer == null || tvMiscellaneousTotalCost == null) {
            return;
        }
        
        // Clear existing breakdown
        miscellaneousBreakdownContainer.removeAllViews();
        
        if (miscellaneousSummary == null || miscellaneousSummary.categories.isEmpty()) {
            // Show "No data" message
            TextView noDataText = new TextView(this);
            noDataText.setText("No miscellaneous expenses recorded yet");
            noDataText.setTextSize(14f);
            noDataText.setTextColor(0xFF999999);
            noDataText.setPadding(0, 8, 0, 8);
            miscellaneousBreakdownContainer.addView(noDataText);
            
            tvMiscellaneousTotalCost.setText("₱0.00");
            return;
        }
        
        // Display categorized breakdown by expense name
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        for (Map.Entry<String, CategoryBreakdown> entry : miscellaneousSummary.categories.entrySet()) {
            CategoryBreakdown breakdown = entry.getValue();
            
            // Create expense card
            androidx.cardview.widget.CardView expenseCard = new androidx.cardview.widget.CardView(this);
            androidx.cardview.widget.CardView.LayoutParams cardParams = new androidx.cardview.widget.CardView.LayoutParams(
                androidx.cardview.widget.CardView.LayoutParams.MATCH_PARENT,
                androidx.cardview.widget.CardView.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 12);
            expenseCard.setLayoutParams(cardParams);
            expenseCard.setRadius(8f);
            expenseCard.setCardElevation(2f);
            expenseCard.setCardBackgroundColor(0xFFF5F5F5);
            expenseCard.setUseCompatPadding(true);
            
            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(16, 16, 16, 16);
            expenseCard.addView(cardContent);
            
            // Expense name
            TextView expenseName = new TextView(this);
            expenseName.setText(breakdown.categoryName);
            expenseName.setTextSize(16f);
            expenseName.setTextColor(0xFF333333);
            expenseName.setTypeface(null, android.graphics.Typeface.BOLD);
            expenseName.setPadding(0, 0, 0, 8);
            cardContent.addView(expenseName);
            
            // Expense cost
            TextView costText = new TextView(this);
            costText.setText("Total Cost: ₱" + df.format(breakdown.totalCost));
            costText.setTextSize(13f);
            costText.setTextColor(0xFF666666);
            cardContent.addView(costText);
            
            miscellaneousBreakdownContainer.addView(expenseCard);
        }
        
        // Update total cost
        if (miscellaneousSummary.itemCount > 0) {
            String costRange;
            if (miscellaneousSummary.minCost == miscellaneousSummary.maxCost) {
                costRange = "₱" + df.format(miscellaneousSummary.totalCost);
            } else {
                // Calculate total range: min total (min cost × item count) to max total (max cost × item count)
                double minTotal = miscellaneousSummary.minCost * miscellaneousSummary.itemCount;
                double maxTotal = miscellaneousSummary.maxCost * miscellaneousSummary.itemCount;
                costRange = String.format(Locale.getDefault(), 
                    "₱%.0f - ₱%.0f (avg: ₱%.0f)", 
                    minTotal, maxTotal, miscellaneousSummary.totalCost);
            }
            tvMiscellaneousTotalCost.setText(costRange);
        } else {
            tvMiscellaneousTotalCost.setText("₱0.00");
        }
    }
    
    /**
     * Updates the cost breakdown section with expense ranges
     */
    private void updateCostBreakdown() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        // Update Labor Total Cost
        if (tvCostBreakdownLabor != null) {
            if (laborSummary != null && laborSummary.itemCount > 0) {
                String costRange = formatCostRange(laborSummary, df);
                tvCostBreakdownLabor.setText(costRange);
            } else {
                tvCostBreakdownLabor.setText("₱0.00");
            }
        }
        
        // Update Equipment/Tools Total Cost
        if (tvCostBreakdownEquipment != null) {
            if (equipmentSummary != null && equipmentSummary.itemCount > 0) {
                String costRange = formatCostRange(equipmentSummary, df);
                tvCostBreakdownEquipment.setText(costRange);
            } else {
                tvCostBreakdownEquipment.setText("₱0.00");
            }
        }
        
        // Update Material Total Cost
        if (tvCostBreakdownMaterial != null) {
            if (materialSummary != null && materialSummary.itemCount > 0) {
                String costRange = formatCostRange(materialSummary, df);
                tvCostBreakdownMaterial.setText(costRange);
            } else {
                tvCostBreakdownMaterial.setText("₱0.00");
            }
        }
        
        // Update Miscellaneous Total Cost
        if (tvCostBreakdownMiscellaneous != null) {
            if (miscellaneousSummary != null && miscellaneousSummary.itemCount > 0) {
                String costRange = formatCostRange(miscellaneousSummary, df);
                tvCostBreakdownMiscellaneous.setText(costRange);
            } else {
                tvCostBreakdownMiscellaneous.setText("₱0.00");
            }
        }
        
        // Update Total Expenses with range
        updateTotalExpensesWithRange();
    }
    
    /**
     * Formats cost range for display (min - max with average)
     */
    private String formatCostRange(ExpenseSummary summary, DecimalFormat df) {
        if (summary.itemCount == 0) {
            return "₱0.00";
        }
        
        if (summary.minCost == summary.maxCost || summary.itemCount == 1) {
            return "₱" + df.format(summary.totalCost);
        }
        
        // Calculate range: min total to max total
        double minTotal = summary.minCost * summary.itemCount;
        double maxTotal = summary.maxCost * summary.itemCount;
        return String.format(Locale.getDefault(), 
            "₱%.0f - ₱%.0f (avg: ₱%.0f)", 
            minTotal, maxTotal, summary.totalCost);
    }
    
    /**
     * Updates total expenses to show range based on all expense categories
     */
    private void updateTotalExpensesWithRange() {
        if (tvTotalExpenses == null) return;
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormat df2 = new DecimalFormat("#,##0.00");
        
        double totalMin = 0.0;
        double totalMax = 0.0;
        double totalAvg = 0.0;
        boolean hasData = false;
        
        // Calculate ranges from all expense summaries
        if (laborSummary != null && laborSummary.itemCount > 0) {
            totalMin += laborSummary.minCost * laborSummary.itemCount;
            totalMax += laborSummary.maxCost * laborSummary.itemCount;
            totalAvg += laborSummary.totalCost;
            hasData = true;
        }
        
        if (equipmentSummary != null && equipmentSummary.itemCount > 0) {
            totalMin += equipmentSummary.minCost * equipmentSummary.itemCount;
            totalMax += equipmentSummary.maxCost * equipmentSummary.itemCount;
            totalAvg += equipmentSummary.totalCost;
            hasData = true;
        }
        
        if (materialSummary != null && materialSummary.itemCount > 0) {
            totalMin += materialSummary.minCost * materialSummary.itemCount;
            totalMax += materialSummary.maxCost * materialSummary.itemCount;
            totalAvg += materialSummary.totalCost;
            hasData = true;
        }
        
        if (miscellaneousSummary != null && miscellaneousSummary.itemCount > 0) {
            totalMin += miscellaneousSummary.minCost * miscellaneousSummary.itemCount;
            totalMax += miscellaneousSummary.maxCost * miscellaneousSummary.itemCount;
            totalAvg += miscellaneousSummary.totalCost;
            hasData = true;
        }
        
        // Update total expenses display
        if (hasData) {
            String totalRange;
            if (totalMin == totalMax) {
                totalRange = "Total Expenses (₱): " + df2.format(totalAvg);
            } else {
                totalRange = String.format(Locale.getDefault(), 
                    "Total Expenses (₱): %.0f - %.0f (avg: %.0f)", 
                    totalMin, totalMax, totalAvg);
            }
            tvTotalExpenses.setText(totalRange);
            
            // Update totalExpenses variable for calculations
            totalExpenses = totalAvg;
        } else {
            tvTotalExpenses.setText("Total Expenses (₱): ₱0.00");
            totalExpenses = 0;
        }
        
        // Recalculate net income
        netIncome = grossIncome - totalExpenses;
        
        // Update net income display
        if (tvNetIncomeCard != null) {
            tvNetIncomeCard.setText("₱" + df2.format(netIncome));
        }
        
        // Apply adjusted projection if needed
        applyAdjustedProjection();
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
        tvAdjustedExpenses.setText("Adjusted Expenses (₱): " + df2.format(projection.adjustedExpenses));
        updateCompletionWarning();
    }
}
