package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;

public class InformationInterface extends AppCompatActivity {

    ListView diseaseListView;
    ArrayAdapter<String> adapter;
    ArrayList<String> diseaseList = new ArrayList<>(Arrays.asList(
            "Early Blight",
            "Late Blight",
            "Fusarium Wilt",
            "Septoria Leaf Spot",
            "Bacterial Spot",
            "Bacterial Speck",
            "Bacterial Canker",
            "Tomato Yellow Leaf Curl Virus (TYLCV)",
            "Tomato Mosaic Virus (ToMV)",
            "Root-Knot Nematodes",
            "Powdery Mildew",
            "Downy Mildew",
            "Anthracnose",
            "Southern Blight",
            "Gray Mold (Botrytis)",
            "Damping Off",
            "Alternaria Canker",
            "Tomato Spotted Wilt Virus (TSWV)"
    ));

    // 🔹 Drawer
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_interface);

        diseaseListView = findViewById(R.id.diseaseListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, diseaseList);
        diseaseListView.setAdapter(adapter);

        // 🔹 Item click removes from list
        diseaseListView.setOnItemClickListener((parent, view, position, id) -> {
            diseaseList.remove(position);
            adapter.notifyDataSetChanged();
        });

        // 🔹 Drawer setup
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tomato App"); // same as other activities

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
