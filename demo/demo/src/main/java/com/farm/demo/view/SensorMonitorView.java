package com.farm.demo.view;

import com.farm.demo.model.*;
import com.farm.demo.service.DataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SensorMonitorView extends VBox {

    private final DataService dataService = DataService.getInstance();

    private TableView<Sensor> sensorTable;
    private ComboBox<Zone> zoneSelector;
    private ListView<String> readingHistoryList;

    // History Filters
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;

    // Simulation/Config fields
    private TextField readingValueField;
    private TextField minThresholdField;
    private TextField maxThresholdField;
    private ComboBox<SensorStatus> statusSelector;

    public SensorMonitorView() {
        setSpacing(15);
        setPadding(new Insets(10));

        Label title = new Label("IoT Sensor Monitoring & Dashboard");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // 1. Zone Selection
        HBox filterSection = createZoneSelectorSection();

        // 2. Main Body
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.60f);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        VBox leftPane = new VBox(10);
        setupSensorTable();
        VBox.setVgrow(sensorTable, Priority.ALWAYS);

        // Form to Add New Sensor
        VBox addSensorForm = createAddSensorForm();
        leftPane.getChildren().addAll(new Label("Registered Sensors:"), sensorTable, new Separator(), addSensorForm);

        VBox rightPane = createControlPane();
        splitPane.getItems().addAll(leftPane, rightPane);

        getChildren().addAll(title, filterSection, splitPane);
        setupSelectionBinding();
    }

    private HBox createZoneSelectorSection() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        zoneSelector = new ComboBox<>();
        zoneSelector.setItems(dataService.getZones());
        zoneSelector.setPromptText("Select Zone to View Dashboard...");
        zoneSelector.setPrefWidth(250);

        zoneSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                sensorTable.getItems().setAll(newV.getSensors());
                readingHistoryList.getItems().clear();
            }
        });

        container.getChildren().addAll(new Label("Active Zone:"), zoneSelector);
        return container;
    }

    private void setupSensorTable() {
        sensorTable = new TableView<>();
        sensorTable.setPlaceholder(new Label("No sensors in this zone."));

        TableColumn<Sensor, String> codeCol = new TableColumn<>("UID");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("uniqueCode"));

        // NEW: Color-coded status indicator column
        TableColumn<Sensor, String> healthCol = new TableColumn<>("Health");
        healthCol.setCellFactory(col -> new TableCell<>() {
            private final Circle circle = new Circle(8);
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Sensor s = getTableRow().getItem();
                    s.getLastReadingValue().ifPresentOrElse(val -> {
                        Object[] status = s.getReadingStatus(val);
                        String level = (String) status[1];
                        if (level.equals("Critical")) circle.setFill(Color.RED);
                        else if (level.equals("Warning")) circle.setFill(Color.ORANGE);
                        else circle.setFill(Color.LIMEGREEN);
                    }, () -> circle.setFill(Color.LIGHTGRAY));
                    setGraphic(circle);
                }
            }
        });

        TableColumn<Sensor, SensorStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Sensor, String> lastValCol = new TableColumn<>("Current Reading");
        lastValCol.setCellValueFactory(cellData -> {
            Sensor s = cellData.getValue();
            return new SimpleStringProperty(s.getLastReadingValue()
                    .map(val -> val + " " + s.getUnitOfMeasurement())
                    .orElse("No Data"));
        });

        sensorTable.getColumns().addAll(codeCol, healthCol, statusCol, lastValCol);
        sensorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private VBox createAddSensorForm() {
        VBox container = new VBox(8);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #eee; -fx-background-radius: 5;");

        Label lbl = new Label("Provision New Sensor");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(5);

        TextField idInput = new TextField(); idInput.setPromptText("UID (e.g. SN-99)");
        ComboBox<String> typeInput = new ComboBox<>();
        typeInput.getItems().addAll("Temperature", "Humidity", "Soil pH", "Dissolved Oxygen");
        typeInput.setValue("Temperature");

        TextField minIn = new TextField("10"); TextField maxIn = new TextField("40");

        grid.add(new Label("UID:"), 0, 0); grid.add(idInput, 1, 0);
        grid.add(new Label("Type:"), 2, 0); grid.add(typeInput, 3, 0);
        grid.add(new Label("Min:"), 0, 1); grid.add(minIn, 1, 1);
        grid.add(new Label("Max:"), 2, 1); grid.add(maxIn, 3, 1);

        Button btnAdd = new Button("Register Sensor");
        btnAdd.setOnAction(e -> {
            Zone z = zoneSelector.getValue();
            if (z == null) { showError("Select a zone first!"); return; }
            try {
                // Creating a biometric sensor as the standard concrete type
                Sensor s = new BiometricSensor(idInput.getText(), z.getCode(),
                        Double.parseDouble(minIn.getText()), Double.parseDouble(maxIn.getText()), "units");
                z.addSensor(s);
                sensorTable.getItems().setAll(z.getSensors());
                dataService.saveToDisk();
                showInfo("Success", "Sensor " + s.getUniqueCode() + " added to " + z.getCode());
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        container.getChildren().addAll(lbl, grid, btnAdd);
        return container;
    }

    private VBox createControlPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(10));

        // 1. Simulation Box
        VBox simBox = new VBox(5);
        readingValueField = new TextField();
        readingValueField.setPromptText("Value to inject...");
        Button btnSim = new Button("Inject Reading");
        btnSim.setMaxWidth(Double.MAX_VALUE);
        btnSim.setOnAction(e -> handleAddReading());
        simBox.getChildren().addAll(new Label("Reading Simulation"), readingValueField, btnSim);

        // 2. Configuration Box
        VBox confBox = new VBox(5);
        minThresholdField = new TextField();
        maxThresholdField = new TextField();
        statusSelector = new ComboBox<>();
        statusSelector.getItems().addAll(SensorStatus.values());
        Button btnSave = new Button("Update Config");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> handleSaveConfig());
        confBox.getChildren().addAll(new Label("Thresholds & Status"), minThresholdField, maxThresholdField, statusSelector, btnSave);

        // 3. Filterable History Box
        VBox histBox = new VBox(8);
        startDatePicker = new DatePicker(LocalDate.now().minusDays(1));
        endDatePicker = new DatePicker(LocalDate.now());
        Button btnFilter = new Button("Filter History");
        btnFilter.setMaxWidth(Double.MAX_VALUE);
        btnFilter.setOnAction(e -> {
            Sensor s = sensorTable.getSelectionModel().getSelectedItem();
            if (s != null) refreshHistoryList(s);
        });

        readingHistoryList = new ListView<>();
        histBox.getChildren().addAll(new Label("Reading History Range:"), startDatePicker, endDatePicker, btnFilter, readingHistoryList);

        pane.getChildren().addAll(simBox, new Separator(), confBox, new Separator(), histBox);
        return pane;
    }

    private void refreshHistoryList(Sensor sensor) {
        readingHistoryList.getItems().clear();
        LocalDateTime start = startDatePicker.getValue().atStartOfDay();
        LocalDateTime end = endDatePicker.getValue().atTime(LocalTime.MAX);

        sensor.getReadingsHistory(start, end).forEach(r ->
                readingHistoryList.getItems().add(r.getTimestamp().toLocalTime() + " -> " + r.getValue())
        );
    }

    private void setupSelectionBinding() {
        sensorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                minThresholdField.setText(String.valueOf(newS.getMinThreshold()));
                maxThresholdField.setText(String.valueOf(newS.getMaxThreshold()));
                statusSelector.setValue(newS.getStatus());
                refreshHistoryList(newS);
            }
        });
    }

    // Logic for Add Reading and Save Config remains the same as your starting file...
    private void handleAddReading() {
        Sensor s = sensorTable.getSelectionModel().getSelectedItem();
        Zone z = zoneSelector.getValue();
        if (s == null || z == null) return;
        try {
            double val = Double.parseDouble(readingValueField.getText());
            dataService.getFarm().addSensorReading(z.getCode(), s.getUniqueCode(), val);
            sensorTable.refresh();
            refreshHistoryList(s);
            readingValueField.clear();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private void handleSaveConfig() {
        Sensor s = sensorTable.getSelectionModel().getSelectedItem();
        Zone z = zoneSelector.getValue();
        if (s == null || z == null) return;
        try {
            double min = Double.parseDouble(minThresholdField.getText());
            double max = Double.parseDouble(maxThresholdField.getText());
            dataService.getFarm().updateSensorThresholds(z.getCode(), s.getUniqueCode(), min, max, null, null);
            dataService.getFarm().changeSensorStatus(z.getCode(), s.getUniqueCode(), statusSelector.getValue());
            sensorTable.refresh();
            showInfo("Updated", "Configuration saved.");
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private void showInfo(String t, String c) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.show();
    }

    private void showError(String c) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(c); a.show();
    }
}