package com.android.tomatoapp.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.android.tomatoapp.R;
import com.android.tomatoapp.auth.data.User;
import com.android.tomatoapp.common.utils.PhilippineLocations;
import com.android.tomatoapp.core.network.FirebaseErrorHandler;
import com.android.tomatoapp.core.ui.MainActivity;

public class Register extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputEditText editTextFullName;
    private TextInputEditText editTextAddress;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private View buttonReg;
    private ProgressBar progressBar;
    private TextView textView;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private final List<ValueEventListener> activeListeners = new ArrayList<>();

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        bindViews();
        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        textView.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        });

        buttonReg.setOnClickListener(v -> attemptRegistration());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupListeners();
    }
    
    private void cleanupListeners() {
        for (ValueEventListener listener : activeListeners) {
            if (databaseRef != null) {
                databaseRef.removeEventListener(listener);
            }
        }
        activeListeners.clear();
    }

    private void bindViews() {
        editTextFullName = findViewById(R.id.fullName);
        editTextAddress = findViewById(R.id.address);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        if (editTextFullName == null || editTextAddress == null ||
                editTextEmail == null || editTextPassword == null ||
                buttonReg == null || progressBar == null || textView == null) {
            Toast.makeText(this, getString(R.string.info_ui_elements_missing), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Add location picker to address field
        if (editTextAddress != null) {
            editTextAddress.setOnClickListener(v -> showLocationPicker());
        }
    }

    private void attemptRegistration() {
        String fullName = safeText(editTextFullName);
        String address = safeText(editTextAddress);
        String email = safeText(editTextEmail);
        String password = safeText(editTextPassword);

        if (!validateInputs(fullName, address, email, password)) {
            return;
        }

        setButtonsEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        checkEmailAvailability(email, emailAvailable -> {
            if (!emailAvailable) {
                progressBar.setVisibility(View.GONE);
                setButtonsEnabled(true);
                    showFieldError(editTextEmail, getString(R.string.error_email_taken));
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            setButtonsEnabled(true);
                            String errorMessage = FirebaseErrorHandler.getErrorMessage(Register.this, task.getException());
                            Toast.makeText(Register.this, errorMessage, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            progressBar.setVisibility(View.GONE);
                            setButtonsEnabled(true);
                            Toast.makeText(this, getString(R.string.error_unknown_occurred), Toast.LENGTH_SHORT).show();
                            return;
                        }

                    // Create user with null values for username and phone fields
                    try {
                        // Create user with null values for username and phone fields
                        User newUser = new User(
                                fullName,
                            null,  // username
                            null,  // usernameLower
                                address,
                                email,
                            null,  // phone
                            null,  // phoneInternational
                            null   // phoneLocal
                        );

                        databaseRef.child(user.getUid()).setValue(newUser)
                                .addOnSuccessListener(unused -> {
                                    progressBar.setVisibility(View.GONE);
                                    setButtonsEnabled(true);
                                    Toast.makeText(Register.this, getString(R.string.success_account_created), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    setButtonsEnabled(true);
                                    Toast.makeText(Register.this, getString(R.string.error_save_user_data), Toast.LENGTH_LONG).show();
                                });
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        setButtonsEnabled(true);
                        Toast.makeText(Register.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Register", "Error during user creation", e);
                    }
                    });
            });
    }
    
    private void checkEmailAvailability(String email, AvailabilityCallback callback) {
        if (TextUtils.isEmpty(email)) {
            callback.onResult(false);
            return;
        }
        
        // Use a more direct approach: check all users and filter manually
        // This avoids potential issues with Firebase query indexing
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                activeListeners.remove(this);
                
                // If snapshot doesn't exist or has no children, email is available
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    callback.onResult(true);
                    return;
                }
                
                // Check each user to see if email matches
                boolean emailExists = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        // Check email field (case-insensitive comparison)
                        String storedEmail = user.email;
                        if (!TextUtils.isEmpty(storedEmail) && !TextUtils.isEmpty(email) &&
                            email.equalsIgnoreCase(storedEmail)) {
                            emailExists = true;
                            break;
                        }
                    }
                }
                
                // Email is available if it doesn't exist
                callback.onResult(!emailExists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                activeListeners.remove(this);
                // On error, allow registration to proceed
                // Firebase will handle duplicate emails at registration time
                callback.onResult(true);
            }
        };
        
        // Query all users instead of using orderByChild (avoids index requirements)
        databaseRef.addListenerForSingleValueEvent(listener);
        activeListeners.add(listener);
    }
    
    private boolean validateInputs(String fullName, String address,
                                   String email, String password) {
        boolean valid = true;
        valid &= requireField(editTextFullName, fullName, getString(R.string.error_field_required));
        if (!TextUtils.isEmpty(fullName) && fullName.length() < 2) {
            showFieldError(editTextFullName, getString(R.string.error_fullname_too_short));
            valid = false;
        }

        valid &= requireField(editTextAddress, address, getString(R.string.error_field_required));
        valid &= requireField(editTextEmail, email, getString(R.string.error_field_required));
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showFieldError(editTextEmail, getString(R.string.error_email_invalid));
            valid = false;
        }

        valid &= requireField(editTextPassword, password, getString(R.string.error_field_required));
        if (!TextUtils.isEmpty(password)) {
            if (password.length() < MIN_PASSWORD_LENGTH) {
                showFieldError(editTextPassword, getString(R.string.error_password_too_short, MIN_PASSWORD_LENGTH));
                valid = false;
            } else if (!isPasswordStrong(password)) {
                showFieldError(editTextPassword, getString(R.string.error_password_weak));
                valid = false;
            }
        }

        return valid;
    }
    
    private boolean isPasswordStrong(String password) {
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasUpper && hasLower && hasDigit;
    }

    private boolean requireField(TextInputEditText field, String value, String message) {
        if (TextUtils.isEmpty(value)) {
            showFieldError(field, message);
            return false;
        }
        field.setError(null);
        return true;
    }

    private void showFieldError(TextInputEditText field, String message) {
        if (field != null) {
            field.setError(message);
            field.requestFocus();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private String safeText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }

    private void setButtonsEnabled(boolean enabled) {
        if (buttonReg != null) buttonReg.setEnabled(enabled);
    }

    private interface AvailabilityCallback {
        void onResult(boolean available);
    }
    
    private void showLocationPicker() {
        // Get all available locations
        String[] locations = PhilippineLocations.getAllLocations();
        if (locations == null || locations.length == 0) {
            Toast.makeText(this, "No locations available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show location selection dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Farm Location")
                .setItems(locations, (dialog, which) -> {
                    String selectedLocation = locations[which];
                    editTextAddress.setText(selectedLocation);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
