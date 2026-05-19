import java.time.LocalDateTime;
public abstract class Sensor{
    protected String uniqueCode;
    protected String zoneID;
    protected SensorStatus status;
    protected double lastValue;
    protected LocalDateTime lastReadingTime;
    protected double minThres;
    protected double maxThres;
    protected String UnitOfMeasurement;
    public Sensor(String uniqueCode, String zoneID, double minThres, double maxThres, String unit) {
        this.uniqueCode = uniqueCode;
        this.zoneID = zoneID;
        this.status = SensorStatus.ACTIVE;
        this.lastValue = 0.0;
        this.lastReadingTime = null;
        this.UnitOfMeasurement = unit;
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
    public String getUnitOfMeasurement() { 
        return UnitOfMeasurement; 
    }
    public void changeStatus(SensorStatus newStatus) { 
        this.status = newStatus; 
    }
    public Object[] isReadingWithinThreshold(double lastValue, double minThres, double maxThres) {
        if (this.status != SensorStatus.ACTIVE) {
            return new Object[]{true, "Sensor is not active"};
        }
        if (lastValue < 0.8 * minThres || lastValue > 1.2 * maxThres) {
            return new Object[]{false, "Critical"};
        } else if (lastValue < minThres || lastValue > maxThres) {
            return new Object[]{false, "Warning"};
        } else {
            return new Object[]{true, "Normal"};
        }
    }
    public abstract void processReading(double value);
    public void updateReading(double newValue) {
        this.lastValue = newValue;
        this.lastReadingTime = LocalDateTime.now();
        processReading(newValue);
    }
    public void suspend() {
        changeStatus(SensorStatus.SUSPENDED);
    }
    public void reactivate() {
        changeStatus(SensorStatus.ACTIVE);
    }
}