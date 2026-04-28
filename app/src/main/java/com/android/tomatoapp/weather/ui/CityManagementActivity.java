package com.android.tomatoapp.weather.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tomatoapp.R;
import com.android.tomatoapp.common.models.UserLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.AutoCompleteTextView;
import com.android.tomatoapp.common.utils.PhilippineLocations;
import java.util.Collections;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CityManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View emptyState;
    private List<UserLocation> locationList = new ArrayList<>();
    private List<UserLocation> filteredList = new ArrayList<>();
    private LocationAdapter adapter;
    private android.widget.EditText searchEditText;
    private DatabaseReference userLocationsRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_management);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        userLocationsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("locations");

        recyclerView = findViewById(R.id.cityRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LocationAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddCity).setOnClickListener(v -> showStructuredLocationDialog());

        setupSearch();
        loadLocations();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(locationList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (UserLocation loc : locationList) {
                if (loc.city.toLowerCase().contains(lowerQuery) || 
                    loc.province.toLowerCase().contains(lowerQuery)) {
                    filteredList.add(loc);
                }
            }
        }
        updateUI();
    }

    private void loadLocations() {
        userLocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locationList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserLocation loc = ds.getValue(UserLocation.class);
                    if (loc != null) {
                        loc.id = ds.getKey();
                        locationList.add(loc);
                    }
                }
                filter(searchEditText.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CityManagementActivity.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (filteredList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void showStructuredLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_picker, null);
        AlertDialog dialog = builder.setView(dialogView).create();

        AutoCompleteTextView provinceSpinner = dialogView.findViewById(R.id.provinceAutoComplete);
        AutoCompleteTextView citySpinner = dialogView.findViewById(R.id.cityAutoComplete);
        AutoCompleteTextView brgySpinner = dialogView.findViewById(R.id.brgyAutoComplete);
        
        com.google.android.material.textfield.TextInputLayout cityLayout = dialogView.findViewById(R.id.cityLayout);
        com.google.android.material.textfield.TextInputLayout brgyLayout = dialogView.findViewById(R.id.brgyLayout);
        
        android.widget.Button btnApply = dialogView.findViewById(R.id.btnApply);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        final String[] provinceSelected = {""};
        final String[] citySelected = {""};
        final String[] brgySelected = {""};
        final int[] finalRegionIndex = {-1};

        List<String> allProvincesList = new ArrayList<>();
        for (int i = 0; i <= 17; i++) {
            String[] labels = getLabelsForRegion(i);
            for (String label : labels) {
                String[] parts = label.split(", ");
                if (parts.length > 1 && !allProvincesList.contains(parts[1])) {
                    allProvincesList.add(parts[1]);
                }
            }
        }
        Collections.sort(allProvincesList);
        android.widget.ArrayAdapter<String> provinceAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allProvincesList);
        provinceSpinner.setAdapter(provinceAdapter);

        provinceSpinner.setOnItemClickListener((parent, view, position, id) -> {
            provinceSelected[0] = (String) parent.getItemAtPosition(position);
            citySpinner.setText(""); brgySpinner.setText("");
            cityLayout.setEnabled(true); brgyLayout.setEnabled(false);
            btnApply.setEnabled(false);

            List<String> citiesInProvince = new ArrayList<>();
            for (int i = 0; i <= 17; i++) {
                String[] labels = getLabelsForRegion(i);
                for (String label : labels) {
                    if (label.contains(provinceSelected[0])) {
                        citiesInProvince.add(label.split(", ")[0]);
                        finalRegionIndex[0] = i;
                    }
                }
            }
            Collections.sort(citiesInProvince);
            citySpinner.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, citiesInProvince));
        });

        citySpinner.setOnItemClickListener((parent, view, position, id) -> {
            citySelected[0] = (String) parent.getItemAtPosition(position);
            brgySpinner.setText(""); brgyLayout.setEnabled(true); btnApply.setEnabled(false);
            brgySpinner.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, getMockBarangays(citySelected[0])));
        });

        brgySpinner.setOnItemClickListener((parent, view, position, id) -> {
            brgySelected[0] = (String) parent.getItemAtPosition(position);
            btnApply.setEnabled(true);
        });

        btnApply.setOnClickListener(v -> {
            // Save to Firebase
            double lat = 14.5995, lon = 120.9842;
            String[] labels = getLabelsForRegion(finalRegionIndex[0]);
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].startsWith(citySelected[0])) {
                    lat = getLatsForRegion(finalRegionIndex[0])[i];
                    lon = getLonsForRegion(finalRegionIndex[0])[i];
                    break;
                }
            }

            String locId = userLocationsRef.push().getKey();
            if (locId != null) {
                com.android.tomatoapp.common.models.UserLocationEntity newLocEntity = new com.android.tomatoapp.common.models.UserLocationEntity(
                        locId, currentUser.getUid(), provinceSelected[0], citySelected[0], brgySelected[0], lat, lon, locationList.isEmpty()
                );
                
                com.android.tomatoapp.core.network.LocalDataManager.getInstance(this).saveUserLocationToLocal(newLocEntity, "CREATE");
                com.android.tomatoapp.core.network.LocalDataManager.getInstance(this).processSyncQueue(this, currentUser.getUid());
                
                // Keep the list updated immediately
                UserLocation newLoc = new UserLocation(locId, provinceSelected[0], citySelected[0], brgySelected[0], lat, lon, locationList.isEmpty());
                userLocationsRef.child(locId).setValue(newLoc); // Firebase offline cache will also handle this
                Toast.makeText(this, "Added " + citySelected[0], Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String[] getMockBarangays(String city) {
        return new String[]{"Poblacion", "San Jose", "Santa Maria", "San Pedro", "Santo Niño", "San Juan", "Santa Cruz", "San Roque", "Maligaya", "Bagong Pag-asa"};
    }

    private String[] getLabelsForRegion(int index) {
        switch (index) {
            case 0: return PhilippineLocations.getRegion1Labels();
            case 1: return PhilippineLocations.getRegion2Labels();
            case 2: return PhilippineLocations.getRegion3Labels();
            case 3: return PhilippineLocations.getRegion4Labels();
            case 4: return PhilippineLocations.getRegion4BLabels();
            case 5: return PhilippineLocations.getRegion5Labels();
            case 6: return PhilippineLocations.getRegion6Labels();
            case 7: return PhilippineLocations.getRegion7Labels();
            case 8: return PhilippineLocations.getRegion8Labels();
            case 9: return PhilippineLocations.getRegion9Labels();
            case 10: return PhilippineLocations.getRegion10Labels();
            case 11: return PhilippineLocations.getRegion11Labels();
            case 12: return PhilippineLocations.getRegion12Labels();
            case 13: return PhilippineLocations.getRegion13Labels();
            case 14: return PhilippineLocations.getRegion14Labels();
            case 15: return PhilippineLocations.getRegion15Labels();
            case 16: return PhilippineLocations.getRegion16Labels();
            default: return PhilippineLocations.getRegion17Labels();
        }
    }

    private double[] getLatsForRegion(int index) {
        switch (index) {
            case 0: return PhilippineLocations.getRegion1Lats();
            case 1: return PhilippineLocations.getRegion2Lats();
            case 2: return PhilippineLocations.getRegion3Lats();
            case 3: return PhilippineLocations.getRegion4Lats();
            case 4: return PhilippineLocations.getRegion4BLats();
            case 5: return PhilippineLocations.getRegion5Lats();
            case 6: return PhilippineLocations.getRegion6Lats();
            case 7: return PhilippineLocations.getRegion7Lats();
            case 8: return PhilippineLocations.getRegion8Lats();
            case 9: return PhilippineLocations.getRegion9Lats();
            case 10: return PhilippineLocations.getRegion10Lats();
            case 11: return PhilippineLocations.getRegion11Lats();
            case 12: return PhilippineLocations.getRegion12Lats();
            case 13: return PhilippineLocations.getRegion13Lats();
            case 14: return PhilippineLocations.getRegion14Lats();
            case 15: return PhilippineLocations.getRegion15Lats();
            case 16: return PhilippineLocations.getRegion16Lats();
            default: return PhilippineLocations.getRegion17Lats();
        }
    }

    private double[] getLonsForRegion(int index) {
        switch (index) {
            case 0: return PhilippineLocations.getRegion1Lons();
            case 1: return PhilippineLocations.getRegion2Lons();
            case 2: return PhilippineLocations.getRegion3Lons();
            case 3: return PhilippineLocations.getRegion4Lons();
            case 4: return PhilippineLocations.getRegion4BLons();
            case 5: return PhilippineLocations.getRegion5Lons();
            case 6: return PhilippineLocations.getRegion6Lons();
            case 7: return PhilippineLocations.getRegion7Lons();
            case 8: return PhilippineLocations.getRegion8Lons();
            case 9: return PhilippineLocations.getRegion9Lons();
            case 10: return PhilippineLocations.getRegion10Lons();
            case 11: return PhilippineLocations.getRegion11Lons();
            case 12: return PhilippineLocations.getRegion12Lons();
            case 13: return PhilippineLocations.getRegion13Lons();
            case 14: return PhilippineLocations.getRegion14Lons();
            case 15: return PhilippineLocations.getRegion15Lons();
            case 16: return PhilippineLocations.getRegion16Lons();
            default: return PhilippineLocations.getRegion17Lons();
        }
    }

    private void setDefaultLocation(UserLocation location) {
        for (UserLocation loc : locationList) {
            userLocationsRef.child(loc.id).child("isDefault").setValue(loc.id.equals(location.id));
        }
        
        // Update current active location in SharedPreferences for immediate use
        SharedPreferences wp = getSharedPreferences("WeatherPref", MODE_PRIVATE);
        wp.edit()
                .putLong("lat", Double.doubleToLongBits(location.lat))
                .putLong("lon", Double.doubleToLongBits(location.lon))
                .putString("name", location.getFullLabel())
                .apply();
        
        Toast.makeText(this, location.city + " set as default", Toast.LENGTH_SHORT).show();
    }

    private void removeLocation(UserLocation location) {
        if (location.isDefault) {
            Toast.makeText(this, "Cannot remove default location", Toast.LENGTH_SHORT).show();
            return;
        }
        userLocationsRef.child(location.id).removeValue();
    }

    // Inner Adapter Class
    private class LocationAdapter extends RecyclerView.Adapter<LocationViewHolder> {
        private final List<UserLocation> items;

        LocationAdapter(List<UserLocation> items) { this.items = items; }

        @NonNull
        @Override
        public LocationViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_city_card, parent, false);
            return new LocationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
            UserLocation loc = items.get(position);
            holder.cityName.setText(loc.city);
            holder.locationPath.setText(loc.province + " • " + loc.brgy);
            holder.defaultBadge.setVisibility(loc.isDefault ? View.VISIBLE : View.GONE);
            
            holder.btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(CityManagementActivity.this, v);
                popup.getMenu().add("Set as Default");
                popup.getMenu().add("Remove");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Set as Default")) setDefaultLocation(loc);
                    else if (item.getTitle().equals("Remove")) removeLocation(loc);
                    return true;
                });
                popup.show();
            });
        }

        @Override
        public int getItemCount() { return items.size(); }
    }

    private static class LocationViewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView cityName, locationPath, defaultBadge;
        android.widget.ImageButton btnMenu;
        LocationViewHolder(View v) {
            super(v);
            cityName = v.findViewById(R.id.cityName);
            locationPath = v.findViewById(R.id.locationPath);
            defaultBadge = v.findViewById(R.id.defaultBadge);
            btnMenu = v.findViewById(R.id.btnMenu);
        }
    }
}
