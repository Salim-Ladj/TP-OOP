package com.farm.demo.controller;

import com.farm.demo.model.*;
import com.farm.demo.storage.StorageManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LivestocksController {

    private final List<Animal> animals = new ArrayList<>();

    public LivestocksController() {
        loadData();
    }

    public List<Animal> getAllAnimals() { return animals; }

    public Animal createAnimal(String id, String species,
                               AnimalType type, double weight, String tag) {
        Animal a = new Animal(id, species, type, weight);
        a.setTag(tag);
        a.setHealthStatus(HealthStatus.HEALTHY);
        animals.add(a);
        saveData();
        return a;
    }

    public void updateAnimal(Animal a, String species, double weight,
                             HealthStatus status) {
        a.setSpecies(species);
        a.setWeight(weight);
        a.setHealthStatus(status);
        saveData();
    }

    public void deleteAnimal(Animal a) {
        animals.remove(a);
        saveData();
    }

    /** Adds an already-constructed Animal directly (used by ZonesView). */
    public void addAnimal(Animal a) {
        animals.add(a);
        saveData();
    }

    /** Find an animal by its unique number (ID). */
    public Animal getAnimalById(String id) {
        return animals.stream()
                .filter(a -> a.getUniqueNumber().equalsIgnoreCase(id))
                .findFirst().orElse(null);
    }

    /**
     * Returns a sorted list of distinct zone IDs currently assigned
     * to at least one animal. Populates the ComboBox in LivestockView.
     */
    public List<String> getZoneIds() {
        return animals.stream()
                .map(Animal::getZoneId)
                .filter(z -> z != null && !z.isBlank())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Returns the zone ID stored on the animal itself.
     * Used by the filter predicate in LivestockView.
     */
    public String getZoneIdForAnimal(Animal a) {
        return a.getZoneId();
    }

    /** Log a health event for an animal identified by id. Returns true when logged. */
    public boolean logHealthEventForAnimal(String id, String eventDescription,
                                           double currentWeight, int currentAge) {
        Animal a = getAnimalById(id);
        if (a == null) return false;
        a.logHealthEvent(eventDescription, currentWeight, currentAge);
        saveData();
        return true;
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    private void loadData() {
        animals.clear();
        List<Animal> loaded = StorageManager.loadAnimals();
        if (loaded.isEmpty()) {
            loadSampleData();
            saveData();
        } else {
            animals.addAll(loaded);
        }
    }

    public void saveData() {
        StorageManager.saveAnimals(animals);
    }

    // ── Sample data ───────────────────────────────────────────────────────────

    private void loadSampleData() {
        Animal a1 = new Animal("AN-8842", "Black Angus", AnimalType.RUMINANT, 542);
        a1.setTag("A-02");
        a1.setZoneId("ZN-221");
        a1.setHealthStatus(HealthStatus.HEALTHY);
        a1.setFeedConversionRatio(6.2);
        a1.setNextVaccineDate(LocalDate.now().plusDays(14));
        a1.logHealthEvent("Annual Checkup | Dr. Miller | Perfect vitals.", 542, 0);
        a1.logHealthEvent("Deworming | Admin-04 | Standard protocol.", 542, 0);

        Animal a2 = new Animal("AN-8910", "Holstein Friesian", AnimalType.RUMINANT, 610);
        a2.setTag("B-591");
        a2.setZoneId("ZN-221");
        a2.setHealthStatus(HealthStatus.HEALTHY);

        Animal a3 = new Animal("AN-9003", "Jersey Cow", AnimalType.RUMINANT, 390);
        a3.setTag("N-254");
        a3.setZoneId("ZN-045");
        a3.setHealthStatus(HealthStatus.SICK);

        String[] ids  = {"AN-9200","AN-9201","AN-9202","AN-9203","AN-9204",
                "AN-9205","AN-9206","AN-9207","AN-9208","AN-9209"};
        String[] tags = {"S-50","S-81","S-42","S-23","S-34","S-95","S-44","S-10","S-59","—"};
        String[] zones= {"ZN-221","ZN-221","ZN-045","ZN-045","ZN-221",
                "ZN-045","ZN-221","ZN-221","ZN-045","ZN-221"};
        for (int i = 0; i < ids.length; i++) {
            Animal a = new Animal(ids[i], "Hereford Prime", AnimalType.RUMINANT, 500 + i * 3);
            a.setTag(tags[i]);
            a.setZoneId(zones[i]);
            a.setHealthStatus(HealthStatus.HEALTHY);
            animals.add(a);
        }

        animals.add(0, a1);
        animals.add(1, a2);
        animals.add(2, a3);
    }
}