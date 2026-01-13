package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtistProfileController {

    @FXML
    private VBox sidebar;

    @FXML
    private HBox dashboardHBox;
    @FXML
    private HBox artworksHBox;
    @FXML
    private HBox ordersHBox;
    @FXML
    private HBox earningsHBox;
    @FXML
    private HBox profileHBox;
    @FXML
    private HBox logoutHBox;

    @FXML
    private Label profileNameLabel;

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    
    @FXML
    private Button saveButton;

    private static final Logger LOGGER = Logger.getLogger(ArtistProfileController.class.getName());

    @FXML
    public void initialize() {
        try {
            // Set profile name from current user
            String fullName = CurrentUser.getFullName();
            String email = CurrentUser.getEmail();
            
            if (fullName != null) {
                if (profileNameLabel != null) {
                    profileNameLabel.setText(fullName);
                }
                
                // Populate fields
                if (nameField != null) nameField.setText(fullName);
            }
            
            if (email != null && emailField != null) {
                emailField.setText(email);
            }
            
            // Load additional profile data from database
            if (email != null) {
                DatabaseHelper dbHelper = DatabaseHelper.getInstance();
                java.util.Map<String, String> profile = dbHelper.getUserProfile(email);
                if (profile != null) {
                    String phone = profile.get("phone");
                    String address = profile.get("address");
                    if (phone != null && phoneField != null) phoneField.setText(phone);
                    if (address != null && addressField != null) addressField.setText(address);
                }
            }
            
            // Add listeners to text fields to enable save button when any field changes
            if (nameField != null) {
                nameField.textProperty().addListener((obs, oldVal, newVal) -> enableSaveButton());
            }
            if (emailField != null) {
                emailField.textProperty().addListener((obs, oldVal, newVal) -> enableSaveButton());
            }
            if (phoneField != null) {
                phoneField.textProperty().addListener((obs, oldVal, newVal) -> enableSaveButton());
            }
            if (addressField != null) {
                addressField.textProperty().addListener((obs, oldVal, newVal) -> enableSaveButton());
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Profile controller", ex);
        }
    }
    
    private void enableSaveButton() {
        if (saveButton != null) {
            saveButton.setDisable(false);
        }
    }
    
    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String currentEmail = CurrentUser.getEmail();

        if (name.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Name and Email are required fields");
            return;
        }

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showAlert("Error", "Please enter a valid email address");
            return;
        }

        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            
            // Check if email is being changed and if the new email already exists
            if (!email.equals(currentEmail)) {
                // Check if the new email is already in use
                String existingUser = dbHelper.getUserDebugInfo(email);
                if (existingUser != null && !existingUser.equals("NO_USER")) {
                    showAlert("Error", "This email is already in use by another account.");
                    return;
                }
            }
            
            // Update the current user's name in the CurrentUser class
            CurrentUser.setFullName(name);
            
            // Update the profile name label
            if (profileNameLabel != null) {
                profileNameLabel.setText(name);
            }
            
            // Save to database
            boolean success = dbHelper.updateUserProfile(
                currentEmail, // current email
                name,
                email,
                phone,
                address
            );
            
            if (success) {
                // Update the email in CurrentUser if it was changed
                if (!email.equals(currentEmail)) {
                    CurrentUser.setEmail(email);
                }
                
                showAlert("Success", "Profile updated successfully!");
                saveButton.setDisable(true);
            } else {
                showAlert("Error", "Failed to update profile. Please try again.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating profile", e);
            showAlert("Error", "An error occurred while updating your profile: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void openDashboard(MouseEvent e) {
        navigateTo("/com/example/artflow/ArtistDashboard.fxml", e);
    }

    @FXML
    private void openMyArtworks(MouseEvent e) {
        navigateTo("/com/example/artflow/ArtistMyArtwork.fxml", e);
    }

    @FXML
    private void openOrders(MouseEvent e) {
        navigateTo("/com/example/artflow/ArtistCompletedOrders.fxml", e);
    }

    @FXML
    private void openProfile(MouseEvent e) {
        // Already on profile page
    }

    @FXML
    private void handleLogout(MouseEvent e) {
        try {
            CurrentUser.setFullName(null);
            CurrentUser.setUserType(null);
            CurrentUser.setEmail(null);
            navigateTo("/com/example/artflow/ArtistLogin.fxml", e);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Logout failed", ex);
        }
    }

    private void navigateTo(String fxmlPath, MouseEvent e) {
        try {
            javafx.scene.Node src = (javafx.scene.Node) e.getSource();
            Stage stage = (Stage) src.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Navigation failed to " + fxmlPath, ex);
        }
    }
}
