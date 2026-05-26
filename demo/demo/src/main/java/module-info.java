module com.farm.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.farm.demo to javafx.fxml;
    exports com.farm.demo;
}