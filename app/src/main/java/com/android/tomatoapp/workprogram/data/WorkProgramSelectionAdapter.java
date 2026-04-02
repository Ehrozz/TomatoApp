package com.android.tomatoapp.workprogram.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tomatoapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for selecting work programs in export dialog.
 */
public class WorkProgramSelectionAdapter extends RecyclerView.Adapter<WorkProgramSelectionAdapter.ViewHolder> {

    private final List<WorkProgramEntity> workPrograms;
    private final List<Boolean> selectedStates;

    public WorkProgramSelectionAdapter(List<WorkProgramEntity> workPrograms) {
        this.workPrograms = workPrograms;
        this.selectedStates = new ArrayList<>();
        for (int i = 0; i < workPrograms.size(); i++) {
            selectedStates.add(true); // All selected by default
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_work_program_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkProgramEntity program = workPrograms.get(position);
        
        holder.checkbox.setChecked(selectedStates.get(position));
        holder.cultivarName.setText(program.cultivarName != null ? program.cultivarName : "Unknown");
        holder.startDate.setText("Start: " + (program.startingDate != null ? program.startingDate : "N/A"));
        
        double profit = program.projectedIncome - program.projectedExpenses;
        holder.profit.setText(String.format("Profit: ₱%,.2f", profit));

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedStates.set(position, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return workPrograms.size();
    }

    public List<WorkProgramEntity> getSelectedPrograms() {
        List<WorkProgramEntity> selected = new ArrayList<>();
        for (int i = 0; i < workPrograms.size(); i++) {
            if (selectedStates.get(i)) {
                selected.add(workPrograms.get(i));
            }
        }
        return selected;
    }

    public void selectAll() {
        for (int i = 0; i < selectedStates.size(); i++) {
            selectedStates.set(i, true);
        }
        notifyDataSetChanged();
    }

    public void deselectAll() {
        for (int i = 0; i < selectedStates.size(); i++) {
            selectedStates.set(i, false);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        TextView cultivarName;
        TextView startDate;
        TextView profit;

        ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            cultivarName = itemView.findViewById(R.id.programCultivar);
            startDate = itemView.findViewById(R.id.programDate);
            profit = itemView.findViewById(R.id.programProfit);
        }
    }
}





