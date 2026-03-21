package com.android.tomatoapp.financial.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Data Model Classes
class LaborItem {
    int numWorkers;
    double dailyWage;
    double totalCost;
    String notes;
    
    LaborItem() {
        numWorkers = 0;
        dailyWage = 0.0;
        totalCost = 0.0;
        notes = "";
    }
}

class MaterialItem {
    String materialName;
    double quantity;
    String quantityUnit; // kg, grams, liters, pieces
    double unitCost;
    double totalCost;
    
    MaterialItem() {
        materialName = "";
        quantity = 0.0;
        quantityUnit = "";
        unitCost = 0.0;
        totalCost = 0.0;
    }
}

class EquipmentItem {
    String equipmentName;
    double usageValue; // Usage amount (number)
    String usageUnit; // Usage unit (minutes, hours)
    boolean isOwned; // Whether user owns the equipment
    double cost; // Rental cost per hour
    double totalCost; // Calculated: (usageValue converted to hours) × cost
    boolean isCostLocked; // Whether cost field should remain uneditable (if saved from Firebase)
    
    EquipmentItem() {
        equipmentName = "";
        usageValue = 0.0;
        usageUnit = "hours";
        isOwned = false;
        cost = 0.0;
        totalCost = 0.0;
        isCostLocked = false;
    }
}

class MiscellaneousItem {
    String expenseName;
    double cost;
    
    MiscellaneousItem() {
        expenseName = "";
        cost = 0.0;
    }
}

public class DailyExpensesActivity extends BaseDrawerActivity {

    // Header views
    private TextView dateHeader;
    private TextView cultivarHeader;
    private TextView tvTotalExpenses;
    private TextView tvLaborTotal;
    private TextView tvMaterialTotal;
    private TextView tvEquipmentTotal;
    private TextView tvMiscTotal;

    // Buttons
    private MaterialButton btnEdit;
    private MaterialButton btnSave;
    private MaterialButton btnAddLaborItem;
    private MaterialButton btnAddMaterialItem;
    private MaterialButton btnAddEquipmentItem;
    private MaterialButton btnAddMiscItem;

    // Containers
    private LinearLayout laborItemsContainer;
    private LinearLayout materialItemsContainer;
    private LinearLayout equipmentItemsContainer;
    private LinearLayout miscItemsContainer;

    // Data lists
    private List<LaborItem> laborItems;
    private List<MaterialItem> materialItems;
    private List<EquipmentItem> equipmentItems;
    private List<MiscellaneousItem> miscItems;

    // View holders for dynamic items
    private List<View> laborItemViews;
    private List<View> materialItemViews;
    private List<View> equipmentItemViews;
    private List<View> miscItemViews;

    // State
    private boolean isEditMode = false;

