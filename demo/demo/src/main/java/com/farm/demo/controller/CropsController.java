package com.farm.demo.controller;

import com.farm.demo.model.*;
import com.farm.demo.storage.StorageManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller to manage crops with file-based persistence.
 */
public class CropsController {

    private final List<Crop> crops = new ArrayList<>();

    public CropsController() {
        loadData();
    }

    public List<Crop> getAllCrops() { return crops; }

    public Crop createCrop(CropType type, String species, double minPH, double maxPH,
                           GrowthStage initialStage, double minMoisture, double maxMoisture, LocalDate expectedHarvestDate) {
        Crop c = new Crop(type, species, minPH, maxPH, initialStage, minMoisture, maxMoisture, expectedHarvestDate);
        crops.add(c);
        saveData();
        return c;
    }

    public Crop findBySpecies(String species) {
        return crops.stream()
                .filter(c -> c.getSpecies().equalsIgnoreCase(species))
                .findFirst().orElse(null);
    }

    /** Adds an already-constructed Crop directly (used by ZonesView). */
    public void addCrop(Crop c) {
        crops.add(c);
        saveData();
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    private void loadData() {
        crops.clear();
        List<Crop> loaded = StorageManager.loadCrops();
        if (loaded.isEmpty()) {
            loadSampleData();
            saveData();
        } else {
            crops.addAll(loaded);
        }
    }

    public void saveData() {
        StorageManager.saveCrops(crops);
    }

    // ── Sample data ───────────────────────────────────────────────────────────

    private void loadSampleData() {
        // Minimal sample crop
        crops.add(new Crop(CropType.VEGETABLE, "Tomato", 5.5, 7.0, GrowthStage.SOWING, 40.0, 80.0, LocalDate.now().plusMonths(3)));
        crops.add(new Crop(CropType.CEREAL, "Corn", 5.5, 7.5, GrowthStage.GROWTH, 30.0, 70.0, LocalDate.now().plusMonths(4)));
    }
}