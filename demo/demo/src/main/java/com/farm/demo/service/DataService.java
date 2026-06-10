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

    // --- AJOUT DE L'ESSENTIEL POUR LES ALERTS ET LES SENSORS ---
    // Listes persistantes écoutées par l'interface des capteurs et des alertes
    private final ObservableList<Alert> activeAlerts = FXCollections.observableArrayList();
    private final ObservableList<Sensor> sensors = FXCollections.observableArrayList();

    private DataService() {
        this.farm = new Farm("F-01", "Green Valley");
        // 1. Try to load from files
        FilePersistenceManager.loadData(farm);

        // 2. If files were empty, seed initial data
        if (farm.getZones().isEmpty()) {
            seedInitialData();
        }
        refreshAll();
    }

    public void saveToDisk() {
        FilePersistenceManager.saveData(farm);
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

        // --- AJOUT DE LA SYNCHRONISATION DES SENSORS ET DES ALERTS ---
        // 1. Synchronisation de la liste des capteurs
        sensors.clear();
        for (Zone z : farm.getZones()) {
            sensors.addAll(z.getSensors());
        }

        // 2. Synchronisation de la liste des alertes actives triées par gravité
        activeAlerts.setAll(AlertManager.getActiveAlertsSortedBySeverity());
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

    public ObservableList<ProductionRecord> getProductionRecords() {
        ObservableList<ProductionRecord> records = FXCollections.observableArrayList();
        for (Zone zone : farm.getZones()) {
            if (zone.getProductionRecord() != null) {
                records.add(zone.getProductionRecord());
            }
        }
        return records;
    }

    public ObservableList<Zone> getZonesWithFeeding() {
        return farm.getZones().stream()
                .filter(z -> z instanceof LivestockZone || z instanceof AquacultureZone)
                .collect(javafx.collections.FXCollections::observableArrayList,
                        java.util.List::add,
                        java.util.List::addAll);
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

    // --- ACCESSEURS POUR LES ALERTS ET LES SENSORS ---
    public ObservableList<Alert> getActiveAlerts() { return activeAlerts; }
    public ObservableList<Sensor> getSensors() { return sensors; }
}