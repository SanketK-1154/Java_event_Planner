package com.eventplanner.ui;

import com.eventplanner.model.User;
import com.eventplanner.service.AuthService;
import com.eventplanner.ui.admin.AdminDashboard; // <-- ADD THIS IMPORT

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginScreen extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private AuthService authService;

    public LoginScreen() {
        this.authService = new AuthService();

        setTitle("Event Planner - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Email Components ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        emailField = new JTextField(20);
        add(emailField, gbc);

        // --- Password Components ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // --- Action Listeners ---
        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin()); // Allow Enter key login
        registerButton.addActionListener(e -> openRegisterScreen());
    }

    // *** VERIFY THIS METHOD STRUCTURE ***
    private void performLogin() {
        String email = emailField.getText().trim().toLowerCase();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email and Password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Call AuthService to handle login logic
        User user = authService.login(email, password); // 'user' variable is defined here

        // --- Check if login was successful ---
        if (user != null) { // 'user' variable is accessible inside this block
            JOptionPane.showMessageDialog(this, "Login Successful!\nWelcome, " + user.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);

            // Check user role to open correct dashboard
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                // Open Admin Dashboard
                new AdminDashboard(user).setVisible(true); // Use 'user' here
            } else if ("USER".equalsIgnoreCase(user.getRole())) {
                // Open User Dashboard
                new UserDashboard(user).setVisible(true); // Use 'user' here
            } else {
                // Handle unknown roles
                JOptionPane.showMessageDialog(this, "Login successful, but role '" + user.getRole() + "' has no defined dashboard.", "Warning", JOptionPane.WARNING_MESSAGE);
            }

            dispose(); // Close the login window

        } else {
            // Login failed
            JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterScreen() {
        new RegisterScreen().setVisible(true);
        this.dispose();
    }
}