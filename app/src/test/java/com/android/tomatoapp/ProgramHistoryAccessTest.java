package com.android.tomatoapp;

import com.android.tomatoapp.workprogram.data.ProgramHistory;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProgramHistoryAccessTest {
    @Test
    public void currentAndPreviousDatesAreAccessible() {
        LocalDate placement = LocalDate.of(2026, 11, 5);
        LocalDate today = LocalDate.of(2026, 11, 5);

        assertTrue(ProgramHistory.isAccessible(today, placement, today, true));
        assertTrue(ProgramHistory.isAccessible(
                today.minusDays(1), placement, today, true));
    }

    @Test
    public void futureDatesRemainOutsideNormalAccess() {
        LocalDate placement = LocalDate.of(2026, 11, 5);
        LocalDate today = LocalDate.of(2026, 11, 5);

        assertFalse(ProgramHistory.isAccessible(
                today.plusDays(1), placement, today, true));
    }
}
