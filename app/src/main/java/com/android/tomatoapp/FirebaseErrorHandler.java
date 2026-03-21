package com.android.tomatoapp;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mapping Firebase exceptions to user-friendly error messages.
 */
public class FirebaseErrorHandler {
    
    private static final Map<String, Integer> ERROR_CODE_MAP = new HashMap<>();
    
    static {
        // Authentication errors
        ERROR_CODE_MAP.put("ERROR_INVALID_EMAIL", R.string.error_email_invalid);
        ERROR_CODE_MAP.put("ERROR_WRONG_PASSWORD", R.string.error_login_failed);
        ERROR_CODE_MAP.put("ERROR_USER_NOT_FOUND", R.string.error_account_not_found);
        ERROR_CODE_MAP.put("ERROR_USER_DISABLED", R.string.error_account_not_found);
        ERROR_CODE_MAP.put("ERROR_TOO_MANY_REQUESTS", R.string.error_verification_quota);
        ERROR_CODE_MAP.put("ERROR_OPERATION_NOT_ALLOWED", R.string.error_authentication_failed);
        ERROR_CODE_MAP.put("ERROR_WEAK_PASSWORD", R.string.error_password_weak);
        ERROR_CODE_MAP.put("ERROR_EMAIL_ALREADY_IN_USE", R.string.error_email_taken);
        ERROR_CODE_MAP.put("ERROR_INVALID_CREDENTIAL", R.string.error_login_failed);
        ERROR_CODE_MAP.put("ERROR_NETWORK_REQUEST_FAILED", R.string.error_network);
    }
    
    /**
     * Gets a user-friendly error message for a Firebase exception.
     * @param context The context for accessing string resources
     * @param exception The Firebase exception
     * @return User-friendly error message
     */
    public static String getErrorMessage(Context context, Exception exception) {
        if (exception == null) {
            return context.getString(R.string.error_unknown);
        }
        
        String errorCode = null;
        if (exception instanceof FirebaseAuthException) {
            errorCode = ((FirebaseAuthException) exception).getErrorCode();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorCode = "ERROR_INVALID_CREDENTIAL";
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            errorCode = "ERROR_USER_NOT_FOUND";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            errorCode = "ERROR_EMAIL_ALREADY_IN_USE";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorCode = "ERROR_WEAK_PASSWORD";
        }
        
        if (!TextUtils.isEmpty(errorCode) && ERROR_CODE_MAP.containsKey(errorCode)) {
            return context.getString(ERROR_CODE_MAP.get(errorCode));
        }
        
        // Generic fallback - don't expose technical details
        String message = exception.getMessage();
        if (!TextUtils.isEmpty(message)) {
            // Only use message if it's user-friendly, otherwise use generic error
            if (message.contains("network") || message.contains("Network")) {
                return context.getString(R.string.error_network);
            }
        }
        
        return context.getString(R.string.error_unknown);
    }
}

