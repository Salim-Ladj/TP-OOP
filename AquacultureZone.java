import java.util.List;
import java.util.ArrayList;

public class AquacultureZone extends Zone {
    // Attributes
    private List<Animal> species;
    private int numberOfAnimals; 
    private FeedingProgram feedingProgram;

    // Constructor
    public AquacultureZone(String code, String name) {
        super(code, name);
        this.species = new ArrayList<>();
        this.numberOfAnimals = 0;
    }

    public void setFeedingProgram(FeedingProgram fp) {
        this.feedingProgram = fp;
    }

    public FeedingProgram getFeedingProgram(){
        return this.feedingProgram;
    }

    // Methods
    @Override
    public void addEntity(Object entity) {
        if (entity instanceof Animal) {
            this.species.add((Animal) entity);
            this.numberOfAnimals++;
        }
    }

    @Override
    public void display() {
        System.out.println("Aquaculture Zone: " + name);
        System.out.println("Status: " + status);
        System.out.println("Number of Aquatic Species: " + numberOfAnimals);
    }

    public void sensorReport() {
        // Call sensor methos
    }
}