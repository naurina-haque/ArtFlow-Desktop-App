package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class ArtistLoginController {
    @FXML
    private Button back;
    @FXML
    private ImageView imageview;

    @FXML
    private Label signup;

    @FXML
    private Button login;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private DatabaseHelper dbHelper;

    public void initialize()
    {
        dbHelper = DatabaseHelper.getInstance();

        imageview.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                imageview.fitWidthProperty().bind(newScene.widthProperty());
                imageview.fitHeightProperty().bind(newScene.heightProperty());
            }
        });

        signup.setOnMouseClicked(e->{
            try {
                Stage stage = (Stage) signup.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ArtistSignup.fxml"));
                Scene scene= new Scene(loader.load(),600,400);
                double width = stage.getWidth();
                double height = stage.getHeight();
                stage.setScene(scene);
                stage.setHeight(height);
                stage.setWidth(width);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });

        back.setOnAction(e->{
            try {
                Stage stage = (Stage) back.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Select.fxml"));
                Scene scene= new Scene(loader.load(),600,400);
                double width = stage.getWidth();
                double height = stage.getHeight();
                stage.setScene(scene);
                stage.setHeight(height);
                stage.setWidth(width);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });
        login.setOnAction(e->{
            handleLogin();
        });
    }

    private void handleLogin() {
        String email = emailField.getText().trim().toLowerCase();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter email and password.");
            return;
        }

        String fullName = dbHelper.loginUser(email, password, "artist");
        if (fullName != null) {
            try {
                Stage stage = (Stage) login.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/artflow/ArtistDashboard.fxml"));
                Scene scene= new Scene(loader.load(),600,400);
                ArtistDashboardController controller = loader.getController();
                controller.setProfileName(fullName);
                String firstName = fullName.split(" ")[0];
                controller.setWelcomeFirstName(firstName);
                controller.setCurrentUserType("artist");

                double width = stage.getWidth();
                double height = stage.getHeight();
                stage.setScene(scene);
                stage.setHeight(height);
                stage.setWidth(width);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid credentials.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
