package com.android.tomatoapp.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to map cultivar names to their image resources.
 * This class provides a centralized way to manage cultivar images.
 * Images are stored in app/src/main/res/drawable/ folder.
 * 
 * Cultivar names must match exactly as defined in cultivarsData array.
 */
public class CultivarImageHelper {
    
    // Map of cultivar names to their image resource IDs
    // Cultivar names match exactly with cultivarsData array in Workprogram.java
    private static final Map<String, Integer> cultivarImageMap = new HashMap<String, Integer>() {{
        // All 33 cultivars - mapped to their corresponding image files
        // Format: put("Exact Cultivar Name from cultivarsData", R.drawable.image_filename);
        
        // Row 1-8: Victory F1 through Super Pope
        put("Victory F1", R.drawable.victory_f1);
        put("HOPE F1", R.drawable.hope_f1);
        put("Maganda F1", R.drawable.maganda_f1);
        put("Malakas F1", R.drawable.malakas_f1);
        put("Rocky 1 F1", R.drawable.rocky_1_f1);
        put("Improved KS Apollo", R.drawable.improved_ks_apollo);
        put("Improved Pope", R.drawable.improved_pope);
        put("Super Pope", R.drawable.super_pope);
        
        // Row 9-11: Maguilas, Maunlad, Mapalad
        put("Maguilas", R.drawable.maguilas);
        put("Maunlad", R.drawable.maunlad);
        put("Mapalad", R.drawable.mapalad);
        
        // Row 12-15: Abiona F1 through Anita F1
        put("Abiona F1", R.drawable.abiona_f1);
        put("Akna F1", R.drawable.akna_f1);
        put("Amari F1", R.drawable.amari_f1);
        put("Anita F1", R.drawable.anita_f1);
        
        // Row 16-21: Colette F1 through Rubellite F1
        put("Colette F1", R.drawable.colette_f1);
        put("Danica F1", R.drawable.danica_f1);
        put("Granger F1", R.drawable.granger_f1);
        put("Janet F1", R.drawable.janet_f1);
        put("Platinum F1", R.drawable.platinum_f1);
        put("Reina F1", R.drawable.reina_f1);
        put("Renata F1", R.drawable.renata_f1);
        put("Rubellite F1", R.drawable.rubellite_f1);
        
        // Row 22-27: TOM-055 F1 through NSIC 1999 Tm09
        put("TOM-055 F1", R.drawable.tom_055_f1);
        put("TOM-262 OP", R.drawable.tom_262_op);
        put("Dalwangan Tm1", R.drawable.dalwangan_tm1);
        put("Dalwangan Tm2", R.drawable.dalwangan_tm2);
        put("NSIC 1999 Tm09", R.drawable.nsic_199_tm09); // Note: Image filename is "nsic_199_tm09" (199 not 1999)
        
        // Row 28-33: Mara through Maxxime
        put("Mara", R.drawable.mara);
        put("AniMax 1", R.drawable.animax_1);
        put("AniMax 2", R.drawable.animax_2);
        put("Golden Globe", R.drawable.golden_globe);
        put("Maxxime", R.drawable.maxxime);
    }};
    
    // Default image resource (fallback when cultivar image is not found)
    private static final int DEFAULT_IMAGE = R.mipmap.ic_logo;
    
    /**
     * Gets the image resource ID for a specific cultivar.
     * 
     * @param cultivarName The name of the cultivar (must match exactly with cultivarsData)
     * @return The image resource ID, or DEFAULT_IMAGE if not found
     */
    public static int getCultivarImageResource(String cultivarName) {
        if (cultivarName == null || cultivarName.isEmpty()) {
            return DEFAULT_IMAGE;
        }
        
        // Try exact match first (most common case)
        Integer imageRes = cultivarImageMap.get(cultivarName);
        if (imageRes != null) {
            return imageRes;
        }
        
        // Try case-insensitive match (for robustness)
        for (Map.Entry<String, Integer> entry : cultivarImageMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(cultivarName)) {
                return entry.getValue();
            }
        }
        
        // Fallback to default logo if no match found
        return DEFAULT_IMAGE;
    }
    
    /**
     * Checks if an image resource exists for a specific cultivar.
     * 
     * @param cultivarName The name of the cultivar
     * @return true if an image resource exists, false otherwise
     */
    public static boolean hasCultivarImage(String cultivarName) {
        if (cultivarName == null || cultivarName.isEmpty()) {
            return false;
        }
        
        if (cultivarImageMap.containsKey(cultivarName)) {
            return true;
        }
        
        // Try case-insensitive match
        for (String key : cultivarImageMap.keySet()) {
            if (key.equalsIgnoreCase(cultivarName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the suggested filename for a cultivar image.
     * Converts cultivar name to a valid Android resource filename.
     * 
     * @param cultivarName The name of the cultivar
     * @return Suggested filename (e.g., "cultivar_victory_f1")
     */
    public static String getSuggestedImageFilename(String cultivarName) {
        if (cultivarName == null || cultivarName.isEmpty()) {
            return "cultivar_default";
        }
        
        // Convert to lowercase and replace spaces/special characters with underscores
        String filename = cultivarName.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "")
                .replaceAll("_+", "_");
        
        return filename; // Return without "cultivar_" prefix since images are already named
    }
    
    /**
     * Gets all available cultivar names that have images.
     * 
     * @return Array of cultivar names that have images mapped
     */
    public static String[] getCultivarsWithImages() {
        return cultivarImageMap.keySet().toArray(new String[0]);
    }
    
    /**
     * Gets the count of cultivars that have images available.
     * 
     * @return Number of cultivars with images
     */
    public static int getCultivarImageCount() {
        return cultivarImageMap.size();
    }
}
