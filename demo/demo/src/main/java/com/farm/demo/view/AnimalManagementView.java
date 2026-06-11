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

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnimalManagementView extends VBox {

    private TableView<Animal> table;
    private DataService dataService = DataService.getInstance();
    private FilteredList<Animal> filteredData;

    public AnimalManagementView() {
        setSpacing(20);
        setPadding(new Insets(10));

        Label title = new Label("Animal Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // 1. Filter Bar
        HBox filterBar = createFilterBar();

        // 2. Table
        setupTable();

        // 3. Health Actions
        HBox healthActions = createHealthActions();

        // 4. Registration Form
        VBox registrationForm = createRegistrationForm();

        getChildren().addAll(title, filterBar, table, healthActions, new Separator(), registrationForm);
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(5, 0, 5, 0));

        TextField zoneFilterField = new TextField();
        zoneFilterField.setPromptText("Enter Zone Code (e.g. LZ-01)");
        zoneFilterField.setPrefWidth(250);

        // Logic for filtering
        filteredData = new FilteredList<>(dataService.getAnimals(), p -> true);
        zoneFilterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(animal -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                if (animal.getZoneId() != null && animal.getZoneId().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        filterBar.getChildren().addAll(new Label("Filter by Zone Code:"), zoneFilterField);
        return filterBar;
    }

    private void setupTable() {
        table = new TableView<>();
        // Bind table to the filtered list
        table.setItems(filteredData);

        TableColumn<Animal, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("uniqueNumber"));

        TableColumn<Animal, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneId"));

        TableColumn<Animal, String> speciesCol = new TableColumn<>("Species");
        speciesCol.setCellValueFactory(new PropertyValueFactory<>("species"));

        TableColumn<Animal, AnimalType> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Animal, Double> weightCol = new TableColumn<>("Weight (kg)");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));

        TableColumn<Animal, HealthStatus> healthCol = new TableColumn<>("Health Status");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("health"));

        table.getColumns().addAll(idCol, zoneCol, speciesCol, typeCol, weightCol, healthCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private HBox createHealthActions() {
        Button btnLogHealth = new Button("Log Health Event / Weight Change");
        btnLogHealth.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnLogHealth.setOnAction(e -> {
            Animal selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Please select an animal first.");
                return;
            }
            openHealthLogDialog(selected);
        });

        Button btnHistory = new Button("View Full Health History");
        btnHistory.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnHistory.setOnAction(e -> {
            Animal selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showHealthHistoryDialog(selected);
            } else {
                showError("Please select an animal to view history.");
            }
        });

        return new HBox(10, btnLogHealth, btnHistory);
    }

    private void showHealthHistoryDialog(Animal animal) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Health History: " + animal.getSpecies() + " (" + animal.getId() + ")");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(500);

        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        Map<HealthStatus, List<String>> historyMap = animal.getHealthHistory();

        for (HealthStatus status : HealthStatus.values()) {
            Label statusLabel = new Label("Category: " + status);
            statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            ListView<String> listView = new ListView<>();
            List<String> logs = historyMap.get(status);

            if (logs == null || logs.isEmpty()) {
                listView.getItems().add("No records found.");
            } else {
                listView.getItems().addAll(logs);
            }
            listView.setPrefHeight(100);

            container.getChildren().addAll(statusLabel, listView);
        }

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    private VBox createRegistrationForm() {
        VBox form = new VBox(10);
        Label lbl = new Label("Register New Animal");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField idField = new TextField();
        TextField speciesField = new TextField();
        TextField weightField = new TextField();

        ComboBox<AnimalType> typeBox = new ComboBox<>();
        typeBox.getItems().setAll(AnimalType.values());
        typeBox.setValue(AnimalType.RUMINANT);

        // NEW: Health Status selection for registration
        ComboBox<HealthStatus> healthStatusBox = new ComboBox<>();
        healthStatusBox.getItems().setAll(HealthStatus.values());
        healthStatusBox.setValue(HealthStatus.HEALTHY);

        ComboBox<Zone> zoneBox = new ComboBox<>();
        zoneBox.getItems().setAll(dataService.getFarm().getZones().stream()
                .filter(z -> z instanceof LivestockZone || z instanceof AquacultureZone)
                .toList());

        grid.add(new Label("ID:"), 0, 0); grid.add(idField, 1, 0);
        grid.add(new Label("Species:"), 0, 1); grid.add(speciesField, 1, 1);
        grid.add(new Label("Type:"), 2, 0); grid.add(typeBox, 3, 0);
        grid.add(new Label("Weight:"), 2, 1); grid.add(weightField, 3, 1);
        grid.add(new Label("Target Zone:"), 0, 2); grid.add(zoneBox, 1, 2);

        // Add the Health Status box to the grid
        grid.add(new Label("Initial Health:"), 2, 2); grid.add(healthStatusBox, 3, 2);

        Button btnAdd = new Button("Register Animal");
        btnAdd.setOnAction(e -> {
            try {
                Zone selectedZone = zoneBox.getValue();
                if (selectedZone == null) {
                    showError("Please select a target zone.");
                    return;
                }

                // Pass healthStatusBox.getValue() to the constructor
                Animal a = new Animal(
                        typeBox.getValue(),
                        idField.getText(),
                        speciesField.getText(),
                        0,
                        Double.parseDouble(weightField.getText()),
                        healthStatusBox.getValue()
                );

                // Set the Zone ID so the filter works
                a.setZoneId(selectedZone.getCode());

                selectedZone.addEntity(a);
                dataService.refreshAll();

                // Clear fields
                idField.clear();
                speciesField.clear();
                weightField.clear();
                showInfo("Animal registered successfully as " + healthStatusBox.getValue());
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(lbl, grid, btnAdd);
        return form;
    }

    private void openHealthLogDialog(Animal animal) {
        // 1. Create the custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Health & Weight");
        dialog.setHeaderText("Updating records for: " + animal.getSpecies() + " (" + animal.getId() + ")");

        // 2. Set the button types (OK and Cancel)
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 3. Create the layout and fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<HealthStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().setAll(HealthStatus.values());
        statusCombo.setValue(animal.getHealth()); // Default to current status

        TextField weightField = new TextField(String.valueOf(animal.getWeight()));
        weightField.setPromptText("New Weight");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description of event");

        grid.add(new Label("Health Status:"), 0, 0);
        grid.add(statusCombo, 1, 0);
        grid.add(new Label("Current Weight (kg):"), 0, 1);
        grid.add(weightField, 1, 1);
        grid.add(new Label("Event Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // 4. Process the result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double newWeight = Double.parseDouble(weightField.getText());
                HealthStatus newStatus = statusCombo.getValue();
                String desc = descriptionField.getText();

                // Update the animal object properties
                animal.setHealth(newStatus);
                animal.setWeight(newWeight);

                // Log the event in the history
                // We use the current age as your model requires age
                animal.logHealthEvent(desc, newWeight, animal.getAge());

                // Refresh the UI
                table.refresh();
                showInfo("Animal records updated successfully.");

            } catch (NumberFormatException e) {
                showError("Invalid weight. Please enter a numeric value.");
            } catch (Exception ex) {
                showError("Update failed: " + ex.getMessage());
            }
        }
    }

    private void showInfo(String msg) {
        // We use the fully qualified name to avoid conflict with your Model's Alert class
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}