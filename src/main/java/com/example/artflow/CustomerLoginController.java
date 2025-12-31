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

public class CustomerLoginController {
        @FXML
        private Button back2;
        @FXML
        private ImageView imageview2;

        @FXML
        private Label signup2;

        @FXML
        private Button login2;

        @FXML
        private TextField emailField;

        @FXML
        private PasswordField passwordField;

        private DatabaseHelper dbHelper;

        public void initialize()
        {
            dbHelper = DatabaseHelper.getInstance();

            imageview2.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    imageview2.fitWidthProperty().bind(newScene.widthProperty());
                    imageview2.fitHeightProperty().bind(newScene.heightProperty());
                }
            });

            signup2.setOnMouseClicked(e->{
                try {
                    Stage stage = (Stage) signup2.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerSignup.fxml"));
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

            back2.setOnAction(e->{
                try {
                    Stage stage = (Stage) back2.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Select.fxml"));
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
            login2.setOnAction(e->{
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

            String fullName = dbHelper.loginUser(email, password, "customer");
            if (fullName != null) {
                try {
                    Stage stage = (Stage) login2.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/artflow/CustomerDashboard.fxml"));
                    Scene scene= new Scene(loader.load(),600,400);
                    // CustomerDashboard.fxml uses ArtistDashboardController for the shared layout
                    ArtistDashboardController controller = loader.getController();
                    controller.setProfileName(fullName);
                    String firstName = fullName.split(" ")[0];
                    controller.setWelcomeFirstName(firstName);
                    controller.setCurrentUserType("customer");

                    double width = stage.getWidth();
                    double height = stage.getHeight();
                    stage.setScene(scene);
                    stage.setHeight(height);
                    stage.setWidth(width);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Unable to open dashboard: " + ex.getMessage());
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
