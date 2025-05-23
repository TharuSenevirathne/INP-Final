package org.example.finalproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client1 extends Application {

    public static void main(String[] args) {
        launch();
    }
    @Override
    public void start(Stage stage) throws Exception {
        Parent load = FXMLLoader.load(getClass().getResource("/View/Client.fxml"));
        Scene scene = new Scene(load);
        stage.setTitle("Client 1");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.close();
    }
}