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

public class CropManagementView extends VBox {

    private TableView<Crop> table;
    private DataService dataService = DataService.getInstance();
    private FilteredList<Crop> filteredCrops;

    public CropManagementView() {
        setSpacing(15);
        setPadding(new Insets(10));

        Label title = new Label("Crop Management & Reporting");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Initialize FilteredList first so setupTable can use it
        filteredCrops = new FilteredList<>(dataService.getCrops(), p -> true);

        HBox filterBar = createFilterBar();
        setupTable();

        HBox actions = createActionButtons();
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

        // Logic for filtering
        java.util.function.Consumer<String> filterLogic = (val) -> {
            filteredCrops.setPredicate(crop -> {
                String sText = speciesSearch.getText().toLowerCase();
                String zText = zoneSearch.getText().toLowerCase();

                boolean matchesSpecies = crop.getSpecies().toLowerCase().contains(sText);
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
            if (selected != null) openUpdateStageDialog(selected);
            else showError("Select a crop from the table.");
        });

        Button btnReport = new Button("Generate Zone Status Report");
        btnReport.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        btnReport.setOnAction(e -> openZoneReportDialog());

        return new HBox(10, btnUpdateStage, btnReport);
    }

    private void openZoneReportDialog() {
        ChoiceDialog<Zone> dialog = new ChoiceDialog<>();
        dialog.getItems().addAll(dataService.getFarm().getZones().stream().filter(z -> z instanceof CropZone).toList());
        dialog.setTitle("Zone Report");
        dialog.setHeaderText("Generate Status Report");
        dialog.setContentText("Choose a Crop Zone:");

        dialog.showAndWait().ifPresent(zone -> {
            CropZone cz = (CropZone) zone;
            StringBuilder report = new StringBuilder("Status Report for " + cz.getName() + " (" + cz.getCode() + ")\n");
            report.append("==========================================\n\n");

            if (cz.getCrops().isEmpty()) {
                report.append("No crops currently registered in this zone.");
            } else {
                for (Crop c : cz.getCrops()) {
                    report.append("Species: ").append(c.getSpecies()).append("\n")
                            .append("Stage: ").append(c.getCurrentStage()).append("\n")
                            .append("Harvest: ").append(c.getExpectedHarvestDate()).append("\n")
                            .append("Optimal pH: ").append(c.getMinPH()).append("-").append(c.getMaxPH()).append("\n")
                            .append("------------------------------------------\n");
                }
            }

            TextArea textArea = new TextArea(report.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);

            javafx.scene.control.Alert reportAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            reportAlert.getDialogPane().setContent(textArea);
            reportAlert.setTitle("Crop Status Report");
            reportAlert.setHeaderText(null);
            reportAlert.show();
        });
    }

    private VBox createRegistrationForm() {
        VBox form = new VBox(10);
        Label lbl = new Label("Register New Crop with Soil Requirements");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        TextField speciesField = new TextField();
        ComboBox<CropType> typeBox = new ComboBox<>();
        typeBox.getItems().setAll(CropType.values());
        DatePicker harvestPicker = new DatePicker(LocalDate.now().plusMonths(3));
        ComboBox<Zone> zoneBox = new ComboBox<>();
        zoneBox.getItems().setAll(dataService.getFarm().getZones().stream().filter(z -> z instanceof CropZone).toList());

        TextField minPH = new TextField("6.0"); minPH.setPrefWidth(50);
        TextField maxPH = new TextField("7.5"); maxPH.setPrefWidth(50);
        TextField minMoist = new TextField("20.0"); minMoist.setPrefWidth(50);
        TextField maxMoist = new TextField("60.0"); maxMoist.setPrefWidth(50);

        grid.add(new Label("Species:"), 0, 0); grid.add(speciesField, 1, 0);
        grid.add(new Label("Type:"), 0, 1); grid.add(typeBox, 1, 1);
        grid.add(new Label("Target Zone:"), 0, 2); grid.add(zoneBox, 1, 2);

        grid.add(new Label("Expected Harvest:"), 2, 0); grid.add(harvestPicker, 3, 0);
        grid.add(new Label("pH Range (Min/Max):"), 2, 1);
        grid.add(new HBox(5, minPH, new Label("-"), maxPH), 3, 1);
        grid.add(new Label("Moisture % (Min/Max):"), 2, 2);
        grid.add(new HBox(5, minMoist, new Label("-"), maxMoist), 3, 2);

        Button btnAdd = new Button("Register & Add Crop");
        btnAdd.setOnAction(e -> {
            try {
                Zone z = zoneBox.getValue();
                Crop c = new Crop(
                        typeBox.getValue(),
                        speciesField.getText(),
                        Double.parseDouble(minPH.getText()),
                        Double.parseDouble(maxPH.getText()),
                        GrowthStage.SOWING,
                        Double.parseDouble(minMoist.getText()),
                        Double.parseDouble(maxMoist.getText()),
                        harvestPicker.getValue()
                );
                c.setZoneId(z.getCode());
                z.addEntity(c);
                dataService.refreshAll();
                showInfo("Crop Registered Successfully.");
            } catch (Exception ex) {
                showError("Input Error: Ensure all numbers are valid.");
            }
        });

        form.getChildren().addAll(lbl, grid, btnAdd);
        return form;
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

    private void showInfo(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setHeaderText(null); alert.setContentText(msg); alert.show();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setHeaderText(null); alert.setContentText(msg); alert.show();
    }
}