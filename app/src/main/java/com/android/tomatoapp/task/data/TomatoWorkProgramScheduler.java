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
        LocalDate plowStart = input.seedsConfirmedDate;
        LocalDate plowEnd = plowStart == null
                ? null : plowStart.plusDays(Math.max(1, plowDays) - 1);
        LocalDate phase1End = input.transplantDate == null
                ? scheduleThroughDate
                : input.transplantDate.minusDays(1);
        LocalDate sowing = input.sowingDate;
        LocalDate transplant = input.transplantDate;

        List<ScheduledTask> result = new ArrayList<>();
        addMilestones(result, input, viewedDate);
        if (plowStart != null) {
            add(result, "plow", 1, TaskTemplate.Type.MAIN, "Plow the land", plowStart, plowEnd,
                    "seeds-confirmed", 0, input.variety,
                    "Multi-day task. Computed from farm area at " + input.hectaresPerPlowingDay + " ha/day.");
            add(result, "harrow", 1, TaskTemplate.Type.MAIN, "Harrowing", plowEnd, plowEnd,
                    "plow", 0, input.variety, "Complete on the last plowing date.");
        }
        if (sowing != null && (input.seedsConfirmedDate == null || !sowing.isBefore(input.seedsConfirmedDate))) {
            add(result, "sow", 1, TaskTemplate.Type.MAIN, "Sow seeds in seedbed/trays", sowing, sowing,
                    "seeds-confirmed", 0, input.variety, "Sow 0.5-1 cm deep, one seed per hole.");
            add(result, "medium", 1, TaskTemplate.Type.MAINTENANCE, "Prepare growing medium", sowing,
                    sowing.plusDays(6), "sow", 0, input.variety,
                    "Equal parts vermicompost, coir dust, and soil. Alternatives: sterilized compost or sand/soil mix. Sterilize or solarize.");
            add(result, "seed-quantity", 1, TaskTemplate.Type.MAINTENANCE, "Prepare tomato seeds", sowing,
                    sowing, "sow", 0, input.variety, "Prepare 180-200 g of seed per hectare.");
            addRecurring(result, "seedling-water", 1, TaskTemplate.Type.MAINTENANCE,
                    "Water seedlings daily or as needed", sowing.plusDays(1), phase1End, "sow", 1, input.variety,
                    "Starts the day after sowing; never appears before seeds are sown.");
            add(result, "harden", 1, TaskTemplate.Type.NOTE, "Harden seedlings (optional)",
                    sowing.plusDays(18), sowing.plusDays(20),
                    "sow", 0, input.variety, "Optional: 5-7 days before transplanting, gradually reduce water and expose to sunlight.");
            add(result, "overgrown-seedlings", 1, TaskTemplate.Type.NOTE, "Check for overgrown seedlings",
                    sowing.plusDays(31), sowing.plusDays(31), "sow", 0, input.variety,
                    "Seedlings may be overgrown; record transplanting when they are ready.");
        }
        if (plowEnd != null) {
            LocalDate bedsDate = max(scheduleStartDate.plusDays(21), plowEnd);
            add(result, "beds", 1, TaskTemplate.Type.MAIN, "Construct furrows/beds", bedsDate,
                    bedsDate, "plow", 0, input.variety,
                    bedDetails(input.variety));
        }

        if (transplant != null) {
            LocalDate phase2End = transplant.plusDays(27);
            add(result, "transplant", 2, TaskTemplate.Type.MAIN, "Transplant seedlings", transplant, transplant,
                    "sow", 0, input.variety, "Use 25-30 day-old seedlings with 3-5 true leaves. Water immediately after transplanting. " + spacing(input.variety));
            add(result, "mulch", 2, TaskTemplate.Type.MAIN, "Apply mulch", transplant, transplant.plusDays(4),
                    "transplant", 0, input.variety, "Black plastic is the main option; rice hull or coconut husk is optional. Cut one hole per seedling.");
            add(result, "basal-fertilizer", 2, TaskTemplate.Type.MAIN, "Apply basal fertilizer", transplant, transplant,
                    "transplant", 0, input.variety, "14-14-14 or 15-15-15 at 10 g/plant; optional organic fertilizer 0.5-1 kg/hill.");
            add(result, "trellis-install", 3, TaskTemplate.Type.MAIN, "Install trellis/stakes", transplant.plusDays(30),
                    transplant.plusDays(36), "transplant", 0, input.variety, "Install supports about one month after transplanting.");
            addRecurring(result, "trellis-tie", 2, TaskTemplate.Type.MAINTENANCE, "Tie plants to trellis",
                    transplant.plusDays(37), transplant.plusDays(55), "transplant", 7, input.variety,
                    "Weekly ties after supports are installed.");
            add(result, "side-dress-1", 2, TaskTemplate.Type.MAIN, "First side-dressing", transplant.plusDays(10),
                    transplant.plusDays(14), "transplant", 0, input.variety, "10 g Urea + 10 g MOP per plant.");
            addRecurring(result, "water-2", 2, TaskTemplate.Type.MAINTENANCE, "Water plants", transplant,
                    phase2End, "transplant", 2, input.variety, "Every other day.");
            addRecurring(result, "weed-2", 2, TaskTemplate.Type.MAINTENANCE, "Weed", transplant.plusDays(6),
                    phase2End, "transplant", 7, input.variety, "Once per week.");
            addRecurring(result, "monitor-2", 2, TaskTemplate.Type.MAINTENANCE, "Monitor pests and disease",
                    transplant.plusDays(6), phase2End, "transplant", 7, input.variety, "Once per week.");
            addRecurring(result, "prune-2", 2, TaskTemplate.Type.MAINTENANCE, "Prune or remove suckers",
                    transplant.plusDays(6), phase2End, "transplant", 7, input.variety, "Once per week if overcrowded.");
            add(result, "side-dress-2", 3, TaskTemplate.Type.MAIN, "Second side-dressing", transplant.plusDays(21),
                    transplant.plusDays(27), "transplant", 0, input.variety, "10 g Urea + 10 g MOP per plant.");
            add(result, "side-dress-3", 3, TaskTemplate.Type.MAIN, "Third side-dressing", transplant.plusDays(35),
                    transplant.plusDays(41), "transplant", 0, input.variety, "Two weeks after the second side-dressing: 10 g Urea + 10 g MOP per plant.");
            add(result, "hilling", 3, TaskTemplate.Type.MAIN, "Weed and hill-up", transplant.plusDays(25),
                    transplant.plusDays(30), "transplant", 0, input.variety, "Perform at 25-30 DAT.");
            add(result, "side-dress-4", 4, TaskTemplate.Type.MAIN, "Side-dressing at peak flowering/fruit set", transplant.plusDays(56),
                    transplant.plusDays(62), "transplant", 0, input.variety, "At peak flowering/fruit set: 10 g Urea + 10 g MOP per plant.");
            add(result, "trellis-adjust", 4, TaskTemplate.Type.MAIN, "Adjust trellis", transplant.plusDays(56),
                    transplant.plusDays(62), "transplant", 0, input.variety, "Indeterminate/semi-determinate only: replace weak supports and retie.");
            addRecurring(result, "water-3", 3, TaskTemplate.Type.MAINTENANCE, "Water plants", transplant.plusDays(28),
                    transplant.plusDays(55), "transplant", 2, input.variety, "Every other day.");
            addRecurring(result, "monitor-3", 3, TaskTemplate.Type.MAINTENANCE, "Monitor pests and disease",
                    transplant.plusDays(28), transplant.plusDays(55), "transplant", 7, input.variety, "Once per week.");
            addRecurring(result, "prune-3", 3, TaskTemplate.Type.MAINTENANCE, "Prune", transplant.plusDays(28),
                    transplant.plusDays(55), "transplant", 7, input.variety, "Once per week; indeterminate varieties continue trellis pruning.");
            addRecurring(result, "water-4", 4, TaskTemplate.Type.MAINTENANCE, "Water plants", transplant.plusDays(56),
                    transplant.plusDays(83), "transplant", 2, input.variety, "Every other day.");
            addRecurring(result, "monitor-4", 4, TaskTemplate.Type.MAINTENANCE, "Monitor pests and disease",
                    transplant.plusDays(56), transplant.plusDays(83), "transplant", 3, input.variety, "Every 3 days.");
            addRecurring(result, "soil-check", 4, TaskTemplate.Type.MAINTENANCE, "Check mulch and soil",
                    transplant.plusDays(56), transplant.plusDays(83), "transplant", 7, input.variety, "Once per week.");
            addRecurring(result, "prune-4", 4, TaskTemplate.Type.MAINTENANCE, "Prune", transplant.plusDays(60),
                    transplant.plusDays(83), "transplant", 7, input.variety, "Once per week, on the 5th or 6th day only.");
            add(result, "flowering-transition", 4, TaskTemplate.Type.NOTE, "Check flowering and one-time tasks",
                    transplant.plusDays(56), transplant.plusDays(56), "transplant", 0, input.variety,
                    "Transition when flowers are present and all one-time tasks are complete.");
            LocalDate phase5Start = transplant.plusDays(84);
            add(result, "peak-fruiting-feed", 5, TaskTemplate.Type.MAIN, "Side-dress during peak fruiting",
                    phase5Start, phase5Start, "transplant", 0, input.variety, "10 g Urea + 10 g MOP per plant.");
            add(result, "harvest-notes", 5, TaskTemplate.Type.NOTE, "Harvest stages and technique",
                    phase5Start, throughDate, "transplant", 0, input.variety, "Note only; no completion required.");
            add(result, "postharvest-notes", 5, TaskTemplate.Type.NOTE, "Postharvest handling",
                    phase5Start, throughDate, "transplant", 0, input.variety, "Note only; no completion required.");
            addRecurring(result, "water-5", 5, TaskTemplate.Type.MAINTENANCE, "Water plants", phase5Start,
                    throughDate, "transplant", 2, input.variety, "Every 2 days while plants are producing.");
            addRecurring(result, "cleanup-5", 5, TaskTemplate.Type.MAINTENANCE, "Prune and clean up",
                    phase5Start, throughDate, "transplant", 7, input.variety, "Once per week.");
            addRecurring(result, "monitor-5", 5, TaskTemplate.Type.MAINTENANCE, "Monitor pests and disease",
                    phase5Start, throughDate, "transplant", 3, input.variety, "Every 3 days.");
            addRecurring(result, "stop-indicator", 5, TaskTemplate.Type.MILESTONE, "Are plants still alive and producing?",
                    phase5Start, throughDate, "transplant", 14, input.variety,
                    "If the answer is no, end the workprogram.");
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
                input.stopDate, date, input.hectaresPerPlowingDay)
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
        if (input.seedsConfirmedDate != null
                && viewedDate.equals(input.seedsConfirmedDate)) {
            addMilestone(result, "milestone-seeds", 1, "Seeds confirmed", viewedDate,
                    "Seeds were confirmed on this date.");
        }

        if (input.sowingDate == null && input.seedsConfirmedDate != null) {
            LocalDate sowTarget = max(scheduleStartDate.plusDays(NURSERY_READY_DAY - 1),
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
        return "Determinate: 50 x 50 cm. Indeterminate/semi-determinate: 50-75 x 150 cm.";
    }

    private static String bedDetails(String variety) {
        return isTrellised(variety)
                ? "Spacing: 75-100 cm for semi-determinate; 150 cm for indeterminate. Beds 1 m wide and 20 cm high."
                : "Spacing: 75-100 cm for determinate. Beds 1 m wide and 20 cm high.";
    }

    private static boolean isTrellised(String variety) {
        return variety != null && TRELLISED.contains(variety.trim().toLowerCase(java.util.Locale.US));
    }
}
