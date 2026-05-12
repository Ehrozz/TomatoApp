package com.android.tomatoapp.analytics.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tomatoapp.R;
import com.android.tomatoapp.analytics.data.AnalyticsManager;
import com.android.tomatoapp.analytics.data.AnalyticsPdfExporter;
import com.android.tomatoapp.analytics.data.ResearchExporter;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.financial.ui.CurrentExpensesActivity;
import com.android.tomatoapp.workprogram.data.WorkProgramDataHelper;
import com.android.tomatoapp.workprogram.data.WorkProgramEntity;
import com.android.tomatoapp.workprogram.data.WorkProgramRepository;
import com.android.tomatoapp.workprogram.data.WorkProgramSelectionAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
 * Analytics screen showing cultivar productivity in table and chart form.
 */
public class AnalyticsActivity extends BaseBottomNavActivity {

    private Spinner viewModeSpinner;
    private Spinner cultivarFilterSpinner;
    private Spinner seasonFilterSpinner;
    private RecyclerView tableRecyclerView;
    private BarChart barChart;
    private LineChart completionChart;
    private ProgressBar progressBar;
    private TextView emptyText;

    private WorkProgramRepository repository;
    private AnalyticsManager analyticsManager;

    private final List<WorkProgramEntity> allPrograms = new ArrayList<>();
    private final List<AnalyticsManager.CultivarSummary> summaries = new ArrayList<>();
    private final List<WorkProgramEntity> displayedPrograms = new ArrayList<>();
    private final List<AnalyticsManager.CultivarSummary> displayedSummaries = new ArrayList<>();
    private WorkProgramCardAdapter workProgramCardAdapter;
    private TextView tvNetProfitStat;
    private TextView tvCompletionStat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        setupBottomNavigation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cultivar Analytics");
        }

        repository = new WorkProgramRepository(this);
        analyticsManager = new AnalyticsManager();

        viewModeSpinner = findViewById(R.id.viewModeSpinner);
        cultivarFilterSpinner = findViewById(R.id.cultivarFilterSpinner);
        seasonFilterSpinner = findViewById(R.id.seasonFilterSpinner);
        tableRecyclerView = findViewById(R.id.tableRecyclerView);
        barChart = findViewById(R.id.barChart);
        completionChart = findViewById(R.id.completionChart);
        progressBar = findViewById(R.id.analyticsProgress);
        emptyText = findViewById(R.id.emptyText);
        tvNetProfitStat = findViewById(R.id.tvNetProfitStat);
        tvCompletionStat = findViewById(R.id.tvCompletionStat);
        Button btnExportPdf = findViewById(R.id.btnExportPdf);
        Button btnExportCsv = findViewById(R.id.btnExportCsv);

        setupViewModeSelector();
        setupRecyclerView();
        loadData();

        if (btnExportPdf != null) {
            btnExportPdf.setOnClickListener(v -> showExportDialog());
        }
        
        if (btnExportCsv != null) {
            btnExportCsv.setOnClickListener(v -> exportToCsv());
        }
    }

    private void setupViewModeSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Table", "Chart"}
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(R.color.text_primary, null));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(R.color.text_primary, null));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewModeSpinner.setAdapter(adapter);
        viewModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.text_primary, null));
                }
                updateViewMode(position == 0 ? "table" : "chart");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupRecyclerView() {
        tableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workProgramCardAdapter = new WorkProgramCardAdapter(displayedPrograms);
        tableRecyclerView.setAdapter(workProgramCardAdapter);
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        repository.loadAllForCurrentUser(items -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            allPrograms.clear();
            if (items != null) {
                allPrograms.addAll(items);
            }

            View emptyStateCard = findViewById(R.id.emptyStateCard);
            View chartsContainer = findViewById(R.id.chartsContainer);

            if (allPrograms.isEmpty()) {
                if (emptyStateCard != null) {
                    emptyStateCard.setVisibility(View.VISIBLE);
                }
                emptyText.setVisibility(View.VISIBLE);
                tableRecyclerView.setVisibility(View.GONE);
                if (chartsContainer != null) {
                    chartsContainer.setVisibility(View.GONE);
                }
                barChart.setVisibility(View.GONE);
            } else {
                if (emptyStateCard != null) {
                    emptyStateCard.setVisibility(View.GONE);
                }
                emptyText.setVisibility(View.GONE);
                buildCultivarFilter();
                buildSeasonFilter();
                updateSummaries();
            }
        }));
    }

    private void buildSeasonFilter() {
        List<String> seasons = new ArrayList<>();
        seasons.add("All seasons");
        seasons.add("On-season");
        seasons.add("Off-season");
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                seasons
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(R.color.text_primary, null));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(R.color.text_primary, null));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonFilterSpinner.setAdapter(adapter);
        seasonFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.text_primary, null));
                }
                String selected = position == 0 ? null : (position == 1 ? "on-season" : "off-season");
                filterBySeason(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void buildCultivarFilter() {
        List<String> cultivars = new ArrayList<>();
        cultivars.add("All cultivars");
        for (WorkProgramEntity e : allPrograms) {
            if (e.cultivarName != null && !cultivars.contains(e.cultivarName)) {
                cultivars.add(e.cultivarName);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                cultivars
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(R.color.text_primary, null));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(R.color.text_primary, null));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cultivarFilterSpinner.setAdapter(adapter);
        cultivarFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.text_primary, null));
                }
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
        displayedPrograms.clear();
        displayedPrograms.addAll(allPrograms);
        displayedSummaries.clear();
        displayedSummaries.addAll(summaries);
        workProgramCardAdapter.updateData(displayedPrograms);
        updateTopStatsFromPrograms(displayedPrograms);
        updateChart();
        updateCompletionChart();
    }

    private String currentSeasonFilter = null;
    private String currentCultivarFilter = null;
    
    private void filterSummaries(String cultivarFilter) {
        currentCultivarFilter = cultivarFilter;
        applyFilters();
    }
    
    private void filterBySeason(String seasonFilter) {
        currentSeasonFilter = seasonFilter;
        applyFilters();
    }
    
    private void applyFilters() {
        // Filter programs first
        List<WorkProgramEntity> filteredPrograms = new ArrayList<>();
        for (WorkProgramEntity e : allPrograms) {
            boolean matchesCultivar = currentCultivarFilter == null || 
                                     (e.cultivarName != null && e.cultivarName.equals(currentCultivarFilter));
            boolean matchesSeason = currentSeasonFilter == null || 
                                   (e.season != null && e.season.equals(currentSeasonFilter)) ||
                                   (currentSeasonFilter != null && currentSeasonFilter.equals("off-season") && e.isOffSeason) ||
                                   (currentSeasonFilter != null && currentSeasonFilter.equals("on-season") && !e.isOffSeason);
            
            if (matchesCultivar && matchesSeason) {
                filteredPrograms.add(e);
            }
        }
        
        // Recalculate summaries with filtered programs
        List<AnalyticsManager.CultivarSummary> filtered = analyticsManager.summarizeByCultivar(filteredPrograms);
        Collections.sort(filtered, (a, b) -> Double.compare(b.getProfitPerArea(), a.getProfitPerArea()));
        displayedPrograms.clear();
        displayedPrograms.addAll(filteredPrograms);
        displayedSummaries.clear();
        displayedSummaries.addAll(filtered);
        workProgramCardAdapter.updateData(displayedPrograms);
        updateTopStatsFromPrograms(displayedPrograms);
        updateChart();
        updateCompletionChart();
    }

    private void updateTopStatsFromPrograms(List<WorkProgramEntity> programs) {
        if (tvNetProfitStat == null || tvCompletionStat == null) {
            return;
        }
        if (programs == null || programs.isEmpty()) {
            tvNetProfitStat.setText("₱0.0k");
            tvCompletionStat.setText("0%");
            return;
        }

        double totalProfit = 0.0;
        double weightedCompletionSum = 0.0;
        double weightedCompletionArea = 0.0;
        for (WorkProgramEntity program : programs) {
            totalProfit += (program.adjustedIncome - program.adjustedExpenses);
            if (program.areaSize > 0) {
                weightedCompletionSum += program.completionRate * program.areaSize;
                weightedCompletionArea += program.areaSize;
            } else {
                weightedCompletionSum += program.completionRate;
                weightedCompletionArea += 1.0;
            }
        }

        double avgCompletion = weightedCompletionArea > 0 ? weightedCompletionSum / weightedCompletionArea : 0.0;
        tvNetProfitStat.setText(String.format(Locale.getDefault(), "₱%.1fk", totalProfit / 1000.0));
        tvCompletionStat.setText(String.format(Locale.getDefault(), "%.0f%%", avgCompletion));
    }

    private void updateViewMode(String mode) {
        View chartsContainer = findViewById(R.id.chartsContainer);
        if ("chart".equals(mode)) {
            if (chartsContainer != null) {
                chartsContainer.setVisibility(View.VISIBLE);
            }
            barChart.setVisibility(View.VISIBLE);
            if (completionChart != null) {
                completionChart.setVisibility(View.VISIBLE);
            }
            tableRecyclerView.setVisibility(View.GONE);
            updateChart();
            updateCompletionChart();
        } else {
            if (chartsContainer != null) {
                chartsContainer.setVisibility(View.GONE);
            }
            barChart.setVisibility(View.GONE);
            if (completionChart != null) {
                completionChart.setVisibility(View.GONE);
            }
            tableRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateChart() {
        if (displayedSummaries.isEmpty()) {
            barChart.clear();
            return;
        }
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        List<AnalyticsManager.CultivarSummary> chartSummaries = new ArrayList<>(displayedSummaries);
        Collections.sort(chartSummaries, (a, b) -> Double.compare(b.getProfitPerArea(), a.getProfitPerArea()));
        for (AnalyticsManager.CultivarSummary s : chartSummaries) {
            entries.add(new BarEntry(index, (float) s.getProfitPerArea()));
            labels.add(s.cultivarName);
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Profit per area");
        // Use vibrant colors matching the design
        dataSet.setColors(
                getResources().getColor(R.color.sidebar_dark_green, null),
                getResources().getColor(R.color.tomato_red, null),
                getResources().getColor(R.color.warm_orange, null),
                getResources().getColor(R.color.scan_blue, null),
                getResources().getColor(R.color.fresh_green, null)
        );
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));

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
        if (displayedSummaries.isEmpty()) {
            completionChart.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<AnalyticsManager.CultivarSummary> chartSummaries = new ArrayList<>(displayedSummaries);
        Collections.sort(chartSummaries, (a, b) -> Double.compare(b.getAverageCompletionRate(), a.getAverageCompletionRate()));

        int index = 0;
        for (AnalyticsManager.CultivarSummary s : chartSummaries) {
            entries.add(new Entry(index, (float) s.getAverageCompletionRate()));
            labels.add(s.cultivarName);
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Average completion rate (%)");
        // Use vibrant colors matching the design
        int lineColor = getResources().getColor(R.color.scan_blue, null);
        int circleColor = getResources().getColor(R.color.sidebar_dark_green, null);
        dataSet.setColor(lineColor);
        dataSet.setCircleColor(circleColor);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));
        dataSet.setFillColor(lineColor);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(30);

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

    // --- Adapter for individual work programs ---
    class WorkProgramCardAdapter extends RecyclerView.Adapter<WorkProgramCardAdapter.WorkProgramViewHolder> {
        private List<WorkProgramEntity> items;

        WorkProgramCardAdapter(List<WorkProgramEntity> items) {
            this.items = items;
        }

        void updateData(List<WorkProgramEntity> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public WorkProgramViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_work_program_analytics_card, parent, false);
            return new WorkProgramViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WorkProgramViewHolder holder, int position) {
            WorkProgramEntity program = items.get(position);
            String title = program.cultivarName != null ? program.cultivarName : "Saved work program";
            holder.programTitle.setText(title);
            holder.programSubtitle.setText(String.format(Locale.getDefault(), "Start: %s | Area: %.2f ha", 
                    program.startingDate != null ? program.startingDate : "N/A", program.areaSize));
            holder.adjustedIncome.setText(String.format(Locale.getDefault(), "Adj income: ₱%,.0f", program.adjustedIncome));
            holder.adjustedExpenses.setText(String.format(Locale.getDefault(), "Adj expenses: ₱%,.0f", program.adjustedExpenses));
            holder.adjustedProfit.setText(String.format(Locale.getDefault(), "Adj profit: ₱%,.0f", program.adjustedIncome - program.adjustedExpenses));
            holder.projectedProfit.setText(String.format(Locale.getDefault(), "Projected profit: ₱%,.0f", program.projectedIncome - program.projectedExpenses));
            holder.completionRate.setText(String.format(Locale.getDefault(), "Completion: %.0f%%", program.completionRate));

            holder.openProgramButton.setOnClickListener(v -> openWorkProgram(program));
            holder.financeButton.setOnClickListener(v -> openFinance(program));
            holder.itemView.setOnClickListener(v -> openFinance(program));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class WorkProgramViewHolder extends RecyclerView.ViewHolder {
            TextView programTitle, programSubtitle, adjustedIncome, adjustedExpenses, adjustedProfit, projectedProfit, completionRate;
            com.google.android.material.button.MaterialButton openProgramButton, financeButton;

            WorkProgramViewHolder(View itemView) {
                super(itemView);
                programTitle = itemView.findViewById(R.id.programTitle);
                programSubtitle = itemView.findViewById(R.id.programSubtitle);
                adjustedIncome = itemView.findViewById(R.id.adjustedIncome);
                adjustedExpenses = itemView.findViewById(R.id.adjustedExpenses);
                adjustedProfit = itemView.findViewById(R.id.adjustedProfit);
                projectedProfit = itemView.findViewById(R.id.projectedProfit);
                completionRate = itemView.findViewById(R.id.completionRate);
                openProgramButton = itemView.findViewById(R.id.btnOpenProgram);
                financeButton = itemView.findViewById(R.id.btnOpenFinance);
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
            holder.area.setText(String.format(Locale.getDefault(), "%.2f hectare", e.areaSize));
            holder.phases.setText(getPhasesSummary(e));
            holder.income.setText(String.format(Locale.getDefault(), "₱%,.0f", e.projectedIncome));
            holder.expenses.setText(String.format(Locale.getDefault(), "₱%,.0f", e.projectedExpenses));
            double profit = e.projectedIncome - e.projectedExpenses;
            holder.profit.setText(String.format(Locale.getDefault(), "₱%,.0f", profit));
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

    static class WorkProgramViewHolder extends RecyclerView.ViewHolder {
        TextView area, startDate, phases, income, expenses, profit;

        WorkProgramViewHolder(View itemView) {
            super(itemView);
            area = itemView.findViewById(R.id.nestedArea);
            startDate = itemView.findViewById(R.id.nestedStartDate);
            phases = itemView.findViewById(R.id.nestedPhases);
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
            holder.area.setText(String.format(Locale.getDefault(), "Area: %.2f hectare", s.totalArea));
            holder.income.setText(String.format(Locale.getDefault(), "Income: ₱%,.0f", s.totalAdjustedIncome));
            holder.expenses.setText(String.format(Locale.getDefault(), "Expenses: ₱%,.0f", s.totalAdjustedExpenses));
            holder.profit.setText(String.format(Locale.getDefault(), "Adjusted Profit: ₱%,.0f", s.getAdjustedProfit()));
            holder.profitPerArea.setText(String.format(Locale.getDefault(), "Adj Profit/Area: ₱%,.0f", s.getAdjustedProfitPerArea()));
            holder.completionRate.setText(String.format(Locale.getDefault(), "Completion: %.0f%%", s.getAverageCompletionRate()));
            holder.adjustedProfit.setText(String.format(Locale.getDefault(), "Adjusted Profit: ₱%,.0f", s.getAdjustedProfit()));
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
    
    private void exportToCsv() {
        if (allPrograms.isEmpty()) {
            Toast.makeText(this, "No work programs to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            // Get filtered programs based on current filters
            List<WorkProgramEntity> programsToExport = new ArrayList<>();
            for (WorkProgramEntity e : allPrograms) {
                boolean matchesCultivar = currentCultivarFilter == null || 
                                         (e.cultivarName != null && e.cultivarName.equals(currentCultivarFilter));
                boolean matchesSeason = currentSeasonFilter == null || 
                                       (e.season != null && e.season.equals(currentSeasonFilter)) ||
                                       (currentSeasonFilter != null && currentSeasonFilter.equals("off-season") && e.isOffSeason) ||
                                       (currentSeasonFilter != null && currentSeasonFilter.equals("on-season") && !e.isOffSeason);
                
                if (matchesCultivar && matchesSeason) {
                    programsToExport.add(e);
                }
            }
            
            String filePath = ResearchExporter.exportToCsv(this, programsToExport, false);
            
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (filePath != null) {
                    Toast.makeText(this, "Data exported to Downloads: " + filePath, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void openWorkProgram(WorkProgramEntity program) {
        if (program == null || program.id == null) return;
        Intent intent = new Intent(this, com.android.tomatoapp.workprogram.ui.Workprogram.class);
        intent.putExtra("programId", program.id);
        intent.putExtra("cultivar", program.cultivarName);
        intent.putExtra("startDate", program.startingDate);
        startActivity(intent);
    }

    private void openFinance(WorkProgramEntity program) {
        if (program == null || program.id == null) return;
        Intent intent = new Intent(this, CurrentExpensesActivity.class);
        intent.putExtra("programId", program.id);
        intent.putExtra("cultivar", program.cultivarName);
        intent.putExtra("startDate", program.startingDate);
        startActivity(intent);
    }
}
