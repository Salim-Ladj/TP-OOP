import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public String addZone(Zone zone) {
        if (zones.stream().anyMatch(z -> z.getCode().equals(zone.getCode()))) {
            return "A zone with the code " + zone.getCode() + " already exists.";
        }
        this.zones.add(zone);
        return "Zone " + zone.getCode() + " added successfully.";
    }

    public Optional<Zone> getZoneByCode(String zoneCode) {
        return zones.stream().filter(z -> z.getCode().equals(zoneCode)).findFirst();
    }

    public String deactivateZone(String zoneCode) {
        Optional<Zone> zone = getZoneByCode(zoneCode);
        if (zone.isPresent() && zone.get().getStatus() == ZoneStatus.ACTIVE) {
            zone.get().suspend();
            return "Zone " + zoneCode + " deactivated successfully.";
        }
        return "Zone " + zoneCode + " not found or already deactivated.";
    }

    public String reactivateZone(String zoneCode) {
        Optional<Zone> zone = getZoneByCode(zoneCode);
        if (zone.isPresent() && zone.get().getStatus() == ZoneStatus.SUSPENDED) {
            zone.get().reactivate();
            return "Zone " + zoneCode + " reactivated successfully.";
        }
        return "Zone " + zoneCode + " not found or not suspended.";
    }

    public String displayZonesOverview() {
        if (zones.isEmpty()) {
            return "No zones registered for this farm.";
        }
        StringBuilder overview = new StringBuilder("\n--- Overview of Zones in Farm '" + this.name + "' ---\n");
        for (Zone zone : zones) {
            String status = zone.getStatus().name();
            int numSensors = zone.getSensors().size();
            String zoneType = zone.getClass().getSimpleName();
            overview.append("Code: ").append(zone.getCode())
                    .append(", Name: ").append(zone.getName())
                    .append(", Type: ").append(zoneType)
                    .append(", Status: ").append(status)
                    .append(", Sensors: ").append(numSensors)
                    .append("\n");
        }
        return overview.toString();
    }

    public List<Alert> getActiveAlerts() {
        return AlertManager.getActiveAlertsSortedBySeverity();
    }

    public List<Alert> getAlertHistory(String zoneCode, String sensorUniqueCodePartial, SeverityLevel severityLevel,
                                       LocalDateTime startDate, LocalDateTime endDate) {
        return AlertManager.getAlertHistory(zoneCode, sensorUniqueCodePartial, severityLevel, startDate, endDate);
    }

    public String acknowledgeAlert(int alertId) {
        return AlertManager.acknowledgeAlert(alertId);
    }

    public String dismissAlert(int alertId) {
        return AlertManager.dismissAlert(alertId);
    }

    public String getAlertsGraphicalOverview() {
        return AlertManager.getAlertsOverviewBySeverity();
    }

    public String browseSensorReadingHistory(String zoneCode, String sensorUniqueCode, LocalDateTime startDate, LocalDateTime endDate) {
        Optional<Zone> zoneOpt = getZoneByCode(zoneCode);
        if (zoneOpt.isEmpty()) {
            return "Zone " + zoneCode + " not found.";
        }
        Optional<Sensor> sensorOpt = zoneOpt.get().getSensorByUniqueCode(sensorUniqueCode);
        if (sensorOpt.isEmpty()) {
            return "Sensor " + sensorUniqueCode + " not found in zone " + zoneCode + ".";
        }

        List<SensorReading> history = sensorOpt.get().getReadingsHistory(startDate, endDate);
        if (history.isEmpty()) {
            return "No readings found for sensor " + sensorUniqueCode + " in the specified period.";
        }

        StringBuilder historyReport = new StringBuilder("Reading History for Sensor " + sensorUniqueCode + " in Zone " + zoneCode + ":\n");
        for (SensorReading reading : history) {
            historyReport.append("- ").append(reading.getTimestamp()).append(": ").append(reading.getValue()).append(" ").append(sensorOpt.get().getUnitOfMeasurement()).append("\n");
        }
        return historyReport.toString();
    }

    public String changeSensorStatus(String zoneCode, String sensorUniqueCode, SensorStatus newStatus) {
        Optional<Zone> zoneOpt = getZoneByCode(zoneCode);
        if (zoneOpt.isEmpty()) {
            return "Zone " + zoneCode + " not found.";
        }
        Optional<Sensor> sensorOpt = zoneOpt.get().getSensorByUniqueCode(sensorUniqueCode);
        if (sensorOpt.isEmpty()) {
            return "Sensor " + sensorUniqueCode + " not found in zone " + zoneCode + ".";
        }

        Sensor sensor = sensorOpt.get();
        if (sensor.getStatus() == newStatus) {
            return "Sensor " + sensorUniqueCode + " is already " + newStatus.name() + ".";
        }

        sensor.changeStatus(newStatus);
        return "Sensor " + sensorUniqueCode + " status changed to " + newStatus.name() + " successfully.";
    }

    public String updateSensorThresholds(String zoneCode, String sensorUniqueCode, double minThreshold, double maxThreshold, Double minLongThreshold, Double maxLongThreshold) {
        Optional<Zone> zoneOpt = getZoneByCode(zoneCode);
        if (zoneOpt.isEmpty()) {
            return "Zone " + zoneCode + " not found.";
        }
        Optional<Sensor> sensorOpt = zoneOpt.get().getSensorByUniqueCode(sensorUniqueCode);
        if (sensorOpt.isEmpty()) {
            return "Sensor " + sensorUniqueCode + " not found in zone " + zoneCode + ".";
        }

        Sensor sensor = sensorOpt.get();
        sensor.setMinThreshold(minThreshold);
        sensor.setMaxThreshold(maxThreshold);

        StringBuilder result = new StringBuilder("Sensor " + sensorUniqueCode + " thresholds updated to Min: " + minThreshold + ", Max: " + maxThreshold + ".");

        if (sensor instanceof GPSCollarSensor gpsSensor) {
            if (minLongThreshold != null && maxLongThreshold != null) {
                gpsSensor.setMinThresLong(minLongThreshold);
                gpsSensor.setMaxThresLong(maxLongThreshold);
                result.append(" GPS Longitude thresholds updated to Min: ").append(minLongThreshold).append(", Max: ").append(maxLongThreshold).append(".");
            } else {
                result.append(" GPS Longitude thresholds were not updated as parameters were missing.");
            }
        }
        return result.toString();
    }

    // Method to add a sensor's reading (delegating to the sensor's specific add method)
    public String addSensorReading(String zoneCode, String sensorUniqueCode, double value) { // Renamed from updateSensorReading
        Optional<Zone> zoneOpt = getZoneByCode(zoneCode);
        if (zoneOpt.isEmpty()) {
            return "Zone " + zoneCode + " not found.";
        }
        Optional<Sensor> sensorOpt = zoneOpt.get().getSensorByUniqueCode(sensorUniqueCode);
        if (sensorOpt.isEmpty()) {
            return "Sensor " + sensorUniqueCode + " not found in zone " + zoneCode + ".";
        }

        return sensorOpt.get().addReading(value); // Call the addReading method
    }

    // Overloaded method for GPS sensors to add both latitude and longitude
    public String addSensorReading(String zoneCode, String sensorUniqueCode, double latitude, double longitude) { // Renamed from updateSensorReading
        Optional<Zone> zoneOpt = getZoneByCode(zoneCode);
        if (zoneOpt.isEmpty()) {
            return "Zone " + zoneCode + " not found.";
        }
        Optional<Sensor> sensorOpt = zoneOpt.get().getSensorByUniqueCode(sensorUniqueCode);
        if (sensorOpt.isEmpty()) {
            return "Sensor " + sensorUniqueCode + " not found in zone " + zoneCode + ".";
        }

        if (sensorOpt.get() instanceof GPSCollarSensor gpsSensor) {
            return gpsSensor.addGPSReading(latitude, longitude); // Call the specialized GPS add method
        } else {
            return "Sensor " + sensorUniqueCode + " is not a GPS Collar Sensor. Use the single-value add method.";
        }
    }
}