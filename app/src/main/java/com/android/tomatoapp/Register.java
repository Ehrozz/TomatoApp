package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

    @Override
    public void onStart() {
        super.onStart();
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

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        // Send OTP
        buttonSendOtp.setOnClickListener(v -> {
            String phone = editTextPhone.getText().toString().trim();

            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ For testing, skip sending SMS
            if (isTesting && phone.equals(TEST_PHONE)) {
                Toast.makeText(this, "Test OTP: " + TEST_OTP, Toast.LENGTH_LONG).show();
                return;
            }

            // ✅ Require E.164 format for real SMS
            if (!phone.startsWith("+")) {
                Toast.makeText(this, "Phone must start with + (e.g. +639123456789)", Toast.LENGTH_LONG).show();
                return;
            }

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
                                    if (e instanceof FirebaseTooManyRequestsException) {
                                        Toast.makeText(Register.this, "Quota exceeded.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Register.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCodeSent(@NonNull String id,
                                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                    progressBar.setVisibility(View.GONE);
                                    verificationId = id;
                                    Toast.makeText(Register.this, "OTP sent", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
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

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
        });

        // Register with Email + Password
        buttonReg.setOnClickListener(v -> {
            if (!phoneVerified) {
                Toast.makeText(Register.this, "Please verify phone number first", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            String fullName = editTextFullName.getText().toString().trim();
            String username = editTextUsername.getText().toString().trim();
            String address = editTextAddress.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(username) ||
                    TextUtils.isEmpty(address) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(phone)) {
                Toast.makeText(Register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();

                                User newUser = new User(fullName, username, address, email, phone);
                                databaseRef.child(userId).setValue(newUser);

                                Toast.makeText(Register.this, "Account Created", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(Register.this, "Authentication failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        phoneVerified = true;
                        Toast.makeText(Register.this, "Phone verified", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Register.this, "OTP Verification failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
