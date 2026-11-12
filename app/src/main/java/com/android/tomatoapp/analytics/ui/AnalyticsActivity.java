package com.android.tomatoapp.analytics.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import com.android.tomatoapp.analytics.data.AnalyticsManager;
import com.android.tomatoapp.analytics.data.AnalyticsPdfExporter;
import com.android.tomatoapp.analytics.data.DiseaseExportGroup;
import com.android.tomatoapp.analytics.data.ResearchExporter;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.detection.data.DetectionHistoryManager;
import com.android.tomatoapp.detection.ui.DetectionResults;
import com.android.tomatoapp.financial.ui.CurrentExpensesActivity;
import com.android.tomatoapp.settings.data.SettingsPreferences;
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
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

/**
 * Analytics screen showing cultivar productivity in table and chart form.
 */
public class AnalyticsActivity extends BaseBottomNavActivity {

    private Spinner viewModeSpinner;
    private Spinner cultivarFilterSpinner;
    private Spinner seasonFilterSpinner;
    private LinearLayout programCardsContainer;
    private BarChart barChart;
    private LineChart completionChart;
    private ProgressBar progressBar;
    private TextView emptyText;
    private final Set<String> expandedProgramIds = new HashSet<>();
    private final Set<String> expandedDiseaseCards = new HashSet<>();

    private WorkProgramRepository repository;
    private AnalyticsManager analyticsManager;

    private final List<WorkProgramEntity> allPrograms = new ArrayList<>();
    private final List<AnalyticsManager.CultivarSummary> summaries = new ArrayList<>();
    private final List<WorkProgramEntity> displayedPrograms = new ArrayList<>();
    private final List<AnalyticsManager.CultivarSummary> displayedSummaries = new ArrayList<>();
    private TextView tvNetProfitStat;
    private TextView tvCompletionStat;
    private boolean diseaseTab = false;
    private List<JSONObject> diseaseHistory = new ArrayList<>();
    private List<DiseaseExportGroup> currentDiseaseGroups = new ArrayList<>();
    private MaterialButtonToggleGroup tabGroup;
    private TextView headerTitle;
    private TextView headerSubtitle;
    private LinearLayout diseaseSection;
    private LinearLayout diseaseListContainer;
    private View diseaseEmptyCard;
    private TextView tvScanTotal;
    private TextView tvScanHealthy;
    private TextView tvScanInfected;
    private LinearLayout exportRow;
    private View financialResults;
    private View statsGrid;

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
        programCardsContainer = findViewById(R.id.programCardsContainer);
        barChart = findViewById(R.id.barChart);
        completionChart = findViewById(R.id.completionChart);
        // Enable scrolling/interaction on charts so users can pan/zoom when data is wide
        if (barChart != null) {
            barChart.setTouchEnabled(true);
            barChart.setDragEnabled(true);
            barChart.setScaleEnabled(true);
            barChart.setPinchZoom(true);
            barChart.setAutoScaleMinMaxEnabled(true);
        }
        if (completionChart != null) {
            completionChart.setTouchEnabled(true);
            completionChart.setDragEnabled(true);
            completionChart.setScaleEnabled(true);
            completionChart.setPinchZoom(true);
            completionChart.setAutoScaleMinMaxEnabled(true);
        }
        progressBar = findViewById(R.id.analyticsProgress);
        emptyText = findViewById(R.id.emptyText);
        tvNetProfitStat = findViewById(R.id.tvNetProfitStat);
        tvCompletionStat = findViewById(R.id.tvCompletionStat);
        tabGroup = findViewById(R.id.tabGroup);
        headerTitle = findViewById(R.id.headerTitle);
        headerSubtitle = findViewById(R.id.headerSubtitle);
        diseaseSection = findViewById(R.id.diseaseSection);
        diseaseListContainer = findViewById(R.id.diseaseListContainer);
        diseaseEmptyCard = findViewById(R.id.diseaseEmptyCard);
        tvScanTotal = findViewById(R.id.tvScanTotal);
        tvScanHealthy = findViewById(R.id.tvScanHealthy);
        tvScanInfected = findViewById(R.id.tvScanInfected);
        exportRow = findViewById(R.id.exportRow);
        financialResults = findViewById(R.id.financialResults);
        statsGrid = findViewById(R.id.statsGrid);
        Button btnExportPdf = findViewById(R.id.btnExportPdf);
        Button btnExportCsv = findViewById(R.id.btnExportCsv);

