package com.android.tomatoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

/** @noinspection ALL*/
public class Cost extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    // Input fields
    private EditText landAreaInput, cultivarInput, numberOfTreesInput, fertilizerInput, totalFertilizerInput;

    // Output fields
    private TextView expenseOutput, incomeOutput, roiOutput;

    @SuppressLint({"CutPasteId", "DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost);

        // Drawer setup
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tomato App");

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

        // Bind inputs
        landAreaInput = findViewById(R.id.input_land_area);
        cultivarInput = findViewById(R.id.input_cultivar);
        numberOfTreesInput = findViewById(R.id.input_trees);
        fertilizerInput = findViewById(R.id.input_fertilizer);
        totalFertilizerInput = findViewById(R.id.input_fertilizer);

        // Bind outputs
        expenseOutput = findViewById(R.id.output_expense);
        incomeOutput = findViewById(R.id.output_income);
        roiOutput = findViewById(R.id.output_roi);

        // Bind button
        // Button
        Button calculateButton = findViewById(R.id.btn_calculate);

        // Button logic
        calculateButton.setOnClickListener(v -> {
            try {
                // Get inputs
                double landArea = Double.parseDouble(landAreaInput.getText().toString());
                cultivarInput.getText().toString();
                int numberOfTrees = Integer.parseInt(numberOfTreesInput.getText().toString());
                double fertilizer = Double.parseDouble(fertilizerInput.getText().toString());
                Double.parseDouble(totalFertilizerInput.getText().toString());

                // Dummy calculation logic (replace with real formula)
                double expense = fertilizer * numberOfTrees;
                double income = (landArea * numberOfTrees) * 10; // assume ₱10 per unit
                double roi = (income - expense) / expense * 100;

                // Set results
                expenseOutput.setText(String.format("₱ %.2f", expense));
                incomeOutput.setText(String.format("₱ %.2f", income));
                roiOutput.setText(String.format("%.2f %%", roi));

            } catch (Exception e) {
                expenseOutput.setText("Invalid input");
                incomeOutput.setText("Invalid input");
                roiOutput.setText("Invalid input");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) return true;
        if (item.getItemId() == R.id.action_back) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
