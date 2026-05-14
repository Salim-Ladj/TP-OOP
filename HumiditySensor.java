public class HumiditySensor extends Sensor {

    public HumiditySensor(String uniqueCode, String zoneId, double minHumidityThreshold, double maxHumidityThreshold) {
        super(uniqueCode, zoneId, minHumidityThreshold, maxHumidityThreshold);
    }

    @Override
    public void processReading(double value) {
        Object[] result = isReadingWithinThreshold(value, this.minThres, this.maxThres);
        if (! (Boolean) result[0]) {
            String message = "HUMIDITY ALERT (" + uniqueCode + "): " + "is " + result[1] + ".";
            if ((String) result[1] == "Critical") {
                AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
            } else if ((String) result[1] == "Warning") {
                 AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.WARNING, message);
            }
        } else {
            System.out.println("Humidity (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}