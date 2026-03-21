package com.android.tomatoapp.common.utils;

import android.content.Context;

import androidx.annotation.DrawableRes;

import java.util.Locale;

/**
 * Temporary placeholder for cultivar-phase reference visuals.
 * Can be replaced later with per-cultivar assets pulled from remote config.
 */
public final class ReferenceImageProvider {

    private ReferenceImageProvider() {}

    public static ReferenceImage getReference(Context context, String cultivar, int phase) {
        String cultivarLabel = cultivar != null ? cultivar : context.getString(R.string.app_name);
        String phaseLabel = phaseLabel(phase);
        String description = buildDescription(context, cultivarLabel, phase);
        @DrawableRes int imageRes = resolveImage(context, cultivar, phase);
        String issueHint = buildIssuesHint(phase);
        String warningHint = buildWarningsHint(phase);
        return new ReferenceImage(imageRes, phaseLabel, description, issueHint, warningHint);
    }

    private static String phaseLabel(int phase) {
        switch (phase) {
            case 1:
                return "Phase 1 · Land & Soil Preparation";
            case 2:
                return "Phase 2 · Vegetative";
            case 3:
                return "Phase 3 · Flowering";
            case 4:
                return "Phase 4 · Maturity";
            case 5:
                return "Phase 5 · Post-harvest";
            default:
                return "Phase";
        }
    }

    private static String buildDescription(Context context, String cultivar, int phase) {
        switch (phase) {
            case 1:
                return cultivar + " seedlings should appear sturdy with deep green leaves. Soil should remain moist but well drained.";
            case 2:
                return "Vegetative stage: focus on balanced foliage growth, even staking, and early pest monitoring.";
            case 3:
                return "Flowering stage: expect visible blossoms. Remove excess suckers and maintain foliar sprays as needed.";
            case 4:
                return "Maturity stage: fruits should be expanding with light blush. Maintain irrigation and nutrient feeds.";
            case 5:
                return "Post-harvest phase: plants are winding down. Collect remaining fruits and clear debris.";
            default:
                return context.getString(R.string.monitor_reference_hint);
        }
    }

    private static String buildIssuesHint(int phase) {
        switch (phase) {
            case 1:
                return "Watch for damping-off, pale leaves, or waterlogging.";
            case 2:
                return "Check for nutrient stress, wilting tips, insect bites.";
            case 3:
                return "Look for flower drop, pest scars, or fungal spots.";
            case 4:
                return "Inspect fruit cracking, uneven ripening, or pest damage.";
            case 5:
                return "Identify leftover diseased foliage or fallen fruits.";
            default:
                return "";
        }
    }

    private static String buildWarningsHint(int phase) {
        switch (phase) {
            case 1:
                return "Keep seedlings under shade net and avoid overwatering.";
            case 2:
                return "Secure trellis supports and maintain mulch coverage.";
            case 3:
                return "Maintain spray intervals and remove diseased tissues.";
            case 4:
                return "Avoid excessive nitrogen and manage irrigation carefully.";
            case 5:
                return "Clean up beds promptly to prevent pest carryover.";
            default:
                return "";
        }
    }

    private static int resolveImage(Context context, String cultivar, int phase) {
        if (cultivar != null) {
            String slug = cultivar.toLowerCase(Locale.getDefault())
                    .replaceAll("[^a-z0-9]", "");
            if (!slug.isEmpty()) {
                String candidate = "monitor_" + slug + "_phase" + phase;
                int resId = context.getResources().getIdentifier(candidate, "drawable", context.getPackageName());
                if (resId != 0) {
                    return resId;
                }
            }
        }
        String defaultName = "monitor_phase" + phase;
        int defaultRes = context.getResources().getIdentifier(defaultName, "drawable", context.getPackageName());
        if (defaultRes != 0) {
            return defaultRes;
        }
        return getPlaceholderImage(phase);
    }

    private static int getPlaceholderImage(int phase) {
        switch (phase) {
            case 1:
                return R.drawable.phase1;
            case 2:
                return R.drawable.phase2;
            case 3:
                return R.drawable.phase3;
            case 4:
                return R.drawable.phase4;
            case 5:
                return R.drawable.phase5;
            default:
                return R.mipmap.ic_logo;
        }
    }

    public static class ReferenceImage {
        public final int imageRes;
        public final String phaseLabel;
        public final String description;
        public final String issueHint;
        public final String warningHint;

        public ReferenceImage(@DrawableRes int imageRes,
                              String phaseLabel,
                              String description,
                              String issueHint,
                              String warningHint) {
            this.imageRes = imageRes;
            this.phaseLabel = phaseLabel;
            this.description = description;
            this.issueHint = issueHint;
            this.warningHint = warningHint;
        }
    }
}

