package com.example.artflow;

import java.sql.*;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


public class DatabaseHelper {
    private final String dbUrl;
    private static DatabaseHelper instance;
    private Connection connection;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private boolean hasUsernameColumn = false;

    private DatabaseHelper() {
        try {
            String dbPath = Paths.get("artflow.db").toAbsolutePath().toString();
            dbUrl = "jdbc:sqlite:" + dbPath;

            System.out.println("Initializing database connection...");
            System.out.println("Database URL: " + dbUrl);
            System.out.println("Working Directory: " + System.getProperty("user.dir"));

            System.out.println("Database full path: " + dbPath);

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(dbUrl);
            System.out.println("Database connection established successfully!");

            createTablesIfMissing();
            ensureSchemaUpToDate();

        } catch (Exception e) {
            System.err.println("Error initializing database:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    private void createTablesIfMissing() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "password TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "user_type TEXT NOT NULL," +
                "first_name TEXT," +
                "last_name TEXT," +
                "full_name TEXT" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Ensured users table exists (non-destructive).");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureSchemaUpToDate() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)");
            Set<String> cols = new HashSet<>();
            while (rs.next()) {
                String name = rs.getString("name");
                if (name != null) cols.add(name.toLowerCase());
            }

            hasUsernameColumn = cols.contains("username");

            if (!cols.contains("first_name")) {
                try {
                    stmt.execute("ALTER TABLE users ADD COLUMN first_name TEXT");
                    System.out.println("Added missing column: first_name");
                } catch (SQLException ex) {
                }
            }
            if (!cols.contains("last_name")) {
                try {
                    stmt.execute("ALTER TABLE users ADD COLUMN last_name TEXT");
                    System.out.println("Added missing column: last_name");
                } catch (SQLException ex) {
                }
            }
            if (!cols.contains("full_name")) {
                try {
                    stmt.execute("ALTER TABLE users ADD COLUMN full_name TEXT");
                    System.out.println("Added missing column: full_name");
                } catch (SQLException ex) {
                }
            }

            if (hasUsernameColumn) {
                System.out.println("Existing 'username' column detected; signup will continue to populate it for compatibility.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean signupUser(String firstName, String lastName, String email, String password, String userType) {
        String fullName = (firstName + " " + lastName).trim();
        System.out.println("Attempting to sign up user: " + email + " (" + fullName + ")");

        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
            System.out.println("Sign up error: first and last name required");
            return false;
        }
        if (password == null || password.length() < 6) {
            System.out.println("Sign up error: password must be at least 6 characters");
            return false;
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            System.out.println("Sign up error: invalid email format");
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();

        String sql;
        if (hasUsernameColumn) {
            sql = "INSERT INTO users(username, password, email, user_type, first_name, last_name, full_name) VALUES(?,?,?,?,?,?,?)";
        } else {
            sql = "INSERT INTO users(password, email, user_type, first_name, last_name, full_name) VALUES(?,?,?,?,?,?)";
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int idx = 1;
            if (hasUsernameColumn) {
                pstmt.setString(idx++, normalizedEmail); // username = email for compatibility
            }
            pstmt.setString(idx++, password); // In production, hash the password
            pstmt.setString(idx++, normalizedEmail);
            pstmt.setString(idx++, userType);
            pstmt.setString(idx++, firstName.trim());
            pstmt.setString(idx++, lastName.trim());
            pstmt.setString(idx++, fullName);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Sign up complete for: " + normalizedEmail);
                return true;
            } else {
                System.out.println("Sign up failed: no rows inserted");
                return false;
            }
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("unique") && msg.contains("email")) {
                System.out.println("Sign up error: email already registered");
            } else {
                System.err.println("Error signing up user:");
                e.printStackTrace();
            }
            return false;
        }
    }

    public String loginUser(String email, String password, String userType) {
        if (email == null || password == null) return null;
        String normalizedEmail = email.trim().toLowerCase();
        String sql = "SELECT first_name, last_name, full_name FROM users WHERE email = ? COLLATE NOCASE AND password = ? AND user_type = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, normalizedEmail);
            pstmt.setString(2, password); // In production, verify hashed password
            pstmt.setString(3, userType);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String fullName = rs.getString("full_name");
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                if ((fullName == null || fullName.trim().isEmpty()) && first != null) {
                    fullName = (first + (last != null && !last.trim().isEmpty() ? " " + last : "")).trim();
                }
                if (fullName == null || fullName.trim().isEmpty()) {
                    fullName = normalizedEmail;
                }
                System.out.println("Login successful for: " + normalizedEmail + " (" + fullName + ")");
                return fullName;
            } else {
                System.out.println("Login failed for: " + normalizedEmail);
                return null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public boolean validateUser(String email, String password, String userType) {
        return loginUser(email, password, userType) != null;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
