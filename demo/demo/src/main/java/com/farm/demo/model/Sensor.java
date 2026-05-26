package com.farm.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class Sensor {
    protected String uniqueCode;
    protected String zoneID;
    protected SensorStatus status;
    protected double minThres; 
    protected double maxThres; 
    protected String UnitOfMeasurement;
    protected List<SensorReading> readings; 

    public Sensor(String uniqueCode, String zoneID, double minThres, double maxThres, String unit) throws ThresholdException {
        if (minThres < 0 || maxThres < 0 || minThres >= maxThres) {
            throw new ThresholdException("Invalid threshold values");
        }
        this.uniqueCode = uniqueCode;
        this.zoneID = zoneID;
        this.status = SensorStatus.ACTIVE;
        this.UnitOfMeasurement = unit;
        this.minThres = minThres;
        this.maxThres = maxThres;
        this.readings = new ArrayList<>(); 
    }

    // Convenience constructor with defaults to simplify sample data creation
    public Sensor(String uniqueCode) {
        this.uniqueCode = uniqueCode;
        this.zoneID = "";
        this.status = SensorStatus.ACTIVE;
        this.UnitOfMeasurement = "units";
        this.minThres = 0.0;
        this.maxThres = 100.0;
        this.readings = new ArrayList<>();
    }

    public String getUniqueCode() {
        return uniqueCode;
    }
    public String getZoneId() {
        return zoneID;
    }
    public SensorStatus getStatus() {
        return status;
    }

    public Optional<Double> getLastReadingValue() {
        return readings.isEmpty() ? Optional.empty() : Optional.of(readings.get(readings.size() - 1).getValue());
    }

    public Optional<LocalDateTime> getLastReadingTimestamp() {
        return readings.isEmpty() ? Optional.empty() : Optional.of(readings.get(readings.size() - 1).getTimestamp());
    }

    public double getMinThreshold() {
        return minThres;
    }
    public double getMaxThreshold() {
        return maxThres;
    }
    public void setMinThreshold(double minThres) throws ThresholdException {
        if (minThres < 0 || minThres >= maxThres) {
            throw new ThresholdException("Invalid threshold values");
        }
        this.minThres = minThres;
    }
    public void setMaxThreshold(double maxThres) throws ThresholdException {
        if (maxThres < 0 || maxThres <= minThres) {
            throw new ThresholdException("Invalid threshold values");
        }
        this.maxThres = maxThres;
    }
    public String getUnitOfMeasurement() {
        return UnitOfMeasurement;
    }

    public void changeStatus(SensorStatus newStatus) {
        this.status = newStatus;
    }

    public Object[] getReadingStatus(double value) {
        if (value < 0.8 * minThres || value > 1.2 * maxThres) {
            return new Object[]{false, "Critical"};
        } else if (value < minThres || value > maxThres) {
            return new Object[]{false, "Warning"};
        } else {
            return new Object[]{true, "Normal"};
        }
    }

    public final String addReading(double newValue) { 
        if (this.status != SensorStatus.ACTIVE) {
            return "Sensor " + uniqueCode + " is " + this.status.name() + ". Reading not processed.";
        }
        
        LocalDateTime now = LocalDateTime.now();
        SensorReading newSensorReading = new SensorReading(newValue, now);
        this.readings.add(newSensorReading);

        return processReading(newValue);
    }

    protected abstract String processReading(double value);

    public List<SensorReading> getReadingsHistory(LocalDateTime startDate, LocalDateTime endDate) {
        return readings.stream()
                       .filter(reading -> (startDate == null || reading.getTimestamp().isAfter(startDate) || reading.getTimestamp().isEqual(startDate)))
                       .filter(reading -> (endDate == null || reading.getTimestamp().isBefore(endDate) || reading.getTimestamp().isEqual(endDate)))
                       .sorted(Comparator.comparing(SensorReading::getTimestamp).reversed())
                       .collect(Collectors.toList());
    }

    public List<SensorReading> getAllReadings() {
        return Collections.unmodifiableList(readings);
    }

    public void suspend() {
        changeStatus(SensorStatus.SUSPENDED);
    }
    public void reactivate() {
        changeStatus(SensorStatus.ACTIVE);
    }
    public void markAsFaulty() {
        changeStatus(SensorStatus.FAULTY);
    }
}