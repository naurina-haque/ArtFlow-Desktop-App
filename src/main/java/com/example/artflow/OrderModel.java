package com.example.artflow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OrderModel {
    private final String id;
    private final String customerName;
    private final String artistName;
    private final String artTitle;
    private final int quantity;
    private final double amount; // total amount
    private final String orderedOn; // ISO string
    private final String status; // pending/completed

    public OrderModel(String customerName, String artistName, String artTitle, int quantity, double amount, String status) {
        this(UUID.randomUUID().toString(), customerName, artistName, artTitle, quantity, amount,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), status);
    }

    public OrderModel(String id, String customerName, String artistName, String artTitle, int quantity, double amount, String orderedOn, String status) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.customerName = customerName;
        this.artistName = artistName;
        this.artTitle = artTitle;
        this.quantity = quantity;
        this.amount = amount;
        this.orderedOn = orderedOn;
        this.status = status;
    }

    public String getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getArtistName() { return artistName; }
    public String getArtTitle() { return artTitle; }
    public int getQuantity() { return quantity; }
    public double getAmount() { return amount; }
    public String getOrderedOn() { return orderedOn; }
    public String getStatus() { return status; }
}

