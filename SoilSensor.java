public class SoilSensor extends Sensor {
    public SoilSensor(String uniqueCode, String zoneId, double minSoilThreshold, double maxSoilThreshold) {
        super(uniqueCode, zoneId, minSoilThreshold, maxSoilThreshold);
    }

    @Override
    public void processReading(double value) {
        if (!isReadingWithinThreshold()) {
            String message = "SOIL ALERT (" + uniqueCode + "): " + value + " is outside the range [" + getMinThreshold() + ", " + getMaxThreshold() + "].";
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
        } else {
            System.out.println("Soil (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}