package com.android.tomatoapp.workprogram.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Setup questionnaire answers used to start a work program at the correct phase
 * without deleting expense history for work already done.
 */
public class IntakeAnswers {

    public static final String YES = "Yes";
    public static final String ONGOING = "On going";
    public static final String NOT_YET = "Not yet";
    public static final String NOT_DONE = "Not done";
    public static final String NOT_NEEDED = "Not needed";
    public static final String SOME = "Some plants";
    public static final String ALL = "All plants";
    public static final String FINISH = "Finish";
    public static final String NOT = "Not";
    public static final String NO_FLOWERS = "No flowers yet";

    public String landReady = "";
    public String seedlingReady = "";
    public String transplanted = "";
    public String flowers = "";
    public String fruitsMaturing = "";

    public String plowCount = "";
    public String harrowLastPlowing = "";
    public String madeFurrows = "";
    public String madeBeds = "";

    public String growingMediumReady = "";
    public String sterilizedMedium = "";
    public String seedReady = "";
    public String sownSeeds = "";
    public String seedlingHardening = "";

    public String transplantedAll = "";
    public String laidMulch = "";
    public String trellisOrStakes = "";
    public String basalFertilizer = "";
    public String firstSideDressing = "";

    public String secondSideDressing = "";
    public String thirdSideDressing = "";
    public String removedWeeds = "";
    public String hilledUp = "";

    public String fourthSideDressing = "";
    public String replacedSupports = "";

    public String startedHarvesting = "";

    /** Milestone anchors captured during onboarding; never inferred from today. */
    public String seedsConfirmedDate = "";
    public String sowingDate = "";
    public String transplantDate = "";
    public String stopDate = "";

    public static class Evaluation {
        public boolean canCreate = true;
        public String blockReason = "";
        public int startPhase = 1;
        public final List<Integer> skippedPhases = new ArrayList<>();
        public final List<Integer> catchUpPhases = new ArrayList<>();
        public boolean reachedFloweringQuestions;
        public boolean reachedFruitQuestions;
    }

    public Evaluation evaluate() {
        Evaluation result = new Evaluation();

        if (NOT_YET.equals(landReady) && NOT_YET.equals(seedlingReady)) {
            result.canCreate = false;
            result.blockReason = "Land and seedlings are both not ready yet. Finish at least one before starting a work program.";
            return result;
        }

        boolean bothYes = YES.equals(landReady) && YES.equals(seedlingReady);
        boolean anyNotYet = NOT_YET.equals(landReady) || NOT_YET.equals(seedlingReady);
        boolean anyOngoing = ONGOING.equals(landReady) || ONGOING.equals(seedlingReady);

        if (anyNotYet && !bothYes) {
            result.startPhase = 1;
            fillSkippedAndCatchUp(result);
            return result;
        }

        if (!bothYes && !anyOngoing) {
            result.startPhase = 1;
            fillSkippedAndCatchUp(result);
            return result;
        }

        if (ONGOING.equals(transplanted)) {
            result.startPhase = 2;
            fillSkippedAndCatchUp(result);
            return result;
        }

        if (!YES.equals(transplanted)) {
            result.startPhase = 1;
            fillSkippedAndCatchUp(result);
            return result;
        }

        result.reachedFloweringQuestions = true;
        if (SOME.equals(flowers)) {
            result.startPhase = 3;
            fillSkippedAndCatchUp(result);
            return result;
        }
        if (ALL.equals(flowers)) {
            result.startPhase = 4;
            fillSkippedAndCatchUp(result);
            return result;
        }
        if (FINISH.equals(flowers)) {
            result.reachedFruitQuestions = true;
            if (NOT_YET.equals(fruitsMaturing) || NOT.equals(fruitsMaturing)) {
                result.startPhase = 4;
            } else if (YES.equals(fruitsMaturing)) {
                result.startPhase = 5;
            } else {
                result.canCreate = false;
                result.blockReason = "Please answer whether fruits are starting to mature.";
                return result;
            }
            fillSkippedAndCatchUp(result);
            return result;
        }

        result.startPhase = 2;
        fillSkippedAndCatchUp(result);
        return result;
    }

