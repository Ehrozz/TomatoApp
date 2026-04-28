package com.android.tomatoapp.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.android.tomatoapp.R;
import com.android.tomatoapp.common.ui.dialogs.TermsDialog;
import com.android.tomatoapp.common.utils.PhoneUtils;
import com.android.tomatoapp.core.network.FirebaseErrorHandler;
import com.android.tomatoapp.core.ui.MainActivity;

public class Login extends AppCompatActivity {
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private View buttonLogin;
    private View buttonGoogleSignIn;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView textView;
    private DatabaseReference usersRef;

    // Google Sign-In
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    
    // Identifier resolution
    private static final long IDENTIFIER_RESOLUTION_TIMEOUT_MS = 30_000L; // 30 seconds
    private final List<ValueEventListener> activeListeners = new ArrayList<>();
    private Handler timeoutHandler;
    private boolean isResolvingIdentifier = false;

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Check if terms are accepted before redirecting
            if (!TermsDialog.areTermsAccepted(this, currentUser.getUid())) {
                // Show terms dialog before allowing access
                showTermsDialogBeforeLogin(currentUser.getUid());
            } else {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        cancelIdentifierResolution();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupListeners();
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
            timeoutHandler = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        
        // Initialize views first
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        buttonGoogleSignIn = findViewById(R.id.btn_google_signin);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        textView = findViewById(R.id.registerNow);
        timeoutHandler = new Handler(Looper.getMainLooper());
        
        // Null checks
        if (editTextEmail == null || editTextPassword == null || buttonLogin == null ||
                buttonGoogleSignIn == null || progressBar == null || textView == null) {
            Toast.makeText(this, getString(R.string.info_ui_elements_missing), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        initializeLogin();
    }
    
    private void initializeLogin() {

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google button click
        buttonGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // Register link
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        // Email/Username/Phone login
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        setupLoginListeners();
    }
    
    private void setupLoginListeners() {
        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            setLoginEnabled(false);
            String identifier = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(identifier)) {
                Toast.makeText(Login.this, getString(R.string.error_enter_identifier), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                setLoginEnabled(true);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, getString(R.string.error_enter_password), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                setLoginEnabled(true);
                return;
            }

            resolveEmailForIdentifier(identifier, resolvedEmail -> {
                if (resolvedEmail == null) {
                    progressBar.setVisibility(View.GONE);
                    setLoginEnabled(true);
                    Toast.makeText(Login.this, getString(R.string.error_account_not_found), Toast.LENGTH_SHORT).show();
                } else {
                    signInWithEmail(resolvedEmail, password);
                }
            });
        });
    }

