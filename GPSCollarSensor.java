import java.time.LocalDateTime;
public class GPSCollarSensor extends Sensor {
    private double lastLongitude; 
    private double minThresLong;
    private double maxThresLong;
    public GPSCollarSensor(String uniqueCode, String zoneId, double maxThres, double minThres, double lastLongitude, double maxThresLong, double minThresLong) {
        super(uniqueCode, zoneId, maxThres, minThres); 
        this.lastLongitude = lastLongitude;
        this.minThresLong = minThresLong;
        this.maxThresLong = maxThresLong;
    }

    public void updateReading(double latitude, double longitude) {
        this.lastValue = latitude; 
        this.lastLongitude = longitude; 
        this.lastReadingTime = LocalDateTime.now();
        processReading(latitude); 
    }

    public double getLastLongitude() { return lastLongitude; }


    @Override
    public void processReading(double value) {
        Object[] result1 = isReadingWithinThreshold(value, this.minThres, this.maxThres);
        Object[] result2 = isReadingWithinThreshold(this.lastLongitude, this.minThresLong, this.maxThresLong);
        if (! (Boolean) result1[0] || ! (Boolean) result2[0]) {
            if ((String) result1[1] == "Critical" || (String) result2[1] == "Critical") {
                String message = "GPS COLLAR ALERT (" + uniqueCode + "): " + "is " + result1[1] + ".";
                AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.CRITICAL, message);
            } else {
                String message = "GPS COLLAR ALERT (" + uniqueCode + "): " + "is " + result1[1] + ".";
                 AlertManager.createAlert(this.getUniqueCode(), this.getZoneId(), value, SeverityLevel.WARNING, message);
            }
        } else {
            System.out.println("GPS Collar (" + uniqueCode + "): " + value + " is normal.");
        }
    }
}