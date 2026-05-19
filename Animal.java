import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.LinkedHashMap;

public abstract class Animal {
    // Attributes
    private AnimalType type;
    private String uniqueNumber;
    private String species;
    private int age;
    private double weight;
    private HealthStatus health;
    private Map<HealthStatus, List<String>> healthHistory;

    // Constructor
    public Animal(AnimalType type, String uniqueNumber, String species,int age, double weight, HealthStatus health) {
        this.type = type;
        this.uniqueNumber = uniqueNumber;
        this.species = species;
        this.age = age;
        this.weight = weight;
        this.health = health;
        this.healthHistory = new EnumMap<>(HealthStatus.class);
        for (HealthStatus status : HealthStatus.values()) {
            this.healthHistory.put(status, new ArrayList<>());
        }
     }

    // Methods
    public void logHealthEvent(String eventDescription, double currentWeight,int currentAge) {
        if (currentAge < 0 || currentWeight < 0) {
            throw new IllegalArgumentException("Age and weight must be positive values.");
        }
        if (currentAge < age ) {
            throw new IllegalArgumentException("Current age cannot be less than the previously recorded age.");
        }
        String event = "Age: " + currentAge + ", Weight: " + currentWeight + ", Event: " + eventDescription;
        age = currentAge;
        weight = currentWeight; 
        this.healthHistory.get(health).add(event);
    }
    public void displayHealthHistory() {
        System.out.println("Health History for " + species + " (ID: " + uniqueNumber + "):");
        for (Map.Entry<HealthStatus, List<String>> entry : healthHistory.entrySet()) {
            HealthStatus statusCategory = entry.getKey();
            List<String> eventsForStatus = entry.getValue();
            
            System.out.println("\nCategory [" + statusCategory + "]:");
            if (eventsForStatus.isEmpty()) {
                System.out.println("  No historical events logged under this status.");
            } else {
                for (String record : eventsForStatus) {
                    System.out.println("  - " + record);
                }
            }
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
    // Getters and Setters
    public AnimalType getType() {
        return type;
    }
    public String getUniqueNumber() {
        return uniqueNumber;
    }
    public String getSpecies() {
        return species;
    }
    public int getAge() {
        return age;
    }
    public double getWeight() {
        return weight;
    }
    public HealthStatus getHealth() {
        return health;
    }
    public void setHealth(HealthStatus health) {
        this.health = health;
    }

}