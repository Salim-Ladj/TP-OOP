package com.farm.demo.view;

import com.farm.demo.model.*;
import com.farm.demo.service.DataService;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDate;

public class ProductionManagementView extends VBox {

    private TableView<ProductionRecord> table;
    private DataService dataService = DataService.getInstance();
    private FilteredList<ProductionRecord> filteredRecords;

    public ProductionManagementView() {
        setSpacing(15);
        setPadding(new Insets(15));

        Label title = new Label("Production & Yield Tracking");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // 1. Filter Bar
        HBox filterBar = createFilterBar();

        // 2. Table
        setupTable();

        // 3. Form to Add/Update Record
        VBox form = createProductionForm();

        getChildren().addAll(title, filterBar, table, new Separator(), form);
    }

    private HBox createFilterBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Enter Zone Code (e.g. CZ-01)");
        searchField.setPrefWidth(250);

        // Initialize FilteredList with the data from the service
        filteredRecords = new FilteredList<>(dataService.getProductionRecords(), p -> true);

        // Add Listener to filter as user types
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredRecords.setPredicate(record -> {
                // If filter text is empty, display all records
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Match against the Zone Code
                if (record.getZoneCode() != null && record.getZoneCode().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match
            });
        });

        bar.getChildren().addAll(new Label("Filter by Zone:"), searchField);
        return bar;
    }

    private void setupTable() {
        table = new TableView<>();
        // Bind table to the FilteredList instead of the raw list
        table.setItems(filteredRecords);
        table.setPrefHeight(200);

        TableColumn<ProductionRecord, String> zoneCol = new TableColumn<>("Zone Code");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneCode"));

        TableColumn<ProductionRecord, ProductionType> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("productionType"));

        TableColumn<ProductionRecord, Double> valCol = new TableColumn<>("Amount");
        valCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        TableColumn<ProductionRecord, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductionRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(zoneCol, typeCol, valCol, unitCol, dateCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private VBox createProductionForm() {
        VBox container = new VBox(10);
        Label lbl = new Label("Record Production for a Zone");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        ComboBox<Zone> zoneBox = new ComboBox<>();
        zoneBox.getItems().setAll(dataService.getZones());

        ComboBox<ProductionType> typeBox = new ComboBox<>();
        typeBox.getItems().setAll(ProductionType.values());

        TextField amountField = new TextField();
        TextField unitField = new TextField();

        grid.add(new Label("Select Zone:"), 0, 0); grid.add(zoneBox, 1, 0);
        grid.add(new Label("Type:"), 0, 1); grid.add(typeBox, 1, 1);
        grid.add(new Label("Amount:"), 2, 0); grid.add(amountField, 3, 0);
        grid.add(new Label("Unit:"), 2, 1); grid.add(unitField, 3, 1);

        Button btnSave = new Button("Save Production Record");
        btnSave.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        btnSave.setOnAction(e -> {
            try {
                Zone selectedZone = zoneBox.getValue();
                if (selectedZone == null) throw new Exception("Please select a zone.");

                double amount = Double.parseDouble(amountField.getText());

                ProductionRecord record = new ProductionRecord(
                        selectedZone.getCode(),
                        typeBox.getValue(),
                        amount,
                        unitField.getText(),
                        LocalDate.now()
                );

                selectedZone.setProductionRecord(record);

                // Refresh data to show new record in table
                dataService.refreshAll();
                table.setItems(dataService.getProductionRecords());

                // Update the filtered reference
                filteredRecords = new FilteredList<>(dataService.getProductionRecords(), p -> true);
                table.setItems(filteredRecords);

                showInfo("Success", "Production record updated.");

            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        container.getChildren().addAll(lbl, grid, btnSave);
        return container;
    }

    private void showInfo(String title, String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.show();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error"); alert.setHeaderText(null); alert.setContentText(msg); alert.show();
    }
}