package com.android.tomatoapp.analytics.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tomatoapp.R;
import com.android.tomatoapp.analytics.data.AnalyticsPdfExporter;
import com.android.tomatoapp.core.ui.BaseDrawerActivity;
import com.android.tomatoapp.workprogram.data.WorkProgramDataHelper;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;
import com.android.tomatoapp.workprogram.data.WorkProgramRepository;
import com.android.tomatoapp.workprogram.data.WorkProgramSelectionAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Details screen for a specific cultivar showing all work programs with table and chart views.
 */
public class CultivarDetailsActivity extends BaseDrawerActivity {

    private String cultivarName;
    private Spinner viewModeSpinner;
    private LinearLayout spreadsheetRows;
    private BarChart barChart;
    private LineChart lineChart;
    private ProgressBar progressBar;
    private TextView emptyText;
    private TextView cultivarTitle;
    private View chartsContainer;
    private View spreadsheetContainer;

    private WorkProgramRepository repository;
    private final List<WorkProgramEntity> workPrograms = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cultivar_details);

        // Get cultivar name from intent
        Intent intent = getIntent();
        cultivarName = intent.getStringExtra("cultivar_name");
        if (cultivarName == null) {
            finish();
            return;
        }

        setupDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cultivar Details");
        }

        repository = new WorkProgramRepository(this);

        viewModeSpinner = findViewById(R.id.viewModeSpinner);
        spreadsheetRows = findViewById(R.id.spreadsheetRows);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        cultivarTitle = findViewById(R.id.cultivarTitle);
        chartsContainer = findViewById(R.id.chartsContainer);
        spreadsheetContainer = findViewById(R.id.spreadsheetContainer);
        Button btnExportPdf = findViewById(R.id.btnExportPdf);

        cultivarTitle.setText(cultivarName);

        setupViewModeSelector();
        loadData();

        if (btnExportPdf != null) {
            btnExportPdf.setOnClickListener(v -> showExportDialog());
        }
    }

    private void setupViewModeSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Spreadsheet", "Charts"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewModeSpinner.setAdapter(adapter);
        viewModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateViewMode(position == 0 ? "spreadsheet" : "charts");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }


    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        repository.loadAllForCurrentUser(items -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            workPrograms.clear();
            if (items != null) {
                // Filter work programs for this cultivar
                for (WorkProgramEntity e : items) {
                    if (e.cultivarName != null && e.cultivarName.equals(cultivarName)) {
                        workPrograms.add(e);
                    }
                }
            }

            // Sort by date (newest first)
            Collections.sort(workPrograms, (a, b) -> {
                if (a.startingDate == null && b.startingDate == null) return 0;
                if (a.startingDate == null) return 1;
                if (b.startingDate == null) return -1;
                return b.startingDate.compareTo(a.startingDate);
            });

            View emptyStateCard = findViewById(R.id.emptyStateCard);

            if (workPrograms.isEmpty()) {
                if (emptyStateCard != null) {
                    emptyStateCard.setVisibility(View.VISIBLE);
                }
                emptyText.setVisibility(View.VISIBLE);
                if (spreadsheetContainer != null) {
                    spreadsheetContainer.setVisibility(View.GONE);
                }
                chartsContainer.setVisibility(View.GONE);
            } else {
                if (emptyStateCard != null) {
                    emptyStateCard.setVisibility(View.GONE);
                }
                emptyText.setVisibility(View.GONE);
                populateSpreadsheet();
                updateCharts();
            }
        }));
    }

    private void updateViewMode(String mode) {
        if ("charts".equals(mode)) {
            if (spreadsheetContainer != null) {
                spreadsheetContainer.setVisibility(View.GONE);
            }
            if (chartsContainer != null) {
            chartsContainer.setVisibility(View.VISIBLE);
            }
            updateCharts();
        } else {
            if (spreadsheetContainer != null) {
                spreadsheetContainer.setVisibility(View.VISIBLE);
            }
            if (chartsContainer != null) {
            chartsContainer.setVisibility(View.GONE);
        }
        }
    }
    
    private void populateSpreadsheet() {
        if (spreadsheetRows == null) return;
        
        spreadsheetRows.removeAllViews();
        
        for (WorkProgramEntity e : workPrograms) {
            LinearLayout row = createSpreadsheetRow(e);
            spreadsheetRows.addView(row);
        }
    }
    
    private LinearLayout createSpreadsheetRow(WorkProgramEntity e) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        
        // Match header row layout exactly - no margins, no padding
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 0);
        row.setLayoutParams(rowParams);
        row.setPadding(0, 0, 0, 0);
        
        // Alternate row colors for better readability (spreadsheet style)
        boolean isEven = workPrograms.indexOf(e) % 2 == 0;
        int rowColor = isEven ? 
            getResources().getColor(R.color.white, null) : 
            0xFFF5F5F5; // Light gray for alternating rows
        row.setBackgroundColor(rowColor);
        
        // Column 1: Cultivar Name
        row.addView(createCell(e.cultivarName != null ? e.cultivarName : "N/A", 120));
        
        // Column 2: Area Size
        row.addView(createCell(String.format(Locale.getDefault(), "%.2f ha", e.areaSize), 100));
        
        // Column 3: Start Date
        row.addView(createCell(e.startingDate != null ? e.startingDate : "N/A", 110));
        
        // Columns 4-8: Phase dates
        String[] phaseDates = getPhaseDates(e);
        for (int i = 0; i < 5; i++) {
            row.addView(createCell(phaseDates[i], 100));
        }
        
        // Column 9: Detections
        row.addView(createCell(getDetectionsSummaryForEntity(e), 100));
        
        // Column 10: Income
        row.addView(createCell(String.format(Locale.getDefault(), "₱%,.0f", e.projectedIncome), 130, true));
        
        // Column 11: Expenses
        row.addView(createCell(String.format(Locale.getDefault(), "₱%,.0f", e.projectedExpenses), 130, true));
        
        // Column 12: Profit
        double profit = e.projectedIncome - e.projectedExpenses;
        row.addView(createCell(String.format(Locale.getDefault(), "₱%,.0f", profit), 130, true, profit >= 0));
        
        return row;
    }
    
    private TextView createCell(String text, int width) {
        return createCell(text, width, false, false);
    }
    
    private TextView createCell(String text, int width, boolean isFinancial) {
        return createCell(text, width, isFinancial, false);
    }
    
    private TextView createCell(String text, int widthDp, boolean isFinancial, boolean isPositive) {
        TextView cell = new TextView(this);
        cell.setText(text);
        
        // Convert dp to pixels to match XML exactly
        float density = getResources().getDisplayMetrics().density;
        int widthPx = Math.round(widthDp * density);
        int heightPx = Math.round(48 * density);
        
        // Use exact pixel dimensions to match header cells
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPx, heightPx);
        params.setMargins(0, 0, 0, 0);
        cell.setLayoutParams(params);
        
        // Force exact width - no shrinking or expanding
        cell.setMinWidth(widthPx);
        cell.setMaxWidth(widthPx);
        cell.setWidth(widthPx);
        
        // Set alignment based on content type - match header alignment exactly
        if (isFinancial) {
            cell.setGravity(android.view.Gravity.CENTER | android.view.Gravity.CENTER_VERTICAL);
        } else {
            // Left align for text columns to match header
            cell.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
        }
        
        // Match header padding exactly (8dp = 8 * density pixels)
        int paddingPx = Math.round(8 * density);
        cell.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        cell.setTextSize(12);
        cell.setBackgroundResource(R.drawable.spreadsheet_cell_border);
        cell.setSingleLine(true);
        cell.setEllipsize(android.text.TextUtils.TruncateAt.END);
        
        if (isFinancial) {
            if (isPositive) {
                cell.setTextColor(getResources().getColor(R.color.sidebar_dark_green, null));
            } else {
                cell.setTextColor(getResources().getColor(R.color.tomato_red, null));
            }
            cell.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            cell.setTextColor(getResources().getColor(R.color.text_primary, null));
        }
        
        return cell;
    }
    
    private String[] getPhaseDates(WorkProgramEntity e) {
        String[] dates = {"N/A", "N/A", "N/A", "N/A", "N/A"};
        
        String phasesJson = e.phasesJson;
        if (phasesJson == null || phasesJson.isEmpty()) {
            if (e.cultivarName != null && e.startingDate != null) {
                phasesJson = WorkProgramDataHelper.calculatePhasesJson(e.cultivarName, e.startingDate);
            }
        }
        
        if (phasesJson != null) {
            try {
                org.json.JSONObject phases = new org.json.JSONObject(phasesJson);
                for (int i = 1; i <= 5; i++) {
                    String phaseKey = "phase" + i;
                    if (phases.has(phaseKey)) {
                        org.json.JSONObject phase = phases.getJSONObject(phaseKey);
                        if (phase.has("startDate")) {
                            String date = phase.getString("startDate");
                            dates[i - 1] = date != null && date.length() >= 10 ? date.substring(5) : date;
                        }
                    }
                }
            } catch (org.json.JSONException ex) {
                // Keep default N/A values
            }
        }
        
        return dates;
    }
    
    private String getDetectionsSummaryForEntity(WorkProgramEntity e) {
        if (e.startingDate != null) {
            return WorkProgramDataHelper.getDetectionsSummary(this, e.startingDate);
        }
        return "None";
    }

    private void updateCharts() {
        if (workPrograms.isEmpty()) {
            barChart.clear();
            lineChart.clear();
            return;
        }

        // Bar Chart: Income vs Expenses comparison
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        int index = 0;
        for (WorkProgramEntity e : workPrograms) {
            incomeEntries.add(new BarEntry(index, (float) e.projectedIncome));
            expenseEntries.add(new BarEntry(index, (float) e.projectedExpenses));
            labels.add(e.startingDate != null ? e.startingDate.substring(5) : String.valueOf(index + 1));
            index++;
        }

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Income");
        incomeDataSet.setColor(getResources().getColor(R.color.sidebar_dark_green, null));
        incomeDataSet.setValueTextSize(12f);
        incomeDataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(getResources().getColor(R.color.chart_orange, null));
        expenseDataSet.setValueTextSize(12f);
        expenseDataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));

        BarData barData = new BarData(incomeDataSet, expenseDataSet);
        float barWidth = 0.4f;
        barData.setBarWidth(barWidth);
        
        barChart.setData(barData);
        
        // Group bars - call on chart, not on BarData
        float groupSpace = 0.04f;
        float barSpace = 0.02f;
        barChart.groupBars(0f, groupSpace, barSpace);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.invalidate();

        // Line Chart: Profit trend over time
        List<Entry> profitEntries = new ArrayList<>();
        index = 0;
        for (WorkProgramEntity e : workPrograms) {
            double profit = e.projectedIncome - e.projectedExpenses;
            profitEntries.add(new Entry(index, (float) profit));
            index++;
        }

        LineDataSet profitDataSet = new LineDataSet(profitEntries, "Profit");
        int profitColor = getResources().getColor(R.color.tomato_red, null);
        profitDataSet.setColor(profitColor);
        profitDataSet.setValueTextSize(12f);
        profitDataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));
        profitDataSet.setLineWidth(3f);
        profitDataSet.setCircleColor(profitColor);
        profitDataSet.setCircleRadius(5f);
        profitDataSet.setFillColor(profitColor);
        profitDataSet.setDrawFilled(true);
        profitDataSet.setFillAlpha(30);

        LineData lineData = new LineData(profitDataSet);
        lineChart.setData(lineData);
        XAxis lineXAxis = lineChart.getXAxis();
        lineXAxis.setGranularity(1f);
        lineXAxis.setGranularityEnabled(true);
        lineXAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        lineXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineXAxis.setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // --- Adapter for work programs table ---
    class WorkProgramAdapter extends RecyclerView.Adapter<WorkProgramViewHolder> {
        private final List<WorkProgramEntity> items;
        private final android.content.Context context;

        WorkProgramAdapter(List<WorkProgramEntity> items) {
            this.items = items;
            this.context = CultivarDetailsActivity.this;
        }

        @Override
        public WorkProgramViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_work_program_details, parent, false);
            return new WorkProgramViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WorkProgramViewHolder holder, int position) {
            WorkProgramEntity e = items.get(position);
            holder.cultivarName.setText(e.cultivarName != null ? e.cultivarName : "N/A");
            holder.areaSize.setText(String.format(Locale.getDefault(), "%.2f hectare", e.areaSize));
            holder.startingDate.setText(e.startingDate != null ? e.startingDate : "N/A");
            holder.phases.setText(getPhasesSummary(e));
            holder.projectedIncome.setText(String.format(Locale.getDefault(), "₱%,.2f", e.projectedIncome));
            holder.projectedExpenses.setText(String.format(Locale.getDefault(), "₱%,.2f", e.projectedExpenses));
            double profit = e.projectedIncome - e.projectedExpenses;
            holder.profit.setText(String.format(Locale.getDefault(), "₱%,.2f", profit));
        }

        private String getPhasesSummary(WorkProgramEntity e) {
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

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private void showExportDialog() {
        if (workPrograms.isEmpty()) {
            Toast.makeText(this, "No work programs to export", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_work_programs, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.workProgramsRecyclerView);
        Button btnSelectAll = dialogView.findViewById(R.id.btnSelectAll);
        Button btnDeselectAll = dialogView.findViewById(R.id.btnDeselectAll);

        WorkProgramSelectionAdapter adapter = new WorkProgramSelectionAdapter(workPrograms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnSelectAll.setOnClickListener(v -> adapter.selectAll());
        btnDeselectAll.setOnClickListener(v -> adapter.deselectAll());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Export to PDF")
                .setView(dialogView)
                .setPositiveButton("Export", (d, which) -> {
                    List<WorkProgramEntity> selected = adapter.getSelectedPrograms();
                    if (selected.isEmpty()) {
                        Toast.makeText(this, "Please select at least one work program", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    exportToPdf(selected);
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void exportToPdf(List<WorkProgramEntity> selectedPrograms) {
        progressBar.setVisibility(View.VISIBLE);
        
        // Include charts if they're visible
        boolean includeCharts = chartsContainer.getVisibility() == View.VISIBLE;
        
        // Ensure charts are rendered before capturing
        if (includeCharts) {
            barChart.post(() -> {
                lineChart.post(() -> {
                    // Charts are now rendered, proceed with export
                    exportPdfInBackground(selectedPrograms, includeCharts);
                });
            });
        } else {
            exportPdfInBackground(selectedPrograms, false);
        }
    }

    private void exportPdfInBackground(List<WorkProgramEntity> selectedPrograms, boolean includeCharts) {
        new Thread(() -> {
            BarChart chartToExport = includeCharts ? barChart : null;
            LineChart lineChartToExport = includeCharts ? lineChart : null;

            String filePath = AnalyticsPdfExporter.exportToPdf(
                    this,
                    selectedPrograms,
                    cultivarName,
                    includeCharts,
                    chartToExport,
                    lineChartToExport
            );

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (filePath != null) {
                    Toast.makeText(this, "PDF exported: " + filePath, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to export PDF", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    static class WorkProgramViewHolder extends RecyclerView.ViewHolder {
        TextView cultivarName, areaSize, startingDate, phases, projectedIncome, projectedExpenses, profit;

        WorkProgramViewHolder(View itemView) {
            super(itemView);
            cultivarName = itemView.findViewById(R.id.detailsCultivarName);
            areaSize = itemView.findViewById(R.id.detailsAreaSize);
            startingDate = itemView.findViewById(R.id.detailsStartingDate);
            phases = itemView.findViewById(R.id.detailsPhases);
            projectedIncome = itemView.findViewById(R.id.detailsProjectedIncome);
            projectedExpenses = itemView.findViewById(R.id.detailsProjectedExpenses);
            profit = itemView.findViewById(R.id.detailsProfit);
        }
    }
}
