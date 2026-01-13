package com.example.artflow;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
import javafx.scene.layout.FlowPane;

import java.io.File;
import java.util.function.Consumer;

public class AddArtworkController {

    @FXML
    public VBox previewHolder; // container with controls

    @FXML
    public VBox previewCardContainer; // hosts preview ImageView

    @FXML
    public ImageView previewImageView; // NEW: direct ImageView to show uploaded image

    @FXML
    public TextField titleField;

    @FXML
    public TextArea descriptionArea;

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

    @FXML
    public Label headerLabel;

    @FXML
    public Label imageSectionLabel;

    @FXML
    public Label create;

    @FXML
    public Label sublabel;

    // flag to ignore the click immediately after a drag-drop to prevent opening file chooser twice
    private volatile boolean droppedJustNow = false;

    private File chosenImageFile;
    private ArtistMyArtworkController parentController;
    private FlowPane parentArtworksFlow;
    private boolean editMode = false;
    private String editingId = null;
    private Consumer<ArtworkModel> editCallback = null;

    public void setParentController(ArtistMyArtworkController parent) {
        this.parentController = parent;
    }

    public void setParentArtworksFlow(FlowPane flow) {
        this.parentArtworksFlow = flow;
    }

    public void loadForEdit(ArtworkModel model, Consumer<ArtworkModel> onSaveCallback) {
        if (model == null) return;
        editMode = true;
        editingId = model.getId();
        titleField.setText(model.getTitle());
        if (descriptionArea != null) descriptionArea.setText(model.getDescription() == null ? "" : model.getDescription());
        priceField.setText(model.getPrice());
        categoryCombo.getSelectionModel().select(model.getCategory());
        if (model.getImagePath() != null) {
            try {
                chosenImageFile = new File(new java.net.URI(model.getImagePath()));
            } catch (Exception ignored) { chosenImageFile = null; }
            // show preview image
            if (chosenImageFile != null) {
                setPreviewImage(chosenImageFile);
            }
        }
        this.editCallback = onSaveCallback;
        // update UI to reflect edit mode
        if (headerLabel != null) headerLabel.setText("Edit Art");
        if (saveBtn != null) saveBtn.setText("Save");
        if (imageSectionLabel != null) imageSectionLabel.setText("Edit Image");
        if (create != null) create.setText("Edit ArtWork");
        if (sublabel != null) sublabel.setText("");
    }