    // --- Google Sign-In methods ---
    private void signInWithGoogle() {
        if (buttonGoogleSignIn != null) {
            buttonGoogleSignIn.setEnabled(false);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Google sign in failed", e);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (buttonGoogleSignIn != null) {
                    buttonGoogleSignIn.setEnabled(true);
                }
                Toast.makeText(this, getString(R.string.error_google_signin_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (buttonGoogleSignIn != null) {
                        buttonGoogleSignIn.setEnabled(true);
                    }
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if terms are accepted before proceeding
                            if (!TermsDialog.areTermsAccepted(this, user.getUid())) {
                                showTermsDialogBeforeLogin(user.getUid());
                            } else {
                                if (user.getEmail() != null) {
                                    Toast.makeText(this, getString(R.string.welcome_user, user.getEmail()), Toast.LENGTH_SHORT).show();
                                }
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        String errorMessage = FirebaseErrorHandler.getErrorMessage(this, task.getException());
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        setLoginEnabled(true);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Check if terms are accepted before proceeding
                                if (!TermsDialog.areTermsAccepted(Login.this, user.getUid())) {
                                    showTermsDialogBeforeLogin(user.getUid());
                                } else {
                                    proceedToMainActivity();
                                }
                            } else {
                                proceedToMainActivity();
                            }
                        } else {
                            String errorMessage = FirebaseErrorHandler.getErrorMessage(Login.this, task.getException());
                            Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void proceedToMainActivity() {
        Toast.makeText(Login.this, getString(R.string.success_login), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void showTermsDialogBeforeLogin(String userId) {
        TermsDialog dialog = new TermsDialog(this, userId, new TermsDialog.OnTermsAcceptedListener() {
            @Override
            public void onTermsAccepted() {
                // Terms accepted, proceed to main activity
                proceedToMainActivity();
            }
            
            @Override
            public void onTermsDeclined() {
                // User must accept terms - sign them out
                mAuth.signOut();
                Toast.makeText(Login.this, getString(R.string.terms_must_accept), Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    private void resolveEmailForIdentifier(String identifier, EmailResolutionCallback callback) {
        if (isResolvingIdentifier) {
            callback.onResult(null);
            return;
        }
        
        isResolvingIdentifier = true;
        startIdentifierResolutionTimeout(callback);
        
        if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            cancelIdentifierResolution();
            callback.onResult(identifier);
            return;
        }

        String trimmed = identifier.trim();
        if (trimmed.isEmpty()) {
            cancelIdentifierResolution();
            callback.onResult(null);
            return;
        }

        if (PhoneUtils.isLikelyPhone(trimmed)) {
            querySequential(new String[]{"phone", "phoneInternational", "phoneLocal"},
                    PhoneUtils.buildCandidates(trimmed),
                    callback);
        } else {
            String normalizedUsername = PhoneUtils.normalizeUsername(trimmed);
            querySequential(new String[]{"usernameLower"}, new String[]{normalizedUsername}, callback);
        }
    }
    
    private void startIdentifierResolutionTimeout(EmailResolutionCallback callback) {
        if (timeoutHandler != null) {
            timeoutHandler.postDelayed(() -> {
                if (isResolvingIdentifier) {
                    cleanupListeners();
                    isResolvingIdentifier = false;
                    callback.onResult(null);
                }
            }, IDENTIFIER_RESOLUTION_TIMEOUT_MS);
        }
    }
    
    private void cancelIdentifierResolution() {
        isResolvingIdentifier = false;
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    private void querySequential(String[] fields, String[] candidates, EmailResolutionCallback callback) {
        queryRecursive(fields, candidates, 0, 0, callback);
    }

    private void queryRecursive(String[] fields,
                                String[] candidates,
                                int fieldIndex,
                                int candidateIndex,
                                EmailResolutionCallback callback) {
        if (fields == null || candidates == null ||
                fieldIndex >= fields.length || candidateIndex >= candidates.length) {
            callback.onResult(null);
            return;
        }

        String field = fields[fieldIndex];
        String value = candidates[candidateIndex];
        if (TextUtils.isEmpty(value)) {
            advance(fields, candidates, fieldIndex, candidateIndex, callback);
            return;
        }

        Query query = usersRef.orderByChild(field).equalTo(value);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isResolvingIdentifier) {
                    return; // Resolution was cancelled
                }
                
                for (DataSnapshot child : snapshot.getChildren()) {
                    String email = child.child("email").getValue(String.class);
                    if (!TextUtils.isEmpty(email)) {
                        cleanupListeners();
                        cancelIdentifierResolution();
                        callback.onResult(email);
                        return;
                    }
                }
                advance(fields, candidates, fieldIndex, candidateIndex, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Login", "Identifier lookup cancelled: " + error.getMessage());
                if (!isResolvingIdentifier) {
                    return;
                }
                advance(fields, candidates, fieldIndex, candidateIndex, callback);
            }
        };
        query.addListenerForSingleValueEvent(listener);
        activeListeners.add(listener);
    }

    private void advance(String[] fields,
                         String[] candidates,
                         int fieldIndex,
                         int candidateIndex,
                         EmailResolutionCallback callback) {
        if (!isResolvingIdentifier) {
            return; // Resolution was cancelled
        }
        
        int nextField = fieldIndex;
        int nextCandidate = candidateIndex + 1;
        if (nextCandidate >= candidates.length) {
            nextField++;
            nextCandidate = 0;
        }
        if (nextField >= fields.length) {
            cleanupListeners();
            cancelIdentifierResolution();
            callback.onResult(null);
        } else {
            queryRecursive(fields, candidates, nextField, nextCandidate, callback);
        }
    }

    private void setLoginEnabled(boolean enabled) {
        if (buttonLogin != null) buttonLogin.setEnabled(enabled);
    }
    
    private void cleanupListeners() {
        for (ValueEventListener listener : activeListeners) {
            if (usersRef != null) {
                usersRef.removeEventListener(listener);
            }
        }
        activeListeners.clear();
    }

    private interface EmailResolutionCallback {
        void onResult(String email);
    }
}
