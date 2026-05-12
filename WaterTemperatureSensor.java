public class WaterTemperatureSensor extends Sensor {
    public WaterTemperatureSensor(String uniqueCode, String zoneId, double minThreshold, double maxThreshold) {
        super(uniqueCode, zoneId, minThreshold, maxThreshold);
    }

    @Override
    public void processReading(double value) {
        if (!isReadingWithinThreshold()) {
            String message = "ALERT Water's Temperature (" + uniqueCode + ") out of range. Value: " + value + ", Range: [" + getMinThreshold() + "-" + getMaxThreshold() + "]";
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
        } else {
            System.out.println("Water's Temperature (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}