package com.example.artflow;

public class User {
    private int id;
    private String username;
    private String email;
    private String userType;

    public User(int id, String username, String email, String userType) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userType = userType;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getUserType() { return userType; }
}
