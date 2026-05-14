public class TemperatureSensor extends Sensor {
    public TemperatureSensor(String uniqueCode, String zoneId, double minTempThreshold, double maxTempThreshold) {
        super(uniqueCode, zoneId, minTempThreshold, maxTempThreshold);
    }

    @Override
    public void processReading(double value) {
        Object[] result = isReadingWithinThreshold(value, this.minThres, this.maxThres);
        if (! (Boolean) result[0]) {
            String message = "TEMPERATURE ALERT (" + uniqueCode + "): " + "is " + result[1] + ".";
            if ((String) result[1] == "Critical") {
                AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
            } else if ((String) result[1] == "Warning") {
                 AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.WARNING, message);
            }
        } else {
            System.out.println("Temperature (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}