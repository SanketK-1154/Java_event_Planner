package com.eventplanner.ui.panels;

import com.eventplanner.model.Event;
import com.eventplanner.model.User;
import com.eventplanner.service.EventService;
import com.toedter.calendar.IDateEvaluator;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDayChooser;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarPanel extends JPanel {

    private User loggedInUser;
    private EventService eventService;
    private JCalendar jCalendar;
    // Store events fetched for the currently viewed month/user to improve performance
    private Map<String, List<Event>> eventsByDateMap = new HashMap<>();
    private SimpleDateFormat mapKeyFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private JTextArea selectedDateEventsArea; // To display events for selected day
    private EventDateEvaluator dateEvaluator; // Custom evaluator for highlighting/tooltips

    // Styling
    private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 150); // Light Yellow
    private static final Color SELECTED_DAY_BG = new Color(180, 210, 255); // Light Blue
    private static final Font EVENT_DISPLAY_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public CalendarPanel(User user, EventService eventService) {
        this.loggedInUser = user;
        this.eventService = eventService;
        this.dateEvaluator = new EventDateEvaluator(); // Initialize evaluator

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 245, 250)); // Match theme

        // --- Calendar Component ---
        jCalendar = new JCalendar();
        jCalendar.setDecorationBordersVisible(true);
        // Use our custom evaluator for highlighting and tooltips
        jCalendar.getDayChooser().addDateEvaluator(dateEvaluator);
        // Set a custom background for the selected day
        jCalendar.getDayChooser().setDayBordersVisible(true); // Helps selection visibility
