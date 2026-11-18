package com.android.tomatoapp.task.data;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates the tomato program from rules and event dates, rather than fixed day numbers.
 */
public final class TomatoWorkProgramScheduler {
    public static final double DEFAULT_HECTARES_PER_PLOWING_DAY = 0.10;
    /** Calendar day number in which the nursery is ready for sowing. */
    public static final int NURSERY_READY_DAY = 7;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Set<String> TRELLISED = new HashSet<>(
            Arrays.asList("indeterminate", "semi-determinate"));

    private TomatoWorkProgramScheduler() { }

    public static final class Input {
        public final LocalDate plantingStartDate;
        public final double farmAreaHectares;
        @Nullable public final String variety;
        @Nullable public final LocalDate seedsConfirmedDate;
        @Nullable public final LocalDate sowingDate;
        @Nullable public final LocalDate transplantDate;
        @Nullable public final LocalDate stopDate;
        @Nullable public final LocalDate today;
        public final double hectaresPerPlowingDay;
        /** Retained for constructor compatibility; missed days never shift biological dates. */
        @Deprecated public final int delayDays;
        @Nullable public final LocalDate p3;
        @Nullable public final LocalDate p4;
        @Nullable public final LocalDate p5;

        public Input(LocalDate plantingStartDate, double farmAreaHectares, String variety,
                     @Nullable LocalDate seedsConfirmedDate, @Nullable LocalDate sowingDate,
                     @Nullable LocalDate transplantDate, @Nullable LocalDate stopDate) {
            this(plantingStartDate, farmAreaHectares, variety, seedsConfirmedDate, sowingDate,
                    transplantDate, stopDate, null, DEFAULT_HECTARES_PER_PLOWING_DAY);
        }

        public Input(LocalDate plantingStartDate, double farmAreaHectares, String variety,
                     @Nullable LocalDate seedsConfirmedDate, @Nullable LocalDate sowingDate,
                     @Nullable LocalDate transplantDate, @Nullable LocalDate stopDate,
                     @Nullable LocalDate today, double hectaresPerPlowingDay) {
            this(plantingStartDate, farmAreaHectares, variety, seedsConfirmedDate, sowingDate,
                    transplantDate, stopDate, today, hectaresPerPlowingDay, 0);
        }

        public Input(LocalDate plantingStartDate, double farmAreaHectares, String variety,
                     @Nullable LocalDate seedsConfirmedDate, @Nullable LocalDate sowingDate,
                     @Nullable LocalDate transplantDate, @Nullable LocalDate stopDate,
                     @Nullable LocalDate today, double hectaresPerPlowingDay, int delayDays) {
            this(plantingStartDate, farmAreaHectares, variety, seedsConfirmedDate, sowingDate,
                    transplantDate, stopDate, today, hectaresPerPlowingDay, delayDays,
                    null, null, null);
        }

        public Input(LocalDate plantingStartDate, double farmAreaHectares, String variety,
                     @Nullable LocalDate seedsConfirmedDate, @Nullable LocalDate sowingDate,
                     @Nullable LocalDate transplantDate, @Nullable LocalDate stopDate,
                     @Nullable LocalDate today, double hectaresPerPlowingDay, int delayDays,
                     @Nullable LocalDate p3, @Nullable LocalDate p4, @Nullable LocalDate p5) {
            if (plantingStartDate == null || farmAreaHectares <= 0 || hectaresPerPlowingDay <= 0) {
                throw new IllegalArgumentException("Planting date, area, and plowing capacity are required");
            }
            this.plantingStartDate = plantingStartDate;
            this.farmAreaHectares = farmAreaHectares;
            this.variety = variety;
            this.seedsConfirmedDate = seedsConfirmedDate;
            this.sowingDate = sowingDate;
            this.transplantDate = transplantDate;
            this.stopDate = stopDate;
            this.today = today;
            this.hectaresPerPlowingDay = hectaresPerPlowingDay;
            this.delayDays = 0;
            this.p3 = p3;
            this.p4 = p4;
            this.p5 = p5;
        }

        public Input(LocalDate plantingStartDate, double farmAreaHectares, String variety,
                     @Nullable LocalDate seedsConfirmedDate, @Nullable LocalDate sowingDate,
                     @Nullable LocalDate transplantDate, @Nullable LocalDate stopDate,
                     double hectaresPerPlowingDay) {
            this(plantingStartDate, farmAreaHectares, variety, seedsConfirmedDate, sowingDate,
                    transplantDate, stopDate, null, hectaresPerPlowingDay);
        }
    }

