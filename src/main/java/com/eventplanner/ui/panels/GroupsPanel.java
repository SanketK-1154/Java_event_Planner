package com.eventplanner.ui.panels;

import com.eventplanner.model.Group;
import com.eventplanner.model.User;
import com.eventplanner.service.GroupService;
import com.eventplanner.ui.UserDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class GroupsPanel extends JPanel {

    private User loggedInUser;
    private GroupService groupService;
    private JTable groupTable;
    private DefaultTableModel tableModel;
    private List<Group> currentGroups;

    private final String[] columnNames = {"Group ID", "Group Name", "Admin Name"};
    private static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14); // Consistent header font
    private static final Color TABLE_GRID_COLOR = new Color(220, 220, 220); // Consistent grid color


    public GroupsPanel(User user, GroupService groupService) {
        this.loggedInUser = user;
        this.groupService = groupService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 245, 250)); // Consistent background

        // --- Top Panel: Join Group ---
        JPanel joinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        joinPanel.setBorder(BorderFactory.createTitledBorder("Join a Group"));
        joinPanel.setBackground(getBackground()); // Match panel background
        JTextField joinCodeField = new JTextField(15);
        // Add Icon
        JButton joinButton = createStyledButton("Join", "src/main/resources/icons/join_icon.png"); // Placeholder icon

        joinPanel.add(new JLabel("Enter Join Code:"));
        joinPanel.add(joinCodeField);
        joinPanel.add(joinButton);

        add(joinPanel, BorderLayout.NORTH);

        // --- Center Panel: Group List ---
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                return String.class;
            }
        };
        groupTable = new JTable(tableModel);
        groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Style Table
        groupTable.setRowHeight(25);
        groupTable.setGridColor(TABLE_GRID_COLOR);
        groupTable.setShowVerticalLines(false);
        JTableHeader header = groupTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(new Color(230, 235, 240));
        header.setForeground(new Color(60, 60, 60));
        header.setReorderingAllowed(false);

        // Hide Group ID column
        groupTable.getColumnModel().getColumn(0).setMinWidth(0);
        groupTable.getColumnModel().getColumn(0).setMaxWidth(0);
        groupTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(groupTable);
        groupTable.setFillsViewportHeight(true);

        add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Panel: Leave Group ---
        JPanel leavePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leavePanel.setBackground(getBackground());
        // Add Icon
        JButton leaveButton = createStyledButton("Leave Selected Group", "src/main/resources/icons/leave_icon.png"); // Placeholder icon
        leavePanel.add(leaveButton);
        add(leavePanel, BorderLayout.SOUTH);


        // --- Action Listeners ---
        joinButton.addActionListener(e -> {
            String code = joinCodeField.getText().trim();
            if (!code.isEmpty()) {
                Group joinedGroup = groupService.joinGroup(loggedInUser, code);
                if (joinedGroup != null) {
                    JOptionPane.showMessageDialog(this, "Joined group: " + joinedGroup.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
                    joinCodeField.setText("");
                    refreshGroupList();
                    notifyDashboardRefresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid code or already joined.", "Join Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a join code.", "Input Required", JOptionPane.WARNING_MESSAGE);
            }
        });
        leaveButton.addActionListener(e -> leaveSelectedGroup());

        refreshGroupList();
    }

    // Helper method to create styled buttons with optional icons (copy from EventsPanel or create a Utility class)
    private JButton createStyledButton(String text, String iconPath) {
        ImageIcon icon = null;
        if (iconPath != null) {
            java.net.URL imgURL = getClass().getClassLoader().getResource(iconPath);
            if (imgURL != null) {
                icon = new ImageIcon(imgURL);
            } else { System.err.println("Icon not found: " + iconPath); }
        }
        JButton button = new JButton(text, icon);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public void refreshGroupList() {
        currentGroups = groupService.getUserGroups(loggedInUser);
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            if (currentGroups != null) {
                for (Group group : currentGroups) {
                    Vector<Object> row = new Vector<>();
                    row.add(group.getId());
                    row.add(group.getName());
                    row.add(group.getAdminName());
                    tableModel.addRow(row);
                }
            }
        });
    }

    private void leaveSelectedGroup() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a group to leave.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = groupTable.convertRowIndexToModel(selectedRow);
        int groupId = (Integer) tableModel.getValueAt(modelRow, 0);
        String groupName = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Leave group '" + groupName + "'?",
                "Confirm Leave Group", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = groupService.leaveGroup(loggedInUser, groupId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Left group: " + groupName, "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshGroupList();
                notifyDashboardRefresh();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to leave group.", "Error", JOptionPane.ERROR_MESSAGE);
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