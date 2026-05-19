public class TemperatureSensor extends Sensor {
    public TemperatureSensor(String uniqueCode, String zoneId, double minTempThreshold, double maxTempThreshold, String unit) {
        super(uniqueCode, zoneId, minTempThreshold, maxTempThreshold, unit);
    }

    @Override
    public void processReading(double value) {
        Object[] result = isReadingWithinThreshold(value, this.minThres, this.maxThres);
        if (! (Boolean) result[0]) {
            String message = "TEMPERATURE ALERT (" + uniqueCode + "): " + "is " + result[1] + ".";
            if ("Critical".equals(result[1])) {
                AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
            } else if ("Warning".equals(result[1])) {
                 AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.WARNING, message);
            }
        } else {
            System.out.println("Temperature (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}