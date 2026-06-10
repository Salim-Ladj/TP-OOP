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

public class ZoneManagementView extends VBox {

    private TableView<Zone> table;
    private DataService dataService = DataService.getInstance();

    public ZoneManagementView() {
        setSpacing(20);
        setPadding(new Insets(10));

        // 1. Title
        Label title = new Label("Zone Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // 2. The Table
        setupTable();

        // 3. Action Buttons (Deactivate / Reactivate)
        HBox actions = setupActionButtons();

        // 4. Registration Form (Add New Zone)
        VBox form = setupAddZoneForm();

        getChildren().addAll(title, table, actions, new Separator(), form);
    }

    private void setupTable() {
        table = new TableView<>();
        table.setItems(dataService.getZones());

        TableColumn<Zone, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Zone, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Custom column to show the Zone Type (Class name)
        TableColumn<Zone, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));

        TableColumn<Zone, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(codeCol, nameCol, typeCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private HBox setupActionButtons() {
        Button btnToggle = new Button("Toggle Active/Suspended");
        btnToggle.setOnAction(e -> {
            Zone selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getStatus() == ZoneStatus.ACTIVE) {
                    selected.suspend();
                } else {
                    selected.reactivate();
                }
                table.refresh(); // Refresh visual state
            }
        });

        HBox hbox = new HBox(10, btnToggle);
        return hbox;
    }

    private VBox setupAddZoneForm() {
        VBox container = new VBox(10);
        Label formTitle = new Label("Register New Zone");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField codeField = new TextField();
        TextField nameField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Crop Zone", "Livestock Zone", "Aquaculture Zone");
        typeBox.setValue("Crop Zone");

        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Type:"), 2, 0);
        grid.add(typeBox, 3, 0);

        Button btnAdd = new Button("Add Zone");
        btnAdd.setOnAction(e -> {
            String code = codeField.getText();
            String name = nameField.getText();
            String type = typeBox.getValue();

            if (code.isEmpty() || name.isEmpty()) {
                showError("Please fill in all fields.");
                return;
            }

            Zone newZone;
            switch (type) {
                case "Livestock Zone" -> newZone = new LivestockZone(code, name);
                case "Aquaculture Zone" -> newZone = new AquacultureZone(code, name);
                default -> newZone = new CropZone(code, name);
            }

            dataService.getFarm().addZone(newZone);
            dataService.refreshAll(); // Update the ObservableList

            codeField.clear();
            nameField.clear();
        });

        container.getChildren().addAll(formTitle, grid, btnAdd);
        return container;
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}