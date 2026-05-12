public class FeedingProgram {
    // Attributes
    private String feedType;
    private double quantityPerMeal; // e.g., in kg or grams


    // Constructor
    public FeedingProgram(String feedType, double quantityPerMeal, int mealsPerDay) {
        this.feedType = feedType;
        this.quantityPerMeal = quantityPerMeal;
    }

    // Methods
    public void updateSchedule(double newQuantity) {
        this.quantityPerMeal = newQuantity;
    }

    public String getFullScheduleDetails() {
        String details = "Feed Type: " + feedType + "\n" +
                         "Quantity per Meal: " + quantityPerMeal + " kg\n";
        return details; 
    }

    // Getters and Setters
    public String getFeedType() { return feedType; }
    public void setFeedType(String feedType) { this.feedType = feedType; }
    public double getQuantityPerMeal() { return quantityPerMeal; }
    public void setQuantityPerMeal(double quantityPerMeal) { this.quantityPerMeal = quantityPerMeal; }
    
}