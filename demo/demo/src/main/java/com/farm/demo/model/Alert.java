package com.farm.demo.model;

import java.time.LocalDateTime;

public class Alert {
    private int alertId;
    private String sensorUniqueCode;
    private String zoneId;
    private LocalDateTime alertTimestamp;
    private double readingValue;
    private SeverityLevel severityLevel;
    private String message;
    private boolean isAcknowledged;

    public Alert(int alertId, String sensorUniqueCode, String zoneId, double readingValue, SeverityLevel severityLevel, String message) {
        this.alertId = alertId;
        this.sensorUniqueCode = sensorUniqueCode;
        this.zoneId = zoneId;
        this.alertTimestamp = LocalDateTime.now();
        this.readingValue = readingValue;
        this.severityLevel = severityLevel;
        this.message = message;
        this.isAcknowledged = false;
    }

    public int getAlertId() { return alertId; }
    public String getSensorUniqueCode() { return sensorUniqueCode; }
    public String getZoneId() { return zoneId; }
    public LocalDateTime getAlertTimestamp() { return alertTimestamp; }
    public double getReadingValue() { return readingValue; }
    public SeverityLevel getSeverityLevel() { return severityLevel; }
    public String getMessage() { return message; }
    public boolean isAcknowledged() { return isAcknowledged; }

    public String acknowledge() {
        if (!this.isAcknowledged) {
            this.isAcknowledged = true;
            return "Alert " + alertId + " (Sensor " + sensorUniqueCode + ") has been acknowledged.";
        }
        return "Alert " + alertId + " is already acknowledged.";
    }

    public String dismiss() {
        if (this.isAcknowledged) {
             this.isAcknowledged = false;
             return "Alert " + alertId + " (Sensor " + sensorUniqueCode + ") has been dismissed (un-acknowledged).";
        }
        return "Alert " + alertId + " was not acknowledged, or already dismissed.";
    }

    @Override
    public String toString() {
        return "Alert ID: " + alertId +
               ", Sensor: " + sensorUniqueCode +
               ", Zone: " + zoneId +
               ", Timestamp: " + alertTimestamp +
               ", Reading: " + readingValue +
               ", Severity: " + severityLevel +
               ", Message: " + message +
               ", Acknowledged: " + isAcknowledged;
    }
}