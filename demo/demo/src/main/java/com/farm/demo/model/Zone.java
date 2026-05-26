package com.farm.demo.model;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public abstract class Zone implements Suspendable {
    protected String code;
    protected String name;
    protected ZoneStatus status;
    protected ArrayList<Sensor> sensors; 
    protected ProductionRecord productionRecord;
    protected String location;

    public Zone(String code, String name) {
        this.code = code;
        this.name = name;
        this.status = ZoneStatus.ACTIVE;
        this.sensors = new ArrayList<>();
        this.location = "";
    }

    // Abstract Methods 
    public abstract void display();
    
    public abstract void addEntity(Object entity);

    // Common Methods (defined in the abstract class)
    public void addSensor(Sensor sensor) {
        if (this.status == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.code + " is currently SUSPENDED.");
        }
        if (sensor != null) {
            this.sensors.add(sensor);
        }
    }

    public abstract void setProductionRecord(ProductionRecord record);/*  {
        if (this.status == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.code + " is currently SUSPENDED.");
        }
        this.productionRecord = record;
    }*/

    // Interface Method Implementations
    @Override
    public void suspend() {
        this.status = ZoneStatus.SUSPENDED;
        for (Sensor sensor : sensors) {
            sensor.suspend();
        }
    }

    @Override
    public void reactivate() {
        this.status = ZoneStatus.ACTIVE;
        for (Sensor sensor : sensors) {
            sensor.reactivate();
        }
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public ZoneStatus getStatus() { return status; }
    public ProductionRecord getProductionRecord() { return productionRecord; }
    public List<Sensor> getSensors() { return sensors; }
    public Optional<Sensor> getSensorByUniqueCode(String uniqueCode) {
        return sensors.stream().filter(s -> s.getUniqueCode().equals(uniqueCode)).findFirst();
    }

    // New setters used by controllers
    public void setLocation(String location) { this.location = location; }
    public String getLocation() { return location; }
    public void setStatus(ZoneStatus status) { this.status = status; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
}