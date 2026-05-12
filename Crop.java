import java.time.LocalDate;

public class Crop {
    // Attributes
    private CropType type; // Using the Enum
    private String species;
    private CropFamily family; // Using the Enum
    private LocalDate plantingDate;
    private LocalDate expectedHarvestDate;
    private GrowthStage currentStage;
    private double minPH, maxPH;
    private double minMoisture, maxMoisture;

    // Constructor
    public Crop(CropType type, String species, CropFamily family, double minPH, double maxPH,GrowthStage initialStage, double minMoisture, double maxMoisture) {
        this.type = type;
        this.species = species;
        this.family = family;
        this.plantingDate = LocalDate.now(); 
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
    
    public String generateStatusReport() { 
        return ""; 
    }

    public boolean checkSoilRequirements(double ph, double moisture) { 
        return false; 
    }
}