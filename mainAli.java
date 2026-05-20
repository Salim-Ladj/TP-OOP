import java.time.LocalDateTime;
import java.util.List;
 
public class mainAli {
 
    public static void main(String[] args) { 
        Farm farm = new Farm("FARM-001", "ESI Green Valley");
        System.out.println("Farm created: " + farm.getName() + " (ID=" + farm.getFarmId() + ")\n");
 
        CropZone cropZone = new CropZone("CZ-01", "North Crop Field");
        LivestockZone livestockZone = new LivestockZone("LZ-01", "East Livestock Area");
        AquacultureZone aquaZone = new AquacultureZone("AZ-01", "South Aquaculture Tank");
 
        System.out.println(farm.addZone(cropZone));
        System.out.println(farm.addZone(livestockZone));
        System.out.println(farm.addZone(aquaZone));
        System.out.println(farm.addZone(new CropZone("CZ-01", "Duplicate Zone")) + "\n");
 
        System.out.println("--- Zones Overview ---");
        System.out.println(farm.displayZonesOverview());
 
        TemperatureSensor tempSensor = new TemperatureSensor("TEMP-CZ01", "CZ-01", 10.0, 35.0, "°C");
        HumiditySensor humiditySensor = new HumiditySensor("HUMI-CZ01", "CZ-01", 30.0, 80.0, "%");
        SoilSensor soilSensor = new SoilSensor("SOIL-CZ01", "CZ-01", 5.5, 7.5, "pH");
        BiometricSensor bioSensor = new BiometricSensor("BIO-LZ01", "LZ-01", 36.0, 39.5, "°C");
        GPSCollarSensor gpsSensor = new GPSCollarSensor("GPS-LZ01", "LZ-01", 33.0, 35.0, 2.0, 4.0, "°");
        WaterTemperatureSensor waterTempSensor = new WaterTemperatureSensor("WTEMP-AZ01", "AZ-01", 18.0, 28.0, "°C");
        DissolvedOxygenSensor dissolvedOxygenSensor = new DissolvedOxygenSensor("DOX-AZ01", "AZ-01", 6.0, 12.0, "mg/L");
 
        cropZone.addSensor(tempSensor);
        cropZone.addSensor(humiditySensor);
        cropZone.addSensor(soilSensor);
        livestockZone.addSensor(bioSensor);
        livestockZone.addSensor(gpsSensor);
        aquaZone.addSensor(waterTempSensor);
        aquaZone.addSensor(dissolvedOxygenSensor);
 
        System.out.println("Sensors assigned to zones.\n");
 
        System.out.println("--- Lecture normale (pas d'alerte attendue) ---");
        System.out.println(tempSensor.addReading(22.0));
        System.out.println(humiditySensor.addReading(55.0));
        System.out.println(soilSensor.addReading(6.5));
        System.out.println(bioSensor.addReading(38.0));
        System.out.println(waterTempSensor.addReading(23.0));
        System.out.println(dissolvedOxygenSensor.addReading(8.0));
        System.out.println(gpsSensor.addGPSReading(34.0, 3.0));
 
        System.out.println("\n--- Lecture en zone d'avertissement (warning) ---");
        System.out.println(tempSensor.addReading(38.0));
        System.out.println(humiditySensor.addReading(25.0));
        System.out.println(soilSensor.addReading(8.0));
 
        System.out.println("\n--- Lecture critique (critical) ---");
        System.out.println(bioSensor.addReading(50.0));
        System.out.println(waterTempSensor.addReading(5.0));
        System.out.println(dissolvedOxygenSensor.addReading(0.5));
        System.out.println(gpsSensor.addGPSReading(10.0, 20.0));
 
        System.out.println("\n--- Alertes actives triées par gravité ---");
        List<Alert> activeAlerts = farm.getActiveAlerts();
        if (activeAlerts.isEmpty()) {
            System.out.println("Aucune alerte active.");
        } else {
            activeAlerts.forEach(System.out::println);
        }
 
        if (!activeAlerts.isEmpty()) {
            System.out.println("\n--- Reconnaissance et suppression d'une alerte ---");
            Alert firstAlert = activeAlerts.get(0);
            System.out.println(farm.acknowledgeAlert(firstAlert.getAlertId()));
            System.out.println(farm.dismissAlert(firstAlert.getAlertId()));
        }
 
        System.out.println("\n--- Test de changement de statut de capteur ---");
        System.out.println("Suspension de " + tempSensor.getUniqueCode() + "...");
        tempSensor.suspend();
        System.out.println(tempSensor.addReading(12.0));
        System.out.println("Réactivation de " + tempSensor.getUniqueCode() + "...");
        tempSensor.reactivate();
        System.out.println(tempSensor.addReading(15.0));
 
        System.out.println("\n--- Historique des alertes critiques ---");
        List<Alert> criticalAlerts = farm.getAlertHistory(null, null, SeverityLevel.CRITICAL, null, LocalDateTime.now());
        System.out.println("Total critical alerts: " + criticalAlerts.size());
        criticalAlerts.forEach(System.out::println);
 
        System.out.println("\n========================================");
        System.out.println("   TEST FONCTIONNEL COMPLET");
        System.out.println("========================================");
    }
}
