package com.android.tomatoapp.detection.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DetectionStatusTest {
    @Test public void healthyLeafExample() {
        assertEquals(DetectionStatus.LOW_CONFIDENCE,
                DetectionStatus.fromScores(0.759f, 0.155f, 0.80f, 0.15f));
    }

    @Test public void ambiguous() {
        assertEquals(DetectionStatus.AMBIGUOUS,
                DetectionStatus.fromScores(0.55f, 0.45f, 0.50f, 0.15f));
    }
}
