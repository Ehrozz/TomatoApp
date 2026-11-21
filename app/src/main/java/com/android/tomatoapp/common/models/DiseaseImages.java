package com.android.tomatoapp.common.models;

import android.content.Context;
import com.android.tomatoapp.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class DiseaseImages {
    private DiseaseImages() {}

    // Different names used in the app -> one canonical name
    private static final HashMap<String, String> ALIASES = new HashMap<>();
    // Canonical name -> existing file names that don't follow the pattern
    private static final HashMap<String, String> BASE_OVERRIDES = new HashMap<>();
    static {
        ALIASES.put("yellow leaf curl", "tomato yellow leaf curl virus");
        ALIASES.put("tomato leaf curl virus", "tomato yellow leaf curl virus");
        BASE_OVERRIDES.put("tomato yellow leaf curl virus", "disease_yellow_leaf_curl");
        BASE_OVERRIDES.put("black leaf mold", "disease_black_leaf_mold");
    }

    // Extra photo angles: file suffix and the caption shown to the user
    public static final String[] ANGLES = {"_leaf", "_fruit", "_stem", "_plant", "_underside", "_closeup", "_damage"};
    public static final String[] LABELS = {"Leaf", "Fruit", "Stem", "Whole plant", "Leaf underside", "Close-up", "Damage"};

    public static final class Slide {
        public final int res;
        public final String label;
        public final boolean placeholder;
        Slide(int res, String label, boolean placeholder) {
            this.res = res; this.label = label; this.placeholder = placeholder;
        }
    }

    /** File name (without extension) of the main photo, e.g. "disease_leaf_mold". */
    static String baseFor(String diseaseName) {
        String key = diseaseName == null ? "" : diseaseName.trim().toLowerCase(Locale.ROOT);
        key = key.replaceAll("\\s*\\(.*\\)", "").trim();      // drop "(scientific name)"
        String canon = ALIASES.get(key);
        if (canon != null) key = canon;
        String fixed = BASE_OVERRIDES.get(key);
        if (fixed != null) return fixed;
        return "disease_" + key.replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    private static int drawableId(Context ctx, String name) {
        return ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
    }

    /** Thumbnail for the list: main photo, or the logo until the photo exists. */
    public static int resFor(Context ctx, String diseaseName) {
        String base = baseFor(diseaseName);
        int id = drawableId(ctx, base);
        if (id != 0) return id;
        for (String angle : ANGLES) {
            id = drawableId(ctx, base + angle);
            if (id != 0) return id;
        }
        return R.mipmap.ic_logo;
    }

    /** All available photos for the detail gallery. Never empty. */
    public static List<Slide> slidesFor(Context ctx, String diseaseName) {
        List<Slide> out = new ArrayList<>();
        String base = baseFor(diseaseName);
        int main = drawableId(ctx, base);
        if (main != 0) out.add(new Slide(main, "Overview", false));
        for (int i = 0; i < ANGLES.length; i++) {
            int id = drawableId(ctx, base + ANGLES[i]);
            if (id != 0) out.add(new Slide(id, LABELS[i], false));
        }
        if (out.isEmpty()) out.add(new Slide(R.mipmap.ic_logo, "Photo coming soon", true));
        return out;
    }
}