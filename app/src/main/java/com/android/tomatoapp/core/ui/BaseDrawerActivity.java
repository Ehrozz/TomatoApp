package com.android.tomatoapp.core.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

/**
 * Base activity that provides drawer navigation functionality to all activities.
 * Activities should extend this class and call setupDrawer() in onCreate().
 */
public abstract class BaseDrawerActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle toggle;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply language setting
        String language = SettingsPreferences.getLanguage(newBase);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    private boolean wasOffline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme setting before super.onCreate
        applyTheme();
        super.onCreate(savedInstanceState);
        
        // Check initial connectivity state
        wasOffline = !LocalDataManager.isOnline(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if we just came back online
        boolean isOnline = LocalDataManager.isOnline(this);
        if (wasOffline && isOnline) {
            // We just came back online, sync local data to Firebase
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                LocalDataManager.getInstance(this).syncWorkProgramsToFirebase(this, currentUser.getUid());
            }
        }
        wasOffline = !isOnline;
    }

    private void applyTheme() {
        String theme = SettingsPreferences.getTheme(this);
        if (theme.equals(SettingsPreferences.THEME_DARK)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme.equals(SettingsPreferences.THEME_LIGHT)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    /**
     * Sets up the drawer navigation. Call this in onCreate() after setContentView().
     * The layout must include a DrawerLayout with id drawer_layout and NavigationView with id navigation_view.
     */
    protected void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        // Try both possible IDs for navigation view
        navigationView = findViewById(R.id.navigation_view);
        if (navigationView == null) {
            navigationView = findViewById(R.id.nav_view);
        }

        if (drawerLayout == null || navigationView == null) {
            // Drawer not available in this layout, skip setup
            return;
        }

        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        
        // Add listener to hide/show action bar instantly when drawer opens/closes
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            private boolean actionBarVisible = true;

            @Override
            public void onDrawerSlide(@NonNull android.view.View drawerView, float slideOffset) {
                // Hide/show action bar instantly based on slide offset
                if (getSupportActionBar() != null) {
                    if (slideOffset > 0.1f && actionBarVisible) {
                        // Hide instantly when drawer starts opening
                        getSupportActionBar().hide();
                        actionBarVisible = false;
                    } else if (slideOffset <= 0.1f && !actionBarVisible) {
                        // Show instantly when drawer is mostly closed
                        getSupportActionBar().show();
                        actionBarVisible = true;
                    }
                }
            }

            @Override
            public void onDrawerOpened(@NonNull android.view.View drawerView) {
                // Ensure action bar is hidden when drawer is fully open
                if (getSupportActionBar() != null && actionBarVisible) {
                    getSupportActionBar().hide();
                    actionBarVisible = false;
                }
            }

            @Override
            public void onDrawerClosed(@NonNull android.view.View drawerView) {
                // Ensure action bar is shown when drawer is fully closed
                if (getSupportActionBar() != null && !actionBarVisible) {
                    getSupportActionBar().show();
                    actionBarVisible = true;
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Optional: handle state changes
            }
        });
        
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = handleNavigationItem(item.getItemId());
            drawerLayout.closeDrawers();
            return handled;
        });
        
        // Setup drawer header with user profile
        setupDrawerHeader();
    }
    
    /**
     * Sets up the drawer header with user profile picture and name.
     */
    private void setupDrawerHeader() {
        if (navigationView == null) return;
        
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;
        
        ImageView profileImage = headerView.findViewById(R.id.sidebarProfileImage);
        TextView userName = headerView.findViewById(R.id.sidebarUserName);
        TextView userEmail = headerView.findViewById(R.id.sidebarUserEmail);
        View drawerHeader = headerView.findViewById(R.id.drawerHeader);
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        // Set email
        if (userEmail != null && currentUser.getEmail() != null) {
            userEmail.setText(currentUser.getEmail());
        }
        
        // Load profile picture
        if (profileImage != null) {
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null && !photoUrl.toString().isEmpty()) {
                // TODO: Load image from URL - can use Glide or Picasso if added to dependencies
                // For now, use default logo
                profileImage.setImageResource(R.mipmap.ic_logo);
            } else {
                // Use default logo if no profile picture
                profileImage.setImageResource(R.mipmap.ic_logo);
            }
        }
        
        // Load user name from Firebase database
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && userName != null) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.fullName != null && !user.fullName.isEmpty()) {
                        userName.setText(user.fullName);
                    } else {
                        // Fallback to display name or email
                        String displayName = currentUser.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            userName.setText(displayName);
                        } else {
                            String email = currentUser.getEmail();
                            if (email != null && email.contains("@")) {
                                userName.setText(email.substring(0, email.indexOf("@")));
                            } else {
                                userName.setText("User");
                            }
                        }
                    }
                } else {
                    // No user data, use display name or email
                    if (userName != null) {
                        String displayName = currentUser.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            userName.setText(displayName);
                        } else {
                            String email = currentUser.getEmail();
                            if (email != null && email.contains("@")) {
                                userName.setText(email.substring(0, email.indexOf("@")));
                            } else {
                                userName.setText("User");
                            }
                        }
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Use fallback name
                if (userName != null) {
                    String displayName = currentUser.getDisplayName();
                    if (displayName != null && !displayName.isEmpty()) {
                        userName.setText(displayName);
                    } else {
                        String email = currentUser.getEmail();
                        if (email != null && email.contains("@")) {
                            userName.setText(email.substring(0, email.indexOf("@")));
                        } else {
                            userName.setText("User");
                        }
                    }
                }
            }
        });
        
        // Make header clickable to open profile
        if (drawerHeader != null) {
            drawerHeader.setOnClickListener(v -> {
                if (!(BaseDrawerActivity.this instanceof ProfileActivity)) {
                    startActivity(new Intent(BaseDrawerActivity.this, ProfileActivity.class));
                    drawerLayout.closeDrawers();
                }
            });
        }
    }

    /**
     * Handles navigation item selection. Override this method to customize navigation behavior.
     * @param itemId The ID of the selected menu item
     * @return true if the item was handled, false otherwise
     */
    protected boolean handleNavigationItem(int itemId) {
        if (itemId == R.id.nav_home) {
            // Don't navigate if already on MainActivity
            if (!(this instanceof MainActivity)) {
                startActivity(new Intent(this, MainActivity.class));
            }
            return true;
        } else if (itemId == R.id.nav_profile) {
            if (!(this instanceof ProfileActivity)) {
                startActivity(new Intent(this, ProfileActivity.class));
            }
            return true;
        } else if (itemId == R.id.nav_analytics) {
            if (!(this instanceof AnalyticsActivity)) {
                startActivity(new Intent(this, AnalyticsActivity.class));
            }
            return true;
        } else if (itemId == R.id.nav_season_comparison) {
            if (!(this instanceof SeasonComparisonActivity)) {
                startActivity(new Intent(this, SeasonComparisonActivity.class));
            }
            return true;
        } else if (itemId == R.id.nav_settings) {
            if (!(this instanceof SettingsActivity)) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            return true;
        } else if (itemId == R.id.nav_user_manual) {
            if (!(this instanceof UserManualActivity)) {
                startActivity(new Intent(this, UserManualActivity.class));
            }
            return true;
        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menu removed - notification icon is now in the main layout
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle != null && toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

