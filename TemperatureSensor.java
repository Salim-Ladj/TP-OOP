public class TemperatureSensor extends Sensor {
    public TemperatureSensor(String uniqueCode, String zoneId, double minTempThreshold, double maxTempThreshold, String unit) throws ThresholdException {
        super(uniqueCode, zoneId, minTempThreshold, maxTempThreshold, unit);
    }

    @Override
    protected String processReading(double value) {
        Object[] result = getReadingStatus(value);
        String statusMessage = (String) result[1];

        if (! (Boolean) result[0]) {
            SeverityLevel severity = statusMessage.equals("Critical") ? SeverityLevel.CRITICAL : SeverityLevel.WARNING;
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, severity,
                                     "TEMPERATURE ALERT (" + uniqueCode + "): " + statusMessage + " reading: " + value + " " + UnitOfMeasurement);
            return statusMessage + " - Alert created.";
        } else {
            return "Normal reading: " + value + " " + UnitOfMeasurement;
        }
    }
}