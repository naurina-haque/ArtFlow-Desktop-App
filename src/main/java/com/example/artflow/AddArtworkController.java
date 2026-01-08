package com.example.artflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.FlowPane;

import java.io.File;
import java.io.IOException;

public class AddArtworkController {

    @FXML
    public VBox previewHolder;

    @FXML
    public TextField titleField;

    @FXML
    public TextField priceField;

    @FXML
    public ComboBox<String> categoryCombo;

    @FXML
    public Button chooseImageBtn;

    @FXML
    public Button saveBtn;

    @FXML
    public Button cancelBtn;

    @FXML
    public Button removeImageBtn;

    @FXML
    public VBox dropArea;

    @FXML
    public javafx.scene.control.Label uploadStatusLabel;

    private File chosenImageFile;
    private ArtistMyArtworkController parentController;
    private FlowPane parentArtworksFlow;

    public void setParentController(ArtistMyArtworkController parent) {
        this.parentController = parent;
    }

    public void setParentArtworksFlow(FlowPane flow) {
        this.parentArtworksFlow = flow;
        // also inform parent controller if present
        if (this.parentController != null && flow != null) {
            try {
                // attempt to set parent's artworksFlow via reflection-like access if needed
                // but simpler: ensure parent's field is non-null by calling a small helper if available
                // we'll rely on parentController.addArtworkNode which checks artworksFlow.
            } catch (Exception ignored) {
            }
        }
    }

    @FXML
    private void initialize() {
        // guard: avoid running design-time/runtime-only code when opened in SceneBuilder
        if (categoryCombo == null || titleField == null || priceField == null) {
            return;
        }

        // populate categories
        categoryCombo.getItems().addAll(
                "All",
                "Coloured Portrait Art",
                "B&W Portrait Art",
                "Digital Art",
                "Landscape Art",
                "Watercolor Art",
                "Acrylic Art",
                "Line Art",
                "Pencil Sketch Art"
        );
        categoryCombo.getSelectionModel().selectFirst();

        // wire up listeners to update preview
        titleField.textProperty().addListener((obs, oldV, newV) -> updatePreviewCardFromFields());
        priceField.textProperty().addListener((obs, oldV, newV) -> updatePreviewCardFromFields());
        categoryCombo.valueProperty().addListener((obs, oldV, newV) -> updatePreviewCardFromFields());

        // drag & drop handling on dropArea
        if (dropArea != null) {
            dropArea.setOnDragOver(this::onDragOver);
            dropArea.setOnDragDropped(this::onDragDropped);
            // ensure click handler is bound at runtime in case FXML binding fails
            dropArea.setOnMouseClicked(this::onDropAreaClicked);
        }

        // ensure upload button action is bound at runtime in case FXML binding fails
        if (chooseImageBtn != null) {
            chooseImageBtn.setOnAction(this::onChooseImage);
        }

        if (removeImageBtn != null) {
            removeImageBtn.setOnAction(e -> {
                chosenImageFile = null;
                // If there is a preview card, clear only the image inside it (keep title, category, price)
                if (previewHolder != null && !previewHolder.getChildren().isEmpty()) {
                    javafx.scene.Node node = previewHolder.getChildren().get(0);
                    if (node != null) {
                        javafx.scene.Node imgNode = node.lookup("#cardImageView");
                        if (imgNode instanceof ImageView) {
                            ((ImageView) imgNode).setImage(null);
                        }
                    }
                } else {
                    // fallback: clear previewHolder if nothing specific to update
                    if (previewHolder != null) previewHolder.getChildren().clear();
                }
            });
        }
    }

