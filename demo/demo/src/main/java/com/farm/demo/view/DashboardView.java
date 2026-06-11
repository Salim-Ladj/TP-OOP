package com.farm.demo.view;

import com.farm.demo.model.*;
import com.farm.demo.service.DataService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardView extends VBox {

    private DataService dataService = DataService.getInstance();

    public DashboardView() {
        setSpacing(25);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f8f9fa;");

        // 1. Title
        Label title = new Label("Farm Overview Dashboard");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // 2. Stat Tiles (Summary)
        HBox statTiles = createStatTiles();

        // 3. Main Content (Chart + Alerts Summary)
        HBox mainContent = new HBox(20);
        mainContent.getChildren().addAll(createZoneChart(), createAlertSummary());
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        getChildren().addAll(title, statTiles, mainContent);
    }

    private HBox createStatTiles() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);

        container.getChildren().addAll(
                createTile("Total Zones", String.valueOf(dataService.getTotalZones()), "#3498db"),
                //createTile("Active Alerts", String.valueOf(dataService.getActiveAlertCount()), "#e74c3c"),
                createTile("Livestock", String.valueOf(dataService.getTotalAnimals()), "#2ecc71"),
                createTile("Crops", String.valueOf(dataService.getTotalCrops()), "#f1c40f")
        );

        return container;
    }

    private VBox createTile(String title, String value, String color) {
        VBox tile = new VBox(5);
        tile.setPrefSize(200, 100);
        tile.setAlignment(Pos.CENTER);
        tile.setStyle("-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 0 0 5 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label lblTitle = new Label(title);
        lblTitle.setTextFill(Color.GRAY);

        Label lblValue = new Label(value);
        lblValue.setFont(Font.font("System", FontWeight.BOLD, 28));
        lblValue.setTextFill(Color.web(color));

        tile.getChildren().addAll(lblTitle, lblValue);
        return tile;
    }

    private VBox createZoneChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        HBox.setHgrow(container, Priority.ALWAYS);

        Label lbl = new Label("Zone Distribution");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 16));

        PieChart pieChart = new PieChart();
        int cropZones = (int) dataService.getZones().stream().filter(z -> z instanceof CropZone).count();
        int livestockZones = (int) dataService.getZones().stream().filter(z -> z instanceof LivestockZone).count();
        int aquaZones = (int) dataService.getZones().stream().filter(z -> z instanceof AquacultureZone).count();

        pieChart.getData().add(new PieChart.Data("Crops", cropZones));
        pieChart.getData().add(new PieChart.Data("Livestock", livestockZones));
        pieChart.getData().add(new PieChart.Data("Aquaculture", aquaZones));
        pieChart.setLabelsVisible(true);

        container.getChildren().addAll(lbl, pieChart);
        return container;
    }

    private VBox createAlertSummary() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setPrefWidth(400);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label lbl = new Label("Recent Alerts Overview");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Using your AlertManager's textual overview
        Label lblOverview = new Label(AlertManager.getAlertsOverviewBySeverity());
        lblOverview.setWrapText(true);
        lblOverview.setFont(Font.font("Monospaced", 14));

        //Button btnViewAll = new Button("Go to Alert Center");
        //btnViewAll.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");

        container.getChildren().addAll(lbl, new Separator(), lblOverview, new Spacer());
        return container;
    }

    // Helper for layout
    private static class Spacer extends javafx.scene.layout.Region {
        public Spacer() { VBox.setVgrow(this, Priority.ALWAYS); }
    }
}