package com.example.artflow;

public class ArtworkModel {
    private final String title;
    private final String price;
    private final String category;
    private final String imagePath; // file URI or null

    public ArtworkModel(String title, String price, String category, String imagePath) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
    }

    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }
}

