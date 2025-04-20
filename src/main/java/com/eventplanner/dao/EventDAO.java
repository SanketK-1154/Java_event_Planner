package com.eventplanner.dao;

import com.eventplanner.model.Event;
import com.eventplanner.model.User; // Needed for the new method

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    // --- Event Creation (Handles both Personal and Group Events) ---

    /**
     * Adds a new event to the database.
     * Sets assigned_group_id based on the Event object's value.
     */
    public boolean addEvent(Event event) throws SQLException {
        // SQL now includes assigned_group_id as a parameter
        String sql = "INSERT INTO events (user_id, title, description, date_time, location, category, priority, assigned_group_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, event.getUserId()); // Creator ID
            stmt.setString(2, event.getTitle());
            stmt.setString(3, event.getDescription());
            stmt.setTimestamp(4, event.getDateTime());
            stmt.setString(5, event.getLocation());
            stmt.setString(6, event.getCategory());
            stmt.setString(7, event.getPriority());

            // Set assigned_group_id (could be null for personal events)
            if (event.getAssignedGroupId() != null) {
                stmt.setInt(8, event.getAssignedGroupId());
            } else {
                stmt.setNull(8, Types.INTEGER); // Explicitly set NULL if no group assigned
            }

            int rowsAffected = stmt.executeUpdate();

            // Set the generated ID back into the event object
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        event.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    // --- Event Updating (Keep existing updatePersonalEvent or adapt/create updateGroupEvent later) ---
    public boolean updatePersonalEvent(Event event) throws SQLException {
        // Make sure we only update personal events owned by the user
        String sql = "UPDATE events SET title = ?, description = ?, date_time = ?, location = ?, category = ?, priority = ? " +
                "WHERE id = ? AND user_id = ? AND assigned_group_id IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDescription());
            stmt.setTimestamp(3, event.getDateTime());
            stmt.setString(4, event.getLocation());
            stmt.setString(5, event.getCategory());
            stmt.setString(6, event.getPriority());
            stmt.setInt(7, event.getId());
            stmt.setInt(8, event.getUserId()); // Ensure ownership

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    // TODO: Add updateGroupEvent method later if needed (check admin permission)

    // --- Event Deletion (Keep existing deletePersonalEvent or adapt/create deleteGroupEvent later) ---
    public boolean deletePersonalEvent(int eventId, int userId) throws SQLException {
        // Ensure the user owns the event and it's personal
        String sql = "DELETE FROM events WHERE id = ? AND user_id = ? AND assigned_group_id IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    // TODO: Add deleteGroupEvent method later if needed (check admin permission)


    // --- Event Fetching ---

    /**
     * Gets all events created by a specific admin user.
     * Includes both personal events created by the admin and group events created by the admin.
     */
    public List<Event> getEventsCreatedByAdmin(int adminId) throws SQLException {
        List<Event> events = new ArrayList<>();
        // Fetch events where the user_id matches the adminId
        // We still join users to get the creator name (which will be the admin's name here)
        String sql = "SELECT e.*, COALESCE(creator.name, 'Unknown') as creator_name " +
                "FROM events e " +
                "JOIN users creator ON e.user_id = creator.id " +
                "WHERE e.user_id = ? " + // Filter by creator ID
                "ORDER BY e.date_time DESC"; // Show newest first perhaps

        System.out.println("DEBUG: EventDAO getting events created by admin ID: " + adminId); // Debug

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);

            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("DEBUG: DAO found event created by admin - ID: " + rs.getInt("id") + ", Title: " + rs.getString("title") ); // Debug
                events.add(mapRowToEvent(rs)); // Reuse existing mapper
            }
            System.out.println("DEBUG: DAO query for admin events finished. Found " + count + " rows."); // Debug
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getEventsCreatedByAdmin:");
            throw e;
        }
        return events;
    }

    public boolean deleteAdminEvent(int eventId, int adminId) throws SQLException {
        // Delete the event only if the ID matches AND the user_id matches the admin's ID
        String sql = "DELETE FROM events WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, adminId); // Ensure admin owns (created) the event
            int rowsAffected = stmt.executeUpdate();
            System.out.println("DEBUG: Attempted to delete event ID " + eventId + " by admin ID " + adminId + ". Rows affected: " + rowsAffected); // Debug
            return rowsAffected > 0;
        }
    }


    public boolean isLocationBooked(String location, Timestamp dateTime) throws SQLException {
        String sql = "SELECT COUNT(*) FROM events WHERE LOWER(location) = LOWER(?) AND date_time = ?";
        if (location == null || location.trim().isEmpty() || dateTime == null) {
            System.out.println("DEBUG: isLocationBooked check skipped (null/empty location or dateTime).");
            return false;
        }

        String locationToCheck = location.trim();
        System.out.println("DEBUG: Checking isLocationBooked for Location: '" + locationToCheck + "' (lower: '" + locationToCheck.toLowerCase() + "'), DateTime: " + dateTime + " (ms: " + dateTime.getTime() + ")"); // Debug Params

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, locationToCheck); // Use trimmed version
            stmt.setTimestamp(2, dateTime);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("DEBUG: Location check query found " + count + " existing events."); // Debug Result
                return count > 0;
            }
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException during isLocationBooked check:");
            e.printStackTrace(); // Print stack trace for DB errors
            throw e; // Re-throw
        }
        return false; // Default to false if query fails somehow
    }

    // --- Keep getEventsForUser, getEventDatesForUser, getEventsForDate, mapRowToEvent ---
    // Ensure mapRowToEvent correctly handles category/priority/assigned_group_id (it should already)

    public List<Event> getEventsForUser(int userId) throws SQLException {
        // ... (existing code - should now correctly fetch group events added by admin) ...
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, COALESCE(creator.name, 'Unknown') as creator_name " +
                "FROM events e " +
                "LEFT JOIN users creator ON e.user_id = creator.id " +
                "WHERE " +
                " (e.user_id = ? AND e.assigned_group_id IS NULL) " + // User's personal events
                " OR " +
                " (e.assigned_group_id IN (SELECT gm.group_id FROM group_memberships gm WHERE gm.user_id = ?)) " + // Events from user's groups
                "ORDER BY e.date_time ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId); stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) { events.add(mapRowToEvent(rs)); }
        }
        return events;
    }

    public List<String> getEventDatesForUser(int userId) throws SQLException {
        // ... (existing code) ...
        List<String> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT DATE_FORMAT(e.date_time, '%Y-%m-%d') as event_date " +
                "FROM events e " +
                "WHERE " +
                " (e.user_id = ? AND e.assigned_group_id IS NULL) " +
                " OR " +
                " (e.assigned_group_id IN (SELECT gm.group_id FROM group_memberships gm WHERE gm.user_id = ?)) " +
                "ORDER BY event_date ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId); stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) { dates.add(rs.getString("event_date")); }
        }
        return dates;
    }

    public List<Event> getEventsForDate(int userId, java.util.Date date) throws SQLException {
        // ... (existing code) ...
        List<Event> events = new ArrayList<>();
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        String sql = "SELECT e.*, COALESCE(creator.name, 'Unknown') as creator_name " +
                "FROM events e " +
                "LEFT JOIN users creator ON e.user_id = creator.id " +
                "WHERE DATE(e.date_time) = ? " +
                "AND (" +
                "      (e.user_id = ? AND e.assigned_group_id IS NULL) " +
                "      OR " +
                "      (e.assigned_group_id IN (SELECT gm.group_id FROM group_memberships gm WHERE gm.user_id = ?)) " +
                "    )" +
                "ORDER BY e.date_time ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, sqlDate); stmt.setInt(2, userId); stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) { events.add(mapRowToEvent(rs)); }
        }
        return events;
    }

    public boolean eventExists(int creatorUserId, String title, Timestamp dateTime, Integer assignedGroupId) throws SQLException {
        // Need to handle null assignedGroupId carefully in the SQL query
        String sql;
        if (assignedGroupId == null) {
            // Check for personal event collision (assigned_group_id IS NULL)
            sql = "SELECT COUNT(*) FROM events WHERE user_id = ? AND title = ? AND DATE(date_time) = DATE(?) AND assigned_group_id IS NULL";
        } else {
            // Check for specific group event collision
            sql = "SELECT COUNT(*) FROM events WHERE user_id = ? AND title = ? AND DATE(date_time) = DATE(?) AND assigned_group_id = ?";
        }

        // Basic validation on input
        if (title == null || title.trim().isEmpty() || dateTime == null) {
            System.out.println("DEBUG: eventExists check skipped (null/empty title or dateTime).");
            return false;
        }
        String titleToCheck = title.trim();
        // Convert java.sql.Timestamp to java.sql.Date for DATE() comparison
        java.sql.Date dateToCheck = new java.sql.Date(dateTime.getTime());

        System.out.println("DEBUG: Checking eventExists for Creator: " + creatorUserId + ", Title: '" + titleToCheck + "', Date: " + dateToCheck + ", GroupID: " + assignedGroupId); // Debug Params

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, creatorUserId);
            stmt.setString(2, titleToCheck);
            stmt.setDate(3, dateToCheck); // Compare DATE part only
            if (assignedGroupId != null) {
                stmt.setInt(4, assignedGroupId); // Set group ID only if checking for group event
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("DEBUG: eventExists check query found " + count + " matching events."); // Debug Result
                return count > 0; // Return true if count > 0 (exists)
            }
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException during eventExists check:");
            e.printStackTrace();
            throw e;
        }
        return false;
    }

    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        // ... (existing code - should be correct) ...
        Integer assignedGroupId = rs.getObject("assigned_group_id") != null ? rs.getInt("assigned_group_id") : null;
        String creatorName = rs.getString("creator_name");
        return new Event(
                rs.getInt("id"), rs.getInt("user_id"), rs.getString("title"), rs.getString("description"),
                rs.getTimestamp("date_time"), rs.getString("location"), rs.getString("category"),
                rs.getString("priority"), assignedGroupId, creatorName
        );
    }
}