package com.farm.demo.view;

import com.farm.demo.model.*;
import com.farm.demo.service.DataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SensorMonitorView extends VBox {

    private final DataService dataService = DataService.getInstance();

    private TableView<Sensor> sensorTable;
    private ComboBox<Zone> zoneSelector;
    private ListView<String> readingHistoryList;

    private TextField readingValueField;
    private TextField minThresholdField;
    private TextField maxThresholdField;
    private ComboBox<SensorStatus> statusSelector;

    public SensorMonitorView() {
        setSpacing(15);
        setPadding(new Insets(10));

        Label title = new Label("Sensor Monitoring & Configuration");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Filtre de sélection de la zone
        HBox filterSection = createZoneSelectorSection();

        // Séparateur principal
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.60f);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Partie Gauche : Table des capteurs
        VBox leftPane = new VBox(5);
        setupSensorTable();
        VBox.setVgrow(sensorTable, Priority.ALWAYS);
        leftPane.getChildren().addAll(new Label("Registered Sensors :"), sensorTable);

        // Partie Droite : Formulaires d'action et historique
        VBox rightPane = createControlPane();

        splitPane.getItems().addAll(leftPane, rightPane);

        getChildren().addAll(title, filterSection, splitPane);

        setupSelectionBinding();
    }

    private HBox createZoneSelectorSection() {
        HBox container = new HBox(10);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        zoneSelector = new ComboBox<>();
        zoneSelector.setItems(dataService.getZones());
        zoneSelector.setPromptText("Select Zone...");
        zoneSelector.setPrefWidth(220);

        // Filtrage de la table au changement de zone
        zoneSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldZone, newZone) -> {
            if (newZone != null) {
                sensorTable.getItems().setAll(newZone.getSensors());
                readingHistoryList.getItems().clear();
            }
        });

        container.getChildren().addAll(new Label("Select Zone :"), zoneSelector);
        return container;
    }

    private void setupSensorTable() {
        sensorTable = new TableView<>();
        sensorTable.setPlaceholder(new Label("No sensors registered in this zone."));

        TableColumn<Sensor, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("uniqueCode"));

        TableColumn<Sensor, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));

        TableColumn<Sensor, SensorStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Sensor, Double> minCol = new TableColumn<>("Min");
        minCol.setCellValueFactory(new PropertyValueFactory<>("minThreshold"));

        TableColumn<Sensor, Double> maxCol = new TableColumn<>("Max");
        maxCol.setCellValueFactory(new PropertyValueFactory<>("maxThreshold"));

        TableColumn<Sensor, String> lastValCol = new TableColumn<>("Last Value");
        lastValCol.setCellValueFactory(cellData -> {
            Sensor s = cellData.getValue();
            return new SimpleStringProperty(s.getLastReadingValue()
                    .map(val -> val + " " + s.getUnitOfMeasurement())
                    .orElse("N/A"));
        });

        sensorTable.getColumns().addAll(codeCol, typeCol, statusCol, minCol, maxCol, lastValCol);
        sensorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private VBox createControlPane() {
        VBox pane = new VBox(12);

        // Section simulation
        VBox simulationBox = new VBox(5);
        simulationBox.setPadding(new Insets(10));
        simulationBox.setStyle("-fx-border-color: #ccc; -fx-background-radius: 5;");
        Label lblSim = new Label("Simulate Sensor Reading");
        lblSim.setFont(Font.font("System", FontWeight.BOLD, 12));
        readingValueField = new TextField();
        readingValueField.setPromptText("Enter numeric value...");
        Button btnAddReading = new Button("Submit Reading");
        btnAddReading.setMaxWidth(Double.MAX_VALUE);
        btnAddReading.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnAddReading.setOnAction(e -> handleAddReading());
        simulationBox.getChildren().addAll(lblSim, readingValueField, btnAddReading);

        // Section configuration
        VBox configBox = new VBox(5);
        configBox.setPadding(new Insets(10));
        configBox.setStyle("-fx-border-color: #ccc; -fx-background-radius: 5;");
        Label lblConf = new Label("Configure Thresholds & Status");
        lblConf.setFont(Font.font("System", FontWeight.BOLD, 12));

        GridPane grid = new GridPane();
        grid.setHgap(5); grid.setVgap(5);
        minThresholdField = new TextField();
        maxThresholdField = new TextField();
        statusSelector = new ComboBox<>();
        statusSelector.getItems().addAll(SensorStatus.values());

        grid.add(new Label("Min:"), 0, 0); grid.add(minThresholdField, 1, 0);
        grid.add(new Label("Max:"), 0, 1); grid.add(maxThresholdField, 1, 1);
        grid.add(new Label("Status:"), 0, 2); grid.add(statusSelector, 1, 2);

        Button btnSaveConfig = new Button("Save Parameters");
        btnSaveConfig.setMaxWidth(Double.MAX_VALUE);
        btnSaveConfig.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnSaveConfig.setOnAction(e -> handleSaveConfig());
        configBox.getChildren().addAll(lblConf, grid, btnSaveConfig);

        // Section historique
        VBox historyBox = new VBox(5);
        readingHistoryList = new ListView<>();
        readingHistoryList.setPrefHeight(120);
        historyBox.getChildren().addAll(new Label("Reading History :"), readingHistoryList);

        pane.getChildren().addAll(simulationBox, configBox, historyBox);
        return pane;
    }

    private void setupSelectionBinding() {
        sensorTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSensor, newSensor) -> {
            if (newSensor != null) {
                minThresholdField.setText(String.valueOf(newSensor.getMinThreshold()));
                maxThresholdField.setText(String.valueOf(newSensor.getMaxThreshold()));
                statusSelector.setValue(newSensor.getStatus());
                refreshHistoryList(newSensor);
            }
        });
    }

    private void refreshHistoryList(Sensor sensor) {
        readingHistoryList.getItems().clear();
        sensor.getAllReadings().forEach(reading ->
                readingHistoryList.getItems().add(reading.getTimestamp() + " : " + reading.getValue() + " " + sensor.getUnitOfMeasurement())
        );
    }

    private void handleAddReading() {
        Zone activeZone = zoneSelector.getValue();
        Sensor activeSensor = sensorTable.getSelectionModel().getSelectedItem();

        if (activeZone == null || activeSensor == null) {
            showError("Select a Zone and a Sensor first.");
            return;
        }

        try {
            double value = Double.parseDouble(readingValueField.getText().trim());
            String outcome = dataService.getFarm().addSensorReading(activeZone.getCode(), activeSensor.getUniqueCode(), value);
            showInfo("Reading Processed", outcome);

            dataService.refreshAll();
            sensorTable.refresh();
            refreshHistoryList(activeSensor);
            readingValueField.clear();

        } catch (NumberFormatException e) {
            showError("Invalid input. Please enter a valid decimal number.");
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void handleSaveConfig() {
        Zone activeZone = zoneSelector.getValue();
        Sensor activeSensor = sensorTable.getSelectionModel().getSelectedItem();

        if (activeZone == null || activeSensor == null) {
            showError("No sensor selected.");
            return;
        }

        try {
            double min = Double.parseDouble(minThresholdField.getText().trim());
            double max = Double.parseDouble(maxThresholdField.getText().trim());
            SensorStatus newStatus = statusSelector.getValue();

            dataService.getFarm().updateSensorThresholds(activeZone.getCode(), activeSensor.getUniqueCode(), min, max, null, null);
            dataService.getFarm().changeSensorStatus(activeZone.getCode(), activeSensor.getUniqueCode(), newStatus);

            showInfo("Configuration Saved", "Sensor limits and status updated successfully.");
            dataService.refreshAll();
            sensorTable.refresh();

        } catch (Exception ex) {
            showError("Failed to update config: " + ex.getMessage());
        }
    }

    private void showInfo(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }

    private void showError(String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error"); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}