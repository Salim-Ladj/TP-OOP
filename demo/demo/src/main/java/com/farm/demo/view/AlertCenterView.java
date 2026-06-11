package com.farm.demo.view;

import com.farm.demo.model.SeverityLevel;
import com.farm.demo.model.Zone;
import com.farm.demo.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AlertCenterView extends VBox {

    private final DataService dataService = DataService.getInstance();

    private TableView<com.farm.demo.model.Alert> alertsTable;
    private FilteredList<com.farm.demo.model.Alert> filteredAlerts;

    private ComboBox<String> zoneFilter;
    private ComboBox<SeverityLevel> severityFilter;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private CheckBox showHistoryBtn; // Requirement: Browse history

    public AlertCenterView() {
        setSpacing(15);
        setPadding(new Insets(10));

        Label title = new Label("Incident Control & Alert History");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        // 1. Enhanced Filter Bar
        VBox filters = createAdvancedFilterBar();

        // 2. Table Area
        setupAlertsTable();
        VBox.setVgrow(alertsTable, Priority.ALWAYS);

        // 3. Action Buttons
        HBox actions = createActionPane();

        getChildren().addAll(title, filters, alertsTable, actions);

        // Initial Refresh
        refreshAlertData();
    }

    private VBox createAdvancedFilterBar() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd;");

        HBox line1 = new HBox(15);
        line1.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        zoneFilter = new ComboBox<>();
        zoneFilter.getItems().add("All Zones");
        dataService.getZones().forEach(z -> zoneFilter.getItems().add(z.getCode()));
        zoneFilter.setValue("All Zones");

        severityFilter = new ComboBox<>();
        severityFilter.setPromptText("Severity");
        severityFilter.getItems().addAll(SeverityLevel.values());

        showHistoryBtn = new CheckBox("Show Dismissed/Acknowledged (History)");
        showHistoryBtn.setStyle("-fx-font-weight: bold;");

        line1.getChildren().addAll(new Label("Zone:"), zoneFilter, new Label("Severity:"), severityFilter, showHistoryBtn);

        HBox line2 = new HBox(15);
        line2.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        startDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        endDatePicker = new DatePicker(LocalDate.now());
        Button btnApply = new Button("Apply Filters");
        btnApply.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        btnApply.setOnAction(e -> applyFiltering());

        line2.getChildren().addAll(new Label("From:"), startDatePicker, new Label("To:"), endDatePicker, btnApply);

        container.getChildren().addAll(line1, line2);
        return container;
    }

    private void applyFiltering() {
        String selectedZone = zoneFilter.getValue();
        SeverityLevel selectedSeverity = severityFilter.getValue();
        boolean showHistory = showHistoryBtn.isSelected();
        LocalDateTime start = startDatePicker.getValue().atStartOfDay();
        LocalDateTime end = endDatePicker.getValue().atTime(LocalTime.MAX);

        filteredAlerts.setPredicate(alert -> {
            // 1. Status Filter (Active vs History)
            if (!showHistory && alert.isAcknowledged()) return false;

            // 2. Zone Filter
            if (selectedZone != null && !selectedZone.equals("All Zones") && !alert.getZoneId().equals(selectedZone)) return false;

            // 3. Severity Filter
            if (selectedSeverity != null && alert.getSeverityLevel() != selectedSeverity) return false;

            // 4. Date Filter
            if (alert.getAlertTimestamp().isBefore(start) || alert.getAlertTimestamp().isAfter(end)) return false;

            return true;
        });
    }

    private void setupAlertsTable() {
        alertsTable = new TableView<>();

        // IMPORTANT: We wrap the list from AlertManager directly to ensure we see ALL alerts
        // The DataService refreshAll() logic needs to sync with this.
        filteredAlerts = new FilteredList<>(dataService.getActiveAlerts(), p -> true);
        alertsTable.setItems(filteredAlerts);

        TableColumn<com.farm.demo.model.Alert, SeverityLevel> sevCol = new TableColumn<>("!");
        sevCol.setCellValueFactory(new PropertyValueFactory<>("severityLevel"));
        sevCol.setPrefWidth(40);
        sevCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SeverityLevel item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle(""); setText("");
                } else {
                    if (item == SeverityLevel.CRITICAL) setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    else if (item == SeverityLevel.WARNING) setStyle("-fx-background-color: orange;");
                    else setStyle("-fx-background-color: green; -fx-text-fill: white;");
                    setText(item.name().substring(0, 1));
                }
            }
        });

        TableColumn<com.farm.demo.model.Alert, LocalDateTime> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("alertTimestamp"));

        TableColumn<com.farm.demo.model.Alert, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneId"));

        TableColumn<com.farm.demo.model.Alert, String> msgCol = new TableColumn<>("Message");
        msgCol.setCellValueFactory(new PropertyValueFactory<>("message"));

        TableColumn<com.farm.demo.model.Alert, Boolean> statusCol = new TableColumn<>("Ack?");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("isAcknowledged"));

        alertsTable.getColumns().addAll(sevCol, timeCol, zoneCol, msgCol, statusCol);
        alertsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private HBox createActionPane() {
        Button btnAck = new Button("Acknowledge Selected");
        btnAck.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnAck.setOnAction(e -> {
            com.farm.demo.model.Alert selected = alertsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.acknowledge();
                dataService.refreshAll();
                alertsTable.refresh();
                applyFiltering(); // Re-hide if not in history mode
            }
        });

        Button btnRefresh = new Button("Refresh List");
        btnRefresh.setOnAction(e -> refreshAlertData());

        HBox actions = new HBox(10, btnAck, btnRefresh);
        return actions;
    }

    /**
     * Call this to force the UI to pull new alerts from the backend
     */
    public void refreshAlertData() {
        dataService.refreshAll();
        applyFiltering();
    }
}