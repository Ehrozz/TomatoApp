package com.android.tomatoapp.financial.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DailyExpensesHistoryActivity extends BaseDrawerActivity {

    private RecyclerView recyclerView;
    private TextView emptyState;
    private TextView headerText;
    private String programId;
    private String cultivar;
    private String startDate;
    private DatabaseReference expensesRef;
    private FirebaseUser currentUser;
    private ExpenseAdapter adapter;
    private List<ExpenseEntry> expenseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_expenses_history);

        setupDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daily Expenses History");
        }

        // Get intent data
        programId = getIntent().getStringExtra("programId");
        cultivar = getIntent().getStringExtra("cultivar");
        startDate = getIntent().getStringExtra("startDate");

        // Check if user is logged in
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || programId == null) {
            finish();
            return;
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        emptyState = findViewById(R.id.emptyState);
        headerText = findViewById(R.id.headerText);

        // Set header
        if (cultivar != null) {
            headerText.setText("Daily Expenses: " + cultivar);
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(expenseList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase reference
        expensesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("workPrograms")
                .child(programId)
                .child("dailyExpenses");

        // Load expenses data
        loadExpensesData();
    }

    private void loadExpensesData() {
        if (expensesRef == null) return;

        expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseList.clear();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(DailyExpensesHistoryActivity.this);
                
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();
                    if (dateKey == null) continue;
                    
                    try {
                        Date expenseDate = sdf.parse(dateKey);
                        if (expenseDate == null) continue;
                        
                        // Aggregate expenses by category for this date
                        double laborCost = 0.0;
                        double materialCost = 0.0;
                        double equipmentCost = 0.0;
                        double miscellaneousCost = 0.0;
                        int totalWorkers = 0;
                        
                        // Process labor items
                        if (dateSnapshot.hasChild("labor")) {
                            for (DataSnapshot laborSnapshot : dateSnapshot.child("labor").getChildren()) {
                                Double totalCost = laborSnapshot.child("totalCost").getValue(Double.class);
                                Integer numWorkers = laborSnapshot.child("numWorkers").getValue(Integer.class);
                                if (totalCost != null) laborCost += totalCost;
                                if (numWorkers != null) totalWorkers += numWorkers;
                            }
                        }
                        
                        // Process material items
                        if (dateSnapshot.hasChild("material")) {
                            for (DataSnapshot materialSnapshot : dateSnapshot.child("material").getChildren()) {
                                Double totalCost = materialSnapshot.child("totalCost").getValue(Double.class);
                                if (totalCost != null) materialCost += totalCost;
                            }
                        }
                        
                        // Process equipment items (only non-owned)
                        if (dateSnapshot.hasChild("equipment")) {
                            for (DataSnapshot equipmentSnapshot : dateSnapshot.child("equipment").getChildren()) {
                                Boolean isOwned = equipmentSnapshot.child("isOwned").getValue(Boolean.class);
                                // Skip owned equipment
                                if (isOwned != null && isOwned) continue;
                                
                                Double totalCost = equipmentSnapshot.child("totalCost").getValue(Double.class);
                                if (totalCost != null) equipmentCost += totalCost;
                            }
                        }
                        
                        // Process miscellaneous items
                        if (dateSnapshot.hasChild("miscellaneous")) {
                            for (DataSnapshot miscSnapshot : dateSnapshot.child("miscellaneous").getChildren()) {
                                Double cost = miscSnapshot.child("cost").getValue(Double.class);
                                if (cost != null) miscellaneousCost += cost;
                            }
                        }
                        
                        // Only create entry if there are expenses for this date
                        double total = laborCost + materialCost + equipmentCost + miscellaneousCost;
                        if (total > 0) {
                        ExpenseEntry entry = new ExpenseEntry(
                            dateKey,
                            displayFormat.format(expenseDate),
                                laborCost,
                                materialCost,
                                equipmentCost,
                                miscellaneousCost,
                                totalWorkers
                        );
                        
                        expenseList.add(entry);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Skip invalid dates
                    }
                }
                
                // Sort by date (newest first)
                Collections.sort(expenseList, (e1, e2) -> {
                    try {
                        Date d1 = sdf.parse(e1.dateKey);
                        Date d2 = sdf.parse(e2.dateKey);
                        if (d1 != null && d2 != null) {
                            return d2.compareTo(d1); // Descending order
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                    return 0;
                });
                
                // Update UI
                adapter.notifyDataSetChanged();
                
                if (expenseList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show error state
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                emptyState.setText("Error loading expenses");
            }
        });
    }

    private static class ExpenseEntry {
        String dateKey;
        String displayDate;
        double laborCost;
        double materialCost;
        double equipmentCost;
        double miscellaneousCost;
        int numWorkers;

        ExpenseEntry(String dateKey, String displayDate, double laborCost, double materialCost, 
                    double equipmentCost, double miscellaneousCost, int numWorkers) {
            this.dateKey = dateKey;
            this.displayDate = displayDate;
            this.laborCost = laborCost;
            this.materialCost = materialCost;
            this.equipmentCost = equipmentCost;
            this.miscellaneousCost = miscellaneousCost;
            this.numWorkers = numWorkers;
        }

        double getTotal() {
            return laborCost + materialCost + equipmentCost + miscellaneousCost;
        }
    }

    private class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
        private final List<ExpenseEntry> expenses;
        private final Set<Integer> expandedPositions = new HashSet<>();

        ExpenseAdapter(List<ExpenseEntry> expenses) {
            this.expenses = expenses;
        }

        @NonNull
        @Override
        public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_daily_expense_history, parent, false);
            return new ExpenseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
            ExpenseEntry entry = expenses.get(position);
            boolean isExpanded = expandedPositions.contains(position);
            
            holder.dateText.setText(entry.displayDate);
            holder.totalTextSummary.setText(String.format(Locale.getDefault(), "₱%,.2f", entry.getTotal()));
            holder.laborText.setText(String.format(Locale.getDefault(), "₱%,.2f", entry.laborCost));
            holder.materialText.setText(String.format(Locale.getDefault(), "₱%,.2f", entry.materialCost));
            holder.equipmentText.setText(String.format(Locale.getDefault(), "₱%,.2f", entry.equipmentCost));
            holder.miscellaneousText.setText(String.format(Locale.getDefault(), "₱%,.2f", entry.miscellaneousCost));
            
            // Show workers count only if there are labor expenses
            if (entry.laborCost > 0 && entry.numWorkers > 0) {
                holder.workersText.setText(String.format(Locale.getDefault(), "%d worker(s)", entry.numWorkers));
                holder.workersText.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            } else {
                holder.workersText.setVisibility(View.GONE);
            }
            
            // Set expanded/collapsed state (default: collapsed)
            holder.detailsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.expandIcon.setText(isExpanded ? "▼" : "▶");
            
            // Set click listener on header
            holder.headerLayout.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;
                
                if (expandedPositions.contains(adapterPosition)) {
                    expandedPositions.remove(adapterPosition);
                } else {
                    expandedPositions.add(adapterPosition);
                }
                // Notify adapter to update the view
                notifyItemChanged(adapterPosition);
            });
        }

        @Override
        public int getItemCount() {
            return expenses.size();
        }

        class ExpenseViewHolder extends RecyclerView.ViewHolder {
            LinearLayout headerLayout;
            TextView dateText;
            TextView totalTextSummary;
            TextView expandIcon;
            LinearLayout detailsContainer;
            TextView laborText;
            TextView materialText;
            TextView equipmentText;
            TextView miscellaneousText;
            TextView workersText;

            ExpenseViewHolder(@NonNull View itemView) {
                super(itemView);
                headerLayout = itemView.findViewById(R.id.headerLayout);
                dateText = itemView.findViewById(R.id.dateText);
                totalTextSummary = itemView.findViewById(R.id.totalTextSummary);
                expandIcon = itemView.findViewById(R.id.expandIcon);
                detailsContainer = itemView.findViewById(R.id.detailsContainer);
                laborText = itemView.findViewById(R.id.laborText);
                materialText = itemView.findViewById(R.id.materialText);
                equipmentText = itemView.findViewById(R.id.equipmentText);
                miscellaneousText = itemView.findViewById(R.id.miscellaneousText);
                workersText = itemView.findViewById(R.id.workersText);
            }
        }
    }
}

