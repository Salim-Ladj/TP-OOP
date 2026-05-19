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
    }
    public Optional<Zone> getZoneByCode(String zoneCode) {
        return zones.stream().filter(z -> z.getCode().equals(zoneCode)).findFirst();
    }
    public boolean deactivateZone(String zoneCode) { 
        Optional<Zone> zone = getZoneByCode(zoneCode);
        if (zone.isPresent() && zone.get().getStatus() == ZoneStatus.ACTIVE) {
            zone.get().suspend(); 
            return true;
        }
        System.out.println("Zone " + zoneCode + " not found for deactivation.");
        return false;
    }
    public boolean reactivateZone(String zoneCode) { 
        Optional<Zone> zone = getZoneByCode(zoneCode);
        if ( zone.isPresent() && zone.get().getStatus() == ZoneStatus.SUSPENDED) {
            zone.get().reactivate(); 
            return true;
        }
        System.out.println("Zone " + zoneCode + " not found for reactivation.");
        return false;
    }
    public void displayZonesOverview() {
        if (zones.isEmpty()) {
            System.out.println("No zones registered for this farm.");
            return;
        }
        System.out.println("\n--- Overview of Zones in Farm '" + this.name + "' ---");
        for (Zone zone : zones) {
            String status = zone.getStatus().name();
            int numSensors = zone.getSensors().size();
            String zoneType = zone.getClass().getSimpleName(); 
            System.out.println("Code: " + zone.getCode() + ", Name: " + zone.getName() + ", Type: " + zoneType +
                               ", Status: " + status + ", Sensors: " + numSensors);
        }
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

    public boolean dismissAlert(int alertId) {
        return AlertManager.dismissAlert(alertId);
    }
}