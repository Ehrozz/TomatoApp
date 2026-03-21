package com.android.tomatoapp.common.managers;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import java.util.HashSet;


/**
 * Decorator for marking completed days on the calendar.
 * Supports refreshing without removing other decorators.
 */
public class CompletedDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final float radius;
    private final int color;

    public CompletedDecorator(HashSet<CalendarDay> dates, Context context) {
        this.dates = new HashSet<>(dates);
        this.radius = 8f;
        this.color = ContextCompat.getColor(context, R.color.green);
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
