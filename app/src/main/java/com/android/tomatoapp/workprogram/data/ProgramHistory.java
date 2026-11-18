package com.android.tomatoapp.workprogram.data;

import java.time.LocalDate;

public final class ProgramHistory {
    private ProgramHistory() {}

    public static boolean isHistory(LocalDate date, LocalDate placementDate) {
        return placementDate != null && date != null && date.isBefore(placementDate);
    }

    public static boolean shouldTreatAsCompleted(LocalDate date, LocalDate placementDate) {
        return isHistory(date, placementDate);
    }

    public static LocalDate effectiveTransplantDate(LocalDate actualTransplant,
                                                 LocalDate placementDate,
                                                 int startPhase) {
    if (placementDate == null || startPhase < 2) return actualTransplant;
    LocalDate derived = placementDate.minusDays((long) (startPhase - 2) * 28);
    // Respect the real transplant date when it is earlier, so phase 1 ends on it.
    if (actualTransplant != null && actualTransplant.isBefore(derived)) return actualTransplant;
    return derived;
}

    public static boolean isMissed(LocalDate date, LocalDate placementDate, LocalDate today,
                                   boolean hasCountableTasks, boolean completed) {
        return date != null && today != null
                && !isHistory(date, placementDate)
                && date.isBefore(today)
                && hasCountableTasks
                && !completed;
    }

    public static boolean isEligibleForMissed(LocalDate date, LocalDate placementDate,
                                              LocalDate today, int phase, int startPhase,
                                              boolean hasCountableTasks) {
        return placementDate != null && today != null
                && !date.isBefore(placementDate)
                && date.isBefore(today)
                && phase >= startPhase
                && hasCountableTasks;
    }

    public static boolean isAccessible(LocalDate date, LocalDate placementDate, LocalDate today,
                                       boolean ongoing) {
        return date != null && today != null
                && (ongoing && !date.isAfter(today) || !ongoing);
    }
}
