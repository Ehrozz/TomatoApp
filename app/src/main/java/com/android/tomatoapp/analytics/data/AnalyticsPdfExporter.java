package com.android.tomatoapp.analytics.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.android.tomatoapp.workprogram.data.WorkProgramDataHelper;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;
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
            ExportFileHelper.publishToDownloads(context, pdfFile);
            return pdfFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return null;
        }
    }

    /**
     * Export disease scan records to PDF: one section per group (program or
     * "Unlinked scans") with a summary line and a table of scans.
     * @return File path of created PDF, or null if failed / nothing to export
     */
    public static String exportDiseaseToPdf(Context context, List<DiseaseExportGroup> groups) {
        if (groups == null || groups.isEmpty()) {
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

        Paint bgPaint = new Paint();
        bgPaint.setColor(0xFFE0E0E0);

        String[] headers = {"Date", "Phase", "Disease", "Confidence"};
        float[] widths = {110, 50, 250, 90};

        int pageNumber = 1;
        PdfDocument.Page page = document.startPage(
                new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
        Canvas canvas = page.getCanvas();
        int y = MARGIN;

        canvas.drawText("Disease Scan Report", MARGIN, y, titlePaint);
        y += LINE_HEIGHT * 2;
        canvas.drawText("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date()), MARGIN, y, textPaint);
        y += LINE_HEIGHT * 2;

        for (DiseaseExportGroup g : groups) {
            if (y > PAGE_HEIGHT - MARGIN - 120) {
                document.finishPage(page);
                pageNumber++;
                page = document.startPage(
                        new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
                canvas = page.getCanvas();
                y = MARGIN;
            }

            canvas.drawText(g.title != null ? g.title : "N/A", MARGIN, y, headerPaint);
            y += LINE_HEIGHT;
            canvas.drawText(String.format(Locale.getDefault(), "Start: %s | Area: %.2f ha",
                    g.startDate != null ? g.startDate : "N/A", g.area), MARGIN, y, textPaint);
            y += LINE_HEIGHT;

            int total = g.rows.size();
            int healthy = g.healthyCount();
            canvas.drawText(String.format(Locale.getDefault(),
                    "Scans: %d (%d healthy, %d infected) | Avg confidence: %.0f%%",
                    total, healthy, total - healthy, g.avgConfidence()), MARGIN, y, textPaint);
            y += LINE_HEIGHT + 5;

            if (total == 0) {
                canvas.drawText("No scans yet", MARGIN, y, textPaint);
                y += LINE_HEIGHT * 2;
                continue;
            }

            // Table header
            float x = MARGIN;
            canvas.drawRect(MARGIN, y - LINE_HEIGHT + 5, PAGE_WIDTH - MARGIN, y + 5, bgPaint);
            for (int i = 0; i < headers.length; i++) {
                canvas.drawText(headers[i], x + 5, y, headerPaint);
                x += widths[i];
            }
            y += LINE_HEIGHT;

            // Rows
            for (DiseaseExportGroup.Row r : g.rows) {
                if (y > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page);
                    pageNumber++;
                    page = document.startPage(
                            new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
                    canvas = page.getCanvas();
                    y = MARGIN;
                }
                String disease = r.disease != null ? r.disease : "Unknown";
                if (disease.length() > 45) {
                    disease = disease.substring(0, 42) + "...";
                }
                String[] cells = {r.date, String.valueOf(r.phase), disease, r.confidence};
                x = MARGIN;
                for (int i = 0; i < cells.length; i++) {
                    canvas.drawText(cells[i] != null ? cells[i] : "", x + 5, y, textPaint);
                    x += widths[i];
                }
                y += LINE_HEIGHT;
            }
            y += LINE_HEIGHT;
        }

        document.finishPage(page);

        try {
            File pdfFile = createPdfFile(context, "Disease");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.close();
            ExportFileHelper.publishToDownloads(context, pdfFile);
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
        
        try {
            return ExportFileHelper.createExportFile(context, fileName);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create export file", e);
        }
    }
}