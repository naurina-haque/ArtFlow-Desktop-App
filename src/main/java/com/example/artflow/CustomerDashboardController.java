package com.example.artflow;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class CustomerDashboardController {

    @FXML private HBox dashboardHBox;
    @FXML private HBox artworksHBox;
    @FXML private HBox ordersHBox;
    @FXML private HBox earningsHBox;
    @FXML private HBox profileHBox;
    @FXML private HBox logoutHBox;
    @FXML private FlowPane customerFlow;
    @FXML private javafx.scene.control.Label profileNameLabel;
    @FXML private javafx.scene.control.Label firstNameLabel;
    @FXML private javafx.scene.control.Label welcomeLabel;

    // Search & category filtering UI
    @FXML private TextField searchField;
    @FXML private HBox categoryHBox;

    // Master list of artworks (keeps state and supports filtering)
    private final List<ArtworkModel> allArtworks = new ArrayList<>();
    private String activeCategory = "All";

    private String currentUserType;

    @FXML
    private void openDashboard(MouseEvent event) {
        setSidebarSelected(dashboardHBox);
        // Already on dashboard
    }

    @FXML
    private void openMyArtworks(MouseEvent event) {
        setSidebarSelected(artworksHBox);
        // TODO: navigate to MyArtworks view if created
    }

    @FXML
    private void openOrders(MouseEvent event) {
        setSidebarSelected(ordersHBox);
        navigateTo("/com/example/artflow/CustomerTrackOrder.fxml");
    }

    @FXML
    private void openEarnings(MouseEvent event) {
        setSidebarSelected(earningsHBox);
    }

    @FXML
    private void openProfile(MouseEvent event) {
        setSidebarSelected(profileHBox);
        navigateTo("/com/example/artflow/CustomerProfile.fxml");
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        setSidebarSelected(logoutHBox);
        // Clear current user and return to CustomerLogin scene
        try {
            // clear global user
            CurrentUser.setFullName(null);
            CurrentUser.setUserType(null);

            navigateTo("/com/example/artflow/CustomerLogin.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            Stage stage = (Stage) (dashboardHBox == null ? null : dashboardHBox.getScene().getWindow());
            if (stage == null) {
                System.err.println("CustomerDashboardController: unable to get stage for navigation");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
        } catch (Exception ex) {
            System.err.println("Navigation failed to " + fxmlPath);
            ex.printStackTrace();
        }
    }

    private void setSidebarSelected(HBox item) {
        try {
            if (dashboardHBox != null) dashboardHBox.getStyleClass().remove("selected");
            if (artworksHBox != null) artworksHBox.getStyleClass().remove("selected");
            if (ordersHBox != null) ordersHBox.getStyleClass().remove("selected");
            if (earningsHBox != null) earningsHBox.getStyleClass().remove("selected");
            if (profileHBox != null) profileHBox.getStyleClass().remove("selected");
            if (logoutHBox != null) logoutHBox.getStyleClass().remove("selected");
            if (item != null && !item.getStyleClass().contains("selected")) item.getStyleClass().add("selected");
        } catch (Exception ignored) {}
    }

    public void selectMenu(String key) {
        if (key == null) return;
        switch (key.toLowerCase(Locale.ROOT)) {
            case "dashboard": setSidebarSelected(dashboardHBox); break;
            case "artworks": setSidebarSelected(artworksHBox); break;
            case "orders": setSidebarSelected(ordersHBox); break;
            case "earnings": setSidebarSelected(earningsHBox); break;
            case "profile": setSidebarSelected(profileHBox); break;
            default: setSidebarSelected(null); break;
        }
    }

    @FXML
    public void initialize() {
        // initialize search & chips before loading cards
        try {
            if (searchField != null) {
                searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());
            }
            if (categoryHBox != null) {
                for (Node n : categoryHBox.getChildren()) {
                    if (n instanceof Label) {
                        Label chip = (Label) n;
                        chip.setOnMouseClicked(e -> {
                            setActiveCategory(chip.getText());
                        });
                    }
                }
            }
        } catch (Exception ignored) {}

        // Load existing artworks into the master list
        allArtworks.clear();
        List<ArtworkModel> fromDb = ArtworkStore.getInstance().getAll();
        if (fromDb != null) allArtworks.addAll(fromDb);

        // set default active chip (All) if present
        if (categoryHBox != null) {
            for (Node n : categoryHBox.getChildren()) {
                if (n instanceof Label) {
                    Label chip = (Label) n;
                    if ("All".equalsIgnoreCase(chip.getText())) {
                        chip.getStyleClass().add("selected");
                        activeCategory = "All";
                        break;
                    }
                }
            }
        }

        // Render initial filtered set
        applyFilters();

        // Listen for new artworks and update master list then re-apply filters
        ArtworkStore.getInstance().addListener(model -> {
            Platform.runLater(() -> {
                allArtworks.add(model);
                applyFilters();
            });
        });
    }

    private Parent createCardFor(ArtworkModel m) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/artflow/CustomerArtcard.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getResource("/com/example/artflow/CustomerArtCard.fxml");
            if (fxmlUrl == null) {
                System.err.println("Customer artcard FXML not found (tried CustomerArtcard.fxml and CustomerArtCard.fxml)");
                return null;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent card = loader.load();
            // set title/category/price/image
            javafx.scene.Node iv = card.lookup("#cardImageView");
            if (iv instanceof ImageView) {
                javafx.scene.image.Image img = tryLoadImage(m.getImagePath(), 220, 160);
                if (img != null) ((ImageView) iv).setImage(img);
            }
            javafx.scene.Node title = card.lookup("#cardTitleLabel"); if (title instanceof Label) ((Label) title).setText(m.getTitle());
            javafx.scene.Node artistLbl = card.lookup("#cardArtistLabel"); if (artistLbl instanceof Label) ((Label) artistLbl).setText(m.getArtistName() == null ? "" : "by " + m.getArtistName());
            javafx.scene.Node cat = card.lookup("#cardCategoryLabel"); if (cat instanceof Label) ((Label) cat).setText(m.getCategory());
            javafx.scene.Node price = card.lookup("#cardPriceLabel"); if (price instanceof Label) ((Label) price).setText("$" + m.getPrice());
            // wire buy/edit button to open details dialog
            javafx.scene.Node buyBtn = card.lookup("#buyButton");
            if (buyBtn != null) {
                buyBtn.setOnMouseClicked(ev -> {
                    try {
                        var fxml = getClass().getResource("/com/example/artflow/CustomerArtDetails.fxml");
                        if (fxml == null) {
                            System.err.println("CustomerArtDetails.fxml not found");
                            return;
                        }
                        javafx.fxml.FXMLLoader l = new javafx.fxml.FXMLLoader(fxml);
                        javafx.scene.Parent root = l.load();
                        CustomerArtDetailsController detailsCtrl = l.getController();
                        detailsCtrl.setModel(m);
                        Stage stage = new Stage();
                        stage.initOwner(card.getScene() == null ? null : card.getScene().getWindow());
                        stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
                        stage.setTitle(m.getTitle());
                        stage.setScene(new javafx.scene.Scene(root));
                        stage.setResizable(false);
                        stage.showAndWait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
            return card;
        } catch (IOException e) {
            System.err.println("Failed to create customer art card: " + e.getMessage());
            return null;
        }
    }

    // Apply current search text and active category to the master artwork list, and render matching cards
    private void applyFilters() {
        if (customerFlow == null) return;
        customerFlow.getChildren().clear();
        String q = (searchField == null || searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();
        final String query = q;
        for (ArtworkModel m : allArtworks) {
            boolean matchesQuery = query.isEmpty() || (
                    (m.getTitle() != null && m.getTitle().toLowerCase().contains(query)) ||
                    (m.getCategory() != null && m.getCategory().toLowerCase().contains(query)) ||
                    (m.getArtistName() != null && m.getArtistName().toLowerCase().contains(query))
            );
            boolean matchesCategory = (activeCategory == null || activeCategory.isEmpty() || "All".equalsIgnoreCase(activeCategory)) ||
                    (m.getCategory() != null && m.getCategory().equalsIgnoreCase(activeCategory));
            if (matchesQuery && matchesCategory) {
                Parent card = createCardFor(m);
                if (card != null) customerFlow.getChildren().add(card);
            }
        }
    }

    private void setActiveCategory(String category) {
        activeCategory = category == null ? "All" : category;
        // update visual selected state
        if (categoryHBox != null) {
            for (Node n : categoryHBox.getChildren()) {
                if (n instanceof Label) {
                    Label chip = (Label) n;
                    if (chip.getText().equalsIgnoreCase(activeCategory)) {
                        if (!chip.getStyleClass().contains("selected")) chip.getStyleClass().add("selected");
                    } else {
                        chip.getStyleClass().removeIf(s -> "selected".equals(s));
                    }
                }
            }
        }
        applyFilters();
    }

    // Try loading an image from multiple possible path formats:
    // - null/empty: return null
    // - resource path (exists on classpath): use getResource
    // - absolute file path or windows path: create File and toURI
    // - already a URL (starts with file:, http:, etc.)
    private javafx.scene.image.Image tryLoadImage(String path, double reqWidth, double reqHeight) {
        if (path == null || path.isBlank()) return null;

        // 1) If it's already a URL (file:, http:), try directly
        try {
            String trimmed = path.trim();
            if (trimmed.startsWith("file:") || trimmed.startsWith("http:") || trimmed.startsWith("https:")) {
                return new javafx.scene.image.Image(trimmed, reqWidth, reqHeight, true, true);
            }
        } catch (Exception ignored) {}

        // 2) Try resource lookup on classpath
        try {
            URL res = getClass().getResource(path.startsWith("/") ? path : "/" + path);
            if (res == null) {
                // sometimes stored as relative resource under /com/example/artflow/img/...
                res = getClass().getResource("/com/example/artflow/" + path);
            }
            if (res != null) {
                return new javafx.scene.image.Image(res.toExternalForm(), reqWidth, reqHeight, true, true);
            }
        } catch (Exception ignored) {}

        // 3) Try treating path as filesystem path
        try {
            File f = new File(path);
            if (!f.exists()) {
                // if Windows absolute with spaces encoded (%20), try decode
                String decoded = path.replace("%20", " ");
                f = new File(decoded);
            }
            if (f.exists()) {
                return new javafx.scene.image.Image(f.toURI().toString(), reqWidth, reqHeight, true, true);
            }
        } catch (Exception ignored) {}

        // 4) last resort: try loading as-is (may throw)
        try {
            return new javafx.scene.image.Image(path, reqWidth, reqHeight, true, true);
        } catch (Exception ignored) {}

        return null;
    }

    // Allow external callers (e.g. login) to set display name and user type
    public void setProfileName(String fullName) {
        if (profileNameLabel != null && fullName != null) profileNameLabel.setText(fullName);
    }

    public void setWelcomeFirstName(String first) {
        if (firstNameLabel != null && first != null) firstNameLabel.setText(first);
    }

    public void setCurrentUserType(String type) {
        this.currentUserType = type;
    }
}
