public class SoilSensor extends Sensor {
    public SoilSensor(String uniqueCode, String zoneId, double minSoilThreshold, double maxSoilThreshold, String UnitOfMeasurement) {
        super(uniqueCode, zoneId, minSoilThreshold, maxSoilThreshold, UnitOfMeasurement);
    }

    @Override
    public void processReading(double value) {
        Object[] result = isReadingWithinThreshold(value, this.minThres, this.maxThres);
        if (! (Boolean) result[0]) {
            String message = "SOIL ALERT (" + uniqueCode + "): " + "is " + result[1] + ".";
            if ("Critical".equals(result[1])) {
                AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
            } else if ("Warning".equals(result[1])) {
                 AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.WARNING, message);
            }
        } else {
            System.out.println("Soil (" + uniqueCode + "): " + value + " is normal.");
        }
    }
} 