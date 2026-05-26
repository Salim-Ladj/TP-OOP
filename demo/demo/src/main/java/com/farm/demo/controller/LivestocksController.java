package com.farm.demo.controller;

import com.farm.demo.model.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LivestocksController {

    private final List<Animal> animals = new ArrayList<>();

    public LivestocksController() {
        loadSampleData();
    }

    public List<Animal> getAllAnimals() { return animals; }

    public Animal createAnimal(String id, String species,
                               AnimalType type, double weight, String tag) {
        Animal a = new Animal(id, species, type, weight);
        a.setTag(tag);
        a.setHealthStatus(HealthStatus.HEALTHY);
        animals.add(a);
        return a;
    }

    public void updateAnimal(Animal a, String species, double weight,
                             HealthStatus status) {
        a.setSpecies(species);
        a.setWeight(weight);
        a.setHealthStatus(status);
    }

    public void deleteAnimal(Animal a) { animals.remove(a); }

    // ── Sample data ───────────────────────────────────────────────────────────

    private void loadSampleData() {
        // AN-8842
        Animal a1 = new Animal("AN-8842", "Black Angus", AnimalType.RUMINANT, 542);
        a1.setTag("A-02");
        a1.setHealthStatus(HealthStatus.HEALTHY);
        a1.setFeedConversionRatio(6.2);
        a1.setNextVaccineDate(LocalDate.now().plusDays(14));
        a1.logHealthEvent("Annual Checkup | Dr. Miller | Perfect vitals.", 542, 0);
        a1.logHealthEvent("Deworming | Admin-04 | Standard protocol.", 542, 0);

        // AN-8910
        Animal a2 = new Animal("AN-8910", "Holstein Friesian", AnimalType.RUMINANT, 610);
        a2.setTag("B-591");
        a2.setHealthStatus(HealthStatus.HEALTHY);

        // AN-9003
        Animal a3 = new Animal("AN-9003", "Jersey Cow", AnimalType.RUMINANT, 390);
        a3.setTag("N-254");
        a3.setHealthStatus(HealthStatus.SICK);

        // Fill the rest as Hereford Prime
        String[] ids = {"AN-9200","AN-9201","AN-9202","AN-9203","AN-9204",
                "AN-9205","AN-9206","AN-9207","AN-9208","AN-9209"};
        String[] tags = {"S-50","S-81","S-42","S-23","S-34","S-95","S-44","S-10","S-59","—"};
        for (int i = 0; i < ids.length; i++) {
            Animal a = new Animal(ids[i], "Hereford Prime", AnimalType.RUMINANT, 500 + i * 3);
            a.setTag(tags[i]);
            a.setHealthStatus(HealthStatus.HEALTHY);
            animals.add(a);
        }

        animals.add(0, a1);
        animals.add(1, a2);
        animals.add(2, a3);
    }
}
