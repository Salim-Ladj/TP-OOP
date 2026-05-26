package com.farm.demo.model;

import java.util.ArrayList;

public class CropZone extends Zone {
    // Attributes
    private ArrayList<Crop> crops;

    // Constructor
    public CropZone(String code, String name) {
        super(code, name);
        this.crops = new ArrayList<Crop>();
    }

    // Methods
    @Override
    public void addEntity(Object entity) {
        if (this.getStatus() == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.getCode() + " is currently SUSPENDED.");
        }

        // Enforcing Type Rule
        if (!(entity instanceof Crop)) {
            throw new IllegalArgumentException("Type Error: CropZone cannot accept " + entity.getClass().getSimpleName());
        }
        this.crops.add((Crop) entity);
    }

    @Override
    public void display() {
        System.out.println("Crop Zone: " + name);
        System.out.println("Code: " + code);
        System.out.println("Status: " + status);
        System.out.println("Number of Crops: " + crops.size());
    }

    public void generateCropStatusReport() {
        System.out.println("Crop Status Report for Zone: " + name);
        for (Crop crop : crops) {
            crop.displayInfo();
            System.out.println("-------------------------");
        }
    }
    //getters and setters
    public ArrayList<Crop> getCrops() {
        return crops;
    }

    @Override
    public void setProductionRecord(ProductionRecord record) {
        if (this.getStatus() == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.getCode() + " is currently SUSPENDED.");
        }
        if(record.getType() != ProductionType.CROP_YIELD) {
            throw new IllegalArgumentException("Type Error: ProductionRecord of type " + record.getType() + " cannot be set for CropZone.");
        }
        this.productionRecord = record;
    }
}