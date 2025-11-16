package com.android.tomatoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Analytics screen showing cultivar productivity in table and chart form.
 */
public class AnalyticsActivity extends AppCompatActivity {

    private Spinner viewModeSpinner;
    private Spinner cultivarFilterSpinner;
    private RecyclerView tableRecyclerView;
    private BarChart barChart;
    private LineChart completionChart;
    private ProgressBar progressBar;
    private TextView emptyText;

    private WorkProgramRepository repository;
    private AnalyticsManager analyticsManager;

    private final List<WorkProgramEntity> allPrograms = new ArrayList<>();
    private final List<AnalyticsManager.CultivarSummary> summaries = new ArrayList<>();
    private ExpandableCultivarAdapter expandableAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cultivar Analytics");
        }

        repository = new WorkProgramRepository(this);
        analyticsManager = new AnalyticsManager();

        viewModeSpinner = findViewById(R.id.viewModeSpinner);
        cultivarFilterSpinner = findViewById(R.id.cultivarFilterSpinner);
        tableRecyclerView = findViewById(R.id.tableRecyclerView);
        barChart = findViewById(R.id.barChart);
        completionChart = findViewById(R.id.completionChart);
        progressBar = findViewById(R.id.analyticsProgress);
        emptyText = findViewById(R.id.emptyText);
        Button btnExportPdf = findViewById(R.id.btnExportPdf);

        setupViewModeSelector();
        setupRecyclerView();
        loadData();

        if (btnExportPdf != null) {
            btnExportPdf.setOnClickListener(v -> showExportDialog());
        }
    }

    private void setupViewModeSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Table", "Chart"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewModeSpinner.setAdapter(adapter);
        viewModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateViewMode(position == 0 ? "table" : "chart");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupRecyclerView() {
        tableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expandableAdapter = new ExpandableCultivarAdapter(summaries, allPrograms);
        tableRecyclerView.setAdapter(expandableAdapter);
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        repository.loadAllForCurrentUser(items -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            allPrograms.clear();
            if (items != null) {
                allPrograms.addAll(items);
            }

            if (allPrograms.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                tableRecyclerView.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                buildCultivarFilter();
                updateSummaries();
            }
        }));
    }

    private void buildCultivarFilter() {
        List<String> cultivars = new ArrayList<>();
        cultivars.add("All cultivars");
        for (WorkProgramEntity e : allPrograms) {
            if (e.cultivarName != null && !cultivars.contains(e.cultivarName)) {
                cultivars.add(e.cultivarName);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cultivars
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cultivarFilterSpinner.setAdapter(adapter);
        cultivarFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = position == 0 ? null : cultivars.get(position);
                filterSummaries(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void updateSummaries() {
        summaries.clear();
        summaries.addAll(analyticsManager.summarizeByCultivar(allPrograms));
        Collections.sort(summaries, (a, b) -> Double.compare(b.getProfitPerArea(), a.getProfitPerArea()));
        expandableAdapter.updateData(summaries, allPrograms);
        updateChart();
        updateCompletionChart();
    }

    private void filterSummaries(String cultivarFilter) {
        List<AnalyticsManager.CultivarSummary> filtered = new ArrayList<>();
        for (AnalyticsManager.CultivarSummary s : summaries) {
            if (cultivarFilter == null || s.cultivarName.equals(cultivarFilter)) {
                filtered.add(s);
            }
        }
        expandableAdapter.updateData(filtered, allPrograms);
        updateChart();
        updateCompletionChart();
    }

    private void updateViewMode(String mode) {
        if ("chart".equals(mode)) {
            barChart.setVisibility(View.VISIBLE);
            if (completionChart != null) {
                completionChart.setVisibility(View.VISIBLE);
            }
            tableRecyclerView.setVisibility(View.GONE);
            updateChart();
            updateCompletionChart();
        } else {
            barChart.setVisibility(View.GONE);
            if (completionChart != null) {
                completionChart.setVisibility(View.GONE);
            }
            tableRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateChart() {
        if (summaries.isEmpty()) {
            barChart.clear();
            return;
        }
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        // Use all summaries for chart (not filtered)
        List<AnalyticsManager.CultivarSummary> chartSummaries = analyticsManager.summarizeByCultivar(allPrograms);
        Collections.sort(chartSummaries, (a, b) -> Double.compare(b.getProfitPerArea(), a.getProfitPerArea()));
        for (AnalyticsManager.CultivarSummary s : chartSummaries) {
            entries.add(new BarEntry(index, (float) s.getProfitPerArea()));
            labels.add(s.cultivarName);
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Profit per area");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);
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
    }

    private void updateCompletionChart() {
        if (completionChart == null) {
            return;
        }
        if (summaries.isEmpty()) {
            completionChart.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<AnalyticsManager.CultivarSummary> chartSummaries = analyticsManager.summarizeByCultivar(allPrograms);
        Collections.sort(chartSummaries, (a, b) -> Double.compare(b.getAverageCompletionRate(), a.getAverageCompletionRate()));

        int index = 0;
        for (AnalyticsManager.CultivarSummary s : chartSummaries) {
            entries.add(new Entry(index, (float) s.getAverageCompletionRate()));
            labels.add(s.cultivarName);
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Average completion rate (%)");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[1 % ColorTemplate.MATERIAL_COLORS.length]);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);

        LineData data = new LineData(dataSet);
        completionChart.setData(data);

        XAxis xAxis = completionChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = completionChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        completionChart.getAxisRight().setEnabled(false);
        completionChart.getDescription().setEnabled(false);
        completionChart.invalidate();
    }

    // --- Adapter for Cultivars (navigates to details screen on click) ---
    class ExpandableCultivarAdapter extends RecyclerView.Adapter<ExpandableCultivarAdapter.CultivarViewHolder> {
        private List<AnalyticsManager.CultivarSummary> cultivarSummaries;
        private List<WorkProgramEntity> allPrograms;

        ExpandableCultivarAdapter(List<AnalyticsManager.CultivarSummary> summaries, List<WorkProgramEntity> programs) {
            this.cultivarSummaries = summaries;
            this.allPrograms = programs;
        }

        void updateData(List<AnalyticsManager.CultivarSummary> summaries, List<WorkProgramEntity> programs) {
            this.cultivarSummaries = summaries;
            this.allPrograms = programs;
            notifyDataSetChanged();
        }

        @Override
        public CultivarViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cultivar_expandable, parent, false);
            return new CultivarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CultivarViewHolder holder, int position) {
            AnalyticsManager.CultivarSummary summary = cultivarSummaries.get(position);

            // Set cultivar header info
            holder.cultivarName.setText(summary.cultivarName);
            
            int programCount = summary.programCount;
            holder.programCount.setText(programCount + (programCount == 1 ? " program" : " programs"));

            // Set summary stats
            holder.totalArea.setText(String.format("%.2f hectare", summary.totalArea));
            holder.totalProfit.setText(String.format("₱%,.0f", summary.getProfit()));
            holder.profitPerArea.setText(String.format("₱%,.0f", summary.getProfitPerArea()));
            holder.avgCompletionRate.setText(String.format(Locale.getDefault(), "%.0f%%", summary.getAverageCompletionRate()));
            holder.adjustedProfit.setText(String.format("₱%,.0f", summary.getAdjustedProfit()));

            // Navigate to details screen on click
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AnalyticsActivity.this, CultivarDetailsActivity.class);
                intent.putExtra("cultivar_name", summary.cultivarName);
                startActivity(intent);
            });

            // Hide expand icon and work programs list (no longer needed)
            holder.expandIcon.setVisibility(View.GONE);
            holder.workProgramsRecyclerView.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return cultivarSummaries.size();
        }

        class CultivarViewHolder extends RecyclerView.ViewHolder {
            TextView cultivarName, programCount, totalArea, totalProfit, profitPerArea, avgCompletionRate, adjustedProfit;
            ImageView expandIcon;
            RecyclerView workProgramsRecyclerView;

            CultivarViewHolder(View itemView) {
                super(itemView);
                cultivarName = itemView.findViewById(R.id.cultivarNameHeader);
                programCount = itemView.findViewById(R.id.cultivarProgramCount);
                totalArea = itemView.findViewById(R.id.totalArea);
                totalProfit = itemView.findViewById(R.id.totalProfit);
                profitPerArea = itemView.findViewById(R.id.profitPerArea);
                avgCompletionRate = itemView.findViewById(R.id.avgCompletionRate);
                adjustedProfit = itemView.findViewById(R.id.adjustedProfit);
                expandIcon = itemView.findViewById(R.id.expandIcon);
                workProgramsRecyclerView = itemView.findViewById(R.id.workProgramsRecyclerView);
            }
        }
    }

    // --- Adapter for nested work programs (shown when cultivar is expanded) ---
    class WorkProgramAdapter extends RecyclerView.Adapter<WorkProgramViewHolder> {
        private List<WorkProgramEntity> items;
        private final android.content.Context context;

        WorkProgramAdapter(List<WorkProgramEntity> items) {
            this.items = items;
            this.context = AnalyticsActivity.this;
        }

        void updateData(List<WorkProgramEntity> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public WorkProgramViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_work_program_nested, parent, false);
            return new WorkProgramViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WorkProgramViewHolder holder, int position) {
            WorkProgramEntity e = items.get(position);
            holder.startDate.setText(e.startingDate != null ? e.startingDate : "N/A");
            holder.area.setText(String.format("%.2f hectare", e.areaSize));
            holder.phases.setText(getPhasesSummary(e));
            holder.detections.setText(getDetectionsSummary(e));
            holder.income.setText(String.format("₱%,.0f", e.projectedIncome));
            holder.expenses.setText(String.format("₱%,.0f", e.projectedExpenses));
            double profit = e.projectedIncome - e.projectedExpenses;
            holder.profit.setText(String.format("₱%,.0f", profit));
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
                return WorkProgramDataHelper.getPhasesActivitySummary(phases);
            } catch (org.json.JSONException ex) {
                return "N/A";
            }
        }

        private String getDetectionsSummary(WorkProgramEntity e) {
            // Load detection history count from DetectionHistoryManager
            if (e.startingDate != null) {
                return WorkProgramDataHelper.getDetectionsSummary(
                        context,
                        e.startingDate
                );
            }
            return "None";
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    static class WorkProgramViewHolder extends RecyclerView.ViewHolder {
        TextView area, startDate, phases, detections, income, expenses, profit;

        WorkProgramViewHolder(View itemView) {
            super(itemView);
            area = itemView.findViewById(R.id.nestedArea);
            startDate = itemView.findViewById(R.id.nestedStartDate);
            phases = itemView.findViewById(R.id.nestedPhases);
            detections = itemView.findViewById(R.id.nestedDetections);
            income = itemView.findViewById(R.id.nestedIncome);
            expenses = itemView.findViewById(R.id.nestedExpenses);
            profit = itemView.findViewById(R.id.nestedProfit);
        }
    }

    // --- Adapter for summary view (used for chart calculations) ---
    static class SummaryAdapter extends RecyclerView.Adapter<SummaryViewHolder> {
        private final List<AnalyticsManager.CultivarSummary> items;

        SummaryAdapter(List<AnalyticsManager.CultivarSummary> items) {
            this.items = items;
        }

        @Override
        public SummaryViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cultivar_summary, parent, false);
            return new SummaryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SummaryViewHolder holder, int position) {
            AnalyticsManager.CultivarSummary s = items.get(position);
            holder.name.setText(s.cultivarName);
            holder.area.setText(String.format("Area: %.2f hectare", s.totalArea));
            holder.income.setText(String.format("Income: ₱%,.0f", s.totalIncome));
            holder.expenses.setText(String.format("Expenses: ₱%,.0f", s.totalExpenses));
            holder.profit.setText(String.format("Profit: ₱%,.0f", s.getProfit()));
            holder.profitPerArea.setText(String.format("Profit/Area: ₱%,.0f", s.getProfitPerArea()));
            holder.completionRate.setText(String.format(Locale.getDefault(), "Completion: %.0f%%", s.getAverageCompletionRate()));
            holder.adjustedProfit.setText(String.format("Adjusted Profit: ₱%,.0f", s.getAdjustedProfit()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    static class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView name, area, income, expenses, profit, profitPerArea, completionRate, adjustedProfit;

        SummaryViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.summaryCultivarName);
            area = itemView.findViewById(R.id.summaryArea);
            income = itemView.findViewById(R.id.summaryIncome);
            expenses = itemView.findViewById(R.id.summaryExpenses);
            profit = itemView.findViewById(R.id.summaryProfit);
            profitPerArea = itemView.findViewById(R.id.summaryProfitPerArea);
            completionRate = itemView.findViewById(R.id.summaryCompletionRate);
            adjustedProfit = itemView.findViewById(R.id.summaryAdjustedProfit);
        }
    }

    private void showExportDialog() {
        if (allPrograms.isEmpty()) {
            Toast.makeText(this, "No work programs to export", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_work_programs, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.workProgramsRecyclerView);
        Button btnSelectAll = dialogView.findViewById(R.id.btnSelectAll);
        Button btnDeselectAll = dialogView.findViewById(R.id.btnDeselectAll);

        WorkProgramSelectionAdapter adapter = new WorkProgramSelectionAdapter(allPrograms);
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
                    exportToPdf(selected, null);
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void exportToPdf(List<WorkProgramEntity> selectedPrograms, String cultivarName) {
        progressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            String filePath = AnalyticsPdfExporter.exportToPdf(
                    this,
                    selectedPrograms,
                    cultivarName,
                    false, // Don't include charts from main screen
                    null,
                    null
            );

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (filePath != null) {
                    Toast.makeText(this, "PDF exported to Downloads: " + filePath, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to export PDF", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}


