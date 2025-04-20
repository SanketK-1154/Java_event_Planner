package com.eventplanner.ui.panels;

import com.eventplanner.model.Event;
import com.eventplanner.model.User;
import com.eventplanner.service.EventService;
import com.eventplanner.ui.EditProfileDialog;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;


public class DashboardPanel extends JPanel {

    private User loggedInUser;
    private EventService eventService;

    // Profile section components
    private JLabel nameLabel;
    private JLabel emailLabel;
    private JLabel roleLabel;
    private JLabel memberSinceLabel;
    private JButton editProfileButton; // Placeholder button

    // Stats section components
    private JLabel eventCountLabel;
    private JLabel upcomingEventCountLabel;

    // Upcoming events components
    private JTextArea upcomingEventsArea;

    // Date formatter
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy"); // e.g., April 15, 2025


    public DashboardPanel(User user, EventService eventService) {
        this.loggedInUser = user;
        this.eventService = eventService;

        setLayout(new GridBagLayout()); // Use GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 245, 250)); // Match theme

        // --- Profile Section ---
        JPanel profilePanel = createProfilePanel();
        profilePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Your Profile", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(50, 50, 100))); // Titled border
        profilePanel.setBackground(getBackground());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0.4; // Give profile panel some weight
        gbc.weighty = 0; // Don't let profile panel grow vertically much initially
        gbc.insets = new Insets(0, 0, 10, 10); // Bottom and right margin
        add(profilePanel, gbc);


        // --- Stats Section ---
        JPanel statsPanel = createStatsPanel();
        statsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Statistics", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(50, 50, 100)));
        statsPanel.setBackground(getBackground());

        gbc.gridx = 1; // Next column
        gbc.gridy = 0;
        gbc.weightx = 0.6; // Give stats panel more weight
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(0, 0, 10, 0); // Bottom margin only
        add(statsPanel, gbc);


        // --- Upcoming Events Section ---
        JPanel upcomingEventsPanel = createUpcomingEventsPanel();
        upcomingEventsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Next 5 Upcoming Events", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(50, 50, 100)));
        upcomingEventsPanel.setBackground(getBackground());

        gbc.gridx = 0;
        gbc.gridy = 1; // Next row
        gbc.gridwidth = 2; // Span both columns
        gbc.fill = GridBagConstraints.BOTH; // Fill remaining space
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Let upcoming events grow vertically
        gbc.insets = new Insets(0, 0, 0, 0); // No margin
        add(upcomingEventsPanel, gbc);


        // Initial data load
        refreshData();
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(getBackground()); // Match background
        GridBagConstraints gbc = new GridBagConstraints();

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 13);

