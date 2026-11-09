package com.android.tomatoapp;

import com.android.tomatoapp.workprogram.data.ProgressCalculator;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class ProgressCalculatorTest {
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate TRANSPLANT = LocalDate.of(2026, 2, 1);
    private static final LocalDate PLACEMENT = LocalDate.of(2026, 3, 1);
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 1);

    @Test
    public void ongoingPhaseTwoDoesNotStartComplete() {
        ProgressCalculator.Result result = calculate(2);

        assertTrue(result.overall < 100);
        assertTrue(result.stages[1] < 100);
    }

    @Test
    public void ongoingPhaseThreeDoesNotStartComplete() {
        ProgressCalculator.Result result = calculate(3);

        assertTrue(result.overall < 100);
        assertTrue(result.stages[2] < 100);
    }

    @Test
    public void ongoingPhaseFourDoesNotStartComplete() {
        ProgressCalculator.Result result = calculate(4);

        assertTrue(result.overall < 100);
        assertTrue(result.stages[3] < 100);
    }

    private static ProgressCalculator.Result calculate(int startPhase) {
        return ProgressCalculator.calculate(
                START,
                START,
                START.plusDays(4),
                TRANSPLANT,
                null,
                PLACEMENT,
                startPhase,
                "Determinate",
                1.0,
                TODAY,
                Collections.emptySet());
    }
}
