package com.farm.demo.model;

import java.time.LocalDateTime;

public class SensorReading {
    private double value;
    private LocalDateTime timestamp;

    public SensorReading(double value, LocalDateTime timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Value: " + value + " at " + timestamp;
    }
}