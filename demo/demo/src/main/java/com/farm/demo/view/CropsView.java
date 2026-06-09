package com.farm.demo.view;

import com.farm.demo.controller.CropsController;
import com.farm.demo.controller.ZonesController;
import com.farm.demo.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Crops screen — shows all crops pulled from CropZones.
 * Filtering by zone ID or growth stage is available in the top bar.
 */
public class CropsView extends VBox {

    // A small record to carry crop + its zone code together for display
    private record CropRow(Crop crop, String zoneCode, String zoneName) {}

    private final ZonesController zonesController = new ZonesController();

    private final ObservableList<CropRow> allRows = FXCollections.observableArrayList();
    private FilteredList<CropRow> filtered;

    private String activeZoneFilter = null;
    private String activeStageFilter = null;
    private TextField searchField;

    public CropsView() {
        getStyleClass().add("content-area");
        setSpacing(16);
        setFillWidth(true);

        loadRows();
        filtered = new FilteredList<>(allRows, r -> true);

        getChildren().addAll(
                buildTopBar(),
                buildStatsRow(),
                buildTable()
        );
    }

    // ── Load all crops from all CropZones ─────────────────────────────────────

    private void loadRows() {
        allRows.clear();
        for (Zone z : zonesController.getAllZones()) {
            if (z instanceof CropZone cz) {
                for (Crop c : cz.getCrops()) {
                    allRows.add(new CropRow(c, cz.getCode(), cz.getName()));
                }
            }
        }
        // Also load crops from CropsController that may not be in a zone yet
        CropsController cc = new CropsController();
        for (Crop c : cc.getAllCrops()) {
            boolean alreadyIn = allRows.stream().anyMatch(r -> r.crop() == c);
            if (!alreadyIn) allRows.add(new CropRow(c, "—", "Unassigned"));
        }
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 4, 0));

        VBox titles = new VBox(2);
        Label title = new Label("Crops");
        title.getStyleClass().add("topbar-title");
        Label sub = new Label("All crops registered across crop zones.");
        sub.getStyleClass().add("topbar-subtitle");
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Zone filter
        Label zoneLbl = new Label("🗺 Zone:");
        zoneLbl.setStyle("-fx-text-fill:#9999bb;-fx-font-size:12px;");

        ObservableList<String> zoneOptions = FXCollections.observableArrayList();
        zoneOptions.add("All Zones");
        zonesController.getAllZones().stream()
                .filter(z -> z instanceof CropZone)
                .map(Zone::getCode)
                .sorted()
                .forEach(zoneOptions::add);

        ComboBox<String> cmbZone = new ComboBox<>(zoneOptions);
        cmbZone.setValue("All Zones");
        cmbZone.getStyleClass().add("form-combo");
        cmbZone.setPrefWidth(140);
        cmbZone.setStyle("-fx-background-color:#1e1e3a;-fx-border-color:#2a2a4a;-fx-border-radius:8;-fx-background-radius:8;-fx-text-fill:#ccccee;");
        cmbZone.valueProperty().addListener((o, ov, nv) -> {
            activeZoneFilter = (nv == null || nv.equals("All Zones")) ? null : nv;
            applyFilters();
        });

        // Stage filter
        Label stageLbl = new Label("🌱 Stage:");
        stageLbl.setStyle("-fx-text-fill:#9999bb;-fx-font-size:12px;");

        ObservableList<String> stageOptions = FXCollections.observableArrayList();
        stageOptions.add("All Stages");
        for (GrowthStage s : GrowthStage.values()) stageOptions.add(s.name());

        ComboBox<String> cmbStage = new ComboBox<>(stageOptions);
        cmbStage.setValue("All Stages");
        cmbStage.getStyleClass().add("form-combo");
        cmbStage.setPrefWidth(140);
        cmbStage.setStyle("-fx-background-color:#1e1e3a;-fx-border-color:#2a2a4a;-fx-border-radius:8;-fx-background-radius:8;-fx-text-fill:#ccccee;");
        cmbStage.valueProperty().addListener((o, ov, nv) -> {
            activeStageFilter = (nv == null || nv.equals("All Stages")) ? null : nv;
            applyFilters();
        });

        // Search
        searchField = new TextField();
        searchField.setPromptText("🔍  Search by species...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((o, ov, nv) -> applyFilters());

        // Clear
        Button btnClear = new Button("✕");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setPadding(new Insets(6, 10, 6, 10));
        btnClear.setTooltip(new Tooltip("Clear all filters"));
        btnClear.setOnAction(e -> {
            cmbZone.setValue("All Zones");
            cmbStage.setValue("All Stages");
            searchField.clear();
        });

        bar.getChildren().addAll(titles, spacer, zoneLbl, cmbZone, stageLbl, cmbStage, btnClear, searchField);
        return bar;
    }

    private void applyFilters() {
        String q = searchField == null ? "" : searchField.getText().toLowerCase();
        filtered.setPredicate(row -> {
            boolean zoneOk  = activeZoneFilter  == null || activeZoneFilter.equals(row.zoneCode());
            boolean stageOk = activeStageFilter == null || activeStageFilter.equals(row.crop().getCurrentStage().name());
            boolean searchOk = q.isEmpty() || row.crop().getSpecies().toLowerCase().contains(q);
            return zoneOk && stageOk && searchOk;
        });
    }

    // ── Stat row ──────────────────────────────────────────────────────────────

    private HBox buildStatsRow() {
        HBox row = new HBox(12);

        long total    = allRows.size();
        long sowing   = allRows.stream().filter(r -> r.crop().getCurrentStage() == GrowthStage.SOWING).count();
        long growth   = allRows.stream().filter(r -> r.crop().getCurrentStage() == GrowthStage.GROWTH).count();
        long ready    = allRows.stream().filter(r -> r.crop().getCurrentStage() == GrowthStage.HARVEST).count();

        row.getChildren().addAll(
                statCard("🌾  Total Crops",     String.valueOf(total),  "stat-card-blue"),
                statCard("🌱  Sowing",          String.valueOf(sowing), "stat-card-green"),
                statCard("📈  In Growth",       String.valueOf(growth), "stat-card-blue"),
                statCard("✅  Ready to Harvest", String.valueOf(ready),  "stat-card-green")
        );
        return row;
    }

    private VBox statCard(String label, String value, String style) {
        VBox card = new VBox(4);
        card.getStyleClass().add(style);
        HBox.setHgrow(card, Priority.ALWAYS);
        Label lbl = new Label(label); lbl.getStyleClass().add("card-title");
        Label val = new Label(value); val.getStyleClass().add("card-value");
        card.getChildren().addAll(lbl, val);
        return card;
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private TableView<CropRow> buildTable() {
        TableView<CropRow> table = new TableView<>(filtered);
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPlaceholder(new Label("No crops found."));

        // Zone column
        TableColumn<CropRow, String> colZone = new TableColumn<>("Zone");
        colZone.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().zoneCode() + "  " + c.getValue().zoneName()
        ));
        colZone.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                String[] parts = v.split("  ", 2);
                VBox vb = new VBox(1);
                Label code = new Label(parts[0]);
                code.setStyle("-fx-text-fill:#3d5afe;-fx-font-weight:bold;-fx-font-size:12px;");
                Label name = new Label(parts.length > 1 ? parts[1] : "");
                name.setStyle("-fx-text-fill:#555577;-fx-font-size:10px;");
                vb.getChildren().addAll(code, name);
                setGraphic(vb); setText(null);
            }
        });

        // Species
        TableColumn<CropRow, String> colSpecies = new TableColumn<>("Species");
        colSpecies.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().crop().getSpecies()));

        // Type
        TableColumn<CropRow, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().crop().getType().name()));
        colType.setPrefWidth(100);

        // Stage
        TableColumn<CropRow, String> colStage = new TableColumn<>("Growth Stage");
        colStage.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().crop().getCurrentStage().name()));
        colStage.setPrefWidth(120);
        colStage.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(stageIcon(v) + "  " + v);
                badge.getStyleClass().add(v.equals("HARVEST") ? "badge-active"
                        : v.equals("SOWING") ? "badge-warning" : "badge-suspended");
                setGraphic(badge); setText(null);
            }
            private String stageIcon(String s) {
                return switch (s) {
                    case "SOWING"      -> "🌰";
                    case "GERMINATION" -> "🌿";
                    case "GROWTH"      -> "📈";
                    case "MATURITY"    -> "🌻";
                    case "HARVEST"     -> "✅";
                    default            -> "•";
                };
            }
        });

        // pH range
        TableColumn<CropRow, String> colPH = new TableColumn<>("pH Range");
        colPH.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().crop().getMinPH() + " – " + c.getValue().crop().getMaxPH()
        ));
        colPH.setPrefWidth(90);

        // Moisture range
        TableColumn<CropRow, String> colMoisture = new TableColumn<>("Moisture %");
        colMoisture.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().crop().getMinMoisture() + " – " + c.getValue().crop().getMaxMoisture()
        ));
        colMoisture.setPrefWidth(100);

        // Harvest date
        TableColumn<CropRow, String> colHarvest = new TableColumn<>("Expected Harvest");
        colHarvest.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().crop().getExpectedHarvestDate() != null
                        ? c.getValue().crop().getExpectedHarvestDate().toString() : "—"
        ));
        colHarvest.setPrefWidth(130);

        // Update stage action
        TableColumn<CropRow, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(160);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnStage  = new Button("📈 Update Stage");
            private final Button btnDelete = new Button("🗑");
            {
                btnStage.getStyleClass().add("btn-secondary");
                btnDelete.getStyleClass().add("btn-danger");
                btnDelete.setPadding(new Insets(4, 8, 4, 8));

                btnStage.setOnAction(e -> {
                    CropRow row = getTableView().getItems().get(getIndex());
                    showUpdateStageDialog(row.crop(), table);
                });
                btnDelete.setOnAction(e -> {
                    CropRow row = getTableView().getItems().get(getIndex());
                    confirmDeleteCrop(row, table);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox hb = new HBox(6, btnStage, btnDelete);
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
            }
        });

        table.getColumns().addAll(colZone, colSpecies, colType, colStage, colPH, colMoisture, colHarvest, colActions);
        return table;
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void showUpdateStageDialog(Crop crop, TableView<CropRow> table) {
        ChoiceDialog<GrowthStage> dialog = new ChoiceDialog<>(
                crop.getCurrentStage(),
                GrowthStage.values()
        );
        dialog.setTitle("Update Growth Stage");
        dialog.setHeaderText("Crop: " + crop.getSpecies());
        dialog.setContentText("New growth stage:");
        dialog.showAndWait().ifPresent(stage -> {
            crop.setCurrentStage(stage);
            // persist
            CropsController cc = new CropsController();
            cc.saveData();
            table.refresh();
        });
    }

    private void confirmDeleteCrop(CropRow row, TableView<CropRow> table) {
        // Explicitly use the JavaFX dialog layout wrapper to avoid collisions with your custom data Model
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "Remove crop \"" + row.crop().getSpecies() + "\" from zone " + row.zoneCode() + "?",
                ButtonType.YES, ButtonType.NO
        );
        alert.setHeaderText("Confirm Remove");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                // Remove from zone
                for (Zone z : zonesController.getAllZones()) {
                    if (z instanceof CropZone cz && cz.getCode().equals(row.zoneCode())) {
                        cz.getCrops().remove(row.crop());
                    }
                }
                allRows.remove(row);
            }
        });
    }
}