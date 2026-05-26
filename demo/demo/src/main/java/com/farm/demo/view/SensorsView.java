package com.farm.demo.view;

import com.farm.demo.model.*;
import com.farm.demo.controller.ZonesController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * JavaFX view for sensor management.
 * Displays all sensors across zones in a table with their status and readings.
 */
public class SensorsView extends VBox {
    private static final String STYLE_DARK = "-fx-base: #0a1628; -fx-control-inner-background: #0d2137;";

    public SensorsView() {
        setStyle(STYLE_DARK);
        setPadding(new Insets(15));
        setSpacing(10);
        setFillWidth(true);

        // Title
        Label title = new Label("Sensor Management");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        getChildren().add(title);

        // Stats bar
        HBox statsBar = buildStatsBar();
        getChildren().add(statsBar);

        // Sensors table
        TableView<SensorRow> table = buildSensorsTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().add(table);

        // Action buttons
        HBox actionBar = buildActionBar(table);
        getChildren().add(actionBar);
    }

    private HBox buildStatsBar() {
        HBox bar = new HBox(12);
        bar.setPrefHeight(80);
        bar.setStyle("-fx-fill: transparent;");

        // Try to load farm from model context
        bar.getChildren().addAll(
                makeStatCard("—", "Total Sensors"),
                makeStatCard("—", "Active"),
                makeStatCard("—", "Faulty/Suspended")
        );

        return bar;
    }

    private VBox makeStatCard(String value, String label) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color: #0d2137; -fx-border-radius: 8; -fx-background-color: #0d2137; -fx-background-radius: 8;");
        card.setPrefWidth(150);

        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #99aabb;");
        lblLabel.setWrapText(true);

        card.getChildren().addAll(valLabel, lblLabel);
        card.setAlignment(Pos.CENTER);
        return card;
    }

    private TableView<SensorRow> buildSensorsTable() {
        TableView<SensorRow> table = new TableView<>();
        table.setStyle("-fx-control-inner-background: #0d2137; -fx-table-cell-border-color: #1a3350;");

        TableColumn<SensorRow, String> codeCol = new TableColumn<>("Sensor Code");
        codeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().code));

        TableColumn<SensorRow, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().zone));

        TableColumn<SensorRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().type));

        TableColumn<SensorRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().status));

        TableColumn<SensorRow, String> lastReadCol = new TableColumn<>("Last Value");
        lastReadCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().lastReading));

        table.getColumns().addAll(codeCol, zoneCol, typeCol, statusCol, lastReadCol);

        // Populate from ZonesController sample data
        try {
            ZonesController controller = new ZonesController();
            for (Zone zone : controller.getAllZones()) {
                for (Sensor s : zone.getSensors()) {
                    String lastVal = s.getLastReadingValue()
                            .map(v -> String.format("%.2f %s", v, s.getUnitOfMeasurement()))
                            .orElse("No reading");
                    table.getItems().add(new SensorRow(
                            s.getUniqueCode(),
                            zone.getCode(),
                            s.getClass().getSimpleName(),
                            s.getStatus().name(),
                            lastVal
                    ));
                }
            }
        } catch (Exception e) {
            // Sample data might not be available
        }

        return table;
    }

    private HBox buildActionBar(TableView<SensorRow> table) {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(6));
        bar.setStyle("-fx-background-color: #0d2137;");
        bar.setAlignment(Pos.CENTER);

        Button suspendBtn = new Button("Suspend");
        suspendBtn.setStyle("-fx-padding: 8 18; -fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #ffc107; -fx-background-radius: 4;");
        suspendBtn.setOnAction(e -> showAlert("Selected sensor suspended (demo action)", javafx.scene.control.Alert.AlertType.INFORMATION));

        Button reactBtn = new Button("Reactivate");
        reactBtn.setStyle("-fx-padding: 8 18; -fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #00e676; -fx-background-radius: 4;");
        reactBtn.setOnAction(e -> showAlert("Selected sensor reactivated (demo action)", javafx.scene.control.Alert.AlertType.INFORMATION));

        Button faultyBtn = new Button("Mark Faulty");
        faultyBtn.setStyle("-fx-padding: 8 18; -fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #f44336; -fx-background-radius: 4;");
        faultyBtn.setOnAction(e -> showAlert("Selected sensor marked faulty (demo action)", javafx.scene.control.Alert.AlertType.INFORMATION));

        bar.getChildren().addAll(suspendBtn, reactBtn, faultyBtn);
        return bar;
    }

    private void showAlert(String msg, javafx.scene.control.Alert.AlertType type) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle("Action");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Helper class for table data
    public static class SensorRow {
        public String code, zone, type, status, lastReading;
        public SensorRow(String code, String zone, String type, String status, String lastReading) {
            this.code = code;
            this.zone = zone;
            this.type = type;
            this.status = status;
            this.lastReading = lastReading;
        }
    }
}







