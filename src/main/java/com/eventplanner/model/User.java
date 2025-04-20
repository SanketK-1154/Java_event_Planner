package com.eventplanner.model;

import java.sql.Timestamp; // Import Timestamp

public class User {
    private int id;
    private String name;
    private String email;
    private String password; // Hashed password
    private String role;
    private Timestamp registrationDate; // Added field

    // --- Updated Constructor ---
    public User(int id, String name, String email, String password, String role, Timestamp registrationDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.registrationDate = registrationDate; // Initialize
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public Timestamp getRegistrationDate() { return registrationDate; } // Getter for new field

    // --- Setters ---
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    // registrationDate is usually set by DB on creation
    // public void setRegistrationDate(Timestamp registrationDate) { this.registrationDate = registrationDate; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", registrationDate=" + registrationDate + // Added to toString
                '}';
    }
}