package com.eventplanner.ui;

import com.eventplanner.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterScreen extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backToLoginButton;
    private AuthService authService;

    public RegisterScreen() {
        this.authService = new AuthService();

        setTitle("Event Planner - Register");
        setSize(450, 300); // Adjusted size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name Label
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Full Name:"), gbc);
        // Name Field
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(20);
        add(nameField, gbc);

        // Email Label
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Email:"), gbc);
        // Email Field
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        emailField = new JTextField(20);
        add(emailField, gbc);

        // Password Label
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), gbc);
        // Password Field
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);

        // Confirm Password Label
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Confirm Password:"), gbc);
        // Confirm Password Field
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        confirmPasswordField = new JPasswordField(20);
        add(confirmPasswordField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerButton = new JButton("Register");
        backToLoginButton = new JButton("Back to Login");
        buttonPanel.add(registerButton);
        buttonPanel.add(backToLoginButton);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // --- Action Listeners ---
        registerButton.addActionListener(e -> performRegistration());
        // Add action listener for confirm password field Enter key
        confirmPasswordField.addActionListener(e -> performRegistration());


        backToLoginButton.addActionListener(e -> openLoginScreen());
    }

    private void performRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // --- Input Validation ---
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) { // Very basic check
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.length() < 6) { // Example: Minimum password length
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // --- End Validation ---


        boolean success = authService.register(name, email, password);

        if (success) {
            JOptionPane.showMessageDialog(this, "Registration Successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            openLoginScreen(); // Go back to login screen
        } else {
            // AuthService might have printed specific errors (like email exists)
            // Show a generic message here, or try to get more specific error from service if designed that way
            JOptionPane.showMessageDialog(this, "Registration failed. Please check your input or try a different email.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openLoginScreen() {
        new LoginScreen().setVisible(true);
        this.dispose();
    }

    // Main method for testing this screen directly (optional)
    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> new RegisterScreen().setVisible(true));
    // }
}