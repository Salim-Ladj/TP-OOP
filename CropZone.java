import java.util.List;
import java.util.ArrayList;

public class CropZone extends Zone {
    // Attributes
    private List<Crop> crops;

    // Constructor
    public CropZone(String code, String name) {
        super(code, name);
        this.crops = new ArrayList<>();
    }

    // Methods
    @Override
    public void addEntity(Object entity) {
        if (entity instanceof Crop) {
            this.crops.add((Crop) entity);
        }
    }

    @Override
    public void display() {
        System.out.println("Crop Zone: " + name);
        System.out.println("Code: " + code);
        System.out.println("Status: " + status);
        System.out.println("Number of Crops: " + crops.size());
    }

    public void generateCropStatusReport() {
        // Need sensor methods
    }
    //getters and setters
    public List<Crop> getCrops() {
        return crops;
    }
    
}