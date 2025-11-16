package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    TextInputEditText editTextFullName, editTextUsername, editTextAddress, editTextEmail, editTextPassword;
    TextInputEditText editTextPhone, editTextOtp;
    View buttonReg, buttonSendOtp, buttonVerifyOtp;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    DatabaseReference databaseRef;

    private String verificationId;
    private boolean phoneVerified = false;

    // ✅ Toggle between testing and production
    private final boolean isTesting = true;

    // Example Firebase test numbers (add in Firebase Console > Authentication > Phone > Test numbers)
    private final String TEST_PHONE = "+15555555555";
    private final String TEST_OTP = "123456";

    // Validation constants
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int OTP_LENGTH = 6;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$"); // E.164 format

    @Override
    public void onStart() {
        super.onStart();
        // Initialize mAuth if not already initialized
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
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

        // Bind views
        editTextFullName = findViewById(R.id.fullName);
        editTextUsername = findViewById(R.id.username);
        editTextAddress = findViewById(R.id.address);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextPhone = findViewById(R.id.phone);
        editTextOtp = findViewById(R.id.otp);

        buttonReg = findViewById(R.id.btn_register);
        buttonSendOtp = findViewById(R.id.btn_send_otp);
        buttonVerifyOtp = findViewById(R.id.btn_verify_otp);

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Validate that all required views are found
        if (editTextFullName == null || editTextUsername == null || editTextAddress == null ||
                editTextEmail == null || editTextPassword == null || editTextPhone == null ||
                editTextOtp == null || buttonReg == null || buttonSendOtp == null ||
                buttonVerifyOtp == null || progressBar == null || textView == null) {
            Toast.makeText(this, "Error: Some UI elements are missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        // Send OTP
        buttonSendOtp.setOnClickListener(v -> {
            String phone = editTextPhone.getText().toString().trim();

            if (TextUtils.isEmpty(phone)) {
                editTextPhone.setError("Enter phone number");
                editTextPhone.requestFocus();
                return;
            }

            // ✅ For testing, skip sending SMS
            if (isTesting && phone.equals(TEST_PHONE)) {
                Toast.makeText(this, "Test OTP: " + TEST_OTP, Toast.LENGTH_LONG).show();
                return;
            }

            // ✅ Validate E.164 format for real SMS
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                editTextPhone.setError("Invalid phone format. Use E.164 format (e.g. +639123456789)");
                editTextPhone.requestFocus();
                return;
            }

            setButtonsEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phone)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                    signInWithPhoneAuthCredential(credential);
                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                    progressBar.setVisibility(View.GONE);
                                    setButtonsEnabled(true);
                                    if (e instanceof FirebaseTooManyRequestsException) {
                                        Toast.makeText(Register.this, "Quota exceeded. Please try again later.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorMsg = "Verification failed";
                                        if (e.getMessage() != null) {
                                            errorMsg += ": " + e.getMessage();
                                        }
                                        Toast.makeText(Register.this, errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCodeSent(@NonNull String id,
                                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                    progressBar.setVisibility(View.GONE);
                                    setButtonsEnabled(true);
                                    verificationId = id;
                                    Toast.makeText(Register.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .build();

            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        // Verify OTP
        buttonVerifyOtp.setOnClickListener(v -> {
            String code = editTextOtp.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();

            if (TextUtils.isEmpty(code)) {
                editTextOtp.setError("Enter OTP code");
                editTextOtp.requestFocus();
                return;
            }

            if (code.length() != OTP_LENGTH) {
                editTextOtp.setError("OTP must be " + OTP_LENGTH + " digits");
                editTextOtp.requestFocus();
                return;
            }

            // ✅ For testing: bypass Firebase check
            if (isTesting && phone.equals(TEST_PHONE) && code.equals(TEST_OTP)) {
                phoneVerified = true;
                Toast.makeText(this, "Phone verified (test mode)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (verificationId == null) {
                Toast.makeText(this, "Please request OTP first", Toast.LENGTH_SHORT).show();
                return;
            }

            setButtonsEnabled(false);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
        });

        // Register with Email + Password
        buttonReg.setOnClickListener(v -> {
            if (!phoneVerified) {
                Toast.makeText(Register.this, "Please verify phone number first", Toast.LENGTH_SHORT).show();
                return;
            }

            String fullName = editTextFullName.getText().toString().trim();
            String username = editTextUsername.getText().toString().trim();
            String address = editTextAddress.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();

            // Validate all fields with specific error messages
            if (!validateInputs(fullName, username, address, email, password, phone)) {
                return;
            }

            setButtonsEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();

                                User newUser = new User(fullName, username, address, email, phone);
                                databaseRef.child(userId).setValue(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Register.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            setButtonsEnabled(true);
                                            String errorMsg = "Failed to save user data";
                                            if (e.getMessage() != null) {
                                                errorMsg += ": " + e.getMessage();
                                            }
                                            Toast.makeText(Register.this, errorMsg, Toast.LENGTH_LONG).show();
                                        });
                            }
                        } else {
                            setButtonsEnabled(true);
                            String errorMessage = "Authentication failed";
                            if (task.getException() != null) {
                                errorMessage += ": " + task.getException().getMessage();
                            }
                            Toast.makeText(Register.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private boolean validateInputs(String fullName, String username, String address,
                                   String email, String password, String phone) {
        boolean isValid = true;

        if (TextUtils.isEmpty(fullName)) {
            editTextFullName.setError("Full name is required");
            editTextFullName.requestFocus();
            isValid = false;
        } else if (fullName.length() < 2) {
            editTextFullName.setError("Full name must be at least 2 characters");
            editTextFullName.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username is required");
            if (isValid) {
                editTextUsername.requestFocus();
                isValid = false;
            }
        } else if (username.length() < 3) {
            editTextUsername.setError("Username must be at least 3 characters");
            if (isValid) {
                editTextUsername.requestFocus();
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(address)) {
            editTextAddress.setError("Address is required");
            if (isValid) {
                editTextAddress.requestFocus();
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            if (isValid) {
                editTextEmail.requestFocus();
                isValid = false;
            }
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email address");
            if (isValid) {
                editTextEmail.requestFocus();
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            if (isValid) {
                editTextPassword.requestFocus();
                isValid = false;
            }
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            editTextPassword.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            if (isValid) {
                editTextPassword.requestFocus();
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Phone number is required");
            if (isValid) {
                editTextPhone.requestFocus();
                isValid = false;
            }
        }

        return isValid;
    }

    private void setButtonsEnabled(boolean enabled) {
        if (buttonReg != null) {
            buttonReg.setEnabled(enabled);
        }
        if (buttonSendOtp != null) {
            buttonSendOtp.setEnabled(enabled);
        }
        if (buttonVerifyOtp != null) {
            buttonVerifyOtp.setEnabled(enabled);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    setButtonsEnabled(true);
                    if (task.isSuccessful()) {
                        phoneVerified = true;
                        Toast.makeText(Register.this, "Phone verified successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = "OTP Verification failed";
                        if (task.getException() != null) {
                            errorMessage += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(Register.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
