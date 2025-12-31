package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class SelectController {
    @FXML
    private Button customer;

    @FXML
    private Button artist;

    public void initialize()
    {
        artist.setOnAction(e->{
            try {
            Stage stage = (Stage) artist.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ArtistLogin.fxml"));
            Scene scene= new Scene(loader.load(),600,400);
            stage.setScene(scene);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        });
        customer.setOnAction(e->{
            try {
                Stage stage = (Stage) customer.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerLogin.fxml"));
                Scene scene= new Scene(loader.load(),600,400);
                double width = stage.getWidth();
                double height = stage.getHeight();
                stage.setScene(scene);
                stage.setHeight(height);
                stage.setWidth(width);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        });
    }
}
