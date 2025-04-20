package com.eventplanner.ui.panels;

import com.eventplanner.model.Event;
import com.eventplanner.model.User;
import com.eventplanner.service.EventService;
import com.eventplanner.ui.UserDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class EventsPanel extends JPanel {

    private User loggedInUser;
    private EventService eventService;
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Event> currentEvents;

    private final String[] columnNames = {"ID", "Title", "Type", "Date & Time", "Category", "Priority", "Location", "Creator"};
    private static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Color TABLE_GRID_COLOR = new Color(220, 220, 220);


    public EventsPanel(User user, EventService eventService) {
        this.loggedInUser = user;
        this.eventService = eventService;

        setLayout(new BorderLayout(10, 10)); // Use BorderLayout
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 245, 250));

        // --- Table Setup (Keep your existing tableModel and eventTable creation) ---
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 3) return Timestamp.class;
                return String.class;
            }
        };
        eventTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        eventTable.setRowSorter(sorter);

        // Table Appearance Tweaks (Keep these)
        eventTable.setFillsViewportHeight(true);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.setRowHeight(25);
        eventTable.setGridColor(TABLE_GRID_COLOR);
        eventTable.setShowVerticalLines(false);
        JTableHeader header = eventTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(new Color(230, 235, 240));
        header.setForeground(new Color(60, 60, 60));
        header.setReorderingAllowed(false);

        // *** IMPORTANT: Create the ScrollPane for the table ***
        JScrollPane scrollPane = new JScrollPane(eventTable);

        // Hide ID column (Keep this)
        eventTable.getColumnModel().getColumn(0).setMinWidth(0);
        eventTable.getColumnModel().getColumn(0).setMaxWidth(0);
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(0);


        // Double-click listener (Keep this)
        eventTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && eventTable.getSelectedRow() != -1) {
                    int modelRow = eventTable.convertRowIndexToModel(eventTable.getSelectedRow());
                    int eventId = (Integer) tableModel.getValueAt(modelRow, 0);
                    editSelectedEvent(eventId);
                }
            }
        });

        // --- Button Panel Setup ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Arrange buttons left-to-right
        buttonPanel.setBackground(getBackground()); // Match panel background

        // Create buttons using the helper method (ensure relative paths are correct)
        JButton addPersonalButton = createStyledButton("Add Personal Event", "icons/plus.png");
        JButton editPersonalButton = createStyledButton("Edit Selected Personal", "icons/edit.png");
        JButton deletePersonalButton = createStyledButton("Delete Selected Personal", "icons/close-button.png");
