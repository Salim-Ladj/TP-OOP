import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; 

public class Farm {
    private String farmId;
    private String name;
    private List<Zone> zones; 

    public Farm(String farmId, String name) {
        this.farmId = farmId;
        this.name = name;
        this.zones = new ArrayList<>();
    }

    public String getFarmId() { return farmId; }
    public String getName() { return name; }
    public List<Zone> getZones() { return zones; }
    public void addZone(Zone zone) {
        if (zones.stream().anyMatch(z -> z.getCode().equals(zone.getCode()))) {
            System.out.println("A zone with the code " + zone.getCode() + " already exists.");
            return;
        }
        this.zones.add(zone);
        System.out.println("Zone '" + zone.getName() + "' (Code: " + zone.getCode() + ") added to the farm " + this.name + ".");
    }
    public Optional<Zone> getZoneByCode(String zoneCode) {
        return zones.stream().filter(z -> z.getCode().equals(zoneCode)).findFirst();
    }
    public boolean deactivateZone(String zoneCode) { 
        Optional<Zone> zoneOpt = getZoneByCode(zoneCode);
        if (zoneOpt.isPresent()) {
            zoneOpt.get().suspend(); 
            System.out.println("Zone " + zoneCode + " deactivated.");
            return true;
        }
        System.out.println("Zone " + zoneCode + " not found for deactivation.");
        return false;
    }
    public void displayZonesOverview() {
        System.out.println("\n--- Overview of Zones in Farm '" + this.name + "' ---");
        if (zones.isEmpty()) {
            System.out.println("No zones registered for this farm.");
            return;
        }
        for (Zone zone : zones) {
            String status = zone.getStatus().name();
            int numSensors = zone.getSensors().size();
            String zoneType = zone.getClass().getSimpleName(); 
            System.out.println("Code: " + zone.getCode() + ", Name: " + zone.getName() + ", Type: " + zoneType +
                               ", Status: " + status + ", Sensors: " + numSensors);
        }
        System.out.println("----------------------------------------------");
    }
    public List<Alert> getActiveAlerts() {
        return AlertManager.getActiveAlertsSortedBySeverity();
    }

    public List<Alert> getAlertHistory(String zoneCode, String sensorUniqueCodePartial, SeverityLevel severityLevel,
                                       LocalDateTime startDate, LocalDateTime endDate) {
        return AlertManager.getAlertHistory(zoneCode, sensorUniqueCodePartial, severityLevel, startDate, endDate);
    }


    public boolean acknowledgeAlert(int alertId) {
        return AlertManager.acknowledgeAlert(alertId);
    }
}