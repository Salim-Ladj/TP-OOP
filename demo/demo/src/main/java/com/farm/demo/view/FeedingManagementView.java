package com.farm.demo.view;

import com.farm.demo.model.*;
import com.farm.demo.service.DataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class FeedingManagementView extends VBox {

    private TableView<Zone> table;
    private DataService dataService = DataService.getInstance();
    private FilteredList<Zone> filteredZones;

    public FeedingManagementView() {
        setSpacing(15);
        setPadding(new Insets(15));

        Label title = new Label("Feeding Schedule Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // 1. Filter
        HBox filterBar = createFilterBar();

        // 2. Table
        setupTable();

        // 3. Update Form
        VBox form = createFeedingForm();

        getChildren().addAll(title, filterBar, table, new Separator(), form);
    }

    private HBox createFilterBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        TextField search = new TextField();
        search.setPromptText("Filter by Zone Code...");

        filteredZones = new FilteredList<>(dataService.getZonesWithFeeding(), p -> true);
        search.textProperty().addListener((o, old, newVal) -> {
            filteredZones.setPredicate(zone -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return zone.getCode().toLowerCase().contains(newVal.toLowerCase());
            });
        });

        bar.getChildren().addAll(new Label("Search Zone:"), search);
        return bar;
    }

    private void setupTable() {
        table = new TableView<>();
        table.setItems(filteredZones);
        table.setPrefHeight(200);

        TableColumn<Zone, String> codeCol = new TableColumn<>("Zone Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Zone, String> feedTypeCol = new TableColumn<>("Feed Type");
        feedTypeCol.setCellValueFactory(cellData -> {
            FeedingProgram fp = getFP(cellData.getValue());
            return new SimpleStringProperty(fp != null ? fp.getFeedType() : "Not Set");
        });

        TableColumn<Zone, String> qtyCol = new TableColumn<>("Qty/Meal");
        qtyCol.setCellValueFactory(cellData -> {
            FeedingProgram fp = getFP(cellData.getValue());
            return new SimpleStringProperty(fp != null ? fp.getQuantityPerMeal() + " kg" : "-");
        });

        TableColumn<Zone, String> freqCol = new TableColumn<>("Meals/Day");
        freqCol.setCellValueFactory(cellData -> {
            FeedingProgram fp = getFP(cellData.getValue());
            return new SimpleStringProperty(fp != null ? String.valueOf(fp.getNbMealsPerDay()) : "-");
        });

        table.getColumns().addAll(codeCol, feedTypeCol, qtyCol, freqCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private VBox createFeedingForm() {
        VBox container = new VBox(10);
        Label lbl = new Label("Update Feeding Program");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        ComboBox<Zone> zoneBox = new ComboBox<>();
        zoneBox.setItems(dataService.getZonesWithFeeding());

        TextField typeField = new TextField();
        typeField.setPromptText("e.g. Corn Mix, Fish Pellets");

        TextField qtyField = new TextField();
        qtyField.setPromptText("kg per meal");

        TextField freqField = new TextField();
        freqField.setPromptText("times per day");

        grid.add(new Label("Target Zone:"), 0, 0); grid.add(zoneBox, 1, 0);
        grid.add(new Label("Feed Type:"), 0, 1); grid.add(typeField, 1, 1);
        grid.add(new Label("Quantity:"), 2, 0); grid.add(qtyField, 3, 0);
        grid.add(new Label("Frequency:"), 2, 1); grid.add(freqField, 3, 1);

        Button btnSave = new Button("Assign Program");
        btnSave.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");

        btnSave.setOnAction(e -> {
            try {
                Zone z = zoneBox.getValue();
                if (z == null) throw new Exception("Select a zone.");

                FeedingProgram fp = new FeedingProgram(
                        typeField.getText(),
                        Double.parseDouble(qtyField.getText()),
                        Integer.parseInt(freqField.getText())
                );

                // Apply to the specific zone type
                if (z instanceof LivestockZone lz) lz.setFeedingProgram(fp);
                else if (z instanceof AquacultureZone az) az.setFeedingProgram(fp);

                table.refresh();
                showInfo("Feeding program assigned to " + z.getCode());
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        container.getChildren().addAll(lbl, grid, btnSave);
        return container;
    }

    // Helper to extract FeedingProgram from generic Zone
    private FeedingProgram getFP(Zone z) {
        if (z instanceof LivestockZone lz) return lz.getFeedingProgram();
        if (z instanceof AquacultureZone az) return az.getFeedingProgram();
        return null;
    }

    private void showInfo(String msg) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        a.setHeaderText(null); a.setContentText(msg); a.show();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        a.setHeaderText(null); a.setContentText(msg); a.show();
    }
}