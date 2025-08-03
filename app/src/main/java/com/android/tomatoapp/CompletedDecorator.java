package com.android.tomatoapp;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.HashSet;

/**
 * Decorator for marking completed days on the calendar.
 * Ensures only valid days in the provided set are decorated.
 */
public class CompletedDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final Drawable drawable;

    public CompletedDecorator(HashSet<CalendarDay> dates, Context context) {
        // Defensive copy to avoid accidental modification from outside
        this.dates = new HashSet<>(dates);
        this.drawable = ContextCompat.getDrawable(context, R.drawable.circle_completed);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        // Only decorate if the date is explicitly in the set
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (drawable != null) {
            view.setBackgroundDrawable(drawable);
        }
    }
}
