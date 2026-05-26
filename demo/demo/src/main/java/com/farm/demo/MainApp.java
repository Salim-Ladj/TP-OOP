package com.farm.demo;

import com.farm.demo.view.MainLayout;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        MainLayout layout = new MainLayout();
        Scene scene = new Scene(layout, 1200, 700);
        scene.getStylesheets().add(
                getClass().getResource("/com/farm/demo/styles.css").toExternalForm()
        );
        stage.setTitle("SmartFarm Core");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
