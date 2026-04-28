package com.android.tomatoapp.common.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_locations")
public class UserLocationEntity {
    @PrimaryKey
    @NonNull
    public String id;
    
    public String userId;
    public String province;
    public String city;
    public String brgy;
    public double lat;
    public double lon;
    public boolean isDefault;
    public long lastUpdated;

    public UserLocationEntity() {
        this.id = "";
    }

    public UserLocationEntity(@NonNull String id, String userId, String province, String city, String brgy, double lat, double lon, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.province = province;
        this.city = city;
        this.brgy = brgy;
        this.lat = lat;
        this.lon = lon;
        this.isDefault = isDefault;
        this.lastUpdated = System.currentTimeMillis();
    }
}
