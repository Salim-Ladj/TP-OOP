public class Student1Main {
    public static void main(String[] args) {
        CropZone cropZone = new CropZone("CZ-01", "North Field");
        LivestockZone livestockZone = new LivestockZone("LZ-01", "East Barn");
        AquacultureZone aquacultureZone = new AquacultureZone("AZ-01", "Pond A");

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

        livestockZone.addEntity(new Animal(AnimalType.RUMINANT, "AN-001", "Cow", 4, 450.0, HealthStatus.HEALTHY) {});
        aquacultureZone.addEntity(new Animal(AnimalType.AQUATIC, "AN-002", "Tilapia", 1, 2.5, HealthStatus.HEALTHY) {});

        livestockZone.setFeedingProgram(new FeedingProgram("Grass Mix", 5.0, 2));
        aquacultureZone.setFeedingProgram(new FeedingProgram("Pellets", 1.5, 3));

        ProductionRecord record = new ProductionRecord("kg", ProductionType.CROP_YIELD);
        record.updateRecord(1500.0, "Harvested wheat from North Field");

        cropZone.display();
        System.out.println();
        livestockZone.display();
        System.out.println();
        aquacultureZone.display();
        System.out.println();
        System.out.println(record.getRecord());
    }
}
