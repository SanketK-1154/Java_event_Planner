package com.eventplanner.dao;

import com.eventplanner.model.Group;
import com.eventplanner.model.User; // Assuming User model might be needed indirectly or for future methods

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    public Group createGroup(String groupName, int adminId) throws SQLException {
        String joinCode;
        // Generate a unique join code (simple example, might need retry logic)
        do {
            joinCode = generateJoinCode(6); // Generate a 6-character code
        } while (findGroupByJoinCode(joinCode) != null); // Ensure uniqueness

        String sql = "INSERT INTO groups1 (name, admin_id, join_code) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, groupName);
            stmt.setInt(2, adminId);
            stmt.setString(3, joinCode);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    // Need admin name to construct Group object fully, fetch it separately
                    UserDAO userDAO = new UserDAO(); // Consider dependency injection later
                    User admin = userDAO.findUserById(adminId);
                    String adminName = (admin != null) ? admin.getName() : "Unknown Admin";
                    return new Group(generatedId, groupName, adminId, joinCode, adminName);
                }
            }
            return null; // Insert failed or couldn't get ID
        } finally {
            // Close resources manually as we need generatedKeys after stmt closes in try-with-resources
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    /**
     * Retrieves all groups created by a specific admin.
     * Assumes table name is 'groups1'.
     */
    public List<Group> getGroupsByAdminId(int adminId) throws SQLException {
        List<Group> groups = new ArrayList<>();
        // Simpler query as we know the admin ID, but still need admin name for consistency
        String sql = "SELECT g.*, a.name as admin_name " +
                "FROM groups1 g " + // Changed table name
                "JOIN users a ON g.admin_id = a.id " +
                "WHERE g.admin_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groups.add(mapRowToGroup(rs)); // Reuse existing mapper
            }
        }
        return groups;
    }

    // Simple helper to generate random alphanumeric join code
    private String generateJoinCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Adds a user to a group if the join code is valid and the user isn't already a member.
     * Returns the Group object if successful, null otherwise.
     * Assumes group table name is 'groups1'.
     */
    public Group joinGroup(int userId, String joinCode) throws SQLException {
        // This method uses findGroupByJoinCode which needs the table name update.
        // The INSERT statement targets group_memberships, which is correct.
        Group group = findGroupByJoinCode(joinCode); // This now queries groups1
        if (group == null) {
            return null; // Invalid join code
        }

        // Check if user is already a member
        if (isUserMember(userId, group.getId())) {
            return group; // Already a member, return group info
        }

        // Add user to group_memberships table
        String sql = "INSERT INTO group_memberships (user_id, group_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, group.getId());

            int rowsAffected = stmt.executeUpdate();
            return (rowsAffected > 0) ? group : null; // Return group if insert successful
        }
    }

    /**
     * Removes a user from a specific group.
     * Targets group_memberships table, so no change needed here.
     */
    public boolean leaveGroup(int userId, int groupId) throws SQLException {
        String sql = "DELETE FROM group_memberships WHERE user_id = ? AND group_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, groupId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Retrieves all groups that a specific user is a member of.
     * Joins with users table to get admin name.
     * Assumes group table name is 'groups1'.
     */
    public List<Group> getUserGroups(int userId) throws SQLException {
        List<Group> groups = new ArrayList<>();
        // *** MODIFIED SQL: Changed "FROM groups g" to "FROM groups1 g" ***
        String sql = "SELECT g.id, g.name, g.admin_id, g.join_code, a.name as admin_name " +
                "FROM groups1 g " + // Changed table name here
                "JOIN group_memberships gm ON g.id = gm.group_id " +
                "JOIN users a ON g.admin_id = a.id " +
                "WHERE gm.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groups.add(mapRowToGroup(rs));
            }
        }
        return groups;
    }

    // --- Helper Methods ---

    /**
     * Finds a group by its join code.
     * Assumes group table name is 'groups1'.
     */
    private Group findGroupByJoinCode(String joinCode) throws SQLException {
        // *** MODIFIED SQL: Changed "FROM groups g" to "FROM groups1 g" ***
        String sql = "SELECT g.*, a.name as admin_name " +
                "FROM groups1 g " + // Changed table name here
                "JOIN users a ON g.admin_id = a.id " +
                "WHERE g.join_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, joinCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToGroup(rs);
            }
        }
        return null;
    }

    /**
     * Checks if a user is already a member of a group.
     * Targets group_memberships table, so no change needed here.
     */
    private boolean isUserMember(int userId, int groupId) throws SQLException {
        String sql = "SELECT 1 FROM group_memberships WHERE user_id = ? AND group_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, groupId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a row exists
        }
    }

    public boolean removeUserFromGroup(int userIdToRemove, int groupId) throws SQLException {
        // Future enhancement: Could add adminId parameter and check admin owns the group before deleting
        String sql = "DELETE FROM group_memberships WHERE user_id = ? AND group_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userIdToRemove);
            stmt.setInt(2, groupId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    public boolean deleteGroup(int groupId, int adminId) throws SQLException {
        // Delete the group only if the ID matches AND the admin_id matches
        String sql = "DELETE FROM groups1 WHERE id = ? AND admin_id = ?"; // Use groups1 if that's your table name
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, adminId); // Ensure admin owns the group
            int rowsAffected = stmt.executeUpdate();
            System.out.println("DEBUG: Attempted to delete group ID " + groupId + " by admin ID " + adminId + ". Rows affected: " + rowsAffected); // Debug
            return rowsAffected > 0;
        }
    }
    public List<User> getUsersByGroupId(int groupId) throws SQLException {
        List<User> members = new ArrayList<>();
        // Fetch basic user info for members of the specified group
        String sql = "SELECT u.id, u.name, u.email, u.role, u.registration_date " + // Include necessary User fields
                "FROM users u " +
                "JOIN group_memberships gm ON u.id = gm.user_id " +
                "WHERE gm.group_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            UserDAO userDAO = new UserDAO(); // To reuse the mapping logic
            while (rs.next()) {
                // Manually map here or reuse UserDAO's mapRowToUser if it's accessible/suitable
                members.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        null, // Don't fetch/expose password hash here
                        rs.getString("role"),
                        rs.getTimestamp("registration_date")
                ));
            }
        }
        return members;
    }
    /**
     * Maps a row from the ResultSet to a Group object.
     * No change needed here as it uses column names from the SELECT statement.
     */
    private Group mapRowToGroup(ResultSet rs) throws SQLException {
        return new Group(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("admin_id"),
                rs.getString("join_code"),
                rs.getString("admin_name")
        );
    }
}