package com.example.artflow;

public class CurrentUser {
    private static String fullName;
    private static String userType;

    private CurrentUser() {}

    public static void setFullName(String name) { fullName = name; }
    public static String getFullName() { return fullName; }
    public static void setUserType(String type) { userType = type; }
    public static String getUserType() { return userType; }
}