    // Firebase
    private String programId;
    private String cultivar;
    private String date;
    private String startDate;
    private DatabaseReference expensesRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_expenses);

        setupDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daily Expenses");
        }

        // Get intent data
        programId = getIntent().getStringExtra("programId");
        cultivar = getIntent().getStringExtra("cultivar");
        date = getIntent().getStringExtra("date");
        startDate = getIntent().getStringExtra("programStartDate");

        // Check if user is logged in
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        // Initialize views
        dateHeader = findViewById(R.id.dateHeader);
        cultivarHeader = findViewById(R.id.cultivarHeader);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvLaborTotal = findViewById(R.id.tvLaborTotal);
        tvMaterialTotal = findViewById(R.id.tvMaterialTotal);
        tvEquipmentTotal = findViewById(R.id.tvEquipmentTotal);
        tvMiscTotal = findViewById(R.id.tvMiscTotal);

        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnAddLaborItem = findViewById(R.id.btnAddLaborItem);
        btnAddMaterialItem = findViewById(R.id.btnAddMaterialItem);
        btnAddEquipmentItem = findViewById(R.id.btnAddEquipmentItem);
        btnAddMiscItem = findViewById(R.id.btnAddMiscItem);

        laborItemsContainer = findViewById(R.id.laborItemsContainer);
        materialItemsContainer = findViewById(R.id.materialItemsContainer);
        equipmentItemsContainer = findViewById(R.id.equipmentItemsContainer);
        miscItemsContainer = findViewById(R.id.miscItemsContainer);

        // Initialize data lists
        laborItems = new ArrayList<>();
        materialItems = new ArrayList<>();
        equipmentItems = new ArrayList<>();
        miscItems = new ArrayList<>();

        laborItemViews = new ArrayList<>();
        materialItemViews = new ArrayList<>();
        equipmentItemViews = new ArrayList<>();
        miscItemViews = new ArrayList<>();

        // Set headers
        if (date != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dateObj = sdf.parse(date);
                SimpleDateFormat displayFormat = SettingsPreferences.getDateFormatInstance(this);
                String formattedDate = displayFormat.format(dateObj);
                dateHeader.setText("Date: " + formattedDate);
            } catch (Exception e) {
                dateHeader.setText("Date: " + date);
            }
        }
        if (cultivar != null) {
            cultivarHeader.setText("Cultivar: " + cultivar);
        }

        // Initialize Firebase reference
        if (programId != null) {
            expensesRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("workPrograms")
                    .child(programId)
                    .child("dailyExpenses")
                    .child(date);
        }

        // Initialize Firebase reference
        if (programId != null) {
            expensesRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("workPrograms")
                    .child(programId)
                    .child("dailyExpenses")
                    .child(date);
        }

        // Set up button listeners
        btnEdit.setOnClickListener(v -> enterEditMode());
        btnSave.setOnClickListener(v -> saveExpenses());
        btnAddLaborItem.setOnClickListener(v -> addLaborItem());
        btnAddMaterialItem.setOnClickListener(v -> addMaterialItem());
        btnAddEquipmentItem.setOnClickListener(v -> addEquipmentItem());
        btnAddMiscItem.setOnClickListener(v -> addMiscItem());

        // Load existing expenses if available
        // Initial state: start in edit mode (will be disabled if data exists)
        setEditMode(true);
        loadExistingExpenses();
    }

    // Edit/Save Mode Methods
    private void enterEditMode() {
        setEditMode(true);
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        btnEdit.setEnabled(!editMode);
        btnSave.setEnabled(editMode);
        btnAddLaborItem.setEnabled(editMode);
        btnAddMaterialItem.setEnabled(editMode);
        btnAddEquipmentItem.setEnabled(editMode);
        btnAddMiscItem.setEnabled(editMode);

        // Enable/disable all input fields
        enableInputs(editMode);
    }

    private void enableInputs(boolean enabled) {
        // Enable/disable all input fields in containers
        for (View view : laborItemViews) {
            setViewGroupEnabled((ViewGroup) view, enabled);
        }
        for (View view : materialItemViews) {
            setViewGroupEnabled((ViewGroup) view, enabled);
        }
        for (View view : equipmentItemViews) {
            setViewGroupEnabled((ViewGroup) view, enabled);
        }
        for (View view : miscItemViews) {
            setViewGroupEnabled((ViewGroup) view, enabled);
        }
    }

    private void setViewGroupEnabled(ViewGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof EditText) {
                // Check if this is a locked cost field - if so, keep it disabled
                Object tag = child.getTag();
                if (tag != null && tag.toString().startsWith("cost_")) {
                    String tagStr = tag.toString();
                    try {
                        int itemIndex = Integer.parseInt(tagStr.substring("cost_".length()));
                        // Check if this equipment item has locked cost
                        if (itemIndex >= 0 && itemIndex < equipmentItems.size()) {
                            EquipmentItem item = equipmentItems.get(itemIndex);
                            if (item.isCostLocked) {
                                // Keep cost field disabled even if we're enabling other fields
                                child.setEnabled(false);
                                child.setFocusable(false);
                                child.setClickable(false);
                            } else {
                                child.setEnabled(enabled);
                            }
                        } else {
                            child.setEnabled(enabled);
                        }
                    } catch (NumberFormatException e) {
                        child.setEnabled(enabled);
                    }
                } else {
                    child.setEnabled(enabled);
                }
            } else if (child instanceof Spinner) {
                child.setEnabled(enabled);
            } else if (child instanceof ViewGroup) {
                setViewGroupEnabled((ViewGroup) child, enabled);
            }
        }
    }

    // Add Item Methods
    private void addLaborItem() {
        LaborItem item = new LaborItem();
        laborItems.add(item);
        View itemView = createLaborItemView(item, laborItems.size() - 1);
        laborItemViews.add(itemView);
        laborItemsContainer.addView(itemView);
        calculateLaborTotal();
    }

    private void addMaterialItem() {
        MaterialItem item = new MaterialItem();
        materialItems.add(item);
        View itemView = createMaterialItemView(item, materialItems.size() - 1);
        materialItemViews.add(itemView);
        materialItemsContainer.addView(itemView);
        calculateMaterialTotal();
    }

    private void addEquipmentItem() {
        EquipmentItem item = new EquipmentItem();
        equipmentItems.add(item);
        View itemView = createEquipmentItemView(item, equipmentItems.size() - 1);
        equipmentItemViews.add(itemView);
        equipmentItemsContainer.addView(itemView);
        calculateEquipmentTotal();
    }

    private void addMiscItem() {
        MiscellaneousItem item = new MiscellaneousItem();
        miscItems.add(item);
        View itemView = createMiscItemView(item, miscItems.size() - 1);
        miscItemViews.add(itemView);
        miscItemsContainer.addView(itemView);
        calculateMiscTotal();
    }

    // Helper method to convert dp to px
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    // Helper method to convert usage to hours
    private double convertToHours(double value, String unit) {
        if (unit == null) return value;
        
        switch (unit.toLowerCase()) {
            case "minutes":
                return value / 60.0;
            case "hours":
                return value;
            default:
                return value; // Default to hours if unknown
        }
    }
    
    // View Creation Methods
    private View createLaborItemView(LaborItem item, int index) {
        // Main container card
        com.google.android.material.card.MaterialCardView cardView = new com.google.android.material.card.MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
        cardView.setCardElevation(4);
        cardView.setRadius(dpToPx(12));
        cardView.setTag("laborCard_" + index);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        
        // Header with title, expand/collapse, and delete
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setPadding(0, 0, 0, dpToPx(8));
        
        TextView title = new TextView(this);
        title.setText("Labor Item #" + (index + 1));
        title.setTextSize(16);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(titleParams);
        
        ImageView expandIcon = new ImageView(this);
        expandIcon.setImageResource(android.R.drawable.arrow_down_float);
        expandIcon.setTag("expandIcon_" + index);
        expandIcon.setPadding(dpToPx(8), 0, dpToPx(8), 0);
        expandIcon.setLayoutParams(new LinearLayout.LayoutParams(
            dpToPx(32), dpToPx(32)));
        
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setImageResource(android.R.drawable.ic_menu_delete);
        deleteIcon.setTag("deleteIcon_" + index);
        deleteIcon.setPadding(dpToPx(8), 0, 0, 0);
        deleteIcon.setLayoutParams(new LinearLayout.LayoutParams(
            dpToPx(32), dpToPx(32)));
        
        headerLayout.addView(title);
        headerLayout.addView(expandIcon);
        headerLayout.addView(deleteIcon);
        
        // Collapsible content container
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setTag("contentLayout_" + index);
        contentLayout.setVisibility(View.VISIBLE);
        
        // Number of Workers
        TextInputLayout numWorkersLayout = new TextInputLayout(this);
        numWorkersLayout.setHint("Number of Workers");
        numWorkersLayout.setPadding(0, 0, 0, dpToPx(8));
        EditText etNumWorkers = new EditText(this);
        etNumWorkers.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etNumWorkers.setTag("numWorkers_" + index);
        if (item.numWorkers > 0) {
            etNumWorkers.setText(String.valueOf(item.numWorkers));
        }
        numWorkersLayout.addView(etNumWorkers);
        
        // Daily Wage
        TextInputLayout dailyWageLayout = new TextInputLayout(this);
        dailyWageLayout.setHint("Daily Wage (₱)");
        dailyWageLayout.setPadding(0, 0, 0, dpToPx(8));
        EditText etDailyWage = new EditText(this);
        etDailyWage.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etDailyWage.setTag("dailyWage_" + index);
        if (item.dailyWage > 0) {
            etDailyWage.setText(String.valueOf(item.dailyWage));
        }
        dailyWageLayout.addView(etDailyWage);
        
        // Notes section - Spinner picker with "Activity" label
        TextView activityLabel = new TextView(this);
        activityLabel.setText("Activity:");
        activityLabel.setTextSize(14);
        activityLabel.setTextColor(getResources().getColor(R.color.text_secondary));
        activityLabel.setPadding(0, dpToPx(4), 0, dpToPx(4));
        
        Spinner notesSpinner = new Spinner(this);
        notesSpinner.setTag("notes_" + index);
        notesSpinner.setPadding(0, 0, 0, dpToPx(8));
        String[] notesOptions = {"Plowing", "Harrowing", "Pruning", "Others"};
        ArrayAdapter<String> notesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, notesOptions);
        notesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notesSpinner.setAdapter(notesAdapter);
        
        // Custom activity input field (initially hidden)
        TextInputLayout customActivityLayout = new TextInputLayout(this);
        customActivityLayout.setHint("Specify Activity");
        customActivityLayout.setPadding(0, 0, 0, dpToPx(8));
        customActivityLayout.setTag("customActivityLayout_" + index);
        customActivityLayout.setVisibility(View.GONE);
        EditText etCustomActivity = new EditText(this);
        etCustomActivity.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        etCustomActivity.setTag("customActivity_" + index);
        customActivityLayout.addView(etCustomActivity);
        
        // Check if notes is a custom value (not in the options)
        boolean isCustomActivity = false;
        if (item.notes != null && !item.notes.isEmpty()) {
            boolean found = false;
            for (int i = 0; i < notesOptions.length - 1; i++) { // Exclude "Others"
                if (notesOptions[i].equals(item.notes)) {
                    notesSpinner.setSelection(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // It's a custom value
                notesSpinner.setSelection(notesOptions.length - 1); // Select "Others"
                etCustomActivity.setText(item.notes);
                customActivityLayout.setVisibility(View.VISIBLE);
                isCustomActivity = true;
            }
        }
        
        notesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (index < laborItems.size()) {
                    if (position == notesOptions.length - 1) {
                        // "Others" selected - show custom input
                        customActivityLayout.setVisibility(View.VISIBLE);
                        String customText = etCustomActivity.getText().toString().trim();
                        laborItems.get(index).notes = customText.isEmpty() ? "Others" : customText;
                    } else {
                        // Regular option selected - hide custom input
                        customActivityLayout.setVisibility(View.GONE);
                        laborItems.get(index).notes = notesOptions[position];
                    }
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Text watcher for custom activity input
        etCustomActivity.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (index < laborItems.size() && notesSpinner.getSelectedItemPosition() == notesOptions.length - 1) {
                    String customText = s.toString().trim();
                    laborItems.get(index).notes = customText.isEmpty() ? "Others" : customText;
                }
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        contentLayout.addView(numWorkersLayout);
        contentLayout.addView(dailyWageLayout);
        contentLayout.addView(activityLabel);
        contentLayout.addView(notesSpinner);
        contentLayout.addView(customActivityLayout);
        
        mainLayout.addView(headerLayout);
        mainLayout.addView(contentLayout);
        cardView.addView(mainLayout);
        
        // Set up listeners
        expandIcon.setOnClickListener(v -> {
            boolean isVisible = contentLayout.getVisibility() == View.VISIBLE;
            contentLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            expandIcon.setImageResource(isVisible ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        });
        
        deleteIcon.setOnClickListener(v -> {
            laborItems.remove(index);
            laborItemsContainer.removeView(cardView);
            laborItemViews.remove(cardView);
            recalculateLaborItemIndices();
            calculateLaborTotal();
        });
        
        // Text watchers for auto-calculation
        android.text.TextWatcher laborWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLaborItemFromView(index);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        etNumWorkers.addTextChangedListener(laborWatcher);
        etDailyWage.addTextChangedListener(laborWatcher);
        
        // Initial calculation
        updateLaborItemFromView(index);
        
        return cardView;
    }

    private View createMaterialItemView(MaterialItem item, int index) {
        // Main container card
        com.google.android.material.card.MaterialCardView cardView = new com.google.android.material.card.MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
        cardView.setCardElevation(4);
        cardView.setRadius(dpToPx(12));
        cardView.setTag("materialCard_" + index);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        
        // Header with title, expand/collapse, and delete
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setPadding(0, 0, 0, dpToPx(8));
        
        TextView title = new TextView(this);
        title.setText("Material Item #" + (index + 1));
        title.setTextSize(16);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(titleParams);
        
        ImageView expandIcon = new ImageView(this);
        expandIcon.setImageResource(android.R.drawable.arrow_down_float);
        expandIcon.setTag("expandIcon_" + index);
        expandIcon.setPadding(dpToPx(8), 0, dpToPx(8), 0);
        expandIcon.setLayoutParams(new LinearLayout.LayoutParams(
            dpToPx(32), dpToPx(32)));
        
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setImageResource(android.R.drawable.ic_menu_delete);
        deleteIcon.setTag("deleteIcon_" + index);
        deleteIcon.setPadding(dpToPx(8), 0, 0, 0);
        deleteIcon.setLayoutParams(new LinearLayout.LayoutParams(
            dpToPx(32), dpToPx(32)));
        
        headerLayout.addView(title);
        headerLayout.addView(expandIcon);
        headerLayout.addView(deleteIcon);
        
        // Collapsible content container
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setTag("contentLayout_" + index);
        contentLayout.setVisibility(View.VISIBLE);
        
        // Material Name - Spinner picker
        TextInputLayout materialNameLayout = new TextInputLayout(this);
        materialNameLayout.setHint("Material Name");
        materialNameLayout.setPadding(0, 0, 0, dpToPx(8));
        Spinner materialNameSpinner = new Spinner(this);
        materialNameSpinner.setTag("materialName_" + index);
        String[] materialOptions = {"seeds", "fertilizer", "pesticide", "compost", "Others"};
        ArrayAdapter<String> materialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, materialOptions);
        materialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        materialNameSpinner.setAdapter(materialAdapter);
        
        // Custom material input field (initially hidden)
        TextInputLayout customMaterialLayout = new TextInputLayout(this);
        customMaterialLayout.setHint("Specify Material");
        customMaterialLayout.setPadding(0, 0, 0, dpToPx(8));
        customMaterialLayout.setTag("customMaterialLayout_" + index);
        customMaterialLayout.setVisibility(View.GONE);
        EditText etCustomMaterial = new EditText(this);
        etCustomMaterial.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        etCustomMaterial.setTag("customMaterial_" + index);
        customMaterialLayout.addView(etCustomMaterial);
        
        // Check if materialName is a custom value (not in the options)
        if (item.materialName != null && !item.materialName.isEmpty()) {
            boolean found = false;
            for (int i = 0; i < materialOptions.length - 1; i++) { // Exclude "Others"
                if (materialOptions[i].equals(item.materialName)) {
                    materialNameSpinner.setSelection(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // It's a custom value
                materialNameSpinner.setSelection(materialOptions.length - 1); // Select "Others"
                etCustomMaterial.setText(item.materialName);
                customMaterialLayout.setVisibility(View.VISIBLE);
            }
        }
        
        materialNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (index < materialItems.size()) {
                    if (position == materialOptions.length - 1) {
                        // "Others" selected - show custom input
                        customMaterialLayout.setVisibility(View.VISIBLE);
                        String customText = etCustomMaterial.getText().toString().trim();
                        materialItems.get(index).materialName = customText.isEmpty() ? "Others" : customText;
                    } else {
                        // Regular option selected - hide custom input
                        customMaterialLayout.setVisibility(View.GONE);
                        materialItems.get(index).materialName = materialOptions[position];
                    }
                    updateMaterialItemFromView(index);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Text watcher for custom material input
        etCustomMaterial.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (index < materialItems.size() && materialNameSpinner.getSelectedItemPosition() == materialOptions.length - 1) {
                    String customText = s.toString().trim();
                    materialItems.get(index).materialName = customText.isEmpty() ? "Others" : customText;
                    updateMaterialItemFromView(index);
                }
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        materialNameLayout.addView(materialNameSpinner);
        
        // Quantity Used - Number input with Unit Spinner
        LinearLayout quantityRow = new LinearLayout(this);
        quantityRow.setOrientation(LinearLayout.HORIZONTAL);
        quantityRow.setPadding(0, 0, 0, dpToPx(8));
        
        TextInputLayout quantityValueLayout = new TextInputLayout(this);
        quantityValueLayout.setHint("Quantity");
        quantityValueLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        EditText etQuantity = new EditText(this);
        etQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etQuantity.setTag("quantity_" + index);
        if (item.quantity > 0) {
            etQuantity.setText(String.valueOf(item.quantity));
        }
        quantityValueLayout.addView(etQuantity);
        
        TextInputLayout quantityUnitLayout = new TextInputLayout(this);
        quantityUnitLayout.setHint("Unit");
        quantityUnitLayout.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(120), LinearLayout.LayoutParams.WRAP_CONTENT));
        quantityUnitLayout.setPadding(dpToPx(8), 0, 0, 0);
        Spinner quantityUnitSpinner = new Spinner(this);
        quantityUnitSpinner.setTag("quantityUnit_" + index);
        String[] unitOptions = {"kg", "grams", "liters", "pieces"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitOptions);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantityUnitSpinner.setAdapter(unitAdapter);
        if (item.quantityUnit != null && !item.quantityUnit.isEmpty()) {
            for (int i = 0; i < unitOptions.length; i++) {
                if (unitOptions[i].equals(item.quantityUnit)) {
                    quantityUnitSpinner.setSelection(i);
                    break;
                }
            }
        }
        quantityUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (index < materialItems.size()) {
                    materialItems.get(index).quantityUnit = unitOptions[position];
                    updateMaterialItemFromView(index);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        quantityUnitLayout.addView(quantityUnitSpinner);
        
        quantityRow.addView(quantityValueLayout);
        quantityRow.addView(quantityUnitLayout);
        
        // Unit Cost
        TextInputLayout unitCostLayout = new TextInputLayout(this);
        unitCostLayout.setHint("Unit Cost (₱)");
        unitCostLayout.setPadding(0, 0, 0, dpToPx(8));
        EditText etUnitCost = new EditText(this);
        etUnitCost.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etUnitCost.setTag("unitCost_" + index);
        if (item.unitCost > 0) {
            etUnitCost.setText(String.valueOf(item.unitCost));
        }
        unitCostLayout.addView(etUnitCost);
        
        contentLayout.addView(materialNameLayout);
        contentLayout.addView(customMaterialLayout);
        contentLayout.addView(quantityRow);
        contentLayout.addView(unitCostLayout);
        
        mainLayout.addView(headerLayout);
        mainLayout.addView(contentLayout);
        cardView.addView(mainLayout);
        
        // Set up listeners
        expandIcon.setOnClickListener(v -> {
            boolean isVisible = contentLayout.getVisibility() == View.VISIBLE;
            contentLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            expandIcon.setImageResource(isVisible ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        });
        
        deleteIcon.setOnClickListener(v -> {
            materialItems.remove(index);
            materialItemsContainer.removeView(cardView);
            materialItemViews.remove(cardView);
            recalculateMaterialItemIndices();
            calculateMaterialTotal();
        });
        
        // Text watchers for auto-calculation (quantity and unit cost only)
        android.text.TextWatcher materialWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateMaterialItemFromView(index);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
        
        etQuantity.addTextChangedListener(materialWatcher);
        etUnitCost.addTextChangedListener(materialWatcher);
        
        // Initial calculation
        updateMaterialItemFromView(index);
        
        return cardView;
    }

    private View createEquipmentItemView(EquipmentItem item, int index) {
        // Main container card
        com.google.android.material.card.MaterialCardView cardView = new com.google.android.material.card.MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
        cardView.setCardElevation(4);
        cardView.setRadius(dpToPx(12));
        cardView.setTag("equipmentCard_" + index);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        
        // Header with title, expand/collapse, and delete
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setPadding(0, 0, 0, dpToPx(8));
        
        TextView title = new TextView(this);
        title.setText("Equipment/Tools Item #" + (index + 1));
        title.setTextSize(16);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(titleParams);
        
        ImageView expandIcon = new ImageView(this);
        expandIcon.setImageResource(android.R.drawable.arrow_down_float);
        expandIcon.setTag("expandIcon_" + index);
        expandIcon.setPadding(dpToPx(8), 0, dpToPx(8), 0);
        expandIcon.setLayoutParams(new LinearLayout.LayoutParams(
            dpToPx(32), dpToPx(32)));
        
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setImageResource(android.R.drawable.ic_menu_delete);
        deleteIcon.setTag("deleteIcon_" + index);
        deleteIcon.setPadding(dpToPx(8), 0, 0, 0);
        deleteIcon.setLayoutParams(new LinearLayout.LayoutParams(
            dpToPx(32), dpToPx(32)));
        
        headerLayout.addView(title);
        headerLayout.addView(expandIcon);
        headerLayout.addView(deleteIcon);
        
        // Collapsible content container
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setTag("contentLayout_" + index);
        contentLayout.setVisibility(View.VISIBLE);
        
        // Equipment Name
        TextInputLayout equipmentNameLayout = new TextInputLayout(this);
        equipmentNameLayout.setHint("Equipment Name (e.g., sprayer, pump, hoe)");
        equipmentNameLayout.setPadding(0, 0, 0, dpToPx(8));
        EditText etEquipmentName = new EditText(this);
        etEquipmentName.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        etEquipmentName.setTag("equipmentName_" + index);
        if (item.equipmentName != null && !item.equipmentName.isEmpty()) {
            etEquipmentName.setText(item.equipmentName);
        }
        equipmentNameLayout.addView(etEquipmentName);
        
        // Usage - Value input with Unit Spinner
        LinearLayout usageRow = new LinearLayout(this);
        usageRow.setOrientation(LinearLayout.HORIZONTAL);
        usageRow.setPadding(0, 0, 0, dpToPx(8));
        
        TextInputLayout usageValueLayout = new TextInputLayout(this);
        usageValueLayout.setHint("Usage");
        usageValueLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        EditText etUsageValue = new EditText(this);
        etUsageValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etUsageValue.setTag("usageValue_" + index);
        if (item.usageValue > 0) {
            etUsageValue.setText(String.valueOf(item.usageValue));
        }
        usageValueLayout.addView(etUsageValue);
        
        TextInputLayout usageUnitLayout = new TextInputLayout(this);
        usageUnitLayout.setHint("Unit");
        usageUnitLayout.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(140), LinearLayout.LayoutParams.WRAP_CONTENT));
        usageUnitLayout.setPadding(dpToPx(8), 0, 0, 0);
        Spinner usageUnitSpinner = new Spinner(this);
        usageUnitSpinner.setTag("usageUnit_" + index);
        String[] unitOptions = {"minutes", "hours"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitOptions);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usageUnitSpinner.setAdapter(unitAdapter);
        if (item.usageUnit != null && !item.usageUnit.isEmpty()) {
            for (int i = 0; i < unitOptions.length; i++) {
                if (unitOptions[i].equals(item.usageUnit)) {
                    usageUnitSpinner.setSelection(i);
                    break;
                }
            }
        }
        usageUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (index < equipmentItems.size()) {
                    equipmentItems.get(index).usageUnit = unitOptions[position];
                    updateEquipmentItemFromView(index);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        usageUnitLayout.addView(usageUnitSpinner);
        
        usageRow.addView(usageValueLayout);
        usageRow.addView(usageUnitLayout);
        
        // Ownership Question - CheckBox
        LinearLayout ownershipLayout = new LinearLayout(this);
        ownershipLayout.setOrientation(LinearLayout.HORIZONTAL);
        ownershipLayout.setPadding(0, dpToPx(4), 0, dpToPx(8));
        
        android.widget.CheckBox cbOwned = new android.widget.CheckBox(this);
        cbOwned.setText("I own this equipment/tool");
        cbOwned.setTextSize(14);
        cbOwned.setTag("owned_" + index);
        cbOwned.setChecked(item.isOwned);
        ownershipLayout.addView(cbOwned);
        
        // Cost - Rental cost per hour (shown only if NOT owned)
        TextInputLayout costLayout = new TextInputLayout(this);
        costLayout.setHint("Rental cost per hour (₱)");
        costLayout.setPadding(0, 0, 0, dpToPx(8));
        costLayout.setTag("costLayout_" + index);
        EditText etCost = new EditText(this);
        etCost.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etCost.setTag("cost_" + index);
        if (item.cost > 0 && !item.isOwned) {
            etCost.setText(String.valueOf(item.cost));
        }
        // Make cost field uneditable if it was saved (locked)
        if (item.isCostLocked) {
            etCost.setEnabled(false);
            etCost.setFocusable(false);
            etCost.setClickable(false);
            // Change appearance to indicate it's locked
            etCost.setAlpha(0.6f);
        }
        costLayout.addView(etCost);
        costLayout.setVisibility(item.isOwned ? View.GONE : View.VISIBLE);
        
        contentLayout.addView(equipmentNameLayout);
        contentLayout.addView(usageRow);
        contentLayout.addView(ownershipLayout);
        contentLayout.addView(costLayout);
        
        mainLayout.addView(headerLayout);
        mainLayout.addView(contentLayout);
        cardView.addView(mainLayout);
        
        // Set up listeners
        expandIcon.setOnClickListener(v -> {
            boolean isVisible = contentLayout.getVisibility() == View.VISIBLE;
            contentLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            expandIcon.setImageResource(isVisible ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        });
        
        deleteIcon.setOnClickListener(v -> {
            equipmentItems.remove(index);
            equipmentItemsContainer.removeView(cardView);
            equipmentItemViews.remove(cardView);
            recalculateEquipmentItemIndices();
            calculateEquipmentTotal();
        });
        
        // Ownership checkbox listener - show/hide cost field
        cbOwned.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (index < equipmentItems.size()) {
                equipmentItems.get(index).isOwned = isChecked;
                costLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                if (isChecked) {
                    // Clear cost if owned
                    etCost.setText("");
                    equipmentItems.get(index).cost = 0.0;
                    equipmentItems.get(index).totalCost = 0.0;
                }
                // Recalculate total cost
                updateEquipmentItemFromView(index);
            }
        });
        
        // Text watchers for updates
        android.text.TextWatcher equipmentWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateEquipmentItemFromView(index);
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
        
        etEquipmentName.addTextChangedListener(equipmentWatcher);
        etUsageValue.addTextChangedListener(equipmentWatcher);
        etCost.addTextChangedListener(equipmentWatcher);
        
        // Initial update
        updateEquipmentItemFromView(index);
        
        return cardView;
    }

    private View createMiscItemView(MiscellaneousItem item, int index) {
        // Main container card
        com.google.android.material.card.MaterialCardView cardView = new com.google.android.material.card.MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
        cardView.setCardElevation(4);
        cardView.setRadius(dpToPx(12));
        cardView.setTag("miscCard_" + index);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        
        // Header with title, expand/collapse, and delete
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setPadding(0, 0, 0, dpToPx(8));
        
        TextView title = new TextView(this);
        title.setText("Miscellaneous Item #" + (index + 1));
        title.setTextSize(16);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(titleParams);
        
        ImageView expandIcon = new ImageView(this);
        expandIcon.setImageResource(android.R.drawable.arrow_down_float);
        expandIcon.setTag("expandIcon_" + index);
        expandIcon.setPadding(dpToPx(8), 0, dpToPx(8), 0);
        expandIcon.setLayoutParams(new LinearLayout.LayoutParams(
            dpToPx(32), dpToPx(32)));
        
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setImageResource(android.R.drawable.ic_menu_delete);
        deleteIcon.setTag("deleteIcon_" + index);
        deleteIcon.setPadding(dpToPx(8), 0, 0, 0);
        deleteIcon.setLayoutParams(new LinearLayout.LayoutParams(
            dpToPx(32), dpToPx(32)));
        
        headerLayout.addView(title);
        headerLayout.addView(expandIcon);
        headerLayout.addView(deleteIcon);
        
        // Collapsible content container
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setTag("contentLayout_" + index);
        contentLayout.setVisibility(View.VISIBLE);
        
        // Expense Name - Spinner picker
        TextInputLayout expenseNameLayout = new TextInputLayout(this);
        expenseNameLayout.setHint("Expense Name");
        expenseNameLayout.setPadding(0, 0, 0, dpToPx(8));
        Spinner expenseNameSpinner = new Spinner(this);
        expenseNameSpinner.setTag("expenseName_" + index);
        String[] expenseOptions = {
            "Fuel",
            "Electricity",
            "Land Permit",
            "Contingency",
            "Irrigation Water",
            "Transportation",
            "Packaging Materials",
            "Marketing Expenses",
            "Insurance",
            "Loan Interest",
            "Taxes",
            "Storage Fees",
            "Utilities",
            "Repair/Maintenance",
            "Foods for Labor",
            "Miscellaneous Supplies",
            "Others"
        };
        ArrayAdapter<String> expenseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, expenseOptions);
        expenseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseNameSpinner.setAdapter(expenseAdapter);
        
        // Custom expense input field (initially hidden)
        TextInputLayout customExpenseLayout = new TextInputLayout(this);
        customExpenseLayout.setHint("Specify Expense");
        customExpenseLayout.setPadding(0, 0, 0, dpToPx(8));
        customExpenseLayout.setTag("customExpenseLayout_" + index);
        customExpenseLayout.setVisibility(View.GONE);
        EditText etCustomExpense = new EditText(this);
        etCustomExpense.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        etCustomExpense.setTag("customExpense_" + index);
        customExpenseLayout.addView(etCustomExpense);
        
        // Check if expenseName is a custom value (not in the options)
        if (item.expenseName != null && !item.expenseName.isEmpty()) {
            boolean found = false;
            for (int i = 0; i < expenseOptions.length - 1; i++) { // Exclude "Others"
                if (expenseOptions[i].equals(item.expenseName)) {
                    expenseNameSpinner.setSelection(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // It's a custom value
                expenseNameSpinner.setSelection(expenseOptions.length - 1); // Select "Others"
                etCustomExpense.setText(item.expenseName);
                customExpenseLayout.setVisibility(View.VISIBLE);
            }
        }
        
        expenseNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (index < miscItems.size()) {
                    if (position == expenseOptions.length - 1) {
                        // "Others" selected - show custom input
                        customExpenseLayout.setVisibility(View.VISIBLE);
                        String customText = etCustomExpense.getText().toString().trim();
                        miscItems.get(index).expenseName = customText.isEmpty() ? "Others" : customText;
                    } else {
                        // Regular option selected - hide custom input
                        customExpenseLayout.setVisibility(View.GONE);
                        miscItems.get(index).expenseName = expenseOptions[position];
                    }
                    updateMiscItemFromView(index);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Text watcher for custom expense input
        etCustomExpense.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (index < miscItems.size() && expenseNameSpinner.getSelectedItemPosition() == expenseOptions.length - 1) {
                    String customText = s.toString().trim();
                    miscItems.get(index).expenseName = customText.isEmpty() ? "Others" : customText;
                    updateMiscItemFromView(index);
                }
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        expenseNameLayout.addView(expenseNameSpinner);
        
        // Cost
        TextInputLayout costLayout = new TextInputLayout(this);
        costLayout.setHint("Cost (₱)");
        costLayout.setPadding(0, 0, 0, dpToPx(8));
        EditText etCost = new EditText(this);
        etCost.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etCost.setTag("cost_" + index);
        if (item.cost > 0) {
            etCost.setText(String.valueOf(item.cost));
        }
        costLayout.addView(etCost);
        
        contentLayout.addView(expenseNameLayout);
        contentLayout.addView(customExpenseLayout);
        contentLayout.addView(costLayout);
        
        mainLayout.addView(headerLayout);
        mainLayout.addView(contentLayout);
        cardView.addView(mainLayout);
        
        // Set up listeners
        expandIcon.setOnClickListener(v -> {
            boolean isVisible = contentLayout.getVisibility() == View.VISIBLE;
            contentLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            expandIcon.setImageResource(isVisible ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        });
        
        deleteIcon.setOnClickListener(v -> {
            miscItems.remove(index);
            miscItemsContainer.removeView(cardView);
            miscItemViews.remove(cardView);
            recalculateMiscItemIndices();
            calculateMiscTotal();
        });
        
        // Text watcher for cost updates only
        android.text.TextWatcher miscWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateMiscItemFromView(index);
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
        
        etCost.addTextChangedListener(miscWatcher);
        
        // Initial update
        updateMiscItemFromView(index);
        
        return cardView;
    }

    // Calculation Methods
    private void calculateLaborTotal() {
        double total = 0;
        for (LaborItem item : laborItems) {
            total += item.totalCost;
        }
        tvLaborTotal.setText("Labor Sub-total: ₱" + String.format("%,.2f", total));
        calculateGrandTotal();
    }

    private void calculateMaterialTotal() {
        double total = 0;
        for (MaterialItem item : materialItems) {
            total += item.totalCost;
        }
        tvMaterialTotal.setText("Material Sub-total: ₱" + String.format("%,.2f", total));
        calculateGrandTotal();
    }

    private void calculateEquipmentTotal() {
        double total = 0;
        for (EquipmentItem item : equipmentItems) {
            total += item.totalCost;
        }
        tvEquipmentTotal.setText("Equipment Sub-total: ₱" + String.format("%,.2f", total));
        calculateGrandTotal();
    }

    private void calculateMiscTotal() {
        double total = 0;
        for (MiscellaneousItem item : miscItems) {
            total += item.cost;
        }
        tvMiscTotal.setText("Miscellaneous Total: ₱" + String.format("%,.2f", total));
        calculateGrandTotal();
    }

    private void calculateGrandTotal() {
        double total = 0;
        for (LaborItem item : laborItems) {
            total += item.totalCost;
        }
        for (MaterialItem item : materialItems) {
            total += item.totalCost;
        }
        for (EquipmentItem item : equipmentItems) {
            total += item.totalCost;
        }
        for (MiscellaneousItem item : miscItems) {
            total += item.cost;
        }
        tvTotalExpenses.setText("Total: ₱" + String.format("%,.2f", total));
    }

    private void recalculateLaborItemIndices() {
        // Recalculate indices after deletion
        for (int i = 0; i < laborItemViews.size(); i++) {
            View view = laborItemViews.get(i);
            // Update title if needed
        }
    }

    // Save and Load Methods
    private void saveExpenses() {
        if (expensesRef == null) {
            Toast.makeText(this, "Error: Program ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect data from views
        collectDataFromViews();

        Map<String, Object> expenses = new HashMap<>();
            expenses.put("date", date);
            expenses.put("timestamp", System.currentTimeMillis());

        // Save labor items
        List<Map<String, Object>> laborList = new ArrayList<>();
        for (LaborItem item : laborItems) {
            Map<String, Object> laborMap = new HashMap<>();
            laborMap.put("numWorkers", item.numWorkers);
            laborMap.put("dailyWage", item.dailyWage);
            laborMap.put("totalCost", item.totalCost);
            laborMap.put("notes", item.notes);
            laborList.add(laborMap);
        }
        expenses.put("labor", laborList);

        // Save material items
        List<Map<String, Object>> materialList = new ArrayList<>();
        for (MaterialItem item : materialItems) {
            Map<String, Object> materialMap = new HashMap<>();
            materialMap.put("materialName", item.materialName);
            materialMap.put("quantity", item.quantity);
            materialMap.put("quantityUnit", item.quantityUnit);
            materialMap.put("unitCost", item.unitCost);
            materialMap.put("totalCost", item.totalCost);
            materialList.add(materialMap);
        }
        expenses.put("material", materialList);

        // Save equipment items
        List<Map<String, Object>> equipmentList = new ArrayList<>();
        for (EquipmentItem item : equipmentItems) {
            Map<String, Object> equipmentMap = new HashMap<>();
            equipmentMap.put("equipmentName", item.equipmentName);
            equipmentMap.put("usageValue", item.usageValue);
            equipmentMap.put("usageUnit", item.usageUnit);
            equipmentMap.put("isOwned", item.isOwned);
            equipmentMap.put("cost", item.cost);
            equipmentMap.put("totalCost", item.totalCost);
            equipmentList.add(equipmentMap);
        }
        expenses.put("equipment", equipmentList);

        // Save miscellaneous items
        List<Map<String, Object>> miscList = new ArrayList<>();
        for (MiscellaneousItem item : miscItems) {
            Map<String, Object> miscMap = new HashMap<>();
            miscMap.put("expenseName", item.expenseName);
            miscMap.put("cost", item.cost);
            miscList.add(miscMap);
        }
        expenses.put("miscellaneous", miscList);

            expensesRef.setValue(expenses)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Daily expenses saved successfully!", Toast.LENGTH_SHORT).show();
                    // Mark all equipment items as having locked cost once saved
                    for (int i = 0; i < equipmentItems.size() && i < equipmentItemViews.size(); i++) {
                        EquipmentItem item = equipmentItems.get(i);
                        if (!item.isOwned && item.cost > 0) {
                            item.isCostLocked = true;
                            // Update the cost field to be locked in the UI
                            View itemView = equipmentItemViews.get(i);
                            EditText etCost = itemView.findViewWithTag("cost_" + i);
                            if (etCost != null) {
                                etCost.setEnabled(false);
                                etCost.setFocusable(false);
                                etCost.setClickable(false);
                                etCost.setAlpha(0.6f);
                            }
                        }
                    }
                    setEditMode(false);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
    }

    private void loadExistingExpenses() {
        if (expensesRef == null) return;

        expensesRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Load labor items
                    if (snapshot.hasChild("labor")) {
                        for (com.google.firebase.database.DataSnapshot laborSnapshot : snapshot.child("labor").getChildren()) {
                            LaborItem item = new LaborItem();
                            item.numWorkers = laborSnapshot.child("numWorkers").getValue(Integer.class) != null ?
                                    laborSnapshot.child("numWorkers").getValue(Integer.class) : 0;
                            item.dailyWage = laborSnapshot.child("dailyWage").getValue(Double.class) != null ?
                                    laborSnapshot.child("dailyWage").getValue(Double.class) : 0.0;
                            item.totalCost = laborSnapshot.child("totalCost").getValue(Double.class) != null ?
                                    laborSnapshot.child("totalCost").getValue(Double.class) : 0.0;
                            item.notes = laborSnapshot.child("notes").getValue(String.class) != null ?
                                    laborSnapshot.child("notes").getValue(String.class) : "";
                            laborItems.add(item);
                            View itemView = createLaborItemView(item, laborItems.size() - 1);
                            laborItemViews.add(itemView);
                            laborItemsContainer.addView(itemView);
                        }
                    }

                    // Load material items
                    if (snapshot.hasChild("material")) {
                        for (com.google.firebase.database.DataSnapshot materialSnapshot : snapshot.child("material").getChildren()) {
                            MaterialItem item = new MaterialItem();
                            item.materialName = materialSnapshot.child("materialName").getValue(String.class) != null ?
                                    materialSnapshot.child("materialName").getValue(String.class) : "";
                            item.quantity = materialSnapshot.child("quantity").getValue(Double.class) != null ?
                                    materialSnapshot.child("quantity").getValue(Double.class) : 0.0;
                            item.quantityUnit = materialSnapshot.child("quantityUnit").getValue(String.class) != null ?
                                    materialSnapshot.child("quantityUnit").getValue(String.class) : "";
                            item.unitCost = materialSnapshot.child("unitCost").getValue(Double.class) != null ?
                                    materialSnapshot.child("unitCost").getValue(Double.class) : 0.0;
                            item.totalCost = materialSnapshot.child("totalCost").getValue(Double.class) != null ?
                                    materialSnapshot.child("totalCost").getValue(Double.class) : 0.0;
                            materialItems.add(item);
                            View itemView = createMaterialItemView(item, materialItems.size() - 1);
                            materialItemViews.add(itemView);
                            materialItemsContainer.addView(itemView);
                        }
                    }

                    // Load equipment items
                    if (snapshot.hasChild("equipment")) {
                        for (com.google.firebase.database.DataSnapshot equipmentSnapshot : snapshot.child("equipment").getChildren()) {
                            EquipmentItem item = new EquipmentItem();
                            item.equipmentName = equipmentSnapshot.child("equipmentName").getValue(String.class) != null ?
                                    equipmentSnapshot.child("equipmentName").getValue(String.class) : "";
                            // Handle both old format (usage as string) and new format (usageValue + usageUnit)
                            if (equipmentSnapshot.hasChild("usageValue")) {
                                item.usageValue = equipmentSnapshot.child("usageValue").getValue(Double.class) != null ?
                                        equipmentSnapshot.child("usageValue").getValue(Double.class) : 0.0;
                                item.usageUnit = equipmentSnapshot.child("usageUnit").getValue(String.class) != null ?
                                        equipmentSnapshot.child("usageUnit").getValue(String.class) : "hours";
                            } else if (equipmentSnapshot.hasChild("usage")) {
                                // Legacy format - try to parse as number or default to 0
                                String usageStr = equipmentSnapshot.child("usage").getValue(String.class);
                                try {
                                    item.usageValue = usageStr != null && !usageStr.isEmpty() ? Double.parseDouble(usageStr) : 0.0;
        } catch (NumberFormatException e) {
                                    item.usageValue = 0.0;
                                }
                                item.usageUnit = "hours"; // Default to hours for legacy data
                            }
                            item.isOwned = equipmentSnapshot.child("isOwned").getValue(Boolean.class) != null ?
                                    equipmentSnapshot.child("isOwned").getValue(Boolean.class) : false;
                            item.cost = equipmentSnapshot.child("cost").getValue(Double.class) != null ?
                                    equipmentSnapshot.child("cost").getValue(Double.class) : 0.0;
                            // Mark cost as locked if it was loaded from Firebase and has a value
                            if (item.cost > 0 && !item.isOwned) {
                                item.isCostLocked = true;
                            }
                            // Calculate totalCost if not already saved
                            if (equipmentSnapshot.hasChild("totalCost")) {
                                item.totalCost = equipmentSnapshot.child("totalCost").getValue(Double.class) != null ?
                                        equipmentSnapshot.child("totalCost").getValue(Double.class) : 0.0;
                            } else {
                                // Calculate from usage and cost
                                if (!item.isOwned && item.cost > 0 && item.usageValue > 0) {
                                    double usageInHours = convertToHours(item.usageValue, item.usageUnit);
                                    item.totalCost = usageInHours * item.cost;
                                }
                            }
                            equipmentItems.add(item);
                            View itemView = createEquipmentItemView(item, equipmentItems.size() - 1);
                            equipmentItemViews.add(itemView);
                            equipmentItemsContainer.addView(itemView);
                        }
                    }

                    // Load miscellaneous items
                    if (snapshot.hasChild("miscellaneous")) {
                        for (com.google.firebase.database.DataSnapshot miscSnapshot : snapshot.child("miscellaneous").getChildren()) {
                            MiscellaneousItem item = new MiscellaneousItem();
                            item.expenseName = miscSnapshot.child("expenseName").getValue(String.class) != null ?
                                    miscSnapshot.child("expenseName").getValue(String.class) : "";
                            item.cost = miscSnapshot.child("cost").getValue(Double.class) != null ?
                                    miscSnapshot.child("cost").getValue(Double.class) : 0.0;
                            miscItems.add(item);
                            View itemView = createMiscItemView(item, miscItems.size() - 1);
                            miscItemViews.add(itemView);
                            miscItemsContainer.addView(itemView);
                        }
                    }

                    calculateGrandTotal();
                    // If data exists, disable edit mode (view mode)
                    setEditMode(false);
                } else {
                    // No data exists - keep in edit mode (editable for first save)
                    setEditMode(true);
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                // Handle error
            }
        });
    }

    private void collectDataFromViews() {
        // Collect labor data
        for (int i = 0; i < laborItemViews.size() && i < laborItems.size(); i++) {
            updateLaborItemFromView(i);
        }
        // Update material items
        for (int i = 0; i < materialItemViews.size() && i < materialItems.size(); i++) {
            updateMaterialItemFromView(i);
        }
        for (int i = 0; i < equipmentItemViews.size() && i < equipmentItems.size(); i++) {
            updateEquipmentItemFromView(i);
        }
        for (int i = 0; i < miscItemViews.size() && i < miscItems.size(); i++) {
            updateMiscItemFromView(i);
        }
    }
    
    private void updateLaborItemFromView(int index) {
        if (index < 0 || index >= laborItems.size() || index >= laborItemViews.size()) {
            return;
        }

        View itemView = laborItemViews.get(index);
        LaborItem item = laborItems.get(index);
        
        // Find the EditText fields and Spinner
        EditText etNumWorkers = itemView.findViewWithTag("numWorkers_" + index);
        EditText etDailyWage = itemView.findViewWithTag("dailyWage_" + index);
        Spinner notesSpinner = itemView.findViewWithTag("notes_" + index);
        
        if (etNumWorkers != null) {
            try {
                String numWorkersStr = etNumWorkers.getText().toString().trim();
                item.numWorkers = numWorkersStr.isEmpty() ? 0 : Integer.parseInt(numWorkersStr);
        } catch (NumberFormatException e) {
                item.numWorkers = 0;
            }
        }
        
        if (etDailyWage != null) {
            try {
                String dailyWageStr = etDailyWage.getText().toString().trim();
                item.dailyWage = dailyWageStr.isEmpty() ? 0.0 : Double.parseDouble(dailyWageStr);
            } catch (NumberFormatException e) {
                item.dailyWage = 0.0;
            }
        }
        
        // Auto-calculate total cost when workers or wage change
        item.totalCost = item.numWorkers * item.dailyWage;
        
        // Get notes from Spinner or custom input
        if (notesSpinner != null) {
            int selectedPosition = notesSpinner.getSelectedItemPosition();
            String[] notesOptions = {"Plowing", "Harrowing", "Pruning", "Others"};
            if (selectedPosition == notesOptions.length - 1) {
                // "Others" selected - get from custom input
                EditText etCustomActivity = itemView.findViewWithTag("customActivity_" + index);
                if (etCustomActivity != null) {
                    String customText = etCustomActivity.getText().toString().trim();
                    item.notes = customText.isEmpty() ? "Others" : customText;
                } else {
                    item.notes = "Others";
                }
            } else {
                // Regular option selected
                String selectedNote = (String) notesSpinner.getSelectedItem();
                if (selectedNote != null) {
                    item.notes = selectedNote;
                }
            }
        }
        
        // Recalculate totals
        calculateLaborTotal();
    }
    
    private void updateMaterialItemFromView(int index) {
        if (index < 0 || index >= materialItems.size() || index >= materialItemViews.size()) {
            return;
        }
        
        View itemView = materialItemViews.get(index);
        MaterialItem item = materialItems.get(index);
        
        // Find the EditText fields and Spinners
        Spinner materialNameSpinner = itemView.findViewWithTag("materialName_" + index);
        EditText etQuantity = itemView.findViewWithTag("quantity_" + index);
        Spinner quantityUnitSpinner = itemView.findViewWithTag("quantityUnit_" + index);
        EditText etUnitCost = itemView.findViewWithTag("unitCost_" + index);
        
        // Get material name from Spinner or custom input
        if (materialNameSpinner != null) {
            int selectedPosition = materialNameSpinner.getSelectedItemPosition();
            String[] materialOptions = {"seeds", "fertilizer", "pesticide", "compost", "Others"};
            if (selectedPosition == materialOptions.length - 1) {
                // "Others" selected - get from custom input
                EditText etCustomMaterial = itemView.findViewWithTag("customMaterial_" + index);
                if (etCustomMaterial != null) {
                    String customText = etCustomMaterial.getText().toString().trim();
                    item.materialName = customText.isEmpty() ? "Others" : customText;
                } else {
                    item.materialName = "Others";
                }
            } else {
                // Regular option selected
                String selectedMaterial = (String) materialNameSpinner.getSelectedItem();
                if (selectedMaterial != null) {
                    item.materialName = selectedMaterial;
                }
            }
        }
        
        // Get quantity value
        if (etQuantity != null) {
            try {
                String quantityStr = etQuantity.getText().toString().trim();
                item.quantity = quantityStr.isEmpty() ? 0.0 : Double.parseDouble(quantityStr);
            } catch (NumberFormatException e) {
                item.quantity = 0.0;
            }
        }
        
        // Get quantity unit from Spinner
        if (quantityUnitSpinner != null) {
            String selectedUnit = (String) quantityUnitSpinner.getSelectedItem();
            if (selectedUnit != null) {
                item.quantityUnit = selectedUnit;
            }
        }
        
        // Get unit cost
        if (etUnitCost != null) {
            try {
                String unitCostStr = etUnitCost.getText().toString().trim();
                item.unitCost = unitCostStr.isEmpty() ? 0.0 : Double.parseDouble(unitCostStr);
            } catch (NumberFormatException e) {
                item.unitCost = 0.0;
            }
        }
        
        // Auto-calculate total cost
        item.totalCost = item.quantity * item.unitCost;
        
        // Recalculate totals
        calculateMaterialTotal();
    }
    
    private void recalculateMaterialItemIndices() {
        // Recalculate indices after deletion
        for (int i = 0; i < materialItemViews.size(); i++) {
            View view = materialItemViews.get(i);
            // Update title if needed
        }
    }
    
    private void updateEquipmentItemFromView(int index) {
        if (index < 0 || index >= equipmentItems.size() || index >= equipmentItemViews.size()) {
            return;
        }

        View itemView = equipmentItemViews.get(index);
        EquipmentItem item = equipmentItems.get(index);
        
        // Find the EditText fields, Spinner, and CheckBox
        EditText etEquipmentName = itemView.findViewWithTag("equipmentName_" + index);
        EditText etUsageValue = itemView.findViewWithTag("usageValue_" + index);
        Spinner usageUnitSpinner = itemView.findViewWithTag("usageUnit_" + index);
        android.widget.CheckBox cbOwned = itemView.findViewWithTag("owned_" + index);
        EditText etCost = itemView.findViewWithTag("cost_" + index);
        
        if (etEquipmentName != null) {
            item.equipmentName = etEquipmentName.getText().toString().trim();
        }
        
        // Get usage value
        if (etUsageValue != null) {
            try {
                String usageValueStr = etUsageValue.getText().toString().trim();
                item.usageValue = usageValueStr.isEmpty() ? 0.0 : Double.parseDouble(usageValueStr);
            } catch (NumberFormatException e) {
                item.usageValue = 0.0;
            }
        }
        
        // Get usage unit from Spinner
        if (usageUnitSpinner != null) {
            String selectedUnit = (String) usageUnitSpinner.getSelectedItem();
            if (selectedUnit != null) {
                item.usageUnit = selectedUnit;
            }
        }
        
        // Get ownership status
        if (cbOwned != null) {
            item.isOwned = cbOwned.isChecked();
        }
        
        // Only update cost if not owned
        if (etCost != null && !item.isOwned) {
            try {
                String costStr = etCost.getText().toString().trim();
                if (costStr.isEmpty()) {
                    item.cost = 0.0;
                } else {
                    item.cost = Double.parseDouble(costStr);
                }
            } catch (NumberFormatException e) {
                item.cost = 0.0;
            }
        } else if (item.isOwned) {
            item.cost = 0.0; // Cost is 0 if owned
            item.totalCost = 0.0; // Total cost is 0 if owned
        }
        
        // Calculate total cost: (usageValue converted to hours) × cost
        if (!item.isOwned && item.cost > 0 && item.usageValue > 0) {
            double usageInHours = convertToHours(item.usageValue, item.usageUnit);
            item.totalCost = usageInHours * item.cost;
        } else {
            item.totalCost = 0.0;
        }
        
        // Recalculate totals
        calculateEquipmentTotal();
    }
    
    private void recalculateEquipmentItemIndices() {
        // Recalculate indices after deletion
        for (int i = 0; i < equipmentItemViews.size(); i++) {
            View view = equipmentItemViews.get(i);
            // Update title if needed
        }
    }
    
    private void updateMiscItemFromView(int index) {
        if (index < 0 || index >= miscItems.size() || index >= miscItemViews.size()) {
            return;
        }
        
        View itemView = miscItemViews.get(index);
        MiscellaneousItem item = miscItems.get(index);
        
        // Find the Spinner and EditText fields
        Spinner expenseNameSpinner = itemView.findViewWithTag("expenseName_" + index);
        EditText etCost = itemView.findViewWithTag("cost_" + index);
        
        // Get expense name from Spinner or custom input
        if (expenseNameSpinner != null) {
            int selectedPosition = expenseNameSpinner.getSelectedItemPosition();
            String[] expenseOptions = {
                "Fuel",
                "Electricity",
                "Land Permit",
                "Contingency",
                "Irrigation Water",
                "Transportation",
                "Packaging Materials",
                "Marketing Expenses",
                "Insurance",
                "Loan Interest",
                "Taxes",
                "Storage Fees",
                "Utilities",
                "Repair/Maintenance",
                "Foods for Labor",
                "Miscellaneous Supplies",
                "Others"
            };
            if (selectedPosition == expenseOptions.length - 1) {
                // "Others" selected - get from custom input
                EditText etCustomExpense = itemView.findViewWithTag("customExpense_" + index);
                if (etCustomExpense != null) {
                    String customText = etCustomExpense.getText().toString().trim();
                    item.expenseName = customText.isEmpty() ? "Others" : customText;
                } else {
                    item.expenseName = "Others";
                }
            } else {
                // Regular option selected
                String selectedExpense = (String) expenseNameSpinner.getSelectedItem();
                if (selectedExpense != null) {
                    item.expenseName = selectedExpense;
                }
            }
        }
        
        if (etCost != null) {
            try {
                String costStr = etCost.getText().toString().trim();
                item.cost = costStr.isEmpty() ? 0.0 : Double.parseDouble(costStr);
            } catch (NumberFormatException e) {
                item.cost = 0.0;
            }
        }
        
        // Recalculate totals
        calculateMiscTotal();
    }
    
    private void recalculateMiscItemIndices() {
        // Recalculate indices after deletion
        for (int i = 0; i < miscItemViews.size(); i++) {
            View view = miscItemViews.get(i);
            // Update title if needed
        }
    }
}


