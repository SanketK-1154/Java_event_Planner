package com.eventplanner;

import com.eventplanner.ui.LoginScreen;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Set Nimbus Look and Feel for a more modern appearance
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            // Optional: If Nimbus not found, fall back to system L&F
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If Nimbus is not available, proceed with the default L&F
            System.err.println("Nimbus L&F not found, using default.");
            // Optionally set System L&F as fallback
            // try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) {}
        }


        // Ensure UI updates happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Show the login screen to start the application
            new LoginScreen().setVisible(true);
        });
    }
}