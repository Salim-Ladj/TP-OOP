package com.farm.demo.model;

import java.util.List;
import java.util.ArrayList;

public class AquacultureZone extends Zone {
    // Attributes
    private List<Animal> species;
    private int numberOfAnimals; 
    private FeedingProgram feedingProgram;

    // Constructor
    public AquacultureZone(String code, String name) {
        super(code, name);
        this.species = new ArrayList<>();
        this.numberOfAnimals = 0;
    }

    public void setFeedingProgram(FeedingProgram fp) {
        if (this.getStatus() == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.getCode() + " is currently SUSPENDED.");
        }
        this.feedingProgram = fp;
    }

    public FeedingProgram getFeedingProgram(){
        return this.feedingProgram;
    }

    // Methods
    @Override
    public void addEntity(Object entity) {
        if (this.getStatus() == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.getCode() + " is currently SUSPENDED.");
        }

        // Enforcing Type Rule
        if (!(entity instanceof Animal)) {
            throw new IllegalArgumentException("Type Error: AquacultureZone cannot accept " + entity.getClass().getSimpleName());
        }
        this.species.add((Animal) entity);
        this.numberOfAnimals++;
    }

    @Override
    public void display() {
        System.out.println("Aquaculture Zone: " + name);
        System.out.println("Status: " + status);
        System.out.println("Number of Aquatic Species: " + numberOfAnimals);
    }
    public List<Animal> getAnimals() {
        return species;
    }

    @Override
    public void setProductionRecord(ProductionRecord record) {
        if (this.getStatus() == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.getCode() + " is currently SUSPENDED.");
        }
        if(record.getType() != ProductionType.AQUACULTURE) {
            throw new IllegalArgumentException("Type Error: ProductionRecord of type " + record.getType() + " cannot be set for AquacultureZone.");
        }
        this.productionRecord = record;
    }
}