package com.farm.demo.view;

import com.farm.demo.controller.ProductionController;
import com.farm.demo.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Production Records screen.
 */
public class ProductionView extends VBox {

    private final ProductionController controller = new ProductionController();
    private final ObservableList<ProductionRecord> data;

    public ProductionView() {
        getStyleClass().add("content-area");
        setSpacing(16);
        setFillWidth(true);

        data = FXCollections.observableArrayList(controller.getAllRecords());

        getChildren().addAll(
                buildTopBar(),
                buildStatsRow(),
                buildMainContent()
        );
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 4, 0));

        VBox titles = new VBox(2);
        Label title = new Label("Production Records");
        title.getStyleClass().add("topbar-title");
        Label sub = new Label("Track yield, output, and production efficiency per zone.");
        sub.getStyleClass().add("topbar-subtitle");
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnExport = new Button("📥  Export CSV");
        btnExport.getStyleClass().add("btn-secondary");
        btnExport.setOnAction(e -> exportCSV());

        Button btnAdd = new Button("＋  Add Record");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> showAddDialog());

        bar.getChildren().addAll(titles, spacer, btnExport, btnAdd);
        return bar;
    }

    private HBox buildStatsRow() {
        HBox row = new HBox(12);

        double totalYield = data.stream().mapToDouble(ProductionRecord::getValue).sum();
        long zones = data.stream().map(r -> r.getZoneCode()).distinct().count();
        double avgYield = data.isEmpty() ? 0 : totalYield / data.size();

        row.getChildren().addAll(
                statCard("📦  Total Yield",    String.format("%.0f", totalYield) + " u", "stat-card-green"),
                statCard("🗺  Active Zones",   String.valueOf(zones),                     "stat-card-blue"),
                statCard("📊  Avg per Record", String.format("%.1f", avgYield) + " u",   "stat-card-orange"),
                statCard("📋  Total Records",  String.valueOf(data.size()),               "stat-card-blue")
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

    private HBox buildMainContent() {
        HBox row = new HBox(16);
        VBox.setVgrow(row, Priority.ALWAYS);

        VBox tableCard = buildTableCard();
        VBox chartCard = buildChartCard();

        HBox.setHgrow(tableCard, Priority.ALWAYS);
        row.getChildren().addAll(tableCard, chartCard);
        return row;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");
        VBox.setVgrow(box, Priority.ALWAYS);

        Label title = new Label("All Production Records");
        title.getStyleClass().add("section-title");

        TableView<ProductionRecord> table = new TableView<>(data);
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<ProductionRecord, String> colZone = new TableColumn<>("Zone");
        colZone.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getZoneCode()));

        TableColumn<ProductionRecord, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getProductionType().name()));

        TableColumn<ProductionRecord, String> colValue = new TableColumn<>("Value");
        colValue.setCellValueFactory(c ->
                new SimpleStringProperty(
                        String.format("%.1f %s", c.getValue().getValue(), c.getValue().getUnit())
                ));

        TableColumn<ProductionRecord, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDate().toString()));

        TableColumn<ProductionRecord, Void> colDel = new TableColumn<>("");
        colDel.setPrefWidth(50);
        colDel.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑");
            { btn.getStyleClass().add("btn-danger");
                btn.setPadding(new Insets(2, 6, 2, 6));
                btn.setOnAction(e -> {
                    ProductionRecord r = getTableView().getItems().get(getIndex());
                    data.remove(r);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(colZone, colType, colValue, colDate, colDel);
        box.getChildren().addAll(title, table);
        return box;
    }

    private VBox buildChartCard() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");
        box.setMinWidth(280);
        box.setMaxWidth(320);

        Label title = new Label("📊  Yield by Zone");
        title.getStyleClass().add("section-title");

        // Bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Zone");
        yAxis.setLabel("Yield");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setStyle("-fx-background-color:transparent;");
        VBox.setVgrow(chart, Priority.ALWAYS);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // Aggregate by zone
        data.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ProductionRecord::getZoneCode,
                        java.util.stream.Collectors.summingDouble(ProductionRecord::getValue)
                ))
                .forEach((zone, total) ->
                        series.getData().add(new XYChart.Data<>(zone, total))
                );
        chart.getData().add(series);
        chart.setStyle("-fx-background-color:#0f0f1a;-fx-text-fill:#ccccee;");

        // Type breakdown list
        Label breakTitle = new Label("By Production Type");
        breakTitle.getStyleClass().add("card-title");

        VBox breakdown = new VBox(6);
        data.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getProductionType().name(),
                        java.util.stream.Collectors.summingDouble(ProductionRecord::getValue)
                ))
                .forEach((type, total) -> {
                    HBox row = new HBox(8);
                    row.setAlignment(Pos.CENTER_LEFT);
                    Label typeLbl = new Label(type);
                    typeLbl.getStyleClass().add("detail-key");
                    Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                    Label valLbl = new Label(String.format("%.0f", total));
                    valLbl.getStyleClass().add("detail-value");
                    row.getChildren().addAll(typeLbl, sp, valLbl);
                    breakdown.getChildren().add(row);
                });

        box.getChildren().addAll(title, chart, breakTitle, breakdown);
        return box;
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void showAddDialog() {
        Dialog<ProductionRecord> dialog = new Dialog<>();
        dialog.setTitle("Add Production Record");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField fZone  = new TextField(); fZone.setPromptText("Zone code");
        TextField fValue = new TextField(); fValue.setPromptText("Numeric value");
        TextField fUnit  = new TextField(); fUnit.setPromptText("e.g. kg, L, eggs");
        ComboBox<ProductionType> cmbType = new ComboBox<>(
                FXCollections.observableArrayList(ProductionType.values())
        );
        cmbType.setValue(ProductionType.CROP_YIELD);

        grid.addRow(0, new Label("Zone Code:"),       fZone);
        grid.addRow(1, new Label("Production Type:"), cmbType);
        grid.addRow(2, new Label("Value:"),           fValue);
        grid.addRow(3, new Label("Unit:"),            fUnit);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    return controller.createRecord(
                            fZone.getText(),
                            cmbType.getValue(),
                            Double.parseDouble(fValue.getText()),
                            fUnit.getText()
                    );
                } catch (NumberFormatException e) { return null; }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(r -> { if (r != null) data.add(r); });
    }

    private void exportCSV() {
        javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,
                "CSV export — wire to a FileChooser + BufferedWriter in your save logic.",
                ButtonType.OK);
        info.setHeaderText("Export Production Records");
        info.showAndWait();
    }
}
