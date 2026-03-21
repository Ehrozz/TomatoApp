package com.android.tomatoapp.analytics.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for exporting analytics data to PDF.
 */
public class AnalyticsPdfExporter {

    private static final int PAGE_WIDTH = 595; // A4 width in points (8.27 inches at 72 DPI)
    private static final int PAGE_HEIGHT = 842; // A4 height in points (11.69 inches at 72 DPI)
    private static final int MARGIN = 40;
    private static final int LINE_HEIGHT = 20;
    private static final int TITLE_SIZE = 18;
    private static final int HEADER_SIZE = 14;
    private static final int TEXT_SIZE = 10;

    /**
     * Export selected work programs to PDF.
     * @param context Application context
     * @param workPrograms List of work programs to export
     * @param cultivarName Cultivar name (for title)
     * @param includeCharts Whether to include chart images
     * @param barChart Bar chart to capture (can be null)
     * @param lineChart Line chart to capture (can be null)
     * @return File path of created PDF, or null if failed
     */
    public static String exportToPdf(Context context, 
                                     List<WorkProgramEntity> workPrograms,
                                     String cultivarName,
                                     boolean includeCharts,
                                     BarChart barChart,
                                     LineChart lineChart) {
        if (workPrograms == null || workPrograms.isEmpty()) {
            return null;
        }

        PdfDocument document = new PdfDocument();
        TextPaint titlePaint = new TextPaint();
        titlePaint.setTextSize(TITLE_SIZE);
        titlePaint.setFakeBoldText(true);
        titlePaint.setAntiAlias(true);

        TextPaint headerPaint = new TextPaint();
        headerPaint.setTextSize(HEADER_SIZE);
        headerPaint.setFakeBoldText(true);
        headerPaint.setAntiAlias(true);

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setAntiAlias(true);

        int pageNumber = 1;
        int yPosition = MARGIN;

        // Create first page
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Title
        String title = "Analytics Report" + (cultivarName != null ? " - " + cultivarName : "");
        canvas.drawText(title, MARGIN, yPosition, titlePaint);
        yPosition += LINE_HEIGHT * 2;

        // Generation date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateStr = "Generated: " + sdf.format(new Date());
        canvas.drawText(dateStr, MARGIN, yPosition, textPaint);
        yPosition += LINE_HEIGHT * 2;

        // Summary
        String summary = "Total Work Programs: " + workPrograms.size();
        canvas.drawText(summary, MARGIN, yPosition, headerPaint);
        yPosition += LINE_HEIGHT * 2;

        // Table header with proper formatting
        String[] headers = {"Cultivar", "Area (ha)", "Start Date", "Income (₱)", "Expenses (₱)", "Profit (₱)"};
        float[] columnWidths = {100, 70, 90, 100, 100, 100};
        float xPos = MARGIN;
        
        // Draw header background
        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(0xFFE0E0E0);
        canvas.drawRect(MARGIN, yPosition - LINE_HEIGHT, PAGE_WIDTH - MARGIN, yPosition + 5, headerBgPaint);
        
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], xPos + 5, yPosition, headerPaint);
            xPos += columnWidths[i];
        }
        yPosition += LINE_HEIGHT + 5;

        // Draw line under header
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(2);
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
        yPosition += LINE_HEIGHT + 5;

        // Work program rows with proper formatting
        for (WorkProgramEntity e : workPrograms) {
            if (yPosition > PAGE_HEIGHT - MARGIN - 100) {
                // Start new page
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                yPosition = MARGIN;
                
                // Redraw header on new page
                xPos = MARGIN;
                headerBgPaint.setColor(0xFFE0E0E0);
                canvas.drawRect(MARGIN, yPosition - LINE_HEIGHT, PAGE_WIDTH - MARGIN, yPosition + 5, headerBgPaint);
                for (int i = 0; i < headers.length; i++) {
                    canvas.drawText(headers[i], xPos + 5, yPosition, headerPaint);
                    xPos += columnWidths[i];
                }
                yPosition += LINE_HEIGHT + 5;
                canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
                yPosition += LINE_HEIGHT + 5;
            }

            xPos = MARGIN;
            double profit = e.projectedIncome - e.projectedExpenses;
            String[] rowData = {
                e.cultivarName != null ? e.cultivarName : "N/A",
                String.format(Locale.getDefault(), "%.2f", e.areaSize),
                e.startingDate != null ? e.startingDate : "N/A",
                String.format(Locale.getDefault(), "%,.2f", e.projectedIncome),
                String.format(Locale.getDefault(), "%,.2f", e.projectedExpenses),
                String.format(Locale.getDefault(), "%,.2f", profit)
            };

            for (int i = 0; i < rowData.length; i++) {
                String text = rowData[i];
                // Truncate if too long, but preserve numbers
                if (text.length() > 20 && i != 1 && i != 3 && i != 4 && i != 5) {
                    text = text.substring(0, 17) + "...";
                }
                canvas.drawText(text, xPos + 5, yPosition, textPaint);
                xPos += columnWidths[i];
            }
            yPosition += LINE_HEIGHT + 3;
            
            // Draw subtle line between rows
            Paint rowLinePaint = new Paint();
            rowLinePaint.setColor(0xFFF0F0F0);
            rowLinePaint.setStrokeWidth(1);
            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, rowLinePaint);
            yPosition += 3;
        }

        document.finishPage(page);

        // Add charts if requested
        if (includeCharts) {
            if (barChart != null) {
                addChartToPdf(document, barChart, "Income vs Expenses Comparison", pageNumber + 1);
                pageNumber++;
            }
            if (lineChart != null) {
                addChartToPdf(document, lineChart, "Profit Trend", pageNumber + 1);
                pageNumber++;
            }
        }

        // Save PDF
        try {
            File pdfFile = createPdfFile(context, cultivarName);
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.close();
            return pdfFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return null;
        }
    }

    private static void addChartToPdf(PdfDocument document, Object chart, String title, int pageNumber) {
        Bitmap chartBitmap = null;
        try {
            if (chart instanceof BarChart) {
                BarChart barChart = (BarChart) chart;
                // Ensure chart is drawn before capturing
                barChart.setDrawingCacheEnabled(true);
                barChart.buildDrawingCache();
                chartBitmap = barChart.getChartBitmap();
            } else if (chart instanceof LineChart) {
                LineChart lineChart = (LineChart) chart;
                lineChart.setDrawingCacheEnabled(true);
                lineChart.buildDrawingCache();
                chartBitmap = lineChart.getChartBitmap();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (chartBitmap == null || chartBitmap.getWidth() == 0 || chartBitmap.getHeight() == 0) {
            return;
        }

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        TextPaint titlePaint = new TextPaint();
        titlePaint.setTextSize(HEADER_SIZE);
        titlePaint.setFakeBoldText(true);
        titlePaint.setAntiAlias(true);

        int yPos = MARGIN;
        canvas.drawText(title, MARGIN, yPos, titlePaint);
        yPos += LINE_HEIGHT * 2;

        // Scale chart to fit page width
        float scale = (float) (PAGE_WIDTH - 2 * MARGIN) / chartBitmap.getWidth();
        float scaledHeight = chartBitmap.getHeight() * scale;

        if (yPos + scaledHeight > PAGE_HEIGHT - MARGIN) {
            scaledHeight = PAGE_HEIGHT - MARGIN - yPos;
            scale = scaledHeight / chartBitmap.getHeight();
        }

        canvas.save();
        canvas.translate(MARGIN, yPos);
        canvas.scale(scale, scale);
        canvas.drawBitmap(chartBitmap, 0, 0, null);
        canvas.restore();

        document.finishPage(page);
    }

    private static String getPhasesSummary(Context context, WorkProgramEntity e) {
        String phasesJson = e.phasesJson;
        if (phasesJson == null || phasesJson.isEmpty()) {
            if (e.cultivarName != null && e.startingDate != null) {
                phasesJson = WorkProgramDataHelper.calculatePhasesJson(e.cultivarName, e.startingDate);
            }
        }
        
        if (phasesJson == null) {
            return "N/A";
        }
        
        try {
            org.json.JSONObject phases = new org.json.JSONObject(phasesJson);
            // Get phases summary with detections integrated
            return WorkProgramDataHelper.getPhasesActivitySummaryWithDetections(
                    phases, 
                    context, 
                    e.cultivarName, 
                    e.startingDate
            );
        } catch (org.json.JSONException ex) {
            return "N/A";
        }
    }

    private static File createPdfFile(Context context, String cultivarName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Analytics_" + (cultivarName != null ? cultivarName.replaceAll("[^a-zA-Z0-9]", "_") : "All") + "_" + timestamp + ".pdf";
        
        // Try external storage first (Downloads folder)
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        
        // If external storage is not available, use app's external files directory
        if (!downloadsDir.canWrite()) {
            File appDir = context.getExternalFilesDir(null);
            if (appDir != null) {
                return new File(appDir, fileName);
            }
        }
        
        return new File(downloadsDir, fileName);
    }
}

