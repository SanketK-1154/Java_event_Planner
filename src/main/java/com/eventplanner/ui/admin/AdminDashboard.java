package com.eventplanner.ui.admin;

import com.eventplanner.model.User;
import com.eventplanner.service.EventService; // May need later for events
import com.eventplanner.service.GroupService;
import com.eventplanner.ui.LoginScreen;
import com.eventplanner.ui.admin.panels.AdminGroupPanel; // We will create this next
import com.eventplanner.ui.admin.panels.AdminEventPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

// Very similar structure to UserDashboard, but for Admin functionalities
public class AdminDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private User loggedInAdmin; // Admin user object
    // Services will be needed by panels
    private GroupService groupService;
    private EventService eventService; // Keep for potential future event panel

    // Panels
    private AdminGroupPanel adminGroupPanel;
    private AdminEventPanel adminEventPanel;
    // Add other admin panels here later (e.g., AdminEventPanel)

    // Styling Constants (can reuse or define admin-specific ones)
    private static final Color PRIMARY_COLOR = new Color(180, 70, 70); // Example: Admin Red theme
    private static final Color SECONDARY_COLOR = new Color(250, 240, 240); // Light background
    private static final Color SIDEBAR_COLOR = new Color(130, 60, 60); // Darker Red for Sidebar
    private static final Color BUTTON_HOVER_COLOR = new Color(200, 100, 100); // Lighter red for hover
    private static final Color FONT_COLOR_LIGHT = Color.WHITE;
    private static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font SIDEBAR_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private Map<String, JButton> sidebarButtons = new HashMap<>();


    public AdminDashboard(User adminUser) {
        if (!"ADMIN".equalsIgnoreCase(adminUser.getRole())) {
            // Basic security check, though login screen should handle this
            throw new IllegalArgumentException("User provided to AdminDashboard is not an Admin!");
        }
        this.loggedInAdmin = adminUser;
        this.groupService = new GroupService();
        this.eventService = new EventService();

        setTitle("Admin Dashboard - Welcome, " + adminUser.getName());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // --- Header ---
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // --- Sidebar ---
        JPanel sidebarPanel = createSidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);

        // --- Content Area ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(SECONDARY_COLOR);

        // Initialize and add admin panels
        adminGroupPanel = new AdminGroupPanel(loggedInAdmin, groupService);
        // Add other admin panels later
        adminEventPanel = new AdminEventPanel(loggedInAdmin, eventService, groupService);

        contentPanel.add(adminGroupPanel, "Groups"); // Add Group panel
        contentPanel.add(adminEventPanel, "Events"); // Add Event panel later

        add(contentPanel, BorderLayout.CENTER);

        // Show Groups panel by default for Admin
        cardLayout.show(contentPanel, "Groups");
        // refreshAdminPanels(); // Create method to refresh data if needed
    }

    // --- UI Creation Methods (Similar to UserDashboard, adjust styling/icons) ---

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR); // Admin theme color
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel welcomeLabel = new JLabel("Admin Dashboard - Welcome, " + loggedInAdmin.getName() + "!");
        welcomeLabel.setFont(HEADER_FONT);
        welcomeLabel.setForeground(FONT_COLOR_LIGHT);

        ImageIcon logoutIcon = createImageIcon("icons/logout_icon.png", "Logout"); // Reuse relative path
        JButton logoutButton = new JButton("Logout", logoutIcon);
        styleHeaderButton(logoutButton);

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginScreen().setVisible(true);
                dispose();
            }
        });

        headerPanel.add(welcomeLabel, BorderLayout.CENTER);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        sidebar.setPreferredSize(new Dimension(180, 0));

        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // Admin Menu Items & Icons (Placeholders)
        Map<String, String> menuIcons = Map.of(
                "Groups", "icons/group.png",// Existing icon
                    "Events", "icons/events.png"
                // e.g., "Users", "icons/users_icon.png"
        );

        String[] menuItems = {"Groups", "Events"}; // *** Ensure "Events" is in this array ***
        sidebarButtons.clear();

        for (String item : menuItems) { // This loop should now run for "Groups" and "Events"
            ImageIcon icon = createImageIcon(menuIcons.get(item), item + " icon");
            JButton btn = new JButton(item, icon);
            styleSidebarButton(btn);
            btn.addActionListener(e -> switchPanel(item));
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebarButtons.put(item, btn);
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    // --- Styling and Helper Methods (Copy/Adapt from UserDashboard) ---

    private ImageIcon createImageIcon(String relativePath, String description) {
        // Use the same robust method with scaling from UserDashboard
        ImageIcon icon = null;
        final int ICON_SIZE = 16;
        if (relativePath == null || relativePath.trim().isEmpty()) return null;
        java.net.URL imgURL = getClass().getResource("/" + relativePath);
        if (imgURL != null) {
            ImageIcon originalIcon = new ImageIcon(imgURL, description);
            Image scaledImage = originalIcon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImage, description);
        } else {
            System.err.println("Admin Icon resource not found: /" + relativePath);
        }
        return icon;
    }

    private void styleHeaderButton(JButton button) {
        // Adapt styling from UserDashboard
        button.setFont(DEFAULT_FONT);
        button.setForeground(PRIMARY_COLOR);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(230, 230, 230)); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(Color.WHITE); }
        });
    }

    private void styleSidebarButton(JButton button) {
        // Adapt styling from UserDashboard
        button.setFont(SIDEBAR_FONT);
        button.setForeground(FONT_COLOR_LIGHT);
        button.setBackground(SIDEBAR_COLOR);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addMouseListener(new MouseAdapter() {
            Color originalBackground = button.getBackground();
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(BUTTON_HOVER_COLOR); button.setOpaque(true); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(originalBackground); button.setOpaque(false); }
        });
    }

    private void switchPanel(String panelName) {
        // First, show the requested panel
        cardLayout.show(contentPanel, panelName);
        System.out.println("Admin switched to: " + panelName);

        // --- ADDED: Refresh the data for the panel that was just shown ---
        refreshAdminPanelData(panelName);
        // ---------------------------------------------------------------
    }

    // Helper method to refresh the currently visible admin panel
    private void refreshAdminPanelData(String panelName) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("DEBUG: Refreshing data for admin panel: " + panelName);
            switch (panelName) {
                case "Groups":
                    if (adminGroupPanel != null) adminGroupPanel.refreshData(); // Assuming refreshData exists
                    break;
                case "Events":
                    if (adminEventPanel != null) adminEventPanel.refreshData(); // Assuming refreshData exists
                    break;
                // Add cases for other admin panels (e.g., Announcements) later
            }
            System.out.println("DEBUG: Finished refreshing data for admin panel: " + panelName);
        });
    }

    // Make sure the refresh methods exist on the admin panels
    // (e.g., AdminGroupPanel might have refreshGroupList, AdminEventPanel might have refreshEventsList)
    // Adapt the calls above if the method names are different. We called the refresh method 'refreshData'
    // in the AdminGroupPanel code provided earlier. Let's assume AdminEventPanel also has refreshData.

    // Optional: Method to refresh all panels if needed
    public void refreshAdminPanels() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("DEBUG: Refreshing ALL admin panels...");
            if (adminGroupPanel != null) adminGroupPanel.refreshData();
            if (adminEventPanel != null) adminEventPanel.refreshData();
            System.out.println("DEBUG: Finished refreshing ALL admin panels.");
        });
    }


}