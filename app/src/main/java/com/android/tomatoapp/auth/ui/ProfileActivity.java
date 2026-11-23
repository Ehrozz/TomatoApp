package com.android.tomatoapp.auth.ui;

import android.os.Bundle;
import android.content.Intent;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.android.tomatoapp.R;
import com.android.tomatoapp.auth.data.User;
import com.android.tomatoapp.common.models.UserLocation;
import com.android.tomatoapp.common.utils.PhilippineLocations;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import android.content.SharedPreferences;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for viewing and editing user profile information.
 */
public class ProfileActivity extends BaseBottomNavActivity {

    private TextInputEditText editTextFullName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextAddress;
    
    private TextInputLayout layoutFullName;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutAddress;
    
    private Button btnSave;
    private Button btnSetPassword;
    private ProgressBar progressBar;
    private TextView profileNameHeader;
    private TextView profileEmailHeader;
    private ImageView profileAvatarImage;
    private TextView profileAvatarEmoji;
    private String profilePhotoUri;
    private ActivityResultLauncher<String[]> photoPickerLauncher;
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    
    private boolean isLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        setupBottomNavigation();
        
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
        setupPhotoPicker();
        setupSaveButton();
        setupSetPasswordButton();
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
        btnSetPassword = findViewById(R.id.btnSetPassword);
        progressBar = findViewById(R.id.progressBarProfile);
        profileNameHeader = findViewById(R.id.profileNameHeader);
        profileEmailHeader = findViewById(R.id.profileEmailHeader);
        profileAvatarImage = findViewById(R.id.profileAvatarImage);
        profileAvatarEmoji = findViewById(R.id.profileAvatarEmoji);

        View avatarCard = findViewById(R.id.profileAvatarCard);
        if (avatarCard != null) {
            avatarCard.setOnClickListener(v -> launchPhotoPicker());
        }
        
        // Set email from Firebase Auth (read-only)
        if (currentUser.getEmail() != null) {
            String email = currentUser.getEmail();
            editTextEmail.setText(email);
            editTextEmail.setEnabled(false);
            editTextEmail.setFocusable(false);
            if (profileEmailHeader != null) {
                profileEmailHeader.setText(email);
            }
        }
    }
    
    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (!isLoading) {
                saveUserData();
            }
        });
    }

    private void setupSetPasswordButton() {
        if (btnSetPassword == null) {
            return;
        }
        boolean hasPassword = hasPasswordProvider();
        btnSetPassword.setVisibility(hasPassword ? View.GONE : View.VISIBLE);
        btnSetPassword.setOnClickListener(v -> showSetPasswordDialog());
    }

    private boolean hasPasswordProvider() {
        if (currentUser == null) {
            return false;
        }
        for (UserInfo info : currentUser.getProviderData()) {
            if (info != null && "password".equals(info.getProviderId())) {
                return true;
            }
        }
        return false;
    }

    private void showSetPasswordDialog() {
        EditText input = new EditText(this);
        input.setHint("Password");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle("Set password")
                .setMessage("At least 6 characters, with upper, lower, and a digit.")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = input.getText() != null ? input.getText().toString() : "";
                    if (!isPasswordStrong(password)) {
                        Toast.makeText(this,
                                "Password must be at least 6 characters with upper, lower, and a digit.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    linkPassword(password);
                })
                .show();
    }

    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
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

    private void linkPassword(String password) {
        if (currentUser == null || TextUtils.isEmpty(currentUser.getEmail())) {
            Toast.makeText(this, "No email on this account.", Toast.LENGTH_LONG).show();
            return;
        }
        currentUser.linkWithCredential(EmailAuthProvider.getCredential(currentUser.getEmail(), password))
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Password added. You can now log in either way.", Toast.LENGTH_LONG).show();
                    if (btnSetPassword != null) {
                        btnSetPassword.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                        Toast.makeText(this, "Please log out and sign in again, then retry.", Toast.LENGTH_LONG).show();
                    } else {
                        String message = e.getMessage() != null ? e.getMessage() : "Could not set password.";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
            if (profileNameHeader != null) {
                profileNameHeader.setText(user.fullName);
            }
        }
        if (user.address != null) {
            editTextAddress.setText(user.address);
        }
        profilePhotoUri = user.photoUri;
        loadProfilePhoto(profilePhotoUri);
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
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("address", address);
        updates.put("email", email);
        updates.put("photoUri", profilePhotoUri);
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        isLoading = true;
        
        // Save only changed profile fields to preserve username/phone from registration flow.
        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    isLoading = false;

                    // Keep Firebase Auth user display name in sync
                    com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                            new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                    currentUser.updateProfile(profileUpdates);

                    if (profileNameHeader != null) {
                        profileNameHeader.setText(fullName);
                    }

                    if (!address.isEmpty()) {
                        String[] parts = address.split(", ");
                        String brgy = parts.length >= 3 ? parts[0].trim() : "";
                        String city = parts.length >= 2 ? parts[1].trim() : (parts.length == 1 ? parts[0].trim() : "");
                        String province = parts.length >= 3 ? parts[2].trim() : (parts.length == 2 ? parts[1].trim() : "");

                        double[] coords = PhilippineLocations.getCoordinatesForCity(city, province);
                        double lat = coords[0];
                        double lon = coords[1];

                        DatabaseReference locationsRef = userRef.child("locations");
                        locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String existingDefaultId = null;
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    UserLocation loc = ds.getValue(UserLocation.class);
                                    if (loc != null && loc.isDefault) {
                                        existingDefaultId = ds.getKey();
                                        break;
                                    }
                                }
                                String targetId = existingDefaultId != null ? existingDefaultId : locationsRef.push().getKey();
                                if (targetId != null) {
                                    UserLocation updatedLoc = new UserLocation(targetId, province, city, brgy, lat, lon, true);
                                    locationsRef.child(targetId).setValue(updatedLoc);
                                }
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });

                        SharedPreferences wp = getSharedPreferences("WeatherPref", MODE_PRIVATE);
                        wp.edit()
                                .putLong("lat", Double.doubleToLongBits(lat))
                                .putLong("lon", Double.doubleToLongBits(lon))
                                .putString("name", address)
                                .apply();
                    }
                    
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

    private void setupPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handlePickedPhoto
        );
    }

    private void launchPhotoPicker() {
        if (photoPickerLauncher != null) {
            photoPickerLauncher.launch(new String[]{"image/*"});
        }
    }

    private void handlePickedPhoto(Uri uri) {
        if (uri == null) {
            return;
        }

        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
        }

        profilePhotoUri = uri.toString();
        loadProfilePhoto(profilePhotoUri);
    }

    private void loadProfilePhoto(String uriString) {
        if (profileAvatarImage == null || profileAvatarEmoji == null) {
            return;
        }

        if (TextUtils.isEmpty(uriString)) {
            profileAvatarImage.setImageResource(R.drawable.ic_person);
            profileAvatarEmoji.setVisibility(View.VISIBLE);
            profileAvatarImage.setVisibility(View.VISIBLE);
            return;
        }

        try {
            Uri uri = Uri.parse(uriString);
            try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    profileAvatarImage.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                    profileAvatarEmoji.setVisibility(View.GONE);
                    profileAvatarImage.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            profileAvatarImage.setImageResource(R.drawable.ic_person);
            profileAvatarEmoji.setVisibility(View.VISIBLE);
            profileAvatarImage.setVisibility(View.VISIBLE);
        }
    }

}
