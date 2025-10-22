package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class CostSelection extends AppCompatActivity {

    private ListView listViewCultivars;
    private ArrayList<String> cultivarDisplayList = new ArrayList<>();
    private HashMap<String, String> cultivarDateMap = new HashMap<>(); // Cultivar → Date Saved

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_selection);

        listViewCultivars = findViewById(R.id.listViewCultivars);

        // 🗂️ Load cultivars (simulate saved cultivars for now)
        loadCultivars();

        // 🔤 Set up the list adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cultivarDisplayList);
        listViewCultivars.setAdapter(adapter);

        // 🎯 On item click, open Calculator with cultivar info
        listViewCultivars.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDisplay = cultivarDisplayList.get(position);
                String cultivarName = selectedDisplay.split(" \\(")[0]; // Extract name before "("
                String dateSaved = cultivarDateMap.get(cultivarName);

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
            }
        });
    }

    /**
     * This method simulates loading cultivars from saved data.
     * You can later replace this with Firebase or SQLite query results.
     */
    private void loadCultivars() {
        // 🧠 Example saved cultivars (you’ll later replace this with dynamic data)
        addCultivar("Victory F1", "October 21, 2025");
        addCultivar("Maganda F1", "October 19, 2025");
        addCultivar("Maxxime", "October 15, 2025");
        addCultivar("Colette F1", "October 10, 2025");
    }

    private void addCultivar(String cultivar, String date) {
        cultivarDisplayList.add(cultivar + " (" + date + ")");
        cultivarDateMap.put(cultivar, date);
    }
}
