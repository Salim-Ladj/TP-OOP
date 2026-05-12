public class BiometricSensor extends Sensor {
    public BiometricSensor(String uniqueCode, String zoneId, double minThreshold, double maxThreshold) {
        super(uniqueCode, zoneId, minThreshold, maxThreshold);
    }

    @Override
    public void processReading(double value) {
        if (!isReadingWithinThreshold()) {
            String message = "Biometric (" + uniqueCode + ") Out of range: " + value + ", Range: [" + getMinThreshold() + "-" + getMaxThreshold() + "]";
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
        } else {
            System.out.println("Biometric (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}