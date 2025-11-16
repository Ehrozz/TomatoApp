package com.android.tomatoapp;

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
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class Calculator extends AppCompatActivity {

    EditText etHectare, etAWF, etAFP, etMarketValue;
    EditText etFertilizer, etManpower, etPesticide, etSeedlings, etOtherExpenses;
    TextView tvCultivarName, tvDateSaved, tvNP, tvTHGrams, tvTHKg;
    TextView tvNetIncomeCard, tvSummarySubtitle, tvCompletionRate, tvAdjustedNetIncome, tvAdjustedSubtitle, tvAdjustedExpenses, tvCompletionWarning;
    TextView tvCompleteKg, tvCompleteCost, tvUreaKg, tvUreaCost, tvMOPKg, tvMOPCost, tvTotalFertilizerCost;
    TextView tvTotalExpenses;
    TextView btnSummaryDetails;
    TextView tvPesticideTotal, tvPesticidePreventive, tvPesticideCurative, tvPesticideOther;
    CheckBox cbComplete, cbUrea, cbMOP;
    LinearLayout fertilizerContent, pesticideContent;
    ImageView ivFertilizerToggle, ivPesticideToggle;

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
    private FirebaseUser currentUser;
    private double lastSavedGrossIncome = 0;
    private double lastSavedTotalExpenses = 0;
    private String programId; // Store program ID for saving hectare and analytics
    private String cultivarName; // For analytics/work program record
    private String dateSaved;    // For analytics/work program record (starting date)
    private WorkProgramDataHelper.CompletionStats completionStats;
    
    // Fertilizer prices (PHP per kg)
    private static final double PRICE_COMPLETE = 32.20; // PHP 32.20 / kg
    private static final double PRICE_UREA = 32.40;     // PHP 32.40 / kg
    private static final double PRICE_MOP = 40.70;       // PHP 40.70 / kg

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
        
        // Expense inputs
        etFertilizer = findViewById(R.id.etFertilizer);
        etManpower = findViewById(R.id.etManpower);
        etPesticide = findViewById(R.id.etPesticide);
        etSeedlings = findViewById(R.id.etSeedlings);
        etOtherExpenses = findViewById(R.id.etOtherExpenses);

        tvCultivarName = findViewById(R.id.tvCultivarName);
        tvDateSaved = findViewById(R.id.tvDateSaved);
        tvNP = findViewById(R.id.tvNP);
        tvTHGrams = findViewById(R.id.tvTHGrams);
        tvTHKg = findViewById(R.id.tvTHKg);
        tvNetIncomeCard = findViewById(R.id.tvNetIncomeCard);
        tvSummarySubtitle = findViewById(R.id.tvSummarySubtitle);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvAdjustedNetIncome = findViewById(R.id.tvAdjustedNetIncome);
        tvAdjustedSubtitle = findViewById(R.id.tvAdjustedSubtitle);
        tvCompletionWarning = findViewById(R.id.tvCompletionWarning);
        btnSummaryDetails = findViewById(R.id.btnSummaryDetails);
        tvAdjustedExpenses = findViewById(R.id.tvAdjustedExpenses);
        
        // Fertilizer TextViews
        tvCompleteKg = findViewById(R.id.tvCompleteKg);
        tvCompleteCost = findViewById(R.id.tvCompleteCost);
        tvUreaKg = findViewById(R.id.tvUreaKg);
        tvUreaCost = findViewById(R.id.tvUreaCost);
        tvMOPKg = findViewById(R.id.tvMOPKg);
        tvMOPCost = findViewById(R.id.tvMOPCost);
        tvTotalFertilizerCost = findViewById(R.id.tvTotalFertilizerCost);
        
        // Fertilizer checkboxes
        cbComplete = findViewById(R.id.cbComplete);
        cbUrea = findViewById(R.id.cbUrea);
        cbMOP = findViewById(R.id.cbMOP);
        
        // Pesticide breakdown views
        tvPesticideTotal = findViewById(R.id.tvPesticideTotal);
        tvPesticidePreventive = findViewById(R.id.tvPesticidePreventive);
        tvPesticideCurative = findViewById(R.id.tvPesticideCurative);
        tvPesticideOther = findViewById(R.id.tvPesticideOther);

        // Collapsible sections
        fertilizerContent = findViewById(R.id.fertilizerContent);
        pesticideContent = findViewById(R.id.pesticideContent);
        View fertilizerHeader = findViewById(R.id.fertilizerHeader);
        View pesticideHeader = findViewById(R.id.pesticideHeader);
        ivFertilizerToggle = findViewById(R.id.ivFertilizerToggle);
        ivPesticideToggle = findViewById(R.id.ivPesticideToggle);

        // Expense total TextView
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);

        // Summary card "Details" scrolls to breakdown
        final ScrollView scrollView = findViewById(R.id.main);
        final View breakdownTitle = findViewById(R.id.tvBreakdownTitle);
        if (btnSummaryDetails != null && scrollView != null && breakdownTitle != null) {
            btnSummaryDetails.setOnClickListener(v -> {
                scrollView.post(() -> scrollView.smoothScrollTo(0, breakdownTitle.getTop()));
            });
        }

        // Setup collapsible sections (fertilizer & pesticide only)
        setupSectionToggle(fertilizerHeader, fertilizerContent, ivFertilizerToggle);
        setupSectionToggle(pesticideHeader, pesticideContent, ivPesticideToggle);

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
        
        // Initialize Firebase first (needed for fetching hectare)
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            calculationsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("calculations");
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
        
        // Make fertilizer field read-only (auto-filled) - don't add watcher since it's auto-filled
        etFertilizer.setFocusable(false);
        etFertilizer.setClickable(false);
        etFertilizer.setEnabled(false);
        
        // Expense input watchers (excluding fertilizer which is auto-filled)
        etManpower.addTextChangedListener(watcher);
        etPesticide.addTextChangedListener(watcher);
        etSeedlings.addTextChangedListener(watcher);
        etOtherExpenses.addTextChangedListener(watcher);
        
        // Checkbox listeners to update fertilizer cost when selection changes
        CompoundButton.OnCheckedChangeListener checkboxListener = (buttonView, isChecked) -> {
            updateFertilizerCost();
            computeExpenses();
        };
        cbComplete.setOnCheckedChangeListener(checkboxListener);
        cbUrea.setOnCheckedChangeListener(checkboxListener);
        cbMOP.setOnCheckedChangeListener(checkboxListener);
        
        // Initial calculations
        computeFertilizer();
        computeExpenses();
        
        // Auto-save when calculation is complete
        setupAutoSave();

        // Load completion stats for adjusted projections
        loadCompletionStats();
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
                double TH = AWP * currentNP;      // Total Harvest (grams)
                double THKg = TH / 1000;          // Total Harvest (kilograms)
                grossIncome = THKg * marketValue; // ₱

                tvTHGrams.setText("Total Harvest (grams): " + df2.format(TH));
                tvTHKg.setText("Total Harvest (kg): " + df2.format(THKg));
            } else {
                grossIncome = 0;
                tvTHGrams.setText("Total Harvest (grams): —");
                tvTHKg.setText("Total Harvest (kg): —");
            }
            
            // 🔹 Compute fertilizer requirements
            computeFertilizer();

            // 🔹 Update pesticide breakdown (temporary heuristic)
            updatePesticideBreakdown();
            
            // 🔹 Compute expenses and net income
            computeExpenses();
            
            // Auto-save to Firebase when calculation is complete
            if (grossIncome > 0 && totalExpenses >= 0) {
                saveCalculation();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void computeFertilizer() {
        if (hectare <= 0 || baseNP <= 0) {
            // Reset fertilizer displays
            tvCompleteKg.setText("Complete (14-14-14) kg/ha: —");
            tvCompleteCost.setText("Cost: —");
            tvUreaKg.setText("Urea (46-0-0) kg/ha: —");
            tvUreaCost.setText("Cost: —");
            tvMOPKg.setText("MOP (0-0-60) kg/ha: —");
            tvMOPCost.setText("Cost: —");
            tvTotalFertilizerCost.setText("Total Fertilizer Cost (₱): —");
            completeCostTotal = 0;
            ureaCostTotal = 0;
            mopCostTotal = 0;
            fertilizerCost = 0;
            etFertilizer.setText("");
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
        double totalCostTotal = completeCostTotal + ureaCostTotal + mopCostTotal;
        
        // Display results
        tvCompleteKg.setText("Complete (14-14-14): " + df2.format(completeKgTotal) + " kg (" + df2.format(completeKgPerHa) + " kg/ha)");
        tvCompleteCost.setText("Cost: ₱" + df2.format(completeCostTotal) + " (₱" + df2.format(completeCostPerHa) + "/ha)");
        
        tvUreaKg.setText("Urea (46-0-0): " + df2.format(ureaKgTotal) + " kg (" + df2.format(ureaKgPerHa) + " kg/ha)");
        tvUreaCost.setText("Cost: ₱" + df2.format(ureaCostTotal) + " (₱" + df2.format(ureaCostPerHa) + "/ha)");
        
        tvMOPKg.setText("MOP (0-0-60): " + df2.format(mopKgTotal) + " kg (" + df2.format(mopKgPerHa) + " kg/ha)");
        tvMOPCost.setText("Cost: ₱" + df2.format(mopCostTotal) + " (₱" + df2.format(mopCostPerHa) + "/ha)");
        
        tvTotalFertilizerCost.setText("Total Fertilizer Cost: ₱" + df2.format(totalCostTotal) + " (₱" + df2.format(totalCostPerHa) + "/ha)");
        
        // Update fertilizer cost based on checkbox selections
        updateFertilizerCost();
    }
    
    /**
     * Updates the fertilizer cost field based on selected checkboxes
     */
    private void updateFertilizerCost() {
        double selectedCost = 0;
        
        if (cbComplete != null && cbComplete.isChecked()) {
            selectedCost += completeCostTotal;
        }
        if (cbUrea != null && cbUrea.isChecked()) {
            selectedCost += ureaCostTotal;
        }
        if (cbMOP != null && cbMOP.isChecked()) {
            selectedCost += mopCostTotal;
        }
        
        fertilizerCost = selectedCost;
        if (selectedCost > 0) {
            etFertilizer.setText(df2.format(fertilizerCost));
        } else {
            etFertilizer.setText("");
        }
    }
    
    private void computeExpenses() {
        try {
            // Parse expense inputs
            fertilizerCost = parse(etFertilizer);
            manpower = parse(etManpower);
            pesticide = parse(etPesticide);
            seedlings = parse(etSeedlings);
            otherExpenses = parse(etOtherExpenses);
            
            // Calculate total expenses
            totalExpenses = fertilizerCost + manpower + pesticide + seedlings + otherExpenses;
            
            // Calculate net income = Gross Income - Total Expenses
            netIncome = grossIncome - totalExpenses;
            
            // Display results
            if (tvTotalExpenses != null) {
                tvTotalExpenses.setText("Total Expenses (₱): " + df2.format(totalExpenses));
            }

            // Update card net income only
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
     * Temporary pesticide breakdown heuristic.
     *
     * For now we approximate pesticide cost as a fixed amount per hectare and
     * split it into preventive / curative / other components. This should be
     * replaced later with values derived from literature or local studies.
     */
    private void updatePesticideBreakdown() {
        if (tvPesticideTotal == null || tvPesticidePreventive == null ||
                tvPesticideCurative == null || tvPesticideOther == null) {
            return;
        }

        if (hectare <= 0) {
            tvPesticideTotal.setText("Suggested pesticide cost: —");
            tvPesticidePreventive.setText("Preventive sprays: —");
            tvPesticideCurative.setText("Curative sprays: —");
            tvPesticideOther.setText("Other pesticide-related costs: —");
            return;
        }

        // Placeholder: suggested pesticide cost per hectare (PHP)
        double pesticidePerHa = 8000; // TODO: replace with literature-based value
        double suggestedTotal = pesticidePerHa * hectare;

        double preventive = suggestedTotal * 0.4;
        double curative = suggestedTotal * 0.4;
        double other = suggestedTotal * 0.2;

        tvPesticideTotal.setText("Suggested pesticide cost: ₱" + df2.format(suggestedTotal));
        tvPesticidePreventive.setText("Preventive sprays: ₱" + df2.format(preventive));
        tvPesticideCurative.setText("Curative sprays: ₱" + df2.format(curative));
        tvPesticideOther.setText("Other pesticide-related costs: ₱" + df2.format(other));

        // Auto-fill pesticide expense only if user hasn't entered anything yet
        if (etPesticide != null) {
            String current = etPesticide.getText().toString().trim();
            if (current.isEmpty()) {
                etPesticide.setText(df2.format(suggestedTotal));
            }
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
        tvAdjustedExpenses.setText("Adjusted Expenses (₱): " + df2.format(projection.adjustedExpenses));
        updateCompletionWarning();
    }
}
