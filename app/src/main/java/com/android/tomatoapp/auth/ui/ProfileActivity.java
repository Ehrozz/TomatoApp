package com.android.tomatoapp.auth.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.tomatoapp.R;
import com.android.tomatoapp.auth.data.User;
import com.android.tomatoapp.core.ui.BaseDrawerActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Activity for viewing and editing user profile information.
 */
public class ProfileActivity extends BaseDrawerActivity {

    private TextInputEditText editTextFullName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextAddress;
    
    private TextInputLayout layoutFullName;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutAddress;
    
    private Button btnSave;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    
    private boolean isLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        setupDrawer();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
        }
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view your profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize Firebase reference
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        
        initializeViews();
        setupSaveButton();
        loadUserData();
    }
    
    private void initializeViews() {
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextAddress = findViewById(R.id.editTextAddress);
        
        layoutFullName = findViewById(R.id.layoutFullName);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutAddress = findViewById(R.id.layoutAddress);
        btnSave = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.progressBarProfile);
        
        // Set email from Firebase Auth (read-only)
        if (currentUser.getEmail() != null) {
            editTextEmail.setText(currentUser.getEmail());
            editTextEmail.setEnabled(false);
            editTextEmail.setFocusable(false);
            layoutEmail.setHint("Email (cannot be changed)");
        }
    }
    
    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (!isLoading) {
                saveUserData();
            }
        });
    }
    
    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);
        isLoading = true;
        btnSave.setEnabled(false);
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                btnSave.setEnabled(true);
                
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        populateFields(user);
                    } else {
                        // User data doesn't exist, show empty form
                        Toast.makeText(ProfileActivity.this, 
                                "Profile data not found. Please fill in your information.", 
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // No user data in database, show empty form
                    Toast.makeText(ProfileActivity.this, 
                            "Welcome! Please fill in your profile information.", 
                            Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                btnSave.setEnabled(true);
                
                Toast.makeText(ProfileActivity.this, 
                        "Failed to load profile: " + error.getMessage(), 
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void populateFields(User user) {
        if (user.fullName != null) {
            editTextFullName.setText(user.fullName);
        }
        if (user.address != null) {
            editTextAddress.setText(user.address);
        }
        // Email is already set from Firebase Auth
    }
    
    private void saveUserData() {
        // Clear previous errors
        clearErrors();
        
        // Get input values
        String fullName = editTextFullName.getText() != null ? 
                editTextFullName.getText().toString().trim() : "";
        String address = editTextAddress.getText() != null ? 
                editTextAddress.getText().toString().trim() : "";
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
        
        // Validate inputs
        if (!validateInputs(fullName)) {
            return;
        }
        
        // Create User object with null values for username and phone fields
        User user = new User(
                fullName,
                null,  // username
                null,  // usernameLower
                address,
                email,
                null,  // phone
                null,  // phoneInternational
                null   // phoneLocal
        );
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        isLoading = true;
        
        // Save to Firebase
        userRef.setValue(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    isLoading = false;
                    
                    Toast.makeText(ProfileActivity.this, 
                            "Profile updated successfully!", 
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    isLoading = false;
                    
                    String errorMsg = "Failed to save profile";
                    if (e.getMessage() != null) {
                        errorMsg += ": " + e.getMessage();
                    }
                    Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
    }
    
    private boolean validateInputs(String fullName) {
        boolean isValid = true;
        
        // Validate Full Name
        if (TextUtils.isEmpty(fullName)) {
            layoutFullName.setError("Full name is required");
            isValid = false;
        } else {
            layoutFullName.setError(null);
        }
        
        return isValid;
    }
    
    private void clearErrors() {
        layoutFullName.setError(null);
    }

}
