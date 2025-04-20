package com.eventplanner.service;

import com.eventplanner.dao.EventDAO;
import com.eventplanner.model.Event;
import com.eventplanner.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventService {

    private EventDAO eventDAO;

    public EventService() {
        this.eventDAO = new EventDAO();
    }

    /**
     * Gets all relevant events for the logged-in user.
     */
    public List<Event> getEventsForUser(User user) {
        if (user == null) return new ArrayList<>(); // Return empty list if no user
        try {
            return eventDAO.getEventsForUser(user.getId());
        } catch (SQLException e) {
            System.err.println("Error fetching events for user " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    /**
     * Gets distinct event dates for calendar highlighting.
     */
    public List<String> getEventDatesForUser(User user) {
        if (user == null) return new ArrayList<>();
        try {
            return eventDAO.getEventDatesForUser(user.getId());
        } catch (SQLException e) {
            System.err.println("Error fetching event dates for user " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Event> getEventsForDate(User user, java.util.Date date) {
        if (user == null || date == null) return new ArrayList<>();
        try {
            // Make sure to pass java.util.Date, DAO will convert to java.sql.Date
            return eventDAO.getEventsForDate(user.getId(), date);
        } catch (SQLException e) {
            System.err.println("Error fetching events for user " + user.getId() + " on date " + date + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    /**
     * Adds a new event, created by the given user.
     * Performs validation checks for duplicate title/date/group and location/time conflicts.
     * @return null on success, or an error message string on failure.
     */
    public String addEvent(User creator, Event event, Integer targetGroupId) { // Returns String
        if (creator == null || event == null) return "Invalid creator or event data.";
        if (event.getTitle() == null || event.getTitle().trim().isEmpty() || event.getDateTime() == null) return "Event title and date/time are required.";

        // *** MAKE SURE event.setUserId(...) IS REMOVED ***
        // The creator ID should have been set when 'event' was constructed.

        // Explicitly set the group ID on the event object
        event.setAssignedGroupId(targetGroupId); // Assuming setter exists in Event.java
        System.out.println("DEBUG: EventService setting assignedGroupId to: " + targetGroupId + " on event object before DAO call.");

        try {
            // 1. Check for duplicate Title + Date + Group
            boolean exists = eventDAO.eventExists(creator.getId(), event.getTitle(), event.getDateTime(), targetGroupId);
            if (exists) {
                String groupMsg = targetGroupId == null ? "as a personal event" : "for the selected group";
                return "An event with the same title already exists on this date " + groupMsg + ".";
            }

            // 2. Check for Location + Time Conflict
            if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
                boolean booked = eventDAO.isLocationBooked(event.getLocation(), event.getDateTime());
                if (booked) {
                    return "Location '" + event.getLocation() + "' is already booked at this exact time.";
                }
            }

            // If both checks pass, proceed to add event
            boolean success = eventDAO.addEvent(event);
            if (success) {
                return null; // Indicate success
            } else {
                return "Failed to save event to the database.";
            }
        } catch (SQLException e) {
            System.err.println("Error during validation or adding event in service: " + e.getMessage());
            e.printStackTrace();
            return "Database error occurred while adding event.";
        }
    }


    /**
     * Gets all events created by a specific admin user.
     */
    public List<Event> getEventsCreatedByAdmin(User adminUser) {
        if (adminUser == null || !"ADMIN".equalsIgnoreCase(adminUser.getRole())) {
            return new ArrayList<>(); // Return empty list if not an admin
        }
        try {
            return eventDAO.getEventsCreatedByAdmin(adminUser.getId());
        } catch (SQLException e) {
            System.err.println("Error fetching events for admin " + adminUser.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean deleteAdminEvent(User adminUser, int eventId) {
        if (adminUser == null || !"ADMIN".equalsIgnoreCase(adminUser.getRole())) {
            System.err.println("Permission denied: Only admins can delete events via this method.");
            return false;
        }
        try {
            return eventDAO.deleteAdminEvent(eventId, adminUser.getId());
        } catch (SQLException e) {
            System.err.println("Error deleting event ID " + eventId + " in service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing personal event for the user.
     */
    public boolean updatePersonalEvent(User user, Event event) {
        if (user == null || event == null) return false;
        // Ensure the event's user ID matches the logged-in user
        if (event.getUserId() != user.getId()) {
            System.err.println("Security Error: Attempt to update personal event for wrong user.");
            return false;
        }
        try {
            return eventDAO.updatePersonalEvent(event);
        } catch (SQLException e) {
            System.err.println("Error updating personal event: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a personal event owned by the user.
     */
    public boolean deletePersonalEvent(User user, int eventId) {
        if (user == null) return false;
        try {
            return eventDAO.deletePersonalEvent(eventId, user.getId());
        } catch (SQLException e) {
            System.err.println("Error deleting personal event: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}