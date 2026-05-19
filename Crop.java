import java.time.LocalDate;

public class Crop {
    // Attributes
    private CropType type; // Using the Enum
    private String species;
    private LocalDate plantingDate;
    private LocalDate expectedHarvestDate;
    private GrowthStage currentStage;
    private double minPH, maxPH;
    private double minMoisture, maxMoisture;

    // Constructor
    public Crop(CropType type, String species,  double minPH, double maxPH,GrowthStage initialStage, double minMoisture, double maxMoisture, LocalDate expectedHarvestDate) {
        this.type = type;
        this.species = species;
        this.plantingDate = LocalDate.now(); 
        this.expectedHarvestDate = expectedHarvestDate;
        this.currentStage = initialStage; 
        this.minPH = minPH;
        this.maxPH = maxPH;
        this.minMoisture = minMoisture;
        this.maxMoisture = maxMoisture;

     }

    // Methods
    public void updateGrowthStage(GrowthStage newStage) { 
        this.currentStage = newStage;
    }
    

    public boolean checkSoilRequirements(double ph, double moisture) { 
        if (ph < 0 || moisture < 0) {
            throw new IllegalArgumentException("pH and moisture values must be positive.");
        }
        if (ph >= minPH && ph <= maxPH && moisture >= minMoisture && moisture <= maxMoisture) {
            System.out.println("Soil conditions are optimal for " + species);
            return true; 
        }
        if (ph < minPH) {
            System.out.println("Soil is too low for " + species);
        } else if (ph > maxPH) {
            System.out.println("Soil is too high for " + species);
        }
        if (moisture < minMoisture) {
            System.out.println("Soil moisture is too low for " + species);
        } else if (moisture > maxMoisture) {
            System.out.println("Soil moisture is too high for " + species);
        }
        return false; 
    }
    void displayInfo() {
        System.out.println("Crop: " + species + " (" + type + ")");
        System.out.println("Planting Date: " + plantingDate);
        System.out.println("Expected Harvest Date: " + expectedHarvestDate);
        System.out.println("Current Growth Stage: " + currentStage);
        System.out.println("Optimal Soil pH: " + minPH + " - " + maxPH);
        System.out.println("Optimal Soil Moisture: " + minMoisture + "% - " + maxMoisture + "%");
    }
    
    // Getters and Setters
    public CropType getType() {
        return type;
    }   
    public String getSpecies() {
        return species;
    }
    public GrowthStage getCurrentStage() {
        return currentStage;
    }
}