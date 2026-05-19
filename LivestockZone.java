import java.util.List;
import java.util.ArrayList;

public class LivestockZone extends Zone {
    // Attributes
    private List<Animal> animals;
    private int numberOfAnimals;
    private FeedingProgram feedingProgram;
    private AnimalType type;
    // Constructor
    public LivestockZone(String code, String name, AnimalType type) {
        super(code, name);
        this.animals = new ArrayList<>();
        this.numberOfAnimals = 0;
        this.type = type;
    }

   
    // Methods
    @Override
    public void addEntity(Object entity) {
        if (this.getStatus() == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.getCode() + " is currently SUSPENDED.");
        }

        // Enforcing Type Rule
        if (!(entity instanceof Animal)) {
            throw new IllegalArgumentException("Type Error: LivestockZone cannot accept " + entity.getClass().getSimpleName());
        }
        Animal animal = (Animal) entity;
        if (animal.getType() != this.type) {
            throw new IllegalArgumentException("Type Error: Animal of type " + animal.getType() + " cannot be added to LivestockZone of type " + this.type);
        }
        this.animals.add(animal);
        this.numberOfAnimals++;
    }

    @Override
    public void display() {
        System.out.println("Livestock Zone: " + name);
        System.out.println("Status: " + status);
        System.out.println("Number of Animals: " + numberOfAnimals);
    }
    //Getters and Setters
    public List<Animal> getAnimals() {
        return animals;
    }
    public void setFeedingProgram(FeedingProgram fp) {
        if (this.getStatus() == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.getCode() + " is currently SUSPENDED.");
        }
        this.feedingProgram = fp;
    }
    public FeedingProgram getFeedingProgram(){
        return this.feedingProgram;
    }

    public AnimalType getType() {
        return type;
    }
    @Override
    public void setProductionRecord(ProductionRecord record) {
        if (this.getStatus() == ZoneStatus.SUSPENDED) {
            throw new IllegalStateException("Operation Denied: Zone " + this.getCode() + " is currently SUSPENDED.");
        }
        if(this.type ==AnimalType.RUMINANT && record.getType() != ProductionType.MILK) {
            throw new IllegalArgumentException("Type Error: ProductionRecord of type " + record.getType() + " cannot be added to LivestockZone of type " + this.type);
        }
        if(this.type ==AnimalType.POULTRY && record.getType() != ProductionType.EGGS) {
            throw new IllegalArgumentException("Type Error: ProductionRecord of type " + record.getType() + " cannot be added to LivestockZone of type " + this.type);
        }
        this.productionRecord = record;
    }

}