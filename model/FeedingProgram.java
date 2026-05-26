public class FeedingProgram {
    // Attributes
    private String feedType;
    private double quantityPerMeal; 
    private int nbMealsPerDay;

    // Constructor
    public FeedingProgram(String feedType, double quantityPerMeal, int nbMealsPerDay) {
        this.feedType = feedType;
        this.quantityPerMeal = quantityPerMeal;
        this.nbMealsPerDay = nbMealsPerDay;
    }

    // Methods
    public void updateSchedule(double newQuantity, int newNbMealsPerDay) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity per meal must be a positive value.");
        }
        if (newNbMealsPerDay < 0) {
            throw new IllegalArgumentException("Number of meals per day must be a positive integer.");
        }
        quantityPerMeal = newQuantity;
        nbMealsPerDay = newNbMealsPerDay;
        
    }

    public String getFullScheduleDetails() {
        String details = "Feed Type: " + feedType + "\n" +
                         "Quantity per Meal: " + quantityPerMeal + " kg\n" +
                         "Meals per Day: " + nbMealsPerDay;
        return details; 
    }

    // Getters and Setters
    public String getFeedType() { return feedType; }
    public void setFeedType(String feedType) { this.feedType = feedType; }
    public double getQuantityPerMeal() { return quantityPerMeal; }
    public void setQuantityPerMeal(double quantityPerMeal) { this.quantityPerMeal = quantityPerMeal; }
    public int getNbMealsPerDay() { return nbMealsPerDay; }
    public void setNbMealsPerDay(int nbMealsPerDay) { this.nbMealsPerDay = nbMealsPerDay; }

}