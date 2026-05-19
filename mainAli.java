import java.time.LocalDateTime;
import java.util.List;
 
public class mainAli {
 
    public static void main(String[] args) {
 
        System.out.println("========================================");
        System.out.println("   ESI SMART FARM - SYSTEM TEST");
        System.out.println("========================================\n");
 
        // ============================================================
        // 1. FARM SETUP
        // ============================================================
        System.out.println("--- [1] FARM SETUP ---");
 
        Farm farm = new Farm("FARM-001", "ESI Green Valley");
        System.out.println("Farm created: " + farm.getName() + " (ID: " + farm.getFarmId() + ")");
 
        // Create zones
        CropZone cropZone         = new CropZone("CZ-01", "North Crop Field");
        LivestockZone livestockZone = new LivestockZone("LZ-01", "East Livestock Area");
        AquacultureZone aquaZone  = new AquacultureZone("AZ-01", "South Aquaculture Tank");
 
        farm.addZone(cropZone);
        farm.addZone(livestockZone);
        farm.addZone(aquaZone);
 
        // Test duplicate zone code
        farm.addZone(new CropZone("CZ-01", "Duplicate Zone")); // Should print warning
 
        System.out.println();
 
        // ============================================================
        // 2. ZONES OVERVIEW
        // ============================================================
        System.out.println("--- [2] ZONES OVERVIEW ---");
        farm.displayZonesOverview();
        System.out.println();
 
        // ============================================================
        // 3. SENSORS SETUP
        // ============================================================
        System.out.println("--- [3] SENSOR SETUP ---");
 
        // Crop Zone Sensors
        TemperatureSensor tempSensor   = new TemperatureSensor("TEMP-CZ01", "CZ-01", 10.0, 35.0, "°C");
        HumiditySensor humiditySensor  = new HumiditySensor("HUMI-CZ01", "CZ-01", 30.0, 80.0, "%");
        SoilSensor soilSensor          = new SoilSensor("SOIL-CZ01", "CZ-01", 5.5, 7.5, "pH"  );
 
        // Livestock Zone Sensor
        BiometricSensor bioSensor      = new BiometricSensor("BIO-LZ01", "LZ-01", 36.0, 39.5, "°C");
        GPSCollarSensor gpsSensor      = new GPSCollarSensor("GPS-LZ01", "LZ-01",
                                            35.0, 33.0,   // lat thresholds (max, min)
                                            3.0,           // initial longitude
                                            4.0, 2.0, "°");     // long thresholds (max, min)
 
        // Aquaculture Zone Sensors
        WaterTemperatureSensor waterTempSensor     = new WaterTemperatureSensor("WTEMP-AZ01", "AZ-01", 18.0, 28.0, "°C");
        DissolvedOxygenSensor dissolvedOxygenSensor = new DissolvedOxygenSensor("DOX-AZ01", "AZ-01", 6.0, 12.0, "mg/L");
 
        // Add sensors to zones
        cropZone.addSensor(tempSensor);
        cropZone.addSensor(humiditySensor);
        cropZone.addSensor(soilSensor);
        livestockZone.addSensor(bioSensor);
        livestockZone.addSensor(gpsSensor);
        aquaZone.addSensor(waterTempSensor);
        aquaZone.addSensor(dissolvedOxygenSensor);
 
        System.out.println("Sensors added to all zones.");
        System.out.println();
 
        // ============================================================
        // 4. NORMAL SENSOR READINGS (no alerts expected)
        // ============================================================
        System.out.println("--- [4] NORMAL READINGS ---");
 
        tempSensor.updateReading(22.0);        // normal: within [10, 35]
        humiditySensor.updateReading(55.0);    // normal: within [30, 80]
        soilSensor.updateReading(6.5);         // normal: within [5.5, 7.5]
        bioSensor.updateReading(38.0);         // normal: within [36, 39.5]
        waterTempSensor.updateReading(23.0);   // normal: within [18, 28]
        dissolvedOxygenSensor.updateReading(8.0); // normal: within [6, 12]
        gpsSensor.updateReading(34.0, 3.0);    // normal: lat within [33,35], lon within [2,4]
 
        System.out.println();
 
        // ============================================================
        // 5. WARNING READINGS (slightly outside threshold)
        // ============================================================
        System.out.println("--- [5] WARNING READINGS ---");
 
        // Warning zone: outside [min,max] but within [0.8*min, 1.2*max]
        tempSensor.updateReading(38.0);        // above max 35 but below 1.2*35=42 → WARNING
        humiditySensor.updateReading(25.0);    // below min 30 but above 0.8*30=24 → WARNING
        soilSensor.updateReading(8.0);         // above max 7.5 but below 1.2*7.5=9 → WARNING
 
        System.out.println();
 
        // ============================================================
        // 6. CRITICAL READINGS (far outside threshold)
        // ============================================================
        System.out.println("--- [6] CRITICAL READINGS ---");
 
        // Critical zone: below 0.8*min or above 1.2*max
        bioSensor.updateReading(50.0);              // above 1.2*39.5=47.4 → CRITICAL
        waterTempSensor.updateReading(5.0);         // below 0.8*18=14.4 → CRITICAL
        dissolvedOxygenSensor.updateReading(0.5);   // below 0.8*6=4.8 → CRITICAL
        gpsSensor.updateReading(10.0, 20.0);        // far outside both thresholds → CRITICAL
 
        System.out.println();
 
        // ============================================================
        // 7. SENSOR STATUS MANAGEMENT
        // ============================================================
        System.out.println("--- [7] SENSOR STATUS MANAGEMENT ---");
 
        System.out.println("Suspending TEMP-CZ01...");
        tempSensor.suspend();
        System.out.println("Status: " + tempSensor.getStatus());
 
        // Reading on suspended sensor should be ignored
        System.out.print("Reading on suspended sensor (should be ignored): ");
        tempSensor.updateReading(999.0); // no alert because sensor is SUSPENDED
 
        System.out.println("Reactivating TEMP-CZ01...");
        tempSensor.reactivate();
        System.out.println("Status: " + tempSensor.getStatus());
 
        System.out.println();
 
        // ============================================================
        // 8. ACTIVE ALERTS PANEL (sorted by severity: CRITICAL first)
        // ============================================================
        System.out.println("--- [8] ACTIVE ALERTS (sorted by severity) ---");
 
        List<Alert> activeAlerts = farm.getActiveAlerts();
        if (activeAlerts.isEmpty()) {
            System.out.println("No active alerts.");
        } else {
            for (Alert alert : activeAlerts) {
                System.out.println("[" + alert.getSeverityLevel() + "] ID:" + alert.getAlertId()
                        + " | Sensor: " + alert.getSensorUniqueCode()
                        + " | Zone: " + alert.getZoneId()
                        + " | Value: " + alert.getReadingValue()
                        + " | " + alert.getMessage());
            }
        }
        System.out.println();
 
        // ============================================================
        // 9. ACKNOWLEDGE & DISMISS ALERTS
        // ============================================================
        System.out.println("--- [9] ACKNOWLEDGE & DISMISS ALERTS ---");
 
        // Acknowledge alert ID 4 (first critical: bioSensor)
        System.out.println("Acknowledging alert ID 4...");
        farm.acknowledgeAlert(4);
 
        // Try to acknowledge already acknowledged alert
        System.out.println("Acknowledging alert ID 4 again (should fail)...");
        farm.acknowledgeAlert(4);
 
        // Dismiss acknowledged alert
        System.out.println("Dismissing alert ID 4 (acknowledged)...");
        farm.dismissAlert(4);
 
        // Try to dismiss a non-acknowledged alert (should fail)
        System.out.println("Dismissing alert ID 5 (not yet acknowledged, should fail)...");
        farm.dismissAlert(5);
 
        System.out.println();
 
        // ============================================================
        // 10. ACTIVE ALERTS AFTER ACKNOWLEDGE
        // ============================================================
        System.out.println("--- [10] ACTIVE ALERTS AFTER ACKNOWLEDGE ---");
 
        activeAlerts = farm.getActiveAlerts();
        System.out.println("Remaining active alerts: " + activeAlerts.size());
        for (Alert alert : activeAlerts) {
            System.out.println("[" + alert.getSeverityLevel() + "] ID:" + alert.getAlertId()
                    + " | Sensor: " + alert.getSensorUniqueCode()
                    + " | Acknowledged: " + alert.isAcknowledged());
        }
        System.out.println();
 
        // ============================================================
        // 11. ALERT HISTORY WITH FILTERS
        // ============================================================
        System.out.println("--- [11] ALERT HISTORY - FILTER BY ZONE 'AZ-01' ---");
 
        List<Alert> aquaHistory = farm.getAlertHistory("AZ-01", null, null, null, null);
        System.out.println("Alerts for zone AZ-01: " + aquaHistory.size());
        for (Alert alert : aquaHistory) {
            System.out.println("  [" + alert.getSeverityLevel() + "] " + alert.getMessage());
        }
 
        System.out.println("\n--- [11b] ALERT HISTORY - FILTER BY SEVERITY 'CRITICAL' ---");
        List<Alert> criticalHistory = farm.getAlertHistory(null, null, SeverityLevel.CRITICAL, null, null);
        System.out.println("Critical alerts total: " + criticalHistory.size());
        for (Alert alert : criticalHistory) {
            System.out.println("  ID:" + alert.getAlertId() + " | " + alert.getSensorUniqueCode()
                    + " | " + alert.getMessage());
        }
 
        System.out.println("\n--- [11c] ALERT HISTORY - FILTER BY SENSOR PARTIAL 'GPS' ---");
        List<Alert> gpsHistory = farm.getAlertHistory(null, "GPS", null, null, null);
        System.out.println("GPS alerts: " + gpsHistory.size());
        for (Alert alert : gpsHistory) {
            System.out.println("  [" + alert.getSeverityLevel() + "] " + alert.getMessage());
        }
 
        System.out.println("\n--- [11d] ALERT HISTORY - FILTER BY DATE (last 5 seconds) ---");
        List<Alert> recentHistory = farm.getAlertHistory(
                null, null, null,
                LocalDateTime.now().minusSeconds(5),
                LocalDateTime.now()
        );
        System.out.println("Recent alerts (last 5s): " + recentHistory.size());
        System.out.println();
 
        // ============================================================
        // 12. ZONE DEACTIVATE / REACTIVATE
        // ============================================================
        System.out.println("--- [12] ZONE DEACTIVATE / REACTIVATE ---");
 
        System.out.println("Deactivating CZ-01...");
        farm.deactivateZone("CZ-01");
        System.out.println("CZ-01 status: " + cropZone.getStatus());
 
        System.out.println("Deactivating CZ-01 again (should fail, already suspended)...");
        farm.deactivateZone("CZ-01");
 
        System.out.println("Reactivating CZ-01...");
        farm.reactivateZone("CZ-01");
        System.out.println("CZ-01 status: " + cropZone.getStatus());
 
        System.out.println();
 
        // ============================================================
        // 13. FINAL ZONES OVERVIEW
        // ============================================================
        System.out.println("--- [13] FINAL ZONES OVERVIEW ---");
        farm.displayZonesOverview();
 
        System.out.println("\n========================================");
        System.out.println("   TEST COMPLETE");
        System.out.println("========================================");
    }
}