    public String validateDates(int startPhase, java.time.LocalDate appToday) {
        try {
            java.time.LocalDate seeds = empty(seedsConfirmedDate) ? null : java.time.LocalDate.parse(seedsConfirmedDate);
            java.time.LocalDate sow = empty(sowingDate) ? null : java.time.LocalDate.parse(sowingDate);
            java.time.LocalDate transplant = empty(transplantDate) ? null : java.time.LocalDate.parse(transplantDate);
            if (startPhase >= 2 && (seeds == null || sow == null || transplant == null)) {
                return "Please enter the seeds, sowing and transplant dates.";
            }
            if (seeds != null && sow != null && sow.isBefore(seeds)) {
                return "Dates are out of order. Please adjust.";
            }
            if (sow != null && transplant != null && transplant.isBefore(sow)) {
                return "Dates are out of order. Please adjust.";
            }
            if (appToday != null && transplant != null && transplant.isAfter(appToday)) {
                return "The transplant date is in the future. Please adjust.";
            }
            return null;
        } catch (java.time.format.DateTimeParseException error) {
            return "One of the dates is invalid. Please adjust.";
        }
    }

    private static boolean empty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void fillSkippedAndCatchUp(Evaluation result) {
        result.skippedPhases.clear();
        result.catchUpPhases.clear();
        for (int phase = 1; phase < result.startPhase; phase++) {
            result.skippedPhases.add(phase);
            result.catchUpPhases.add(phase);
        }
        if (result.startPhase >= 1 && hasCompletedWorkInCurrentPhase(result.startPhase)) {
            if (!result.catchUpPhases.contains(result.startPhase)) {
                result.catchUpPhases.add(result.startPhase);
            }
        }
    }

    private boolean hasCompletedWorkInCurrentPhase(int phase) {
        switch (phase) {
            case 1:
                return YES.equals(landReady) || YES.equals(seedlingReady)
                        || YES.equals(madeFurrows) || YES.equals(sownSeeds);
            case 2:
                return YES.equals(transplanted) || YES.equals(transplantedAll) || YES.equals(laidMulch);
            case 3:
                return YES.equals(secondSideDressing) || YES.equals(removedWeeds);
            case 4:
                return YES.equals(fourthSideDressing) || YES.equals(replacedSupports);
            case 5:
                return YES.equals(startedHarvesting);
            default:
                return false;
        }
    }

    public Map<String, Object> toMap(int startPhase) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("landReady", landReady);
        map.put("seedlingReady", seedlingReady);
        map.put("transplanted", transplanted);
        map.put("flowers", flowers);
        map.put("fruitsMaturing", fruitsMaturing);
        map.put("plowCount", plowCount);
        map.put("harrowLastPlowing", harrowLastPlowing);
        map.put("madeFurrows", madeFurrows);
        map.put("madeBeds", madeBeds);
        map.put("growingMediumReady", growingMediumReady);
        map.put("sterilizedMedium", sterilizedMedium);
        map.put("seedReady", seedReady);
        map.put("sownSeeds", sownSeeds);
        map.put("seedlingHardening", seedlingHardening);
        map.put("transplantedAll", transplantedAll);
        map.put("laidMulch", laidMulch);
        map.put("trellisOrStakes", trellisOrStakes);
        map.put("basalFertilizer", basalFertilizer);
        map.put("firstSideDressing", firstSideDressing);
        map.put("secondSideDressing", secondSideDressing);
        map.put("thirdSideDressing", thirdSideDressing);
        map.put("removedWeeds", removedWeeds);
        map.put("hilledUp", hilledUp);
        map.put("fourthSideDressing", fourthSideDressing);
        map.put("replacedSupports", replacedSupports);
        map.put("startedHarvesting", startedHarvesting);
        map.put("seedsConfirmedDate", seedsConfirmedDate);
        map.put("sowingDate", sowingDate);
        map.put("transplantDate", transplantDate);
        map.put("stopDate", stopDate);
        map.put("seedsConfirmedDateSource", seedsConfirmedDate.isEmpty() ? "" : "survey");
        map.put("sowingDateSource", sowingDate.isEmpty() ? "" : "survey");
        map.put("transplantDateSource", transplantDate.isEmpty() ? "" : "survey");
        map.put("startPhase", startPhase);
        return map;
    }
}
