package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Locale;

@SuppressWarnings("unused")
public class ArtistDashboardController {

    @SuppressWarnings("unused")
    @FXML
    private AnchorPane manifestModal;

    @SuppressWarnings("unused")
    @FXML
    private Button manifestBtn;

    @SuppressWarnings("unused")
    @FXML
    private Button closeModalBtn;

    @SuppressWarnings("unused")
    @FXML
    private Button composeBtn;

    @SuppressWarnings("unused")
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
    private Label firstNameLabel;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label logoutLabel;

    private static final Logger LOGGER = Logger.getLogger(ArtistDashboardController.class.getName());
    private String currentUserType;

    @FXML
    public void initialize() {
        if (manifestModal != null) {
            manifestModal.setVisible(false);
        }
    }

    public void setProfileName(String fullName) {
        if (profileNameLabel != null && fullName != null) {
            profileNameLabel.setText(fullName);
        }
        if (firstNameLabel != null && fullName != null) {
            String first = fullName.trim().split("\\s+")[0];
            firstNameLabel.setText(first);
        }
    }

    public void setWelcomeFirstName(String firstName) {
        if (firstNameLabel != null && firstName != null) {
            firstNameLabel.setText(firstName);
        }
    }

    public void setCurrentUserType(String userType) {
        this.currentUserType = userType;
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        System.out.println("Logout clicked. currentUserType=" + currentUserType + ", event=" + event);
        try {
            Stage stage = null;
            if (event != null && event.getSource() != null) {
                Object src = event.getSource();
                if (src instanceof javafx.scene.Node) {
                    stage = (Stage) ((javafx.scene.Node) src).getScene().getWindow();
                }
            }

            if (stage == null) {
                System.err.println("Logout: could not determine Stage from event; falling back to profileNameLabel");
                if (profileNameLabel != null && profileNameLabel.getScene() != null) {
                    stage = (Stage) profileNameLabel.getScene().getWindow();
                }
            }

            if (stage == null) {
                System.err.println("Logout failed: no stage available");
                showAlert(Alert.AlertType.ERROR, "Logout failed", "Unable to determine application window for logout.");
                return;
            }

            String fxmlName;
            if ("artist".equalsIgnoreCase(currentUserType)) {
                fxmlName = "ArtistLogin.fxml";
            } else if ("customer".equalsIgnoreCase(currentUserType)) {
                fxmlName = "CustomerLogin.fxml";
            } else {
                fxmlName = "Select.fxml";
            }

            String resourcePath = "/com/example/artflow/" + fxmlName;
            URL resUrl = getClass().getResource(resourcePath);
            if (resUrl == null) {
                String msg = "FXML resource not found at absolute path: " + resourcePath;
                System.err.println(msg);
                showAlert(Alert.AlertType.ERROR, "Logout error", msg);
                return;
            }

            System.out.println("Loading FXML for logout: " + resUrl);
            FXMLLoader loader = new FXMLLoader(resUrl);

            Scene scene = new Scene(loader.load(), 600, 400);
            double width = stage.getWidth();
            double height = stage.getHeight();
            stage.setScene(scene);
            stage.setHeight(height);
            stage.setWidth(width);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Logout error", e);
            showAlert(Alert.AlertType.ERROR, "Logout error", e.getMessage());
        }
    }

    @FXML
    private void openMyArtworks(MouseEvent event) {
        System.out.println("Opening My Artworks from side menu");
        Stage stage = null;
        if (event != null && event.getSource() != null) {
            Object src = event.getSource();
            if (src instanceof javafx.scene.Node) {
                stage = (Stage) ((javafx.scene.Node) src).getScene().getWindow();
            }
        }

        if (stage == null) {
            System.err.println("openMyArtworks: could not determine Stage from event; falling back to profileNameLabel");
            if (profileNameLabel != null && profileNameLabel.getScene() != null) {
                stage = (Stage) profileNameLabel.getScene().getWindow();
            }
        }

        if (stage == null) {
            System.err.println("openMyArtworks failed: no stage available");
            showAlert(Alert.AlertType.ERROR, "Navigation failed", "Unable to determine application window for navigation.");
            return;
        }

        String resourcePath = "/com/example/artflow/ArtistMyArtwork.fxml";
        URL resUrl = getClass().getResource(resourcePath);
        if (resUrl == null) {
            String msg = "MyArtwork FXML resource not found at: " + resourcePath;
            System.err.println(msg);
            showAlert(Alert.AlertType.ERROR, "Navigation error", msg);
            return;
        }

        System.out.println("Loading MyArtworks FXML: " + resUrl);
        FXMLLoader loader = new FXMLLoader(resUrl);
        Scene scene;
        try {
            scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load MyArtworks", ex);
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            showAlert(Alert.AlertType.ERROR, "Navigation error", "Failed to load MyArtworks: " + ex.getMessage() + "\nSee details in console.");
            // also print stacktrace to a dialog for convenience
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Load error");
            a.setHeaderText("Failed to open My Artworks");
            a.getDialogPane().setPrefWidth(600);
            a.setContentText(sw.toString());
            a.showAndWait();
            return;
        }

        // controller's initialize() is invoked by FXMLLoader; ask it to select the 'artworks' sidebar
        ArtistMyArtworkController controller = loader.getController();
        if (controller != null) controller.selectMenu("artworks");
        stage.setScene(scene);

    }

    // Allow other views to ask this dashboard to highlight a sidebar item if needed
    public void selectMenu(String menuKey) {
        if (menuKey == null) return;
        try {
            // clear existing selection
            if (dashboardHBox != null) dashboardHBox.getStyleClass().remove("selected");
            if (artworksHBox != null) artworksHBox.getStyleClass().remove("selected");
            if (ordersHBox != null) ordersHBox.getStyleClass().remove("selected");
            if (earningsHBox != null) earningsHBox.getStyleClass().remove("selected");
            if (profileHBox != null) profileHBox.getStyleClass().remove("selected");
            if (logoutHBox != null) logoutHBox.getStyleClass().remove("selected");

            switch (menuKey.toLowerCase(Locale.ROOT)) {
                case "dashboard": if (dashboardHBox != null) dashboardHBox.getStyleClass().add("selected"); break;
                case "artworks": if (artworksHBox != null) artworksHBox.getStyleClass().add("selected"); break;
                case "orders": if (ordersHBox != null) ordersHBox.getStyleClass().add("selected"); break;
                case "earnings": if (earningsHBox != null) earningsHBox.getStyleClass().add("selected"); break;
                case "profile": if (profileHBox != null) profileHBox.getStyleClass().add("selected"); break;
                default: break;
            }
        } catch (Exception ignored) {}
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
