package com.android.tomatoapp.financial.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "calculations")
public class CalculationEntity {
    @PrimaryKey
    @NonNull
    public String id;

    public String userId;
    public String programId;
    public double grossIncome;
    public double totalExpenses;
    public double netIncome;
    public double hectare;
    public String dateCreated;
    public String dateSaved;
    public String cultivarName;
    public long lastSynced;

    public CalculationEntity() {
        // Default constructor required for Room
        this.id = "";
    }

    @Ignore
    public CalculationEntity(@NonNull String id, String userId, String programId,
                           double grossIncome, double totalExpenses, double netIncome,
                           double hectare, String dateCreated, String dateSaved,
                           String cultivarName, long lastSynced) {
        this.id = id;
        this.userId = userId;
        this.programId = programId;
        this.grossIncome = grossIncome;
        this.totalExpenses = totalExpenses;
        this.netIncome = netIncome;
        this.hectare = hectare;
        this.dateCreated = dateCreated;
        this.dateSaved = dateSaved;
        this.cultivarName = cultivarName;
        this.lastSynced = lastSynced;
    }
}

