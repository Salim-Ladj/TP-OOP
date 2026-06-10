package com.farm.demo;

import com.farm.demo.view.MainLayout;
import com.farm.demo.service.DataService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainLayout root = new MainLayout();

        Scene scene = new Scene(root, 1200, 650);

        // SAVE ON CLOSE
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Saving data to files...");
            DataService.getInstance().saveToDisk();
        });

        primaryStage.setTitle("Smart Farm IoT Suite");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}