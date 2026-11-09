package com.android.tomatoapp;

import com.android.tomatoapp.workprogram.data.IntakeAnswers;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertNull;

public class IntakeAnswersTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 4);

    @Test
    public void phaseTwoAnswersAllowAcceleratedTransplanting() {
        IntakeAnswers answers = baseAnswers();
        answers.transplanted = IntakeAnswers.ONGOING;
        answers.transplantDate = "2026-10-25";

        assertNull(answers.validateDates(2, TODAY));
    }

    @Test
    public void phaseThreeAnswersAllowDelayedTransplanting() {
        IntakeAnswers answers = baseAnswers();
        answers.transplanted = IntakeAnswers.YES;
        answers.flowers = IntakeAnswers.SOME;
        answers.transplantDate = "2026-08-01";

        assertNull(answers.validateDates(3, TODAY));
    }

    @Test
    public void phaseFourAnswersAllowDelayedTransplanting() {
        IntakeAnswers answers = baseAnswers();
        answers.transplanted = IntakeAnswers.YES;
        answers.flowers = IntakeAnswers.ALL;
        answers.transplantDate = "2026-07-01";

        assertNull(answers.validateDates(4, TODAY));
    }

    private static IntakeAnswers baseAnswers() {
        IntakeAnswers answers = new IntakeAnswers();
        answers.seedsConfirmedDate = "2026-06-01";
        answers.sowingDate = "2026-06-05";
        return answers;
    }
}
