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

public class CustomerProfileController {

    @FXML
    private VBox sidebar;

    @FXML
    private HBox dashboardHBox;
    @FXML
    private HBox ordersHBox;
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

    private static final Logger LOGGER = Logger.getLogger(CustomerProfileController.class.getName());

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
        try {
            // Save the updated profile information
            String name = nameField != null ? nameField.getText() : "";
            String email = emailField != null ? emailField.getText() : "";
            String phone = phoneField != null ? phoneField.getText() : "";
            String address = addressField != null ? addressField.getText() : "";
            
            // Update the current user's name and email
            String currentEmail = CurrentUser.getEmail();
            if (name != null && !name.trim().isEmpty()) {
                CurrentUser.setFullName(name.trim());
                if (profileNameLabel != null) {
                    profileNameLabel.setText(name.trim());
                }
            }
            if (email != null && !email.trim().isEmpty()) {
                CurrentUser.setEmail(email.trim());
            }
            
            // Save to database
            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            boolean success = dbHelper.updateUserProfile(
                currentEmail,
                name.trim(),
                email.trim(),
                phone.trim(),
                address.trim()
            );
            
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Profile Updated");
                alert.setHeaderText(null);
                alert.setContentText("Your profile has been updated successfully!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Update Failed");
                alert.setHeaderText(null);
                alert.setContentText("Failed to update profile in database. Please try again.");
                alert.showAndWait();
                return;
            }
            
            // Disable save button after saving
            if (saveButton != null) {
                saveButton.setDisable(true);
            }
            
            LOGGER.info("Profile updated successfully for user: " + name);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to save profile", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save profile changes. Please try again.");
            alert.showAndWait();
        }
    }

    @FXML
    private void openDashboard(MouseEvent e) {
        navigateTo("/com/example/artflow/CustomerDashboard.fxml", e);
    }

    @FXML
    private void openOrders(MouseEvent e) {
        navigateTo("/com/example/artflow/CustomerTrackOrder.fxml", e);
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
            navigateTo("/com/example/artflow/CustomerLogin.fxml", e);
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
