package com.farm.demo.controller;

import com.farm.demo.model.*;
import com.farm.demo.storage.StorageManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductionController {

    private final List<ProductionRecord> records = new ArrayList<>();

    public ProductionController() {
        loadData();
    }

    public List<ProductionRecord> getAllRecords() { return records; }

    public ProductionRecord createRecord(String zoneCode, ProductionType type,
                                         double value, String unit) {
        ProductionRecord r = new ProductionRecord(zoneCode, type, value, unit, LocalDate.now());
        records.add(r);
        saveData();
        return r;
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    private void loadData() {
        records.clear();
        List<ProductionRecord> loaded = StorageManager.loadProductionRecords();
        if (loaded.isEmpty()) {
            loadSampleData();
            saveData();
        } else {
            records.addAll(loaded);
        }
    }

    public void saveData() {
        StorageManager.saveProductionRecords(records);
    }

    // ── Sample data ───────────────────────────────────────────────────────────

    private void loadSampleData() {
        records.add(new ProductionRecord("ZN-102", ProductionType.CROP_YIELD, 8400, "kg",   LocalDate.of(2024,3,1)));
        records.add(new ProductionRecord("ZN-045", ProductionType.AQUACULTURE, 2200, "kg",   LocalDate.of(2024,3,1)));
        records.add(new ProductionRecord("ZN-221", ProductionType.MILK,   15600, "L",    LocalDate.of(2024,3,1)));
        records.add(new ProductionRecord("ZN-108", ProductionType.CROP_YIELD, 6200, "kg",   LocalDate.of(2024,3,1)));
        records.add(new ProductionRecord("ZN-102", ProductionType.CROP_YIELD, 9100, "kg",   LocalDate.of(2024,4,1)));
        records.add(new ProductionRecord("ZN-221", ProductionType.EGGS,    4800, "units",LocalDate.of(2024,4,1)));
        records.add(new ProductionRecord("ZN-045", ProductionType.AQUACULTURE, 2500, "kg",   LocalDate.of(2024,4,1)));
        records.add(new ProductionRecord("ZN-108", ProductionType.CROP_YIELD, 7000, "kg",   LocalDate.of(2024,4,1)));
    }
}
