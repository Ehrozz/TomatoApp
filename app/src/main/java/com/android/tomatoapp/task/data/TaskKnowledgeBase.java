package com.android.tomatoapp.task.data;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class TaskKnowledgeBase {
    public static final String[] REFERENCES = {
            "R1 Philippine Council for Agriculture, Forestry and Natural Resources Research and Development. (n.d.). Tomato production guide (Information Bulletin No. 157). Reproduced by DA Region IV-B (MiMaRoPa).",
            "R2 Pilipinas Kaneko Seeds. (n.d.-a). Production guide (English).",
            "R3 Pilipinas Kaneko Seeds. (n.d.). Tomato varieties.",
            "R4 DA Bureau of Plant Industry. (2025, January 20). Organic tomato seed production.",
            "R5 DA Regional Field Office No. 02. (2018). Tomato production guide.",
            "R6 Agricultural Training Institute Region VII. (n.d.). Techno guide on organic tomato production.",
            "R7 ATI Cordillera Administrative Region. (2022). Tomato production (for urban and home gardening).",
            "R8 Agriculture Monthly. (2023, October 7). Tomato production guide."
    };

    private TaskKnowledgeBase() {}

    public static boolean has(String taskId) {
        return guide(taskId, 0, null, null, null, null) != null;
    }

    public static String guide(String taskId, double areaHa, String growthHabit,
                               LocalDate viewed, LocalDate transplant,
                               LocalDate sowing) {
        if (taskId == null) return null;
        String key = taskId.replaceAll("-\\d{4}-\\d{2}-\\d{2}$", "");

        String why;
        String sources;
        switch (key) {
            case "plow":
                why = "Deep, thorough plowing improves soil conditions for planting and root development.";
                sources = "";
                break;
            case "harrow":
                why = "Harrowing breaks clods and creates a more uniform seedbed.";
                sources = "";
                break;
            case "sow":
                why = "Proper sowing gives seedlings a controlled start before transplanting.";
                sources = "";
                break;
            case "milestone-seeds":
            case "milestone-sowing":
                why = "Recording the seed and sowing stage helps plan transplant timing.";
                sources = "";
                break;
            case "seedling-water":
                why = "Consistent moisture supports seedling establishment without waterlogging.";
                sources = "";
                break;
            case "harden":
                why = "Gradual hardening reduces transplant shock and helps seedlings adjust outdoors.";
                sources = "";
                break;
            case "land-preparation":
                why = "Well-prepared land provides a suitable base for roots and drainage.";
                sources = "";
                break;
            case "land-water":
                why = "Timely irrigation maintains usable soil moisture during land preparation.";
                sources = "";
                break;
            case "beds":
                why = "Raised beds improve drainage and create a defined planting zone.";
                sources = "";
                break;
            case "mulch":
            case "mulch-care-4":
                why = "Mulch conserves soil moisture and suppresses weeds.";
                sources = "";
                break;
            case "transplant":
            case "milestone-transplant":
                why = "Timely transplanting supports recovery, establishment, and early growth.";
                sources = "";
                break;
            case "trellis-install":
            case "trellis-adjust":
                why = "Trellising supports the plant canopy and keeps growth organized.";
                sources = "";
                break;
            case "basal-fertilizer":
                why = "Basal fertilizer supplies nutrients at planting and supports early establishment.";
                sources = "";
                break;
            case "side-dress-1":
            case "side-dress-2":
            case "side-dress-3":
            case "side-dress-4":
                why = "Side-dressing places nutrients where developing plants can use them.";
                sources = "";
                break;
            case "water-2":
            case "water-3":
            case "water-4":
            case "water-5":
                why = "Regular, even watering reduces moisture stress and supports healthy growth.";
                sources = "";
                break;
            case "weed-2":
                why = "Early weed control limits competition for water, nutrients, and light.";
                sources = "";
                break;
            case "monitor-2":
            case "monitor-3":
            case "monitor-4":
            case "monitor-5":
                why = "Regular scanning catches pests and disease early.";
                sources = "";
                break;
            case "prune-2":
            case "prune-3":
            case "prune-4":
                why = "Pruning improves airflow and helps keep the canopy manageable.";
                sources = "";
                break;
            case "hilling":
                why = "Hilling supports the plant base and reduces the exposed root area.";
                sources = "";
                break;
            case "cleanup-5":
                why = "End-of-crop cleanup removes residues that may carry pests or disease forward.";
                sources = "";
                break;
            case "harvest-guidance":
                why = "Careful harvesting reduces damage and helps mature fruit retain quality.";
                sources = "";
                break;
            case "postharvest-handling":
                why = "Prompt, careful handling limits physical damage and quality loss after harvest.";
                sources = "";
                break;
            case "stop-indicator":
                why = "Recording the crop stop point preserves a clear record of the completed program.";
                sources = "";
                break;
            case "phase1-transition":
            case "phase2-transition":
            case "phase3-transition":
            case "phase4-transition":
                why = "Phase transitions align the next work with the crop's current stage.";
                sources = "";
                break;
            default:
                return null;
        }

        StringBuilder result = new StringBuilder("Why: ").append(why);

        if (("sow".equals(key) || "milestone-seeds".equals(key) || "milestone-sowing".equals(key))
                && areaHa > 0) {
            double low = areaHa * 180;
            double high = areaHa * 200;
            result.append("\nEstimate: ").append(formatAmount(low)).append("-")
                    .append(formatAmount(high)).append(" g seeds");
        }

        double[] plantRange = estimatedPlants(areaHa, growthHabit);
        if (plantRange != null
                && ("transplant".equals(key) || "basal-fertilizer".equals(key)
                || "side-dress-1".equals(key) || "side-dress-2".equals(key)
                || "side-dress-3".equals(key) || "side-dress-4".equals(key))) {
            result.append("\nEstimate: ").append(formatAmount(plantRange[0])).append("-")
                    .append(formatAmount(plantRange[1]))
                    .append(" plants per ha, use your actual plant count");
        }

        if (plantRange != null
                && ("basal-fertilizer".equals(key) || "side-dress-1".equals(key)
                || "side-dress-2".equals(key) || "side-dress-3".equals(key)
                || "side-dress-4".equals(key))) {
            double lowKg = plantRange[0] * areaHa * 10.0 / 1000.0;
            double highKg = plantRange[1] * areaHa * 10.0 / 1000.0;
            result.append("\nEstimate: ").append(formatAmount(lowKg)).append("-")
                    .append(formatAmount(highKg))
                    .append(" kg fertilizer per application, use your actual plant count");
        }

        if (key.startsWith("side-dress-") && viewed != null && transplant != null) {
            long dat = ChronoUnit.DAYS.between(transplant, viewed);
            if ("side-dress-1".equals(key)) {
                result.append("\nTiming: Today is DAT ").append(dat).append("; window 10-15");
            } else if ("side-dress-2".equals(key)) {
                result.append("\nTiming: Today is DAT ").append(dat).append("; window 21-27");
            } else if ("side-dress-3".equals(key)) {
                result.append("\nTiming: Today is DAT ").append(dat).append("; window 35-42 (approximate)");
            }
        }

        if (("transplant".equals(key) || "milestone-transplant".equals(key))
                && transplant != null && sowing != null) {
            long days = ChronoUnit.DAYS.between(sowing, transplant);
            result.append("\nTiming: ").append(days)
                    .append(" days since sowing vs 25-30 days; 3-5 true leaves");
        }

        result.append("\nSources: ").append(sources);
        return result.toString();
    }

    private static double[] estimatedPlants(double areaHa, String growthHabit) {
        if (!(areaHa > 0)) return null;
        boolean trellisHabit = growthHabit != null
                && (growthHabit.contains("Semi") || growthHabit.contains("Indeterminate"));
        double lowPerHa;
        double highPerHa;
        if (trellisHabit) {
            lowPerHa = 10000.0 / (1.50 * 0.75);
            highPerHa = 10000.0 / (1.50 * 0.50);
        } else {
            lowPerHa = 10000.0 / (0.50 * 0.50);
            highPerHa = lowPerHa;
        }
        return new double[]{lowPerHa * areaHa, highPerHa * areaHa};
    }

    private static String formatAmount(double value) {
        if (value >= 1000 || value == Math.rint(value)) {
            return String.format(Locale.US, "%,.0f", value);
        }
        return String.format(Locale.US, "%,.2f", value);
    }
}
