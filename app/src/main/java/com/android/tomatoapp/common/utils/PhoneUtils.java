package com.android.tomatoapp.common.utils;

import android.text.TextUtils;

import java.util.LinkedHashSet;
import java.util.Locale;

/**
 * Utility helpers for normalizing user identifiers like phone numbers and usernames.
 */
public final class PhoneUtils {

    private PhoneUtils() { }

    public static String sanitize(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "";
        if (trimmed.startsWith("+")) {
            return "+" + trimmed.substring(1).replaceAll("[^0-9]", "");
        }
        return trimmed.replaceAll("[^0-9]", "");
    }

    public static String normalizeE164(String sanitized) {
        if (TextUtils.isEmpty(sanitized)) return "";
        if (sanitized.startsWith("+")) {
            return sanitized;
        }
        if (sanitized.startsWith("63")) {
            return "+" + sanitized;
        }
        if (sanitized.startsWith("0") && sanitized.length() >= 10) {
            return "+63" + sanitized.substring(1);
        }
        return "+" + sanitized;
    }

    public static String deriveLocal(String international, String fallbackSanitized) {
        if (!TextUtils.isEmpty(international) && international.startsWith("+63") && international.length() > 3) {
            return "0" + international.substring(3);
        }
        return fallbackSanitized;
    }

    public static boolean isLikelyPhone(String identifier) {
        String trimmed = identifier == null ? "" : identifier.trim();
        return trimmed.startsWith("+") || trimmed.matches("^[0-9]{6,}$");
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    public static String[] buildCandidates(String rawPhone) {
        String sanitized = sanitize(rawPhone);
        String international = normalizeE164(sanitized);
        String local = deriveLocal(international, sanitized);

        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        if (!TextUtils.isEmpty(international)) {
            candidates.add(international);
        }
        if (!TextUtils.isEmpty(sanitized)) {
            candidates.add(sanitized);
        }
        if (!TextUtils.isEmpty(local)) {
            candidates.add(local);
        }
        if (!TextUtils.isEmpty(rawPhone)) {
            candidates.add(rawPhone.trim());
        }
        candidates.remove(null);
        return candidates.toArray(new String[0]);
    }
}

