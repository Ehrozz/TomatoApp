package com.android.tomatoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView textView;
    CardView workprogramselectionCard;
    CardView IPMCard;
    CardView CostCard;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        textView = findViewById(R.id.TomatoApp);
        user = mAuth.getCurrentUser();
        workprogramselectionCard = findViewById(R.id.wpsCard);
        IPMCard = findViewById(R.id.ipmCard);
        CostCard = findViewById(R.id.costCard);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        workprogramselectionCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WorkProgramSelection.class);
            startActivity(intent);
        });

        IPMCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IPM.class);
            startActivity(intent);
        });

        CostCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Cost.class);
            startActivity(intent);
        });

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            boolean isFirstLogin = prefs.getBoolean("isFirstLogin_" + user.getUid(), true);

            if (isFirstLogin) {
                // First-time login → show "Welcome"
                textView.setText("Welcome " + user.getEmail());

                // Mark as not first login anymore
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isFirstLogin_" + user.getUid(), false);
                editor.apply();
            } else {
                // Re-login → show random greeting
                String[] greetings = {
                        "Good morning",
                        "Good day",
                        "Hello",
                        "Hi there",
                        "Glad to see you back"
                };

                java.util.Random random = new java.util.Random();
                int index = random.nextInt(greetings.length);
                textView.setText(greetings[index] + " " + user.getDisplayName());
            }
        }



        // Right-side drawer toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toggle.getDrawerArrowDrawable().setDirection(
                    androidx.appcompat.graphics.drawable.DrawerArrowDrawable.ARROW_DIRECTION_END
            );
            getSupportActionBar().setHomeAsUpIndicator(toggle.getDrawerArrowDrawable());
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Handle Home
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Show User Agreement if not accepted yet
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean accepted = prefs.getBoolean("UserAgreementAccepted", false);
        if (!accepted) {
            showUserAgreementDialog(prefs);
        }
    }

    // Show the Agreement Popup
    private void showUserAgreementDialog(SharedPreferences prefs) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_agreement, null);

        CheckBox chkAgree = dialogView.findViewById(R.id.chkAgree);
        Button btnDone = dialogView.findViewById(R.id.btnDone);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("User Agreement")
                .setView(dialogView)
                .setCancelable(false) // must accept
                .create();

        chkAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnDone.setEnabled(isChecked);
        });

        btnDone.setOnClickListener(v -> {
            prefs.edit().putBoolean("UserAgreementAccepted", true).apply();
            dialog.dismiss(); // allow user to continue
        });

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
