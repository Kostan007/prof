package com.example.agrimonitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AgriMonitorApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AgriMonitorApp.class.getResource("/com/example/agrimonitor/main.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1450, 920);
        scene.getStylesheets().add(AgriMonitorApp.class.getResource("/com/example/agrimonitor/styles.css").toExternalForm());

        stage.setTitle("AgriMonitor — Мониторинг стресса растений");
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}