//        jCalendar.getDayChooser().setSelectedDayBackground(SELECTED_DAY_BG);


        // --- Listener for Date Selection ---
        jCalendar.addPropertyChangeListener("calendar", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // This listener handles both programmatic changes and user clicks
                // Check if the day, month or year has changed.
                // The "calendar" property change often signals a date selection.
                updateSelectedDateEvents();

                // Also check if month/year changed to refresh highlighting/tooltips potentially
                Object oldValue = evt.getOldValue();
                Object newValue = evt.getNewValue();
                if (oldValue instanceof Calendar && newValue instanceof Calendar) {
                    Calendar oldCal = (Calendar) oldValue;
                    Calendar newCal = (Calendar) newValue;
                    if (oldCal.get(Calendar.MONTH) != newCal.get(Calendar.MONTH) ||
                            oldCal.get(Calendar.YEAR) != newCal.get(Calendar.YEAR)) {
                        // Month or year changed, might need to re-fetch events for the new view
                        // For simplicity now, we just refresh highlights based on existing map
                        refreshCalendarHighlightsAndTooltips();
                    }
                }
            }
        });

        add(jCalendar, BorderLayout.CENTER);

        // --- Display Area for Selected Date ---
        selectedDateEventsArea = new JTextArea(4, 20); // Rows, Columns
        selectedDateEventsArea.setEditable(false);
        selectedDateEventsArea.setFont(EVENT_DISPLAY_FONT);
        selectedDateEventsArea.setLineWrap(true);
        selectedDateEventsArea.setWrapStyleWord(true);
        selectedDateEventsArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(selectedDateEventsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Events on Selected Date"));
        add(scrollPane, BorderLayout.SOUTH);


        // Initial load and display
        refreshCalendarView(); // Fetches data and updates highlights/tooltips
        updateSelectedDateEvents(); // Show events for initially selected date
    }

    /**
     * Fetches all event data for the current user and updates the internal map
     * and calendar display (highlights, tooltips).
     * Should be called when the user logs in or data might have changed significantly.
     */
    public void refreshCalendarView() {
        System.out.println("DEBUG: CalendarPanel.refreshCalendarView() called");
        // Fetch all events for the user (might be inefficient for very large datasets)
        // A better approach might fetch only for the visible month range, but this is simpler
        List<Event> allEvents = eventService.getEventsForUser(loggedInUser);
        System.out.println("DEBUG: CalendarPanel fetched " + allEvents.size() + " total events.");

        // Group events by date string ("yyyy-MM-dd")
        eventsByDateMap = allEvents.stream()
                .filter(e -> e.getDateTime() != null)
                .collect(Collectors.groupingBy(
                        e -> mapKeyFormatter.format(new Date(e.getDateTime().getTime()))
                ));
        System.out.println("DEBUG: Grouped events into " + eventsByDateMap.size() + " dates.");

        // Update the evaluator with the new map
        dateEvaluator.setEventsMap(eventsByDateMap);

        // Refresh highlights and tooltips for the current view
        refreshCalendarHighlightsAndTooltips();
        // Also update the display for the currently selected date
        updateSelectedDateEvents();
    }

    /**
     * Updates the highlights and tooltips based on the data in eventsByDateMap.
     */
    private void refreshCalendarHighlightsAndTooltips() {
        SwingUtilities.invokeLater(() -> {
            JDayChooser dayChooser = jCalendar.getDayChooser();
            if (dayChooser != null) {
                // Adding the evaluator again ensures it's up-to-date
                // (Or update the evaluator's internal data and repaint)
                dayChooser.removeDateEvaluator(dateEvaluator); // Remove old first
                dayChooser.addDateEvaluator(dateEvaluator);   // Add updated one
                dayChooser.repaint(); // Force repaint
                System.out.println("DEBUG: Calendar highlights/tooltips refreshed.");
            }
        });
    }

    /**
     * Updates the text area below the calendar to show events for the currently selected date.
     */
    private void updateSelectedDateEvents() {
        Date selectedDate = jCalendar.getDate();
        String selectedDateStr = mapKeyFormatter.format(selectedDate);
        System.out.println("DEBUG: Calendar date selected/changed: " + selectedDateStr); // Debug

        List<Event> eventsForDay = eventsByDateMap.getOrDefault(selectedDateStr, new ArrayList<>());

        StringBuilder displayText = new StringBuilder();
        if (eventsForDay.isEmpty()) {
            displayText.append("No events scheduled for ").append(selectedDateStr).append(".");
        } else {
            displayText.append("Events for ").append(selectedDateStr).append(":\n");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); // Time only
            for (Event event : eventsForDay) {
                displayText.append("- ")
                        .append(timeFormat.format(new Date(event.getDateTime().getTime())))
                        .append(" ")
                        .append(event.getTitle())
                        .append(event.isPersonal() ? " (Personal)" : " (Group: " + event.getCreatedBy() + ")")
                        .append("\n");
            }
        }

        // Update the text area on the EDT
        SwingUtilities.invokeLater(() -> {
            selectedDateEventsArea.setText(displayText.toString().trim());
            selectedDateEventsArea.setCaretPosition(0); // Scroll to top
            System.out.println("DEBUG: Updated selected date events display."); // Debug
        });
    }


    /**
     * Inner class implementing IDateEvaluator to customize date appearance and tooltips.
     */
    private static class EventDateEvaluator implements IDateEvaluator {
        private Map<String, List<Event>> eventsMap; // Store all events grouped by "yyyy-MM-dd"
        private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        public void setEventsMap(Map<String, List<Event>> eventsMap) {
            this.eventsMap = eventsMap != null ? eventsMap : new HashMap<>();
        }

        @Override
        public boolean isSpecial(Date date) {
            // A date is special if there are events on that day
            String dateStr = formatter.format(date);
            return eventsMap.containsKey(dateStr) && !eventsMap.get(dateStr).isEmpty();
        }

        @Override
        public Color getSpecialForegroundColor() {
            return null; // Use default text color
        }

        @Override
        public Color getSpecialBackroundColor() {
            // Use the highlight color defined in the outer class
            return CalendarPanel.HIGHLIGHT_COLOR;
        }

        @Override
        public String getSpecialTooltip() {
            // This method might not be called exactly when needed for hover,
            // JCalendar tooltip handling can be tricky. We might need to set tooltips
            // directly on the day components if this doesn't work reliably.
            // Let's return a generic tooltip for now.
            // A better approach might be needed if this isn't sufficient.
            return "View event details"; // Placeholder
        }

        // --- Methods for customizing dates outside the current month ---
        @Override public boolean isInvalid(Date date) { return false; }
        @Override public Color getInvalidForegroundColor() { return null; }
        @Override public Color getInvalidBackroundColor() { return null; }
        @Override public String getInvalidTooltip() { return null; }

        // --- Tooltip Handling directly on component (Alternative if getSpecialTooltip fails) ---
        // JCalendar doesn't directly expose easy tooltip setting per day via evaluator.
        // If the evaluator's tooltip doesn't work, you'd need to iterate through
        // dayChooser.getDayPanel().getComponents() after fetching data, check each JButton's date,
        // lookup events in the map, format the tooltip string (HTML for multi-line),
        // and call setToolTipText() on the button. This is more complex.
        // Let's see if the basic evaluator tooltip provides any feedback first.
    }
}