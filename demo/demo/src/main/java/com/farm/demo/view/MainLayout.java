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
        contentArea.setStyle("-fx-background-color: #ffffff;"); // Clean white background

        // Initial view
        setView(new Label("Select a category to begin."));
        setView(new DashboardView());
        this.setCenter(contentArea);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5); // Small spacing between items
        sidebar.setPadding(new Insets(10, 0, 10, 0)); // No side padding so buttons hit the edges
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #34495e;");

        // Define our navigation items
        addNavButton(sidebar, "Dashboard", new DashboardView()); // We'll build this later
        addNavButton(sidebar, "Zones", new ZoneManagementView());
        addNavButton(sidebar, "Crops", new CropManagementView());
        addNavButton(sidebar, "Animals", new AnimalManagementView());
        addNavButton(sidebar, "Sensors", null);
        addNavButton(sidebar, "Alerts", null);


        return sidebar;
    }

    /**
     * Helper method to create a sidebar button that looks like a menu item
     */
    private void addNavButton(VBox sidebar, String text, Pane viewToOpen) {
        Button btn = new Button(text);

        // STYLING: Make it look like a menu item, not a standard button
        btn.setMaxWidth(Double.MAX_VALUE); // Fill sidebar width
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 20, 12, 20));
        btn.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // CSS Styling: Transparent background, white text
        String normalStyle = "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-background-radius: 0;";
        String hoverStyle = "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-background-radius: 0; -fx-cursor: hand;";

        btn.setStyle(normalStyle);

        // Hover Effects
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));

        // CLICK ACTION
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