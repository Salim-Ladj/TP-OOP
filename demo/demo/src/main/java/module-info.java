module com.farm.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;

    // This line allows JavaFX to use reflection to read your model classes
    opens com.farm.demo.model to javafx.base;

    opens com.farm.demo to javafx.fxml;
    exports com.farm.demo;


}