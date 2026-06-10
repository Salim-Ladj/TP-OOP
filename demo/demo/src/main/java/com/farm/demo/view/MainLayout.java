package com.farm.demo.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainLayout extends BorderPane {

    private StackPane contentArea;

    public MainLayout() {
        // 1. Header
        this.setTop(createHeader());

        // 2. Sidebar Navigation
        this.setLeft(createSidebar());

        // 3. Central Content Area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: #ffffff;");

        setView(new DashboardView());
        this.setCenter(contentArea);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(10, 0, 10, 0));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #34495e;");

        // Boutons de navigation avec instances des contrôles correspondants
        addNavButton(sidebar, "Dashboard", new DashboardView());
        addNavButton(sidebar, "Zones", new ZoneManagementView());
        addNavButton(sidebar, "Crops", new CropManagementView());
        addNavButton(sidebar, "Animals", new AnimalManagementView());
        addNavButton(sidebar, "Sensors", new SensorMonitorView()); // Liaison dynamique de supervision
        addNavButton(sidebar, "Alerts", new AlertCenterView());   // Liaison de gestion des alertes

        return sidebar;
    }

    private void addNavButton(VBox sidebar, String text, Pane viewToOpen) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 20, 12, 20));
        btn.setFont(Font.font("System", FontWeight.NORMAL, 14));

        String normalStyle = "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-background-radius: 0;";
        String hoverStyle = "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-background-radius: 0; -fx-cursor: hand;";

        btn.setStyle(normalStyle);

        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));

        btn.setOnAction(e -> {
            if (viewToOpen != null) {
                setView(viewToOpen);
            } else {
                setView(new StackPane(new Label(text + " view is coming soon...")));
            }
        });

        sidebar.getChildren().add(btn);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("SMART FARM MANAGEMENT SYSTEM");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        header.getChildren().add(title);
        return header;
    }

    public void setView(javafx.scene.Node view) {
        contentArea.getChildren().setAll(view);
    }
}