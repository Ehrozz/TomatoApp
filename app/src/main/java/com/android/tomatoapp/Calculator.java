package com.android.tomatoapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class Calculator extends AppCompatActivity {

    EditText etHectare, etAWF, etAFP, etMarketValue;
    EditText etFertilizer, etManpower, etPesticide, etSeedlings, etOtherExpenses;
    TextView tvCultivarName, tvDateSaved, tvNP, tvTHGrams, tvTHKg, tvGrossIncome;
    TextView tvCompleteKg, tvCompleteCost, tvUreaKg, tvUreaCost, tvMOPKg, tvMOPCost, tvTotalFertilizerCost;
    TextView tvTotalExpenses, tvNetIncome;

    double hectare = 0, AWF = 0, AFP = 0, baseNP = 0, currentNP = 0, marketValue = 0;
    double fertilizerCost = 0, manpower = 0, pesticide = 0, seedlings = 0, otherExpenses = 0;
    double grossIncome = 0, totalExpenses = 0, netIncome = 0;
    String growthHabit = "";
    DecimalFormat df = new DecimalFormat("#,###");
    DecimalFormat df2 = new DecimalFormat("#,###.##");
    
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
        tvGrossIncome = findViewById(R.id.tvGrossIncome);
        
        // Fertilizer TextViews
        tvCompleteKg = findViewById(R.id.tvCompleteKg);
        tvCompleteCost = findViewById(R.id.tvCompleteCost);
        tvUreaKg = findViewById(R.id.tvUreaKg);
        tvUreaCost = findViewById(R.id.tvUreaCost);
        tvMOPKg = findViewById(R.id.tvMOPKg);
        tvMOPCost = findViewById(R.id.tvMOPCost);
        tvTotalFertilizerCost = findViewById(R.id.tvTotalFertilizerCost);
        
        // Expense and net income TextViews
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvNetIncome = findViewById(R.id.tvNetIncome);

        // Get values from intent
        String cultivarName = getIntent().getStringExtra("cultivar_name");
        String dateSaved = getIntent().getStringExtra("date_saved");
        baseNP = getIntent().getDoubleExtra("NP_VALUE", 0); // base NP per hectare
        growthHabit = getIntent().getStringExtra("growth_habit"); // Growth habit

        // Display cultivar info
        tvCultivarName.setText("Cultivar: " + (cultivarName != null ? cultivarName : "N/A"));
        tvDateSaved.setText("Date Saved: " + (dateSaved != null ? dateSaved : "N/A"));
        tvNP.setText("Number of Plants Per Hectare (NP): " + df.format(baseNP));
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
        
        // Initial calculations
        computeFertilizer();
        computeExpenses();
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
                tvGrossIncome.setText("Gross Income (₱): " + df2.format(grossIncome));
            } else {
                grossIncome = 0;
                tvTHGrams.setText("Total Harvest (grams): —");
                tvTHKg.setText("Total Harvest (kg): —");
                tvGrossIncome.setText("Gross Income (₱): —");
            }
            
            // 🔹 Compute fertilizer requirements
            computeFertilizer();
            
            // 🔹 Compute expenses and net income
            computeExpenses();

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
        double completeCostTotal = completeCostPerHa * hectare;
        double ureaCostTotal = ureaCostPerHa * hectare;
        double mopCostTotal = mopCostPerHa * hectare;
        double totalCostTotal = totalCostPerHa * hectare;
        
        // Display results
        tvCompleteKg.setText("Complete (14-14-14): " + df2.format(completeKgTotal) + " kg (" + df2.format(completeKgPerHa) + " kg/ha)");
        tvCompleteCost.setText("Cost: ₱" + df2.format(completeCostTotal) + " (₱" + df2.format(completeCostPerHa) + "/ha)");
        
        tvUreaKg.setText("Urea (46-0-0): " + df2.format(ureaKgTotal) + " kg (" + df2.format(ureaKgPerHa) + " kg/ha)");
        tvUreaCost.setText("Cost: ₱" + df2.format(ureaCostTotal) + " (₱" + df2.format(ureaCostPerHa) + "/ha)");
        
        tvMOPKg.setText("MOP (0-0-60): " + df2.format(mopKgTotal) + " kg (" + df2.format(mopKgPerHa) + " kg/ha)");
        tvMOPCost.setText("Cost: ₱" + df2.format(mopCostTotal) + " (₱" + df2.format(mopCostPerHa) + "/ha)");
        
        tvTotalFertilizerCost.setText("Total Fertilizer Cost: ₱" + df2.format(totalCostTotal) + " (₱" + df2.format(totalCostPerHa) + "/ha)");
        
        // Auto-fill fertilizer cost in expense input (only if valid)
        if (totalCostTotal > 0) {
            fertilizerCost = totalCostTotal;
            etFertilizer.setText(df2.format(fertilizerCost));
        } else {
            fertilizerCost = 0;
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
            tvTotalExpenses.setText("Total Expenses (₱): " + df2.format(totalExpenses));
            
            if (netIncome >= 0) {
                tvNetIncome.setText("Net Income (₱): " + df2.format(netIncome));
                tvNetIncome.setBackgroundColor(0xFF4CAF50); // Green
            } else {
                tvNetIncome.setText("Net Income (₱): " + df2.format(netIncome));
                tvNetIncome.setBackgroundColor(0xFFF44336); // Red
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values on error
            tvTotalExpenses.setText("Total Expenses (₱): —");
            tvNetIncome.setText("Net Income (₱): —");
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
}
