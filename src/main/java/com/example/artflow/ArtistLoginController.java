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
        // Ensure click is captured both via action and mouse click (defensive)
        if (login != null) {
            login.setOnAction(e -> {
                System.out.println("ArtistLoginController: login button action fired");
                handleLogin();
            });
            login.setOnMouseClicked(e -> {
                System.out.println("ArtistLoginController: login button mouseClicked fired");
                // also call handleLogin to be defensive in case Action isn't wired
                handleLogin();
            });
        } else {
            System.err.println("ArtistLoginController: login Button is null in initialize()");
        }
    }

    private void handleLogin() {
        try {
            System.out.println("ArtistLoginController: handleLogin invoked");
            String email = (emailField == null || emailField.getText() == null) ? "" : emailField.getText().trim().toLowerCase();
            String password = (passwordField == null) ? "" : passwordField.getText();

            System.out.println("ArtistLoginController: email='" + email + "' passwordPresent=" + (!password.isEmpty()));

            if (email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter email and password.");
                return;
            }

            if (dbHelper == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Database not initialized.");
                System.err.println("ArtistLoginController: dbHelper is null");
                return;
            }

            // Pre-check credentials to give better feedback
            String status = dbHelper.checkCredentials(email, password, "artist");
            System.out.println("ArtistLoginController: credential check for '" + email + "' -> " + status);
            if (status == null || status.startsWith("ERROR:")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Login failed due to internal error.");
                return;
            }
            switch (status) {
                case "NO_USER":
                    showAlert(Alert.AlertType.ERROR, "No account", "No account found for this email.");
                    return;
                case "WRONG_PASSWORD":
                    showAlert(Alert.AlertType.ERROR, "Invalid password", "Incorrect password — please try again.");
                    return;
                default:
                    if (status.startsWith("WRONG_TYPE:")) {
                        String ut = status.substring("WRONG_TYPE:".length());
                        showAlert(Alert.AlertType.ERROR, "Wrong account type", "This email is registered as '" + ut + "' not as artist.");
                        return;
                    }
                    break;
            }

            // Credentials OK — perform loginUser to retrieve fullName and proceed
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
                    // set global current user
                    CurrentUser.setFullName(fullName);
                    CurrentUser.setUserType("artist");

                    double width = stage.getWidth();
                    double height = stage.getHeight();
                    stage.setScene(scene);
                    stage.setHeight(height);
                    stage.setWidth(width);
                    System.out.println("ArtistLoginController: login successful, opened ArtistDashboard");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unexpected error during login: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // FXML onAction handler (ensures login is invoked when button's onAction is set in FXML)
    public void onLogin(javafx.event.ActionEvent event) {
        System.out.println("ArtistLoginController: onLogin(ActionEvent) invoked from FXML");
        handleLogin();
    }
}
