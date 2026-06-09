package com.farm.demo.model;

import java.util.List;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class Animal {
    // Attributes
    private AnimalType type;
    private String uniqueNumber;
    private String species;
    private int age;
    private double weight;
    private HealthStatus health;
    private Map<HealthStatus, List<String>> healthHistory;
    private String zoneId;

    // Constructor (original detailed)
    public Animal(AnimalType type, String uniqueNumber, String species, int age, double weight, HealthStatus health) {
        this.type = type;
        this.uniqueNumber = uniqueNumber;
        this.species = species;
        this.age = age;
        this.weight = weight;
        this.health = health;
        this.healthHistory = new EnumMap<>(HealthStatus.class);
        for (HealthStatus status : HealthStatus.values()) {
            this.healthHistory.put(status, new ArrayList<>());
        }
        this.tag = "";
        this.feedConversionRatio = 0.0;
        this.nextVaccineDate = null;
     }

    // Convenience constructor used by controllers/views in this project
    public Animal(String uniqueNumber, String species, AnimalType type, double weight) {
        this(type, uniqueNumber, species, 0, weight, HealthStatus.HEALTHY);
    }

    // Minimal constructor
    public Animal(String uniqueNumber, String species) {
        this(AnimalType.RUMINANT, uniqueNumber, species, 0, 0.0, HealthStatus.HEALTHY);
    }

    // Methods
    public void logHealthEvent(String eventDescription, double currentWeight,int currentAge) {
        if (currentAge < 0 || currentWeight < 0) {
            throw new IllegalArgumentException("Age and weight must be positive values.");
        }
        if (currentAge < age ) {
            throw new IllegalArgumentException("Current age cannot be less than the previously recorded age.");
        }
        String event = "Age: " + currentAge + ", Weight: " + currentWeight + ", Event: " + eventDescription;
        age = currentAge;
        weight = currentWeight; 
        this.healthHistory.get(health).add(event);
    }
    public void displayHealthHistory() {
        System.out.println("Health History for " + species + " (ID: " + uniqueNumber + "):");
        for (Map.Entry<HealthStatus, List<String>> entry : healthHistory.entrySet()) {
            HealthStatus statusCategory = entry.getKey();
            List<String> eventsForStatus = entry.getValue();
            
            System.out.println("\nCategory [" + statusCategory + "]:" );
            if (eventsForStatus.isEmpty()) {
                System.out.println("  No historical events logged under this status.");
            } else {
                for (String record : eventsForStatus) {
                    System.out.println("  - " + record);
                }
            }
        }
    }
    public void display() {
        System.out.println("Animal Details:");
        System.out.println("Type: " + type);
        System.out.println("Animal ID: " + uniqueNumber);
        System.out.println("Species: " + species);
        System.out.println("Age: " + age);
        System.out.println("Weight: " + weight);
        System.out.println("Health Status: " + health);
    }
    // Getters and Setters
    public AnimalType getType() {
        return type;
    }
    public String getUniqueNumber() {
        return uniqueNumber;
    }
    public String getSpecies() {
        return species;
    }
    public int getAge() {
        return age;
    }
    public double getWeight() {
        return weight;
    }
    public HealthStatus getHealth() {
        return health;
    }
    public void setHealth(HealthStatus health) {
        this.health = health;
    }

    // Additional convenience fields used elsewhere in the codebase
    private String tag;
    private double feedConversionRatio;
    private java.time.LocalDate nextVaccineDate;

    // Convenience getters/setters to match callers throughout the project
    public String getId() { return uniqueNumber; }
    public void setSpecies(String species) { this.species = species; }
    public void setWeight(double weight) { this.weight = weight; }
    public AnimalType getAnimalType() { return type; }
    public void setType(AnimalType t) { this.type = t; }
    public HealthStatus getHealthStatus() { return health; }
    public void setHealthStatus(HealthStatus h) { this.health = h; }
    public String getTag() { return tag; }
    public void setTag(String t) { this.tag = t; }
    public double getFeedConversionRatio() { return feedConversionRatio; }
    public void setFeedConversionRatio(double v) { this.feedConversionRatio = v; }
    public java.time.LocalDate getNextVaccineDate() { return nextVaccineDate; }
    public void setNextVaccineDate(java.time.LocalDate d) { this.nextVaccineDate = d; }
    public java.util.Map<HealthStatus, java.util.List<String>> getHealthHistory() { return healthHistory; }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
    public String getZoneId() {return zoneId;}

}