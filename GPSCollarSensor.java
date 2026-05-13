import java.time.LocalDateTime;
public class GPSCollarSensor extends Sensor {
    private double lastLongitude; 
    public GPSCollarSensor(String uniqueCode, String zoneId) {
        super(uniqueCode, zoneId, -9999.0, 9999.0); 
        this.lastLongitude = 0.0;
    }

    public void updateReading(double latitude, double longitude) {
        this.lastValue = latitude; 
        this.lastLongitude = longitude; 
        this.lastReadingTime = LocalDateTime.now();
        processReading(latitude); 
    }

    public double getLastLongitude() { return lastLongitude; }


    @Override
    public void processReading(double latitude) { 
        System.out.println("The GPS position for (" + uniqueCode + "): Lat " + lastValue + ", Lon " + lastLongitude);
    }
}