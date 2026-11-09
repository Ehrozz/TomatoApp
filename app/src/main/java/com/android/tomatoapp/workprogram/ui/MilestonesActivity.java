package com.android.tomatoapp.workprogram.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.tomatoapp.R;
import com.android.tomatoapp.common.utils.AppClock;
import com.google.firebase.database.DataSnapshot;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MilestonesActivity extends AppCompatActivity {
    private String programId;
    private String seeds, sowing, transplant, stop;
    private DatabaseReference reference;
    private MaterialButton seedsButton, sowingButton, transplantButton, stopButton;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_milestones);
        programId = getIntent().getStringExtra("programId");
        seeds = null;
        sowing = null;
        transplant = null;
        stop = null;
        String uid = FirebaseAuth.getInstance().getCurrentUser() == null ? null
                : FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid == null || programId == null) {
            finish();
            return;
        }
        reference = FirebaseDatabase.getInstance().getReference("users").child(uid)
                .child("workPrograms").child(programId);
        seedsButton = findViewById(R.id.btnSeedsDate);
        sowingButton = findViewById(R.id.btnSowingDate);
        transplantButton = findViewById(R.id.btnTransplantDate);
        stopButton = findViewById(R.id.btnStopDate);
        reference.get().addOnSuccessListener(snapshot -> {
            seeds = read(snapshot, "seedsConfirmedDate");
            sowing = read(snapshot, "sowingDate");
            transplant = read(snapshot, "transplantDate");
            stop = read(snapshot, "stopDate");
            refreshLabels();
        }).addOnFailureListener(error -> Toast.makeText(this,
                "Could not load milestones: " + error.getMessage(), Toast.LENGTH_LONG).show());
        seedsButton.setOnClickListener(v -> pick("seedsConfirmedDate", seeds));
        sowingButton.setOnClickListener(v -> pick("sowingDate", sowing));
        transplantButton.setOnClickListener(v -> pick("transplantDate", transplant));
        stopButton.setOnClickListener(v -> pick("stopDate", stop));
    }

    private void refreshLabels() {
        seedsButton.setText("Seeds: " + label(seeds));
        sowingButton.setText("Sowing: " + label(sowing));
        transplantButton.setText("Transplanting: " + label(transplant));
        stopButton.setText("Stop: " + label(stop));
    }

    private String label(String value) {
        return value == null ? "not recorded" : value;
    }

    private void pick(String field, String existing) {
        Calendar calendar = Calendar.getInstance();
        if (existing != null) {
            try {
                LocalDate date = LocalDate.parse(existing);
                calendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
            } catch (RuntimeException ignored) { }
        }
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, day) -> {
            String value = String.format(java.util.Locale.US, "%04d-%02d-%02d",
                    year, month + 1, day);
            if (!valid(field, value)) return;
            Map<String, Object> update = new HashMap<>();
            update.put(field, value);
            reference.updateChildren(update).addOnSuccessListener(unused -> {
                if ("seedsConfirmedDate".equals(field)) seeds = value;
                if ("sowingDate".equals(field)) sowing = value;
                if ("transplantDate".equals(field)) transplant = value;
                if ("stopDate".equals(field)) stop = value;
                refreshLabels();
                Intent result = new Intent();
                result.putExtra("milestoneChanged", true);
                setResult(RESULT_OK, result);
            }).addOnFailureListener(error -> Toast.makeText(this,
                    "Could not save correction: " + error.getMessage(), Toast.LENGTH_LONG).show());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        try {
            Calendar appToday = Calendar.getInstance();
            LocalDate today = AppClock.today(this);
            appToday.set(today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
            picker.getDatePicker().setMaxDate(appToday.getTimeInMillis());
        } catch (RuntimeException error) {
            Toast.makeText(this, "Could not read the app calendar date.", Toast.LENGTH_SHORT).show();
        }
        picker.show();
    }

    private boolean valid(String field, String value) {
        LocalDate selected = LocalDate.parse(value);
        if ("sowingDate".equals(field) && seeds == null) return reject("Confirm seeds first.");
        if ("sowingDate".equals(field) && selected.isBefore(LocalDate.parse(seeds))) {
            return reject("Sowing cannot be before seed confirmation.");
        }
        if ("transplantDate".equals(field) && sowing == null) return reject("Record sowing first.");
        if ("transplantDate".equals(field) && seeds != null
                && selected.isBefore(LocalDate.parse(seeds))) {
            return reject("Transplanting cannot be before seed confirmation.");
        }
        if ("stopDate".equals(field) && transplant == null) return reject("Record transplanting first.");
        return true;
    }

    private String read(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value == null ? null : String.valueOf(value);
    }

    private boolean reject(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return false;
    }
}
