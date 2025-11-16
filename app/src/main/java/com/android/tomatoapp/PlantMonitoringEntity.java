package com.android.tomatoapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Local Room entity for plant monitoring entries.
 * Photos are stored locally; firebase sync handles text metadata only.
 */
@Entity(tableName = "plant_monitoring")
public class PlantMonitoringEntity {

    @PrimaryKey
    @NonNull
    public String id;

    public String userId;
    public String programId;
    public int phase;
    public long timestamp;

    public String shortDescription;
    public String issues;
    public String warnings;
    public String notes;
    public String detectionId;

    /**
     * Local filesystem path or content URI pointing to the captured photo.
     */
    public String photoPath;

    public PlantMonitoringEntity(@NonNull String id,
                                 String userId,
                                 String programId,
                                 int phase,
                                 long timestamp,
                                 String shortDescription,
                                 String issues,
                                 String warnings,
                                 String notes,
                                 String detectionId,
                                 String photoPath) {
        this.id = id;
        this.userId = userId;
        this.programId = programId;
        this.phase = phase;
        this.timestamp = timestamp;
        this.shortDescription = shortDescription;
        this.issues = issues;
        this.warnings = warnings;
        this.notes = notes;
        this.detectionId = detectionId;
        this.photoPath = photoPath;
    }
}

