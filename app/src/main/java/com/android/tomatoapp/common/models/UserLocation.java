package com.android.tomatoapp.common.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserLocation {
    public String id;
    public String province;
    public String city;
    public String brgy;
    public double lat;
    public double lon;
    public boolean isDefault;

    public UserLocation() {
        // Default constructor for Firebase
    }

    public UserLocation(String id, String province, String city, String brgy, double lat, double lon, boolean isDefault) {
        this.id = id;
        this.province = province;
        this.city = city;
        this.brgy = brgy;
        this.lat = lat;
        this.lon = lon;
        this.isDefault = isDefault;
    }

    public String getFullLabel() {
        StringBuilder label = new StringBuilder();
        appendPart(label, brgy);
        appendPart(label, city);
        appendPart(label, province);
        return label.toString();
    }

    private void appendPart(StringBuilder label, String value) {
        if (value != null && !value.trim().isEmpty()) {
            if (label.length() > 0) {
                label.append(", ");
            }
            label.append(value.trim());
        }
    }

    @NonNull
    @Override
    public String toString() {
        return getFullLabel();
    }
}
