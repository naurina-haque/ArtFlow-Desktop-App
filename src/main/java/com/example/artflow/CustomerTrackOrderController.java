package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerTrackOrderController {

    @FXML
    private VBox sidebar; // included fragment root

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
    private TableView<OrderModel> ordersTable;

    @FXML
    private TableColumn<OrderModel, String> colId;
    @FXML
    private TableColumn<OrderModel, String> colArtistName;
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

    private static final Logger LOGGER = Logger.getLogger(CustomerTrackOrderController.class.getName());

    @FXML
    public void initialize() {
        try {
            // Set profile name from current user
            if (profileNameLabel != null && CurrentUser.getFullName() != null) {
                profileNameLabel.setText(CurrentUser.getFullName());
            }

            // Configure table columns
            if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            if (colArtistName != null) colArtistName.setCellValueFactory(new PropertyValueFactory<>("artistName"));
            if (colArtTitle != null) colArtTitle.setCellValueFactory(new PropertyValueFactory<>("artTitle"));
            if (colQuantity != null) colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            if (colOrderedOn != null) colOrderedOn.setCellValueFactory(new PropertyValueFactory<>("orderedOn"));
            if (colAmount != null) colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
            if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Load orders for current customer
            loadCustomerOrders();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Track Order controller", ex);
        }
    }

    private void loadCustomerOrders() {
        try {
            String customerName = CurrentUser.getFullName();
            if (customerName == null) {
                LOGGER.warning("No current user found");
                return;
            }

            List<OrderModel> allOrders = DatabaseHelper.getInstance().listOrders();
            
            if (allOrders != null) {
                ordersTable.getItems().clear();
                for (OrderModel order : allOrders) {
                    // Filter orders by customer name
                    if (order != null && customerName.equalsIgnoreCase(order.getCustomerName())) {
                        ordersTable.getItems().add(order);
                    }
                }
                LOGGER.info("Loaded " + ordersTable.getItems().size() + " orders for customer: " + customerName);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load customer orders", ex);
        }
    }

    @FXML
    private void openDashboard(MouseEvent e) {
        navigateTo("/com/example/artflow/CustomerDashboard.fxml", e);
    }

    @FXML
    private void openOrders(MouseEvent e) {
        // Already on orders page
    }

    @FXML
    private void openProfile(MouseEvent e) {
        navigateTo("/com/example/artflow/CustomerProfile.fxml", e);
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
