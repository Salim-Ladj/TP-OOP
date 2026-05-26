package com.farm.demo.model;

import java.time.LocalDateTime;
import java.util.Optional;

public class GPSCollarSensor extends Sensor {
    private double lastLongitudeStored;
    private double minThresLong;
    private double maxThresLong;

    public GPSCollarSensor(String uniqueCode, String zoneId, double minLatitudeThreshold, double maxLatitudeThreshold,
                           double minLongitudeThreshold, double maxLongitudeThreshold, String unitOfMeasurement) throws ThresholdException {
        super(uniqueCode, zoneId, minLatitudeThreshold, maxLatitudeThreshold, unitOfMeasurement);
        this.minThresLong = minLongitudeThreshold;
        this.maxThresLong = maxLongitudeThreshold;
        this.lastLongitudeStored = 0.0;
    }

    @Override
    protected String processReading(double value) {
        Object[] latStatusResult = getReadingStatus(value);
        String latStatusMessage = (String) latStatusResult[1];

        if (! (Boolean) latStatusResult[0]) {
            SeverityLevel severity = latStatusMessage.equals("Critical") ? SeverityLevel.CRITICAL : SeverityLevel.WARNING;
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, severity,
                                     "GPS COLLAR (LATITUDE) ALERT (" + uniqueCode + "): " + latStatusMessage + " reading: " + value + " " + UnitOfMeasurement);
            return latStatusMessage + " - Alert created for Latitude.";
        } else {
            return "Normal latitude reading: " + value + " " + UnitOfMeasurement;
        }
    }

    public String addGPSReading(double latitude, double longitude) { 
        if (this.status != SensorStatus.ACTIVE) {
            return "GPS Sensor " + uniqueCode + " is " + this.status.name() + ". Reading not processed.";
        }

        LocalDateTime now = LocalDateTime.now();
        SensorReading newLatitudeReading = new SensorReading(latitude, now);
        this.readings.add(newLatitudeReading); 
        this.lastLongitudeStored = longitude;

        return _processCombinedGPSReading(latitude, longitude);
    }

    protected Object[] getLongitudeStatus(double longitude) {
        if (longitude < 0.8 * minThresLong || longitude > 1.2 * maxThresLong) {
            return new Object[]{false, "Critical"};
        } else if (longitude < minThresLong || longitude > maxThresLong) {
            return new Object[]{false, "Warning"};
        } else {
            return new Object[]{true, "Normal"};
        }
    }

    private String _processCombinedGPSReading(double latitude, double longitude) {
        Object[] latStatusResult = getReadingStatus(latitude);
        String latStatusMessage = (String) latStatusResult[1];

        Object[] longStatusResult = getLongitudeStatus(longitude);
        String longStatusMessage = (String) longStatusResult[1];

        boolean latCritical = "Critical".equals(latStatusMessage);
        boolean latWarning = "Warning".equals(latStatusMessage);
        boolean longCritical = "Critical".equals(longStatusMessage);
        boolean longWarning = "Warning".equals(longStatusMessage);

        if (latCritical || longCritical) {
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), latitude, SeverityLevel.CRITICAL,
                                     "GPS COLLAR ALERT (" + uniqueCode + "): Critical deviation in Latitude (" + latStatusMessage + ") or Longitude (" + longStatusMessage + "). Lat: " + latitude + ", Long: " + longitude);
            return "Critical - Alert created for Lat/Long.";
        } else if (latWarning || longWarning) {
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), latitude, SeverityLevel.WARNING,
                                     "GPS COLLAR ALERT (" + uniqueCode + "): Warning deviation in Latitude (" + latStatusMessage + ") or Longitude (" + longStatusMessage + "). Lat: " + latitude + ", Long: " + longitude);
            return "Warning - Alert created for Lat/Long.";
        } else {
            return "Normal reading: Lat: " + latitude + ", Long: " + longitude;
        }
    }

    public Optional<Double> getLastLatitudeValue() {
        return getLastReadingValue();
    }

    public double getLastLongitudeStored() {
        return lastLongitudeStored;
    }

    public double getMinThresLong() {
        return minThresLong;
    }

    public void setMinThresLong(double minThresLong) {
        this.minThresLong = minThresLong;
    }

    public double getMaxThresLong() {
        return maxThresLong;
    }

    public void setMaxThresLong(double maxThresLong) {
        this.maxThresLong = maxThresLong;
    }
}