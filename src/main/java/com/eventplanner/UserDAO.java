package com.eventplanner.dao;

import com.eventplanner.model.User;
import com.eventplanner.util.PasswordUtil;

import java.sql.*; // Import Timestamp

public class UserDAO {

    /**
     * Registers a new user with a hashed password.
     * Registration date is handled by the database DEFAULT CURRENT_TIMESTAMP.
     */
    public boolean registerUser(String name, String email, String plainPassword) throws SQLException {
        if (findUserByEmail(email) != null) {
            return false; // Indicate email already exists
        }

        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        String role = "USER";
        // Note: We no longer need to explicitly insert registration_date
        // as the database column has a DEFAULT CURRENT_TIMESTAMP constraint.
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, role);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Attempts to log in a user by checking the provided plaintext password
     * against the stored hash.
     */
    public User loginUser(String email, String plainPassword) throws SQLException {
        User user = findUserByEmail(email);
        if (user != null) {
            if (PasswordUtil.checkPassword(plainPassword, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Finds a user by their email address.
     * Returns the User object or null if not found.
     */
    public User findUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs); // Use helper method
            }
        }
        return null;
    }

    /**
     * Finds a user by their ID.
     * Returns the User object or null if not found.
     */
    public User findUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs); // Use helper method
            }
        }
        return null;
    }


    // *** Updated helper method to map a ResultSet row to a User object ***
    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getTimestamp("registration_date") // Retrieve the new column
        );
    }

    public boolean updateUser(int userId, String newName, String newHashedPassword) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET name = ?");
        boolean updatePassword = (newHashedPassword != null && !newHashedPassword.isEmpty());

        if (updatePassword) {
            sqlBuilder.append(", password = ?");
        }
        sqlBuilder.append(" WHERE id = ?");

        String sql = sqlBuilder.toString();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, newName);

            if (updatePassword) {
                stmt.setString(paramIndex++, newHashedPassword);
            }

            stmt.setInt(paramIndex++, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}