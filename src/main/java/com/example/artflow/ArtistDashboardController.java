package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.List;
import java.util.ArrayList;
import javafx.application.Platform;
import com.example.artflow.ArtworkModel;
import com.example.artflow.OrderModel;
import com.example.artflow.CurrentUser;
import com.example.artflow.DatabaseHelper;

@SuppressWarnings("unused")
public class ArtistDashboardController {
    private static final Logger LOGGER = Logger.getLogger(ArtistDashboardController.class.getName());

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
    
    @FXML
    private Label totalArtworksLabel;
    
    @FXML
    private Label totalOrdersLabel;
    
    @FXML
    private Label totalEarningsLabel;

    // Table and columns for recent orders
    @FXML
    private TableView<OrderModel> recentOrdersTable;

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

    private String currentUserType;

    @FXML
    public void initialize() {
        if (manifestModal != null) {
            manifestModal.setVisible(false);
        }

        // configure table columns
        try {
            if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            if (colCustomerName != null) colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            if (colArtTitle != null) colArtTitle.setCellValueFactory(new PropertyValueFactory<>("artTitle"));
            if (colQuantity != null) colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            if (colOrderedOn != null) colOrderedOn.setCellValueFactory(new PropertyValueFactory<>("orderedOn"));
            if (colAmount != null) colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
            if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            // actions column: Accept / Reject buttons
            if (colActions != null) {
                colActions.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
                    private final javafx.scene.control.Button acceptBtn = new javafx.scene.control.Button("Accept");
                    private final javafx.scene.control.Button rejectBtn = new javafx.scene.control.Button("Reject");
                    private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, acceptBtn, rejectBtn);
                    private boolean processing = false;

                    {
                        // Style the buttons
                        acceptBtn.getStyleClass().add("accept-btn");
                        rejectBtn.getStyleClass().add("reject-btn");
                        
                        // Make buttons take equal space
                        acceptBtn.setMaxWidth(Double.MAX_VALUE);
                        rejectBtn.setMaxWidth(Double.MAX_VALUE);
                        box.setFillHeight(true);
                        
                        // Handle Accept button click
                        acceptBtn.setOnAction(e -> {
                            if (processing) return;
                            processing = true;
                            
                            OrderModel order = getTableView().getItems().get(getIndex());
                            if (order != null) {
                                // Show loading state
                                String originalText = acceptBtn.getText();
                                acceptBtn.setText("Processing...");
                                acceptBtn.setDisable(true);
                                rejectBtn.setDisable(true);
                                
                                // Run in background to keep UI responsive
                                new Thread(() -> {
                                    boolean ok = DatabaseHelper.getInstance().updateOrderStatus(order.getId(), "completed");
                                    
                                    // Update UI on JavaFX Application Thread
                                    Platform.runLater(() -> {
                                        if (ok) {
                                            // Show success message
                                            showAlert(Alert.AlertType.INFORMATION, "Success", "Order marked as completed!");
                                            // Refresh the tables
                                            refreshRecentOrders();
                                            updateDashboardCards();
                                        } else {
                                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to accept order. Please try again.");
                                            // Reset button state on failure
                                            resetButtons(originalText);
                                        }
                                    });
                                }).start();
                            }
                        });
                        
                        // Handle Reject button click
                        rejectBtn.setOnAction(e -> {
                            if (processing) return;
                            processing = true;
                            
                            OrderModel order = getTableView().getItems().get(getIndex());
                            if (order != null) {
                                // Show loading state
                                String originalText = rejectBtn.getText();
                                rejectBtn.setText("Processing...");
                                acceptBtn.setDisable(true);
                                rejectBtn.setDisable(true);
                                
                                // Run in background to keep UI responsive
                                new Thread(() -> {
                                    boolean ok = DatabaseHelper.getInstance().updateOrderStatus(order.getId(), "rejected");
                                    
                                    // Update UI on JavaFX Application Thread
                                    Platform.runLater(() -> {
                                        if (ok) {
                                            // Show success message
                                            showAlert(Alert.AlertType.INFORMATION, "Success", "Order has been rejected.");
                                            // Refresh the tables
                                            refreshRecentOrders();
                                            updateDashboardCards();
                                        } else {
                                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject order. Please try again.");
                                            // Reset button state on failure
                                            resetButtons(originalText);
                                        }
                                    });
                                }).start();
                            }
                        });
                    }
                    
                    // Helper method to reset button states
                    private void resetButtons(String originalText) {
                        processing = false;
                        acceptBtn.setText("Accept");
                        rejectBtn.setText(originalText != null ? originalText : "Reject");
                        acceptBtn.setDisable(false);
                        rejectBtn.setDisable(false);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                            setGraphic(null);
                        } else {
                            setGraphic(box);
                        }
                    }
                });
            }

            // Make table responsive: distribute available width proportionally
            if (recentOrdersTable != null) {
                recentOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                // bind column widths as fractions of the table width (adjust ratios as desired)
                if (colId != null) colId.prefWidthProperty().bind(recentOrdersTable.widthProperty().multiply(0.06));
                if (colCustomerName != null) colCustomerName.prefWidthProperty().bind(recentOrdersTable.widthProperty().multiply(0.15));
                if (colArtTitle != null) colArtTitle.prefWidthProperty().bind(recentOrdersTable.widthProperty().multiply(0.22));
                if (colQuantity != null) colQuantity.prefWidthProperty().bind(recentOrdersTable.widthProperty().multiply(0.07));
                if (colOrderedOn != null) colOrderedOn.prefWidthProperty().bind(recentOrdersTable.widthProperty().multiply(0.12));
                if (colAmount != null) colAmount.prefWidthProperty().bind(recentOrdersTable.widthProperty().multiply(0.10));
                if (colStatus != null) colStatus.prefWidthProperty().bind(recentOrdersTable.widthProperty().multiply(0.08));
                if (colActions != null) colActions.prefWidthProperty().bind(recentOrdersTable.widthProperty().multiply(0.20));

                // Wrap long text in the Title column so large screens show full attributes but small screens wrap
                if (colArtTitle != null) {
                    colArtTitle.setCellFactory(tc -> new TableCell<>() {
                        private final Label lbl = new Label();
                        {
                            lbl.setWrapText(true);
                            lbl.maxWidthProperty().bind(colArtTitle.widthProperty().subtract(10));
                        }

                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setGraphic(null);
                            } else {
                                lbl.setText(item);
                                setGraphic(lbl);
                            }
                        }
                    });
                }
            }

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to configure table columns", ex);
        }

        // load orders for current artist (if set)
        try {
            refreshRecentOrders();
            updateDashboardCards();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to load recent orders during initialize", ex);
        }

        // Sidebar navigation is handled by its own controller (ArtistSidebarController).
    }

    @FXML
    private void openDashboard(MouseEvent event) {
        // mark selection
        try { setSidebarSelectionSafe(dashboardHBox); } catch (Exception ignored) {}
        // already on dashboard; no scene change required
        System.out.println("openDashboard: selected dashboard");
    }

    @FXML
    private void openOrders(MouseEvent event) {
        try {
            setSidebarSelectionSafe(ordersHBox);
            Stage stage = null;
            if (event != null && event.getSource() instanceof javafx.scene.Node) {
                stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            } else if (profileNameLabel != null && profileNameLabel.getScene() != null) {
                stage = (Stage) profileNameLabel.getScene().getWindow();
            }
            if (stage == null) return;
            java.net.URL res = getClass().getResource("/com/example/artflow/ArtistCompletedOrders.fxml");
            if (res == null) {
                System.err.println("ArtistCompletedOrders.fxml not found");
                return;
            }
            FXMLLoader loader = new FXMLLoader(res);
            Scene s = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            ArtistCompletedOrdersController ctrl = loader.getController();
            if (ctrl != null) ctrl.getClass(); // no-op but ensures controller initialized
            stage.setScene(s);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to open Completed Orders", ex);
        }
    }

    @FXML
    private void openProfile(MouseEvent event) {
        try { setSidebarSelectionSafe(profileHBox); } catch (Exception ignored) {}
        System.out.println("openProfile: profile clicked (no navigation implemented)");
    }

    public void setProfileName(String fullName) {
        if (profileNameLabel != null && fullName != null) {
            profileNameLabel.setText(fullName);
        }
        if (firstNameLabel != null && fullName != null) {
            String first = fullName.trim().split("\\s+")[0];
            firstNameLabel.setText(first);
        }

        // when profile name is set, reload orders for that artist explicitly
        refreshRecentOrders();
        updateDashboardCards();
    }

    public void setWelcomeFirstName(String firstName) {
        if (firstNameLabel != null && firstName != null) {
            firstNameLabel.setText(firstName);
        }
    }

    public void setCurrentUserType(String userType) {
        this.currentUserType = userType;
    }

    private void refreshRecentOrders() {
        try {
            String artistName = CurrentUser.getFullName();
            if ((artistName == null || artistName.trim().isEmpty()) && profileNameLabel != null) {
                artistName = profileNameLabel.getText();
            }
            if (artistName == null) artistName = "";

            List<OrderModel> all = DatabaseHelper.getInstance().listOrders();
            // filter orders by artist name (case-insensitive contains) and status
            List<OrderModel> filtered = new ArrayList<>();
            for (OrderModel o : all) {
                if (o == null) continue;
                String an = o.getArtistName();
                String status = o.getStatus() != null ? o.getStatus().toLowerCase() : "";
                if (an == null) an = "";
                
                // Only show pending orders in recent orders
                if ((an.equalsIgnoreCase(artistName) || an.toLowerCase().contains(artistName.toLowerCase())) 
                        && "pending".equals(status)) {
                    filtered.add(o);
                }
            }

            if (recentOrdersTable != null) {
                recentOrdersTable.getItems().clear();
                recentOrdersTable.getItems().addAll(filtered);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing recent orders", e);
        }
    }
    
    private void updateDashboardCards() {
        try {
            String artistName = CurrentUser.getFullName();
            if ((artistName == null || artistName.trim().isEmpty()) && profileNameLabel != null) {
                artistName = profileNameLabel.getText();
            }
            if (artistName == null) artistName = "";

            DatabaseHelper db = DatabaseHelper.getInstance();
            
            // Count total artworks for this artist
            List<ArtworkModel> allArtworks = db.listArtworks();
            int artworkCount = 0;
            for (ArtworkModel artwork : allArtworks) {
                if (artwork != null && artistName.equalsIgnoreCase(artwork.getArtistName())) {
                    artworkCount++;
                }
            }
            if (totalArtworksLabel != null) {
                totalArtworksLabel.setText(String.valueOf(artworkCount));
            }
            
            // Count total orders and calculate earnings for this artist
            List<OrderModel> allOrders = db.listOrders();
            int orderCount = 0;
            double totalEarnings = 0.0;
            
            for (OrderModel order : allOrders) {
                if (order != null && artistName.equalsIgnoreCase(order.getArtistName())) {
                    orderCount++;
                    // Only count completed orders for earnings
                    if ("completed".equalsIgnoreCase(order.getStatus())) {
                        totalEarnings += order.getAmount();
                    }
                }
            }
            
            if (totalOrdersLabel != null) {
                totalOrdersLabel.setText(String.valueOf(orderCount));
            }
            
            if (totalEarningsLabel != null) {
                // Format earnings to 2 decimal places
                totalEarningsLabel.setText(String.format("%.0f", totalEarnings));
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating dashboard cards", e);
        }
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

    // helper to safely set the sidebar selection (used by sidebar click handlers)
    private void setSidebarSelectionSafe(HBox box) {
        try {
            if (dashboardHBox != null) dashboardHBox.getStyleClass().remove("selected");
            if (artworksHBox != null) artworksHBox.getStyleClass().remove("selected");
            if (ordersHBox != null) ordersHBox.getStyleClass().remove("selected");
            if (earningsHBox != null) earningsHBox.getStyleClass().remove("selected");
            if (profileHBox != null) profileHBox.getStyleClass().remove("selected");
            if (logoutHBox != null) logoutHBox.getStyleClass().remove("selected");
            if (box != null && !box.getStyleClass().contains("selected")) box.getStyleClass().add("selected");
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
