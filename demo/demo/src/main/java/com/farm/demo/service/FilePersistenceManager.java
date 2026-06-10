
package com.farm.demo.service;

import com.farm.demo.model.*;
        import java.io.*;
        import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FilePersistenceManager {

    private static final String ZONE_FILE = "zones.txt";
    private static final String ANIMAL_FILE = "animals.txt";
    private static final String CROP_FILE = "crops.txt";
    private static final String SENSOR_FILE = "sensors.txt";
    private static final String PRODUCTION_FILE = "production.txt";
    private static final String FEEDING_FILE = "feeding.txt";

    public static void saveData(Farm farm) {
        saveZones(farm.getZones());
        saveAnimals(farm.getZones());
        saveCrops(farm.getZones());
        saveSensors(farm.getZones());
        saveProduction(farm.getZones());
        saveFeeding(farm.getZones());
    }

    public static void loadData(Farm farm) {
        // IMPORTANT: Load zones first so other items can find their "home"
        loadZones(farm);
        loadAnimals(farm);
        loadCrops(farm);
        loadSensors(farm);
        loadProduction(farm);
        loadFeeding(farm);
    }

    // --- SENSORS ---
    private static void saveSensors(List<Zone> zones) {
        try (PrintWriter out = new PrintWriter(new FileWriter(SENSOR_FILE))) {
            for (Zone z : zones) {
                for (Sensor s : z.getSensors()) {
                    // Format: ZONE|TYPE|UID|STATUS|MIN|MAX|UNIT
                    out.println(z.getCode() + "|" + s.getClass().getSimpleName() + "|" + s.getUniqueCode() + "|" +
                            s.getStatus() + "|" + s.getMinThreshold() + "|" + s.getMaxThreshold() + "|" + s.getUnitOfMeasurement());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void loadSensors(Farm farm) {
        File file = new File(SENSOR_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                farm.getZoneByCode(p[0]).ifPresent(z -> {
                    try {
                        Sensor s;
                        // Use Biometric as default if type is generic
                        s = new BiometricSensor(p[2], z.getCode(), Double.parseDouble(p[4]), Double.parseDouble(p[5]), p[6]);
                        s.changeStatus(SensorStatus.valueOf(p[3]));
                        z.addSensor(s);
                    } catch (Exception e) { e.printStackTrace(); }
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- PRODUCTION ---
    private static void saveProduction(List<Zone> zones) {
        try (PrintWriter out = new PrintWriter(new FileWriter(PRODUCTION_FILE))) {
            for (Zone z : zones) {
                ProductionRecord pr = z.getProductionRecord();
                if (pr != null) {
                    // Format: ZONE|TYPE|VALUE|UNIT|DATE
                    out.println(z.getCode() + "|" + pr.getProductionType() + "|" + pr.getValue() + "|" + pr.getUnit() + "|" + pr.getDate());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void loadProduction(Farm farm) {
        File file = new File(PRODUCTION_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                farm.getZoneByCode(p[0]).ifPresent(z -> {
                    ProductionRecord pr = new ProductionRecord(p[0], ProductionType.valueOf(p[1]), Double.parseDouble(p[2]), p[3], LocalDate.parse(p[4]));
                    z.setProductionRecord(pr);
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- FEEDING PROGRAMS ---
    private static void saveFeeding(List<Zone> zones) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FEEDING_FILE))) {
            for (Zone z : zones) {
                FeedingProgram fp = null;
                if (z instanceof LivestockZone lz) fp = lz.getFeedingProgram();
                else if (z instanceof AquacultureZone az) fp = az.getFeedingProgram();

                if (fp != null) {
                    // Format: ZONE|FEED_TYPE|QTY|FREQ
                    out.println(z.getCode() + "|" + fp.getFeedType() + "|" + fp.getQuantityPerMeal() + "|" + fp.getNbMealsPerDay());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void loadFeeding(Farm farm) {
        File file = new File(FEEDING_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                farm.getZoneByCode(p[0]).ifPresent(z -> {
                    FeedingProgram fp = new FeedingProgram(p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]));
                    if (z instanceof LivestockZone lz) lz.setFeedingProgram(fp);
                    else if (z instanceof AquacultureZone az) az.setFeedingProgram(fp);
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void saveZones(List<Zone> zones) {
        try (PrintWriter out = new PrintWriter(new FileWriter(ZONE_FILE))) {
            for (Zone z : zones) {
                // Format: TYPE|CODE|NAME|STATUS
                String type = z.getClass().getSimpleName();
                out.println(type + "|" + z.getCode() + "|" + z.getName() + "|" + z.getStatus());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void saveAnimals(List<Zone> zones) {
        try (PrintWriter out = new PrintWriter(new FileWriter(ANIMAL_FILE))) {
            for (Zone z : zones) {
                List<Animal> animals = new ArrayList<>();
                if (z instanceof LivestockZone lz) animals = lz.getAnimals();
                else if (z instanceof AquacultureZone az) animals = az.getAnimals();

                for (Animal a : animals) {
                    // Format: ZONE_CODE|UID|SPECIES|TYPE|WEIGHT|STATUS
                    out.println(z.getCode() + "|" + a.getUniqueNumber() + "|" + a.getSpecies() + "|" +
                            a.getType() + "|" + a.getWeight() + "|" + a.getHealth());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void saveCrops(List<Zone> zones) {
        try (PrintWriter out = new PrintWriter(new FileWriter(CROP_FILE))) {
            for (Zone z : zones) {
                if (z instanceof CropZone cz) {
                    for (Crop c : cz.getCrops()) {
                        // Format: ZONE_CODE|SPECIES|TYPE|STAGE|HARVEST_DATE
                        out.println(z.getCode() + "|" + c.getSpecies() + "|" + c.getType() + "|" +
                                c.getCurrentStage() + "|" + c.getExpectedHarvestDate() + "|" + c.getZoneId());
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }




    private static void loadZones(Farm farm) {
        File file = new File(ZONE_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                Zone z;
                if (p[0].equals("LivestockZone")) z = new LivestockZone(p[1], p[2]);
                else if (p[0].equals("AquacultureZone")) z = new AquacultureZone(p[1], p[2]);
                else z = new CropZone(p[1], p[2]);

                z.setStatus(ZoneStatus.valueOf(p[3]));
                farm.addZone(z);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadAnimals(Farm farm) {
        File file = new File(ANIMAL_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                Animal a = new Animal(AnimalType.valueOf(p[3]), p[1], p[2], 0, Double.parseDouble(p[4]), HealthStatus.valueOf(p[5]));
                a.setZoneId(p[0]);
                farm.getZoneByCode(p[0]).ifPresent(z -> z.addEntity(a));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadCrops(Farm farm) {
        File file = new File(CROP_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                Crop c = new Crop(CropType.valueOf(p[2]), p[1], 6.0, 7.5, GrowthStage.valueOf(p[3]), 20.0, 80.0, LocalDate.parse(p[4]));
                c.setZoneId(p[5]);
                farm.getZoneByCode(p[0]).ifPresent(z -> z.addEntity(c));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}