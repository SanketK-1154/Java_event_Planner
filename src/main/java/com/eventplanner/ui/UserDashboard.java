package com.eventplanner.ui;

import com.eventplanner.model.User;
import com.eventplanner.service.EventService;
import com.eventplanner.service.GroupService;
import com.eventplanner.ui.panels.CalendarPanel;
import com.eventplanner.ui.panels.DashboardPanel;
import com.eventplanner.ui.panels.EventsPanel;
import com.eventplanner.ui.panels.GroupsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class UserDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private User loggedInUser;
    private EventService eventService;
    private GroupService groupService;

    // Panels
    private DashboardPanel dashboardPanel;
    private EventsPanel eventsPanel;
    private CalendarPanel calendarPanel;
    private GroupsPanel groupsPanel;

    // Styling Constants (Example Palette)
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Color SECONDARY_COLOR = new Color(227, 229, 237); // Very Light Blue/Gray
    private static final Color SIDEBAR_COLOR = new Color(60, 90, 130); // Darker Blue for Sidebar
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 160, 210); // Lighter blue for hover
    private static final Color FONT_COLOR_LIGHT = Color.WHITE;
    private static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 14); // Or Arial
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font SIDEBAR_FONT = new Font("Segoe UI", Font.BOLD, 14);

    // Store sidebar buttons for hover effects
    private Map<String, JButton> sidebarButtons = new HashMap<>();


    public UserDashboard(User user) {
        this.loggedInUser = user;
        this.eventService = new EventService();
        this.groupService = new GroupService();

        setTitle("Event Planner - Welcome, " + user.getName());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0)); // No gaps for tighter integration
        // Set default font for components where possible (doesn't affect all L&Fs)
        UIManager.put("Label.font", DEFAULT_FONT);
        UIManager.put("Button.font", DEFAULT_FONT);
        UIManager.put("TextField.font", DEFAULT_FONT);
        UIManager.put("Table.font", DEFAULT_FONT);
        // ... etc for other components


        // --- Header ---
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // --- Sidebar ---
        JPanel sidebarPanel = createSidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);

        // --- Content Area ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(SECONDARY_COLOR); // Background for content area

        // Initialize and add panels
        dashboardPanel = new DashboardPanel(loggedInUser, eventService);
        eventsPanel = new EventsPanel(loggedInUser, eventService);
        calendarPanel = new CalendarPanel(loggedInUser, eventService);
        groupsPanel = new GroupsPanel(loggedInUser, groupService);

        // Set background for panels (optional, can be done within panel classes too)
        dashboardPanel.setBackground(SECONDARY_COLOR);
        eventsPanel.setBackground(SECONDARY_COLOR);
        calendarPanel.setBackground(SECONDARY_COLOR);
        groupsPanel.setBackground(SECONDARY_COLOR);

        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(eventsPanel, "Events");
        contentPanel.add(calendarPanel, "Calendar");
        contentPanel.add(groupsPanel, "Groups");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "Dashboard");
        refreshAllPanels();
    }

    private JPanel createHeaderPanel() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel welcomeLabel = new JLabel("Welcome, " + loggedInUser.getName() + "!");
        welcomeLabel.setFont(HEADER_FONT);
        welcomeLabel.setForeground(FONT_COLOR_LIGHT);


        // *** Use RELATIVE path for Logout Icon ***
        ImageIcon logoutIcon = createImageIcon("icons/switch.png", "Logout"); // Relative path
        JButton logoutButton = new JButton("Logout", logoutIcon);
        styleHeaderButton(logoutButton);

        logoutButton.addActionListener(e -> {
            System.out.println("Logout button clicked."); // Debug print
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);

            System.out.println("Confirmation result: " + confirm + " (YES=" + JOptionPane.YES_OPTION + ")"); // Debug print

            if (confirm == JOptionPane.YES_OPTION) {
                System.out.println("Logout confirmed. Creating LoginScreen..."); // Debug print
                try {
                    new LoginScreen().setVisible(true); // Show login screen
                    System.out.println("LoginScreen created and set visible."); // Debug print

                    System.out.println("Disposing UserDashboard..."); // Debug print
                    dispose(); // Close this dashboard
                    System.out.println("UserDashboard disposed."); // Debug print
                } catch (Exception ex) {
                    System.err.println("Error during logout process:"); // Print any unexpected error
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error during logout: " + ex.getMessage(), "Logout Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("Logout cancelled."); // Debug print
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

        // *** Use RELATIVE paths for icons ***
        Map<String, String> menuIcons = Map.of(
                "Dashboard", "icons/dashboard.png",
                "Events", "icons/events.png",
                "Calendar", "icons/calendar.png",
                "Groups", "icons/group.png"
        );

        String[] menuItems = {"Dashboard", "Events", "Calendar", "Groups"};
        for (String item : menuItems) {
            ImageIcon icon = createImageIcon(menuIcons.get(item), item + " icon"); // Relative path used here
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

    // *** CORRECTED Helper to create ImageIcons using relative paths ***
    private ImageIcon createImageIcon(String relativePath, String description) {
        ImageIcon icon = null;
        final int ICON_SIZE = 16; // Define consistent icon size (e.g., 16x16 or 20x20)

        if (relativePath == null || relativePath.trim().isEmpty()) {
            System.err.println("Icon path is null or empty for: " + description);
            return null;
        }

        // Use getClass().getResource() and prepend "/" to search from classpath root
        java.net.URL imgURL = getClass().getResource("/" + relativePath);

        if (imgURL != null) {
            ImageIcon originalIcon = new ImageIcon(imgURL, description);
            // --- Add Scaling Logic ---
            Image scaledImage = originalIcon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImage, description); // Use the scaled icon
            // -------------------------
        } else {
            System.err.println("Icon resource not found in classpath: /" + relativePath);
            // Return null if the resource wasn't found
        }
        return icon; // Return the scaled icon or null
    }

    // Helper to style header buttons
    private void styleHeaderButton(JButton button) {
        button.setFont(DEFAULT_FONT);
        button.setForeground(PRIMARY_COLOR); // Text color matching primary background
        button.setBackground(Color.WHITE); // Button background
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setOpaque(true); // Needed for background color

        // Simple Hover Effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(230, 230, 230)); // Slightly darker on hover
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE); // Back to original
            }
        });
    }


    // Helper method to style sidebar buttons
    private void styleSidebarButton(JButton button) {
        button.setFont(SIDEBAR_FONT);
        button.setForeground(FONT_COLOR_LIGHT);
        button.setBackground(SIDEBAR_COLOR); // Match sidebar background initially
        button.setOpaque(false); // Make background transparent initially
        button.setContentAreaFilled(false); // Don't paint default button background
        button.setBorderPainted(false); // No border
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT); // Align text and icon left
        button.setIconTextGap(10); // Gap between icon and text
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // Max height, fill width
        button.setAlignmentX(Component.LEFT_ALIGNMENT); // Align button itself to left

        // Hover effect for sidebar buttons
        button.addMouseListener(new MouseAdapter() {
            Color originalBackground = button.getBackground();
            @Override
            public void mouseEntered(MouseEvent e) {
                // Slightly lighten the sidebar color for hover
                button.setBackground(BUTTON_HOVER_COLOR);
                button.setOpaque(true); // Make background visible on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBackground);
                button.setOpaque(false); // Hide background when not hovering
            }
        });
    }


    private void switchPanel(String panelName) {
        // First, show the requested panel using CardLayout
        cardLayout.show(contentPanel, panelName);
        System.out.println("User switched to: " + panelName); // Keep for debugging

        // --- ADDED: Refresh the data for the panel that was just shown ---
        refreshPanelData(panelName);
        // ---------------------------------------------------------------
    }

    // Helper method to refresh the currently visible panel
    // (Ensures refresh happens after the panel is potentially visible)
    private void refreshPanelData(String panelName) {
        // Use invokeLater to be safe with Swing threading, although might not be strictly necessary
        // if called directly from the button's ActionListener (which runs on EDT).
        SwingUtilities.invokeLater(() -> {
            System.out.println("DEBUG: Refreshing data for panel: " + panelName);
            switch (panelName) {
                case "Dashboard":
                    if (dashboardPanel != null) dashboardPanel.refreshData();
                    break;
                case "Events":
                    if (eventsPanel != null) eventsPanel.refreshEvents();
                    break;
                case "Calendar":
                    if (calendarPanel != null) calendarPanel.refreshCalendarView();
                    break;
                case "Groups":
                    if (groupsPanel != null) groupsPanel.refreshGroupList();
                    break;
                // Add cases for any other future panels
            }
            System.out.println("DEBUG: Finished refreshing data for panel: " + panelName);
        });
    }

    // Make sure the refreshAllPanels method also exists if called elsewhere (like from child panels)
    public void refreshAllPanels() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("DEBUG: Refreshing ALL user panels...");
            if (dashboardPanel != null) dashboardPanel.refreshData();
            if (eventsPanel != null) eventsPanel.refreshEvents();
            if (calendarPanel != null) calendarPanel.refreshCalendarView();
            if (groupsPanel != null) groupsPanel.refreshGroupList();
            System.out.println("DEBUG: Finished refreshing ALL user panels.");
        });
    }
}