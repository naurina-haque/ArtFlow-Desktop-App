package com.example.artflow;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtistCompletedOrdersController {

    @FXML
    private VBox sidebar; // included fragment root

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
    private TableView<OrderModel> completedOrdersTable;

    @FXML
    private TableColumn<OrderModel, String> colId;
    @FXML
    private TableColumn<OrderModel, String> colCustomerName;
    @FXML
    private TableColumn<OrderModel, String> colArtTitle;
    @FXML
    private TableColumn<OrderModel, Integer> colQuantity;
    @FXML
    private TableColumn<OrderModel, String> colOrderedOn;
    @FXML
    private TableColumn<OrderModel, Double> colAmount;
    @FXML
    private TableColumn<OrderModel, String> colStatus;
    @FXML
    private TableColumn<OrderModel, Void> colActions;

    private static final Logger LOGGER = Logger.getLogger(ArtistCompletedOrdersController.class.getName());

    @FXML
    public void initialize() {
        try {
            if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            if (colCustomerName != null) colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            if (colArtTitle != null) colArtTitle.setCellValueFactory(new PropertyValueFactory<>("artTitle"));
            if (colQuantity != null) colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            if (colOrderedOn != null) colOrderedOn.setCellValueFactory(new PropertyValueFactory<>("orderedOn"));
            if (colAmount != null) colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
            if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

            // load only completed orders for this artist
            String artistName = CurrentUser.getFullName();
            if (artistName == null || artistName.trim().isEmpty()) {
                if (profileNameLabel != null) {
                    artistName = profileNameLabel.getText();
                }
            }
            if (artistName == null) artistName = "";

            List<OrderModel> all = DatabaseHelper.getInstance().listOrders();
            if (all != null) {
                for (OrderModel o : all) {
                    if (o == null) continue;
                    String status = o.getStatus() != null ? o.getStatus().toLowerCase() : "";
                    String an = o.getArtistName();
                    if (an == null) an = "";
                    
                    // Only show completed orders for this artist
                    if ("completed".equals(status) && 
                        (an.equalsIgnoreCase(artistName) || an.toLowerCase().contains(artistName.toLowerCase()))) {
                        completedOrdersTable.getItems().add(o);
                    }
                }
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Completed Orders controller", ex);
        }

        // Sidebar navigation is handled centrally by ArtistSidebarController.
    }

    private void openDashboard(MouseEvent e) {
        try {
            javafx.scene.Node src = (javafx.scene.Node) e.getSource();
            javafx.stage.Stage stage = (javafx.stage.Stage) src.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/artflow/ArtistDashboard.fxml"));
            javafx.scene.Scene s = new javafx.scene.Scene(loader.load(), stage.getWidth(), stage.getHeight());
            ArtistDashboardController c = loader.getController();
            if (c != null) c.selectMenu("dashboard");
            stage.setScene(s);
        } catch (Exception ex) { LOGGER.log(Level.SEVERE, "Navigation error to dashboard", ex); }
    }

    private void openMyArtworks(MouseEvent e) {
        try {
            javafx.scene.Node src = (javafx.scene.Node) e.getSource();
            javafx.stage.Stage stage = (javafx.stage.Stage) src.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/artflow/ArtistMyArtwork.fxml"));
            javafx.scene.Scene s = new javafx.scene.Scene(loader.load(), stage.getWidth(), stage.getHeight());
            ArtistMyArtworkController c = loader.getController();
            if (c != null) c.selectMenu("artworks");
            stage.setScene(s);
        } catch (Exception ex) { LOGGER.log(Level.SEVERE, "Navigation error to my artworks", ex); }
    }

    private void openOrders(MouseEvent e) {
        // already in orders view
    }

    private void openProfile(MouseEvent e) {
        // implement if you have profile view
    }

    private void handleLogout(MouseEvent e) {
        try {
            javafx.scene.Node src = (javafx.scene.Node) e.getSource();
            javafx.stage.Stage stage = (javafx.stage.Stage) src.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/artflow/ArtistLogin.fxml"));
            javafx.scene.Scene s = new javafx.scene.Scene(loader.load(), stage.getWidth(), stage.getHeight());
            stage.setScene(s);
        } catch (Exception ex) { LOGGER.log(Level.SEVERE, "Logout navigation failed", ex); }
    }

    // Recursively attach same mouse handler to parent and all descendant nodes
    private void wireClickable(javafx.scene.Parent parent, javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        if (parent == null || handler == null) return;
        try {
            parent.setOnMouseClicked(handler);
        } catch (Exception ignored) {}
        try {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                try { child.setOnMouseClicked(handler); } catch (Exception ignored) {}
                if (child instanceof javafx.scene.Parent p) wireClickable(p, handler);
            }
        } catch (Exception ignored) {}
    }
}
