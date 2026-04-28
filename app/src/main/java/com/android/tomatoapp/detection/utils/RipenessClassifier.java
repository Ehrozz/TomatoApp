package com.android.tomatoapp.detection.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Lightweight, image-based ripeness/stage estimation for tomato fruits.
 *
 * This is NOT a replacement for a dedicated ripeness model, but it is:
 * - non-static (derived from the captured image)
 * - fast (sampling-based)
 * - robust enough for "ripe vs unripe" and a simple maturity stage label
 */
public final class RipenessClassifier {

    private RipenessClassifier() {}

    public static final class Result {
        public final String ripeness;      // "Ripe" | "Unripe" | "Unknown"
        public final String stage;         // e.g. "Green", "Breaker/Turning", "Red"
        public final float confidence01;   // 0..1

        public Result(String ripeness, String stage, float confidence01) {
            this.ripeness = ripeness;
            this.stage = stage;
            this.confidence01 = clamp01(confidence01);
        }
    }

    /**
     * Estimate ripeness based on HSV color distribution.
     * Assumes fruit roughly centered (common for camera capture UX).
     */
    public static Result classify(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return new Result("Unknown", "Unknown", 0f);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= 0 || h <= 0) return new Result("Unknown", "Unknown", 0f);

        // Focus on center region to reduce background influence.
        int x0 = (int) (w * 0.15f);
        int x1 = (int) (w * 0.85f);
        int y0 = (int) (h * 0.15f);
        int y1 = (int) (h * 0.85f);

        // Sampling step: scale with image size.
        int step = Math.max(2, Math.min(w, h) / 120);

        float redLike = 0f;
        float greenLike = 0f;
        float valid = 0f;

        float[] hsv = new float[3];

        for (int y = y0; y < y1; y += step) {
            for (int x = x0; x < x1; x += step) {
                int c = bitmap.getPixel(x, y);
                int a = Color.alpha(c);
                if (a < 200) continue;

                Color.colorToHSV(c, hsv);
                float hue = hsv[0];        // 0..360
                float sat = hsv[1];        // 0..1
                float val = hsv[2];        // 0..1

                // Ignore low-saturation/low-value pixels (white/gray/black background, glare).
                if (sat < 0.25f || val < 0.20f) continue;

                valid += 1f;

                // Red wraps around: [0..20] U [340..360]
                boolean isRed = (hue <= 20f || hue >= 340f);
                // Green typical: [70..160]
                boolean isGreen = (hue >= 70f && hue <= 160f);

                if (isRed) redLike += 1f;
                if (isGreen) greenLike += 1f;
            }
        }

        if (valid < 50f) {
            return new Result("Unknown", "Unknown", 0.1f);
        }

        float redRatio = redLike / valid;
        float greenRatio = greenLike / valid;

        // Confidence based on separation and signal strength.
        float signal = Math.max(redRatio, greenRatio);
        float separation = Math.abs(redRatio - greenRatio);
        float confidence = clamp01((signal * 0.7f) + (separation * 1.3f));

        // Stage mapping (simple, fruit-focused)
        if (redRatio >= 0.30f && greenRatio <= 0.15f) {
            return new Result("Ripe", "Red", confidence);
        }
        if (greenRatio >= 0.35f && redRatio <= 0.12f) {
            return new Result("Unripe", "Green", confidence);
        }
        if (redRatio >= 0.15f && greenRatio >= 0.15f) {
            // Mixed colors typically indicates breaker/turning stages
            return new Result(redRatio >= greenRatio ? "Ripe" : "Unripe", "Breaker/Turning", confidence);
        }
        if (redRatio >= 0.18f) {
            return new Result("Ripe", "Turning", confidence);
        }
        if (greenRatio >= 0.22f) {
            return new Result("Unripe", "Light Green", confidence);
        }
        return new Result("Unknown", "Unknown", clamp01(confidence * 0.6f));
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}

