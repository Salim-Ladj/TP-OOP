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
    // Total hosted entities (crops/animals)
    private Label lblEntities;

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
        lblEntities  = new Label("0");

        row.getChildren().addAll(
                statCard("📋", "Total Zones",      lblTotal,     "stat-card-blue"),
                statCard("✅", "Active",            lblActive,    "stat-card-green"),
                statCard("⚠️", "Suspended",         lblSuspended, "stat-card-orange"),
                statCard("📡", "Total Sensors",     lblSensors,   "stat-card-blue"),
                statCard("🌾", "Total Entities",    lblEntities,  "stat-card-blue")
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
        long entities  = zoneData.stream().mapToLong(z -> getEntityCount(z)).sum();

        lblTotal.setText(String.valueOf(zoneData.size()));
        lblActive.setText(String.valueOf(active));
        lblSuspended.setText(String.valueOf(suspended));
        lblSensors.setText(String.valueOf(sensors));
        if (lblEntities != null) lblEntities.setText(String.valueOf(entities));
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
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(65);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPlaceholder(new Label("No zones found."));
        table.setPrefWidth(1200);

        // Columns
        TableColumn<Zone, String> colCode = new TableColumn<>("Zone Code");
        colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCode()));
        colCode.setPrefWidth(90);
        colCode.setMinWidth(90);
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
        colName.setPrefWidth(180);
        colName.setMinWidth(180);
        colName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setGraphic(null); }
                else {
                    Label lbl = new Label(v);
                    lbl.setWrapText(true);
                    lbl.setStyle("-fx-font-size:12px; -fx-text-fill:#ffffff;");
                    setGraphic(lbl); setText(null);
                }
            }
        });

        TableColumn<Zone, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(typeLabel(c.getValue())));
        colType.setPrefWidth(100);
        colType.setMinWidth(100);

        TableColumn<Zone, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus().name()
        ));
        colStatus.setPrefWidth(90);
        colStatus.setMinWidth(90);
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

        TableColumn<Zone, String> colEntities = new TableColumn<>("Entities");
        colEntities.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(getEntityCount(c.getValue()))));
        colEntities.setPrefWidth(80);
        colEntities.setMinWidth(80);

        TableColumn<Zone, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(480);
        colActions.setMinWidth(480);
        colActions.setResizable(false);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit    = new Button("✏️  Edit");
            private final Button btnToggle  = new Button("⏸  Suspend");
            private final Button btnAssign  = new Button("➕  Assign");
            private final Button btnDelete  = new Button("🗑  Delete");
            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnToggle.getStyleClass().add("btn-secondary");
                btnAssign.getStyleClass().add("btn-primary");
                btnDelete.getStyleClass().add("btn-danger");

                // Set smaller fixed widths to fit 2x2 grid
                btnEdit.setMinWidth(80);
                btnToggle.setMinWidth(80);
                btnAssign.setMinWidth(80);
                btnDelete.setMinWidth(80);

                btnEdit.setPrefWidth(80);
                btnToggle.setPrefWidth(80);
                btnAssign.setPrefWidth(80);
                btnDelete.setPrefWidth(80);

                btnEdit.setMaxWidth(80);
                btnToggle.setMaxWidth(80);
                btnAssign.setMaxWidth(80);
                btnDelete.setMaxWidth(80);

                btnEdit.setPadding(new Insets(8, 12, 8, 12));
                btnToggle.setPadding(new Insets(8, 12, 8, 12));
                btnAssign.setPadding(new Insets(8, 12, 8, 12));
                btnDelete.setPadding(new Insets(8, 12, 8, 12));

                btnEdit.setStyle("-fx-font-size: 12; -fx-text-alignment: center; -fx-wrap-text: false;");
                btnToggle.setStyle("-fx-font-size: 12; -fx-text-alignment: center; -fx-wrap-text: false;");
                btnAssign.setStyle("-fx-font-size: 12; -fx-text-alignment: center; -fx-wrap-text: false;");
                btnDelete.setStyle("-fx-font-size: 12; -fx-text-alignment: center; -fx-wrap-text: false;");

                // Add tooltips for better UX
                btnEdit.setTooltip(new Tooltip("Edit zone details and configuration"));
                btnToggle.setTooltip(new Tooltip("Suspend or activate this zone"));
                btnAssign.setTooltip(new Tooltip("Assign crops or animals to zone"));
                btnDelete.setTooltip(new Tooltip("Remove zone from system"));

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
                btnAssign.setOnAction(e -> {
                    Zone z = getTableView().getItems().get(getIndex());
                    showAssignDialog(z);
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
                if (z.getStatus() == ZoneStatus.ACTIVE) {
                    btnToggle.setText("⏸  Suspend");
                } else {
                    btnToggle.setText("▶  Activate");
                }
                // Arrange buttons in 2x2 grid
                HBox row1 = new HBox(6, btnEdit, btnToggle);
                HBox row2 = new HBox(6, btnAssign, btnDelete);
                row1.setAlignment(Pos.CENTER_LEFT);
                row2.setAlignment(Pos.CENTER_LEFT);
                VBox vb = new VBox(6, row1, row2);
                vb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(vb);
            }
        });

        table.getColumns().addAll(colCode, colName, colType, colStatus, colEntities, colActions);

        // Wrap in ScrollPane for better control
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-control-inner-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

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

        box.getChildren().addAll(scrollPane, pager);
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
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

        Button btnFeeding = new Button("🍽  Feeding Program");
        btnFeeding.getStyleClass().add("btn-secondary");
        btnFeeding.setOnAction(e -> {
            if (selectedZone instanceof LivestockZone) {
                showFeedingDialog((LivestockZone) selectedZone);
            } else {
                showError("Feeding program available for Livestock zones only.");
            }
        });

        box.getChildren().addAll(
                formTitle,
                new Separator(),
                nameBlock, codeBlock, typeBlock, statusBlock, locBlock,
                btnRow,
                new Separator(),
                sensorBox,
                btnFeeding
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
        // Use AlertManager to obtain current active alerts count
        long alerts = com.farm.demo.model.AlertManager.getActiveAlertsSortedBySeverity().size();

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

    // Helper to determine number of hosted entities in a zone
    private int getEntityCount(Zone z) {
        if (z instanceof CropZone) return ((CropZone) z).getCrops().size();
        if (z instanceof LivestockZone) return ((LivestockZone) z).getAnimals().size();
        return 0;
    }

    /**
     * Shows a contextual "Add" dialog based on zone type:
     *  - LivestockZone  → Add Animal form
     *  - CropZone       → Add Crop form
     *  - AquacultureZone → Add Animal form (aquatic)
     * The new entity is created and immediately assigned to the zone.
     */
    private void showAssignDialog(Zone z) {
        if (z instanceof LivestockZone lz) {
            showAddAnimalToZoneDialog(lz);
        } else if (z instanceof AquacultureZone az) {
            showAddAnimalToZoneDialog(az);
        } else if (z instanceof CropZone cz) {
            showAddCropToZoneDialog(cz);
        } else {
            showError("This zone type does not support adding entities.");
        }
    }

    // ── Add Animal to zone ────────────────────────────────────────────────────

    private void showAddAnimalToZoneDialog(Zone z) {
        Dialog<Animal> dialog = new Dialog<>();
        dialog.setTitle("Add Animal to " + z.getName());
        dialog.setHeaderText("🐄  New animal — will be assigned to zone " + z.getCode());

        ButtonType saveBtn = new ButtonType("Add Animal", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField fId      = new TextField(); fId.setPromptText("AN-XXXX");
        TextField fSpecies = new TextField(); fSpecies.setPromptText("e.g. Black Angus");
        TextField fWeight  = new TextField(); fWeight.setPromptText("kg");
        TextField fTag     = new TextField(); fTag.setPromptText("Tag #");
        ComboBox<AnimalType> cmbAnimalType = new ComboBox<>(
                FXCollections.observableArrayList(AnimalType.values())
        );
        cmbAnimalType.setValue(z instanceof AquacultureZone ? AnimalType.AQUATIC : AnimalType.RUMINANT);

        int r = 0;
        grid.addRow(r++, styled("Animal ID:"),   fId);
        grid.addRow(r++, styled("Species:"),     fSpecies);
        grid.addRow(r++, styled("Animal Type:"), cmbAnimalType);
        grid.addRow(r++, styled("Weight (kg):"), fWeight);
        grid.addRow(r++, styled("Tag:"),         fTag);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            String id = fId.getText().trim();
            String sp = fSpecies.getText().trim();
            if (id.isEmpty() || sp.isEmpty()) return null;
            try {
                double w = fWeight.getText().isBlank() ? 0
                        : Double.parseDouble(fWeight.getText());
                Animal a = new Animal(id, sp, cmbAnimalType.getValue(), w);
                a.setTag(fTag.getText().trim());
                a.setZoneId(z.getCode());
                a.setHealthStatus(HealthStatus.HEALTHY);
                return a;
            } catch (NumberFormatException ex) { return null; }
        });

        dialog.showAndWait().ifPresent(animal -> {
            if (animal == null) return;
            // Save to animal store
            com.farm.demo.controller.LivestocksController lc =
                    new com.farm.demo.controller.LivestocksController();
            lc.addAnimal(animal);

            // Assign to zone object so entity count updates
            controller.assignAnimalToZone(z, animal);

            int idx = zoneData.indexOf(z);
            if (idx >= 0) zoneData.set(idx, z);
            refreshStats();
        });
    }

    // ── Add Crop to zone ──────────────────────────────────────────────────────

    private void showAddCropToZoneDialog(CropZone z) {
        Dialog<Crop> dialog = new Dialog<>();
        dialog.setTitle("Add Crop to " + z.getName());
        dialog.setHeaderText("🌱  New crop — will be assigned to zone " + z.getCode());

        ButtonType saveBtn = new ButtonType("Add Crop", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField fSpecies     = new TextField(); fSpecies.setPromptText("e.g. Tomato");
        TextField fMinPH       = new TextField(); fMinPH.setPromptText("e.g. 5.5");
        TextField fMaxPH       = new TextField(); fMaxPH.setPromptText("e.g. 7.0");
        TextField fMinMoisture = new TextField(); fMinMoisture.setPromptText("e.g. 30");
        TextField fMaxMoisture = new TextField(); fMaxMoisture.setPromptText("e.g. 80");
        TextField fHarvestDate = new TextField(); fHarvestDate.setPromptText("YYYY-MM-DD");
        ComboBox<CropType> cmbCropType = new ComboBox<>(
                FXCollections.observableArrayList(CropType.values())
        );
        cmbCropType.setValue(CropType.VEGETABLE);
        ComboBox<GrowthStage> cmbStage = new ComboBox<>(
                FXCollections.observableArrayList(GrowthStage.values())
        );
        cmbStage.setValue(GrowthStage.SOWING);

        int r = 0;
        grid.addRow(r++, styled("Species:"),        fSpecies);
        grid.addRow(r++, styled("Crop Type:"),      cmbCropType);
        grid.addRow(r++, styled("Growth Stage:"),   cmbStage);
        grid.addRow(r++, styled("Min pH:"),         fMinPH);
        grid.addRow(r++, styled("Max pH:"),         fMaxPH);
        grid.addRow(r++, styled("Min Moisture %:"), fMinMoisture);
        grid.addRow(r++, styled("Max Moisture %:"), fMaxMoisture);
        grid.addRow(r++, styled("Harvest Date:"),   fHarvestDate);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            String sp = fSpecies.getText().trim();
            if (sp.isEmpty()) return null;
            try {
                double minPH  = fMinPH.getText().isBlank()       ? 5.5  : Double.parseDouble(fMinPH.getText());
                double maxPH  = fMaxPH.getText().isBlank()        ? 7.5  : Double.parseDouble(fMaxPH.getText());
                double minMoi = fMinMoisture.getText().isBlank()  ? 30.0 : Double.parseDouble(fMinMoisture.getText());
                double maxMoi = fMaxMoisture.getText().isBlank()  ? 80.0 : Double.parseDouble(fMaxMoisture.getText());
                java.time.LocalDate harvest = fHarvestDate.getText().isBlank()
                        ? java.time.LocalDate.now().plusMonths(3)
                        : java.time.LocalDate.parse(fHarvestDate.getText().trim());
                return new Crop(cmbCropType.getValue(), sp, minPH, maxPH,
                        cmbStage.getValue(), minMoi, maxMoi, harvest);
            } catch (Exception ex) { return null; }
        });

        dialog.showAndWait().ifPresent(crop -> {
            if (crop == null) return;
            // Save to crop store
            com.farm.demo.controller.CropsController cc =
                    new com.farm.demo.controller.CropsController();
            cc.addCrop(crop);

            // Assign to zone
            controller.assignCropToZone(z, crop);

            int idx = zoneData.indexOf(z);
            if (idx >= 0) zoneData.set(idx, z);
            refreshStats();
        });
    }

    /** Small helper — styled form label */
    private Label styled(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-label");
        return l;
    }



    private void saveZone(){
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
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Validation Error");
        a.showAndWait();
    }

    // ── Feeding program dialog ───────────────────────────────────────────────

    private void showFeedingDialog(LivestockZone lz) {
        if (lz == null) return;
        FeedingProgram fp = lz.getFeedingProgram();
        Dialog<FeedingProgram> d = new Dialog<>();
        d.setTitle("Feeding Program — " + lz.getName());
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(8); grid.setVgap(8); grid.setPadding(new Insets(12));
        TextField fType = new TextField(fp != null ? fp.getFeedType() : "");
        TextField fQty  = new TextField(fp != null ? String.valueOf(fp.getQuantityPerMeal()) : "");
        TextField fMeals= new TextField(fp != null ? String.valueOf(fp.getNbMealsPerDay()) : "");

        grid.addRow(0, new Label("Feed Type:"), fType);
        grid.addRow(1, new Label("Quantity per Meal (kg):"), fQty);
        grid.addRow(2, new Label("Meals per Day:"), fMeals);

        d.getDialogPane().setContent(grid);
        d.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    double q = Double.parseDouble(fQty.getText());
                    int m = Integer.parseInt(fMeals.getText());
                    return new FeedingProgram(fType.getText(), q, m);
                } catch (NumberFormatException ex) { return null; }
            }
            return null;
        });

        d.showAndWait().ifPresent(newFp -> {
            if (newFp != null) {
                lz.setFeedingProgram(newFp);
                refreshStats();
            }
        });
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