package com.android.tomatoapp;

/**
 * Helper class for phase-related utilities
 */
public class PhaseHelper {
    
    /**
     * Get full phase name with description
     * Format: "Phase X - Description"
     */
    public static String getPhaseNameWithDescription(int phase) {
        switch (phase) {
            case 1:
                return "Phase 1 - Land and Soil Preparation";
            case 2:
                return "Phase 2 - Transplant and Establishment";
            case 3:
                return "Phase 3 - Vegetative Growth";
            case 4:
                return "Phase 4 - Flowering and Fruit Set";
            case 5:
                return "Phase 5 - Harvest";
            default:
                return "Phase " + phase;
        }
    }
    
    /**
     * Get phase description only
     */
    public static String getPhaseDescription(int phase) {
        switch (phase) {
            case 1:
                return "Land and Soil Preparation";
            case 2:
                return "Transplant and Establishment";
            case 3:
                return "Vegetative Growth";
            case 4:
                return "Flowering and Fruit Set";
            case 5:
                return "Harvest";
            default:
                return "Unknown Phase";
        }
    }
}

