package com.android.tomatoapp.financial.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;

import com.android.tomatoapp.R;
import com.android.tomatoapp.financial.data.CalculationModel;
import com.android.tomatoapp.financial.data.DailyExpenseMath;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.task.data.TaskSchedule;
import com.android.tomatoapp.workprogram.data.WorkProgramDataHelper;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;
import com.android.tomatoapp.core.network.LocalDataManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class Calculator extends BaseBottomNavActivity {

    private static final String TAG = "Calculator";
    
    EditText etHectare, etAWF, etAFP, etMarketValue;
    EditText etFinanceLossPct, etPhaseBudget1, etPhaseBudget2, etPhaseBudget3, etPhaseBudget4, etPhaseBudget5;
    TextView tvCostProjectionTitle, tvCostPerKg, tvBreakEvenPrice, tvBreakEvenYield, tvMargin, tvWhatIfPrices;
    TextView tvPlannedCost, tvCompletionWarning;
    LinearLayout advancedFieldsContainer;
    com.google.android.material.button.MaterialButton btnAdvancedToggle;
    TextView tvCultivarName, tvCultivarNameTag, tvDateSaved, tvNP, tvSubTotalHarvest;
    Spinner spinnerHarvestUnit;
    TextView tvHarvestPredictionYieldPerHa, tvHarvestPredictionTotalYield, tvHarvestPredictionDate;
    TextView tvNetIncomeCard, tvSummarySubtitle, tvCompletionRate, tvAdjustedNetIncome, tvAdjustedSubtitle, tvAdjustedExpenses;
    TextView tvTotalExpenses;
    com.google.android.material.button.MaterialButton btnDailyExpensesHistory;
    com.google.android.material.button.MaterialButton btnSaveToAnalytics;
    com.google.android.material.button.MaterialButton btnEditMetrics;
    // Expense category cards
    TextView tvLaborTotalCost, tvLaborTotalWorkers;
    TextView tvMaterialTotalCost;
    TextView tvEquipmentTotalCost, tvEquipmentTotalUsage;
    TextView tvMiscellaneousTotalCost;
    LinearLayout laborItemsContainer, materialItemsContainer, equipmentItemsContainer, miscItemsContainer;
    LinearLayout breakdownContainer;
    private final Set<String> expandedBreakdownKeys = new HashSet<>();

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
    private WorkProgramEntity loadedProgram;
    private double plantsPlanted = 0;
    private double financeLossPct = 0;
    private double[] phaseBudgets = new double[5];
    
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
    private List<DayLog> dayLogs = new ArrayList<>();
    
    // Helper classes for expense items
    private static class LaborItem {
        String notes;
        String activityName;
        double dailyWage;
        int numWorkers;
        double totalCost;
    }
    
    private static class MaterialItem {
        String materialName;
        double quantity;
        String quantityUnit;
        double unitCost;
        String priceUnit = "";
        double totalCost;
    }
    
    private static class EquipmentItem {
        String equipmentName;
        double usageValue;
        String usageUnit;
        boolean isOwned;
        double cost;
        double totalCost;
    }
    
    private static class MiscItem {
        String expenseName;
        double cost;
    }

    private static class DayLog {
        String dateKey;
        int dayNumber;
        boolean catchUp;
        int phase;
        List<LaborItem> laborItems = new ArrayList<>();
        List<MaterialItem> materialItems = new ArrayList<>();
        List<EquipmentItem> equipmentItems = new ArrayList<>();
        List<MiscItem> miscItems = new ArrayList<>();
        double dayTotal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        setupBottomNavigation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Projected Income/Expenses");
        }

        // Bind UI elements
        etHectare = findViewById(R.id.etHectare);
        etAWF = findViewById(R.id.etAWF);
        etAFP = findViewById(R.id.etAFP);
        etMarketValue = findViewById(R.id.etMarketValue);
        etFinanceLossPct = findViewById(R.id.etFinanceLossPct);
        etPhaseBudget1 = findViewById(R.id.etPhaseBudget1);
        etPhaseBudget2 = findViewById(R.id.etPhaseBudget2);
        etPhaseBudget3 = findViewById(R.id.etPhaseBudget3);
        etPhaseBudget4 = findViewById(R.id.etPhaseBudget4);
        etPhaseBudget5 = findViewById(R.id.etPhaseBudget5);
        tvCostProjectionTitle = findViewById(R.id.tvCostProjectionTitle);
        tvPlannedCost = findViewById(R.id.tvPlannedCost);
        tvCostPerKg = findViewById(R.id.tvCostPerKg);
        tvBreakEvenPrice = findViewById(R.id.tvBreakEvenPrice);
        tvBreakEvenYield = findViewById(R.id.tvBreakEvenYield);
        tvMargin = findViewById(R.id.tvMargin);
        tvWhatIfPrices = findViewById(R.id.tvWhatIfPrices);
        advancedFieldsContainer = findViewById(R.id.advancedFieldsContainer);
        btnAdvancedToggle = findViewById(R.id.btnAdvancedToggle);
        
        tvCultivarName = findViewById(R.id.tvCultivarName);
        tvCultivarNameTag = findViewById(R.id.tvCultivarNameTag);
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
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        
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
        breakdownContainer = findViewById(R.id.breakdownContainer);

        // Get values from intent
        cultivarName = getIntent().getStringExtra("cultivar_name");
        dateSaved = getIntent().getStringExtra("date_saved");
        baseNP = getIntent().getDoubleExtra("NP_VALUE", 0); // base NP per hectare
        growthHabit = getIntent().getStringExtra("growth_habit"); // Growth habit
        double prefilledHectare = getIntent().getDoubleExtra("hectare_prefilled", 0);
        programId = getIntent().getStringExtra("program_id");

        // Display cultivar info
        tvCultivarName.setText(String.format(Locale.getDefault(), "Cultivar: %s", (cultivarName != null ? cultivarName : "N/A")));
        if (tvCultivarNameTag != null) {
            String tagText = cultivarName != null ? cultivarName : "N/A";
            if (dateSaved != null) tagText += " · " + dateSaved;
            tvCultivarNameTag.setText(tagText);
        }
        tvDateSaved.setText(String.format(Locale.getDefault(), "Date Saved: %s", (dateSaved != null ? dateSaved : "N/A")));
        tvNP.setText(String.format(Locale.getDefault(), "Number of Plants Per Hectare (NP): %s", df.format(baseNP)));
        
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
        if (etFinanceLossPct != null) etFinanceLossPct.addTextChangedListener(watcher);
        EditText[] budgets = {etPhaseBudget1, etPhaseBudget2, etPhaseBudget3, etPhaseBudget4, etPhaseBudget5};
        for (EditText budget : budgets) if (budget != null) budget.addTextChangedListener(watcher);
        if (btnAdvancedToggle != null) btnAdvancedToggle.setOnClickListener(v -> {
            if (advancedFieldsContainer == null) return;
            boolean show = advancedFieldsContainer.getVisibility() != View.VISIBLE;
            advancedFieldsContainer.setVisibility(show ? View.VISIBLE : View.GONE);
            btnAdvancedToggle.setText(show ? "Advanced ▲" : "Advanced ▼");
        });
        if (prefilledHectare > 0) compute();
        
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
        renderBreakdown();
        
        // Set default net income to zero
        if (tvNetIncomeCard != null) {
            tvNetIncomeCard.setText("₱0.00");
        }
        
        // Setup harvest unit spinner
        setupHarvestUnitSpinner();
        
        // Setup "Save to Analytics" button
        setupSaveToAnalyticsButton();

        if (programId != null) {
            reloadProgramInputs();
        }

        // Show the estimated harvest date (same timeline as the work program)
        loadEstimatedHarvestDate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        com.google.firebase.auth.FirebaseUser u =
                com.google.firebase.auth.FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (u != null) {
            com.android.tomatoapp.core.network.LocalDataManager
                    .getInstance(this)
                    .processSyncQueue(this, u.getUid());
        }
        reloadProgramInputs();
    }

    private void reloadProgramInputs() {
        if (programId == null || currentUser == null) return;
        LocalDataManager manager = LocalDataManager.getInstance(this);
        List<WorkProgramEntity> programs = manager.getWorkProgramsFromLocal(currentUser.getUid());
        WorkProgramEntity found = null;
        for (WorkProgramEntity entity : programs) {
            if (programId.equals(entity.id)) { found = entity; break; }
        }
        if (found != null) applyLoadedProgram(found);
        else fallbackLoadProgramFromFirebase();
    }

    private void applyLoadedProgram(WorkProgramEntity entity) {
        loadedProgram = entity;
        plantsPlanted = entity.plantCount;
        etHectare.setEnabled(false);
        etHectare.setText(plantsPlanted > 0 ? df.format(plantsPlanted) : "0");
        tvNP.setText(String.format(Locale.getDefault(), "Plants planted: %s",
                plantsPlanted > 0 ? df.format(plantsPlanted) : "not recorded"));
        if (entity.awf > 0) etAWF.setText(plainNumber(entity.awf));
        if (entity.afp > 0) etAFP.setText(plainNumber(entity.afp));
        if (entity.marketValue > 0) etMarketValue.setText(plainNumber(entity.marketValue));
        if (entity.financeLossPct >= 0 && etFinanceLossPct != null) {
            etFinanceLossPct.setText(plainNumber(entity.financeLossPct));
        }
        setBudgetFromEntity(etPhaseBudget1, entity.phaseBudget1);
        setBudgetFromEntity(etPhaseBudget2, entity.phaseBudget2);
        setBudgetFromEntity(etPhaseBudget3, entity.phaseBudget3);
        setBudgetFromEntity(etPhaseBudget4, entity.phaseBudget4);
        setBudgetFromEntity(etPhaseBudget5, entity.phaseBudget5);
        completionStats = new WorkProgramDataHelper.CompletionStats();
        completionStats.completionRate = entity.completionRate;
        completionStats.totalTasks = entity.totalTasks;
        completionStats.completedTasks = entity.completedTasks;
        completionStats.missedTasks = entity.missedTasks;
        completionStats.skippedTasks = entity.skippedTasks;
        completionStats.phaseTotals[0] = entity.totalTasks;
        completionStats.phaseCompleted[0] = entity.completedTasks;
        applyRoomCompletionWarning(entity);
        showEstimatedHarvestDate(entity.sowingDate, entity.transplantDate);
        compute();
    }

    private void setBudgetFromEntity(EditText field, double value) {
        if (field != null && value > 0) field.setText(plainNumber(value));
    }

    private String plainNumber(double value) {
        return String.format(Locale.US, "%.2f", value).replaceAll("\\.?0+$", "");
    }

    private void applyRoomCompletionWarning(WorkProgramEntity entity) {
        if (tvCompletionWarning == null) return;
        if (entity == null) {
            tvCompletionWarning.setVisibility(View.GONE);
            return;
        }
        double[] rates = {entity.phase1Completion, entity.phase2Completion, entity.phase3Completion,
                entity.phase4Completion, entity.phase5Completion};
        int weakest = -1;
        for (int i = 0; i < rates.length; i++) {
            if (rates[i] > 0 && rates[i] < 100 && (weakest < 0 || rates[i] < rates[weakest])) {
                weakest = i;
            }
        }
        if (weakest >= 0) {
            tvCompletionWarning.setText(String.format(Locale.getDefault(),
                    "Phase %d completion is %.0f%%, yields may be lower",
                    weakest + 1, rates[weakest]));
            tvCompletionWarning.setVisibility(View.VISIBLE);
            return;
        }
        if (entity.completionRate > 0 && entity.completionRate < 100) {
            tvCompletionWarning.setText(String.format(Locale.getDefault(),
                    "Completion is %.0f%%, yields may be lower", entity.completionRate));
            tvCompletionWarning.setVisibility(View.VISIBLE);
            return;
        }
        tvCompletionWarning.setVisibility(View.GONE);
    }

    private void fallbackLoadProgramFromFirebase() {
        FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("workPrograms").child(programId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        WorkProgramEntity entity = new WorkProgramEntity();
                        entity.id = programId;
                        entity.plantCount = DailyExpenseMath.asInt(snapshot.child("plantCount").getValue());
                        if (entity.plantCount == 0) {
                            entity.plantCount = DailyExpenseMath.asInt(
                                    snapshot.child("intake").child("plantCount").getValue());
                        }
                        entity.areaSize = DailyExpenseMath.asDouble(snapshot.child("areaSize").getValue());
                        entity.startingDate = DailyExpenseMath.asString(snapshot.child("startingDate").getValue());
                        entity.sowingDate = DailyExpenseMath.asString(snapshot.child("sowingDate").getValue());
                        entity.transplantDate = DailyExpenseMath.asString(snapshot.child("transplantDate").getValue());
                        entity.stopDate = DailyExpenseMath.asString(snapshot.child("stopDate").getValue());
                        entity.phase3Date = DailyExpenseMath.asString(snapshot.child("phase3Date").getValue());
                        entity.phase4Date = DailyExpenseMath.asString(snapshot.child("phase4Date").getValue());
                        entity.phase5Date = DailyExpenseMath.asString(snapshot.child("phase5Date").getValue());
                        entity.awf = DailyExpenseMath.asDouble(snapshot.child("farmMetrics").child("awf").getValue());
                        entity.afp = DailyExpenseMath.asDouble(snapshot.child("farmMetrics").child("afp").getValue());
                        entity.marketValue = DailyExpenseMath.asDouble(
                                snapshot.child("farmMetrics").child("marketValue").getValue());
                        entity.financeLossPct = DailyExpenseMath.asDouble(snapshot.child("financeLossPct").getValue());
                        entity.phaseBudget1 = DailyExpenseMath.asDouble(snapshot.child("phaseBudget1").getValue());
                        entity.phaseBudget2 = DailyExpenseMath.asDouble(snapshot.child("phaseBudget2").getValue());
                        entity.phaseBudget3 = DailyExpenseMath.asDouble(snapshot.child("phaseBudget3").getValue());
                        entity.phaseBudget4 = DailyExpenseMath.asDouble(snapshot.child("phaseBudget4").getValue());
                        entity.phaseBudget5 = DailyExpenseMath.asDouble(snapshot.child("phaseBudget5").getValue());
                        entity.completionRate = DailyExpenseMath.asDouble(snapshot.child("completionRate").getValue());
                        entity.phase1Completion = DailyExpenseMath.asDouble(snapshot.child("phase1Completion").getValue());
                        entity.phase2Completion = DailyExpenseMath.asDouble(snapshot.child("phase2Completion").getValue());
                        entity.phase3Completion = DailyExpenseMath.asDouble(snapshot.child("phase3Completion").getValue());
                        entity.phase4Completion = DailyExpenseMath.asDouble(snapshot.child("phase4Completion").getValue());
                        entity.phase5Completion = DailyExpenseMath.asDouble(snapshot.child("phase5Completion").getValue());
                        applyLoadedProgram(entity);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setMetricsLocked(boolean locked) {
        etHectare.setEnabled(false);
        etAWF.setEnabled(!locked);
        etAFP.setEnabled(!locked);
        etMarketValue.setEnabled(!locked);
        if (btnEditMetrics != null) btnEditMetrics.setEnabled(locked);
        if (btnSaveToAnalytics != null) btnSaveToAnalytics.setEnabled(!locked);
    }

    private void loadFarmMetrics() {
        if (programId == null || currentUser == null) return;

        DatabaseReference farmMetricsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("workPrograms")
                .child(programId)
                .child("farmMetrics");

        farmMetricsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                Double awf = snapshot.child("awf").getValue(Double.class);
                Double afp = snapshot.child("afp").getValue(Double.class);
                Double market = snapshot.child("marketValue").getValue(Double.class);
                if (awf != null && awf > 0) etAWF.setText(df2.format(awf));
                if (afp != null && afp > 0) etAFP.setText(df2.format(afp));
                if (market != null && market > 0) etMarketValue.setText(df2.format(market));
                setMetricsLocked(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    
    /**
     * Sets up the "Save to Analytics" button
     */
    private void setupSaveToAnalyticsButton() {
        btnSaveToAnalytics = findViewById(R.id.btnSaveToAnalytics);
        btnEditMetrics = findViewById(R.id.btnEditMetrics);
        if (btnSaveToAnalytics == null) return;
        
        // Only show button if we have a program ID (linked to a work program)
        if (programId == null || cultivarName == null || dateSaved == null) {
            btnSaveToAnalytics.setVisibility(View.GONE);
            if (btnEditMetrics != null) btnEditMetrics.setVisibility(View.GONE);
            return;
        }
        
        btnSaveToAnalytics.setVisibility(View.VISIBLE);
        if (btnEditMetrics != null) {
            btnEditMetrics.setVisibility(View.VISIBLE);
            btnEditMetrics.setOnClickListener(v -> setMetricsLocked(false));
        }
        setMetricsLocked(false);
        btnSaveToAnalytics.setOnClickListener(v -> {
            // Validate that calculation is complete
            if (plantsPlanted <= 0 || grossIncome <= 0) {
                showSingleToast("Please complete the calculation before saving to analytics", Toast.LENGTH_SHORT);
                return;
            }
            
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Save to Analytics")
                    .setMessage("This will save the current calculation to Analytics and Season Comparison. Continue?")
                    .setPositiveButton("Save", (dialog, which) -> saveToAnalytics())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
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

        double displayValue;

        if (selectedUnit.equals("kg")) {
            displayValue = totalHarvestKg;
        } else {
            displayValue = totalHarvestGrams;
        }

        tvSubTotalHarvest.setText(
                String.format(
                        Locale.getDefault(),
                        "%s %s",
                        df2.format(displayValue),
                        selectedUnit
                )
        );
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
                showSingleToast("Daily expenses history is only available for saved work programs", Toast.LENGTH_SHORT);
                return;
            }
            
            if (cultivarName == null || dateSaved == null) {
                showSingleToast("Missing required information", Toast.LENGTH_SHORT);
                return;
            }
            
            try {
                Intent intent = new Intent(Calculator.this, CurrentExpensesActivity.class);
                intent.putExtra("programId", programId);
                intent.putExtra("cultivar", cultivarName);
                intent.putExtra("startDate", dateSaved);
                startActivity(intent);
            } catch (Exception e) {
                showSingleToast("Unable to open daily expenses history", Toast.LENGTH_SHORT);
                Log.e(TAG, "Error opening daily expenses history", e);
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
                dayLogs.clear();
                
                // Iterate through all dates with expenses
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    double dailyTotal = 0;
                    String dateKey = dateSnapshot.getKey() == null ? "" : dateSnapshot.getKey();
                    DayLog dayLog = new DayLog();
                    dayLog.dateKey = dateKey;
                    dayLog.catchUp = "CATCH_UP".equals(DailyExpenseMath.asString(dateSnapshot.child("entryType").getValue()));
                    dayLog.phase = DailyExpenseMath.asInt(dateSnapshot.child("phase").getValue());
                    try {
                        dayLog.dayNumber = (int) ChronoUnit.DAYS.between(
                                LocalDate.parse(dateSaved),
                                LocalDate.parse(dateKey.substring(0, 10))) + 1;
                    } catch (Exception e) {
                        dayLog.dayNumber = 0;
                    }
                    
                    // Process labor expenses
                    if (dateSnapshot.hasChild("labor")) {
                        for (DataSnapshot laborSnapshot : dateSnapshot.child("labor").getChildren()) {
                            double totalCost = DailyExpenseMath.asDouble(laborSnapshot.child("totalCost").getValue());
                            int numWorkers = DailyExpenseMath.asInt(laborSnapshot.child("numWorkers").getValue());
                            double dailyWage = DailyExpenseMath.asDouble(laborSnapshot.child("dailyWage").getValue());
                            String notes = DailyExpenseMath.asString(laborSnapshot.child("notes").getValue());
                            
                            if (totalCost > 0) {
                                aggregatedLaborCost += totalCost;
                                dailyTotal += totalCost;
                                
                                LaborItem item = new LaborItem();
                                item.activityName = !notes.isEmpty() ? notes : "Labor";
                                item.notes = notes;
                                item.dailyWage = dailyWage;
                                item.numWorkers = numWorkers;
                                item.totalCost = totalCost;
                                laborItems.add(item);
                                dayLog.laborItems.add(item);
                            }
                            
                            if (numWorkers > 0) {
                                aggregatedLaborWorkerCount += numWorkers;
                            }
                        }
                    }
                    
                    // Process material expenses
                    if (dateSnapshot.hasChild("material")) {
                        for (DataSnapshot materialSnapshot : dateSnapshot.child("material").getChildren()) {
                            double totalCost = DailyExpenseMath.asDouble(materialSnapshot.child("totalCost").getValue());
                            String materialName = DailyExpenseMath.asString(materialSnapshot.child("materialName").getValue());
                            double quantity = DailyExpenseMath.asDouble(materialSnapshot.child("quantity").getValue());
                            String quantityUnit = DailyExpenseMath.asString(materialSnapshot.child("quantityUnit").getValue());
                            double unitCost = DailyExpenseMath.asDouble(materialSnapshot.child("unitCost").getValue());
                            String priceUnit = DailyExpenseMath.asString(materialSnapshot.child("priceUnit").getValue());
                            
                            if (totalCost > 0) {
                                aggregatedMaterialCost += totalCost;
                                dailyTotal += totalCost;
                                
                                MaterialItem item = new MaterialItem();
                                item.materialName = !materialName.isEmpty() ? materialName : "Material";
                                item.quantity = quantity;
                                item.quantityUnit = quantityUnit;
                                item.unitCost = unitCost;
                                item.priceUnit = priceUnit;
                                item.totalCost = totalCost;
                                materialItems.add(item);
                                dayLog.materialItems.add(item);
                            }
                        }
                    }
                    
                    // Process equipment expenses (only non-owned equipment)
                    if (dateSnapshot.hasChild("equipment")) {
                        for (DataSnapshot equipmentSnapshot : dateSnapshot.child("equipment").getChildren()) {
                            boolean isOwned = DailyExpenseMath.asBoolean(equipmentSnapshot.child("isOwned").getValue(), false);
                            double totalCost = DailyExpenseMath.asDouble(equipmentSnapshot.child("totalCost").getValue());
                            if (totalCost <= 0) {
                                totalCost = DailyExpenseMath.asDouble(equipmentSnapshot.child("cost").getValue());
                            }
                            String equipmentName = DailyExpenseMath.asString(equipmentSnapshot.child("equipmentName").getValue());
                            double usageValue = DailyExpenseMath.asDouble(equipmentSnapshot.child("usageValue").getValue());
                            String usageUnit = DailyExpenseMath.asString(equipmentSnapshot.child("usageUnit").getValue());
                            double cost = DailyExpenseMath.asDouble(equipmentSnapshot.child("cost").getValue());
                            EquipmentItem dayItem = new EquipmentItem();
                            dayItem.equipmentName = !equipmentName.isEmpty() ? equipmentName : "Equipment";
                            dayItem.usageValue = usageValue;
                            dayItem.usageUnit = usageUnit.isEmpty() ? "hours" : usageUnit;
                            dayItem.isOwned = isOwned;
                            dayItem.cost = cost;
                            dayItem.totalCost = isOwned ? 0 : totalCost;
                            dayLog.equipmentItems.add(dayItem);
                            
                            if (usageValue > 0) {
                                double hours = "hours".equals(usageUnit) || usageUnit.isEmpty() ? usageValue : (usageValue / 60.0);
                                aggregatedEquipmentUsageHours += hours;
                            }
                            
                            if (!isOwned) {
                                if (totalCost > 0) {
                                    aggregatedEquipmentCost += totalCost;
                                    dailyTotal += totalCost;
                                    
                                    // Store individual item
                                    EquipmentItem item = new EquipmentItem();
                                    item.equipmentName = !equipmentName.isEmpty() ? equipmentName : "Equipment";
                                    item.usageValue = usageValue;
                                    item.usageUnit = usageUnit.isEmpty() ? "hours" : usageUnit;
                                    item.isOwned = false;
                                    item.cost = cost;
                                    item.totalCost = totalCost;
                                    equipmentItems.add(item);
                                }
                            }
                        }
                    }
                    
                    // Process miscellaneous expenses
                    if (dateSnapshot.hasChild("miscellaneous")) {
                        for (DataSnapshot miscSnapshot : dateSnapshot.child("miscellaneous").getChildren()) {
                            double cost = DailyExpenseMath.asDouble(miscSnapshot.child("cost").getValue());
                            String expenseName = DailyExpenseMath.asString(miscSnapshot.child("expenseName").getValue());
                            
                            if (cost > 0) {
                                aggregatedMiscCost += cost;
                                dailyTotal += cost;
                                
                                MiscItem item = new MiscItem();
                                item.expenseName = !expenseName.isEmpty() ? expenseName : "Miscellaneous";
                                item.cost = cost;
                                miscItems.add(item);
                                dayLog.miscItems.add(item);
                            }
                        }
                    }
                    
                    // Store daily total for min/max/avg calculation
                    if (dailyTotal > 0) {
                        dailyExpenseTotals.add(dailyTotal);
                    }
                    dayLog.dayTotal = dailyTotal;
                    dayLogs.add(dayLog);
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
                
                renderBreakdown();
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
    
    private void renderBreakdown() {
        if (breakdownContainer == null) return;
        breakdownContainer.removeAllViews();
        if (dayLogs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No daily expenses logged yet");
            empty.setTextSize(13);
            empty.setTextColor(getResources().getColor(android.R.color.darker_gray));
            breakdownContainer.addView(empty);
            return;
        }

        Collections.sort(dayLogs, (left, right) -> left.dateKey.compareTo(right.dateKey));
        int logNumber = 1;
        for (DayLog log : dayLogs) {
            MaterialCardView card = new MaterialCardView(this);
            card.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            card.setRadius(dp(16));
            card.setCardElevation(dp(2));
            card.setCardBackgroundColor(getResources().getColor(android.R.color.white));

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            LinearLayout header = new LinearLayout(this);
            header.setGravity(android.view.Gravity.CENTER_VERTICAL);
            header.setPadding(dp(14), dp(14), dp(14), dp(14));

            TextView headerText = new TextView(this);
            headerText.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            String dateText = formatBreakdownDate(log.dateKey);
            String label = log.catchUp
                    ? String.format(Locale.getDefault(), "Before joining – Phase %d", log.phase)
                    : String.format(Locale.getDefault(), "Day %d", log.dayNumber);
            headerText.setText(String.format(Locale.getDefault(), "Log %d · %s · %s · ₱%,.2f",
                    logNumber, label, dateText, log.dayTotal));
            headerText.setTextSize(13);
            headerText.setTypeface(null, android.graphics.Typeface.BOLD);
            header.addView(headerText);

            TextView indicator = new TextView(this);
            indicator.setText(expandedBreakdownKeys.contains(log.dateKey) ? "▼" : "▶");
            indicator.setTextSize(13);
            header.addView(indicator);

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setPadding(dp(14), 0, dp(14), dp(14));
            body.setVisibility(expandedBreakdownKeys.contains(log.dateKey) ? View.VISIBLE : View.GONE);
            addBreakdownSections(body, log);
            header.setOnClickListener(v -> {
                boolean expanded = body.getVisibility() == View.VISIBLE;
                body.setVisibility(expanded ? View.GONE : View.VISIBLE);
                indicator.setText(expanded ? "▶" : "▼");
                if (expanded) expandedBreakdownKeys.remove(log.dateKey);
                else expandedBreakdownKeys.add(log.dateKey);
            });
            content.addView(header);
            content.addView(body);
            card.addView(content);
            LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) card.getLayoutParams();
            cardParams.bottomMargin = dp(9);
            card.setLayoutParams(cardParams);
            breakdownContainer.addView(card);
            logNumber++;
        }
    }

    private void addBreakdownSections(LinearLayout body, DayLog log) {
        if (!log.laborItems.isEmpty()) {
            TableLayout table = createBreakdownTable(body, "Labor");
            addBreakdownRow(table, true, "Labor item #", "No. of workers", "Activity", "Daily wage (₱)", "Sub-total");
            double subtotal = 0;
            int index = 1;
            for (LaborItem item : log.laborItems) {
                addBreakdownRow(table, false, String.valueOf(index++), String.valueOf(item.numWorkers),
                        item.notes.isEmpty() ? item.activityName : item.notes, money(item.dailyWage), money(item.totalCost));
                subtotal += item.totalCost;
            }
            addBreakdownRow(table, false, "Sub-total", "", "", "", money(subtotal));
        }
        if (!log.materialItems.isEmpty()) {
            TableLayout table = createBreakdownTable(body, "Material");
            addBreakdownRow(table, true, "Material item #", "Material", "Quantity", "Price", "Sub-total");
            double subtotal = 0;
            int index = 1;
            for (MaterialItem item : log.materialItems) {
                addBreakdownRow(table, false, String.valueOf(index++), item.materialName,
                        df2.format(item.quantity) + " " + item.quantityUnit, materialPriceLabel(item), money(item.totalCost));
                subtotal += item.totalCost;
            }
            addBreakdownRow(table, false, "Sub-total", "", "", "", money(subtotal));
        }
        if (!log.equipmentItems.isEmpty()) {
            TableLayout table = createBreakdownTable(body, "Equipment");
            addBreakdownRow(table, true, "Equipment item #", "Equipment", "Usage", "Rate/hr (₱)", "Sub-total");
            double subtotal = 0;
            int index = 1;
            for (EquipmentItem item : log.equipmentItems) {
                addBreakdownRow(table, false, String.valueOf(index++), item.equipmentName,
                        df2.format(item.usageValue) + " " + item.usageUnit,
                        item.isOwned ? "Owned" : money(item.cost), money(item.totalCost));
                subtotal += item.totalCost;
            }
            addBreakdownRow(table, false, "Sub-total", "", "", "", money(subtotal));
        }
        if (!log.miscItems.isEmpty()) {
            TableLayout table = createBreakdownTable(body, "Miscellaneous");
            addBreakdownRow(table, true, "Misc item #", "Expense", "Cost (₱)");
            double subtotal = 0;
            int index = 1;
            for (MiscItem item : log.miscItems) {
                addBreakdownRow(table, false, String.valueOf(index++), item.expenseName, money(item.cost));
                subtotal += item.cost;
            }
            addBreakdownRow(table, false, "Sub-total", "", money(subtotal));
        }
    }

    private TableLayout createBreakdownTable(LinearLayout body, String title) {
        TextView sectionTitle = new TextView(this);
        sectionTitle.setText(title);
        sectionTitle.setTextSize(12);
        sectionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        sectionTitle.setPadding(0, dp(10), 0, dp(4));
        body.addView(sectionTitle);
        TableLayout table = new TableLayout(this);
        table.setStretchAllColumns(true);
        body.addView(table);
        return table;
    }

    private void addBreakdownRow(TableLayout table, boolean bold, String... values) {
        TableRow row = new TableRow(this);
        for (String value : values) {
            TextView cell = new TextView(this);
            cell.setText(value);
            cell.setTextSize(11);
            cell.setPadding(dp(3), dp(4), dp(3), dp(4));
            if (bold) cell.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(cell);
        }
        table.addView(row);
    }

    private String formatBreakdownDate(String dateKey) {
        try {
            Date date = Date.from(LocalDate.parse(dateKey.substring(0, 10))
                    .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            return SettingsPreferences.getDateFormatInstance(this).format(date);
        } catch (Exception e) {
            return dateKey;
        }
    }

    private String money(double value) {
        return String.format(Locale.getDefault(), "₱%,.2f", value);
    }

    private String materialPriceLabel(MaterialItem item) {
        if (item.priceUnit == null || item.priceUnit.isEmpty()) return money(item.unitCost); // old logs
        if (item.priceUnit.equalsIgnoreCase("Total paid")) return money(item.unitCost) + " total";
        return money(item.unitCost) + "/" + item.priceUnit;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
    
    /**
     * Updates the yield part of the Harvest Prediction card.
     * The estimated harvest DATE is handled separately by loadEstimatedHarvestDate().
     */
    private void updateHarvestPrediction(double totalHarvestKg, double hectareValue) {
        if (hectareValue > 0 && totalHarvestKg > 0) {
            double yieldPerHa = totalHarvestKg / hectareValue;
            if (tvHarvestPredictionYieldPerHa != null) {
                tvHarvestPredictionYieldPerHa.setText(String.format(Locale.getDefault(), "%.2f kg/hectare", yieldPerHa));
            }
            if (tvHarvestPredictionTotalYield != null) {
                tvHarvestPredictionTotalYield.setText(String.format(Locale.getDefault(), "%.2f kg", totalHarvestKg));
            }
        } else {
            if (tvHarvestPredictionYieldPerHa != null) {
                tvHarvestPredictionYieldPerHa.setText("— kg/hectare");
            }
            if (tvHarvestPredictionTotalYield != null) {
                tvHarvestPredictionTotalYield.setText("— kg");
            }
        }
    }

    /** Reads the program's saved milestone dates, then shows the estimated harvest date. */
    private void loadEstimatedHarvestDate() {
        if (tvHarvestPredictionDate == null) return;
        if (currentUser == null || programId == null) {
            showEstimatedHarvestDate(null, null);
            return;
        }
        FirebaseDatabase.getInstance()
                .getReference("users").child(currentUser.getUid())
                .child("workPrograms").child(programId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String sowing = readProgramDate(snapshot, "sowingDate");
                        String transplant = readProgramDate(snapshot, "transplantDate");
                        showEstimatedHarvestDate(sowing, transplant);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showEstimatedHarvestDate(null, null);
                    }
                });
    }

    private String readProgramDate(DataSnapshot program, String key) {
        DataSnapshot[] places = {
                program,
                program.child("intake"),
                program.child("placement").child("answers")
        };
        for (DataSnapshot place : places) {
            Object value = place.child(key).getValue();
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    /** Same timeline the Workprogram calendar uses: transplant = sowing/start + 25 days. */
    private void showEstimatedHarvestDate(String sowing, String transplant) {
        if (tvHarvestPredictionDate == null) return;
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(dateSaved);
            java.time.LocalDate transplantDay = transplant != null
                    ? java.time.LocalDate.parse(transplant)
                    : (sowing != null ? java.time.LocalDate.parse(sowing) : start).plusDays(25);
            java.time.LocalDate harvest = transplantDay.plusDays(maturityDays);
            Date harvestDate = Date.from(harvest.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            tvHarvestPredictionDate.setText(SettingsPreferences.getDateFormatInstance(this).format(harvestDate));
        } catch (Exception e) {
            tvHarvestPredictionDate.setText("—");
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
                tvSummarySubtitle.setText(String.format(Locale.getDefault(), "%s%s", currentText, rangeText));
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
        
        workProgramRef.child("areaSize").addListenerForSingleValueEvent(
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
     * Saves calculation to Firebase (for calculation history only, not analytics)
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
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save calculation", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }
    
    /**
     * Saves calculation data to Analytics and Season Comparison
     * This is only called when user explicitly clicks "Save to Analytics" button
     */
    private void saveToAnalytics() {
        if (currentUser == null || programId == null) {
            Toast.makeText(this, "Cannot save to analytics: Missing program information", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (plantsPlanted <= 0 || grossIncome <= 0) {
            Toast.makeText(this, "Please complete the calculation before saving to analytics", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("projectedIncome", grossIncome);
        updates.put("projectedExpenses", totalExpenses);
        updates.put("farmMetrics/awf", AWF);
        updates.put("farmMetrics/afp", AFP);
        updates.put("farmMetrics/marketValue", marketValue);
        updates.put("financeLossPct", financeLossPct);
        updates.put("phaseBudget1", phaseBudgets[0]);
        updates.put("phaseBudget2", phaseBudgets[1]);
        updates.put("phaseBudget3", phaseBudgets[2]);
        updates.put("phaseBudget4", phaseBudgets[3]);
        updates.put("phaseBudget5", phaseBudgets[4]);

        if (btnSaveToAnalytics != null) btnSaveToAnalytics.setEnabled(false);

        LocalDataManager.getInstance(this)
                .updateProgramFields(currentUser.getUid(), programId, updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Successfully saved to Analytics and Season Comparison", Toast.LENGTH_SHORT).show();
                    setMetricsLocked(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save to analytics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (btnSaveToAnalytics != null) btnSaveToAnalytics.setEnabled(true);
                });
    }

    private void compute() {
        try {
            AWF = parse(etAWF);
            AFP = parse(etAFP);
            marketValue = parse(etMarketValue);
            financeLossPct = etFinanceLossPct == null ? 0 : parse(etFinanceLossPct);
            financeLossPct = Math.max(0, Math.min(100, financeLossPct));
            for (int i = 0; i < 5; i++) {
                EditText field = i == 0 ? etPhaseBudget1 : i == 1 ? etPhaseBudget2 :
                        i == 2 ? etPhaseBudget3 : i == 3 ? etPhaseBudget4 : etPhaseBudget5;
                phaseBudgets[i] = field == null ? 0 : Math.max(0, parse(field));
            }
            currentNP = plantsPlanted;
            tvNP.setText(String.format(Locale.getDefault(), "Plants planted: %s",
                    plantsPlanted > 0 ? df.format(plantsPlanted) : "not recorded"));

            if (plantsPlanted > 0 && AWF > 0 && AFP > 0) {
                totalHarvestKg = plantsPlanted * AFP * AWF / 1000.0
                        * (1 - financeLossPct / 100.0);
                totalHarvestGrams = totalHarvestKg * 1000;
                grossIncome = marketValue > 0 ? totalHarvestKg * marketValue : 0;

                // Update harvest display based on selected unit
                updateHarvestDisplay();
                
                // Update Harvest Prediction card
                updateHarvestPrediction(totalHarvestKg,
                        loadedProgram != null && loadedProgram.areaSize > 0 ? loadedProgram.areaSize : 1);
            } else {
                grossIncome = 0;
                totalHarvestGrams = 0;
                totalHarvestKg = 0;
                
                // Update harvest display
                updateHarvestDisplay();
                
                // Clear yield values (the estimated harvest date stays visible)
                if (tvHarvestPredictionYieldPerHa != null) {
                    tvHarvestPredictionYieldPerHa.setText("— kg/hectare");
                }
                if (tvHarvestPredictionTotalYield != null) {
                    tvHarvestPredictionTotalYield.setText("— kg");
                }
            }
            
            // 🔹 Compute expenses and net income
            computeExpenses();
            
            // Note: Auto-save to analytics removed - user must click "Save to Analytics" button
            
            // Ensure net income defaults to zero if no calculation
            if (grossIncome == 0 && totalExpenses == 0 && tvNetIncomeCard != null) {
                tvNetIncomeCard.setText("₱0.00");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in calculation logic", e);
        }
    }
    
    private void computeExpenses() {
        try {
            double[] actualByPhase = new double[5];
            Collections.sort(dayLogs, (left, right) -> left.dateKey.compareTo(right.dateKey));
            for (DayLog log : dayLogs) {
                int phase = log.catchUp ? log.phase : getPhaseForDate(log.dateKey);
                if (phase >= 1 && phase <= 5) actualByPhase[phase - 1] += log.dayTotal;
            }

            boolean budgetsEntered = false;
            for (double budget : phaseBudgets) if (budget > 0) budgetsEntered = true;
            int currentPhase = getCurrentPhase();
            totalExpenses = 0;
            if (budgetsEntered) {
                for (int i = 0; i < 5; i++) {
                    double cost = i + 1 < currentPhase
                            ? actualByPhase[i]
                            : (currentPhase > 0 && i + 1 >= currentPhase
                                ? Math.max(actualByPhase[i], phaseBudgets[i])
                                : actualByPhase[i]);
                    totalExpenses += cost;
                }
            } else {
                for (double actual : actualByPhase) totalExpenses += actual;
            }

            netIncome = grossIncome - totalExpenses;

            if (tvTotalExpenses != null) {
                tvTotalExpenses.setText(String.format(Locale.getDefault(), "Total Expenses: ₱%s", df2.format(totalExpenses)));
            }
            if (tvNetIncomeCard != null) {
                if (grossIncome > 0 || totalExpenses > 0) {
                    tvNetIncomeCard.setText(String.format(Locale.getDefault(), "₱%s", df2.format(netIncome)));
                } else {
                    tvNetIncomeCard.setText("₱0.00");
                }
            }
            if (tvCostProjectionTitle != null) {
                tvCostProjectionTitle.setText(budgetsEntered ? "Projected costs" : "Costs logged so far");
            }
            if (tvPlannedCost != null) tvPlannedCost.setText(String.format(Locale.getDefault(),
                    budgetsEntered ? "Projected cost: ₱%s" : "Net so far: ₱%s",
                    df2.format(budgetsEntered ? totalExpenses : netIncome)));
            double costPerKg = totalHarvestKg > 0 ? totalExpenses / totalHarvestKg : 0;
            double breakEvenPrice = totalHarvestKg > 0 ? totalExpenses / totalHarvestKg : 0;
            double breakEvenYield = marketValue > 0 ? totalExpenses / marketValue : 0;
            double margin = grossIncome > 0 ? netIncome / grossIncome * 100 : 0;
            if (tvCostPerKg != null) tvCostPerKg.setText(String.format(Locale.getDefault(),
                    "Cost/kg: ₱%s", df2.format(costPerKg)));
            if (tvBreakEvenPrice != null) tvBreakEvenPrice.setText(String.format(Locale.getDefault(),
                    "Break-even price: ₱%s/kg", df2.format(breakEvenPrice)));
            if (tvBreakEvenYield != null) tvBreakEvenYield.setText(String.format(Locale.getDefault(),
                    "Break-even yield: %s kg", df2.format(breakEvenYield)));
            if (tvMargin != null) tvMargin.setText(String.format(Locale.getDefault(),
                    "Margin: %.2f%%", margin));
            if (tvWhatIfPrices != null) {
                tvWhatIfPrices.setText(String.format(Locale.getDefault(),
                        "what-if, not a forecast\n-20%%: ₱%s  |  0%%: ₱%s  |  +20%%: ₱%s",
                        df2.format(totalHarvestKg * marketValue * 0.80 - totalExpenses),
                        df2.format(netIncome),
                        df2.format(totalHarvestKg * marketValue * 1.20 - totalExpenses)));
            }
            applyAdjustedProjection();
        } catch (Exception e) {
            Log.e(TAG, "Error calculating income/expenses", e);
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

    private int getCurrentPhase() {
        try {
            LocalDate today = LocalDate.now();
            return getPhaseForDate(today.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private int getPhaseForDate(String dateKey) {
        try {
            LocalDate date = LocalDate.parse(dateKey);
            if (loadedProgram != null) {
                LocalDate planting = parseDate(loadedProgram.startingDate);
                if (planting == null) planting = parseDate(dateSaved);
                LocalDate transplant = parseDate(loadedProgram.transplantDate);
                LocalDate stop = parseDate(loadedProgram.stopDate);
                return TaskSchedule.getPhaseNumber(planting, transplant, stop, date,
                        parseDate(loadedProgram.phase3Date),
                        parseDate(loadedProgram.phase4Date),
                        parseDate(loadedProgram.phase5Date));
            }
            return dateSaved == null ? 0 : TaskSchedule.getPhaseNumber(
                    parseDate(dateSaved), null, null, date, null, null, null);
        } catch (Exception e) {
            return 0;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return LocalDate.parse(value.trim()); } catch (Exception e) { return null; }
    }

    private double parse(EditText et) {
        if (et == null) return 0;
        String value = et.getText().toString().trim().replace(",", "");
        try {
            return value.isEmpty() ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    private void updateCompletionWarning() {
        if (tvCompletionWarning == null) return;
        if (loadedProgram == null) {
            tvCompletionWarning.setVisibility(View.GONE);
            return;
        }
        applyRoomCompletionWarning(loadedProgram);
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
        return String.format(Locale.getDefault(), "Focus on %s tasks to recover momentum.", phaseLabelFromIndex(weakestPhase));
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
        if (tvCompletionRate != null && (programId == null || currentUser == null)) {
            tvCompletionRate.setText("0% Complete");
        }
    }

    private void applyAdjustedProjection() {
        if (tvCompletionRate != null) {
            double rate = loadedProgram != null ? loadedProgram.completionRate
                    : (completionStats != null ? completionStats.completionRate : 0);
            tvCompletionRate.setText(String.format(Locale.getDefault(), "%.0f%% Complete", rate));
        }

        if (tvAdjustedNetIncome == null || tvAdjustedExpenses == null) {
            return;
        }
        double adjustedNetIncome = netIncome;
        tvAdjustedNetIncome.setText(String.format(Locale.getDefault(), "₱%s", df2.format(adjustedNetIncome)));
        if (tvAdjustedSubtitle != null) {
            tvAdjustedSubtitle.setText("Adjusted net income");
        }
        tvAdjustedExpenses.setText(String.format(Locale.getDefault(),
                "Adjusted Expenses: ₱%s", df2.format(totalExpenses)));
        updateCompletionWarning();
    }
}