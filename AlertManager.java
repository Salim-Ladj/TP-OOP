import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AlertManager {
    private static List<Alert> allAlerts = new ArrayList<>();
    private static int nextAlertId = 1; 

    private AlertManager() {}
    public static void createAlert(String sensorUniqueCode, String zoneId, double readingValue, SeverityLevel severity, String message) {
        Alert newAlert = new Alert(nextAlertId++, sensorUniqueCode, zoneId, readingValue, severity, message);
        allAlerts.add(newAlert);
        System.out.println("New Alert (" + severity + "): " + message + " - ID: " + newAlert.getAlertId());
    }



    public static List<Alert> getActiveAlertsSortedBySeverity() {
        return allAlerts.stream()
                .filter(alert -> !alert.isAcknowledged()) 
                .sorted(Comparator.comparing(Alert::getSeverityLevel).reversed())
                .collect(Collectors.toList());
    }
    public static boolean acknowledgeAlert(int alertId) { 
        for (Alert alert : allAlerts) {
            if (alert.getAlertId() == alertId && !alert.isAcknowledged()) { 
                alert.acknowledge();
                return true;
            }
        }
        System.out.println("Alert " + alertId + " not found or already acknowledged.");
        return false;
    }

    public static List<Alert> getAlertHistory(String zoneId, String sensorUniqueCode, SeverityLevel severityLevel,
                                              LocalDateTime startDate, LocalDateTime endDate) {
        return allAlerts.stream()
                .filter(alert -> (zoneId == null || alert.getZoneId().equals(zoneId)))
                .filter(alert -> (sensorUniqueCode == null || alert.getSensorUniqueCode().contains(sensorUniqueCode)))
                .filter(alert -> (severityLevel == null || alert.getSeverityLevel().equals(severityLevel)))
                .filter(alert -> (startDate == null || alert.getAlertTimestamp().isAfter(startDate) || alert.getAlertTimestamp().isEqual(startDate)))
                .filter(alert -> (endDate == null || alert.getAlertTimestamp().isBefore(endDate) || alert.getAlertTimestamp().isEqual(endDate)))
                .sorted(Comparator.comparing(Alert::getAlertTimestamp).reversed())
                .collect(Collectors.toList());
    }
}