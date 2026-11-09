package com.android.tomatoapp;

import com.android.tomatoapp.workprogram.data.ProgramHistory;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class ProgramHistoryEffectiveDateTest {
    private static final LocalDate PLACEMENT = LocalDate.of(2026, 11, 5);
    private static final LocalDate ACTUAL = LocalDate.of(2026, 8, 1);

    @Test
    public void phaseTwoStartsAtPlacement() {
        assertEquals(PLACEMENT, ProgramHistory.effectiveTransplantDate(ACTUAL, PLACEMENT, 2));
    }

    @Test
    public void phaseThreeStartsTwentyEightDaysBeforePlacement() {
        assertEquals(PLACEMENT.minusDays(28),
                ProgramHistory.effectiveTransplantDate(ACTUAL, PLACEMENT, 3));
    }

    @Test
    public void phaseOneKeepsActualTransplantDate() {
        assertEquals(ACTUAL, ProgramHistory.effectiveTransplantDate(ACTUAL, PLACEMENT, 1));
    }
}
