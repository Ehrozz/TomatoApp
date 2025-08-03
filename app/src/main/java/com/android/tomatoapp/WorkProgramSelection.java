package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WorkProgramSelection extends AppCompatActivity {

    private ListView programListView;
    private FloatingActionButton btnAdd;
    private ArrayList<String> programList = new ArrayList<>();
    private ArrayList<String> displayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private DatabaseReference dbRef;
    private String userId;

    // 🔹 Drawer
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_program_selection);

        programListView = findViewById(R.id.workProgramList);
        btnAdd = findViewById(R.id.addButton);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        programListView.setAdapter(adapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("workPrograms");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                programList.clear();
                displayList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String programId = child.getKey();
                    String cultivar = child.child("cultivar").getValue(String.class);
                    String startDate = child.child("startDate").getValue(String.class);

                    if (programId != null && cultivar != null && startDate != null) {
                        programList.add(programId + "|" + cultivar + "|" + startDate);
                        displayList.add(cultivar + " (" + startDate + ")");
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(WorkProgramSelection.this, Workprogram.class);
            startActivity(intent);
        });

        programListView.setOnItemClickListener((adapterView, view, position, id) -> {
            String selected = programList.get(position);
            String[] parts = selected.split("\\|");
            String programId = parts[0];
            String cultivar = parts[1];
            String date = parts[2];

            Intent intent = new Intent(WorkProgramSelection.this, Workprogram.class);
            intent.putExtra("programId", programId);
            intent.putExtra("cultivar", cultivar);
            intent.putExtra("startDate", date);
            startActivity(intent);
        });

        // 🔹 Drawer setup
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tomato App"); // Title on the right will be handled in XML

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
    }

    // 🔹 Inflate the back button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    // 🔹 Handle toggle + back button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_back) {
            finish(); // Go back to previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
