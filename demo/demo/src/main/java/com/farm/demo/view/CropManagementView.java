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
import java.util.Optional;

public class CropManagementView extends VBox {

    private TableView<Crop> table;
    private DataService dataService = DataService.getInstance();
    private FilteredList<Crop> filteredCrops;

    public CropManagementView() {
        setSpacing(15); // Slightly tighter spacing
        setPadding(new Insets(10));

        Label title = new Label("Crop Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // 1. Filter Bar (Species and Zone)
        HBox filterBar = createFilterBar();

        // 2. Table
        setupTable();

        // 3. Actions
        HBox actions = createActionButtons();

        // 4. Registration Form
        VBox registrationForm = createRegistrationForm();

        getChildren().addAll(title, filterBar, table, actions, new Separator(), registrationForm);
    }

    private HBox createFilterBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField speciesSearch = new TextField();
        speciesSearch.setPromptText("Filter by Species...");

        TextField zoneSearch = new TextField();
        zoneSearch.setPromptText("Filter by Zone Code...");

        filteredCrops = new FilteredList<>(dataService.getCrops(), p -> true);

        // Combined Listener for both search fields
        java.util.function.Consumer<String> filterLogic = (val) -> {
            // Inside the filteredCrops logic:
            filteredCrops.setPredicate(crop -> {
                String sText = speciesSearch.getText().toLowerCase();
                String zText = zoneSearch.getText().toLowerCase();

                boolean matchesSpecies = crop.getSpecies().toLowerCase().contains(sText);

                // FIX: Now filters by the actual zoneId attribute
                boolean matchesZone = crop.getZoneId() != null &&
                        crop.getZoneId().toLowerCase().contains(zText);

                return matchesSpecies && (zText.isEmpty() || matchesZone);
            });
        };

        speciesSearch.textProperty().addListener((o, old, newVal) -> filterLogic.accept(newVal));
        zoneSearch.textProperty().addListener((o, old, newVal) -> filterLogic.accept(newVal));

        bar.getChildren().addAll(new Label("Species:"), speciesSearch, new Label("Zone Code:"), zoneSearch);
        return bar;
    }

    private void setupTable() {
        table = new TableView<>();
        table.setItems(filteredCrops);
        table.setPrefHeight(250);

        TableColumn<Crop, String> speciesCol = new TableColumn<>("Species");
        speciesCol.setCellValueFactory(new PropertyValueFactory<>("species"));

        // FIX: Now looks for the getZoneId() method we added to Crop.java
        TableColumn<Crop, String> zoneCol = new TableColumn<>("Zone Code");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneId"));

        TableColumn<Crop, GrowthStage> stageCol = new TableColumn<>("Stage");
        stageCol.setCellValueFactory(new PropertyValueFactory<>("currentStage"));

        TableColumn<Crop, LocalDate> plantCol = new TableColumn<>("Planted");
        plantCol.setCellValueFactory(new PropertyValueFactory<>("plantingDate"));

        TableColumn<Crop, LocalDate> harvestCol = new TableColumn<>("Harvest Date");
        harvestCol.setCellValueFactory(new PropertyValueFactory<>("expectedHarvestDate"));

        table.getColumns().addAll(speciesCol, zoneCol, stageCol, plantCol, harvestCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private HBox createActionButtons() {
        Button btnUpdateStage = new Button("Update Growth Stage");
        btnUpdateStage.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        btnUpdateStage.setOnAction(e -> {
            Crop selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openUpdateStageDialog(selected);
            } else {
                showError("Select a crop from the table.");
            }
        });

        return new HBox(10, btnUpdateStage);
    }

    private void openUpdateStageDialog(Crop crop) {
        ChoiceDialog<GrowthStage> dialog = new ChoiceDialog<>(crop.getCurrentStage(), GrowthStage.values());
        dialog.setTitle("Update Stage");
        dialog.setHeaderText("Updating: " + crop.getSpecies());
        dialog.setContentText("Select new stage:");

        dialog.showAndWait().ifPresent(newStage -> {
            crop.updateGrowthStage(newStage);
            table.refresh();
        });
    }

    private VBox createRegistrationForm() {
        VBox form = new VBox(10);
        Label lbl = new Label("Register New Crop");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        TextField speciesField = new TextField();
        ComboBox<CropType> typeBox = new ComboBox<>();
        typeBox.getItems().setAll(CropType.values());
        typeBox.setValue(CropType.VEGETABLES);

        DatePicker harvestPicker = new DatePicker(LocalDate.now().plusMonths(3));
        ComboBox<Zone> zoneBox = new ComboBox<>();
        zoneBox.getItems().setAll(dataService.getFarm().getZones().stream()
                .filter(z -> z instanceof CropZone).toList());

        grid.add(new Label("Species:"), 0, 0); grid.add(speciesField, 1, 0);
        grid.add(new Label("Type:"), 0, 1); grid.add(typeBox, 1, 1);
        grid.add(new Label("Target Zone:"), 2, 0); grid.add(zoneBox, 3, 0);
        grid.add(new Label("Harvest Date:"), 2, 1); grid.add(harvestPicker, 3, 1);

        Button btnAdd = new Button("Add Crop");
        btnAdd.setOnAction(e -> {
            try {
                Zone z = zoneBox.getValue();
                if (z == null) { showError("Select a zone."); return; }

                Crop c = new Crop(typeBox.getValue(), speciesField.getText(),
                        6.0, 7.5, GrowthStage.SOWING, 20.0, 80.0, harvestPicker.getValue());

                // FIX: Set the zone code so it can be displayed and filtered
                c.setZoneId(z.getCode());

                z.addEntity(c);
                dataService.refreshAll();

                showInfo("Crop added to " + z.getCode());
                speciesField.clear();
            } catch (Exception ex) {
                showError("Input error.");
            }
        });


        form.getChildren().addAll(lbl, grid, btnAdd);
        return form;
    }

    private void showInfo(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setHeaderText(null); alert.setContentText(msg); alert.show();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setHeaderText(null); alert.setContentText(msg); alert.show();
    }
}