package com.farm.demo;

import com.farm.demo.view.MainLayout;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainLayout root = new MainLayout();

        Scene scene = new Scene(root, 1200, 650);

        primaryStage.setTitle("Smart Farm IoT Suite");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}