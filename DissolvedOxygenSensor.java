public class DissolvedOxygenSensor extends Sensor {
    public DissolvedOxygenSensor(String uniqueCode, String zoneId, double minThreshold, double maxThreshold, String UnitOfMeasurement) {
        super(uniqueCode, zoneId, minThreshold, maxThreshold, UnitOfMeasurement);
    }

    @Override
    public void processReading(double value) {
        Object[] result = isReadingWithinThreshold(value, this.minThres, this.maxThres);
        if (! (Boolean) result[0]) {
            String message = "DISSOLVED OXYGEN ALERT (" + uniqueCode + "): " + "is " + result[1] + ".";
            if ("Critical".equals(result[1])) {
                AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
            } else if ("Warning".equals(result[1])) {
                 AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.WARNING, message);
            }
        } else {
            System.out.println("Dissolved Oxygen (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}