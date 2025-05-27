package com.image.imageapp;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        AppLogger.log(AppLogger.Level.INFO, "Aplikacja uruchomiona");
        primaryStage.setOnCloseRequest(e -> AppLogger.log(AppLogger.Level.INFO, "Aplikacja zamknięta"));
        new ImageApp();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
