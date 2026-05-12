import java.util.List;
import java.util.ArrayList;

public class LivestockZone extends Zone {
    // Attributes
    private List<Animal> animals;
    private int numberOfAnimals;
    private FeedingProgram feedingProgram;

    // Constructor
    public LivestockZone(String code, String name) {
        super(code, name);
        this.animals = new ArrayList<>();
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
            this.animals.add((Animal) entity);
            this.numberOfAnimals++;
        }
    }

    @Override
    public void display() {
        System.out.println("Livestock Zone: " + name);
        System.out.println("Status: " + status);
        System.out.println("Number of Animals: " + numberOfAnimals);
    }
}