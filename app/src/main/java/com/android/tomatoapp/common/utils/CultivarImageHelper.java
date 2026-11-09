package com.android.tomatoapp.common.utils;

import java.util.HashMap;
import java.util.Map;
import com.android.tomatoapp.R;

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
        put("Improved KS Apollo", R.drawable.improved_ks_apollo);
        put("Diamante Max", R.mipmap.ic_logo); // TODO: Add actual image when available
        put("Jewel F1", R.mipmap.ic_logo); // TODO: Add actual image when available
        put("Marimar", R.mipmap.ic_logo); // TODO: Add actual image when available
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
