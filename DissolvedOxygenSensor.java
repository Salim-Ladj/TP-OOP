public class DissolvedOxygenSensor extends Sensor {
    public DissolvedOxygenSensor(String uniqueCode, String zoneId, double minThreshold, double maxThreshold, String unitOfMeasurement) {
        super(uniqueCode, zoneId, minThreshold, maxThreshold, unitOfMeasurement);
    }

    @Override
    protected String processReading(double value) {
        Object[] result = getReadingStatus(value);
        String statusMessage = (String) result[1];

        if (! (Boolean) result[0]) {
            SeverityLevel severity = statusMessage.equals("Critical") ? SeverityLevel.CRITICAL : SeverityLevel.WARNING;
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, severity,
                                     "DISSOLVED OXYGEN ALERT (" + uniqueCode + "): " + statusMessage + " reading: " + value + " " + UnitOfMeasurement);
            return statusMessage + " - Alert created.";
        } else {
            return "Normal reading: " + value + " " + UnitOfMeasurement;
        }
    }
}