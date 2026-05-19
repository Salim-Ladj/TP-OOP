public class Student1Main {
    public static void main(String[] args) {
        CropZone cropZone = new CropZone("CZ-01", "North Field");
        LivestockZone livestockZone = new LivestockZone("LZ-01", "East Barn", AnimalType.RUMINANT);
        AquacultureZone aquacultureZone = new AquacultureZone("AZ-01", "Pond A");
        // Create and add a crop with exception handling
        try {
            cropZone.addEntity(new Crop(
                CropType.CEREALS,
                "Wheat",
                6.0,
                7.5,
                GrowthStage.GROWTH,
                40.0,
                70.0,
                java.time.LocalDate.now().plusMonths(3)
            ));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.err.println("Failed to add crop: " + ex.getMessage());
        }

        // Create animals
        Animal cow = new Animal(AnimalType.RUMINANT, "AN-001", "Cow", 4, 450.0, HealthStatus.HEALTHY) {};
        Animal tilapia = new Animal(AnimalType.AQUATIC, "AN-002", "Tilapia", 1, 2.5, HealthStatus.HEALTHY) {};

        // Add animals with try/catch to show error handling
        try {
            livestockZone.addEntity(cow);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.err.println("Failed to add animal to livestock zone: " + ex.getMessage());
        }

        try {
            aquacultureZone.addEntity(tilapia);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.err.println("Failed to add animal to aquaculture zone: " + ex.getMessage());
        }

        // Assign feeding programs
        try {
            livestockZone.setFeedingProgram(new FeedingProgram("Grass Mix", 5.0, 2));
            aquacultureZone.setFeedingProgram(new FeedingProgram("Pellets", 1.5, 3));
        } catch (IllegalStateException ex) {
            System.err.println("Failed to set feeding program: " + ex.getMessage());
        }

        // Create and update a production record with validation
        ProductionRecord record = new ProductionRecord("kg", ProductionType.CROP_YIELD);
        try {
            record.updateRecord(1500.0, "Harvested wheat from North Field");
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid production record update: " + ex.getMessage());
        }

        // Display basic zone summaries
        cropZone.display();
        System.out.println();
        livestockZone.display();
        System.out.println();
        aquacultureZone.display();
        System.out.println();
        System.out.println(record.getRecord());

        // --- Test Animal health history EnumMap ---
        System.out.println("\n--- Testing Animal health history (EnumMap) ---");
        // Log a normal health event for cow
        try {
            cow.logHealthEvent("Routine check - all good", 455.0, 5);
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed logging health event: " + ex.getMessage());
        }

        // Change cow health to SICK and log event
        cow.setHealth(HealthStatus.SICK);
        cow.logHealthEvent("Fever detected, given medication", 452.0, 5);

        // Change cow health to QUARANTINED and log event
        cow.setHealth(HealthStatus.QUARANTINED);
        cow.logHealthEvent("Moved to quarantine for observation", 452.0, 5);

        // Log invalid event to trigger exception (negative weight)
        try {
            cow.logHealthEvent("Invalid event test", -10.0, 5);
        } catch (IllegalArgumentException ex) {
            System.err.println("Expected error (invalid health log): " + ex.getMessage());
        }

        // Display the cow's health history (should show entries under each enum key)
        cow.displayHealthHistory();

        // --- Test zone suspension behavior ---
        System.out.println("\n--- Testing Zone suspension behavior ---");
        livestockZone.suspend();
        try {
            // This should fail because the zone is suspended
            livestockZone.addEntity(new Animal(AnimalType.RUMINANT, "AN-003", "Sheep", 2, 60.0, HealthStatus.HEALTHY) {});
        } catch (IllegalStateException ex) {
            System.err.println("Expected error (adding to suspended zone): " + ex.getMessage());
        }

        // Reactivate and add
        livestockZone.reactivate();
        try {
            livestockZone.addEntity(new Animal(AnimalType.RUMINANT, "AN-003", "Sheep", 2, 60.0, HealthStatus.HEALTHY) {});
            System.out.println("Successfully added Sheep after reactivation.");
        } catch (Exception ex) {
            System.err.println("Unexpected error after reactivation: " + ex.getMessage());
        }

        // --- Test wrong-type addition to CropZone ---
        System.out.println("\n--- Testing wrong-type addition to CropZone ---");
        try {
            cropZone.addEntity(cow); // wrong type
        } catch (IllegalArgumentException ex) {
            System.err.println("Expected type error: " + ex.getMessage());
        }

        // --- Test FeedingProgram validation ---
        System.out.println("\n--- Testing FeedingProgram validation ---");
        try {
            FeedingProgram fp = livestockZone.getFeedingProgram();
            System.out.println("Current feeding program:\n" + fp.getFullScheduleDetails());
            // attempt invalid update
            fp.updateSchedule(-1.0, 2);
        } catch (IllegalArgumentException ex) {
            System.err.println("Expected feeding program validation error: " + ex.getMessage());
        }
    }
}
