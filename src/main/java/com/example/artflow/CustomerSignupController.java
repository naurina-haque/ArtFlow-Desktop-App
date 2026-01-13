package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class CustomerSignupController {

        @FXML
        private Button signup4;
        @FXML
        private Button back4;
        @FXML
        private Label login4;

        @FXML
        private TextField firstNameField;
        @FXML
        private TextField lastNameField;
        @FXML
        private TextField emailField;
        @FXML
        private PasswordField passwordField;
        @FXML
        private PasswordField confirmPasswordField;

        private DatabaseHelper dbHelper;

        public void initialize()
        {
            dbHelper = DatabaseHelper.getInstance();

            signup4.setOnAction(e-> handleSignup());

            back4.setOnAction(e->{
                try {
                    Stage stage = (Stage) back4.getScene().getWindow();
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
            login4.setOnMouseClicked(e->{
                try {
                    Stage stage = (Stage) login4.getScene().getWindow();
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

        private void handleSignup() {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            String fullName = (firstName + " " + lastName).trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("Error", "All fields are required!");
                return;
            }
            if (!password.equals(confirmPassword)) {
                showAlert("Error", "Passwords do not match!");
                return;
            }
            if (password.length() < 6) {
                showAlert("Error", "Password must be at least 6 characters");
                return;
            }

            try {
                boolean success = dbHelper.signupUser(firstName, lastName, email.toLowerCase(), password, "customer");
                if (success) {
                    showShortAlert("Sign up complete");
                    // navigate to login (customer currently uses CustomerLogin view)
                    Stage stage = (Stage) signup4.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerLogin.fxml"));
                    Scene scene= new Scene(loader.load(),600,400);
                    double width = stage.getWidth();
                    double height = stage.getHeight();
                    stage.setScene(scene);
                    stage.setHeight(height);
                    stage.setWidth(width);
                } else {
                    showAlert("Error", "Sign up failed. Email may already be registered.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Unexpected error: " + e.getMessage());
            }
        }

        private void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        private void showShortAlert(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }


}
