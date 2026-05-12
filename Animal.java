import java.util.List;
import java.util.ArrayList;

public abstract class Animal {
    // Attributes
    private AnimalType type;
    private String uniqueNumber;
    private String species;
    private int age;
    private double weight;
    private HealthStatus health;
    private List<String> healthHistory; // Simplified log of illnesses/weight changes

    // Constructor
    public Animal(AnimalType type, String uniqueNumber, String species,int age, double weight, HealthStatus health) {
        this.type = type;
        this.uniqueNumber = uniqueNumber;
        this.species = species;
        this.age = age;
        this.weight = weight;
        this.health = health; // Default health status
        this.healthHistory = new ArrayList<>();
     }

    // Methods
    public void logHealthEvent(String eventDescription, double currentWeight,int currentAge) {
        String logEntry = "Age: " + currentAge + ", Weight: " + currentWeight + ", Event: " + eventDescription;
        this.healthHistory.add(logEntry);

    }
    public void displayHealthHistory() {
        System.out.println("Health History for " + species + " (ID: " + uniqueNumber + "):");
        for (String entry : healthHistory) {
            System.out.println(entry);
        }
    }
    public void display() {
        System.out.println("Animal Details:");
        System.out.println("Type: " + type);
        System.out.println("Animal ID: " + uniqueNumber);
        System.out.println("Species: " + species);
        System.out.println("Age: " + age);
        System.out.println("Weight: " + weight);
        System.out.println("Health Status: " + health);
    }

}