    @FXML
    private void onChooseImage(ActionEvent event) {
        System.out.println("onChooseImage() called");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose artwork image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Window owner = null;
        if (previewHolder != null && previewHolder.getScene() != null) {
            owner = previewHolder.getScene().getWindow();
        }
        if (owner == null) {
            // fallback: pick any showing window
            for (Window w : Window.getWindows()) {
                if (w.isShowing()) {
                    owner = w;
                    break;
                }
            }
        }
        try {
            File f = null;
            if (owner != null) {
                if (uploadStatusLabel != null) uploadStatusLabel.setText("Opening file chooser...");
                f = chooser.showOpenDialog(owner);
            }
            if (f == null) {
                // fallback to open without owner
                System.out.println("showOpenDialog with owner returned null or owner not found, trying without owner");
                if (uploadStatusLabel != null) uploadStatusLabel.setText("Opening file chooser (no owner)...");
                f = chooser.showOpenDialog(null);
            }

            if (f != null) {
                chosenImageFile = f;
                System.out.println("Selected image: " + f.getAbsolutePath());
                if (uploadStatusLabel != null) uploadStatusLabel.setText("Selected: " + f.getName());
                updatePreviewImage(f);
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Selected image: " + f.getAbsolutePath(), ButtonType.OK);
                info.setHeaderText("Image selected");
                info.showAndWait();
            } else {
                System.out.println("File chooser returned no selection (user cancelled or closed dialog)");
                if (uploadStatusLabel != null) uploadStatusLabel.setText("No file selected");
                Alert info = new Alert(Alert.AlertType.INFORMATION, "No file selected or file chooser closed.", ButtonType.OK);
                info.setHeaderText("No selection");
                info.showAndWait();
            }
        } catch (Exception ex) {
            System.err.println("Error showing file chooser: " + ex.getMessage());
            ex.printStackTrace();
            if (uploadStatusLabel != null) uploadStatusLabel.setText("Error opening file chooser: " + ex.getMessage());
        }
    }

    @FXML
    private void onDropAreaClicked(MouseEvent event) {
        // delegate to same logic as the Upload button
        onChooseImage(null);
    }

    private void onDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File f = db.getFiles().get(0);
            chosenImageFile = f;
            updatePreviewImage(f);
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void updatePreviewCardFromFields() {
        Image img = null;
        if (chosenImageFile != null) {
            img = new Image(chosenImageFile.toURI().toString(), 242, 180, true, true);
        }
        loadPreviewCard(img, titleField.getText().isEmpty() ? "Title" : titleField.getText(), priceField.getText().isEmpty() ? "0" : priceField.getText(), categoryCombo.getValue());
    }

    private void updatePreviewImage(File f) {
        if (f == null) return;
        try {
            Image img = new Image(f.toURI().toString(), 242, 180, true, true);
            loadPreviewCard(img, titleField.getText().isEmpty() ? "Title" : titleField.getText(), priceField.getText().isEmpty() ? "0" : priceField.getText(), categoryCombo.getValue());
        } catch (Exception ex) {
            System.err.println("Error loading preview image: " + ex.getMessage());
        }
    }

    private void loadPreviewCard(Image img, String title, String price, String category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/artflow/ArtistArtcard.fxml"));
            Parent card = loader.load();

            // set values
            javafx.scene.Node imageView = card.lookup("#cardImageView");
            if (imageView instanceof ImageView && img != null) {
                ((ImageView) imageView).setImage(img);
            }
            javafx.scene.Node titleLbl = card.lookup("#cardTitleLabel");
            if (titleLbl instanceof Label) ((Label) titleLbl).setText(title);
            javafx.scene.Node catLbl = card.lookup("#cardCategoryLabel");
            if (catLbl instanceof Label) ((Label) catLbl).setText(category);
            javafx.scene.Node priceLbl = card.lookup("#cardPriceLabel");
            if (priceLbl instanceof Label) ((Label) priceLbl).setText("$" + price);

            // store metadata for filtering
            card.setUserData(category);

            previewHolder.getChildren().clear();
            previewHolder.getChildren().add(card);
        } catch (IOException e) {
            System.err.println("Error loading art card for preview: " + e.getMessage());
        }
    }

    @FXML
    private void onSave() {
        System.out.println("AddArtworkController.onSave called. parentController=" + (parentController != null));
        // Create a lightweight model and hand to parent to render (avoids reparenting nodes)
        String imgPath = null;
        if (chosenImageFile != null) {
            imgPath = chosenImageFile.toURI().toString();
        }
        ArtworkModel model = new ArtworkModel(titleField.getText(), priceField.getText(), categoryCombo.getValue(), imgPath);
        // add to central store; listeners (like ArtistMyArtworkController) will receive updates
        ArtworkStore.getInstance().add(model);

        // close dialog
        Stage stage = (Stage) previewHolder.getScene().getWindow();
        stage.close();

    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) previewHolder.getScene().getWindow();
        stage.close();
    }
}
