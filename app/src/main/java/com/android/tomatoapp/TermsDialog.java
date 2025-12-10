package com.android.tomatoapp;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

public class TermsDialog extends Dialog {
    
    private static final String PREFS_NAME = "TermsPrefs";
    private static final String KEY_TERMS_ACCEPTED = "terms_accepted";
    private static final String KEY_TERMS_VERSION = "terms_version";
    private static final int CURRENT_TERMS_VERSION = 3;
    
    private Context context;
    private OnTermsAcceptedListener listener;
    private String userId;
    
    public interface OnTermsAcceptedListener {
        void onTermsAccepted();
        void onTermsDeclined();
    }
    
    public TermsDialog(@NonNull Context context, OnTermsAcceptedListener listener) {
        this(context, null, listener);
    }
    
    public TermsDialog(@NonNull Context context, String userId, OnTermsAcceptedListener listener) {
        super(context);
        this.context = context;
        this.userId = userId;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_user_agreement);
        setCancelable(false);
        
        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtAgreement = findViewById(R.id.txtAgreement);
        MaterialCheckBox chkAgree = findViewById(R.id.chkAgree);
        MaterialButton btnAccept = findViewById(R.id.btnAccept);
        
        if (txtTitle == null || txtAgreement == null || chkAgree == null || btnAccept == null) {
            dismiss();
            if (listener != null) {
                listener.onTermsDeclined();
            }
            return;
        }
        
        // Set title
        txtTitle.setText(context.getString(R.string.terms_title));
        
        // Set terms text (support HTML formatting)
        String termsText = context.getString(R.string.terms_full_text);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            txtAgreement.setText(Html.fromHtml(termsText.replace("\n", "<br>"), Html.FROM_HTML_MODE_LEGACY));
        } else {
            txtAgreement.setText(Html.fromHtml(termsText.replace("\n", "<br>")));
        }
        
        // Enable/disable accept button based on checkbox
        chkAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAccept.setEnabled(isChecked);
        });
        
        // Handle accept button click
        btnAccept.setOnClickListener(v -> {
            if (chkAgree.isChecked()) {
                saveTermsAcceptance();
                dismiss();
                if (listener != null) {
                    listener.onTermsAccepted();
                }
            }
        });
    }
    
    private void saveTermsAcceptance() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        if (userId != null && !userId.isEmpty()) {
            // Save per user
            editor.putBoolean(KEY_TERMS_ACCEPTED + "_" + userId, true);
            editor.putInt(KEY_TERMS_VERSION + "_" + userId, CURRENT_TERMS_VERSION);
        } else {
            // Legacy: save globally
            editor.putBoolean(KEY_TERMS_ACCEPTED, true);
            editor.putInt(KEY_TERMS_VERSION, CURRENT_TERMS_VERSION);
        }
        editor.apply();
        
        // Also set legacy flag in AppPrefs for backward compatibility
        SharedPreferences appPrefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        appPrefs.edit().putBoolean("UserAgreementAccepted", true).apply();
    }
    
    /**
     * Check if terms have been accepted by a specific user
     * @param context The application context
     * @param userId The user ID to check terms acceptance for
     * @return true if terms have been accepted, false otherwise
     */
    public static boolean areTermsAccepted(Context context, String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String acceptedKey = KEY_TERMS_ACCEPTED + "_" + userId;
        String versionKey = KEY_TERMS_VERSION + "_" + userId;
        
        boolean accepted = prefs.getBoolean(acceptedKey, false);
        int version = prefs.getInt(versionKey, 0);
        
        // If terms version has changed, require re-acceptance
        if (version < CURRENT_TERMS_VERSION) {
            return false;
        }
        
        return accepted;
    }
    
    /**
     * Check if terms have been accepted (legacy method for backwards compatibility)
     * @param context The application context
     * @return true if terms have been accepted, false otherwise
     */
    public static boolean areTermsAccepted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean accepted = prefs.getBoolean(KEY_TERMS_ACCEPTED, false);
        int version = prefs.getInt(KEY_TERMS_VERSION, 0);
        
        // If terms version has changed, require re-acceptance
        if (version < CURRENT_TERMS_VERSION) {
            return false;
        }
        
        return accepted;
    }
    
    /**
     * Reset terms acceptance for a specific user (for testing or if terms are updated)
     * @param context The application context
     * @param userId The user ID to reset terms acceptance for
     */
    public static void resetTermsAcceptance(Context context, String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TERMS_ACCEPTED + "_" + userId);
        editor.remove(KEY_TERMS_VERSION + "_" + userId);
        editor.apply();
    }
    
    /**
     * Reset terms acceptance (for testing or if terms are updated)
     * @param context The application context
     */
    public static void resetTermsAcceptance(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TERMS_ACCEPTED);
        editor.remove(KEY_TERMS_VERSION);
        editor.apply();
    }
}

