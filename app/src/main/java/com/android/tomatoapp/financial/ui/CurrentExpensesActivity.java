package com.android.tomatoapp.financial.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.android.tomatoapp.R;
import com.android.tomatoapp.common.utils.PhaseHelper;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;
import com.android.tomatoapp.settings.data.SettingsPreferences;
import com.android.tomatoapp.task.data.TaskSchedule;

public class CurrentExpensesActivity extends BaseBottomNavActivity {

    private TextView cultivarHeader;
    private TextView dateRangeHeader;
    private RecyclerView phase1Table, phase2Table, phase3Table, phase4Table, phase5Table;
    private MaterialCardView phase1Card, phase2Card, phase3Card, phase4Card, phase5Card;
    private String programId;
    private String cultivar;
    private String startDate;
    private DatabaseReference expensesRef;
    private FirebaseUser currentUser;
    private int maturityDays;
    private Map<Integer, List<ExpenseEntry>> allExpensesByPhase = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_expenses);

        setupBottomNavigation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Current Expenses");
        }

        // Get intent data
        programId = getIntent().getStringExtra("programId");
        cultivar = getIntent().getStringExtra("cultivar");
        startDate = getIntent().getStringExtra("startDate");

        // Check if user is logged in
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || programId == null) {
            Toast.makeText(this, "Error: Missing required information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        cultivarHeader = findViewById(R.id.cultivarHeader);
        dateRangeHeader = findViewById(R.id.dateRangeHeader);
        phase1Table = findViewById(R.id.phase1Table);
        phase2Table = findViewById(R.id.phase2Table);
        phase3Table = findViewById(R.id.phase3Table);
        phase4Table = findViewById(R.id.phase4Table);
        phase5Table = findViewById(R.id.phase5Table);
        phase1Card = findViewById(R.id.phase1Card);
        phase2Card = findViewById(R.id.phase2Card);
        phase3Card = findViewById(R.id.phase3Card);
        phase4Card = findViewById(R.id.phase4Card);
        phase5Card = findViewById(R.id.phase5Card);
        
        // Setup RecyclerViews
        phase1Table.setLayoutManager(new LinearLayoutManager(this));
        phase2Table.setLayoutManager(new LinearLayoutManager(this));
        phase3Table.setLayoutManager(new LinearLayoutManager(this));
        phase4Table.setLayoutManager(new LinearLayoutManager(this));
        phase5Table.setLayoutManager(new LinearLayoutManager(this));

        // Set headers
        if (cultivar != null) {
            cultivarHeader.setText("Cultivar: " + cultivar);
        }

        // Get maturity days for this cultivar
        maturityDays = getMaturityDays(cultivar);

        // Initialize Firebase reference
        expensesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("workPrograms")
                .child(programId)
                .child("dailyExpenses");

        // Load expenses data
        loadExpensesData();
        
        // Setup export buttons
        com.google.android.material.button.MaterialButton btnExportPDF = findViewById(R.id.btnExportPDF);
        com.google.android.material.button.MaterialButton btnExportCSV = findViewById(R.id.btnExportCSV);
        
        if (btnExportPDF != null) {
            btnExportPDF.setOnClickListener(v -> exportToPDF());
        }
        
        if (btnExportCSV != null) {
            btnExportCSV.setOnClickListener(v -> exportToCSV());
        }
    }

    private String getActivityNameFromPhase(int phase) {
        switch (phase) {
            case 1:
                return "Land Preparation";
            case 2:
                return "Vegetative";
            case 3:
                return "Flowering";
            case 4:
                return "Maturity";
            case 5:
                return "Post-harvest";
            default:
                return "";
        }
    }
    
    private int getMaturityDays(String cultivar) {
        // Get maturity days from cultivars data (same as Workprogram)
        String[][] cultivarsData = {
            {"Victory F1", "Semi-determinate", "90", "110"},
            {"HOPE F1", "Semi-determinate", "90", "110"},
            {"Maganda F1", "Semi-determinate", "80", "100"},
            {"Malakas F1", "Semi-determinate", "95", "115"},
            {"Rocky 1 F1", "Semi-determinate", "90", "110"},
            {"Improved KS Apollo", "Semi-determinate", "85", "105"},
            {"Improved Pope", "Semi-determinate", "85", "105"},
            {"Super Pope", "Semi-determinate", "85", "105"},
            {"Maguilas", "Determinate", "85", "105"},
            {"Maunlad", "Determinate", "80", "100"},
            {"Mapalad", "Determinate", "80", "100"},
            {"Amari F1", "Semi-determinate", "110", "130"},
            {"Anita F1", "Semi-determinate", "110", "130"},
            {"Colette F1", "Determinate", "105", "125"},
            {"Danica F1", "Semi-determinate", "105", "125"},
            {"Granger F1", "Semi-determinate", "105", "125"},
            {"Janet F1", "Semi-determinate", "120", "140"},
            {"Platinum F1", "Semi-determinate", "100", "120"},
            {"Reina F1", "Semi-determinate", "105", "125"},
            {"Renata F1", "Semi-determinate", "105", "125"},
            {"Rubellite F1", "Semi-determinate", "90", "110"},
            {"TOM-055 F1", "Semi-determinate", "60", "75"},
            {"TOM-262 OP", "Determinate", "60", "75"},
            {"Dalwangan Tm1", "Determinate", "90", "110"},
            {"Dalwangan Tm2", "Determinate", "90", "110"},
            {"NSIC 1999 Tm09", "Determinate", "100", "120"},
            {"Mara", "Determinate", "78", "95"},
            {"AniMax 1", "Determinate", "87", "105"},
            {"AniMax 2", "Semi-determinate", "87", "105"},
            {"Golden Globe", "Semi-determinate", "92", "112"},
            {"Maxxime", "Indeterminate", "105", "125"}
        };

        for (String[] c : cultivarsData) {
            if (c[0].equals(cultivar)) {
                try {
                    return Integer.parseInt(c[3]); // use max maturity days
                } catch (NumberFormatException e) {
                    return 90; // default
                }
            }
        }
        return 90; // default
    }

    private void loadExpensesData() {
        if (expensesRef == null || startDate == null) return;

        expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Process expenses data - create one row per expense item
                Map<Integer, List<ExpenseEntry>> expensesByPhase = new HashMap<>();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();
                    if (dateKey == null) continue;
                    
                    try {
                        Date expenseDate = sdf.parse(dateKey);
                        Date programStart = sdf.parse(startDate);
                        int phase = 1; // Default phase
                        
                        if (expenseDate != null && programStart != null) {
                            long diff = expenseDate.getTime() - programStart.getTime();
                            int dayNumber = (int) (diff / (1000 * 60 * 60 * 24)) + 1;
                            
                            if (dayNumber > 0 && maturityDays > 0) {
                                phase = TaskSchedule.getPhaseNumber(maturityDays, dayNumber);
                            }
                        }
                        
                        // Process labor items
                        if (dateSnapshot.hasChild("labor")) {
                            String activityName = getActivityNameFromPhase(phase);
                            for (DataSnapshot laborSnapshot : dateSnapshot.child("labor").getChildren()) {
                                Integer numWorkers = laborSnapshot.child("numWorkers").getValue(Integer.class);
                                Double totalCost = laborSnapshot.child("totalCost").getValue(Double.class);
                                String notes = laborSnapshot.child("notes").getValue(String.class);
                                
                                ExpenseEntry entry = new ExpenseEntry(
                                    dateKey,
                                    activityName, // Activity from phase
                                    "Hired Work",
                                    totalCost != null ? totalCost : 0.0,
                                    numWorkers != null ? numWorkers : 0,
                                    0.0, // materialCost
                                    0.0, // equipmentCost
                                    0.0, // miscCost
                                    notes != null ? notes : "" // Notes contains specific activity like "Plowing"
                                );
                                
                                if (!expensesByPhase.containsKey(phase)) {
                                    expensesByPhase.put(phase, new ArrayList<>());
                                }
                                expensesByPhase.get(phase).add(entry);
                            }
                        }
                        
                        // Process material items
                        if (dateSnapshot.hasChild("material")) {
                            String activityName = getActivityNameFromPhase(phase);
                            for (DataSnapshot materialSnapshot : dateSnapshot.child("material").getChildren()) {
                                Double totalCost = materialSnapshot.child("totalCost").getValue(Double.class);
                                String materialName = materialSnapshot.child("materialName").getValue(String.class);
                                String quantityUnit = materialSnapshot.child("quantityUnit").getValue(String.class);
                                Double quantity = materialSnapshot.child("quantity").getValue(Double.class);
                                
                                String notes = materialName != null ? materialName : "";
                                if (quantity != null && quantityUnit != null && !quantityUnit.isEmpty()) {
                                    notes += " (" + quantity + " " + quantityUnit + ")";
                                }
                                
                                ExpenseEntry entry = new ExpenseEntry(
                                    dateKey,
                                    activityName, // Activity from phase
                                    "Material",
                                    0.0, // laborCost
                                    0, // numWorkers
                                    totalCost != null ? totalCost : 0.0,
                                    0.0, // equipmentCost
                                    0.0, // miscCost
                                    notes
                                );
                                
                                if (!expensesByPhase.containsKey(phase)) {
                                    expensesByPhase.put(phase, new ArrayList<>());
                                }
                                expensesByPhase.get(phase).add(entry);
                            }
                        }
                        
                        // Process equipment items
                        if (dateSnapshot.hasChild("equipment")) {
                            String activityName = getActivityNameFromPhase(phase);
                            for (DataSnapshot equipmentSnapshot : dateSnapshot.child("equipment").getChildren()) {
                                // Get totalCost if available (new format), otherwise calculate from cost
                                Double totalCost = equipmentSnapshot.child("totalCost").getValue(Double.class);
                                String equipmentName = equipmentSnapshot.child("equipmentName").getValue(String.class);
                                
                                // Build notes with usage information
                                String notes = equipmentName != null ? equipmentName : "";
                                String usageStr = "";
                                
                                // Handle new format (usageValue + usageUnit)
                                if (equipmentSnapshot.hasChild("usageValue")) {
                                    Double usageValue = equipmentSnapshot.child("usageValue").getValue(Double.class);
                                    String usageUnit = equipmentSnapshot.child("usageUnit").getValue(String.class);
                                    if (usageValue != null && usageValue > 0) {
                                        usageStr = usageValue + " " + (usageUnit != null ? usageUnit : "hours");
                                    }
                                } else if (equipmentSnapshot.hasChild("usage")) {
                                    // Legacy format
                                    String usage = equipmentSnapshot.child("usage").getValue(String.class);
                                    if (usage != null && !usage.isEmpty()) {
                                        usageStr = usage;
                                    }
                                }
                                
                                if (!usageStr.isEmpty()) {
                                    notes += " (" + usageStr + ")";
                                }
                                
                                ExpenseEntry entry = new ExpenseEntry(
                                    dateKey,
                                    activityName, // Activity from phase
                                    "Equipment/Tools",
                                    0.0, // laborCost
                                    0, // numWorkers
                                    0.0, // materialCost
                                    totalCost != null ? totalCost : 0.0, // Use totalCost instead of cost
                                    0.0, // miscCost
                                    notes
                                );
                                
                                if (!expensesByPhase.containsKey(phase)) {
                                    expensesByPhase.put(phase, new ArrayList<>());
                                }
                                expensesByPhase.get(phase).add(entry);
                            }
                        }
                        
                        // Process miscellaneous items
                        if (dateSnapshot.hasChild("miscellaneous")) {
                            String activityName = getActivityNameFromPhase(phase);
                            for (DataSnapshot miscSnapshot : dateSnapshot.child("miscellaneous").getChildren()) {
                                Double cost = miscSnapshot.child("cost").getValue(Double.class);
                                String expenseName = miscSnapshot.child("expenseName").getValue(String.class);
                                
                                ExpenseEntry entry = new ExpenseEntry(
                                    dateKey,
                                    activityName, // Activity from phase
                                    "Miscellaneous",
                                    0.0, // laborCost
                                    0, // numWorkers
                                    0.0, // materialCost
                                    0.0, // equipmentCost
                                    cost != null ? cost : 0.0,
                                    expenseName != null ? expenseName : ""
                                );
                                
                                if (!expensesByPhase.containsKey(phase)) {
                                    expensesByPhase.put(phase, new ArrayList<>());
                                }
                                expensesByPhase.get(phase).add(entry);
                            }
                        }
                        
                    } catch (Exception e) {
                        // Skip invalid dates
                    }
                }
                
                // Display expenses in tables
                displayExpensesByPhase(expensesByPhase);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CurrentExpensesActivity.this, "Error loading expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayExpensesByPhase(Map<Integer, List<ExpenseEntry>> expensesByPhase) {
        allExpensesByPhase = expensesByPhase;
        
        // Display each phase's expenses
        displayPhaseExpenses(1, phase1Table, phase1Card, expensesByPhase.get(1));
        displayPhaseExpenses(2, phase2Table, phase2Card, expensesByPhase.get(2));
        displayPhaseExpenses(3, phase3Table, phase3Card, expensesByPhase.get(3));
        displayPhaseExpenses(4, phase4Table, phase4Card, expensesByPhase.get(4));
        displayPhaseExpenses(5, phase5Table, phase5Card, expensesByPhase.get(5));
        
        // Update date range header
        updateDateRangeHeader(expensesByPhase);
    }
    
    private void displayPhaseExpenses(int phase, RecyclerView recyclerView, MaterialCardView card, List<ExpenseEntry> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            card.setVisibility(View.GONE);
            return;
        }
        
        card.setVisibility(View.VISIBLE);
        
        // Update card title with full phase description
        updatePhaseCardTitle(card, phase);
        
        ExpenseTableAdapter adapter = new ExpenseTableAdapter(expenses);
        recyclerView.setAdapter(adapter);
    }
    
    private void updatePhaseCardTitle(MaterialCardView card, int phase) {
        if (card == null) return;
        
        // Find the TextView that contains the phase title
        TextView phaseTitle = findPhaseTitleTextView(card);
        if (phaseTitle != null) {
            phaseTitle.setText(PhaseHelper.getPhaseNameWithDescription(phase));
        }
    }
    
    private TextView findPhaseTitleTextView(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                String text = textView.getText().toString();
                if (text.startsWith("Phase")) {
                    return textView;
                }
            } else if (child instanceof ViewGroup) {
                TextView found = findPhaseTitleTextView((ViewGroup) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    private void updateDateRangeHeader(Map<Integer, List<ExpenseEntry>> expensesByPhase) {
        String earliestDate = null;
        String latestDate = null;
        
        for (List<ExpenseEntry> entries : expensesByPhase.values()) {
            for (ExpenseEntry entry : entries) {
                if (earliestDate == null || entry.date.compareTo(earliestDate) < 0) {
                    earliestDate = entry.date;
                }
                if (latestDate == null || entry.date.compareTo(latestDate) > 0) {
                    latestDate = entry.date;
                }
            }
        }
        
        if (earliestDate != null && latestDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(this);
                Date start = sdf.parse(earliestDate);
                Date end = sdf.parse(latestDate);
                if (start != null && end != null) {
                    dateRangeHeader.setText("Date Range: " + displayFormat.format(start) + " to " + displayFormat.format(end));
                }
            } catch (Exception e) {
                dateRangeHeader.setText("Date Range: " + earliestDate + " to " + latestDate);
            }
        } else {
            dateRangeHeader.setText("No expenses recorded yet");
        }
    }
    
    // Table Adapter
    private class ExpenseTableAdapter extends RecyclerView.Adapter<ExpenseTableAdapter.ExpenseViewHolder> {
        private final List<ExpenseEntry> expenses;
        private final SimpleDateFormat displayFormat;
        
        ExpenseTableAdapter(List<ExpenseEntry> expenses) {
            this.expenses = expenses;
            this.displayFormat = SettingsPreferences.getDateFormatInstance(CurrentExpensesActivity.this);
        }
        
        @NonNull
        @Override
        public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_expense_table_row, parent, false);
            return new ExpenseViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
            ExpenseEntry entry = expenses.get(position);
            
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(entry.date);
                if (date != null) {
                    holder.dateText.setText(displayFormat.format(date));
                } else {
                    holder.dateText.setText(entry.date);
                }
            } catch (Exception e) {
                holder.dateText.setText(entry.date);
            }
            
            holder.activityText.setText(entry.activity != null ? entry.activity : "");
            holder.categoryText.setText(entry.category != null ? entry.category : "");
            holder.laborCostText.setText(entry.laborCost > 0 ? String.format("₱%,.2f", entry.laborCost) : "₱0");
            holder.workersText.setText(entry.numWorkers > 0 ? String.valueOf(entry.numWorkers) : "0");
            holder.materialCostText.setText(entry.materialCost > 0 ? String.format("₱%,.2f", entry.materialCost) : "₱0");
            holder.equipmentCostText.setText(entry.equipmentCost > 0 ? String.format("₱%,.2f", entry.equipmentCost) : "₱0");
            holder.miscCostText.setText(entry.miscCost > 0 ? String.format("₱%,.2f", entry.miscCost) : "₱0");
            holder.notesText.setText(entry.notes != null ? entry.notes : "");
            holder.totalText.setText(String.format("₱%,.2f", entry.getTotal()));
        }
        
        @Override
        public int getItemCount() {
            return expenses.size();
        }
        
        class ExpenseViewHolder extends RecyclerView.ViewHolder {
            TextView dateText, activityText, categoryText, laborCostText, workersText;
            TextView materialCostText, equipmentCostText, miscCostText, notesText, totalText;
            
            ExpenseViewHolder(@NonNull View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.dateText);
                activityText = itemView.findViewById(R.id.activityText);
                categoryText = itemView.findViewById(R.id.categoryText);
                laborCostText = itemView.findViewById(R.id.laborCostText);
                workersText = itemView.findViewById(R.id.workersText);
                materialCostText = itemView.findViewById(R.id.materialCostText);
                equipmentCostText = itemView.findViewById(R.id.equipmentCostText);
                miscCostText = itemView.findViewById(R.id.miscCostText);
                notesText = itemView.findViewById(R.id.notesText);
                totalText = itemView.findViewById(R.id.totalText);
            }
        }
    }

    private static class ExpenseEntry {
        String date;
        String activity; // From labor notes
        String category; // Hired Work, Material, Equipment/Tools, Miscellaneous
        double laborCost;
        int numWorkers;
        double materialCost;
        double equipmentCost;
        double miscCost;
        String notes;
        
        ExpenseEntry(String date, String activity, String category, double laborCost, int numWorkers,
                    double materialCost, double equipmentCost, double miscCost, String notes) {
            this.date = date;
            this.activity = activity != null ? activity : "";
            this.category = category != null ? category : "";
            this.laborCost = laborCost;
            this.numWorkers = numWorkers;
            this.materialCost = materialCost;
            this.equipmentCost = equipmentCost;
            this.miscCost = miscCost;
            this.notes = notes != null ? notes : "";
        }
        
        double getTotal() {
            return laborCost + materialCost + equipmentCost + miscCost;
        }
    }
    
    private void exportToPDF() {
        if (allExpensesByPhase.isEmpty()) {
            Toast.makeText(this, "No expenses to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
                return;
            }
        }
        
        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            
            android.graphics.Canvas canvas = page.getCanvas();
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setTextSize(16);
            paint.setColor(android.graphics.Color.BLACK);
            
            float y = 50;
            float margin = 50;
            
            // Title
            paint.setTextSize(24);
            paint.setFakeBoldText(true);
            canvas.drawText("Current Expenses Report", margin, y, paint);
            y += 40;
            
            paint.setTextSize(14);
            paint.setFakeBoldText(false);
            if (cultivar != null) {
                canvas.drawText("Cultivar: " + cultivar, margin, y, paint);
                y += 25;
            }
            
            String dateRange = dateRangeHeader.getText().toString();
            canvas.drawText(dateRange, margin, y, paint);
            y += 40;
            
            // Export each phase
            String[] phaseNames = {
                "Phase 1: Land & Soil Preparation",
                "Phase 2: Vegetative",
                "Phase 3: Flowering",
                "Phase 4: Maturity",
                "Phase 5: Post-harvest"
            };
            
            for (int phase = 1; phase <= 5; phase++) {
                List<ExpenseEntry> expenses = allExpensesByPhase.get(phase);
                if (expenses == null || expenses.isEmpty()) continue;
                
                if (y > 750) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }
                
                paint.setTextSize(18);
                paint.setFakeBoldText(true);
                canvas.drawText(phaseNames[phase - 1], margin, y, paint);
                y += 30;
                
                paint.setTextSize(9);
                paint.setFakeBoldText(false);
                canvas.drawText("Date          Activity         Category   Labor    Workers Material Equipment Misc     Notes     Total", margin, y, paint);
                y += 20;
                
                double phaseTotal = 0;
                for (ExpenseEntry entry : expenses) {
                    if (y > 800) {
                        document.finishPage(page);
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        y = 50;
                    }
                    
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(this);
                        Date date = sdf.parse(entry.date);
                        String dateStr = date != null ? displayFormat.format(date) : entry.date;
                        if (dateStr.length() > 12) dateStr = dateStr.substring(0, 12);
                        String activity = entry.activity != null && entry.activity.length() > 12 ? entry.activity.substring(0, 12) : (entry.activity != null ? entry.activity : "");
                        String category = entry.category != null && entry.category.length() > 10 ? entry.category.substring(0, 10) : (entry.category != null ? entry.category : "");
                        String notes = entry.notes != null && entry.notes.length() > 10 ? entry.notes.substring(0, 10) : (entry.notes != null ? entry.notes : "");
                        
                        String line = String.format(Locale.getDefault(), 
                            "%-12s %-15s %-10s %8.2f  %4d     %8.2f  %9.2f  %8.2f  %-10s %8.2f",
                            dateStr, activity, category,
                            entry.laborCost, entry.numWorkers, entry.materialCost,
                            entry.equipmentCost, entry.miscCost, notes, entry.getTotal());
                        canvas.drawText(line, margin, y, paint);
                        y += 18;
                        phaseTotal += entry.getTotal();
                    } catch (Exception e) {
                        // Skip invalid entries
                    }
                }
                
                paint.setFakeBoldText(true);
                canvas.drawText(String.format(Locale.getDefault(), "Phase %d Total: ₱%,.2f", phase, phaseTotal), margin, y, paint);
                y += 30;
            }
            
            document.finishPage(page);
            
            // Save file
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "Expenses_" + cultivar + "_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".pdf";
            File file = new File(downloadsDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            
            Toast.makeText(this, "PDF exported to Downloads: " + fileName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void exportToCSV() {
        if (allExpensesByPhase.isEmpty()) {
            Toast.makeText(this, "No expenses to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1002);
                return;
            }
        }
        
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "Expenses_" + cultivar + "_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".csv";
            File file = new File(downloadsDir, fileName);
            
            FileWriter writer = new FileWriter(file);
            
            // Write header
            writer.append("Cultivar,").append(cultivar != null ? cultivar : "").append("\n");
            writer.append("Date Range,").append(dateRangeHeader.getText().toString()).append("\n\n");
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(this);
            
            String[] phaseNames = {
                "Phase 1: Land & Soil Preparation",
                "Phase 2: Vegetative",
                "Phase 3: Flowering",
                "Phase 4: Maturity",
                "Phase 5: Post-harvest"
            };
            
            // Write each phase
            for (int phase = 1; phase <= 5; phase++) {
                List<ExpenseEntry> expenses = allExpensesByPhase.get(phase);
                if (expenses == null || expenses.isEmpty()) continue;
                
                writer.append("\n").append(phaseNames[phase - 1]).append("\n");
                writer.append("Date,Activity,Category,Labor Cost,Number of Workers,Material Cost,Equipment/Tools Cost,Miscellaneous Cost,Notes,Total Cost\n");
                
                double phaseTotal = 0;
                for (ExpenseEntry entry : expenses) {
                    try {
                        Date date = sdf.parse(entry.date);
                        String dateStr = date != null ? displayFormat.format(date) : entry.date;
                        String activity = entry.activity != null ? entry.activity.replace(",", ";") : "";
                        String category = entry.category != null ? entry.category.replace(",", ";") : "";
                        String notes = entry.notes != null ? entry.notes.replace(",", ";") : "";
                        
                        writer.append(dateStr).append(",")
                              .append(activity).append(",")
                              .append(category).append(",")
                              .append(String.format(Locale.getDefault(), "%.2f", entry.laborCost)).append(",")
                              .append(String.valueOf(entry.numWorkers)).append(",")
                              .append(String.format(Locale.getDefault(), "%.2f", entry.materialCost)).append(",")
                              .append(String.format(Locale.getDefault(), "%.2f", entry.equipmentCost)).append(",")
                              .append(String.format(Locale.getDefault(), "%.2f", entry.miscCost)).append(",")
                              .append(notes).append(",")
                              .append(String.format(Locale.getDefault(), "%.2f", entry.getTotal())).append("\n");
                        phaseTotal += entry.getTotal();
                    } catch (Exception e) {
                        // Skip invalid entries
                    }
                }
                writer.append("Phase Total,,,,,,,,").append(String.format(Locale.getDefault(), "%.2f", phaseTotal)).append("\n");
            }
            
            writer.flush();
            writer.close();
            
            Toast.makeText(this, "CSV exported to Downloads: " + fileName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error exporting CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 1001) {
                exportToPDF();
            } else if (requestCode == 1002) {
                exportToCSV();
            }
        } else {
            Toast.makeText(this, "Permission denied. Cannot export file.", Toast.LENGTH_SHORT).show();
        }
    }
}

