package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.net.URL;

@SuppressWarnings("unused")
public class ArtistMyArtworkController {

    @FXML
    private TextField searchField;

    @FXML
    private HBox categoryHBox;

    @FXML
    private FlowPane artworksFlow;

    @FXML
    private Button addArtworkBtn;

    // ids added for responsive binding
    @FXML
    private javafx.scene.control.ScrollPane centerScroll;

    @FXML
    private VBox contentVBox;

    // store all art nodes so we can filter them without losing them
    private final List<Parent> allArtNodes = new ArrayList<>();
    private final List<ArtworkModel> artworkModels = new ArrayList<>();

    // keep the current computed card width so renderArtworkModel can size cards to fit exactly 4 columns
    private volatile double currentCardWidth = 260;
    private static final int TARGET_COLUMNS = 4;

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

    private static final Logger LOGGER = Logger.getLogger(ArtistMyArtworkController.class.getName());

    @FXML
    public void initialize() {

        if (categoryHBox != null && !categoryHBox.getChildren().isEmpty()) {
            categoryHBox.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
            // mark first chip selected by default (use findFirst to avoid indexed access warning)
            categoryHBox.getChildren().stream().findFirst().ifPresent(n -> n.getStyleClass().add("selected"));
        }

        if (addArtworkBtn != null) {
            addArtworkBtn.setOnAction(this::openAddArtwork);

            // responsive behavior: adjust button size based on the window width once the scene is available
            Platform.runLater(() -> {
                if (addArtworkBtn.getScene() != null) {
                    var window = addArtworkBtn.getScene().getWindow();
                    if (window instanceof Stage stage) {
                        stage.widthProperty().addListener((obs, oldV, newV) -> adjustAddButtonForWidth(newV.doubleValue()));
                        adjustAddButtonForWidth(stage.getWidth());
                    }
                }
            });
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        }

        // relayout grid when grid width changes so columns adjust responsively
        if (artworksFlow != null) {
            artworksFlow.widthProperty().addListener((obs, oldV, newV) -> applyFilters());
        }

        // Responsive: bind content width to scroll viewport, and set FlowPane wrap length
        Platform.runLater(() -> {
            try {
                if (centerScroll != null && contentVBox != null && artworksFlow != null) {
                    centerScroll.viewportBoundsProperty().addListener((obs, oldB, newB) -> recomputeCardLayout(newB.getWidth()));

                    // set initial values if viewport already known
                    double initialW = centerScroll.getViewportBounds().getWidth();
                    if (initialW > 0) {
                        recomputeCardLayout(initialW);
                    }
                }
            } catch (Exception ignored) {}
        });

        // load existing artworks from store and listen for new ones
        List<ArtworkModel> existing = ArtworkStore.getInstance().getAll();
        if (existing != null && !existing.isEmpty()) {
            artworkModels.addAll(existing);
            applyFilters();
        }
        ArtworkStore.getInstance().addListener(this::addArtwork);
    }

    // Recompute layout aiming for TARGET_COLUMNS per row; updates currentCardWidth and FlowPane wrapLength
    private void recomputeCardLayout(double viewportW) {
        if (viewportW <= 0 || contentVBox == null || artworksFlow == null) return;

        double leftPad = contentVBox.getPadding() != null ? contentVBox.getPadding().getLeft() : 20;
        double rightPad = contentVBox.getPadding() != null ? contentVBox.getPadding().getRight() : 20;
        double available = Math.max(50, viewportW - leftPad - rightPad);
        double hgap = artworksFlow.getHgap();

        // Compute card width so exactly TARGET_COLUMNS fit
        int columns = TARGET_COLUMNS;
        double cardWidth = Math.floor((available - (columns - 1) * hgap) / (double) columns);
        if (cardWidth < 180) {
            // fallback: reduce columns if card would be too small
            columns = Math.max(1, (int) Math.floor((available + hgap) / (180 + hgap)));
            cardWidth = Math.floor((available - (columns - 1) * hgap) / (double) columns);
        }

        currentCardWidth = Math.max(140, cardWidth);

        final double wrapLengthUnclamped = columns * (currentCardWidth + hgap) - hgap;
        final double wrapLength = Math.min(wrapLengthUnclamped, available);

        final double vw = viewportW;
        Platform.runLater(() -> {
            contentVBox.setPrefWidth(vw);
            artworksFlow.setPrefWrapLength(wrapLength);
            // trigger re-render to apply new widths
            applyFilters();
        });
    }

