package com.android.tomatoapp.common.models;

public class LocationEntry {
    public final String name;
    public final double latitude;
    public final double longitude;
    public final String region;
    public final String province;
    public final String cityMunicipality;
    public final String barangay;

    public LocationEntry(String name, double latitude, double longitude, String region, String province, String cityMunicipality, String barangay) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.region = region;
        this.province = province;
        this.cityMunicipality = cityMunicipality;
        this.barangay = barangay;
    }

    public String getDisplayName() {
        if (barangay != null && !barangay.isEmpty()) {
            return barangay + ", " + cityMunicipality + ", " + province;
        }
        return name;
    }
}

