package com.eventplanner.service;

import com.eventplanner.dao.UserDAO;
import com.eventplanner.model.User;
import com.eventplanner.util.PasswordUtil; // Use your PasswordUtil

import java.sql.SQLException;

public class UserService {

    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Updates user profile information.
     * Handles password checking and hashing if a new password is provided.
     */
    public UpdateResult updateProfile(User currentUser, String newName, String currentPlainPassword, String newPlainPassword) {
        if (currentUser == null || newName == null || newName.trim().isEmpty()) {
            return new UpdateResult(false, "Invalid input provided.", currentUser);
        }

        String newHashedPassword = null;
        boolean changePassword = !newPlainPassword.isEmpty();

        if (changePassword) {
            // 1. Verify current password
            if (currentPlainPassword.isEmpty() || !PasswordUtil.checkPassword(currentPlainPassword, currentUser.getPassword())) {
                return new UpdateResult(false, "Incorrect current password.", currentUser);
            }
            // 2. Hash the new password
            newHashedPassword = PasswordUtil.hashPassword(newPlainPassword);
        }

        // 3. Call DAO to update
        try {
            boolean success = userDAO.updateUser(currentUser.getId(), newName.trim(), newHashedPassword);
            if (success) {
                // Fetch the updated user object to return (contains new name/pass hash)
                User updatedUser = userDAO.findUserById(currentUser.getId());
                return new UpdateResult(true, "Profile updated successfully.", updatedUser);
            } else {
                // This might happen if the ID wasn't found, though unlikely here
                return new UpdateResult(false, "Failed to update profile in database.", currentUser);
            }
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            // Could check e.getMessage() for specific SQL errors if needed
            return new UpdateResult(false, "Database error during update.", currentUser);
        }
    }

    // Inner class to return update status, message, and updated user
    public static class UpdateResult {
        private final boolean success;
        private final String message;
        private final User updatedUser;

        public UpdateResult(boolean success, String message, User updatedUser) {
            this.success = success;
            this.message = message;
            this.updatedUser = updatedUser;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUpdatedUser() { return updatedUser; }
    }
}