    private void adjustAddButtonForWidth(double width) {
        // Use a fraction of the width but clamp between reasonable sizes
        double pref = Math.max(100, Math.min(180, width * 0.18));
        if (addArtworkBtn != null) {
            addArtworkBtn.setPrefWidth(pref);
            if (width < 420) {
                if (!addArtworkBtn.getStyleClass().contains("small-add-btn")) addArtworkBtn.getStyleClass().add("small-add-btn");
            } else {
                addArtworkBtn.getStyleClass().remove("small-add-btn");
            }
        }
    }

    @FXML
    private void handleSearchAction(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleCategoryClick(MouseEvent event) {
        Object src = event.getSource();
        if (!(src instanceof Label clicked)) return;

        categoryHBox.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
        clicked.getStyleClass().add("selected");
        System.out.println("Selected category: " + clicked.getText());
        applyFilters();
    }

    private void applyFilters() {
        final String search = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";
        final String selectedCategory;
        {
            String sc = null;
            if (categoryHBox != null) {
                for (javafx.scene.Node n : categoryHBox.getChildren()) {
                    if (n.getStyleClass().contains("selected") && n instanceof Label) {
                        sc = ((Label) n).getText();
                        break;
                    }
                }
            }
            selectedCategory = sc;
        }

        if (artworksFlow == null) {
            System.out.println("applyFilters: artworksFlow is null — nothing to update");
            return;
        }

        final String selectedCategoryFinal = selectedCategory; // ensure effectively final for lambda
        Platform.runLater(() -> {
             artworksFlow.getChildren().clear();
             java.util.Set<String> seen = new java.util.HashSet<>();
             for (ArtworkModel m : artworkModels) {
                // dedupe by title|category|price|image
                String key = (m.getTitle() == null ? "" : m.getTitle().trim()) + "|" + (m.getCategory() == null ? "" : m.getCategory().trim()) + "|" + (m.getPrice() == null ? "" : m.getPrice().trim()) + "|" + (m.getImagePath() == null ? "" : m.getImagePath().trim());
                if (seen.contains(key)) continue;
                seen.add(key);

                // Category filter: if a specific category chip is selected (not All), require artwork category to match
                boolean matchesCategory = true;
                if (selectedCategoryFinal != null && !"All".equalsIgnoreCase(selectedCategoryFinal)) {
                    matchesCategory = m.getCategory() != null && selectedCategoryFinal.equalsIgnoreCase(m.getCategory());
                }

                // Search filter: if search text provided, match against title OR category (case-insensitive)
                boolean matchesSearch = true;
                if (!search.isEmpty()) {
                     String title = m.getTitle() == null ? "" : m.getTitle().toLowerCase(Locale.ROOT);
                     String category = m.getCategory() == null ? "" : m.getCategory().toLowerCase(Locale.ROOT);
                     matchesSearch = title.contains(search) || category.contains(search);
                 }

                if (matchesCategory && matchesSearch) {
                    Parent card = renderArtworkModel(m);
                    if (card != null) artworksFlow.getChildren().add(card);
                }
            }
        });
    }

    private Parent renderArtworkModel(ArtworkModel m) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/artflow/ArtistArtcard.fxml"));
            Parent card = loader.load();
            javafx.scene.Node imageView = card.lookup("#cardImageView");
            if (imageView instanceof ImageView && m.getImagePath() != null) {
                try {
                    Image img = new Image(m.getImagePath(), currentCardWidth * 0.8, 180, true, true);
                    ((ImageView) imageView).setImage(img);
                    // also set fit width on the image view to scale correctly
                    ((ImageView) imageView).setFitWidth(currentCardWidth * 0.8);
                } catch (Exception ignored) {}
            }
            javafx.scene.Node titleLbl = card.lookup("#cardTitleLabel");
            if (titleLbl instanceof Label) ((Label) titleLbl).setText(m.getTitle());
            javafx.scene.Node catLbl = card.lookup("#cardCategoryLabel");
            if (catLbl instanceof Label) ((Label) catLbl).setText(m.getCategory());
            javafx.scene.Node priceLbl = card.lookup("#cardPriceLabel");
            if (priceLbl instanceof Label) ((Label) priceLbl).setText("$" + m.getPrice());

            card.setUserData(m.getCategory());

            // ensure the card prefers the computed width so FlowPane places exactly TARGET_COLUMNS per row
            try {
                card.prefWidth(currentCardWidth);
                card.setStyle("-fx-pref-width: " + currentCardWidth + "px;");
            } catch (Exception ignore) {}

            // wire delete button
            javafx.scene.Node deleteBtn = card.lookup("#deleteButton");
            if (deleteBtn != null) deleteBtn.setOnMouseClicked(ev -> {
                if (artworksFlow != null) artworksFlow.getChildren().remove(card);
                artworkModels.remove(m);
                ArtworkStore.getInstance().removeById(m.getId());
            });

            // wire edit button
            javafx.scene.Node editBtn = card.lookup("#editButton");
            if (editBtn != null) editBtn.setOnMouseClicked(ev -> {
                try {
                    java.net.URL fxmlUrl = getClass().getResource("/com/example/artflow/ArtistAddArtwork.fxml");
                    FXMLLoader loader1 = new FXMLLoader(fxmlUrl);
                    Parent root = loader1.load();
                    AddArtworkController ctrl = loader1.getController();
                    // populate dialog for edit
                    ctrl.loadForEdit(m, updated -> {
                        // update our in-memory list and refresh view
                        for (int i = 0; i < artworkModels.size(); i++) {
                            if (artworkModels.get(i).getId().equals(updated.getId())) {
                                artworkModels.set(i, updated);
                                break;
                            }
                        }
                        applyFilters();
                    });

                    Stage stage = new Stage();
                    Stage owner = null;
                    if (addArtworkBtn != null && addArtworkBtn.getScene() != null) {
                        var w = addArtworkBtn.getScene().getWindow();
                        if (w instanceof Stage st) owner = st;
                    }
                    if (owner != null) {
                        stage.initOwner(owner);
                        stage.initModality(Modality.WINDOW_MODAL);
                    } else {
                        stage.initModality(Modality.APPLICATION_MODAL);
                    }
                    stage.setTitle("Edit Artwork");
                    stage.setScene(new Scene(root));
                    stage.setResizable(false);
                    stage.showAndWait();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error opening edit dialog", ex);
                }
            });

            return card;
        } catch (IOException e) {
            System.err.println("Error rendering artwork model: " + e.getMessage());
            return null;
        }
    }

    // FlowPane wraps automatically; we can optionally adjust node widths on resize if needed
    @FXML
    public void openAddArtwork(ActionEvent event) {
        // debug: immediately reflect on the button if available
        if (addArtworkBtn != null) {
            addArtworkBtn.setText("Opening...");
            addArtworkBtn.setDisable(true);
        }

        System.out.println("openAddArtwork: handler called");

        try {
            java.net.URL fxmlUrl = getClass().getResource("/com/example/artflow/ArtistAddArtwork.fxml");
            System.out.println("Attempting to load FXML from: " + fxmlUrl);
            if (fxmlUrl == null) {
                Alert nf = new Alert(Alert.AlertType.ERROR, "ArtistAddArtwork.fxml resource not found on classpath.", ButtonType.OK);
                nf.setHeaderText("Resource missing");
                nf.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            AddArtworkController controller = loader.getController();
            controller.setParentController(this);
            // provide the parent's FlowPane reference to the Add dialog controller so it can ensure the parent flow is available
            FlowPane flowRef = artworksFlow;
            if (flowRef == null && addArtworkBtn != null && addArtworkBtn.getScene() != null) {
                javafx.scene.Node found = addArtworkBtn.getScene().lookup("#artworksFlow");
                if (found instanceof FlowPane) flowRef = (FlowPane) found;
            }
            if (flowRef != null) controller.setParentArtworksFlow(flowRef);

            Stage stage = new Stage();

            // set owner when possible so the dialog is modal over the current window
            Stage owner = null;
            if (event != null && event.getSource() instanceof javafx.scene.Node) {
                owner = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            } else if (addArtworkBtn != null && addArtworkBtn.getScene() != null) {
                var w = addArtworkBtn.getScene().getWindow();
                if (w instanceof Stage st) owner = st;
            }
            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            } else {
                stage.initModality(Modality.APPLICATION_MODAL);
            }

            stage.setTitle("Add Artwork");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            System.out.println("FXML loaded, showing stage");
            stage.showAndWait();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open Add Artwork", e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Alert err = new Alert(Alert.AlertType.ERROR, sw.toString(), ButtonType.OK);
            err.setHeaderText("Failed to open Add Artwork");
            err.getDialogPane().setPrefWidth(600);
            err.showAndWait();
        } finally {
             if (addArtworkBtn != null) {
                 addArtworkBtn.setText("+ Add Artwork");
                 addArtworkBtn.setDisable(false);
             }
         }
     }


     @SuppressWarnings("unused")
     public void addArtworkNode(Parent artCardNode) {
        System.out.println("addArtworkNode called — node=" + artCardNode + ", thread=" + Thread.currentThread().getName());
        if (artCardNode == null) return;
        // Ensure UI changes happen on JavaFX thread
        Platform.runLater(() -> {
            allArtNodes.add(artCardNode);
             // in case artworksFlow was not yet initialized, try to locate it from the scene
             if (artworksFlow == null) {
                 if (searchField != null && searchField.getScene() != null) {
                     javafx.scene.Node found = searchField.getScene().lookup("#artworksFlow");
                     if (found instanceof FlowPane) {
                         artworksFlow = (FlowPane) found;
                         System.out.println("addArtworkNode: located artworksFlow via scene lookup");
                     }
                 }
             }

            // ensure 'All' category is selected so the newly published artwork becomes visible
            if (categoryHBox != null) {
                for (javafx.scene.Node n : categoryHBox.getChildren()) {
                    if (n instanceof Label lbl) {
                        if ("All".equalsIgnoreCase(lbl.getText())) {
                            categoryHBox.getChildren().forEach(ch -> ch.getStyleClass().remove("selected"));
                            if (!lbl.getStyleClass().contains("selected")) lbl.getStyleClass().add("selected");
                            break;
                        }
                    }
                }
            }

            System.out.println("addArtworkNode: allArtNodes size after add = " + allArtNodes.size());

            applyFilters();
        });
    }

    public void addArtwork(ArtworkModel model) {
        if (model == null) return;
        // Ensure UI updates happen on JavaFX thread and artworksFlow is available
        Platform.runLater(() -> {
            System.out.println("addArtwork called: title='" + model.getTitle() + "', category='" + model.getCategory() + "', image='" + model.getImagePath() + "'");
            artworkModels.add(model);
            System.out.println("artworkModels size after add: " + artworkModels.size());
            // always rebuild the flowpane from models to avoid duplicates
            applyFilters();
         });
     }

    // Navigation helpers: mark which sidebar item is selected (adds 'selected' style class)
    private void setSidebarSelected(HBox item) {
        try {
            if (dashboardHBox != null) dashboardHBox.getStyleClass().remove("selected");
            if (artworksHBox != null) artworksHBox.getStyleClass().remove("selected");
            if (ordersHBox != null) ordersHBox.getStyleClass().remove("selected");
            if (earningsHBox != null) earningsHBox.getStyleClass().remove("selected");
            if (profileHBox != null) profileHBox.getStyleClass().remove("selected");
            if (logoutHBox != null) logoutHBox.getStyleClass().remove("selected");

            if (item != null) {
                if (!item.getStyleClass().contains("selected")) item.getStyleClass().add("selected");
            }
        } catch (Exception ignored) {}
    }

    @FXML
    private void openDashboard(MouseEvent event) {
        setSidebarSelected(dashboardHBox);
        // load ArtistDashboard.fxml into current stage and tell it to highlight dashboard
        try {
            Stage stage = null;
            if (event != null && event.getSource() instanceof javafx.scene.Node) {
                stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            } else if (addArtworkBtn != null && addArtworkBtn.getScene() != null) {
                stage = (Stage) addArtworkBtn.getScene().getWindow();
            }
            if (stage == null) return;
            URL res = getClass().getResource("/com/example/artflow/ArtistDashboard.fxml");
            if (res == null) return;
            FXMLLoader loader = new FXMLLoader(res);
            Scene s = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            // request the dashboard's controller to select the dashboard menu
            ArtistDashboardController ctrl = loader.getController();
            if (ctrl != null) ctrl.selectMenu("dashboard");
            stage.setScene(s);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Navigation error in openDashboard", ex);
        }
    }

    @FXML
    private void openMyArtworks(MouseEvent event) {
        // This method exists so the artworks HBox in the MyArtworks FXML can call it
        // We simply mark the artworks sidebar item as selected (no reload required)
        setSidebarSelected(artworksHBox);
        // Optionally, refresh the view if desired
        applyFilters();
    }

    @FXML
    private void openOrders(MouseEvent event) {
        setSidebarSelected(ordersHBox);
        // stub: implement navigation to orders page if exists
        System.out.println("Open Orders (not implemented).");
    }

    @FXML
    private void openEarnings(MouseEvent event) {
        setSidebarSelected(earningsHBox);
        // stub: implement navigation to earnings page if exists
        System.out.println("Open Earnings (not implemented).");
    }

    @FXML
    private void openProfile(MouseEvent event) {
        setSidebarSelected(profileHBox);
        // stub: implement profile navigation if exists
        System.out.println("Open Profile (not implemented).");
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        // delegate to ArtistDashboardController's logout flow if possible; otherwise load Select or login
        System.out.println("Logout clicked from MyArtworks");
        // try to find the Dashboard controller on scene and call its handleLogout
        try {
            if (event != null && event.getSource() instanceof javafx.scene.Node node) {
                var window = node.getScene() != null ? node.getScene().getWindow() : null;
                if (window instanceof Stage stage) {
                    // load the artist login screen on logout
                    URL res = getClass().getResource("/com/example/artflow/ArtistLogin.fxml");
                    if (res != null) {
                        FXMLLoader loader = new FXMLLoader(res);
                        Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
                        stage.setScene(scene);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Logout navigation failed", ex);
        }
    }

    // Allow external controllers (e.g. ArtistDashboardController) to request which sidebar
    // item should be visually selected when this view is shown.
    public void selectMenu(String menuKey) {
        if (menuKey == null) return;
        switch (menuKey.toLowerCase(Locale.ROOT)) {
            case "dashboard": setSidebarSelected(dashboardHBox); break;
            case "artworks": setSidebarSelected(artworksHBox); break;
            case "orders": setSidebarSelected(ordersHBox); break;
            case "earnings": setSidebarSelected(earningsHBox); break;
            case "profile": setSidebarSelected(profileHBox); break;
            default: setSidebarSelected(null); break;
        }
    }
 }
