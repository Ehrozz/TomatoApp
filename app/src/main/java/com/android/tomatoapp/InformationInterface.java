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
            "Tomato Leaf Curl Virus",
            "Early Blight",
            "Late Blight",
            "Bacterial Wilt",
            "Fusarium Wilt",
            "Anthracnose ",
            "Black Leaf Mold"
    ));

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

        diseaseListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDisease = (String) parent.getItemAtPosition(position);

            Intent intent = new Intent(InformationInterface.this, DiseaseView.class);
            intent.putExtra("disease_name", selectedDisease);
            startActivity(intent);
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.information_section);
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true; // no back button menu
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
