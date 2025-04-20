package com.eventplanner.ui.admin.panels;

import com.eventplanner.model.Group;
import com.eventplanner.model.User;
import com.eventplanner.service.GroupService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AdminGroupPanel extends JPanel {

    private User loggedInAdmin;
    private GroupService groupService;

    // Group List Components
    private JTable groupTable;
    private DefaultTableModel groupTableModel;
    private JScrollPane groupScrollPane;
    private JTextField groupNameField;
    private JButton createGroupButton;
    private List<Group> currentAdminGroups; // Store fetched groups
    private JButton deleteGroupButton;
    // Group Member Components
    private JTable memberTable;
    private DefaultTableModel memberTableModel;
    private JScrollPane memberScrollPane;
    private JButton removeMemberButton;
    private JLabel selectedGroupNameLabel; // Show which group's members are displayed
    private List<User> currentGroupMembers; // Store fetched members

    // Table Column Definitions
    private final String[] groupColumnNames = {"ID", "Group Name", "Join Code"};
    private final String[] memberColumnNames = {"User ID", "Name", "Email"};

    // Styling
    private static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Color TABLE_GRID_COLOR = new Color(220, 220, 220);
    private static final Color PANEL_BACKGROUND = new Color(250, 240, 240); // Match admin theme

    public AdminGroupPanel(User adminUser, GroupService groupService) {
        this.loggedInAdmin = adminUser;
        this.groupService = groupService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(PANEL_BACKGROUND);

        // --- Top Panel: Create Group ---
        JPanel createPanel = createCreateGroupPanel();
        add(createPanel, BorderLayout.NORTH);

        // --- Center Area: Use JSplitPane for Groups List and Members List ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(350); // Initial divider location
        splitPane.setResizeWeight(0.4); // How space is distributed on resize
        splitPane.setBorder(null); // Remove border from split pane itself
        splitPane.setBackground(getBackground());

        // Left side of Split Pane: Group List
        JPanel groupListPanel = createGroupListPanel();
        splitPane.setLeftComponent(groupListPanel);

        // Right side of Split Pane: Member List
        JPanel memberListPanel = createMemberListPanel();
        splitPane.setRightComponent(memberListPanel);

        add(splitPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        createGroupButton.addActionListener(e -> createNewGroup());
        removeMemberButton.addActionListener(e -> removeSelectedMember());

        // Listener for group table selection changes
        groupTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Prevent double events, check if selection is stable
                if (!e.getValueIsAdjusting()) {
                    updateMemberListForSelectedGroup();
                }
            }
        });

        // Initial data load
        refreshGroupList();
        // Initially clear member list or show placeholder
        updateMemberList(new ArrayList<>(), null);
    }

    // --- Panel Creation Helper Methods ---

    private JPanel createCreateGroupPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Create New Group"));
        panel.setBackground(getBackground());
        groupNameField = new JTextField(25);
        createGroupButton = new JButton("Create Group");
        // Style button if needed
        panel.add(new JLabel("New Group Name:"));
        panel.add(groupNameField);
        panel.add(createGroupButton);
        return panel;
    }

    private JPanel createGroupListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Add gap
        panel.setBorder(BorderFactory.createTitledBorder("My Groups"));
        panel.setBackground(getBackground());

        // ... (groupTableModel and groupTable setup as before) ...
        groupTableModel = new DefaultTableModel(groupColumnNames, 0) { /* ... */ };
        groupTable = new JTable(groupTableModel);
        setupTableStyle(groupTable, groupTableModel);
        groupTable.getColumnModel().getColumn(0).setMaxWidth(0); // Hide ID
        groupTable.getColumnModel().getColumn(0).setMinWidth(0);
        groupTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        groupScrollPane = new JScrollPane(groupTable);
        panel.add(groupScrollPane, BorderLayout.CENTER);

        // Add Delete Button Panel at the bottom of this section
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBackground(getBackground());
        deleteGroupButton = new JButton("Delete Selected Group");
        // Optional: Add icon
        deleteGroupButton.addActionListener(e -> deleteSelectedGroup());
        actionPanel.add(deleteGroupButton);

        panel.add(actionPanel, BorderLayout.SOUTH); // Add button below group table
        return panel;
    }

    // Add this new method to handle the delete group action
    private void deleteSelectedGroup() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a group from the 'My Groups' table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get model index
        int modelRow = groupTable.convertRowIndexToModel(selectedRow);
        int groupId = (Integer) groupTableModel.getValueAt(modelRow, 0); // Hidden ID
        String groupName = (String) groupTableModel.getValueAt(modelRow, 1); // Name for confirmation

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the group '" + groupName + "'?\n" +
                        "All members will be removed from the group.\n" +
                        "Events assigned to this group will become unassigned.\n" +
                        "This action cannot be undone.",
                "Confirm Group Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = groupService.deleteGroup(loggedInAdmin, groupId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Group '" + groupName + "' deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshGroupList(); // Refresh the group list table
                // Clear the members table as the selected group is gone
                updateMemberList(new ArrayList<>(), null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the selected group.\nIt might have already been deleted or you may not have permission.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createMemberListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Add gaps
        panel.setBorder(BorderFactory.createTitledBorder("Group Members"));
        panel.setBackground(getBackground());

        selectedGroupNameLabel = new JLabel("Select a group to view members");
        selectedGroupNameLabel.setFont(TABLE_HEADER_FONT);
        selectedGroupNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0)); // Padding
        panel.add(selectedGroupNameLabel, BorderLayout.NORTH);

        memberTableModel = new DefaultTableModel(memberColumnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 0 ? Integer.class : String.class; }
        };
        memberTable = new JTable(memberTableModel);
        setupTableStyle(memberTable, memberTableModel); // Apply common style
        // Hide User ID column
        memberTable.getColumnModel().getColumn(0).setMaxWidth(0);
        memberTable.getColumnModel().getColumn(0).setMinWidth(0);
        memberTable.getColumnModel().getColumn(0).setPreferredWidth(0);


        memberScrollPane = new JScrollPane(memberTable);
        panel.add(memberScrollPane, BorderLayout.CENTER);

        removeMemberButton = new JButton("Remove Selected User");
        // Style button if needed
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(getBackground());
        bottomPanel.add(removeMemberButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Helper to apply common styling to tables
    private void setupTableStyle(JTable table, DefaultTableModel model) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setGridColor(TABLE_GRID_COLOR);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(new Color(240, 230, 230));
        header.setForeground(new Color(60, 60, 60));
        header.setReorderingAllowed(false);

        // Optional: Add sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
    }


    // --- Action and Data Refresh Methods ---

    private void createNewGroup() {
        String groupName = groupNameField.getText().trim();
        if (groupName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a group name.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Group createdGroup = groupService.createGroup(loggedInAdmin, groupName);
        if (createdGroup != null) {
            JOptionPane.showMessageDialog(this, "Group '" + createdGroup.getName() + "' created.\nJoin Code: " + createdGroup.getJoinCode(), "Success", JOptionPane.INFORMATION_MESSAGE);
            groupNameField.setText("");
            refreshGroupList();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create group.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedMember() {
        int selectedMemberRow = memberTable.getSelectedRow();
        int selectedGroupRow = groupTable.getSelectedRow();

        if (selectedMemberRow < 0 || selectedGroupRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a group and then a member to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get model indices in case of sorting
        int groupModelRow = groupTable.convertRowIndexToModel(selectedGroupRow);
        int memberModelRow = memberTable.convertRowIndexToModel(selectedMemberRow);

        int groupId = (Integer) groupTableModel.getValueAt(groupModelRow, 0); // Hidden ID
        int userIdToRemove = (Integer) memberTableModel.getValueAt(memberModelRow, 0); // Hidden ID
        String memberName = (String) memberTableModel.getValueAt(memberModelRow, 1);
        String groupName = (String) groupTableModel.getValueAt(groupModelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove user '" + memberName + "' from group '" + groupName + "'?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = groupService.removeUserFromGroup(loggedInAdmin, userIdToRemove, groupId);
            if (success) {
                JOptionPane.showMessageDialog(this, "User removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Refresh only the member list for the currently selected group
                updateMemberListForSelectedGroup();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Called when group selection changes
    private void updateMemberListForSelectedGroup() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow < 0) {
            // No group selected, clear member list
            updateMemberList(new ArrayList<>(), null);
            return;
        }

        // Get model index in case table is sorted
        int modelRow = groupTable.convertRowIndexToModel(selectedRow);
        int selectedGroupId = (Integer) groupTableModel.getValueAt(modelRow, 0); // Get ID from hidden col
        String selectedGroupName = (String) groupTableModel.getValueAt(modelRow, 1);

        // Fetch members for this group
        List<User> members = groupService.getUsersInGroup(selectedGroupId);
        updateMemberList(members, selectedGroupName);
    }

    // Updates the members table UI
    private void updateMemberList(List<User> members, String groupName) {
        SwingUtilities.invokeLater(() -> {
            memberTableModel.setRowCount(0); // Clear member table
            if (groupName != null) {
                selectedGroupNameLabel.setText("Members of: " + groupName);
            } else {
                selectedGroupNameLabel.setText("Select a group to view members");
            }

            if (members != null) {
                System.out.println("DEBUG: Updating member list table with " + members.size() + " members.");
                for (User member : members) {
                    Vector<Object> row = new Vector<>();
                    row.add(member.getId()); // Hidden ID
                    row.add(member.getName());
                    row.add(member.getEmail());
                    memberTableModel.addRow(row);
                }
            } else {
                System.out.println("DEBUG: Member list is null.");
            }
        });
    }

    // Refreshes the list of groups created by the admin
    public void refreshGroupList() {
        currentAdminGroups = groupService.getGroupsByAdmin(loggedInAdmin);
        SwingUtilities.invokeLater(() -> {
            int selectedRow = groupTable.getSelectedRow(); // Preserve selection if possible
            groupTableModel.setRowCount(0);
            if (currentAdminGroups != null) {
                for (Group group : currentAdminGroups) {
                    Vector<Object> row = new Vector<>();
                    row.add(group.getId());
                    row.add(group.getName());
                    row.add(group.getJoinCode());
                    groupTableModel.addRow(row);
                }
                // Try to restore selection
                if (selectedRow >= 0 && selectedRow < groupTable.getRowCount()) {
                    groupTable.setRowSelectionInterval(selectedRow, selectedRow);
                } else {
                    updateMemberList(new ArrayList<>(), null); // Clear members if selection lost
                }
            }
            System.out.println("DEBUG: Refreshed admin group list. Count: " + groupTableModel.getRowCount());
        });
    }

    // Method called by parent dashboard if needed
    public void refreshData() {
        refreshGroupList();
        // updateMemberListForSelectedGroup(); // Update members based on current selection
    }
}