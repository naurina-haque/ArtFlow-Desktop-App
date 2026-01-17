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

            System.out.println("================================================");
            System.out.println("DATABASE INITIALIZATION");
            System.out.println("================================================");
            System.out.println("Database URL: " + dbUrl);
            System.out.println("Working Directory: " + System.getProperty("user.dir"));
            System.out.println("Database full path: " + dbPath);
            System.out.println("Database file exists: " + new java.io.File(dbPath).exists());
            System.out.println("================================================");

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(dbUrl);
            System.out.println("Database connection established successfully!");

            createTablesIfMissing();
            ensureSchemaUpToDate();
            

            debugPrintTableCounts();

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

    /**
     * Debug helper: print DB path and list existing tables.
     */
    public void debugPrintTables() {
        System.out.println("Database URL: " + dbUrl);
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")) {
            System.out.println("Existing tables:");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to list tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Debug helper: print row counts for all tables
     */
    public void debugPrintTableCounts() {
        System.out.println("------------------------------------------------");
        System.out.println("DATABASE TABLE CONTENTS:");
        System.out.println("------------------------------------------------");
        try (Statement stmt = connection.createStatement()) {
            // Count users
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            if (rs.next()) {
                System.out.println("Users table: " + rs.getInt("count") + " records");
            }
            
            // Count artworks
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM artworks");
            if (rs.next()) {
                System.out.println("Artworks table: " + rs.getInt("count") + " records");
            }
            
            // Count orders
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM orders");
            if (rs.next()) {
                System.out.println("Orders table: " + rs.getInt("count") + " records");
            }
            System.out.println("------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Failed to count table rows: " + e.getMessage());
        }
    }

    /** Return the filesystem path of the SQLite database file (without jdbc:sqlite: prefix) */
    public String getDbFilePath() {
        if (dbUrl == null) return null;
        String p = dbUrl;
        if (p.startsWith("jdbc:sqlite:")) p = p.substring("jdbc:sqlite:".length());
        return p;
    }

    private void createTablesIfMissing() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "password TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "user_type TEXT NOT NULL," +
                "first_name TEXT," +
                "last_name TEXT," +
                "full_name TEXT," +
                "phone TEXT," +
                "address TEXT" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Ensured users table exists (non-destructive).");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Ensure phone and address columns exist for older DBs
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)");
            boolean hasPhone = false;
            boolean hasAddress = false;
            while (rs.next()) {
                String name = rs.getString("name");
                if ("phone".equalsIgnoreCase(name)) hasPhone = true;
                if ("address".equalsIgnoreCase(name)) hasAddress = true;
            }
            if (!hasPhone) {
                try {
                    stmt.execute("ALTER TABLE users ADD COLUMN phone TEXT");
                    System.out.println("Added missing column: phone");
                } catch (SQLException ex) { }
            }
            if (!hasAddress) {
                try {
                    stmt.execute("ALTER TABLE users ADD COLUMN address TEXT");
                    System.out.println("Added missing column: address");
                } catch (SQLException ex) { }
            }
        } catch (SQLException ignored) {}


        // create artworks table for persisting artworks
        String artSql = "CREATE TABLE IF NOT EXISTS artworks (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "price TEXT, " +
                "category TEXT, " +
                "image_path TEXT, " +
                "artist_name TEXT, " +
                "description TEXT" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(artSql);
            System.out.println("Ensured artworks table exists.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // ensure artist_name column exists for older DBs
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(artworks)");
            boolean hasArtist = false;
            while (rs.next()) {
                String name = rs.getString("name");
                if ("artist_name".equalsIgnoreCase(name)) { hasArtist = true; break; }
            }
            if (!hasArtist) {
                try {
                    stmt.execute("ALTER TABLE artworks ADD COLUMN artist_name TEXT");
                    System.out.println("Added missing column: artist_name");
                } catch (SQLException ex) { }
            }

            boolean hasDesc = false;
            rs = stmt.executeQuery("PRAGMA table_info(artworks)");
            while (rs.next()) {
                String name = rs.getString("name");
                if ("description".equalsIgnoreCase(name)) { hasDesc = true; break; }
            }
            if (!hasDesc) {
                try { stmt.execute("ALTER TABLE artworks ADD COLUMN description TEXT"); System.out.println("Added missing column: description"); } catch (SQLException ex) { }
            }
        } catch (SQLException ignored) {}
        // create orders table
        String ordersSql = "CREATE TABLE IF NOT EXISTS orders (" +
                "id TEXT PRIMARY KEY, " +
                "customer_name TEXT, " +
                "artist_name TEXT, " +
                "art_title TEXT, " +
                "quantity INTEGER, " +
                "amount REAL, " +
                "ordered_on TEXT, " +
                "status TEXT" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(ordersSql);
            System.out.println("Ensured orders table exists.");
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
                pstmt.setString(idx++, normalizedEmail);
            }
            pstmt.setString(idx++, password);
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
            pstmt.setString(2, password);
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

    public String getUserDebugInfo(String email) {
        if (email == null) return "email==null";
        String normalized = email.trim().toLowerCase();
        String sql = "SELECT password, user_type FROM users WHERE email = ? COLLATE NOCASE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return "NO_USER";
            String storedPw = rs.getString("password");
            String ut = rs.getString("user_type");
            if (ut == null) ut = "";
            if (!ut.equalsIgnoreCase("artist") && !ut.equalsIgnoreCase("buyer")) return "WRONG_TYPE:" + ut;
            if (storedPw == null) storedPw = "";
            return "OK";
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR:" + e.getMessage();
        }
    }


    public String getEmailForFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return null;
        String sql = "SELECT email FROM users WHERE full_name LIKE ? COLLATE NOCASE LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + fullName.trim() + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (SQLException e) {
            System.err.println("Error getting email for full name: " + e.getMessage());
        }
        return null;
    }


    public String checkCredentials(String email, String password, String expectedUserType) {
        if (email == null) return "NO_USER";
        String normalized = email.trim().toLowerCase();
        String sql = "SELECT password, user_type FROM users WHERE email = ? COLLATE NOCASE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return "NO_USER";
            String storedPw = rs.getString("password");
            String ut = rs.getString("user_type");
            if (ut == null) ut = "";
            if (!ut.equalsIgnoreCase(expectedUserType)) return "WRONG_TYPE:" + ut;
            if (storedPw == null) storedPw = "";
            if (!storedPw.equals(password)) return "WRONG_PASSWORD";
            return "OK";
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR:" + e.getMessage();
        }
    }

    public boolean validateUser(String email, String password, String userType) {
        return loginUser(email, password, userType) != null;
    }


    public synchronized boolean updateUserProfile(String currentEmail, String newFullName, String newEmail, String phone, String address) {
        if (currentEmail == null) return false;
        
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, address = ? WHERE email = ? COLLATE NOCASE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newFullName);
            ps.setString(2, newEmail != null ? newEmail.trim().toLowerCase() : currentEmail);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, currentEmail.trim().toLowerCase());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Profile updated in database for user: " + newFullName);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    

    public synchronized java.util.Map<String, String> getUserProfile(String email) {
        if (email == null) return null;
        
        String sql = "SELECT full_name, email, phone, address FROM users WHERE email = ? COLLATE NOCASE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                java.util.Map<String, String> profile = new java.util.HashMap<>();
                profile.put("full_name", rs.getString("full_name"));
                profile.put("email", rs.getString("email"));
                profile.put("phone", rs.getString("phone"));
                profile.put("address", rs.getString("address"));
                return profile;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user profile: " + e.getMessage());
        }
        return null;
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

    // Artwork persistence helpers
    public synchronized boolean insertArtwork(ArtworkModel m) {
        if (m == null) return false;
        String sql = "INSERT OR REPLACE INTO artworks(id, title, price, category, image_path, artist_name, description) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, m.getId());
            ps.setString(2, m.getTitle());
            ps.setString(3, m.getPrice());
            ps.setString(4, m.getCategory());
            ps.setString(5, m.getImagePath());
            ps.setString(6, m.getArtistName());
            ps.setString(7, m.getDescription());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting artwork: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean updateArtwork(ArtworkModel m) {
        if (m == null) return false;
        String sql = "UPDATE artworks SET title = ?, price = ?, category = ?, image_path = ?, artist_name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getPrice());
            ps.setString(3, m.getCategory());
            ps.setString(4, m.getImagePath());
            ps.setString(5, m.getArtistName());
            ps.setString(6, m.getDescription());
            ps.setString(7, m.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating artwork: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean deleteArtwork(String id) {
        if (id == null) return false;
        String sql = "DELETE FROM artworks WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting artwork: " + e.getMessage());
            return false;
        }
    }

    public synchronized java.util.List<ArtworkModel> listArtworks() {
        java.util.List<ArtworkModel> out = new java.util.ArrayList<>();
        String sql = "SELECT id, title, price, category, image_path, artist_name, description FROM artworks";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String price = rs.getString("price");
                String category = rs.getString("category");
                String imagePath = rs.getString("image_path");
                String artistName = null;
                String description = null;
                try { artistName = rs.getString("artist_name"); } catch (Exception ignored) {}
                try { description = rs.getString("description"); } catch (Exception ignored) {}
                out.add(new ArtworkModel(id, title, price, category, imagePath, artistName, description));
            }
        } catch (SQLException e) {
            System.err.println("Error reading artworks from DB: " + e.getMessage());
        }
        return out;
    }

    public synchronized boolean insertOrder(OrderModel o) {
        if (o == null) return false;
        String sql = "INSERT INTO orders(id, customer_name, artist_name, art_title, quantity, amount, ordered_on, status) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, o.getId());
            ps.setString(2, o.getCustomerName());
            ps.setString(3, o.getArtistName());
            ps.setString(4, o.getArtTitle());
            ps.setInt(5, o.getQuantity());
            ps.setDouble(6, o.getAmount());
            ps.setString(7, o.getOrderedOn());
            ps.setString(8, o.getStatus());
            ps.executeUpdate();
            // Debug: print DB path
            try { System.out.println("Order inserted into DB file: " + getDbFilePath() + " (id=" + o.getId() + ")"); }
            catch (Exception ignored) {}


            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting order: " + e.getMessage());
            return false;
        }
    }

    public synchronized java.util.List<OrderModel> listOrders() {
        java.util.List<OrderModel> out = new java.util.ArrayList<>();
        String sql = "SELECT id, customer_name, artist_name, art_title, quantity, amount, ordered_on, status FROM orders ORDER BY ordered_on DESC";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String customer = rs.getString("customer_name");
                String artist = rs.getString("artist_name");
                String title = rs.getString("art_title");
                int qty = rs.getInt("quantity");
                double amount = rs.getDouble("amount");
                String orderedOn = rs.getString("ordered_on");
                String status = rs.getString("status");
                out.add(new OrderModel(id, customer, artist, title, qty, amount, orderedOn, status));
            }
        } catch (SQLException e) {
            System.err.println("Error reading orders from DB: " + e.getMessage());
        }
        return out;
    }

    public synchronized boolean updateOrderStatus(String orderId, String newStatus) {
        if (orderId == null || newStatus == null) return false;
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, orderId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }
}
