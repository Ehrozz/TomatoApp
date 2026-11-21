package com.android.tomatoapp.financial.data;

import com.google.firebase.database.DataSnapshot;

/**
 * Shared parsing for work-program daily expense logs.
 * Firebase stores whole numbers as Long, so typed Double/Integer reads often return null.
 * Totals are taken once per line item so projected calculator costs are never added here.
 */
public final class DailyExpenseMath {

    private DailyExpenseMath() {}

    public static double asDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof Boolean) return ((Boolean) value) ? 1.0 : 0.0;
        if (value instanceof String) {
            try {
                String trimmed = ((String) value).trim().replace(",", "");
                if (trimmed.isEmpty()) return 0.0;
                return Double.parseDouble(trimmed);
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public static int asInt(Object value) {
        return (int) Math.round(asDouble(value));
    }

    public static boolean asBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        if (value instanceof String) {
            String text = ((String) value).trim();
            if ("true".equalsIgnoreCase(text) || "1".equals(text) || "yes".equalsIgnoreCase(text)) {
                return true;
            }
            if ("false".equalsIgnoreCase(text) || "0".equals(text) || "no".equalsIgnoreCase(text)) {
                return false;
            }
        }
        return fallback;
    }

    public static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /** Sum every date under workPrograms/{id}/dailyExpenses. */
    public static double sumAllDates(DataSnapshot dailyExpensesRoot) {
        if (dailyExpensesRoot == null || !dailyExpensesRoot.exists()) return 0.0;
        double total = 0.0;
        for (DataSnapshot dateNode : dailyExpensesRoot.getChildren()) {
            total += sumDateNode(dateNode);
        }
        return total;
    }

    /** Sum one day's labor, material, equipment, and miscellaneous without overlap. */
    public static double sumDateNode(DataSnapshot dateNode) {
        if (dateNode == null || !dateNode.exists()) return 0.0;
        double total = 0.0;
        total += sumCategory(dateNode.child("labor"), "totalCost");
        total += sumCategory(dateNode.child("material"), "totalCost");
        total += sumEquipment(dateNode.child("equipment"));
        total += sumCategory(dateNode.child("miscellaneous"), "cost");
        return total;
    }

    private static double sumCategory(DataSnapshot category, String costKey) {
        double total = 0.0;
        for (DataSnapshot item : category.getChildren()) {
            total += asDouble(item.child(costKey).getValue());
        }
        return total;
    }

    private static double sumEquipment(DataSnapshot equipment) {
        double total = 0.0;
        for (DataSnapshot item : equipment.getChildren()) {
            if (asBoolean(item.child("isOwned").getValue(), false)) continue;
            double line = asDouble(item.child("totalCost").getValue());
            if (line <= 0) line = asDouble(item.child("cost").getValue());
            total += line;
        }
        return total;
    }

    /** Cost of one material line. Returns -1 when the quantity unit and price unit can't be converted. */
    public static double materialTotal(double qty, String qtyUnit, double price, String priceUnit) {
        double raw = rawMaterialTotal(qty, qtyUnit, price, priceUnit);
        return raw < 0 ? -1 : Math.round(raw * 100.0) / 100.0;
    }

    private static double rawMaterialTotal(double qty, String qtyUnit, double price, String priceUnit) {
        String p = normUnit(priceUnit);
        if (p.isEmpty() || p.equals("total paid")) return price;   // price is the whole purchase
        String q = normUnit(qtyUnit);
        if (q.equals(p)) return qty * price;                       // e.g. 3 sack x price per sack
        double qf = toBase(q), pf = toBase(p);
        if (qf <= 0 || pf <= 0 || family(q) != family(p)) return -1;
        return (qty * qf) * (price / pf);                          // both converted to g or mL
    }

    private static String normUnit(String u) {
        if (u == null) return "";
        switch (u.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "g": case "gram": case "grams": return "g";
            case "kg": case "kilogram": case "kilograms": return "kg";
            case "ml": case "milliliter": case "milliliters": return "ml";
            case "l": case "liter": case "liters": case "litre": case "litres": return "l";
            default: return u.trim().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private static double toBase(String u) {
        switch (u) {
            case "g": case "ml": return 1;
            case "kg": case "l": return 1000;
            default: return -1;   // pack, sack, bottle, pieces have no conversion
        }
    }

    private static int family(String u) {
        switch (u) {
            case "g": case "kg": return 1;    // weight
            case "ml": case "l": return 2;    // volume
            default: return 0;                // count / package
        }
    }
}
