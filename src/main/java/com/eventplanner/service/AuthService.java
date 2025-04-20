package com.eventplanner.service;

import com.eventplanner.dao.UserDAO;
import com.eventplanner.model.User;

import java.sql.SQLException;

public class AuthService {

    private UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO(); // Instantiate DAO
    }

    public User login(String email, String password) {
        try {
            return userDAO.loginUser(email, password);
        } catch (SQLException e) {
            // Log the exception (using a proper logging framework is recommended)
            System.err.println("Login Error: " + e.getMessage());
            e.printStackTrace();
            return null; // Indicate login failure due to error
        }
    }

    public boolean register(String name, String email, String password) {
        if (name == null || name.trim().isEmpty() ||
                email == null || email.trim().isEmpty() || !email.contains("@") || // Basic email validation
                password == null || password.isEmpty()) {
            // Basic validation failed
            System.err.println("Registration Error: Invalid input.");
            return false;
        }

        try {
            return userDAO.registerUser(name.trim(), email.trim().toLowerCase(), password); // Store email lowercase
        } catch (SQLException e) {
            // Check if it's a duplicate email error (this depends on DB constraints or DAO logic)
            if (e.getMessage().toLowerCase().contains("duplicate entry")) { // Basic check
                System.err.println("Registration Error: Email already exists.");
            } else {
                System.err.println("Registration Error: " + e.getMessage());
                e.printStackTrace();
            }
            return false; // Indicate registration failure
        }
    }
}