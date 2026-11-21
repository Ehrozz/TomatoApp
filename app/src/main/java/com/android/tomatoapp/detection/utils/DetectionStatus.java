package com.android.tomatoapp.detection.utils;

public enum DetectionStatus {
    ACCEPTED("Result Confirmed", true),
    LOW_CONFIDENCE("Not Confident Enough", false),
    AMBIGUOUS("Result Unclear", false),
    LOW_CONFIDENCE_AMBIGUOUS("Result Unclear", false),
    NOT_TOMATO("Not a Tomato", false),
    NO_PEST("No Pest Found", false),
    WRONG_SCAN("Wrong Scan Type", false),
    POOR_IMAGE("Poor Image Quality", false);

    public final String title;
    public final boolean accepted;

    DetectionStatus(String title, boolean accepted) {
        this.title = title;
        this.accepted = accepted;
    }

    /** lead = p1 - p2 (top-1 minus top-2 probability). */
    public static DetectionStatus fromScores(float p1, float p2, float minConfidence, float minLead) {
        boolean confidenceOk = p1 >= minConfidence;
        boolean leadOk = (p1 - p2) >= minLead;
        if (confidenceOk && leadOk) return ACCEPTED;
        if (leadOk) return LOW_CONFIDENCE;
        if (confidenceOk) return AMBIGUOUS;
        return LOW_CONFIDENCE_AMBIGUOUS;
    }
}