    public static List<ScheduledTask> generate(Input input) {
        LocalDate end = input.stopDate != null ? input.stopDate
                : input.plantingStartDate.plusDays(365);
        return generate(input, end);
    }

    public static List<ScheduledTask> generate(Input input, LocalDate throughDate) {
        if (throughDate == null || throughDate.isBefore(input.plantingStartDate)) {
            return new ArrayList<>();
        }
        if (input.stopDate != null && throughDate.isAfter(input.stopDate)) {
            throughDate = input.stopDate;
        }
        if (throughDate.isBefore(input.plantingStartDate)) {
            return new ArrayList<>();
        }
        final LocalDate scheduleStartDate = input.plantingStartDate;
        final LocalDate scheduleThroughDate = throughDate;
        final LocalDate viewedDate = input.today == null ? throughDate : input.today;
        int plowDays = (int) Math.ceil(input.farmAreaHectares / input.hectaresPerPlowingDay);
        LocalDate plowStart = scheduleStartDate;
        LocalDate plowEnd = plowStart.plusDays(Math.max(1, plowDays) - 1);
        LocalDate phase1End = input.transplantDate == null
                ? scheduleThroughDate
                : input.transplantDate.minusDays(1);
        LocalDate sowing = input.sowingDate;
        LocalDate transplant = input.transplantDate;

        List<ScheduledTask> result = new ArrayList<>();
        addMilestones(result, input, viewedDate);
        add(result, "plow", 1, TaskTemplate.Type.MAIN, "Plow the land", plowStart, plowEnd,
                "planting-start", 0, input.variety,
                "Duration depends on the size of the farmland (at " + input.hectaresPerPlowingDay
                        + " ha/day). Land preparation starts on the program start date.");
        add(result, "harrow", 1, TaskTemplate.Type.MAIN, "Harrowing", plowEnd, plowEnd,
                "plow", 0, input.variety,
                "On the last plowing, perform harrowing on the same date. Duration depends on the size of the land.");
        boolean sowingUnlocked = sowing != null && input.seedsConfirmedDate != null
                && !sowing.isBefore(input.seedsConfirmedDate);
        if (sowingUnlocked) {
            add(result, "sow", 1, TaskTemplate.Type.MAIN, "Sow seeds in seedbed/trays", sowing, sowing,
                    "seeds-confirmed", 0, input.variety,
                    "Sow seeds at 0.5-1 cm depth in trays or seedbeds, one seed per hole.");
            addRecurring(result, "seedling-water", 1, TaskTemplate.Type.MAINTENANCE,
                    "Water seedlings daily or as needed", sowing.plusDays(1), phase1End, "sow", 1, input.variety,
                    "Water seedlings daily or as needed to maintain moisture.");
            add(result, "harden", 1, TaskTemplate.Type.NOTE, "Seedling hardening (optional best practice)",
                    sowing.plusDays(18), sowing.plusDays(20),
                    "sow", 0, input.variety,
                    "5-7 days before transplanting: gradually reduce water and expose seedlings to sunlight.");
            add(result, "phase1-transition", 1, TaskTemplate.Type.NOTE, "Phase 1 Transition indicator",
                    sowing.plusDays(24), phase1End, "sow", 0, input.variety,
                    "Wait for seedlings to develop 3-5 true leaves before moving to Phase 2.");
        } else {
            add(result, "land-preparation", 1, TaskTemplate.Type.MAIN,
                    "Prepare land and growing area", scheduleStartDate, scheduleStartDate,
                    "planting-start", 0, input.variety,
                    "Prepare the land, beds, furrows, and growing area before sowing.");
            addRecurring(result, "land-water", 1, TaskTemplate.Type.MAINTENANCE,
                    "Maintain soil moisture", scheduleStartDate.plusDays(1), phase1End,
                    "planting-start", 2, input.variety,
                    "Water or maintain soil moisture as needed while the land is being prepared.");
        }
        if (plowEnd != null) {
            LocalDate bedsDate = max(scheduleStartDate.plusDays(21), plowEnd);
            add(result, "beds", 1, TaskTemplate.Type.MAIN, "Construct furrows", bedsDate,
                    bedsDate, "plow", 0, input.variety,
                    bedDetails(input.variety));
        }

        if (transplant != null) {
            LocalDate ph3 = input.p3 != null ? input.p3 : transplant.plusDays(28);
            LocalDate ph4 = input.p4 != null ? input.p4 : transplant.plusDays(56);
            LocalDate ph5 = input.p5 != null ? input.p5 : transplant.plusDays(84);
            LocalDate phase2End = ph3.minusDays(1);
            add(result, "mulch", 2, TaskTemplate.Type.MAIN, "Mulching (3 to 5 days)", transplant, transplant.plusDays(4),
                    "transplant", 0, input.variety,
                    "Main: black plastic mulch to conserve moisture, suppress weeds, and prevent soil splash. Optional: rice hull or coconut husk mulch. Lay mulch immediately after transplanting, cut a hole per seedling, and secure edges with soil.");
            add(result, "transplant", 2, TaskTemplate.Type.MAIN, "Transplanting", transplant, transplant,
                    "sow", 0, input.variety,
                    "Transplant 25-30-day-old seedlings in the morning, late afternoon, or on cloudy days. Spacing: Determinate 50 x 50 cm; Indeterminate/semi-determinate 50-75 x 150 cm. Ensure seedlings have 3-5 true leaves. Water immediately after transplanting to reduce stress.");
            add(result, "trellis-install", 2, TaskTemplate.Type.MAIN, "Trellising / staking (indeterminate/semi-determinate only)", transplant.plusDays(28),
                    transplant.plusDays(35), "transplant", 0, input.variety,
                    "Install supports (bamboo, ipil-ipil posts, or nylon ties) 1 month after transplanting. Tie seedlings gently to prevent stem breakage.");
            add(result, "basal-fertilizer", 2, TaskTemplate.Type.MAIN, "Fertilization / nutrient management (Basal)", transplant, transplant,
                    "transplant", 0, input.variety,
                    "Basal (at transplanting): complete fertilizer 14-14-14 or 15-15-15, 10 g per plant along the furrow. Optional organic: vermicompost or well-decomposed manure, 0.5-1 kg per hill.");
            add(result, "side-dress-1", 2, TaskTemplate.Type.MAIN, "Fertilization: First side-dressing (10-15 DAT)", transplant.plusDays(10),
                    transplant.plusDays(15), "transplant", 0, input.variety,
                    "First side-dressing (10-15 DAT): Urea (46-0-0) 10 g + Muriate of Potash (0-0-60) 10 g per plant.");
            addRecurring(result, "water-2", 2, TaskTemplate.Type.MAINTENANCE, "Water plants", transplant,
                    phase2End, "transplant", 1, input.variety, "Watering: daily or as needed, adjust based on weather.");
            addRecurring(result, "weed-2", 2, TaskTemplate.Type.MAINTENANCE, "Weeding", transplant.plusDays(6),
                    phase2End, "transplant", 7, input.variety, "Weeding: 1 a week.");
            addRecurring(result, "monitor-2", 2, TaskTemplate.Type.MAINTENANCE, "Pest/disease scan",
                    transplant.plusDays(6), phase2End, "transplant", 7, input.variety, "Pest/disease scan: 1 a week.");
            addRecurring(result, "prune-2", 2, TaskTemplate.Type.MAINTENANCE, "Pruning/sucker removal",
                    transplant.plusDays(6), phase2End, "transplant", 7, input.variety, "Pruning/sucker removal: 1 a week (only if overcrowded; remove weak shoots).");
            add(result, "phase2-transition", 2, TaskTemplate.Type.NOTE, "Phase 2 Transition indicator",
                    transplant.plusDays(25), phase2End, "transplant", 0, input.variety,
                    "Plants begin flowering; all one-time activities complete.");

            LocalDate phase3End = ph4.minusDays(1);
            add(result, "side-dress-2", 3, TaskTemplate.Type.MAIN, "Side-dressing / nutrient management (Second side-dressing)", ph3.plusDays(21 - 28),
                    ph3.plusDays(27 - 28), "transplant", 0, input.variety,
                    "Second side-dressing 3-4 weeks after transplanting: Urea (46-0-0) 10 g per plant + Muriate of Potash (0-0-60) 10 g per plant. Optional organic: vermicompost or well-decomposed manure, 0.5-1 kg per plant.");
            add(result, "hilling", 3, TaskTemplate.Type.MAIN, "Weeding / hilling-up", ph3.plusDays(25 - 28),
                    ph3.plusDays(30 - 28), "transplant", 0, input.variety,
                    "Remove weeds manually or with shallow cultivation. Hill up soil around the base of plants 25-30 days after transplanting.");
            add(result, "side-dress-3", 3, TaskTemplate.Type.MAIN, "Side-dressing / nutrient management (Third side-dressing)", ph3.plusDays(35 - 28),
                    ph3.plusDays(42 - 28), "transplant", 0, input.variety,
                    "Third side-dressing 3-4 weeks after second side-dressing: Urea (46-0-0) 10 g per plant + Muriate of Potash (0-0-60) 10 g per plant. Optional organic: vermicompost or well-decomposed manure, 0.5-1 kg per plant.");
            addRecurring(result, "water-3", 3, TaskTemplate.Type.MAINTENANCE, "Water plants", ph3.plusDays(28 - 28),
                    phase3End, "transplant", 1, input.variety, "Watering: daily or as needed, more if hot/dry.");
            addRecurring(result, "monitor-3", 3, TaskTemplate.Type.MAINTENANCE, "Pest/disease scan",
                    ph3.plusDays(28 - 28), phase3End, "transplant", 7, input.variety, "Pest/disease scan: 1 a week; apply biopesticides or chemicals if thresholds are exceeded.");
            addRecurring(result, "prune-3", 3, TaskTemplate.Type.MAINTENANCE, "Pruning", ph3.plusDays(28 - 28),
                    phase3End, "transplant", 7, input.variety, "Pruning: 1 a week; remove lower leaves and side shoots for airflow. Indeterminate varieties continue trellis pruning.");
            add(result, "phase3-transition", 3, TaskTemplate.Type.NOTE, "Phase 3 Transition indicator",
                    transplant.plusDays(50), phase3End, "transplant", 0, input.variety,
                    "Move to Phase 4 when flowers are present and all one-time activities are complete.");

            LocalDate phase4End = ph5.minusDays(1);
            add(result, "side-dress-4", 4, TaskTemplate.Type.MAIN, "Fertilization / nutrient management (Fourth side-dressing)", ph4.plusDays(56 - 56),
                    ph4.plusDays(62 - 56), "transplant", 0, input.variety,
                    "Fourth side-dressing at peak flowering/fruit set, using the same rates: Urea (46-0-0) 10 g per plant + Muriate of Potash (0-0-60) 10 g per plant. Optional organic: vermicompost or well-decomposed manure, 0.5-1 kg per plant.");
            add(result, "trellis-adjust", 4, TaskTemplate.Type.MAIN, "Trellising adjustment (indeterminate/semi-determinate only)", ph4.plusDays(56 - 56),
                    ph4.plusDays(62 - 56), "transplant", 0, input.variety,
                    "Replace weak supports and tie plants properly.");
            addRecurring(result, "water-4", 4, TaskTemplate.Type.MAINTENANCE, "Water plants", ph4.plusDays(56 - 56),
                    phase4End, "transplant", 1, input.variety, "Watering: daily or as needed, more frequent if hot/dry.");
            addRecurring(result, "monitor-4", 4, TaskTemplate.Type.MAINTENANCE, "Pest/disease scan",
                    ph4.plusDays(56 - 56), phase4End, "transplant", 3, input.variety, "Pest/disease scan: every 3 days.");
            addRecurring(result, "mulch-care-4", 4, TaskTemplate.Type.MAINTENANCE, "Mulching / soil care",
                    ph4.plusDays(56 - 56), phase4End, "transplant", 7, input.variety, "Mulching / soil care: 1 a week; maintain mulch.");
            addRecurring(result, "prune-4", 4, TaskTemplate.Type.MAINTENANCE, "Pruning", ph4.plusDays(60 - 56),
                    phase4End, "transplant", 7, input.variety, "Pruning: once a week, on the 5th or 6th day only; remove unnecessary shoots or diseased leaves.");
            add(result, "phase4-transition", 4, TaskTemplate.Type.NOTE, "Phase 4 Transition indicator (week 16)",
                    transplant.plusDays(77), phase4End, "transplant", 0, input.variety,
                    "Flowers continue setting, fruits start maturing; one-time activities completed.");

            LocalDate phase5Start = ph5;
            add(result, "harvest-guidance", 5, TaskTemplate.Type.NOTE, "Harvesting (weekly according to fruit maturity and market)",
                    phase5Start, throughDate, "transplant", 0, input.variety,
                    "Harvesting, weekly according to fruit maturity and market:\n• Mature green: cream streaks at blossom end; suitable for distant markets.\n• Breaker / pink: blossom end turns pinkish/reddish; suitable for nearby markets.\n• Red ripe: fully red; ideal for home consumption or immediate sale.\nTechnique: Harvest early morning or late afternoon to avoid heat stress. Cut or twist fruit carefully. Use baskets, crates, or padded containers to prevent bruising.");
            add(result, "postharvest-handling", 5, TaskTemplate.Type.NOTE, "Postharvest handling (reminder only)",
                    phase5Start, throughDate, "transplant", 0, input.variety,
                    "Sort by size, color, and condition; wash gently, dry, and pack in sturdy crates with cushioning.");
            addRecurring(result, "water-5", 5, TaskTemplate.Type.MAINTENANCE, "Water plants", phase5Start,
                    throughDate, "transplant", 1, input.variety, "Watering: daily or as needed; avoid excess to prevent fruit splitting.");
            addRecurring(result, "cleanup-5", 5, TaskTemplate.Type.MAINTENANCE, "Pruning / cleanup",
                    phase5Start, throughDate, "transplant", 7, input.variety, "Pruning / cleanup: once a week; remove senescent or diseased leaves.");
            addRecurring(result, "monitor-5", 5, TaskTemplate.Type.MAINTENANCE, "Pest/disease scan",
                    phase5Start, throughDate, "transplant", 3, input.variety, "Pest/disease scan: every 3 days; treat as needed.");
            addRecurring(result, "stop-indicator", 5, TaskTemplate.Type.MILESTONE, "Have you harvested or tomato plants dies?",
                    phase5Start, throughDate, "transplant", 14, input.variety,
                    "Check whether harvesting is completed or the plants have reached the end of their lifecycle.");
        }
                if (transplant != null) {
                for (int i = 0; i < result.size(); i++) {
                        ScheduledTask t = result.get(i);
                        if (t.phase < 2) continue;
                        int p = TaskSchedule.getPhaseNumber(
                                input.plantingStartDate, transplant, input.stopDate, t.startDate,
                                input.p3, input.p4, input.p5);
                        if (p >= 2 && p != t.phase) {
                        result.set(i, new ScheduledTask(t.id, p, t.type, t.title, t.startDate,
                                t.endDate, t.trigger, t.frequencyDays, t.variety, t.details));
                        }
                }
                }
        filterVariety(result, input.variety);
        result.removeIf(task -> task.startDate.isAfter(scheduleThroughDate));
        Collections.sort(result, Comparator.comparing((ScheduledTask task) -> task.startDate)
                .thenComparing(task -> task.id));
        return result;
    }

