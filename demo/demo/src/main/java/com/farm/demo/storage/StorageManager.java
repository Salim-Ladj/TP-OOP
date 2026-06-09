package com.farm.demo.storage;

import com.farm.demo.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Manages persistence of farm data to text files.
 * Uses simple text format with pipe-delimited values for easy parsing.
 */
public class StorageManager {
    private static final String DATA_DIR = "farm_data";
    private static final String ZONES_FILE = "zones.txt";
    private static final String ANIMALS_FILE = "animals.txt";
    private static final String HEALTH_EVENTS_FILE = "health_events.txt";
    private static final String CROPS_FILE = "crops.txt";
    private static final String PRODUCTION_FILE = "production.txt";

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
    }

    // ── ZONES ───────────────────────────────────────────────────────────────

    public static void saveZones(List<Zone> zones) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(getPath(ZONES_FILE)))) {
            for (Zone z : zones) {
                String type = z instanceof CropZone ? "CROP" : z instanceof LivestockZone ? "LIVESTOCK" : "AQUACULTURE";
                writer.printf("%s|%s|%s|%s|%s%n",
                        z.getCode(),
                        z.getName(),
                        z.getLocation(),
                        z.getStatus().name(),
                        type
                );
            }
        } catch (IOException e) {
            System.err.println("Error saving zones: " + e.getMessage());
        }
    }

    public static List<Zone> loadZones() {
        List<Zone> zones = new ArrayList<>();
        File file = new File(getPath(ZONES_FILE));
        if (!file.exists()) return zones;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;

                String code = parts[0];
                String name = parts[1];
                String location = parts[2];
                ZoneStatus status = ZoneStatus.valueOf(parts[3]);
                String type = parts[4];

                Zone zone;
                if ("CROP".equals(type)) {
                    zone = new CropZone(code, name);
                } else if ("LIVESTOCK".equals(type)) {
                    zone = new LivestockZone(code, name);
                } else {
                    zone = new AquacultureZone(code, name);
                }
                zone.setLocation(location);
                zone.setStatus(status);
                zones.add(zone);
            }
        } catch (IOException e) {
            System.err.println("Error loading zones: " + e.getMessage());
        }
        return zones;
    }

    // ── ANIMALS ─────────────────────────────────────────────────────────────

    public static void saveAnimals(List<Animal> animals) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(getPath(ANIMALS_FILE)))) {
            for (Animal a : animals) {
                writer.printf("%s|%s|%s|%d|%.1f|%s%n",
                        a.getUniqueNumber(),
                        a.getSpecies(),
                        a.getType().name(),
                        a.getAge(),
                        a.getWeight(),
                        a.getHealth().name()
                );
            }
        } catch (IOException e) {
            System.err.println("Error saving animals: " + e.getMessage());
        }
        saveHealthEvents(animals);
    }

    public static List<Animal> loadAnimals() {
        List<Animal> animals = new ArrayList<>();
        File file = new File(getPath(ANIMALS_FILE));
        if (!file.exists()) return animals;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 6) continue;

                String uniqueNumber = parts[0];
                String species = parts[1];
                AnimalType type = AnimalType.valueOf(parts[2]);
                int age = Integer.parseInt(parts[3]);
                double weight = Double.parseDouble(parts[4]);
                HealthStatus health = HealthStatus.valueOf(parts[5]);

                Animal animal = new Animal(type, uniqueNumber, species, age, weight, health);
                animals.add(animal);
            }
        } catch (IOException e) {
            System.err.println("Error loading animals: " + e.getMessage());
        }
        loadHealthEvents(animals);
        return animals;
    }

    private static void saveHealthEvents(List<Animal> animals) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(getPath(HEALTH_EVENTS_FILE)))) {
            for (Animal animal : animals) {
                Map<HealthStatus, List<String>> history = animal.getHealthHistory();
                if (history == null) continue;

                for (Map.Entry<HealthStatus, List<String>> entry : history.entrySet()) {
                    List<String> events = entry.getValue();
                    if (events == null) continue;

                    for (String event : events) {
                        writer.printf("%s|%s|%s%n",
                                animal.getUniqueNumber(),
                                entry.getKey().name(),
                                encode(event)
                        );
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving health events: " + e.getMessage());
        }
    }

    private static void loadHealthEvents(List<Animal> animals) {
        File file = new File(getPath(HEALTH_EVENTS_FILE));
        if (!file.exists()) return;

        Map<String, Animal> animalsById = new HashMap<>();
        for (Animal animal : animals) {
            animalsById.put(animal.getUniqueNumber(), animal);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", 3);
                if (parts.length < 3) continue;

                Animal animal = animalsById.get(parts[0]);
                if (animal == null) continue;

                HealthStatus status = HealthStatus.valueOf(parts[1]);
                animal.getHealthHistory()
                        .computeIfAbsent(status, ignored -> new ArrayList<>())
                        .add(decode(parts[2]));
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading health events: " + e.getMessage());
        }
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    // ── CROPS ───────────────────────────────────────────────────────────────

    public static void saveCrops(List<Crop> crops) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(getPath(CROPS_FILE)))) {
            for (Crop c : crops) {
                writer.printf("%s|%s|%s|%s|%s|%.1f|%.1f|%.1f|%.1f|%s%n",
                        c.getType().name(),
                        c.getSpecies(),
                        c.getPlantingDate(),
                        c.getExpectedHarvestDate(),
                        c.getCurrentStage().name(),
                        c.getMinPH(),
                        c.getMaxPH(),
                        c.getMinMoisture(),
                        c.getMaxMoisture(),
                        c.getId()
                );
            }
        } catch (IOException e) {
            System.err.println("Error saving crops: " + e.getMessage());
        }
    }

    public static List<Crop> loadCrops() {
        List<Crop> crops = new ArrayList<>();
        File file = new File(getPath(CROPS_FILE));
        if (!file.exists()) return crops;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 10) continue;

                CropType type = CropType.valueOf(parts[0]);
                String species = parts[1];
                LocalDate plantingDate = LocalDate.parse(parts[2]);
                LocalDate harvestDate = LocalDate.parse(parts[3]);
                GrowthStage stage = GrowthStage.valueOf(parts[4]);
                double minPH = Double.parseDouble(parts[5]);
                double maxPH = Double.parseDouble(parts[6]);
                double minMoisture = Double.parseDouble(parts[7]);
                double maxMoisture = Double.parseDouble(parts[8]);

                Crop crop = new Crop(type, species, minPH, maxPH, stage, minMoisture, maxMoisture, harvestDate);
                crops.add(crop);
            }
        } catch (IOException e) {
            System.err.println("Error loading crops: " + e.getMessage());
        }
        return crops;
    }

    // ── PRODUCTION RECORDS ──────────────────────────────────────────────────

    public static void saveProductionRecords(List<ProductionRecord> records) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(getPath(PRODUCTION_FILE)))) {
            for (ProductionRecord r : records) {
                writer.printf("%s|%s|%.1f|%s|%s%n",
                        r.getZoneCode(),
                        r.getProductionType().name(),
                        r.getValue(),
                        r.getUnit(),
                        r.getDate()
                );
            }
        } catch (IOException e) {
            System.err.println("Error saving production records: " + e.getMessage());
        }
    }

    public static List<ProductionRecord> loadProductionRecords() {
        List<ProductionRecord> records = new ArrayList<>();
        File file = new File(getPath(PRODUCTION_FILE));
        if (!file.exists()) return records;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;

                String zoneCode = parts[0];
                ProductionType type = ProductionType.valueOf(parts[1]);
                double value = Double.parseDouble(parts[2]);
                String unit = parts[3];
                LocalDate date = LocalDate.parse(parts[4]);

                ProductionRecord record = new ProductionRecord(zoneCode, type, value, unit, date);
                records.add(record);
            }
        } catch (IOException e) {
            System.err.println("Error loading production records: " + e.getMessage());
        }
        return records;
    }

    // ── UTILITY ─────────────────────────────────────────────────────────────

    private static String getPath(String filename) {
        return Paths.get(DATA_DIR, filename).toString();
    }

    public static void deleteAllData() {
        try {
            Files.deleteIfExists(Paths.get(getPath(ZONES_FILE)));
            Files.deleteIfExists(Paths.get(getPath(ANIMALS_FILE)));
            Files.deleteIfExists(Paths.get(getPath(HEALTH_EVENTS_FILE)));
            Files.deleteIfExists(Paths.get(getPath(CROPS_FILE)));
            Files.deleteIfExists(Paths.get(getPath(PRODUCTION_FILE)));
        } catch (IOException e) {
            System.err.println("Error deleting data files: " + e.getMessage());
        }
    }
}
