package com.android.tomatoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity for comparing on-season vs off-season performance.
 * Key for proving off-season tomato planting viability.
 */
public class SeasonComparisonActivity extends BaseDrawerActivity {
    
    private ProgressBar progressBar;
    private TextView emptyText;
    private RecyclerView comparisonRecyclerView;
    private BarChart comparisonChart;
    
    private WorkProgramRepository repository;
    private AnalyticsManager analyticsManager;
    private final List<WorkProgramEntity> allPrograms = new ArrayList<>();
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season_comparison);
        
        setupDrawer();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Season Comparison");
        }
        
        repository = new WorkProgramRepository(this);
        analyticsManager = new AnalyticsManager();
        
        progressBar = findViewById(R.id.comparisonProgress);
        emptyText = findViewById(R.id.comparisonEmptyText);
        comparisonRecyclerView = findViewById(R.id.comparisonRecyclerView);
        comparisonChart = findViewById(R.id.comparisonChart);
        
        setupRecyclerView();
        loadData();
    }
    
    private void setupRecyclerView() {
        comparisonRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Will set adapter after loading data
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
                comparisonRecyclerView.setVisibility(View.GONE);
                comparisonChart.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                comparisonRecyclerView.setVisibility(View.VISIBLE);
                comparisonChart.setVisibility(View.VISIBLE);
                updateComparison();
            }
        }));
    }
    
    private void updateComparison() {
        List<AnalyticsManager.SeasonSummary> seasonSummaries = analyticsManager.summarizeBySeason(allPrograms);
        AnalyticsManager.SeasonComparison comparison = analyticsManager.compareSeasons(seasonSummaries);
        
        // Update RecyclerView with comparison data
        SeasonComparisonAdapter adapter = new SeasonComparisonAdapter(comparison, seasonSummaries);
        comparisonRecyclerView.setAdapter(adapter);
        
        // Update chart
        updateComparisonChart(seasonSummaries);
    }
    
    private void updateComparisonChart(List<AnalyticsManager.SeasonSummary> summaries) {
        if (summaries.isEmpty()) {
            comparisonChart.clear();
            return;
        }
        
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        int index = 0;
        for (AnalyticsManager.SeasonSummary summary : summaries) {
            entries.add(new BarEntry(index, (float) summary.getProfitPerArea()));
            labels.add(summary.season.equals("on-season") ? "On-Season" : "Off-Season");
            index++;
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Profit per Area (PHP/hectare)");
        dataSet.setColors(
                getResources().getColor(R.color.sidebar_dark_green, null),
                getResources().getColor(R.color.tomato_red, null)
        );
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));
        
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        
        comparisonChart.setData(data);
        XAxis xAxis = comparisonChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        
        comparisonChart.getAxisRight().setEnabled(false);
        comparisonChart.getDescription().setEnabled(false);
        comparisonChart.setFitBars(true);
        comparisonChart.invalidate();
    }
    
    private static class SeasonComparisonAdapter extends RecyclerView.Adapter<SeasonComparisonAdapter.ComparisonViewHolder> {
        private final AnalyticsManager.SeasonComparison comparison;
        private final List<AnalyticsManager.SeasonSummary> summaries;
        
        SeasonComparisonAdapter(AnalyticsManager.SeasonComparison comparison, 
                                List<AnalyticsManager.SeasonSummary> summaries) {
            this.comparison = comparison;
            this.summaries = summaries;
        }
        
        @Override
        public ComparisonViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_season_comparison, parent, false);
            return new ComparisonViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ComparisonViewHolder holder, int position) {
            if (position == 0 && comparison != null) {
                // Show comparison summary
                holder.bindComparison(comparison);
            } else {
                // Show individual season summaries
                int summaryIndex = position - 1;
                if (summaryIndex < summaries.size()) {
                    holder.bindSummary(summaries.get(summaryIndex));
                }
            }
        }
        
        @Override
        public int getItemCount() {
            return 1 + summaries.size(); // 1 for comparison + summaries
        }
        
        static class ComparisonViewHolder extends RecyclerView.ViewHolder {
            TextView title, metric1, metric2, metric3, metric4;
            
            ComparisonViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.comparisonTitle);
                metric1 = itemView.findViewById(R.id.comparisonMetric1);
                metric2 = itemView.findViewById(R.id.comparisonMetric2);
                metric3 = itemView.findViewById(R.id.comparisonMetric3);
                metric4 = itemView.findViewById(R.id.comparisonMetric4);
            }
            
            void bindComparison(AnalyticsManager.SeasonComparison comp) {
                if (title != null) title.setText("📊 Season Comparison");
                if (metric1 != null) {
                    metric1.setText(String.format(Locale.getDefault(), 
                        "Profit Difference: ₱%,.2f (%.1f%%)", 
                        comp.getProfitDifference(), 
                        comp.getProfitDifferencePercent()));
                }
                if (metric2 != null) {
                    metric2.setText(String.format(Locale.getDefault(), 
                        "Yield Difference: %.2f kg/hectare (%.1f%%)", 
                        comp.getYieldDifference(), 
                        comp.getYieldDifferencePercent()));
                }
                if (metric3 != null && comp.onSeason != null) {
                    metric3.setText(String.format(Locale.getDefault(), 
                        "On-Season Programs: %d", comp.onSeason.programCount));
                }
                if (metric4 != null && comp.offSeason != null) {
                    metric4.setText(String.format(Locale.getDefault(), 
                        "Off-Season Programs: %d", comp.offSeason.programCount));
                }
            }
            
            void bindSummary(AnalyticsManager.SeasonSummary summary) {
                if (title != null) {
                    title.setText(summary.season.equals("on-season") ? "🌞 On-Season" : "🌧️ Off-Season");
                }
                if (metric1 != null) {
                    metric1.setText(String.format(Locale.getDefault(), 
                        "Programs: %d | Area: %.2f ha", summary.programCount, summary.totalArea));
                }
                if (metric2 != null) {
                    metric2.setText(String.format(Locale.getDefault(), 
                        "Profit: ₱%,.2f (₱%,.2f/ha)", summary.getProfit(), summary.getProfitPerArea()));
                }
                if (metric3 != null) {
                    metric3.setText(String.format(Locale.getDefault(), 
                        "Avg Yield: %.2f kg/ha", summary.getAverageYield()));
                }
                if (metric4 != null) {
                    metric4.setText(String.format(Locale.getDefault(), 
                        "Completion Rate: %.1f%%", summary.getAverageCompletionRate()));
                }
            }
        }
    }
}

