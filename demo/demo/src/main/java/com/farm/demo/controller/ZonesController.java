package com.farm.demo.controller;

import com.farm.demo.model.*;
import com.farm.demo.storage.StorageManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Zones Overview screen.
 * Bridges the view with your existing model classes.
 * Integrates with StorageManager for file-based persistence.
 */
public class ZonesController {

    // In-memory store (loaded from/saved to file storage)
    private final List<Zone> zones = new ArrayList<>();

    public ZonesController() {
        loadData();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Zone> getAllZones() {
        return zones;
    }

    public Zone findByCode(String code) {
        return zones.stream()
                .filter(z -> z.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    /**
     * Creates a new zone of the correct subtype.
     * Returns null if the code already exists.
     */
    public Zone createZone(String name, String code, String type,
                           String location, ZoneStatus status) {
        if (findByCode(code) != null) return null;

        Zone z = switch (type) {
            case "Livestock"    -> new LivestockZone(code, name);
            case "Aquaculture"  -> new AquacultureZone(code, name);
            default             -> new CropZone(code, name);
        };
        z.setLocation(location);
        z.setStatus(status);
        zones.add(z);
        saveData();
        return z;
    }

    public void updateZone(Zone z, String name, String code,
                           String location, ZoneStatus status) {
        z.setName(name);
        z.setCode(code);
        z.setLocation(location);
        z.setStatus(status);
        saveData();
    }

    public void deleteZone(Zone z) {
        zones.remove(z);
        saveData();
    }

    /** Toggles between ACTIVE and SUSPENDED (cascades to sensors). */
    public void toggleStatus(Zone z) {
        if (z.getStatus() == ZoneStatus.ACTIVE) {
            z.suspend();
        } else {
            z.reactivate();
        }
        saveData();
    }

    // ── Zone assignment helpers ─────────────────────────────────────────────────

    /** Assigns an Animal to a zone (returns true on success). */
    public boolean assignAnimalToZone(Zone z, Animal a) {
        if (z == null || a == null) return false;
        try {
            z.addEntity(a);
            saveData();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Assigns a Crop to a zone (returns true on success). */
    public boolean assignCropToZone(Zone z, Crop c) {
        if (z == null || c == null) return false;
        try {
            z.addEntity(c);
            saveData();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the number of hosted entities for a zone (crops or animals). */
    public int getHostedEntityCount(Zone z) {
        if (z == null) return 0;
        if (z instanceof CropZone) return ((CropZone) z).getCrops().size();
        if (z instanceof LivestockZone) return ((LivestockZone) z).getAnimals().size();
        return 0;
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    private void loadData() {
        zones.clear();
        List<Zone> loaded = StorageManager.loadZones();
        if (loaded.isEmpty()) {
            loadSampleData();
            saveData();
        } else {
            zones.addAll(loaded);
        }
    }

    public void saveData() {
        StorageManager.saveZones(zones);
    }

    // ── Sample data ───────────────────────────────────────────────────────────

    private void loadSampleData() {
        CropZone z1 = new CropZone("ZN-102", "North Field Corn");
        z1.setStatus(ZoneStatus.ACTIVE);
        z1.setLocation("Sector A1");
        z1.addSensor(new HumiditySensor("S-001"));
        z1.addSensor(new RainfallSensor("S-002"));

        AquacultureZone z2 = new AquacultureZone("ZN-045", "East Basin Aquaculture");
        z2.setStatus(ZoneStatus.ACTIVE);
        z2.setLocation("Sector B2");
        z2.addSensor(new DissolvedOxygenSensor("S-003"));

        LivestockZone z3 = new LivestockZone("ZN-221", "Hillside Pasture");
        z3.setStatus(ZoneStatus.ACTIVE);
        z3.setLocation("Sector C3");
        z3.addSensor(new BiometricSensor("S-004"));
        z3.suspend();

        CropZone z4 = new CropZone("ZN-108", "South Greenhouse");
        z4.setStatus(ZoneStatus.ACTIVE);
        z4.setLocation("Sector D1");

        zones.addAll(List.of(z1, z2, z3, z4));
    }
}
