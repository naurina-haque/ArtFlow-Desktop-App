package com.example.artflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Splashscreen extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Diagnostic: ensure DB initialized and print test user info
        try {
            DatabaseHelper db = DatabaseHelper.getInstance();
            String info = db.getUserDebugInfo("artist@test.local");
            System.out.println("DB debug on startup: " + info);
            String cred = db.checkCredentials("artist@test.local", "password", "artist");
            System.out.println("DB credential check (artist@test.local/password): " + cred);
        } catch (Exception e) {
            System.err.println("Error initializing DB in Splashscreen: " + e.getMessage());
            e.printStackTrace();
        }

        FXMLLoader fxmlLoader1 = new FXMLLoader(Splashscreen.class.getResource("splash.fxml"));
        Scene scene1 = new Scene(fxmlLoader1.load(), 1200, 700);

        stage.setTitle("ArtFlow");
        stage.setScene(scene1);
        stage.setResizable(true);
        stage.centerOnScreen();

        stage.show();



    }

}
