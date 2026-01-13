package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtistSidebarController {

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
    private Label logoutLabel;

    private static final Logger LOGGER = Logger.getLogger(ArtistSidebarController.class.getName());

    @FXML
    public void initialize() {
        // wire recursively so clicks on image/label also trigger navigation
        try {
            if (dashboardHBox != null) wireClickable(dashboardHBox, this::openDashboard);
            if (artworksHBox != null) wireClickable(artworksHBox, this::openMyArtworks);
            if (ordersHBox != null) wireClickable(ordersHBox, this::openOrders);
            if (earningsHBox != null) wireClickable(earningsHBox, this::openEarnings);
            if (profileHBox != null) wireClickable(profileHBox, this::openProfile);
            if (logoutHBox != null) wireClickable(logoutHBox, this::handleLogout);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to wire sidebar handlers", ex);
        }
    }

    private void openDashboard(MouseEvent e) {
        navigateTo(e, "/com/example/artflow/ArtistDashboard.fxml", "dashboard");
    }

    private void openMyArtworks(MouseEvent e) {
        navigateTo(e, "/com/example/artflow/ArtistMyArtwork.fxml", "artworks");
    }

    private void openOrders(MouseEvent e) {
        navigateTo(e, "/com/example/artflow/ArtistCompletedOrders.fxml", "orders");
    }

    private void openEarnings(MouseEvent e) {
        // if you have an earnings view, replace with the path; for now no-op but select menu
        selectLocal("earnings");
    }

    private void openProfile(MouseEvent e) {
        navigateTo(e, "/com/example/artflow/ArtistProfile.fxml", "profile");
    }

    private void handleLogout(MouseEvent e) {
        try {
            Stage stage = findStage(e);
            if (stage == null) return;
            URL res = getClass().getResource("/com/example/artflow/ArtistLogin.fxml");
            if (res == null) {
                System.err.println("ArtistLogin.fxml not found");
                return;
            }
            FXMLLoader loader = new FXMLLoader(res);
            Scene s = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            stage.setScene(s);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Logout navigation failed", ex);
        }
    }

    // central navigate helper: load FXML into the current stage and call selectMenu(menuKey) on its controller if available
    private void navigateTo(MouseEvent e, String resourcePath, String menuKey) {
        try {
            Stage stage = findStage(e);
            if (stage == null) return;
            URL res = getClass().getResource(resourcePath);
            if (res == null) {
                System.err.println(resourcePath + " not found");
                return;
            }
            FXMLLoader loader = new FXMLLoader(res);
            Scene s = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            Object ctrl = loader.getController();
            // if destination has selectMenu(String), call it
            try {
                java.lang.reflect.Method m = ctrl == null ? null : ctrl.getClass().getMethod("selectMenu", String.class);
                if (m != null) m.invoke(ctrl, menuKey);
            } catch (NoSuchMethodException ignored) {
                // ignore if method doesn't exist
            }
            stage.setScene(s);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Navigation failed to " + resourcePath, ex);
        }
    }

    // select style locally on this sidebar instance (useful when scene not changing)
    private void selectLocal(String menuKey) {
        try {
            dashboardHBox.getStyleClass().remove("selected");
            artworksHBox.getStyleClass().remove("selected");
            ordersHBox.getStyleClass().remove("selected");
            earningsHBox.getStyleClass().remove("selected");
            profileHBox.getStyleClass().remove("selected");
            logoutHBox.getStyleClass().remove("selected");

            switch (menuKey == null ? "" : menuKey.toLowerCase()) {
                case "dashboard": if (dashboardHBox != null) dashboardHBox.getStyleClass().add("selected"); break;
                case "artworks": if (artworksHBox != null) artworksHBox.getStyleClass().add("selected"); break;
                case "orders": if (ordersHBox != null) ordersHBox.getStyleClass().add("selected"); break;
                case "earnings": if (earningsHBox != null) earningsHBox.getStyleClass().add("selected"); break;
                case "profile": if (profileHBox != null) profileHBox.getStyleClass().add("selected"); break;
                default: break;
            }
        } catch (Exception ignored) {}
    }

    // Find the Stage from the mouse event or fallback to any showing Stage
    private Stage findStage(MouseEvent e) {
        try {
            if (e != null && e.getSource() instanceof javafx.scene.Node) {
                javafx.scene.Node n = (javafx.scene.Node) e.getSource();
                if (n.getScene() != null && n.getScene().getWindow() instanceof Stage) return (Stage) n.getScene().getWindow();
            }
        } catch (Exception ignored) {}
        try {
            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                if (w instanceof Stage st && st.isShowing()) return st;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Recursively attach handler to parent and child nodes
    private void wireClickable(javafx.scene.Parent parent, javafx.event.EventHandler<MouseEvent> handler) {
        if (parent == null || handler == null) return;
        try { parent.setOnMouseClicked(handler); } catch (Exception ignored) {}
        try {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                try { child.setOnMouseClicked(handler); } catch (Exception ignored) {}
                if (child instanceof javafx.scene.Parent p) wireClickable(p, handler);
            }
        } catch (Exception ignored) {}
    }
}
