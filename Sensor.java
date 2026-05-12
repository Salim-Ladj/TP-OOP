import java.time.LocalDateTime;
public abstract class Sensor{
    protected String uniqueCode;
    protected String zoneID;
    protected SensorStatus status;
    protected double lastValue;
    protected LocalDateTime lastReadingTime;
    protected double minThres;
    protected double maxThres;
    public Sensor(String uniqueCode, String zoneID, double minThres, double maxThres){
        this.uniqueCode = uniqueCode;
        this.zoneID = zoneID;
        this.status = SensorStatus.ACTIVE;
        this.lastValue = 0.0;
        this.lastReadingTime = null;
        this.minThres = minThres;
        this.maxThres = maxThres;
    }
    public String getUniqueCode() { 
        return uniqueCode; 
    }
    public String getZoneId() { 
        return zoneID; 
    }
    public SensorStatus getStatus() { 
        return status; 
    }
    public double getLastReading() { 
        return lastValue; 
    }
    public LocalDateTime getLastReadingTimestamp() { 
        return lastReadingTime; 
    }
    public double getMinThreshold() { 
        return minThres; 
    }
    public double getMaxThreshold() { 
        return maxThres; 
    }
    public void changeStatus(SensorStatus newStatus) { 
        this.status = newStatus; 
        System.out.println("Sensor " + uniqueCode + " status changed to " + newStatus);
    }
    public boolean isReadingWithinThreshold() {
        if (this.status != SensorStatus.ACTIVE) {
            return true;
        }
        return lastValue >= minThres && lastValue <= maxThres;
    }
    public abstract void processReading(double value);
    public void updateReading(double newValue) {
        this.lastValue = newValue;
        this.lastReadingTime = LocalDateTime.now();
    }
}