package com.eventplanner.service;

import com.eventplanner.dao.GroupDAO;
import com.eventplanner.model.Group;
import com.eventplanner.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GroupService {

    private GroupDAO groupDAO;

    public GroupService() {
        this.groupDAO = new GroupDAO();
    }

    public Group createGroup(User adminUser, String groupName) {
        // Basic validation
        if (adminUser == null || !"ADMIN".equalsIgnoreCase(adminUser.getRole())) {
            System.err.println("Error creating group: Invalid admin user provided.");
            return null;
        }
        if (groupName == null || groupName.trim().isEmpty()) {
            System.err.println("Error creating group: Group name cannot be empty.");
            return null;
        }

        try {
            return groupDAO.createGroup(groupName.trim(), adminUser.getId());
        } catch (SQLException e) {
            System.err.println("Error creating group in service: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all groups managed by the specified admin user.
     */
    public List<Group> getGroupsByAdmin(User adminUser) {
        if (adminUser == null) return new ArrayList<>();
        try {
            return groupDAO.getGroupsByAdminId(adminUser.getId());
        } catch (SQLException e) {
            System.err.println("Error fetching groups for admin " + adminUser.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean removeUserFromGroup(User adminUser, int userIdToRemove, int groupId) {
        if (adminUser == null || !"ADMIN".equalsIgnoreCase(adminUser.getRole())) {
            System.err.println("Permission denied: Only admins can remove users.");
            return false; // Or throw exception
        }
        // Optional: Add check here to ensure adminUser.getId() is the admin_id of groupId

        try {
            return groupDAO.removeUserFromGroup(userIdToRemove, groupId);
        } catch (SQLException e) {
            System.err.println("Error removing user " + userIdToRemove + " from group " + groupId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the list of users who are members of a specific group.
     */
    public List<User> getUsersInGroup(int groupId) {
        try {
            return groupDAO.getUsersByGroupId(groupId);
        } catch (SQLException e) {
            System.err.println("Error fetching users for group " + groupId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    /**
     * Allows a user to join a group using a join code.
     * Returns the joined Group object or null if failed/invalid code.
     */
    public Group joinGroup(User user, String joinCode) {
        if (user == null || joinCode == null || joinCode.trim().isEmpty()) {
            return null;
        }
        try {
            return groupDAO.joinGroup(user.getId(), joinCode.trim());
        } catch (SQLException e) {
            System.err.println("Error joining group: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteGroup(User adminUser, int groupId) {
        if (adminUser == null || !"ADMIN".equalsIgnoreCase(adminUser.getRole())) {
            System.err.println("Permission denied: Only admins can delete groups.");
            return false;
        }
        try {
            // DAO checks if adminUser.getId() actually owns the group
            return groupDAO.deleteGroup(groupId, adminUser.getId());
        } catch (SQLException e) {
            System.err.println("Error deleting group ID " + groupId + " in service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Allows a user to leave a group.
     */
    public boolean leaveGroup(User user, int groupId) {
        if (user == null) {
            return false;
        }
        try {
            return groupDAO.leaveGroup(user.getId(), groupId);
        } catch (SQLException e) {
            System.err.println("Error leaving group: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the list of groups a user is currently a member of.
     */
    public List<Group> getUserGroups(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        try {
            return groupDAO.getUserGroups(user.getId());
        } catch (SQLException e) {
            System.err.println("Error fetching user groups: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}