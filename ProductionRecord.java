import java.time.LocalDate;

public class ProductionRecord {
    // Attributes
    private double amount;
    private String unit; // "Liters", "kg", "Units"
    private LocalDate date;
    private String description;
    private ProductionType type; 

    // Constructor
    public ProductionRecord(String unit, ProductionType type) {
        this.unit = unit;
        this.type = type;
        this.date = LocalDate.now(); // Default to current date
     }

    // Methods
    public void record(double value, String note) { 
        this.amount = value;
        this.description = note;
    }
    public String getRecord() { 
        String formattedDate = date.toString(); 
        String formattedAmount = String.format("%.2f", amount);
        String Record = " Date :"+formattedDate + " \n Amount : " + formattedAmount + " " + unit + " \n Description: " + description + " \n Type: " + type;
        return Record; // Placeholder for formatted string
    }
    // Getters and Setters
    public double getAmount() {
        return amount;
    }
    public String getUnit() {
        return unit;
    }
    public LocalDate getDate() {
        return date;
    }
    public String getDescription() {
        return description;
    }
    public ProductionType getType() {
        return type;
    }
}