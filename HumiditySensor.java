public class HumiditySensor extends Sensor {

    public HumiditySensor(String uniqueCode, String zoneId, double minHumidityThreshold, double maxHumidityThreshold) {
        super(uniqueCode, zoneId, minHumidityThreshold, maxHumidityThreshold);
    }

    @Override
    public void processReading(double value) {
        if (!isReadingWithinThreshold()) {
            String message = "HUMIDITY or Rainfall  ALERT (" + uniqueCode + "): " + value + " is outside the range [" + getMinThreshold() + ", " + getMaxThreshold() + "].";
            AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
        } else {
            System.out.println("Humidity and Rainfall (" + uniqueCode + "): " + value + " are normal.");
        }
    }
}