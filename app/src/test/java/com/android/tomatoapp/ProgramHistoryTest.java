package com.android.tomatoapp;

import com.android.tomatoapp.workprogram.data.ProgramHistory;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProgramHistoryTest {
    private static final LocalDate PLACEMENT = LocalDate.of(2026, 11, 4);

    @Test
    public void datesBeforePlacementAreCompletedHistory() {
        assertTrue(ProgramHistory.shouldTreatAsCompleted(
                LocalDate.of(2026, 11, 3), PLACEMENT));
        assertFalse(ProgramHistory.shouldTreatAsCompleted(PLACEMENT, PLACEMENT));
    }

    @Test
    public void historicalDatesCannotBeMissed() {
        assertFalse(ProgramHistory.isMissed(
                LocalDate.of(2026, 11, 3), PLACEMENT,
                LocalDate.of(2026, 11, 4), true, false));
    }

    @Test
    public void ongoingDatesThroughTodayAreAccessible() {
        assertTrue(ProgramHistory.isAccessible(
                LocalDate.of(2026, 11, 3), PLACEMENT,
                LocalDate.of(2026, 11, 4), true));
    }

    @Test
    public void onlyPostPlacementPastActiveDatesCanBeMissed() {
        assertTrue(ProgramHistory.isEligibleForMissed(
                LocalDate.of(2026, 11, 4), PLACEMENT,
                LocalDate.of(2026, 11, 5), 2, 2, true));
        assertFalse(ProgramHistory.isEligibleForMissed(
                LocalDate.of(2026, 11, 3), PLACEMENT,
                LocalDate.of(2026, 11, 5), 2, 2, true));
        assertFalse(ProgramHistory.isEligibleForMissed(
                LocalDate.of(2026, 11, 6), PLACEMENT,
                LocalDate.of(2026, 11, 5), 2, 2, true));
    }
}
