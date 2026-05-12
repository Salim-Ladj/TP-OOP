public class DissolvedOxygenSensor extends Sensor {
    public DissolvedOxygenSensor(String uniqueCode, String zoneId, double minThreshold, double maxThreshold) {
        super(uniqueCode, zoneId, minThreshold, maxThreshold);
    }

    @Override
    public void processReading(double value) {
        if (!isReadingWithinThreshold()) {
            String message = "Dissolved Oxygen (" + uniqueCode + ") out of range. Value: " + value + ", Range: [" + getMinThreshold() + "-" + getMaxThreshold() + "]";
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
        } else {
            System.out.println("Dissolved Oxygen (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}