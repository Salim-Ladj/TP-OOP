import java.time.LocalDateTime;

public class Alert {
    private int alertId;
    private String sensorUniqueCode;
    private String zoneId;
    private LocalDateTime alertTimestamp;
    private double readingValue;
    private SeverityLevel severityLevel;
    private String message;
    private boolean isAcknowledged;

    public Alert(int alertID, String sensorUniqueCode, String zoneId, double readingValue, SeverityLevel severityLevel, String message) {
        this.alertId = alertID; 
        this.sensorUniqueCode = sensorUniqueCode;
        this.zoneId = zoneId;
        this.alertTimestamp = LocalDateTime.now();
        this.readingValue = readingValue;
        this.severityLevel = severityLevel;
        this.message = message;
        this.isAcknowledged = false; 
    }

    public int getAlertId() { return alertId; }
    public String getSensorUniqueCode() { return sensorUniqueCode; }
    public String getZoneId() { return zoneId; }
    public LocalDateTime getAlertTimestamp() { return alertTimestamp; }
    public double getReadingValue() { return readingValue; }
    public SeverityLevel getSeverityLevel() { return severityLevel; }
    public String getMessage() { return message; }
    public boolean isAcknowledged() { return isAcknowledged; }

    public void acknowledge() {
        this.isAcknowledged = true;
        System.out.println("Alert " + alertId + " (Sensor " + sensorUniqueCode + ") has been acknowledged.");
    }
}