package com.android.tomatoapp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.regex.Pattern;

public class RegisterValidationTest {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 30;
    
    @Test
    public void testUsernamePattern() {
        assertTrue("Valid username", USERNAME_PATTERN.matcher("user123").matches());
        assertTrue("Valid username with underscore", USERNAME_PATTERN.matcher("user_name").matches());
        assertFalse("Invalid username with space", USERNAME_PATTERN.matcher("user name").matches());
        assertFalse("Invalid username with special char", USERNAME_PATTERN.matcher("user@name").matches());
    }
    
    @Test
    public void testUsernameLength() {
        assertTrue("Username too short", "ab".length() < MIN_USERNAME_LENGTH);
        assertTrue("Username too long", "a".repeat(MAX_USERNAME_LENGTH + 1).length() > MAX_USERNAME_LENGTH);
        assertTrue("Valid username length", "user".length() >= MIN_USERNAME_LENGTH && "user".length() <= MAX_USERNAME_LENGTH);
    }
    
    @Test
    public void testPhonePattern() {
        assertTrue("Valid phone", PHONE_PATTERN.matcher("+1234567890").matches());
        assertTrue("Valid international phone", PHONE_PATTERN.matcher("+639123456789").matches());
        assertFalse("Invalid phone without +", PHONE_PATTERN.matcher("1234567890").matches());
        assertFalse("Invalid phone too short", PHONE_PATTERN.matcher("+12345").matches());
    }
    
    @Test
    public void testPasswordStrength() {
        assertTrue("Strong password", isPasswordStrong("Password123"));
        assertFalse("Weak password - no uppercase", isPasswordStrong("password123"));
        assertFalse("Weak password - no lowercase", isPasswordStrong("PASSWORD123"));
        assertFalse("Weak password - no digit", isPasswordStrong("Password"));
    }
    
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
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
}

