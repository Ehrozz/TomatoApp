package com.android.tomatoapp;

public class WorkLogModel {
    public String cultivar;
    public String date;
    public int dayNumber;
    public String status;

    public WorkLogModel() {
        // Required for Firebase
    }

    public WorkLogModel(String cultivar, String date, int dayNumber, String status) {
        this.cultivar = cultivar;
        this.date = date;
        this.dayNumber = dayNumber;
        this.status = status;
    }
}
