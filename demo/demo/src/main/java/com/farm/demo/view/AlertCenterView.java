package com.farm.demo.view;

import com.farm.demo.model.SeverityLevel; // Import specific models
import com.farm.demo.model.Zone;
// We do NOT import com.farm.demo.model.Alert to avoid conflict
import com.farm.demo.service.DataService;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AlertCenterView extends VBox {

    private final DataService dataService = DataService.getInstance();

    // Use fully qualified names for your model Alert
    private TableView<com.farm.demo.model.Alert> alertsTable;
    private FilteredList<com.farm.demo.model.Alert> filteredAlerts;

    private ComboBox<String> zoneFilter;
    private ComboBox<SeverityLevel> severityFilter;

    public AlertCenterView() {
        setSpacing(20);
        setPadding(new Insets(10));

        Label title = new Label("Incident & Alert Control Center");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        HBox filterBar = createFilterBar();

        HBox body = new HBox(15);
        VBox.setVgrow(body, Priority.ALWAYS);

        setupAlertsTable();
        HBox.setHgrow(alertsTable, Priority.ALWAYS);

        VBox actionPane = createActionPane();

        body.getChildren().addAll(alertsTable, actionPane);
        getChildren().addAll(title, filterBar, body);
    }

    private HBox createFilterBar() {
        HBox container = new HBox(10);
        container.setPadding(new Insets(5));
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        filteredAlerts = new FilteredList<>(dataService.getActiveAlerts(), p -> true);

        zoneFilter = new ComboBox<>();
        zoneFilter.getItems().add("All");
        dataService.getZones().forEach(z -> zoneFilter.getItems().add(z.getCode()));
        zoneFilter.setValue("All");

        severityFilter = new ComboBox<>();
        severityFilter.getItems().addAll(SeverityLevel.values());

        Button btnClear = new Button("Clear Filters");
        btnClear.setOnAction(e -> {
            zoneFilter.setValue("All");
            severityFilter.getSelectionModel().clearSelection();
            filteredAlerts.setPredicate(p -> true);
        });

        zoneFilter.valueProperty().addListener((o, old, newVal) -> applyFiltering());
        severityFilter.valueProperty().addListener((o, old, newVal) -> applyFiltering());

        container.getChildren().addAll(
                new Label("Zone:"), zoneFilter,
                new Label("Severity:"), severityFilter,
                btnClear
        );
        return container;
    }

    private void applyFiltering() {
        String selectedZone = zoneFilter.getValue();
        SeverityLevel selectedSeverity = severityFilter.getValue();

        filteredAlerts.setPredicate(alert -> {
            boolean matchesZone = (selectedZone == null || selectedZone.equals("All") || alert.getZoneId().equals(selectedZone));
            boolean matchesSeverity = (selectedSeverity == null || alert.getSeverityLevel() == selectedSeverity);
            return matchesZone && matchesSeverity;
        });
    }

    private void setupAlertsTable() {
        alertsTable = new TableView<>();
        alertsTable.setItems(filteredAlerts);
        alertsTable.setPlaceholder(new Label("No active alerts. Operational status normal."));

        // Use fully qualified model path for TableColumn types
        TableColumn<com.farm.demo.model.Alert, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("alertId"));
        idCol.setPrefWidth(50);

        TableColumn<com.farm.demo.model.Alert, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneId"));

        TableColumn<com.farm.demo.model.Alert, String> sensorCol = new TableColumn<>("Sensor");
        sensorCol.setCellValueFactory(new PropertyValueFactory<>("sensorUniqueCode"));

        TableColumn<com.farm.demo.model.Alert, Double> valCol = new TableColumn<>("Value");
        valCol.setCellValueFactory(new PropertyValueFactory<>("readingValue"));

        TableColumn<com.farm.demo.model.Alert, SeverityLevel> sevCol = new TableColumn<>("Severity");
        sevCol.setCellValueFactory(new PropertyValueFactory<>("severityLevel"));

        sevCol.setCellFactory(col -> new TableCell<com.farm.demo.model.Alert, SeverityLevel>() {
            @Override
            protected void updateItem(SeverityLevel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item.name());
                    if (item == SeverityLevel.CRITICAL) {
                        setStyle("-fx-text-fill: white; -fx-background-color: #d9534f; -fx-font-weight: bold;");
                    } else if (item == SeverityLevel.WARNING) {
                        setStyle("-fx-text-fill: black; -fx-background-color: #f0ad4e; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: white; -fx-background-color: #5cb85c;");
                    }
                }
            }
        });

        TableColumn<com.farm.demo.model.Alert, String> msgCol = new TableColumn<>("Description Message");
        msgCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        msgCol.setPrefWidth(220);

        alertsTable.getColumns().addAll(idCol, zoneCol, sensorCol, valCol, sevCol, msgCol);
        alertsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private VBox createActionPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-border-color: #ccc; -fx-background-radius: 5;");
        pane.setPrefWidth(200);

        Label lblTitle = new Label("Alert Actions");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        Button btnAcknowledge = new Button("Acknowledge");
        btnAcknowledge.setMaxWidth(Double.MAX_VALUE);
        btnAcknowledge.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");
        btnAcknowledge.setOnAction(e -> {
            com.farm.demo.model.Alert selected = alertsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String outcome = dataService.getFarm().acknowledgeAlert(selected.getAlertId());
                showInfo("Alert Acknowledged", outcome);
                dataService.refreshAll();
            } else {
                showError("Please select an alert from the table.");
            }
        });

        Button btnDismiss = new Button("Dismiss");
        btnDismiss.setMaxWidth(Double.MAX_VALUE);
        btnDismiss.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
        btnDismiss.setOnAction(e -> {
            com.farm.demo.model.Alert selected = alertsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String outcome = dataService.getFarm().dismissAlert(selected.getAlertId());
                showInfo("Alert Dismissed", outcome);
                dataService.refreshAll();
            } else {
                showError("Please select an alert from the table.");
            }
        });

        pane.getChildren().addAll(lblTitle, btnAcknowledge, btnDismiss);
        return pane;
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