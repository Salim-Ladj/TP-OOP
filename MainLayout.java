package com.farm.demo.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Root layout: fixed sidebar on the left, swappable content on the right.
 */
public class MainLayout extends HBox {

    private final StackPane contentHolder = new StackPane();

    // Keep references so we can highlight the active button
    private Button btnZones, btnLivestock, btnProduction;

    // Cached views (lazy-init)
    private Node zonesView;
    private Node livestockView;
    private Node productionView;

    public MainLayout() {
        getStyleClass().add("root");
        setFillHeight(true);

        VBox sidebar = buildSidebar();
        HBox.setHgrow(contentHolder, Priority.ALWAYS);
        contentHolder.setAlignment(Pos.TOP_LEFT);

        getChildren().addAll(sidebar, contentHolder);

        // Show zones by default
        showZones();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        VBox.setVgrow(sidebar, Priority.ALWAYS);

        // Header
        VBox header = new VBox(2);
        header.getStyleClass().add("sidebar-header");
        Label appTitle = new Label("🌱 SmartFarm Core");
        appTitle.getStyleClass().add("sidebar-app-title");
        Label appSub = new Label("Management Suite");
        appSub.getStyleClass().add("sidebar-app-subtitle");
        header.getChildren().addAll(appTitle, appSub);

        // Spacer between header and nav
        Region topSpacer = new Region();
        topSpacer.setPrefHeight(8);

        // Nav buttons
        btnZones      = navButton("  📋  Zones Overview");
        btnLivestock  = navButton("  🐄  Livestock Registry");
        btnProduction = navButton("  📊  Production Records");

        btnZones.setOnAction(e -> showZones());
        btnLivestock.setOnAction(e -> showLivestock());
        btnProduction.setOnAction(e -> showProduction());

        // Push footer to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer (user info)
        VBox footer = buildFooter();

        sidebar.getChildren().addAll(
            header, topSpacer,
            btnZones, btnLivestock, btnProduction,
            spacer, footer
        );
        return sidebar;
    }

    private Button navButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private VBox buildFooter() {
        VBox footer = new VBox(2);
        footer.getStyleClass().add("sidebar-footer");

        HBox userRow = new HBox(10);
        userRow.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size:22px;");

        VBox userInfo = new VBox(1);
        Label userName = new Label("Manager Core");
        userName.getStyleClass().add("sidebar-user-name");
        Label userRole = new Label("ADMIN ACCESS");
        userRole.getStyleClass().add("sidebar-user-role");
        userInfo.getChildren().addAll(userName, userRole);

        userRow.getChildren().addAll(avatar, userInfo);
        footer.getChildren().add(userRow);
        return footer;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void setActive(Button active) {
        for (Button b : new Button[]{btnZones, btnLivestock, btnProduction}) {
            b.getStyleClass().remove("active");
        }
        active.getStyleClass().add("active");
    }

    private void showZones() {
        setActive(btnZones);
        if (zonesView == null) zonesView = new ZonesView();
        setContent(zonesView);
    }

    private void showLivestock() {
        setActive(btnLivestock);
        if (livestockView == null) livestockView = new LivestockView();
        setContent(livestockView);
    }

    private void showProduction() {
        setActive(btnProduction);
        if (productionView == null) productionView = new ProductionView();
        setContent(productionView);
    }

    private void setContent(Node node) {
        contentHolder.getChildren().setAll(node);
        StackPane.setAlignment(node, Pos.TOP_LEFT);
    }
}
