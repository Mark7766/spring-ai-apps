package com.sandy.newston;

import java.sql.*;

/**
 * Data Operation
 */
public class DatabaseHelper {
    private static final String DB_FILE = "keys.db";
    static void initDB() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS keys (key TEXT PRIMARY KEY)");
        }
    }

    static boolean keyExists(String key) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM keys WHERE key = ?")) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    static void setKey(String key) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
             PreparedStatement pstmt = conn.prepareStatement("INSERT OR IGNORE INTO keys (key) VALUES (?)")) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
        }
    }
}