    @FXML
    private void initialize() {
        // guard: avoid running design-time/runtime-only code when opened in SceneBuilder
        if (categoryCombo == null || titleField == null || priceField == null) {
            System.out.println("AddArtworkController: running in design mode or missing controls");
            return;
        }

        // ensure defaults for add mode
        if (headerLabel != null) headerLabel.setText("Create New Artwork");
        if (saveBtn != null) saveBtn.setText("🚀 Publish Artwork");
        if (imageSectionLabel != null) imageSectionLabel.setText("📷 Artwork Image");
        if (create != null) create.setText("Create New ArtWork");


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

        // wire up listeners to update preview (title/price/category only affect preview text when using artcard; we'll keep hooks)
        titleField.textProperty().addListener((obs, oldV, newV) -> {});
        priceField.textProperty().addListener((obs, oldV, newV) -> {});
        categoryCombo.valueProperty().addListener((obs, oldV, newV) -> {});

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
                // remove chosen image, clear preview image view and show drop area again
                System.out.println("Remove clicked (lambda)");
                chosenImageFile = null;
                if (previewImageView != null) previewImageView.setImage(null);
                if (previewCardContainer != null) {
                    previewCardContainer.setVisible(false);
                    previewCardContainer.setManaged(false);
                }
                if (dropArea != null) {
                    dropArea.setVisible(true);
                    dropArea.setManaged(true);
                }
                if (uploadStatusLabel != null) uploadStatusLabel.setText("Image removed");
            });
        }

        // initial state: dropArea visible, preview hidden
        if (dropArea != null) { dropArea.setVisible(true); dropArea.setManaged(true); }
        if (previewCardContainer != null) { previewCardContainer.setVisible(false); previewCardContainer.setManaged(false); }
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
                if (uploadStatusLabel != null) uploadStatusLabel.setText("Opening file chooser (no owner)...");
                f = chooser.showOpenDialog(null);
            }

            if (f != null) {
                System.out.println("File chosen: " + f.getAbsolutePath());
                chosenImageFile = f;
                if (uploadStatusLabel != null) uploadStatusLabel.setText("Selected: " + f.getName());
                setPreviewImage(f);
            } else {
                if (uploadStatusLabel != null) uploadStatusLabel.setText("No file selected");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (uploadStatusLabel != null) uploadStatusLabel.setText("Error opening file chooser: " + ex.getMessage());
        }
    }

    @FXML
    private void onDropAreaClicked(MouseEvent event) {
        // If we just handled a drop, ignore the click to avoid opening file chooser twice
        if (droppedJustNow) {
            droppedJustNow = false;
            return;
        }
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
        System.out.println("onDragDropped called");
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File f = db.getFiles().get(0);
            System.out.println("File dropped: " + f.getAbsolutePath());
            chosenImageFile = f;
            setPreviewImage(f);
            success = true;
            // set flag so the following click doesn't re-open file chooser
            droppedJustNow = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void setPreviewImage(File f) {
        System.out.println("setPreviewImage called; file=" + (f == null ? "null" : f.getAbsolutePath()));
        if (f == null) return;
        try {
            // load image synchronously (no background loading) to ensure dimensions available
            Image img = new Image(f.toURI().toString(), 0, 0, true, true, false);
            System.out.println("Image loaded: width=" + img.getWidth() + ", height=" + img.getHeight());
            javafx.application.Platform.runLater(() -> {
                try {
                    if (previewImageView == null) {
                        // try lookup from scene as a fallback
                        if (previewHolder != null && previewHolder.getScene() != null) {
                            javafx.scene.Node looked = previewHolder.getScene().lookup("#previewImageView");
                            if (looked instanceof ImageView) {
                                previewImageView = (ImageView) looked;
                                System.out.println("previewImageView located via scene.lookup");
                            }
                        }
                    }
                } catch (Exception lookupEx) {
                    System.out.println("previewImageView lookup failed: " + lookupEx.getMessage());
                }
                if (previewImageView != null) {
                    System.out.println("previewImageView available — setting image");
                    previewImageView.setImage(img);
                    previewImageView.setSmooth(true);
                    previewImageView.setPreserveRatio(true);
                    // bind fitWidth to container width minus some padding so it fits
                    try {
                        if (previewCardContainer != null) {
                            previewImageView.fitWidthProperty().bind(previewCardContainer.widthProperty().subtract(20));
                        } else {
                            previewImageView.setFitWidth(300);
                        }
                    } catch (Exception bindEx) {
                        previewImageView.setFitWidth(300);
                    }
                    previewImageView.setVisible(true);
                } else {
                    System.out.println("previewImageView is null even after lookup — creating one programmatically");
                    if (previewCardContainer != null) {
                        ImageView iv = new ImageView(img);
                        iv.setSmooth(true);
                        iv.setPreserveRatio(true);
                        iv.setFitHeight(220);
                        try { iv.fitWidthProperty().bind(previewCardContainer.widthProperty().subtract(20)); } catch (Exception ignore) {}
                        previewCardContainer.getChildren().clear();
                        previewCardContainer.getChildren().add(iv);
                        previewImageView = iv; // keep reference
                    }
                }
                // show preview container, hide drop area
                if (previewCardContainer != null) {
                    System.out.println("Showing previewCardContainer");
                    previewCardContainer.setVisible(true);
                    previewCardContainer.setManaged(true);
                    previewCardContainer.toFront();
                } else {
                    System.out.println("previewCardContainer is null");
                }
                if (dropArea != null) {
                    System.out.println("Hiding dropArea");
                    dropArea.setVisible(false);
                    dropArea.setManaged(false);
                } else {
                    System.out.println("dropArea is null");
                }
                if (previewHolder != null) previewHolder.requestLayout();
                System.out.println("Preview visibility: imageViewVisible=" + (previewImageView != null && previewImageView.isVisible()) + ", containerVisible=" + (previewCardContainer != null && previewCardContainer.isVisible()));
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onSave() {
        // Create a lightweight model and hand to parent to render (avoids reparenting nodes)
        String imgPath = null;
        if (chosenImageFile != null) {
            imgPath = chosenImageFile.toURI().toString();
        }
        ArtworkModel model;
        String desc = descriptionArea == null ? null : descriptionArea.getText();
        if (editMode && editingId != null) {
            model = new ArtworkModel(editingId, titleField.getText(), priceField.getText(), categoryCombo.getValue(), imgPath, CurrentUser.getFullName(), desc);
            ArtworkStore.getInstance().update(model);
            if (editCallback != null) editCallback.accept(model);
        } else {
            model = new ArtworkModel(titleField.getText(), priceField.getText(), categoryCombo.getValue(), imgPath, CurrentUser.getFullName(), desc);
            ArtworkStore.getInstance().add(model);
        }

        // close dialog
        Stage stage = (Stage) previewHolder.getScene().getWindow();
        stage.close();

    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) previewHolder.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onRemoveImage(ActionEvent event) {
        System.out.println("onRemoveImage called");
        // same behavior as removeImageBtn action but compatible with FXML handler signature
        chosenImageFile = null;
        if (previewImageView != null) previewImageView.setImage(null);
        if (previewCardContainer != null) {
            previewCardContainer.setVisible(false);
            previewCardContainer.setManaged(false);
        }
        if (dropArea != null) {
            dropArea.setVisible(true);
            dropArea.setManaged(true);
        }
        if (uploadStatusLabel != null) uploadStatusLabel.setText("Image removed");
    }
}
