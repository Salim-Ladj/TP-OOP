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
        this.date = LocalDate.now(); 
     }

    // Methods
    public String getRecord() { 
        String formattedDate = date.toString(); 
        String formattedAmount = String.format("%.2f", amount);
        String Record = " Date :"+formattedDate + " \n Amount : " + formattedAmount + " " + unit + " \n Description: " + description + " \n Type: " + type;
        return Record; 
    }
    public void updateRecord(double value, String note) { 
        if (value < 0) {
            throw new IllegalArgumentException("Amount must be a positive value.");
        }
        this.amount = value;
        this.description = note;
        this.date = LocalDate.now(); 
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