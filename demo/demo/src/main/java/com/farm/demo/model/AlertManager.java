package com.farm.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AlertManager {
    private static List<Alert> allAlerts = new ArrayList<>();
    private static int nextAlertId = 1;

    private AlertManager() {} 

    public static void createAlert(String sensorUniqueCode, String zoneId, double readingValue, SeverityLevel severity, String message) {
        Alert newAlert = new Alert(nextAlertId++, sensorUniqueCode, zoneId, readingValue, severity, message);
        allAlerts.add(newAlert);
    }

    public static List<Alert> getActiveAlertsSortedBySeverity() {
        return allAlerts.stream()
                .filter(alert -> !alert.isAcknowledged()) 
                .sorted(Comparator.comparing(Alert::getSeverityLevel).reversed()) 
                .collect(Collectors.toList());
    }

    public static Optional<Alert> getAlertById(int alertId) {
        return allAlerts.stream()
                        .filter(alert -> alert.getAlertId() == alertId)
                        .findFirst();
    }

    public static String acknowledgeAlert(int alertId) {
        Optional<Alert> alertOpt = getAlertById(alertId);
        if (alertOpt.isPresent()) {
            return alertOpt.get().acknowledge();
        }
        return "Alert " + alertId + " not found.";
    }

    public static String dismissAlert(int alertId) {
        Optional<Alert> alertOpt = getAlertById(alertId);
        if (alertOpt.isPresent()) {
            return alertOpt.get().dismiss();
        }
        return "Alert " + alertId + " not found.";
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

    public static String getAlertsOverviewBySeverity() {
        long criticalCount = allAlerts.stream().filter(a -> a.getSeverityLevel() == SeverityLevel.CRITICAL && !a.isAcknowledged()).count();
        long warningCount = allAlerts.stream().filter(a -> a.getSeverityLevel() == SeverityLevel.WARNING && !a.isAcknowledged()).count();
        long normalCount = allAlerts.stream().filter(a -> a.getSeverityLevel() == SeverityLevel.NORMAL && !a.isAcknowledged()).count();

        return "Active Alerts Overview:\n" +
               "Critical 🔴: " + criticalCount + " alerts\n" +
               "Warning 🟡: " + warningCount + " alerts\n" +
               "Normal 🟢: " + normalCount + " events";
    }
}