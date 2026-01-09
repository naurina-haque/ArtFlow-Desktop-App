package com.example.artflow;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class CustomerArtDetailsController {
    @FXML private ImageView detailImageView;
    @FXML private Label detailTitle;
    @FXML private Label detailArtist;
    @FXML private Label detailCategory;
    @FXML private Label detailPrice;
    @FXML private Button buyButton;
    @FXML private Button closeButton;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Label detailTotal;
    @FXML private Label detailDescription; // Added description label

    private ArtworkModel model;

    public void setModel(ArtworkModel m) {
        this.model = m;
        if (m == null) return;
        detailTitle.setText(m.getTitle());
        detailArtist.setText(m.getArtistName() == null ? "" : "by " + m.getArtistName());
        detailCategory.setText(m.getCategory());
        detailPrice.setText("$" + m.getPrice());
        if (detailDescription != null) detailDescription.setText(m.getDescription() == null ? "" : m.getDescription());

        // load image gracefully
        try {
            if (m.getImagePath() != null && !m.getImagePath().isBlank()) {
                Image img = new Image(m.getImagePath(), 480, 320, true, true);
                detailImageView.setImage(img);
            }
        } catch (Exception ignored) {
            // leave image empty on failure
        }

        // initialize total based on default spinner value
        updateTotalFromSpinner();
    }

    @FXML
    private void initialize() {
        // Configure quantity spinner
        try {
            SpinnerValueFactory.IntegerSpinnerValueFactory vf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1);
            if (quantitySpinner != null) quantitySpinner.setValueFactory(vf);
            if (quantitySpinner != null) quantitySpinner.valueProperty().addListener((obs, oldV, newV) -> updateTotalFromSpinner());
        } catch (Exception ignored) {}

        if (closeButton != null) {
            closeButton.setOnAction(e -> {
                Stage s = (Stage) closeButton.getScene().getWindow();
                s.close();
            });
        }

        if (buyButton != null) {
            buyButton.setOnAction(e -> {
                try {
                    int qty = (quantitySpinner == null || quantitySpinner.getValue() == null) ? 1 : quantitySpinner.getValue();
                    double unit = 0.0;
                    if (model != null && model.getPrice() != null) {
                        try { unit = Double.parseDouble(model.getPrice().replaceAll("[^0-9.\\-]", "")); } catch (Exception ignored) {}
                    }
                    double total = unit * qty;

                    String customer = CurrentUser.getFullName() == null ? "Guest" : CurrentUser.getFullName();
                    String artist = model == null ? "" : (model.getArtistName() == null ? "" : model.getArtistName());
                    String title = model == null ? "" : model.getTitle();

                    OrderModel order = new OrderModel(customer, artist, title, qty, total, "pending");
                    boolean ok = DatabaseHelper.getInstance().insertOrder(order);
                    if (ok) {
                        // derive DB path same as DatabaseHelper uses (relative project artflow.db)
                        String dbPath = "artflow.db";
                        try { dbPath = java.nio.file.Paths.get("artflow.db").toAbsolutePath().toString(); } catch (Exception ignored) {}
                        // derive CSV path next to the DB
                        String csvPathStr = "orders_log.csv";
                        try {
                            java.nio.file.Path dbPathP = java.nio.file.Paths.get(dbPath);
                            java.nio.file.Path csvPath = (dbPathP.getParent() == null ? java.nio.file.Paths.get("orders_log.csv") : dbPathP.getParent().resolve("orders_log.csv"));
                            csvPathStr = csvPath.toAbsolutePath().toString();
                        } catch (Exception ignored) {}
                        Alert a = new Alert(Alert.AlertType.INFORMATION);
                        a.setTitle("Order placed");
                        a.setHeaderText(null);
                        a.setContentText("Order placed successfully.\nOrder id: " + order.getId() + "\nDatabase file: " + dbPath + "\nCSV log: " + csvPathStr + "\nOpen the CSV file to inspect the order.");
                        a.showAndWait();
                        System.out.println("Order created: id=" + order.getId() + ", db=" + dbPath + ", csv=" + csvPathStr);
                      } else {
                         Alert a = new Alert(Alert.AlertType.ERROR);
                         a.setTitle("Order failed");
                         a.setHeaderText(null);
                         a.setContentText("Failed to place the order. Please try again.");
                         a.showAndWait();
                     }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setTitle("Order error");
                    a.setHeaderText(null);
                    a.setContentText("Unexpected error: " + ex.getMessage());
                    a.showAndWait();
                } finally {
                    Stage s = (Stage) buyButton.getScene().getWindow();
                    s.close();
                }
            });
        }
    }

    private void updateTotalFromSpinner() {
        if (detailTotal == null) return;
        int qty = 1;
        if (quantitySpinner != null && quantitySpinner.getValue() != null) qty = quantitySpinner.getValue();
        double unit = 0.0;
        if (model != null && model.getPrice() != null) {
            try {
                String p = model.getPrice().replaceAll("[^0-9.\\-]", "");
                if (!p.isBlank()) unit = Double.parseDouble(p);
            } catch (Exception ignored) { unit = 0.0; }
        }
        double total = unit * qty;
        detailTotal.setText(String.format("$%.2f", total));
    }
}
