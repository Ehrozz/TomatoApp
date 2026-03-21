package com.android.tomatoapp;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.HashSet;

/**
 * Decorator for marking missed days on the calendar.
 * Supports refreshing without removing other decorators.
 */
public class MissedDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final float radius;
    private final int color;

    public MissedDecorator(HashSet<CalendarDay> dates, Context context) {
        this.dates = new HashSet<>(dates);
        this.radius = 8f; // size of dot
        this.color = ContextCompat.getColor(context, R.color.red); // red
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(radius, color));
    }
}
