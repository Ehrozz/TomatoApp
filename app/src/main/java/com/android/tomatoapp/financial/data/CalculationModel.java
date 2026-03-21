package com.android.tomatoapp.financial.data;

public class CalculationModel {
    public double grossIncome;
    public double totalExpenses;
    public double netIncome;
    public double hectare;
    public String dateCreated;

    public CalculationModel() {
        // Default constructor required for Firebase
    }

    public CalculationModel(double grossIncome, double totalExpenses, double netIncome, double hectare, String dateCreated) {
        this.grossIncome = grossIncome;
        this.totalExpenses = totalExpenses;
        this.netIncome = netIncome;
        this.hectare = hectare;
        this.dateCreated = dateCreated;
    }
}

