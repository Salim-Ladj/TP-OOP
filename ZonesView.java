package com.farm.demo.view;

import com.farm.demo.controller.ZonesController;
import com.farm.demo.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Zones Overview screen — matches the left panel in the UI screenshot.
 * Layout:
 *   top-bar  (title + search + Add button)
 *   stat-row (4 summary cards)
 *   main-row (table  |  add/edit form)
 */
public class ZonesView extends VBox {

    // ── Controller ────────────────────────────────────────────────────────────
    private final ZonesController controller = new ZonesController();

    // ── UI state ──────────────────────────────────────────────────────────────
    private final ObservableList<Zone> zoneData;
    private FilteredList<Zone> filteredZones;

    // Form fields
    private TextField fldName, fldCode, fldLocation;
    private ComboBox<String> cmbType, cmbStatus;
    private Zone selectedZone = null;         // null → add mode

    // Stat labels
    private Label lblTotal, lblActive, lblSuspended, lblSensors;

    public ZonesView() {
        getStyleClass().add("content-area");
        setSpacing(16);
        setFillWidth(true);

        zoneData = FXCollections.observableArrayList(controller.getAllZones());
        filteredZones = new FilteredList<>(zoneData, z -> true);

        getChildren().addAll(
            buildTopBar(),
            buildStatRow(),
            buildMainRow()
        );
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 12, 0));

        VBox titles = new VBox(2);
        Label title = new Label("Zones Overview");
        title.getStyleClass().add("topbar-title");
        Label sub = new Label("Manage physical farm boundaries, production types, and operational statuses.");
        sub.getStyleClass().add("topbar-subtitle");
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("🔍  Search resources...");
        search.getStyleClass().add("search-field");
        search.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.toLowerCase();
            filteredZones.setPredicate(z ->
                q.isEmpty() ||
                z.getName().toLowerCase().contains(q) ||
                z.getCode().toLowerCase().contains(q)
            );
        });

        Button btnAdd = new Button("＋  Add New Zone");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> clearForm());

        bar.getChildren().addAll(titles, spacer, search, btnAdd);
        return bar;
    }

    // ── Stat row ──────────────────────────────────────────────────────────────

    private HBox buildStatRow() {
        HBox row = new HBox(12);
        row.setFillHeight(true);

        lblTotal     = new Label("0");
        lblActive    = new Label("0");
        lblSuspended = new Label("0");
        lblSensors   = new Label("0");

        row.getChildren().addAll(
            statCard("📋", "Total Zones",      lblTotal,     "stat-card-blue"),
            statCard("✅", "Active",            lblActive,    "stat-card-green"),
            statCard("⚠️", "Suspended",         lblSuspended, "stat-card-orange"),
            statCard("📡", "Total Sensors",     lblSensors,   "stat-card-blue")
        );

        refreshStats();
        return row;
    }

    private VBox statCard(String icon, String label, Label valueLabel, String style) {
        VBox card = new VBox(4);
        card.getStyleClass().add(style);
        card.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label ico = new Label(icon + "  " + label);
        ico.getStyleClass().add("card-title");
        valueLabel.getStyleClass().add("card-value");

        card.getChildren().addAll(ico, valueLabel);
        return card;
    }

    private void refreshStats() {
        long active    = zoneData.stream().filter(z -> z.getStatus() == ZoneStatus.ACTIVE).count();
        long suspended = zoneData.stream().filter(z -> z.getStatus() == ZoneStatus.SUSPENDED).count();
        long sensors   = zoneData.stream().mapToLong(z -> z.getSensors().size()).sum();

        lblTotal.setText(String.valueOf(zoneData.size()));
        lblActive.setText(String.valueOf(active));
        lblSuspended.setText(String.valueOf(suspended));
        lblSensors.setText(String.valueOf(sensors));
    }

    // ── Main row: table + form ─────────────────────────────────────────────────

    private HBox buildMainRow() {
        HBox row = new HBox(16);
        row.setFillHeight(true);
        VBox.setVgrow(row, Priority.ALWAYS);

        VBox tableSection = buildTableSection();
        VBox formSection  = buildFormSection();

        HBox.setHgrow(tableSection, Priority.ALWAYS);
        row.getChildren().addAll(tableSection, formSection);
        return row;
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox buildTableSection() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");
        VBox.setVgrow(box, Priority.ALWAYS);

        // Table
        TableView<Zone> table = new TableView<>(filteredZones);
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPlaceholder(new Label("No zones found."));

        // Columns
        TableColumn<Zone, String> colCode = new TableColumn<>("Zone Code");
        colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCode()));
        colCode.setPrefWidth(100);
        colCode.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setGraphic(null); }
                else {
                    Label lbl = new Label(v);
                    lbl.setStyle("-fx-text-fill:#3d5afe;-fx-font-weight:bold;-fx-font-size:12px;");
                    setGraphic(lbl); setText(null);
                }
            }
        });

        TableColumn<Zone, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Zone, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(typeLabel(c.getValue())));
        colType.setPrefWidth(120);

        TableColumn<Zone, String> colStatus = new TableColumn<>("Current Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getStatus().name()
        ));
        colStatus.setPrefWidth(130);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(v);
                badge.getStyleClass().add(
                    v.equals("ACTIVE") ? "badge-active" : "badge-suspended"
                );
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<Zone, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(160);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit    = new Button("✏ Edit");
            private final Button btnToggle  = new Button("⏸");
            private final Button btnDelete  = new Button("🗑");
            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnToggle.getStyleClass().add("btn-secondary");
                btnDelete.getStyleClass().add("btn-danger");
                btnEdit.setPadding(new Insets(4, 8, 4, 8));
                btnToggle.setPadding(new Insets(4, 8, 4, 8));
                btnDelete.setPadding(new Insets(4, 8, 4, 8));

                btnEdit.setOnAction(e -> {
                    Zone z = getTableView().getItems().get(getIndex());
                    loadFormForEdit(z);
                });
                btnToggle.setOnAction(e -> {
                    Zone z = getTableView().getItems().get(getIndex());
                    controller.toggleStatus(z);
                    refreshTable(table);
                    refreshStats();
                });
                btnDelete.setOnAction(e -> {
                    Zone z = getTableView().getItems().get(getIndex());
                    confirmDelete(z, table);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Zone z = getTableView().getItems().get(getIndex());
                btnToggle.setText(z.getStatus() == ZoneStatus.ACTIVE ? "⏸ Suspend" : "▶ Activate");
                HBox hb = new HBox(6, btnEdit, btnToggle, btnDelete);
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
            }
        });

        table.getColumns().addAll(colCode, colName, colType, colStatus, colActions);

        // Pagination label
        HBox pager = new HBox();
        pager.setAlignment(Pos.CENTER_LEFT);
        Label pageInfo = new Label();
        pageInfo.getStyleClass().add("pagination-text");
        filteredZones.addListener((javafx.collections.ListChangeListener<Zone>) c -> {
            pageInfo.setText("Showing 1–" + filteredZones.size() +
                " of " + filteredZones.size() + " Zones");
        });
        pageInfo.setText("Showing 1–" + filteredZones.size() + " of " + filteredZones.size() + " Zones");
        pager.getChildren().add(pageInfo);

        box.getChildren().addAll(table, pager);
        return box;
    }

    private String typeLabel(Zone z) {
        if (z instanceof CropZone)       return "🌱 Crop";
        if (z instanceof LivestockZone)  return "🐄 Livestock";
        if (z instanceof AquacultureZone)return "🐟 Aquaculture";
        return "—";
    }

    private void refreshTable(TableView<Zone> table) {
        table.refresh();
    }

    private void confirmDelete(Zone z, TableView<Zone> table) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete zone \"" + z.getName() + "\"?",
            ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm Delete");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                controller.deleteZone(z);
                zoneData.remove(z);
                refreshStats();
            }
        });
    }

    // ── Form section ──────────────────────────────────────────────────────────

    private VBox buildFormSection() {
        VBox box = new VBox(14);
        box.getStyleClass().add("form-section");
        box.setMinWidth(280);
        box.setMaxWidth(300);

        Label formTitle = new Label("✏  Add / Edit Zone Configuration");
        formTitle.getStyleClass().add("section-title");
        formTitle.setStyle("-fx-font-size:14px;");

        // Zone Name
        VBox nameBlock = fieldBlock("Zone Name", fldName = new TextField());
        fldName.setPromptText("e.g. West Ridge");

        // Zone Code
        VBox codeBlock = fieldBlock("Zone Code", fldCode = new TextField());
        fldCode.setPromptText("e.g. ZN-999-OMEGA");

        // Production Type
        cmbType = new ComboBox<>(FXCollections.observableArrayList(
            "Crop Production", "Livestock", "Aquaculture"
        ));
        cmbType.setValue("Crop Production");
        cmbType.getStyleClass().add("form-combo");
        cmbType.setMaxWidth(Double.MAX_VALUE);
        VBox typeBlock = fieldBlock("Production Type", cmbType);

        // Status
        cmbStatus = new ComboBox<>(FXCollections.observableArrayList("Active", "Suspended"));
        cmbStatus.setValue("Active");
        cmbStatus.getStyleClass().add("form-combo");
        cmbStatus.setMaxWidth(Double.MAX_VALUE);
        VBox statusBlock = fieldBlock("Current Status", cmbStatus);

        // Location
        VBox locBlock = fieldBlock("Location Reference", fldLocation = new TextField());
        fldLocation.setPromptText("GPS Coordinates or Sector ID");

        // Buttons
        HBox btnRow = new HBox(10);
        Button btnSave  = new Button("💾  Save Zone");
        Button btnReset = new Button("Reset");
        btnSave.getStyleClass().add("btn-primary");
        btnReset.getStyleClass().add("btn-secondary");

        btnSave.setOnAction(e -> saveZone());
        btnReset.setOnAction(e -> clearForm());

        HBox.setHgrow(btnSave, Priority.ALWAYS);
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnRow.getChildren().addAll(btnSave, btnReset);

        // Sensor mini-dashboard
        VBox sensorBox = buildSensorMini();

        box.getChildren().addAll(
            formTitle,
            new Separator(),
            nameBlock, codeBlock, typeBlock, statusBlock, locBlock,
            btnRow,
            new Separator(),
            sensorBox
        );
        return box;
    }

    private VBox fieldBlock(String label, Control field) {
        VBox vb = new VBox(4);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        field.getStyleClass().add("form-field");
        field.setMaxWidth(Double.MAX_VALUE);
        vb.getChildren().addAll(lbl, field);
        return vb;
    }

    private VBox buildSensorMini() {
        VBox box = new VBox(10);

        Label title = new Label("📡  Sensor Overview");
        title.getStyleClass().add("card-title");

        // Active sensors card
        VBox activeCard = new VBox(4);
        activeCard.getStyleClass().add("stat-card-green");
        long totalSensors = zoneData.stream().mapToLong(z -> z.getSensors().size()).sum();
        Label sensVal = new Label(String.valueOf(totalSensors));
        sensVal.getStyleClass().add("card-value-small");
        Label sensLbl = new Label("Total Active Sensors");
        sensLbl.getStyleClass().add("card-label");
        activeCard.getChildren().addAll(new Label("📡"), sensVal, sensLbl);

        // Alerts card
        long alerts = zoneData.stream()
            .flatMap(z -> z.getSensors().stream())
            .flatMap(s -> s.getReadings().stream())
            .filter(r -> r != null && r.isOutOfRange())
            .count();

        VBox alertCard = new VBox(4);
        alertCard.getStyleClass().add("stat-card-orange");
        Label alertVal = new Label(String.valueOf(alerts));
        alertVal.getStyleClass().add("card-value-small");
        Label alertLbl = new Label("Alerts in Zones");
        alertLbl.getStyleClass().add("card-label");
        alertCard.getChildren().addAll(new Label("⚠️"), alertVal, alertLbl);

        box.getChildren().addAll(title, activeCard, alertCard);
        return box;
    }

    // ── Form logic ────────────────────────────────────────────────────────────

    private void loadFormForEdit(Zone z) {
        selectedZone = z;
        fldName.setText(z.getName());
        fldCode.setText(z.getCode());
        fldLocation.setText(z.getLocation() != null ? z.getLocation() : "");
        cmbStatus.setValue(z.getStatus() == ZoneStatus.ACTIVE ? "Active" : "Suspended");
        if (z instanceof CropZone)        cmbType.setValue("Crop Production");
        else if (z instanceof LivestockZone)  cmbType.setValue("Livestock");
        else if (z instanceof AquacultureZone) cmbType.setValue("Aquaculture");
    }

    private void clearForm() {
        selectedZone = null;
        fldName.clear();
        fldCode.clear();
        fldLocation.clear();
        cmbType.setValue("Crop Production");
        cmbStatus.setValue("Active");
    }

    private void saveZone() {
        String name   = fldName.getText().trim();
        String code   = fldCode.getText().trim();
        String loc    = fldLocation.getText().trim();
        String type   = cmbType.getValue();
        ZoneStatus st = cmbStatus.getValue().equals("Active")
            ? ZoneStatus.ACTIVE : ZoneStatus.SUSPENDED;

        if (name.isEmpty() || code.isEmpty()) {
            showError("Zone name and code are required.");
            return;
        }

        if (selectedZone == null) {
            // ADD
            Zone z = controller.createZone(name, code, type, loc, st);
            if (z != null) {
                zoneData.add(z);
            } else {
                showError("Zone code already exists.");
                return;
            }
        } else {
            // EDIT
            controller.updateZone(selectedZone, name, code, loc, st);
            int idx = zoneData.indexOf(selectedZone);
            if (idx >= 0) zoneData.set(idx, selectedZone);
        }

        refreshStats();
        clearForm();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Validation Error");
        a.showAndWait();
    }

    // ── Inner separator helper ────────────────────────────────────────────────

    private Separator separator() {
        Separator s = new Separator();
        s.getStyleClass().add("separator");
        return s;
    }

    private static class Separator extends javafx.scene.control.Separator {
        Separator() { super(); }
    }
}
