package com.android.tomatoapp.common.models;

public class DiseaseInfo {
    private String description;
    private String cause;
    private String prevention;
    private String cure;
    private String pest;
    private String symptoms;
    private String pestDescription;
    private boolean noTreatment = false;

    public DiseaseInfo(String description, String cause, String prevention, String cure,
                       String pest, String symptoms, String pestDescription) {
        this.description = description;
        this.cause = cause;
        this.prevention = prevention;
        this.cure = cure;
        this.pest = pest;
        this.symptoms = symptoms;
        this.pestDescription = pestDescription;
    }

    /** Marks a disease with no proven treatment. Chain it onto the constructor call. */
    public DiseaseInfo withNoTreatment() { this.noTreatment = true; return this; }
    public boolean hasNoTreatment() { return noTreatment; }

    public String getDescription() { return description; }
    public String getCause() { return cause; }
    public String getPrevention() { return prevention; }
    public String getCure() { return cure; }
    public String getPest() { return pest; }
    public String getSymptoms() { return symptoms; }
    public String getPestDescription() { return pestDescription; }
}