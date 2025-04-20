package com.eventplanner.ui.admin.panels;

import com.eventplanner.model.Event;
import com.eventplanner.model.Group;
import com.eventplanner.model.User;
import com.eventplanner.service.EventService;
import com.eventplanner.service.GroupService;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.text.SimpleDateFormat;
import java.util.Map;


public class AdminEventPanel extends JPanel {

    private User loggedInAdmin;
    private EventService eventService;
    private GroupService groupService;

    // UI Components for Form
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner dateTimeSpinner;
    private JTextField locationField;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> priorityCombo;
    private JComboBox<GroupWrapper> groupSelectorCombo; // Combo box for groups
    private JButton createEventButton;
    private JButton deleteEventButton;
    // UI Components for Table
    private JTable adminEventsTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private List<Event> currentAdminEvents;

    // Table Columns
    private final String[] columnNames = {"ID", "Title", "Assigned Group", "Date & Time", "Category", "Priority", "Location"};
    private static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Color TABLE_GRID_COLOR = new Color(220, 220, 220);
    private static final Color PANEL_BACKGROUND = new Color(250, 240, 240); // Match admin theme

    public AdminEventPanel(User adminUser, EventService eventService, GroupService groupService) {
        this.loggedInAdmin = adminUser;
        this.eventService = eventService;
        this.groupService = groupService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(PANEL_BACKGROUND);

        // --- Top Panel: Event Creation Form ---
        JPanel formPanel = createEventFormPanel();
        add(formPanel, BorderLayout.NORTH);

        // --- Center Panel: List of Events Created by Admin ---
        JPanel tablePanel = createEventsTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Load initial data
        populateGroupSelector();
        refreshEventsList();
    }


    // --- Panel Creation Methods ---

