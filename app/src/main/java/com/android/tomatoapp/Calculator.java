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
    TextView tvCultivarName, tvDateSaved, tvNP, tvTHGrams, tvTHKg, tvGrossIncome;

    double hectare = 0, AWF = 0, AFP = 0, baseNP = 0, currentNP = 0, marketValue = 0;
    DecimalFormat df = new DecimalFormat("#,###");
    DecimalFormat df2 = new DecimalFormat("#,###.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        // Bind UI elements
        etHectare = findViewById(R.id.etHectare);
        etAWF = findViewById(R.id.etAWF);
        etAFP = findViewById(R.id.etAFP);
        etMarketValue = findViewById(R.id.etMarketValue);

        tvCultivarName = findViewById(R.id.tvCultivarName);
        tvDateSaved = findViewById(R.id.tvDateSaved);
        tvNP = findViewById(R.id.tvNP);
        tvTHGrams = findViewById(R.id.tvTHGrams);
        tvTHKg = findViewById(R.id.tvTHKg);
        tvGrossIncome = findViewById(R.id.tvGrossIncome);

        // Get values from intent
        String cultivarName = getIntent().getStringExtra("cultivar_name");
        String dateSaved = getIntent().getStringExtra("date_saved");
        baseNP = getIntent().getDoubleExtra("NP_VALUE", 0); // base NP per hectare

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
                double grossIncome = THKg * marketValue; // ₱

                tvTHGrams.setText("Total Harvest (grams): " + df2.format(TH));
                tvTHKg.setText("Total Harvest (kg): " + df2.format(THKg));
                tvGrossIncome.setText("Gross Income (₱): " + df2.format(grossIncome));
            } else {
                tvTHGrams.setText("Total Harvest (grams): —");
                tvTHKg.setText("Total Harvest (kg): —");
                tvGrossIncome.setText("Gross Income (₱): —");
            }

        } catch (Exception e) {
            e.printStackTrace();
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
