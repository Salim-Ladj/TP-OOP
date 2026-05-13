import java.time.LocalDateTime;
public class mainAli {
    public static void main(String[] args) {
        testSensorsAndAlerts();
    }

    public static void testSensorsAndAlerts() {
        System.out.println("--- Testing Sensors and Alerts ---");
        
        TemperatureSensor tempSensor = new TemperatureSensor("TEMP-001", "ZONE-A", 15.0, 30.0);
        
        System.out.println("Processing normal reading...");
        tempSensor.updateReading(22.5);
        
        System.out.println("\nProcessing high temperature reading...");
        tempSensor.updateReading(35.0);
        
        System.out.println("\nProcessing very low temperature reading...");
        tempSensor.updateReading(10.0);
        
        System.out.println("\n--- Active Alerts ---");
        for (var alert : AlertManager.getActiveAlertsSortedBySeverity()) {
            System.out.println(alert.getMessage() + " (Severity: " + alert.getSeverityLevel() + ", ID: " + alert.getAlertId() + ")");
        }
        
        System.out.println("\n--- Acknowledging Alert with ID 1 ---");
        AlertManager.acknowledgeAlert(1);
        
        System.out.println("\n--- Active Alerts after acknowledgment ---");
        for (var alert : AlertManager.getActiveAlertsSortedBySeverity()) {
            System.out.println(alert.getMessage() + " (ID: " + alert.getAlertId() + ")");
        }
    }
}
