package com.example.artflow;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {
    @FXML
    private ProgressBar progressbar;


    public void initialize() {
        progressbar.setStyle("-fx-accent:#4a1d7c");
        KeyValue kv = new KeyValue(progressbar.progressProperty(), 1);
        KeyFrame kf = new KeyFrame(Duration.seconds(2), kv);
        Timeline timeline = new Timeline(kf);

        timeline.setOnFinished(e -> {
            try {
                Stage stage = (Stage) progressbar.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Select.fxml"));
                Scene scene = new Scene(loader.load(), 600, 400);
                double width = stage.getWidth();
                double height = stage.getHeight();
                stage.setScene(scene);
                stage.setHeight(height);
                stage.setWidth(width);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        timeline.play();


    }


}