    public static List<TaskModel> getTasksForDate(Input input, LocalDate date) {
        if (date == null) return new ArrayList<>();
        Input viewedInput = input.today == null
                ? new Input(input.plantingStartDate, input.farmAreaHectares, input.variety,
                input.seedsConfirmedDate, input.sowingDate, input.transplantDate,
                input.stopDate, date, input.hectaresPerPlowingDay, 0,
                input.p3, input.p4, input.p5)
                : input;
        List<TaskModel> result = new ArrayList<>();
        int day = (int) ChronoUnit.DAYS.between(viewedInput.plantingStartDate, date) + 1;
        for (ScheduledTask task : generate(viewedInput, date)) {
            if (task.occursOn(date)) result.add(task.toTaskModel(day));
        }
        return result;
    }

    public static String format(LocalDate date) {
        return DATE_FORMAT.format(date);
    }

    private static void add(List<ScheduledTask> result, String id, int phase, TaskTemplate.Type type,
                            String title, LocalDate start, LocalDate end, String trigger,
                            int frequency, String variety, String details) {
        if (start == null || end == null || end.isBefore(start)) return;
        result.add(new ScheduledTask(id + "-" + start, phase, type, title, start, end,
                trigger, frequency, variety, details));
    }

    private static void addMilestones(List<ScheduledTask> result, Input input, LocalDate viewedDate) {
        if (viewedDate == null || viewedDate.isBefore(input.plantingStartDate)
                || (input.stopDate != null && viewedDate.isAfter(input.stopDate))) return;
        LocalDate scheduleStartDate = input.plantingStartDate;
        int dayNumber = (int) ChronoUnit.DAYS.between(scheduleStartDate, viewedDate) + 1;
        if (input.seedsConfirmedDate == null) {
            addMilestone(result, "milestone-seeds", 1, "Confirm seeds", viewedDate,
                    "Confirm that seeds are available before recording sowing.");
        } else if (viewedDate.equals(input.seedsConfirmedDate)) {
            addMilestone(result, "milestone-seeds", 1, "Seeds confirmed", viewedDate,
                    "Seeds were confirmed on this date.");
        }

        if (input.sowingDate == null && input.seedsConfirmedDate != null) {
            LocalDate sowTarget = max(scheduleStartDate.plusDays(NURSERY_READY_DAY),
                    input.seedsConfirmedDate.plusDays(1));
            if (viewedDate.equals(sowTarget)
                    || (viewedDate.equals(input.today) && viewedDate.isAfter(sowTarget))) {
                addMilestone(result, "milestone-sowing", 1, "Sow seeds in seedbed/trays",
                        viewedDate, viewedDate.equals(sowTarget)
                                ? "Confirm sowing completed today."
                                : "Overdue since " + sowTarget + ".");
            }
        } else if (input.sowingDate != null && viewedDate.equals(input.sowingDate)) {
            addMilestone(result, "milestone-sowing", 1, "Sowing confirmed", viewedDate,
                    "Seeds were sown on this date.");
        }

        if (input.sowingDate != null && input.transplantDate == null) {
            LocalDate transplantTarget = input.sowingDate.plusDays(25);
            if (viewedDate.equals(transplantTarget)
                    || (viewedDate.equals(input.today) && viewedDate.isAfter(transplantTarget))) {
                addMilestone(result, "milestone-transplant", 1, "Transplant seedlings",
                        viewedDate, viewedDate.equals(transplantTarget)
                                ? "Seedlings should be 25-30 days old with 3-5 true leaves."
                                : "Overdue since " + transplantTarget + ". Seedlings may be overgrown.");
            }
        } else if (input.transplantDate != null && viewedDate.equals(input.transplantDate)) {
            addMilestone(result, "milestone-transplant", 2, "Transplanting confirmed", viewedDate,
                    "Transplanting was recorded on this date.");
        }
    }

