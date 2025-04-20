package com.eventplanner.model;

public class Group {
    private int id;
    private String name;
    private int adminId; // ID of the Admin who owns the group
    private String joinCode;
    private String adminName; // Name of the admin for display

    // Constructor for loading from DB
    public Group(int id, String name, int adminId, String joinCode, String adminName) {
        this.id = id;
        this.name = name;
        this.adminId = adminId;
        this.joinCode = joinCode; // Usually only needed by admin, but potentially useful
        this.adminName = adminName;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAdminId() { return adminId; }
    public String getJoinCode() { return joinCode; }
    public String getAdminName() { return adminName; }

    // Setters might not be needed for user view

    @Override
    public String toString() {
        return name + " (Admin: " + adminName + ")";
    }
}