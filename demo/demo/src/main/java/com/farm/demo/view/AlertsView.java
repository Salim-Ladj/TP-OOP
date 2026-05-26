package com.farm.demo.view;

import com.farm.demo.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * JavaFX view for alert management.
 * Displays active alerts and alert history from the AlertManager.
 */
public class AlertsView extends VBox {
    private static final String STYLE_DARK = "-fx-base: #0a1628; -fx-control-inner-background: #0d2137;";

    public AlertsView() {
        setStyle(STYLE_DARK);
        setPadding(new Insets(15));
        setSpacing(10);
        setFillWidth(true);

        // Title
        Label title = new Label("Alert Management");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        getChildren().add(title);

        // Stats bar
        HBox statsBar = buildStatsBar();
        getChildren().add(statsBar);

        // Tabbed pane: Active Alerts + History
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: #0a1628;");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        Tab activeTab = new Tab("Active Alerts", buildActiveAlertsTable());
        activeTab.setStyle("-fx-text-fill: white;");

        Tab historyTab = new Tab("History", buildHistoryAlertsTable());
        historyTab.setStyle("-fx-text-fill: white;");

        tabs.getTabs().addAll(activeTab, historyTab);
        getChildren().add(tabs);
    }

    private HBox buildStatsBar() {
        HBox bar = new HBox(12);
        bar.setPrefHeight(80);
        bar.setStyle("-fx-fill: transparent;");

        try {
            java.util.List<com.farm.demo.model.Alert> allAlerts = AlertManager.getActiveAlertsSortedBySeverity();
            long critical = allAlerts.stream().filter(a -> a.getSeverityLevel() == SeverityLevel.CRITICAL).count();
            long warning = allAlerts.stream().filter(a -> a.getSeverityLevel() == SeverityLevel.WARNING).count();

            bar.getChildren().addAll(
                    makeStatCard("" + critical, "Critical", "#f44336"),
                    makeStatCard("" + warning, "Warnings", "#ffc107"),
                    makeStatCard("" + allAlerts.size(), "Total Active", "#0d2137")
            );
        } catch (Exception e) {
            bar.getChildren().addAll(
                    makeStatCard("—", "Critical", "#f44336"),
                    makeStatCard("—", "Warnings", "#ffc107"),
                    makeStatCard("—", "Total Active", "#0d2137")
            );
        }

        return bar;
    }

    private VBox makeStatCard(String value, String label, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color: " + color + "; -fx-border-radius: 8; -fx-background-color: #0d2137; -fx-background-radius: 8;");
        card.setPrefWidth(150);

        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #99aabb;");

        card.getChildren().addAll(valLabel, lblLabel);
        card.setAlignment(Pos.CENTER);
        return card;
    }

    private VBox buildActiveAlertsTable() {
        TableView<AlertRow> table = new TableView<>();
        table.setStyle("-fx-control-inner-background: #0d2137; -fx-table-cell-border-color: #1a3350;");

        TableColumn<AlertRow, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().zone));

        TableColumn<AlertRow, String> sensorCol = new TableColumn<>("Sensor");
        sensorCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().sensor));

        TableColumn<AlertRow, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().message));
        messageCol.setPrefWidth(250);

        TableColumn<AlertRow, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().severity));

        TableColumn<AlertRow, String> dateCol = new TableColumn<>("Timestamp");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().timestamp));

        table.getColumns().addAll(zoneCol, sensorCol, messageCol, severityCol, dateCol);

        // Populate from AlertManager
        try {
            for (com.farm.demo.model.Alert alert : AlertManager.getActiveAlertsSortedBySeverity()) {
                table.getItems().add(new AlertRow(
                        alert.getZoneId(),
                        alert.getSensorUniqueCode(),
                        alert.getMessage(),
                        alert.getSeverityLevel().name(),
                        alert.getAlertTimestamp().toString()
                ));
            }
        } catch (Exception e) {
            // AlertManager might be empty
        }

        VBox pane = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        pane.setStyle("-fx-background-color: #0a1628;");
        return pane;
    }

    private VBox buildHistoryAlertsTable() {
        TableView<AlertRow> table = new TableView<>();
        table.setStyle("-fx-control-inner-background: #0d2137; -fx-table-cell-border-color: #1a3350;");

        TableColumn<AlertRow, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().zone));

        TableColumn<AlertRow, String> sensorCol = new TableColumn<>("Sensor");
        sensorCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().sensor));

        TableColumn<AlertRow, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().message));
        messageCol.setPrefWidth(250);

        TableColumn<AlertRow, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().severity));

        TableColumn<AlertRow, String> dateCol = new TableColumn<>("Timestamp");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().timestamp));

        table.getColumns().addAll(zoneCol, sensorCol, messageCol, severityCol, dateCol);

        // Note: History is empty by default (AlertManager only tracks active alerts)
        // To show history, we would need to implement a persistence layer

        VBox pane = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        pane.setStyle("-fx-background-color: #0a1628;");
        return pane;
    }

    public static class AlertRow {
        public String zone, sensor, message, severity, timestamp;
        public AlertRow(String zone, String sensor, String message, String severity, String timestamp) {
            this.zone = zone;
            this.sensor = sensor;
            this.message = message;
            this.severity = severity;
            this.timestamp = timestamp;
        }
    }
}






