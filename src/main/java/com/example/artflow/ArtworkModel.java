package com.example.artflow;

import java.util.UUID;

public class ArtworkModel {
    private final String id;
    private final String title;
    private final String price;
    private final String category;
    private final String imagePath; // file URI or null
    private final String artistName;
    private final String description;

    public ArtworkModel(String title, String price, String category, String imagePath) {
        this(UUID.randomUUID().toString(), title, price, category, imagePath, null, null);
    }

    // Convenience constructor to include artist name and description when creating a new artwork
    public ArtworkModel(String title, String price, String category, String imagePath, String artistName, String description) {
        this(UUID.randomUUID().toString(), title, price, category, imagePath, artistName, description);
    }

    public ArtworkModel(String id, String title, String price, String category, String imagePath) {
        this(id, title, price, category, imagePath, null, null);
    }

    public ArtworkModel(String id, String title, String price, String category, String imagePath, String artistName, String description) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
        this.artistName = artistName;
        this.description = description;
    }

    public String getDescription() { return description; }

    public String getArtistName() { return artistName; }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }
}