        setupViewModeSelector();
        tabGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                diseaseTab = checkedId == R.id.btnTabDisease;
                applyTab();
            }
        });

        if (btnExportPdf != null) {
            btnExportPdf.setOnClickListener(v -> {
                if (diseaseTab) {
                    exportDiseasePdf();
                } else {
                    showExportDialog();
                }
            });
        }
        
        if (btnExportCsv != null) {
            btnExportCsv.setOnClickListener(v -> {
                if (diseaseTab) {
                    exportDiseaseCsv();
                } else {
                    exportToCsv();
                }
            });
        }
        ((com.google.android.material.button.MaterialButton) findViewById(R.id.btnTabFinancial)).setChecked(true);
        applyTab();
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
                tv.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewModeSpinner.setAdapter(adapter);
        viewModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(android.graphics.Color.BLACK);
                }
                if (diseaseTab) {
                    renderDiseaseView();
                    return;
                }
                updateViewMode(position == 0 ? "table" : "chart");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.google.android.material.button.MaterialButton matchingTab =
                findViewById(diseaseTab ? R.id.btnTabDisease : R.id.btnTabFinancial);
        if (matchingTab != null) matchingTab.setChecked(true);
        applyTab();
        loadData();
    }

    private void applyTab() {
        if (diseaseTab) {
            if (statsGrid != null) statsGrid.setVisibility(View.GONE);
            if (exportRow != null) exportRow.setVisibility(View.VISIBLE);
            if (financialResults != null) financialResults.setVisibility(View.GONE);
            if (diseaseSection != null) diseaseSection.setVisibility(View.VISIBLE);
            if (viewModeSpinner != null) viewModeSpinner.setVisibility(View.VISIBLE);
            if (headerTitle != null) headerTitle.setText("Disease Analytics");
            if (headerSubtitle != null) headerSubtitle.setText("Scan results by work program");
            loadDiseaseAnalytics();
        } else {
            if (statsGrid != null) statsGrid.setVisibility(View.VISIBLE);
            if (exportRow != null) exportRow.setVisibility(View.VISIBLE);
            if (financialResults != null) financialResults.setVisibility(View.VISIBLE);
            if (diseaseSection != null) diseaseSection.setVisibility(View.GONE);
            if (headerTitle != null) headerTitle.setText("Financial Analytics");
            if (headerSubtitle != null) headerSubtitle.setText("Track expenses and profitability by saved work program");
        }
    }

    private void loadData() {
        if (allPrograms.isEmpty() && progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        repository.loadAllForCurrentUser(items -> runOnUiThread(() -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            allPrograms.clear();
            if (items != null) {
                for (WorkProgramEntity item : items) {
                    if (item != null && item.cultivarName != null && !item.cultivarName.isEmpty()) {
                        allPrograms.add(item);
                    }
                }
            }

            View emptyStateCard = findViewById(R.id.emptyStateCard);
            View chartsContainer = findViewById(R.id.chartsContainer);

            if (allPrograms.isEmpty()) {
                if (emptyStateCard != null) {
                    emptyStateCard.setVisibility(View.VISIBLE);
                }
                emptyText.setVisibility(View.VISIBLE);
                if (programCardsContainer != null) {
                    programCardsContainer.setVisibility(View.GONE);
                    programCardsContainer.removeAllViews();
                }
                if (chartsContainer != null) {
                    chartsContainer.setVisibility(View.GONE);
                }
                barChart.setVisibility(View.GONE);
                if (diseaseTab) loadDiseaseAnalytics();
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
                tv.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonFilterSpinner.setAdapter(adapter);
        seasonFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(android.graphics.Color.BLACK);
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
                tv.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(android.graphics.Color.BLACK);
                view.setBackgroundColor(android.graphics.Color.WHITE);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cultivarFilterSpinner.setAdapter(adapter);
        cultivarFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(android.graphics.Color.BLACK);
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
        bindProgramCards(displayedPrograms);
        updateTopStatsFromPrograms(displayedPrograms);
        updateChart();
        updateCompletionChart();
        if (diseaseTab) loadDiseaseAnalytics();
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
        bindProgramCards(displayedPrograms);
        updateTopStatsFromPrograms(displayedPrograms);
        updateChart();
        updateCompletionChart();
        if (diseaseTab) loadDiseaseAnalytics();
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
            double expenses = program.loggedExpenses > 0 ? program.loggedExpenses : program.adjustedExpenses;
            totalProfit += (program.adjustedIncome - expenses);
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

    private void loadDiseaseAnalytics() {
        new Thread(() -> {
            try {
                diseaseHistory = DetectionHistoryManager.getHistory(this);
            } catch (Exception e) {
                Log.e("AnalyticsActivity", "Error loading disease analytics", e);
                diseaseHistory = new ArrayList<>();
            }
            runOnUiThread(this::renderDiseaseView);
        }).start();
    }

    private void renderDiseaseView() {
        Set<String> historyIds = new HashSet<>();
        StringBuilder historyIdsDebugBuilder = new StringBuilder();
        for (JSONObject entry : diseaseHistory) {
            String programId = entry.optString("programId", "");
            if (historyIds.add(programId)) {
                if (historyIdsDebugBuilder.length() > 0) historyIdsDebugBuilder.append(",");
                historyIdsDebugBuilder.append(programId);
            }
        }
        String historyIdsDebug = historyIdsDebugBuilder.toString();
        StringBuilder displayedIdsDebugBuilder = new StringBuilder();
        for (WorkProgramEntity program : displayedPrograms) {
            if (displayedIdsDebugBuilder.length() > 0) displayedIdsDebugBuilder.append(",");
            displayedIdsDebugBuilder.append(program.id);
        }
        String displayedIdsDebug = displayedIdsDebugBuilder.toString();
        Log.d("DiseaseTab", "history=" + diseaseHistory.size() + " programs=" + displayedPrograms.size() + " historyIds=" + historyIdsDebug + " displayedIds=" + displayedIdsDebug);
        if (!diseaseTab) return;

        Map<String, List<JSONObject>> entriesByProgram = new HashMap<>();
        List<JSONObject> unlinked = new ArrayList<>();
        for (JSONObject entry : diseaseHistory) {
            String programId = entry.optString("programId", "");
            boolean displayed = false;
            for (WorkProgramEntity program : displayedPrograms) {
                if (program.id.equals(programId)) {
                    displayed = true;
                    break;
                }
            }
            if (displayed) {
                if (!entriesByProgram.containsKey(programId)) {
                    entriesByProgram.put(programId, new ArrayList<>());
                }
                entriesByProgram.get(programId).add(entry);
            } else if (currentCultivarFilter == null && currentSeasonFilter == null) {
                unlinked.add(entry);
            }
        }

        int total = 0;
        int healthy = 0;
        for (List<JSONObject> entries : entriesByProgram.values()) {
            total += entries.size();
            for (JSONObject entry : entries) {
                if (entry.optString("disease", "").toLowerCase(Locale.US).contains("healthy")) {
                    healthy++;
                }
            }
        }
        total += unlinked.size();
        for (JSONObject entry : unlinked) {
            if (entry.optString("disease", "").toLowerCase(Locale.US).contains("healthy")) {
                healthy++;
            }
        }
        int infected = total - healthy;
        tvScanTotal.setText(String.valueOf(total));
        tvScanHealthy.setText(String.format(Locale.US, "%d%%", total == 0 ? 0 : Math.round(healthy * 100f / total)));
        tvScanInfected.setText(String.format(Locale.US, "%d%%", total == 0 ? 0 : Math.round(infected * 100f / total)));

        currentDiseaseGroups = new ArrayList<>();
        if (displayedPrograms.isEmpty() && unlinked.isEmpty()) {
            diseaseEmptyCard.setVisibility(View.VISIBLE);
            diseaseListContainer.setVisibility(View.GONE);
            diseaseListContainer.removeAllViews();
            return;
        }

        diseaseEmptyCard.setVisibility(View.GONE);
        diseaseListContainer.setVisibility(View.VISIBLE);
        diseaseListContainer.removeAllViews();
        boolean tableMode = viewModeSpinner.getSelectedItemPosition() == 0;
        for (WorkProgramEntity program : displayedPrograms) {
            List<JSONObject> entries = entriesByProgram.get(program.id);
            if (entries == null) entries = new ArrayList<>();
            DiseaseExportGroup group = new DiseaseExportGroup(
                    program.cultivarName,
                    program.startingDate != null ? program.startingDate : "N/A",
                    program.areaSize);
            addDiseaseExportRows(group, entries);
            currentDiseaseGroups.add(group);
            diseaseListContainer.addView(buildDiseaseCard(
                    program.id,
                    program.cultivarName,
                    String.format(Locale.getDefault(), "Start: %s | Area: %.2f ha",
                            program.startingDate != null ? program.startingDate : "N/A", program.areaSize),
                    entries,
                    tableMode));
        }
        if (!unlinked.isEmpty()) {
            DiseaseExportGroup group = new DiseaseExportGroup("Unlinked scans", "N/A", 0);
            addDiseaseExportRows(group, unlinked);
            currentDiseaseGroups.add(group);
            diseaseListContainer.addView(buildDiseaseCard(
                    "unlinked",
                    "Unlinked scans",
                    "Scans not linked to a displayed program",
                    unlinked,
                    tableMode));
        }
    }

    private void addDiseaseExportRows(DiseaseExportGroup group, List<JSONObject> entries) {
        List<JSONObject> sortedEntries = new ArrayList<>(entries);
        Collections.sort(sortedEntries, (a, b) -> scanDate(b).compareTo(scanDate(a)));
        for (JSONObject entry : sortedEntries) {
            String disease = entry.optString("disease", "Unknown");
            String status = disease.toLowerCase(Locale.US).contains("healthy") ? "Healthy" : "Infected";
            group.rows.add(new DiseaseExportGroup.Row(
                    displayScanDate(scanDate(entry)),
                    entry.optInt("phase", 0),
                    disease,
                    status,
                    entry.optString("accuracy", "0%")));
        }
    }

    private View buildDiseaseCard(String key, String title, String subtitle, List<JSONObject> entries, boolean tableMode) {
        com.google.android.material.card.MaterialCardView card =
                new com.google.android.material.card.MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(14));
        card.setCardBackgroundColor(Color.WHITE);
        card.setContentPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout headerText = new LinearLayout(this);
        headerText.setOrientation(LinearLayout.VERTICAL);
        headerText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        headerText.addView(diseaseText(title, 16, R.color.text_primary, true));
        headerText.addView(diseaseText(subtitle, 12, R.color.text_secondary, false));
        String scanSummary = "No scans yet";
        if (!entries.isEmpty()) {
            int healthy = 0;
            double confidenceTotal = 0;
            int confidenceCount = 0;
            Map<String, Integer> diseaseCounts = new HashMap<>();
            for (JSONObject entry : entries) {
                String disease = entry.optString("disease", "Unknown");
                if (disease.toLowerCase(Locale.US).contains("healthy")) healthy++;
                diseaseCounts.put(disease, diseaseCounts.containsKey(disease) ? diseaseCounts.get(disease) + 1 : 1);
                Double confidence = parseConfidence(entry.optString("accuracy", ""));
                if (confidence != null) {
                    confidenceTotal += confidence;
                    confidenceCount++;
                }
            }
            String topDisease = "Unknown";
            int topCount = 0;
            for (Map.Entry<String, Integer> disease : diseaseCounts.entrySet()) {
                if (disease.getValue() > topCount) {
                    topDisease = disease.getKey();
                    topCount = disease.getValue();
                }
            }
            List<JSONObject> sortedEntries = new ArrayList<>(entries);
            Collections.sort(sortedEntries, (a, b) -> scanDate(b).compareTo(scanDate(a)));
            double averageConfidence = confidenceCount == 0 ? 0 : confidenceTotal / confidenceCount;
            scanSummary = String.format(Locale.getDefault(),
                    "Scans: %d (%d healthy, %d infected) · Avg confidence: %.0f%% · Top: %s · Last scan: %s",
                    entries.size(), healthy, entries.size() - healthy, averageConfidence, topDisease,
                    displayScanDate(scanDate(sortedEntries.get(0))));
        }
        headerText.addView(diseaseText(scanSummary, 12, R.color.sidebar_dark_green, true));
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(android.R.drawable.arrow_down_float);
        arrow.setRotation(expandedDiseaseCards.contains(key) ? 180f : 0f);
        header.addView(headerText);
        header.addView(arrow, new LinearLayout.LayoutParams(dp(24), dp(24)));
        content.addView(header);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setVisibility(expandedDiseaseCards.contains(key) ? View.VISIBLE : View.GONE);

        if (entries.isEmpty()) {
            body.addView(diseaseText("No scans yet", 12, R.color.sidebar_dark_green, true));
        } else {
            int healthy = 0;
            double confidenceTotal = 0;
            int confidenceCount = 0;
            Map<String, Integer> diseaseCounts = new HashMap<>();
            for (JSONObject entry : entries) {
                String disease = entry.optString("disease", "Unknown");
                if (disease.toLowerCase(Locale.US).contains("healthy")) healthy++;
                diseaseCounts.put(disease, diseaseCounts.containsKey(disease) ? diseaseCounts.get(disease) + 1 : 1);
                Double confidence = parseConfidence(entry.optString("accuracy", ""));
                if (confidence != null) {
                    confidenceTotal += confidence;
                    confidenceCount++;
                }
            }
            String topDisease = "Unknown";
            int topCount = 0;
            for (Map.Entry<String, Integer> disease : diseaseCounts.entrySet()) {
                if (disease.getValue() > topCount) {
                    topDisease = disease.getKey();
                    topCount = disease.getValue();
                }
            }
            List<JSONObject> sortedEntries = new ArrayList<>(entries);
            Collections.sort(sortedEntries, (a, b) -> scanDate(b).compareTo(scanDate(a)));
            double averageConfidence = confidenceCount == 0 ? 0 : confidenceTotal / confidenceCount;
            if (tableMode) {
                body.addView(diseaseTableHeader());
                for (int i = 0; i < sortedEntries.size(); i++) {
                    body.addView(diseaseScanRow(sortedEntries.get(i)));
                    if (i < sortedEntries.size() - 1) {
                        View divider = new View(this);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
                        divider.setBackgroundColor(getResources().getColor(R.color.divider, null));
                        body.addView(divider);
                    }
                }
            } else {
                body.addView(diseaseChart(sortedEntries));
            }
        }
        content.addView(body);
        header.setOnClickListener(v -> {
            if (expandedDiseaseCards.contains(key)) {
                expandedDiseaseCards.remove(key);
                body.setVisibility(View.GONE);
                arrow.setRotation(0f);
            } else {
                expandedDiseaseCards.add(key);
                body.setVisibility(View.VISIBLE);
                arrow.setRotation(180f);
            }
        });
        card.addView(content);
        return card;
    }

    private TextView diseaseText(String text, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor((color >>> 24) == 0xFF ? color : getResources().getColor(color, null));
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setPadding(0, 0, 0, dp(6));
        return view;
    }

    private View diseaseTableHeader() {
        LinearLayout row = diseaseRow();
        row.addView(diseaseCell("Date", 3f, R.color.text_secondary, true));
        row.addView(diseaseCell("Phase", 1.5f, R.color.text_secondary, true));
        row.addView(diseaseCell("Disease", 4f, R.color.text_secondary, true));
        row.addView(diseaseCell("Confidence", 2.5f, R.color.text_secondary, true));
        return row;
    }

    private View diseaseScanRow(JSONObject entry) {
        LinearLayout row = diseaseRow();
        boolean healthy = entry.optString("disease", "").toLowerCase(Locale.US).contains("healthy");
        int diseaseColor = healthy ? R.color.fresh_green : R.color.tomato_red;
        row.addView(diseaseCell(displayScanDate(scanDate(entry)), 3f, R.color.text_primary, false));
        row.addView(diseaseCell(String.valueOf(entry.optInt("phase", 0)), 1.5f, R.color.text_primary, false));
        row.addView(diseaseCell(entry.optString("disease", "Unknown"), 4f, diseaseColor, false));
        row.addView(diseaseCell(entry.optString("accuracy", "0%"), 2.5f, R.color.text_primary, false));
        row.setOnClickListener(v -> openDetection(entry));
        return row;
    }

    private LinearLayout diseaseRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));
        return row;
    }

    private TextView diseaseCell(String text, float weight, int color, boolean bold) {
        TextView cell = diseaseText(text, 12, color, bold);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        return cell;
    }

    private BarChart diseaseChart(List<JSONObject> entries) {
        BarChart chart = new BarChart(this);
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(200)));
        Map<String, Integer> counts = new HashMap<>();
        for (JSONObject entry : entries) {
            String disease = entry.optString("disease", "Unknown");
            counts.put(disease, counts.containsKey(disease) ? counts.get(disease) + 1 : 1);
        }
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        Collections.sort(sorted, (a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<BarEntry> bars = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            bars.add(new BarEntry(i, sorted.get(i).getValue()));
            labels.add(sorted.get(i).getKey());
        }
        BarDataSet dataSet = new BarDataSet(bars, "Scans");
        dataSet.setColors(getResources().getColor(R.color.sidebar_dark_green, null),
                getResources().getColor(R.color.tomato_red, null),
                getResources().getColor(R.color.fresh_green, null),
                getResources().getColor(R.color.scan_blue, null));
        dataSet.setValueTextSize(12f);
        chart.setData(new BarData(dataSet));
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.invalidate();
        return chart;
    }

    private void openDetection(JSONObject entry) {
        Intent intent = new Intent(this, DetectionResults.class);
        intent.putExtra("title", entry.optString("disease", "Unknown"));
        intent.putExtra("imageUri", entry.optString("imageUri", ""));
        intent.putExtra("description", entry.optString("description", ""));
        intent.putExtra("symptoms", entry.optString("symptoms", ""));
        intent.putExtra("cause", entry.optString("cause", ""));
        intent.putExtra("cure", entry.optString("cure", ""));
        intent.putExtra("prevention", entry.optString("prevention", ""));
        intent.putExtra("pestTitle", entry.optString("pestTitle", ""));
        intent.putExtra("pestDescription", entry.optString("pestDescription", ""));
        intent.putExtra("pestImageUri", entry.optString("pestImageUri", ""));
        intent.putExtra("accuracy", entry.optString("accuracy", "0%"));
        intent.putExtra("topPredictions", entry.optString("topPredictions", ""));
        intent.putExtra("confidenceWarning", entry.optString("confidenceWarning", ""));
        intent.putExtra("detectionCultivar", entry.optString("cultivar", getString(R.string.detection_cultivar_unspecified)));
        intent.putExtra("detectionPhase", entry.optInt("phase", 0));
        startActivity(intent);
    }

    private String scanDate(JSONObject entry) {
        String date = DetectionHistoryManager.getScanDate(this, entry.optString("imageUri", ""));
        if (date == null || date.isEmpty()) {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(new Date(entry.optLong("timestamp", 0)));
        }
        return date;
    }

    private String displayScanDate(String date) {
        try {
            Date parsed = SettingsPreferences.getDateParseFormat().parse(date);
            return parsed == null ? date : SettingsPreferences.getDateFormatInstance(this).format(parsed);
        } catch (Exception e) {
            return date;
        }
    }

    private Double parseConfidence(String accuracy) {
        try {
            return Double.parseDouble(accuracy.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
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
            if (programCardsContainer != null) {
                programCardsContainer.setVisibility(View.GONE);
            }
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
            if (programCardsContainer != null) {
                programCardsContainer.setVisibility(View.VISIBLE);
            }
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

    private void bindProgramCards(List<WorkProgramEntity> programs) {
        if (programCardsContainer == null) return;
        programCardsContainer.removeAllViews();
        if (programs == null || programs.isEmpty()) {
            programCardsContainer.setVisibility(View.GONE);
            return;
        }
        if (viewModeSpinner == null || viewModeSpinner.getSelectedItemPosition() == 0) {
            programCardsContainer.setVisibility(View.VISIBLE);
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        for (WorkProgramEntity program : programs) {
            View card = inflater.inflate(R.layout.item_work_program_analytics_card, programCardsContainer, false);
            bindProgramCard(card, program);
            programCardsContainer.addView(card);
        }
    }

    private void bindProgramCard(View card, WorkProgramEntity program) {
        TextView programTitle = card.findViewById(R.id.programTitle);
        TextView programSubtitle = card.findViewById(R.id.programSubtitle);
        TextView programSummaryPeek = card.findViewById(R.id.programSummaryPeek);
        TextView adjustedIncome = card.findViewById(R.id.adjustedIncome);
        TextView adjustedExpenses = card.findViewById(R.id.adjustedExpenses);
        TextView adjustedProfit = card.findViewById(R.id.adjustedProfit);
        TextView projectedProfit = card.findViewById(R.id.projectedProfit);
        TextView completionRate = card.findViewById(R.id.completionRate);
        TextView loggedExpenses = card.findViewById(R.id.loggedExpenses);
        android.widget.ProgressBar programProgressBar = card.findViewById(R.id.programProgressBar);
        View programHeader = card.findViewById(R.id.programHeader);
        View programDetails = card.findViewById(R.id.programDetails);
        ImageView expandIcon = card.findViewById(R.id.expandIcon);
        com.google.android.material.button.MaterialButton openProgramButton = card.findViewById(R.id.btnOpenProgram);
        com.google.android.material.button.MaterialButton financeButton = card.findViewById(R.id.btnOpenFinance);

        String title = program.cultivarName != null ? program.cultivarName : "Saved work program";
        if (programTitle != null) programTitle.setText(title);
        if (programSubtitle != null) {
            programSubtitle.setText(String.format(Locale.getDefault(), "Start: %s | Area: %.2f ha",
                    program.startingDate != null ? program.startingDate : "N/A", program.areaSize));
        }
        double displayedExpenses = program.loggedExpenses > 0 ? program.loggedExpenses : program.adjustedExpenses;
        String peek = String.format(Locale.getDefault(), "Logged expenses: ₱%,.0f · Completion: %.0f%%",
                displayedExpenses, program.completionRate);
        if (programSummaryPeek != null) programSummaryPeek.setText(peek);
        if (adjustedIncome != null) {
            adjustedIncome.setText(String.format(Locale.getDefault(), "₱%,.0f", program.adjustedIncome));
        }
        if (adjustedExpenses != null) {
            adjustedExpenses.setText(String.format(Locale.getDefault(), "₱%,.0f", displayedExpenses));
        }
        if (adjustedProfit != null) {
            adjustedProfit.setText(String.format(Locale.getDefault(), "Adj profit: ₱%,.0f",
                    program.adjustedIncome - displayedExpenses));
        }
        if (projectedProfit != null) {
            projectedProfit.setText(String.format(Locale.getDefault(), "Projected profit: ₱%,.0f",
                    program.projectedIncome - program.projectedExpenses));
        }
        if (loggedExpenses != null) {
            loggedExpenses.setText(String.format(Locale.getDefault(), "Logged expenses: ₱%,.0f", displayedExpenses));
        }
        if (completionRate != null) {
            completionRate.setText(String.format(Locale.getDefault(), "Completion: %.0f%%", program.completionRate));
        }
        if (programProgressBar != null) {
            programProgressBar.setProgress((int) Math.round(Math.max(0, Math.min(100, program.completionRate))));
        }

        boolean expanded = program.id != null && expandedProgramIds.contains(program.id);
        applyCardExpandedState(programDetails, expandIcon, expanded);

        View.OnClickListener toggle = v -> {
            if (program.id == null) return;
            if (expandedProgramIds.contains(program.id)) {
                expandedProgramIds.remove(program.id);
            } else {
                expandedProgramIds.add(program.id);
            }
            applyCardExpandedState(programDetails, expandIcon, expandedProgramIds.contains(program.id));
        };
        if (programHeader != null) programHeader.setOnClickListener(toggle);
        card.setOnClickListener(toggle);

        if (openProgramButton != null) {
            openProgramButton.setOnClickListener(v -> openWorkProgram(program));
        }
        if (financeButton != null) {
            financeButton.setOnClickListener(v -> openFinance(program));
        }
    }

    private void applyCardExpandedState(View details, ImageView expandIcon, boolean expanded) {
        if (details != null) {
            details.setVisibility(expanded ? View.VISIBLE : View.GONE);
        }
        if (expandIcon != null) {
            expandIcon.setRotation(expanded ? 180f : 0f);
            expandIcon.setContentDescription(expanded ? "Collapse program details" : "Expand program details");
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

    private void exportDiseaseCsv() {
        boolean hasRows = false;
        for (DiseaseExportGroup group : currentDiseaseGroups) {
            if (!group.rows.isEmpty()) {
                hasRows = true;
                break;
            }
        }
        if (!hasRows) {
            Toast.makeText(this, "No scan records to export", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DiseaseExportGroup> groups = new ArrayList<>(currentDiseaseGroups);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            String filePath = ResearchExporter.exportDiseaseToCsv(this, groups);
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

    private void exportDiseasePdf() {
        boolean hasRows = false;
        for (DiseaseExportGroup group : currentDiseaseGroups) {
            if (!group.rows.isEmpty()) {
                hasRows = true;
                break;
            }
        }
        if (!hasRows) {
            Toast.makeText(this, "No scan records to export", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DiseaseExportGroup> groups = new ArrayList<>(currentDiseaseGroups);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            String filePath = AnalyticsPdfExporter.exportDiseaseToPdf(this, groups);
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