    private static void addMilestone(List<ScheduledTask> result, String id, int phase,
                                     String title, LocalDate date, String details) {
        result.add(new ScheduledTask(id, phase, TaskTemplate.Type.MILESTONE, title, date, date,
                null, 0, null, details));
    }

    private static void addRecurring(List<ScheduledTask> result, String id, int phase,
                                     TaskTemplate.Type type, String title, LocalDate start,
                                     LocalDate end, String trigger, int frequency,
                                     String variety, String details) {
        if (frequency <= 0) {
            add(result, id, phase, type, title, start, end, trigger, frequency, variety, details);
            return;
        }
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(frequency)) {
            if (type == TaskTemplate.Type.MILESTONE) {
                result.add(new ScheduledTask(id, phase, type, title, date, date,
                        trigger, frequency, variety, details));
            } else {
                add(result, id, phase, type, title, date, date, trigger, frequency, variety, details);
            }
        }
    }

    private static void filterVariety(List<ScheduledTask> tasks, String variety) {
        if (isTrellised(variety)) return;
        tasks.removeIf(task -> task.id.startsWith("trellis-"));
    }

    private static LocalDate max(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }

    private static String spacing(String variety) {
        return "Spacing: Determinate 50 x 50 cm; Indeterminate/semi-determinate 50-75 x 150 cm.";
    }

    private static String bedDetails(String variety) {
        return isTrellised(variety)
                ? "Beds should be 1 m wide and 20 cm high. 75-100 cm spacing for semi-determinate; 150 cm spacing for indeterminate varieties."
                : "Beds should be 1 m wide and 20 cm high. 75-100 cm spacing for determinate varieties.";
    }

    private static boolean isTrellised(String variety) {
        return variety != null && TRELLISED.contains(variety.trim().toLowerCase(java.util.Locale.US));
    }
}
