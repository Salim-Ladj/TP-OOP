public class TemperatureSensor extends Sensor {
    public TemperatureSensor(String uniqueCode, String zoneId, double minTempThreshold, double maxTempThreshold) {
        super(uniqueCode, zoneId, minTempThreshold, maxTempThreshold);
    }

    @Override
    public void processReading(double value) {
        if (!isReadingWithinThreshold()) {
            String message = "Temperature (" + uniqueCode + "): " + value + " is outside the range [" + getMinThreshold() + ", " + getMaxThreshold() + "].";
            System.out.println("TEMPERATURE ALERT (" + uniqueCode + "): " + value + " is outside the range [" + getMinThreshold() + ", " + getMaxThreshold() + "].");
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
        } else {
            System.out.println("Temperature (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}