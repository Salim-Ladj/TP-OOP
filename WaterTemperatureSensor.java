public class WaterTemperatureSensor extends Sensor {
    public WaterTemperatureSensor(String uniqueCode, String zoneId, double minThreshold, double maxThreshold, String UnitOfMeasurement) {
        super(uniqueCode, zoneId, minThreshold, maxThreshold, UnitOfMeasurement);
    }

    @Override
    public void processReading(double value) {
        Object[] result = isReadingWithinThreshold(value, this.minThres, this.maxThres);
        if (! (Boolean) result[0]) {
            String message = "WATER TEMPERATURE ALERT (" + uniqueCode + "): " + "is " + result[1] + ".";
            if ("Critical".equals(result[1])) {
                AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
            } else if ("Warning".equals(result[1])) {
                 AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.WARNING, message);
            }
        } else {
            System.out.println("Water Temperature (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}