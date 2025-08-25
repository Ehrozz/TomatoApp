package com.android.tomatoapp;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;


public class PhaseDecorator {

    private final CalendarDay date;
    private final Drawable background;

    public PhaseDecorator(Context context, CalendarDay date, String phase) {
        this.date = date;

        switch (phase) {
            case "Land & Seed Preparation":
                background = ContextCompat.getDrawable(context, R.drawable.phase1);
                break;
            case "Tressing & Fertilization":
                background = ContextCompat.getDrawable(context, R.drawable.phase2);
                break;
            case "Flower to Yield":
                background = ContextCompat.getDrawable(context, R.drawable.phase3);
                break;
            case "Harvesting":
                background = ContextCompat.getDrawable(context, R.drawable.phase4);
                break;
            case "Post-Harvest":
                background = ContextCompat.getDrawable(context, R.drawable.phase5);
                break;
            default:
                background = null;
        }
    }

    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(date);
    }

    public void decorate(DayViewFacade view) {
        if (background != null) {
            view.setBackgroundDrawable(background);
        }
    }

}