        // --- Row 0: Name ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(4, 4, 4, 10);
        panel.add(new JLabel("Name:") {{ setFont(labelFont); }}, gbc); // Title label

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 4, 4, 4);
        // *** Simplified Creation ***
        nameLabel = new JLabel(" "); // Initialize with a space to ensure component has some size
        nameLabel.setFont(valueFont);
        panel.add(nameLabel, gbc); // Value label
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; // Reset for next row

        // --- Row 1: Email ---
        gbc.gridx = 0; gbc.gridy++; gbc.insets = new Insets(4, 4, 4, 10);
        panel.add(new JLabel("Email:") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 4, 4, 4);
        // *** Simplified Creation ***
        emailLabel = new JLabel(" ");
        emailLabel.setFont(valueFont);
        panel.add(emailLabel, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;

        // --- Row 2: Role ---
        gbc.gridx = 0; gbc.gridy++; gbc.insets = new Insets(4, 4, 4, 10);
        panel.add(new JLabel("Role:") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 4, 4, 4);
        // *** Simplified Creation ***
        roleLabel = new JLabel(" ");
        roleLabel.setFont(valueFont);
        panel.add(roleLabel, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;

        // --- Row 3: Member Since ---
        gbc.gridx = 0; gbc.gridy++; gbc.insets = new Insets(4, 4, 4, 10);
        panel.add(new JLabel("Member Since:") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 4, 4, 4);
        // *** Simplified Creation ***
        memberSinceLabel = new JLabel(" ");
        memberSinceLabel.setFont(valueFont);
        panel.add(memberSinceLabel, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;

        // --- Row 4: Edit Button ---
        editProfileButton = new JButton("Edit Profile");
        editProfileButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        editProfileButton.setToolTipText("Click to edit your profile details");
        editProfileButton.addActionListener(e -> openEditProfileDialog());

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 4, 4, 4);
        panel.add(editProfileButton, gbc);

        return panel;
    }

    // *** ADD THIS METHOD to DashboardPanel.java ***
    // This method will be called by the Edit Profile button's action listener
    private void openEditProfileDialog() {
        // Create and display the dialog, passing the current user and the parent frame
        EditProfileDialog dialog = new EditProfileDialog((JFrame) SwingUtilities.getWindowAncestor(this), loggedInUser);
        dialog.setVisible(true);

        // After the dialog is closed, check if the profile was updated
        if (dialog.isProfileUpdated()) {
            // Re-fetch or update the loggedInUser object (important if name changed)
            // For simplicity now, we just refresh the display. A better approach
            // might involve updating the loggedInUser object reference itself.
            System.out.println("Profile potentially updated, refreshing dashboard display.");
            this.loggedInUser = dialog.getUpdatedUser(); // Get potentially updated user from dialog
            refreshData(); // Refresh the dashboard labels
        }
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Vertical layout

        Font statsFont = new Font("Segoe UI", Font.PLAIN, 14);

        eventCountLabel = new JLabel("Total Events: Loading...");
        upcomingEventCountLabel = new JLabel("Upcoming Events: Loading...");
        eventCountLabel.setFont(statsFont);
        upcomingEventCountLabel.setFont(statsFont);
        eventCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        upcomingEventCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(eventCountLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        panel.add(upcomingEventCountLabel);
        // Add more stats JLabels here if needed

        return panel;
    }

    private JPanel createUpcomingEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        upcomingEventsArea = new JTextArea(5, 30); // Rows, Columns
        upcomingEventsArea.setEditable(false);
        upcomingEventsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        upcomingEventsArea.setLineWrap(true);
        upcomingEventsArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(upcomingEventsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }


    // --- WITHIN DashboardPanel.java ---

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("\nDEBUG: DashboardPanel.refreshData() running on EDT.");
            if (loggedInUser == null) {
                System.err.println("DEBUG: loggedInUser is NULL in refreshData!");
                nameLabel.setText("Error"); emailLabel.setText("Error"); roleLabel.setText("Error"); memberSinceLabel.setText("Error");
                eventCountLabel.setText("Error"); upcomingEventCountLabel.setText("Error");
                return;
            }

            // --- Profile Data Update ---
            try { // Wrap profile updates just in case
                System.out.println("DEBUG: Setting Name Label..."); nameLabel.setText(loggedInUser.getName());
                System.out.println("DEBUG: Setting Email Label..."); emailLabel.setText(loggedInUser.getEmail());
                System.out.println("DEBUG: Setting Role Label..."); roleLabel.setText(loggedInUser.getRole());

                System.out.println("DEBUG: Processing Member Since. Reg Date: " + loggedInUser.getRegistrationDate());
                if (loggedInUser.getRegistrationDate() != null) {
                    if (DATE_FORMAT == null) { // Check if formatter is null
                        System.err.println("DEBUG: DATE_FORMAT is NULL!");
                        memberSinceLabel.setText("Error: Formatter");
                    } else {
                        try {
                            String formattedDate = DATE_FORMAT.format(new Date(loggedInUser.getRegistrationDate().getTime()));
                            System.out.println("DEBUG: Date formatted successfully to: " + formattedDate);
                            System.out.println("DEBUG: Attempting to set Member Since Label text...");
                            memberSinceLabel.setText(formattedDate);
                            System.out.println("DEBUG: Finished setting Member Since Label text."); // Check if this prints
                        } catch (Exception e) {
                            System.err.println("DEBUG: Exception during date formatting/setting: " + e.getMessage());
                            e.printStackTrace(); // Print full trace
                            memberSinceLabel.setText("Error: Format");
                        }
                    }
                } else {
                    System.out.println("DEBUG: Registration Date is null. Setting Member Since to N/A");
                    memberSinceLabel.setText("N/A");
                    System.out.println("DEBUG: Finished setting Member Since to N/A.");
                }
                System.out.println("DEBUG: Finished profile section update attempts.");
            } catch (Exception e) {
                System.err.println("DEBUG: Unexpected Exception during profile label updates:");
                e.printStackTrace();
            }


            // --- Stats Data Calculation ---
            List<Event> allEvents = null;
            List<Event> upcomingEvents = null; // Initialize as null first
            int allEventsCount = 0;
            int upcomingEventsCount = 0;

            try {
                System.out.println("DEBUG: Fetching allEvents for stats...");
                allEvents = eventService.getEventsForUser(loggedInUser);
                if (allEvents == null) { allEvents = new ArrayList<>(); }
                allEventsCount = allEvents.size();
                System.out.println("DEBUG: Fetched allEvents count: " + allEventsCount);

                long now = System.currentTimeMillis();
                System.out.println("DEBUG: Filtering for upcoming events... (Now=" + now + ")");
                upcomingEvents = allEvents.stream()
                        .filter(e -> e.getDateTime() != null && e.getDateTime().getTime() > now)
                        .sorted((e1, e2) -> e1.getDateTime().compareTo(e2.getDateTime()))
                        .collect(Collectors.toList());
                upcomingEventsCount = upcomingEvents.size();
                System.out.println("DEBUG: Filtering complete. Upcoming events count: " + upcomingEventsCount);

            } catch (Exception e) {
                System.err.println("DEBUG: Exception during event fetching/filtering for stats:");
                e.printStackTrace();
                upcomingEvents = new ArrayList<>(); // Ensure list is not null on error
            }

            // --- Update Stats Labels ---
            try {
                System.out.println("DEBUG: Setting stats labels...");
                eventCountLabel.setText("Total Events: " + allEventsCount);
                upcomingEventCountLabel.setText("Upcoming Events: " + upcomingEventsCount);
                System.out.println("DEBUG: Finished setting stats labels.");
            } catch(Exception e) { System.err.println("DEBUG: Exception setting stats labels:"); e.printStackTrace(); }

            // --- Upcoming Events List Update ---
            System.out.println("DEBUG: Updating upcoming events text area...");
            StringBuilder upcomingText = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            // *** Add check right before the loop ***
            if (upcomingEvents != null && !upcomingEvents.isEmpty()) {
                System.out.println("DEBUG: UpcomingEvents list is NOT null/empty. Size: " + upcomingEvents.size() + ". Entering loop...");
                int count = 0;
                for (Event event : upcomingEvents) {
                    System.out.println("DEBUG: Loop iteration " + (count+1) + ". Adding event: " + event.getTitle()); // Check if loop body is reached
                    if (count >= 5) break;
                    String type = event.isPersonal() ? "Personal" : "Group";
                    String creator = event.isPersonal() ? "" : " (by " + event.getCreatedBy() + ")";
                    upcomingText.append(String.format("%-20s [%s]%s - %s\n",
                            event.getTitle(), type, creator,
                            sdf.format(new Timestamp(event.getDateTime().getTime()))));
                    count++;
                }
                System.out.println("DEBUG: Finished loop for upcoming events. Items added: " + count);
            } else {
                System.out.println("DEBUG: UpcomingEvents list IS NULL or EMPTY before loop. Size: " + (upcomingEvents == null ? "null" : upcomingEvents.size()));
            }
            // **************************************

            if (upcomingText.length() == 0) {
                upcomingEventsArea.setText("No upcoming events found.");
            } else {
                upcomingEventsArea.setText(upcomingText.toString());
            }
            upcomingEventsArea.setCaretPosition(0);
            System.out.println("DEBUG: Finished updating upcoming events text area.");
            System.out.println("DEBUG: DashboardPanel.refreshData() finished.");

        }); // End SwingUtilities.invokeLater
    }

}