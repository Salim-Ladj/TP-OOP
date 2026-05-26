package com.farm.demo.controller;

import com.farm.demo.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Zones Overview screen.
 * Bridges the view with your existing model classes.
 * Swap the sample data in loadSampleData() for real persistence later.
 */
public class ZonesController {

    // In-memory store (replace with DB/file persistence if needed)
    private final List<Zone> zones = new ArrayList<>();

    public ZonesController() {
        loadSampleData();
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
        return z;
    }

    public void updateZone(Zone z, String name, String code,
                           String location, ZoneStatus status) {
        z.setName(name);
        z.setCode(code);
        z.setLocation(location);
        z.setStatus(status);
    }

    public void deleteZone(Zone z) {
        zones.remove(z);
    }

    /** Toggles between ACTIVE and SUSPENDED (cascades to sensors). */
    public void toggleStatus(Zone z) {
        if (z.getStatus() == ZoneStatus.ACTIVE) {
            z.suspend();
        } else {
            z.reactivate();
        }
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
        z2.addSensor(new DissolvedOxygen("S-003"));

        LivestockZone z3 = new LivestockZone("ZN-221", "Hillside Pasture");
        z3.setStatus(ZoneStatus.SUSPENDED);
        z3.setLocation("Sector C3");
        z3.addSensor(new BiometricSensor("S-004"));

        CropZone z4 = new CropZone("ZN-108", "South Greenhouse");
        z4.setStatus(ZoneStatus.ACTIVE);
        z4.setLocation("Sector D1");

        zones.addAll(List.of(z1, z2, z3, z4));
    }
}
