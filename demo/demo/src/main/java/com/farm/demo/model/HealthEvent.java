package com.farm.demo.model;

import java.time.LocalDate;

public class HealthEvent {
    private LocalDate date;
    private String eventType;
    private String administeredBy;
    private String notes;

    public HealthEvent(LocalDate date, String eventType, String administeredBy, String notes) {
        this.date = date;
        this.eventType = eventType;
        this.administeredBy = administeredBy;
        this.notes = notes;
    }

    public LocalDate getDate() { return date; }
    public String getEventType() { return eventType; }
    public String getAdministeredBy() { return administeredBy; }
    public String getNotes() { return notes; }
}