    private JPanel createEventFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Event for Group"));
        formPanel.setBackground(getBackground());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Title
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        titleField = new JTextField(30);
        formPanel.add(titleField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; // Reset

        // Row 1: Description
        gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.NORTHWEST; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(3, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0; gbc.anchor = GridBagConstraints.WEST; // Reset

        // Row 2: Date/Time & Location
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Date & Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        dateTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateTimeSpinner, "yyyy-MM-dd HH:mm");
        dateTimeSpinner.setEditor(dateEditor);
        dateTimeSpinner.setValue(new Date()); // Default to now
        formPanel.add(dateTimeSpinner, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; // Reset

        gbc.gridx = 2; formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        locationField = new JTextField(15);
        formPanel.add(locationField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; // Reset

        // Row 3: Category & Priority
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        String[] categories = {"Academic", "Social", "Meeting", "Workshop", "Deadline", "Other"};
        categoryCombo = new JComboBox<>(categories);
        formPanel.add(categoryCombo, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        gbc.gridx = 2; formPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        String[] priorities = {"Medium", "High", "Low"};
        priorityCombo = new JComboBox<>(priorities);
        formPanel.add(priorityCombo, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        // Row 4: Group Selector & Create Button
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Assign to Group:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        groupSelectorCombo = new JComboBox<>(); // Populate later
        formPanel.add(groupSelectorCombo, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; // Reset

        gbc.gridx = 3; gbc.anchor = GridBagConstraints.EAST;
        createEventButton = new JButton("Create Event");
        // Add icon later if desired
        createEventButton.addActionListener(e -> createGroupEvent());
        formPanel.add(createEventButton, gbc);

        return formPanel;
    }

    private JPanel createEventsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Events Created By You"));
        panel.setBackground(getBackground());

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 3) return Timestamp.class;
                return String.class;
            }
        };
        adminEventsTable = new JTable(tableModel);
        // Apply styling similar to other tables
        adminEventsTable.setRowHeight(25);
        adminEventsTable.setGridColor(TABLE_GRID_COLOR);
        adminEventsTable.setShowVerticalLines(false);
        adminEventsTable.setFillsViewportHeight(true);
        adminEventsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = adminEventsTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(new Color(240, 230, 230));
        header.setForeground(new Color(60, 60, 60));
        header.setReorderingAllowed(false);

        // Hide ID column
        adminEventsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        adminEventsTable.getColumnModel().getColumn(0).setMinWidth(0);
        adminEventsTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Add sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        adminEventsTable.setRowSorter(sorter);


        scrollPane = new JScrollPane(adminEventsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // TODO: Add Edit/Delete buttons and listeners later
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(getBackground());
        // JButton editButton = new JButton("Edit Selected");
        // JButton deleteButton = new JButton("Delete Selected");
        deleteEventButton = new JButton("Delete Selected Event");
        deleteEventButton.addActionListener(e -> deleteSelectedAdminEvent());
        // buttonPanel.add(editButton);
        // buttonPanel.add(deleteButton);
        buttonPanel.add(deleteEventButton); // Add the created button to the panel
        panel.add(buttonPanel, BorderLayout.SOUTH);


        return panel;
    }

    private void deleteSelectedAdminEvent() {
        int selectedRow = adminEventsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get model index in case of sorting
        int modelRow = adminEventsTable.convertRowIndexToModel(selectedRow);
        int eventId = (Integer) tableModel.getValueAt(modelRow, 0); // Hidden ID column
        String eventTitle = (String) tableModel.getValueAt(modelRow, 1); // Get title for confirmation

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the event '" + eventTitle + "'?\nThis action cannot be undone.",
                "Confirm Event Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = eventService.deleteAdminEvent(loggedInAdmin, eventId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Event '" + eventTitle + "' deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshEventsList(); // Refresh the table in this panel
                // Optionally notify the main dashboard to refresh other panels if needed
                // notifyDashboardRefresh();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the selected event.\nIt might have already been deleted or you may not have permission.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // --- Data Handling and Actions ---

    private void populateGroupSelector() {
        System.out.println("DEBUG: Populating group selector for admin ID: " + loggedInAdmin.getId()); // Debug
        groupSelectorCombo.removeAllItems(); // Clear existing items
        groupSelectorCombo.setEnabled(false); // Disable initially

        List<Group> adminGroups = groupService.getGroupsByAdmin(loggedInAdmin);
        System.out.println("DEBUG: Fetched " + (adminGroups != null ? adminGroups.size() : "null") + " groups for admin."); // Debug

        if (adminGroups != null && !adminGroups.isEmpty()) {
            // Add a placeholder/instruction item first
            groupSelectorCombo.addItem(new GroupWrapper(null, "-- Select Group --"));
            for (Group group : adminGroups) {
                System.out.println("DEBUG: Adding group to combo: " + group.getName() + " (ID: " + group.getId() + ")"); // Debug
                groupSelectorCombo.addItem(new GroupWrapper(group));
            }
            groupSelectorCombo.setEnabled(true); // *** Re-enable the combo box ***
            System.out.println("DEBUG: Group selector populated and enabled."); // Debug
        } else {
            // Keep it disabled if no groups found
            groupSelectorCombo.addItem(new GroupWrapper(null, "No groups created yet"));
            System.out.println("DEBUG: No groups found, selector disabled."); // Debug
        }
    }

    private void createGroupEvent() {
        // --- (Keep the existing code to get data from form fields) ---
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        Date selectedDate = (Date) dateTimeSpinner.getValue();
        String location = locationField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String priority = (String) priorityCombo.getSelectedItem();
        GroupWrapper selectedGroupWrapper = (GroupWrapper) groupSelectorCombo.getSelectedItem();

        // --- (Keep existing basic validation) ---
        if (title.isEmpty() || selectedDate == null) { JOptionPane.showMessageDialog(this, "Title and Date/Time are required.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }
        if (selectedGroupWrapper == null || selectedGroupWrapper.getGroup() == null) { JOptionPane.showMessageDialog(this, "Please select a group.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }

        Integer groupId = selectedGroupWrapper.getGroup().getId();
        Timestamp dateTime = new Timestamp(selectedDate.getTime());

        // --- Create Event Object (Ensure constructor sets creator ID) ---
        Event newEvent = new Event(
                loggedInAdmin.getId(), title, description, dateTime, location, category, priority
        );
        // Group ID will be set by the service now

        // --- Call Service to Add Event - Capture return message ---
        String resultMessage = eventService.addEvent(loggedInAdmin, newEvent, groupId);

        // --- Feedback and Refresh based on result ---
        if (resultMessage == null) { // Null means success
            JOptionPane.showMessageDialog(this, "Event '" + title + "' created for group '" + selectedGroupWrapper.getGroup().getName() + "'.", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Clear form fields on success
            titleField.setText("");
            descriptionArea.setText("");
            locationField.setText("");
            // Reset combo boxes maybe?
            // categoryCombo.setSelectedIndex(0);
            // priorityCombo.setSelectedIndex(0);
            // groupSelectorCombo.setSelectedIndex(0); // Or default to "Select Group..."
            refreshEventsList();
        } else {
            // Show the specific error message from the service
            JOptionPane.showMessageDialog(this, "Failed to create event: " + resultMessage, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void refreshEventsList() {
        currentAdminEvents = eventService.getEventsCreatedByAdmin(loggedInAdmin);
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0); // Clear table
            List<Group> adminGroups = groupService.getGroupsByAdmin(loggedInAdmin); // Needed to display group name
            Map<Integer, String> groupNameMap = new HashMap<>();
            if (adminGroups != null) {
                adminGroups.forEach(g -> groupNameMap.put(g.getId(), g.getName()));
            }


            if (currentAdminEvents != null) {
                System.out.println("DEBUG: AdminEventPanel refreshing table with " + currentAdminEvents.size() + " events.");
                for (Event event : currentAdminEvents) {
                    Vector<Object> row = new Vector<>();
                    row.add(event.getId()); // Hidden ID
                    row.add(event.getTitle());
                    // Display group name if assigned, otherwise "Personal" or ""
                    String groupName = event.getAssignedGroupId() != null ? groupNameMap.getOrDefault(event.getAssignedGroupId(), "Group #" + event.getAssignedGroupId()) : "Personal/None";
                    row.add(groupName);
                    row.add(event.getDateTime());
                    row.add(event.getCategory());
                    row.add(event.getPriority());
                    row.add(event.getLocation());
                    tableModel.addRow(row);
                }
            } else {
                System.out.println("DEBUG: AdminEventPanel found no events created by admin.");
            }
        });
    }

    // Method called by parent dashboard if needed
    public void refreshData() {
        populateGroupSelector();
        refreshEventsList();
    }

    // Helper class to display Group names in JComboBox but store Group object
    private static class GroupWrapper {
        private Group group;
        private String display;

        GroupWrapper(Group group) {
            this.group = group;
            this.display = (group != null) ? group.getName() : "Select Group...";
        }
        // Overload for placeholder text
        GroupWrapper(Group group, String placeholder) {
            this.group = group;
            this.display = placeholder;
        }

        public Group getGroup() {
            return group;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}