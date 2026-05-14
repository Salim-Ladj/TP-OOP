public class WaterTemperatureSensor extends Sensor {
    public WaterTemperatureSensor(String uniqueCode, String zoneId, double minThreshold, double maxThreshold) {
        super(uniqueCode, zoneId, minThreshold, maxThreshold);
    }

    @Override
    public void processReading(double value) {
        Object[] result = isReadingWithinThreshold(value, this.minThres, this.maxThres);
        if (! (Boolean) result[0]) {
            String message = "WATER TEMPERATURE ALERT (" + uniqueCode + "): " + "is " + result[1] + ".";
            if ((String) result[1] == "Critical") {
                AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
            } else if ((String) result[1] == "Warning") {
                 AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.WARNING, message);
            }
        } else {
            System.out.println("Water Temperature (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}