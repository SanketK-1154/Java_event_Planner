package com.eventplanner.ui;

import com.eventplanner.model.User;
import com.eventplanner.service.UserService; // We'll create this service

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class EditProfileDialog extends JDialog {

    private UserService userService;
    private User currentUser;
    private User updatedUser; // To store the potentially updated user
    private boolean profileUpdated = false;

    // UI Components
    private JTextField nameField;
    private JLabel emailLabel; // Email usually not editable
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton saveButton;
    private JButton cancelButton;

    public EditProfileDialog(Frame owner, User user) {
        super(owner, "Edit Profile", true); // true makes it modal
        this.currentUser = user;
        this.updatedUser = user; // Start with current user data
        this.userService = new UserService();

        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(currentUser.getName(), 20);
        formPanel.add(nameField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; // Reset

        // Email (Display Only)
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy++;
        emailLabel = new JLabel(currentUser.getEmail());
        emailLabel.setForeground(Color.GRAY); // Indicate non-editable
        formPanel.add(emailLabel, gbc);

        // --- Password Change Section ---
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 5, 0); // Add space before password section
        formPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5); // Reset insets
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("<html><i>Leave blank to keep<br>current password</i></html>"), gbc);

        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1; gbc.gridy++; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        currentPasswordField = new JPasswordField(20);
        formPanel.add(currentPasswordField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.gridy++; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        newPasswordField = new JPasswordField(20);
        formPanel.add(newPasswordField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Confirm New Password:"), gbc);
        gbc.gridx = 1; gbc.gridy++; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;


        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Add Panels to Dialog ---
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> dispose()); // Just close the dialog

    }

    private void saveChanges() {
        String newName = nameField.getText().trim();
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // --- Validation ---
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Password change validation
        boolean changingPassword = !newPassword.isEmpty() || !confirmPassword.isEmpty() || !currentPassword.isEmpty();
        if (changingPassword) {
            if (currentPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your current password to change it.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // Ensure fields are cleared if user decides not to change password mid-entry
            currentPassword = "";
            newPassword = "";
        }


        // --- Call Service to Update ---
        UserService.UpdateResult result = userService.updateProfile(currentUser, newName, currentPassword, newPassword);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.profileUpdated = true;
            this.updatedUser = result.getUpdatedUser(); // Store the updated user object
            dispose(); // Close dialog on success
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile: " + result.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            // Clear password fields on error maybe?
            // currentPasswordField.setText("");
            // newPasswordField.setText("");
            // confirmPasswordField.setText("");
        }
    }

    // Method for the parent window (DashboardPanel) to check if changes were made
    public boolean isProfileUpdated() {
        return profileUpdated;
    }

    // Method to get the potentially updated user object
    public User getUpdatedUser() {
        return updatedUser;
    }
}