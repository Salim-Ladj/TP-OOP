
package com.farm.demo.service;

import com.farm.demo.model.*;
        import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataService {
    private static DataService instance;
    private final Farm farm;

    // These are the persistent lists the UI tables are "watching"
    private final ObservableList<Zone> zones = FXCollections.observableArrayList();
    private final ObservableList<Animal> animals = FXCollections.observableArrayList();
    private final ObservableList<Crop> crops = FXCollections.observableArrayList();

    private DataService() {
        this.farm = new Farm("F-01", "Green Valley");
        seedInitialData();
        refreshAll();
    }

    public static DataService getInstance() {
        if (instance == null) instance = new DataService();
        return instance;
    }

    private void seedInitialData() {
        farm.addZone(new CropZone("CZ-01", "North Field"));
        farm.addZone(new LivestockZone("LZ-01", "East Paddock"));
    }

    public void refreshAll() {
        // Update Zones
        zones.setAll(farm.getZones());

        // Update Animals
        animals.clear();
        for (Zone z : farm.getZones()) {
            if (z instanceof LivestockZone lz) animals.addAll(lz.getAnimals());
            if (z instanceof AquacultureZone az) animals.addAll(az.getAnimals());
        }

        // Update Crops - THIS IS THE FIX
        crops.clear();
        for (Zone z : farm.getZones()) {
            if (z instanceof CropZone cz) {
                crops.addAll(cz.getCrops());
            }
        }
    }

    public ObservableList<Animal> getAllAnimals() {
        ObservableList<Animal> allAnimals = FXCollections.observableArrayList();
        for (Zone zone : farm.getZones()) {
            if (zone instanceof LivestockZone lz) {
                allAnimals.addAll(lz.getAnimals());
            } else if (zone instanceof AquacultureZone az) {
                allAnimals.addAll(az.getAnimals());
            }
        }
        return allAnimals;
    }

    public ObservableList<Crop> getAllCrops() {
        ObservableList<Crop> allCrops = FXCollections.observableArrayList();
        for (Zone zone : farm.getZones()) {
            if (zone instanceof CropZone cz) {
                allCrops.addAll(cz.getCrops());
            }
        }
        return allCrops;
    }

    // Stats for Dashboard
    public long getActiveAlertCount() {
        return farm.getActiveAlerts().size();
    }

    public int getTotalAnimals() {
        return getAnimals().size();
    }

    public int getTotalCrops() {
        return getCrops().size();
    }

    public int getTotalZones() {
        return farm.getZones().size();
    }

    public ObservableList<Zone> getZones() { return zones; }
    public ObservableList<Animal> getAnimals() { return animals; }
    public ObservableList<Crop> getCrops() { return crops; } // Return the persistent list
    public Farm getFarm() { return farm; }
}