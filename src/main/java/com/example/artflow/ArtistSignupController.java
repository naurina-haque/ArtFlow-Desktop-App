package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class ArtistSignupController {

    @FXML
    private Button signup3;
    @FXML
    private Button back3;
    @FXML
    private Label login3;
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

    public void initialize() {
        dbHelper = DatabaseHelper.getInstance();
        
        signup3.setOnAction(e -> handleSignup());

        back3.setOnAction(e->{
            try {
                Stage stage = (Stage) back3.getScene().getWindow();
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
        login3.setOnMouseClicked(e->{
            try {
                Stage stage = (Stage) login3.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ArtistLogin.fxml"));
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

    @FXML
    private void handleSignup() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        String fullName = (firstName + " " + lastName).trim();

        System.out.println("=== Starting Signup Process ===");
        System.out.println("Name: " + fullName);
        System.out.println("Email: " + email);
        
        // Basic validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            String errorMsg = "All fields are required!";
            System.out.println("Validation failed: " + errorMsg);
            showAlert(Alert.AlertType.ERROR, "Error", errorMsg);
            return;
        }

        if (!password.equals(confirmPassword)) {
            String errorMsg = "Passwords do not match!";
            System.out.println("Validation failed: " + errorMsg);
            showAlert(Alert.AlertType.ERROR, "Error", errorMsg);
            return;
        }

        if (password.length() < 6) {
            String errorMsg = "Password must be at least 6 characters";
            System.out.println("Validation failed: " + errorMsg);
            showAlert(Alert.AlertType.ERROR, "Error", errorMsg);
            return;
        }

        // Email format and signup via DatabaseHelper
        try {
            boolean success = dbHelper.signupUser(firstName, lastName, email.toLowerCase(), password, "artist");
            if (success) {
                System.out.println("User signup successful!");
                showShortAlert("Sign up complete");
                // Clear the form
                firstNameField.clear();
                lastNameField.clear();
                emailField.clear();
                passwordField.clear();
                confirmPasswordField.clear();
                // Navigate to login page
                Stage stage = (Stage) signup3.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ArtistLogin.fxml"));
                Scene scene= new Scene(loader.load(),600,400);
                double width = stage.getWidth();
                double height = stage.getHeight();
                stage.setScene(scene);
                stage.setHeight(height);
                stage.setWidth(width);
            } else {
                String errorMsg = "Sign up failed. Email may already be registered.";
                System.out.println(errorMsg);
                showAlert(Alert.AlertType.ERROR, "Error", errorMsg);
            }
        } catch (Exception e) {
            System.err.println("Unexpected error during sign up:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
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