//        JButton addPersonalButton = new JButton("Add Personal Event");
//        JButton editPersonalButton = new JButton("Edit Selected Personal");
//        JButton deletePersonalButton = new JButton("Delete Selected Personal");// Check filename

        // Add buttons *to the button panel*
        buttonPanel.add(addPersonalButton);
        buttonPanel.add(editPersonalButton);
        buttonPanel.add(deletePersonalButton);

        // *** ADD COMPONENTS TO THE MAIN PANEL'S BORDERLAYOUT ***
        // Add the TABLE (inside its scroll pane) to the CENTER
        add(scrollPane, BorderLayout.CENTER);
        // Add the BUTTON PANEL to the SOUTH
        add(buttonPanel, BorderLayout.SOUTH);
        // DO NOT add the large icon directly here if it displaces the table


        // --- Action Listeners (Keep these) ---
        addPersonalButton.addActionListener(e -> openEventForm(null));
        editPersonalButton.addActionListener(e -> {
            int selectedRow = eventTable.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = eventTable.convertRowIndexToModel(selectedRow);
                int eventId = (Integer) tableModel.getValueAt(modelRow, 0);
                editSelectedEvent(eventId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a personal event to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        deletePersonalButton.addActionListener(e -> deleteSelectedEvent());

        // Initial data load
        refreshEvents();
    }

    private JButton createStyledButton(String text, String relativeIconPath) {
        ImageIcon icon = null;
        final int ICON_SIZE = 16; // Define desired icon size (e.g., 16x16)

        if (relativeIconPath != null && !relativeIconPath.trim().isEmpty()) {
            java.net.URL imgURL = getClass().getResource("/" + relativeIconPath);

            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                // Scale the image
                Image scaledImage = originalIcon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImage); // Use the scaled icon
            } else {
                System.err.println("Icon resource not found in classpath: /" + relativeIconPath);
            }
        }

        JButton button = new JButton(text, icon);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (icon != null) {
            button.setIconTextGap(5); // Gap between icon and text
        }
        return button;
    }



    public void refreshEvents() {
        currentEvents = eventService.getEventsForUser(loggedInUser);
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (Event event : currentEvents) {
                Vector<Object> row = new Vector<>();
                row.add(event.getId());
                row.add(event.getTitle());
                row.add(event.isPersonal() ? "Personal" : "Group");
                row.add(event.getDateTime());
                row.add(event.getCategory());
                row.add(event.getPriority());
                row.add(event.getLocation());
                row.add(event.isPersonal() ? "" : event.getCreatedBy());
                tableModel.addRow(row);
            }
        });
    }

    private Event findEventById(int eventId) {
        if (currentEvents == null) return null;
        return currentEvents.stream().filter(e -> e.getId() == eventId).findFirst().orElse(null);
    }

    private void editSelectedEvent(int eventId) {
        Event eventToEdit = findEventById(eventId);
        if (eventToEdit != null) {
            if (eventToEdit.isPersonal() && eventToEdit.getUserId() == loggedInUser.getId()) {
                openEventForm(eventToEdit);
            } else {
                JOptionPane.showMessageDialog(this, "You can only edit your own personal events.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Could not find the selected event.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = eventTable.convertRowIndexToModel(selectedRow);
        int eventId = (Integer) tableModel.getValueAt(modelRow, 0);
        Event eventToDelete = findEventById(eventId);

        if (eventToDelete != null) {
            if (eventToDelete.isPersonal() && eventToDelete.getUserId() == loggedInUser.getId()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete '" + eventToDelete.getTitle() + "'?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = eventService.deletePersonalEvent(loggedInUser, eventId);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Event deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshEvents();
                        notifyDashboardRefresh();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete event.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "You can only delete your own personal events.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Could not find event to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void openEventForm(Event eventToEdit) {
        // --- Create Form Components ---
        JTextField titleField = new JTextField(20);
        JTextArea descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true); descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        JSpinner dateTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateTimeSpinner, "yyyy-MM-dd HH:mm");
        dateTimeSpinner.setEditor(dateEditor);
        dateTimeSpinner.setValue(eventToEdit != null ? new Date(eventToEdit.getDateTime().getTime()) : new Date());
        JTextField locationField = new JTextField(20);
        String[] categories = {"Academic", "Social", "Meeting", "Workshop", "Personal", "Other"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        String[] priorities = {"High", "Medium", "Low"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);

        if (eventToEdit != null) {
            // Populate fields if editing
            titleField.setText(eventToEdit.getTitle());
            descriptionArea.setText(eventToEdit.getDescription());
            locationField.setText(eventToEdit.getLocation());
            categoryCombo.setSelectedItem(eventToEdit.getCategory() != null ? eventToEdit.getCategory() : "Personal");
            priorityCombo.setSelectedItem(eventToEdit.getPriority() != null ? eventToEdit.getPriority() : "Medium");
        } else {
            // Default values for new personal event
            categoryCombo.setSelectedItem("Personal");
            priorityCombo.setSelectedItem("Medium");
        }

        // --- Layout Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // ... (Add components to formPanel using gbc - code omitted for brevity, assume it's correct) ...
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(3, 3, 3, 3); formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0; formPanel.add(descriptionScrollPane, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0; gbc.anchor = GridBagConstraints.WEST; formPanel.add(new JLabel("Date & Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(dateTimeSpinner, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(locationField, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(categoryCombo, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; formPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(priorityCombo, gbc);


        // --- Show Dialog ---
        String dialogTitle = (eventToEdit == null) ? "Add New Personal Event" : "Edit Personal Event";
        int result = JOptionPane.showConfirmDialog(this, formPanel, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            Date selectedDate = (Date) dateTimeSpinner.getValue();
            if (title.isEmpty() || selectedDate == null) {
                JOptionPane.showMessageDialog(this, "Title and Date/Time are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create or update Event object for PERSONAL event
            Event event = (eventToEdit == null)
                    ? new Event(loggedInUser.getId(), title, descriptionArea.getText().trim(), new Timestamp(selectedDate.getTime()), locationField.getText().trim(), (String)categoryCombo.getSelectedItem(), (String)priorityCombo.getSelectedItem())
                    : eventToEdit; // Start with existing event if editing

            if (eventToEdit != null) { // Update fields if editing
                event.setTitle(title);
                event.setDescription(descriptionArea.getText().trim());
                event.setDateTime(new Timestamp(selectedDate.getTime()));
                event.setLocation(locationField.getText().trim());
                event.setCategory((String)categoryCombo.getSelectedItem());
                event.setPriority((String)priorityCombo.getSelectedItem());
                // Ensure assignedGroupId remains NULL for personal events
                event.setAssignedGroupId(null);
            }
            // Note: Ensure the constructor used above correctly sets userId and leaves assignedGroupId null initially

            // --- Call service to save ---
            String errorMessage = null; // << Variable to store error message string

            if (eventToEdit == null) {
                // Call addEvent with null for group ID (personal event)
                // Store the returned string (null on success, message on error)
                errorMessage = eventService.addEvent(loggedInUser, event, null);
            } else {
                // Call updatePersonalEvent (assuming it returns boolean for now)
                boolean updateSuccess = eventService.updatePersonalEvent(loggedInUser, event);
                if (!updateSuccess) {
                    errorMessage = "Failed to update event."; // Generic update error
                }
            }

            // --- Feedback based on errorMessage ---
            if (errorMessage == null) { // Null means SUCCESS
                JOptionPane.showMessageDialog(this, "Event saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshEvents();
                notifyDashboardRefresh();
            } else {
                // Show the specific error message
                JOptionPane.showMessageDialog(this, "Failed to save event: " + errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void notifyDashboardRefresh() {
        Container parent = SwingUtilities.getAncestorOfClass(UserDashboard.class, this);
        if (parent instanceof UserDashboard) {
            ((UserDashboard) parent).refreshAllPanels();
        }
    }
}