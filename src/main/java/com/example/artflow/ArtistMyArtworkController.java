package com.example.artflow;

import com.example.artflow.ArtworkStore;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.application.Platform;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArtistMyArtworkController {

    @FXML
    private TextField searchField;

    @FXML
    private HBox categoryHBox;

    @FXML
    private FlowPane artworksFlow;

    @FXML
    private Button addArtworkBtn;

    // store all art nodes so we can filter them without losing them
    private final List<Parent> allArtNodes = new ArrayList<>();
    private final List<ArtworkModel> artworkModels = new ArrayList<>();

    @FXML
    public void initialize() {

        if (categoryHBox != null && !categoryHBox.getChildren().isEmpty()) {
            categoryHBox.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
            categoryHBox.getChildren().get(0).getStyleClass().add("selected");
        }

        if (addArtworkBtn != null) {
            addArtworkBtn.setOnAction(this::openAddArtwork);

            // responsive behavior: adjust button size based on the window width once the scene is available
            Platform.runLater(() -> {
                if (addArtworkBtn.getScene() != null && addArtworkBtn.getScene().getWindow() instanceof Stage) {
                    Stage stage = (Stage) addArtworkBtn.getScene().getWindow();
                    stage.widthProperty().addListener((obs, oldV, newV) -> adjustAddButtonForWidth(newV.doubleValue()));
                    adjustAddButtonForWidth(stage.getWidth());
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

        // DEBUG: if no artworks exist, add a sample to verify rendering
        Platform.runLater(() -> {
            try {
                if (artworkModels.isEmpty()) {
                    java.net.URL imgUrl = getClass().getResource("/com/example/artflow/img/background.jpg");
                    String imgPath = imgUrl != null ? imgUrl.toString() : null;
                    ArtworkModel sample = new ArtworkModel("Sample Artwork", "25", "Digital Art", imgPath);
                    artworkModels.add(sample);
                    applyFilters();
                    System.out.println("DEBUG: added sample artwork for visual test");
                }
            } catch (Exception ignored) {}
        });

        // load existing artworks from store and listen for new ones
        List<ArtworkModel> existing = ArtworkStore.getInstance().getAll();
        if (existing != null && !existing.isEmpty()) {
            artworkModels.addAll(existing);
            applyFilters();
        }
        ArtworkStore.getInstance().addListener(m -> {
            // listener runs on arbitrary thread - ensure UI thread
            addArtwork(m);
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
        if (src instanceof Label) {
            Label clicked = (Label) src;

            categoryHBox.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
            if (!clicked.getStyleClass().contains("selected")) {
                clicked.getStyleClass().add("selected");
            }
            System.out.println("Selected category: " + clicked.getText());
            applyFilters();
        }
    }

    private void applyFilters() {
        String search = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";
        String selectedCategory = null;
        if (categoryHBox != null) {
            for (javafx.scene.Node n : categoryHBox.getChildren()) {
                if (n.getStyleClass().contains("selected") && n instanceof Label) {
                    selectedCategory = ((Label) n).getText();
                    break;
                }
            }
        }

        if (artworksFlow == null) {
            System.out.println("applyFilters: artworksFlow is null — nothing to update");
            return;
        }

        // place matching cards into flow pane (it will wrap automatically)
        final String selectedCategoryFinal = selectedCategory;
        final String searchFinal = search;
        Platform.runLater(() -> {
            artworksFlow.getChildren().clear();
            for (ArtworkModel m : artworkModels) {
                boolean matchesCategory = true;
                if (selectedCategoryFinal != null && !"All".equalsIgnoreCase(selectedCategoryFinal) && m.getCategory() != null) {
                    matchesCategory = selectedCategoryFinal.equalsIgnoreCase(m.getCategory());
                }

                boolean matchesSearch = true;
                if (!searchFinal.isEmpty()) {
                    String combined = ((m.getTitle() == null ? "" : m.getTitle()) + " " + (m.getCategory() == null ? "" : m.getCategory())).toLowerCase(Locale.ROOT);
                    matchesSearch = combined.contains(searchFinal);
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
                    Image img = new Image(m.getImagePath(), 242, 180, true, true);
                    ((ImageView) imageView).setImage(img);
                } catch (Exception ignored) {}
            }
            javafx.scene.Node titleLbl = card.lookup("#cardTitleLabel");
            if (titleLbl instanceof Label) ((Label) titleLbl).setText(m.getTitle());
            javafx.scene.Node catLbl = card.lookup("#cardCategoryLabel");
            if (catLbl instanceof Label) ((Label) catLbl).setText(m.getCategory());
            javafx.scene.Node priceLbl = card.lookup("#cardPriceLabel");
            if (priceLbl instanceof Label) ((Label) priceLbl).setText("$" + m.getPrice());

            card.setUserData(m.getCategory());

            // wire delete button
            javafx.scene.Node deleteBtn = card.lookup("#deleteButton");
            if (deleteBtn != null) deleteBtn.setOnMouseClicked(ev -> {
                if (artworksFlow != null) artworksFlow.getChildren().remove(card);
                artworkModels.remove(m);
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
                owner = (Stage) addArtworkBtn.getScene().getWindow();
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
            e.printStackTrace();
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
                    if (n instanceof Label) {
                        Label lbl = (Label) n;
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
            // try to locate artworksFlow if not injected
            if (artworksFlow == null) {
                if (searchField != null && searchField.getScene() != null) {
                    javafx.scene.Node found = searchField.getScene().lookup("#artworksFlow");
                    if (found instanceof FlowPane) {
                        artworksFlow = (FlowPane) found;
                        System.out.println("addArtwork: located artworksFlow via scene lookup");
                    }
                }
            }
            // Immediately render and add the card to the flowpane for instant feedback
            if (artworksFlow != null) {
                Parent card = renderArtworkModel(model);
                if (card != null) artworksFlow.getChildren().add(card);
            } else {
                // fallback to full render pass
                applyFilters();
            }
        });
    }
}
