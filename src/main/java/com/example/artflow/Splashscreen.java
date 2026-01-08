package com.example.artflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Splashscreen extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader1 = new FXMLLoader(Splashscreen.class.getResource("splash.fxml"));
        Scene scene1 = new Scene(fxmlLoader1.load(), 1000, 600);

        stage.setTitle("ArtFlow");
        stage.setScene(scene1);
        stage.setResizable(true);
        stage.centerOnScreen();

        stage.show();



    }

}
