package com.farm.demo.view;

import com.farm.demo.controller.LivestocksController;
import com.farm.demo.model.Animal;
import com.farm.demo.model.AnimalType;
import com.farm.demo.model.HealthStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Livestock Registry screen — matches the right panel in the UI screenshot.
 * Layout:
 *   top-bar  (title + search + New Entry)
 *   HBox {
 *     animal list table (left)
 *     detail panel      (right, shows selected animal's info + health log)
 *   }
 */
public class LivestockView extends HBox {

    private final LivestocksController controller = new LivestocksController();

    private final ObservableList<Animal> animalData;
    private FilteredList<Animal> filtered;

    // Detail panel nodes
    private Label lblAnimalId, lblBreed, lblWeight, lblWeightTrend;
    private Label lblFeedConv, lblNextVaccine, lblHealthStatus;
    private VBox healthLogBox;
    private Animal selectedAnimal;

    public LivestockView() {
        getStyleClass().add("content-area");
        setSpacing(0);
        setFillHeight(true);

        animalData = FXCollections.observableArrayList(controller.getAllAnimals());
        filtered   = new FilteredList<>(animalData, a -> true);

        // Ensure detail panel is created before the table selects a row so
        // the detail labels (e.g. lblAnimalId) are initialized before
        // showDetail(...) may be invoked by the table selection listener.
        VBox rightPanel = buildDetailPanel();
        VBox leftPanel = buildLeftPanel();

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        getChildren().addAll(leftPanel, rightPanel);
    }

    // ── Left panel ────────────────────────────────────────────────────────────

    private VBox buildLeftPanel() {
        VBox box = new VBox(12);
        box.getStyleClass().add("content-area");
        VBox.setVgrow(box, Priority.ALWAYS);

        box.getChildren().addAll(buildTopBar(), buildStatsRow(), buildTable());
        return box;
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 4, 0));

        VBox titles = new VBox(2);
        Label title = new Label("Livestock Registry");
        title.getStyleClass().add("topbar-title");
        Label sub = new Label(animalData.size() + " Head Registered");
        sub.getStyleClass().add("topbar-subtitle");
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("  Search by ID, Breed, or Tag...");
        search.getStyleClass().add("search-field");
        search.setPrefWidth(240);
        search.textProperty().addListener((o, ov, nv) -> {
            String q = nv == null ? "" : nv.toLowerCase();
            filtered.setPredicate(a ->
                    q.isEmpty() ||
                            a.getUniqueNumber().toLowerCase().contains(q) ||
                            a.getSpecies().toLowerCase().contains(q)
            );
        });

        Button btnNew = new Button("＋  New Entry");
        btnNew.getStyleClass().add("btn-primary");
        btnNew.setOnAction(e -> showAddDialog());

        bar.getChildren().addAll(titles, spacer, search, btnNew);
        return bar;
    }

    private HBox buildStatsRow() {
        HBox row = new HBox(12);

        long healthy  = animalData.stream().filter(a -> a.getHealth() == HealthStatus.HEALTHY).count();
        long sick     = animalData.stream().filter(a -> a.getHealth() == HealthStatus.SICK).count();
        long total    = animalData.size();

        row.getChildren().addAll(
                miniStat("  Total Head",  String.valueOf(total),   "stat-card-blue"),
                miniStat("✅  Healthy",      String.valueOf(healthy), "stat-card-green"),
                miniStat("  Sick / At Risk", String.valueOf(sick), "stat-card-red"),
                miniStat("  In Quarantine",
                        String.valueOf(animalData.stream()
                                .filter(a -> a.getHealth() == HealthStatus.QUARANTINED).count()),
                        "stat-card-orange")
        );
        return row;
    }

    private VBox miniStat(String label, String value, String style) {
        VBox card = new VBox(4);
        card.getStyleClass().add(style);
        HBox.setHgrow(card, Priority.ALWAYS);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("card-title");
        Label val = new Label(value);
        val.getStyleClass().add("card-value");
        card.getChildren().addAll(lbl, val);
        return card;
    }

    @SuppressWarnings("unchecked")
    private TableView<Animal> buildTable() {
        TableView<Animal> table = new TableView<>(filtered);
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Animal, String> colId = new TableColumn<>("Animal ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUniqueNumber()));
        colId.setPrefWidth(90);
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                VBox vb = new VBox(1);
                Label id  = new Label(v);
                id.setStyle("-fx-text-fill:#3d5afe;-fx-font-weight:bold;-fx-font-size:12px;");
                Animal a = getTableView().getItems().get(getIndex());
                Label sp = new Label(a.getType() != null ? a.getType().name() : "");
                sp.setStyle("-fx-text-fill:#555577;-fx-font-size:10px;");
                vb.getChildren().addAll(id, sp);
                setGraphic(vb); setText(null);
            }
        });

        TableColumn<Animal, String> colBreed = new TableColumn<>("Breed");
        colBreed.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpecies()));

        TableColumn<Animal, String> colTag = new TableColumn<>("Age");
        colTag.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getAge())));
        colTag.setPrefWidth(70);
        colTag.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label lbl = new Label(v);
                lbl.getStyleClass().add("badge-warning");
                setGraphic(lbl); setText(null);
            }
        });

        // Row click → show detail
        table.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv != null) showDetail(nv);
        });

        table.getColumns().addAll(colId, colBreed, colTag);

        // Select first row by default
        if (!filtered.isEmpty()) {
            table.getSelectionModel().selectFirst();
            showDetail(filtered.get(0));
        }

        return table;
    }

    // ── Right detail panel ────────────────────────────────────────────────────

    private VBox buildDetailPanel() {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("detail-panel");
        panel.setPrefWidth(340);
        panel.setMinWidth(300);

        // Header row
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Animal avatar
        Label avatar = new Label("");
        avatar.setStyle("-fx-font-size:36px;");

        VBox idBox = new VBox(2);
        lblAnimalId = new Label("—");
        lblAnimalId.getStyleClass().add("detail-name");
        lblBreed = new Label("—");
        lblBreed.getStyleClass().add("detail-id");
        idBox.getChildren().addAll(lblAnimalId, lblBreed);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblHealthStatus = new Label("Healthy");
        lblHealthStatus.getStyleClass().add("badge-active");

        header.getChildren().addAll(avatar, idBox, spacer, lblHealthStatus);

        // Quick stats row
        HBox statsRow = new HBox(10);
        lblWeight      = statChip("Weight Trend", "—", "");
        lblWeightTrend = statChip("Change",        "—", "");
        lblFeedConv    = statChip("Feed Conv.",    "—", "");
        lblNextVaccine = statChip("Next Vaccine",  "—", "");
        statsRow.getChildren().addAll(lblWeight, lblFeedConv, lblNextVaccine);

        // Buttons
        HBox btns = new HBox(10);
        Button btnEdit   = new Button("✏  Edit Record");
        Button btnReport = new Button("  Generate Report");
        btnEdit.getStyleClass().add("btn-secondary");
        btnReport.getStyleClass().add("btn-primary");
        btnEdit.setOnAction(e -> showEditDialog(selectedAnimal));
        btnReport.setOnAction(e -> showReport(selectedAnimal));
        btns.getChildren().addAll(btnEdit, btnReport);

        // Health log
        Label logTitle = new Label("  Medical & Health Log");
        logTitle.getStyleClass().add("section-title");
        logTitle.setStyle("-fx-font-size:14px;");

        healthLogBox = new VBox(8);

        ScrollPane scroll = new ScrollPane(healthLogBox);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel.getChildren().addAll(
                header,
                new javafx.scene.control.Separator(),
                statsRow,
                btns,
                new javafx.scene.control.Separator(),
                logTitle,
                scroll
        );
        return panel;
    }

    private Label statChip(String key, String value, String icon) {
        // Returns a label used for layout; actual values set via show detail
        Label lbl = new Label(icon + "  " + value);
        lbl.setStyle("-fx-text-fill:#ccccee;-fx-font-size:12px;");
        return lbl;
    }

    private void showDetail(Animal a) {
        selectedAnimal = a;

        lblAnimalId.setText(a.getUniqueNumber());
        lblBreed.setText(a.getSpecies());

        // Weight / feed
        lblWeight.setText("⚖  " + String.format("%.0f kg", a.getWeight()));
        lblFeedConv.setText("  " + a.getAge() + " years");
        lblNextVaccine.setText("  " + a.getHealth().name());

        // Health badge
        healthLogBox.getChildren().clear();
        switch (a.getHealth()) {
            case HEALTHY -> {
                lblHealthStatus.setText("✅ Healthy");
                lblHealthStatus.getStyleClass().setAll("badge-active");
            }
            case SICK -> {
                lblHealthStatus.setText(" Sick");
                lblHealthStatus.getStyleClass().setAll("badge-suspended");
            }
            default -> {
                lblHealthStatus.setText("⚕ Quarantined");
                lblHealthStatus.getStyleClass().setAll("badge-warning");
            }
        }

        Label historyNote = new Label("Detailed health event entries are not exposed by the current Animal model.");
        historyNote.getStyleClass().add("card-label");
        historyNote.setWrapText(true);
        healthLogBox.getChildren().add(historyNote);
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void showAddDialog() {
        Dialog<Animal> dialog = new Dialog<>();
        dialog.setTitle("New Animal Entry");
        dialog.setHeaderText("Register a new animal");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField fId      = new TextField(); fId.setPromptText("AN-XXXX");
        TextField fSpecies = new TextField(); fSpecies.setPromptText("e.g. Black Angus");
        TextField fWeight  = new TextField(); fWeight.setPromptText("kg");
        TextField fTag     = new TextField(); fTag.setPromptText("Tag #");
        ComboBox<AnimalType> cmbType = new ComboBox<>(
                FXCollections.observableArrayList(AnimalType.values())
        );
        cmbType.setValue(AnimalType.RUMINANT);

        grid.addRow(0, new Label("ID:"),      fId);
        grid.addRow(1, new Label("Species:"), fSpecies);
        grid.addRow(2, new Label("Type:"),    cmbType);
        grid.addRow(3, new Label("Weight:"),  fWeight);
        grid.addRow(4, new Label("Tag:"),     fTag);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                try {
                    double w = fWeight.getText().isEmpty() ? 0
                            : Double.parseDouble(fWeight.getText());
                    return controller.createAnimal(
                            fId.getText(), fSpecies.getText(),
                            cmbType.getValue(), w, fTag.getText()
                    );
                } catch (NumberFormatException ex) { return null; }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            if (a != null) animalData.add(a);
        });
    }

    private void showEditDialog(Animal a) {
        if (a == null) return;
        javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,
                "Edit dialog for " + a.getUniqueNumber() + " — wire to your update logic.",
                ButtonType.OK);
        info.setHeaderText("Edit Animal");
        info.showAndWait();
    }

    private void showReport(Animal a) {
        if (a == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Animal Report\n\n");
        sb.append("ID:      ").append(a.getUniqueNumber()).append("\n");
        sb.append("Species: ").append(a.getSpecies()).append("\n");
        sb.append("Weight:  ").append(a.getWeight()).append(" kg\n");
        sb.append("Health:  ").append(a.getHealth()).append("\n");
        sb.append("Events:  N/A (not available in current model)\n");

        javafx.scene.control.Alert report = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
        report.setHeaderText("  Animal Report — " + a.getUniqueNumber());
        report.showAndWait();
    }
}
