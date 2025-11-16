package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Details screen for a specific cultivar showing all work programs with table and chart views.
 */
public class CultivarDetailsActivity extends AppCompatActivity {

    private String cultivarName;
    private Spinner viewModeSpinner;
    private RecyclerView tableRecyclerView;
    private BarChart barChart;
    private LineChart lineChart;
    private ProgressBar progressBar;
    private TextView emptyText;
    private TextView cultivarTitle;
    private View chartsContainer;

    private WorkProgramRepository repository;
    private final List<WorkProgramEntity> workPrograms = new ArrayList<>();
    private WorkProgramAdapter adapter;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cultivar Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = new WorkProgramRepository(this);

        viewModeSpinner = findViewById(R.id.viewModeSpinner);
        tableRecyclerView = findViewById(R.id.tableRecyclerView);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        cultivarTitle = findViewById(R.id.cultivarTitle);
        chartsContainer = findViewById(R.id.chartsContainer);
        Button btnExportPdf = findViewById(R.id.btnExportPdf);

        cultivarTitle.setText(cultivarName);

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

    private void setupRecyclerView() {
        tableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkProgramAdapter(workPrograms);
        tableRecyclerView.setAdapter(adapter);
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

            if (workPrograms.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                tableRecyclerView.setVisibility(View.GONE);
                chartsContainer.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                updateCharts();
            }
        }));
    }

    private void updateViewMode(String mode) {
        if ("charts".equals(mode)) {
            tableRecyclerView.setVisibility(View.GONE);
            chartsContainer.setVisibility(View.VISIBLE);
            updateCharts();
        } else {
            tableRecyclerView.setVisibility(View.VISIBLE);
            chartsContainer.setVisibility(View.GONE);
        }
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
        incomeDataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        incomeDataSet.setValueTextSize(10f);

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(ColorTemplate.MATERIAL_COLORS[1]);
        expenseDataSet.setValueTextSize(10f);

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
        profitDataSet.setColor(ColorTemplate.MATERIAL_COLORS[2]);
        profitDataSet.setValueTextSize(10f);
        profitDataSet.setLineWidth(2f);
        profitDataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[2]);
        profitDataSet.setCircleRadius(4f);

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
            holder.areaSize.setText(String.format("%.2f hectare", e.areaSize));
            holder.startingDate.setText(e.startingDate != null ? e.startingDate : "N/A");
            holder.phases.setText(getPhasesSummary(e));
            holder.detections.setText(getDetectionsSummary(e));
            holder.projectedIncome.setText(String.format("₱%,.2f", e.projectedIncome));
            holder.projectedExpenses.setText(String.format("₱%,.2f", e.projectedExpenses));
            double profit = e.projectedIncome - e.projectedExpenses;
            holder.profit.setText(String.format("₱%,.2f", profit));
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
            if (e.startingDate != null) {
                return WorkProgramDataHelper.getDetectionsSummary(context, e.startingDate);
            }
            return "None";
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
        TextView cultivarName, areaSize, startingDate, phases, detections, projectedIncome, projectedExpenses, profit;

        WorkProgramViewHolder(View itemView) {
            super(itemView);
            cultivarName = itemView.findViewById(R.id.detailsCultivarName);
            areaSize = itemView.findViewById(R.id.detailsAreaSize);
            startingDate = itemView.findViewById(R.id.detailsStartingDate);
            phases = itemView.findViewById(R.id.detailsPhases);
            detections = itemView.findViewById(R.id.detailsDetections);
            projectedIncome = itemView.findViewById(R.id.detailsProjectedIncome);
            projectedExpenses = itemView.findViewById(R.id.detailsProjectedExpenses);
            profit = itemView.findViewById(R.id.detailsProfit);
        }
    }
}

