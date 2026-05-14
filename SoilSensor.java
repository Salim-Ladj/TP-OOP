public class SoilSensor extends Sensor {
    public SoilSensor(String uniqueCode, String zoneId, double minSoilThreshold, double maxSoilThreshold) {
        super(uniqueCode, zoneId, minSoilThreshold, maxSoilThreshold);
    }

    @Override
    public void processReading(double value) {
        Object[] result = isReadingWithinThreshold(value, this.minThres, this.maxThres);
        if (! (Boolean) result[0]) {
            String message = "SOIL ALERT (" + uniqueCode + "): " + "is " + result[1] + ".";
            if ((String) result[1] == "Critical") {
                AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
            } else if ((String) result[1] == "Warning") {
                 AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.WARNING, message);
            }
        } else {
            System.out.println("Soil (" + uniqueCode + "): " + value + " is normal.");
        }
    }
} 