package com.android.tomatoapp.common.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

public class DonutChartView extends View {
    private Paint paint;
    private RectF rectF;
    private float[] values;
    private int[] colors;
    private float totalValue;

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(50f);
        rectF = new RectF();
    }

    public void setData(float[] values, int[] colors) {
        this.values = values;
        this.colors = colors;
        totalValue = 0;
        for (float value : values) {
            totalValue += value;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        
        // Ensure minimum size
        if (size == 0) {
            size = 200; // Default size in dp
        }
        
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) {
            return;
        }
        
        float centerX = width / 2f;
        float centerY = height / 2f;
        float minDimension = Math.min(width, height);
        float radius = (minDimension / 2f) - 30f; // Leave some padding
        
        // Adjust stroke width based on view size
        paint.setStrokeWidth(Math.max(30f, Math.min(50f, radius * 0.3f)));
        
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Draw background circle (light gray) if no data
        if (values == null || values.length == 0 || totalValue == 0) {
            paint.setColor(0xFFE0E0E0); // Light gray
            canvas.drawArc(rectF, 0, 360, false, paint);
            return;
        }

        float startAngle = -90f; // Start from top
        float sweepAngle;

        for (int i = 0; i < values.length && i < colors.length; i++) {
            if (values[i] > 0) {
                sweepAngle = (values[i] / totalValue) * 360f;
                paint.setColor(colors[i]);
                canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);
                startAngle += sweepAngle;
            }
        }
    }
}

