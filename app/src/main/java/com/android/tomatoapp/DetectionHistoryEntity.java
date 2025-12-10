package com.android.tomatoapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "detection_history")
public class DetectionHistoryEntity {
    @PrimaryKey
    @NonNull
    public String id;

    public String userId;
    public String programId;
    public String imageUri;
    public String disease;
    public String accuracy;
    public String description;
    public String symptoms;
    public String cause;
    public String cure;
    public String prevention;
    public String pestTitle;
    public String pestDescription;
    public String pestImageUri;
    public long timestamp;
    public String cultivar;
    public int phase;
    public long lastSynced;

    public DetectionHistoryEntity() {
        // Default constructor required for Room
        this.id = "";
    }

    @Ignore
    public DetectionHistoryEntity(@NonNull String id, String userId, String programId,
                                 String imageUri, String disease, String accuracy,
                                 String description, String symptoms, String cause,
                                 String cure, String prevention, String pestTitle,
                                 String pestDescription, String pestImageUri,
                                 long timestamp, String cultivar, int phase, long lastSynced) {
        this.id = id;
        this.userId = userId;
        this.programId = programId;
        this.imageUri = imageUri;
        this.disease = disease;
        this.accuracy = accuracy;
        this.description = description;
        this.symptoms = symptoms;
        this.cause = cause;
        this.cure = cure;
        this.prevention = prevention;
        this.pestTitle = pestTitle;
        this.pestDescription = pestDescription;
        this.pestImageUri = pestImageUri;
        this.timestamp = timestamp;
        this.cultivar = cultivar;
        this.phase = phase;
        this.lastSynced = lastSynced;
    }
}

