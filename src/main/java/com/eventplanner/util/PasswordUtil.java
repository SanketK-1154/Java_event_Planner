package com.eventplanner.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hash a password using BCrypt
    public static String hashPassword(String plainTextPassword) {
        // The gensalt method automatically handles generating a random salt
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    // Check a plaintext password against a stored hash
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            // Handle cases where the hash is invalid or not present
            // Log this potentially serious error
            System.err.println("Invalid hash provided for comparison.");
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Handle invalid salt format exceptions during checkpw
            System.err.println("Error checking password: " + e.getMessage());
            return false;
        }
    }
}