import java.util.List;
import java.util.ArrayList;

public abstract class Zone implements Suspendable {
    // Attributes
    protected String code;
    protected String name;
    protected ZoneStatus status;
    protected ArrayList<Sensor> sensors; 
    protected ProductionRecord productionRecord;

    // Constructor
    public Zone(String code, String name) {
        this.code = code;
        this.name = name;
        this.status = ZoneStatus.ACTIVE;
        this.sensors = new ArrayList<>();
    }

    // Abstract Methods (to be implemented by CropZone, LivestockZone, etc.)
    public abstract void display();
    
    public abstract void addEntity(Object entity);

    // Common Methods (defined in the abstract class)
    public void addSensor(Sensor sensor) {
        if (sensor != null) {
            this.sensors.add(sensor);
        }
    }

    public void setProductionRecord(ProductionRecord record) {
        this.productionRecord = record;
    }

    // Interface Method Implementations
    @Override
    public void suspend() {
        this.status = ZoneStatus.SUSPENDED;
    }

    @Override
    public void reactivate() {
        this.status = ZoneStatus.ACTIVE;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public String getName() { return name; }
    public ZoneStatus getStatus() { return status; }
    public ProductionRecord getProductionRecord() { return productionRecord; }
}