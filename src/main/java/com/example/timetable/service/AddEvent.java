package com.example.timetable.service;
import com.example.timetable.model.Event;
import com.example.timetable.repository.eventRepository;

import java.time.LocalDate;
import java.util.Locale;
import java.util.List;
import java.util.Set;

public class AddEvent {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_LOCATION_LENGTH = 100;
    public static final Set<String> VALID_COLORS = Set.of("blue", "green", "yellow", "peach", "purple");

    private final eventRepository eventRepository;


    public AddEvent(eventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public boolean addEvent(int userId, Event event) {
        if (!validateEvent(userId, event, false)) return false;

        return eventRepository.saveEvent(userId, event);
    }

    public boolean updateEvent(int userId, Event event) {
        if (!validateEvent(userId, event, true)) return false;

        return eventRepository.updateEvent(userId, event);
    }

    public List<Event> getEventsBetween(int userId, LocalDate startDate, LocalDate endDate) {
        if (userId <= 0) return List.of();
        return eventRepository.findEventsBetween(userId, startDate, endDate);
    }

    private boolean validateEvent(int userId, Event event, boolean existingEvent) {
        if (userId <= 0 || event == null) return false;
        if (existingEvent && event.getEvent_id() <= 0) return false;

        String title = event.getTitle() == null ? "" : event.getTitle().trim();
        if (title.isEmpty()) {
            System.err.println("Event title cannot be empty.");
            return false;
        }
        if (title.length() > MAX_TITLE_LENGTH) return false;
        event.setTitle(title);

        if (event.getEvent_date() == null || event.getStart_time() == null || event.getEnd_time() == null) {
            return false;
        }
        if (!event.getEnd_time().isAfter(event.getStart_time())) {
            return false;
        }

        String location = event.getLocation() == null ? "" : event.getLocation().trim();
        if (location.length() > MAX_LOCATION_LENGTH) return false;
        event.setLocation(location);

        String color = event.getColor() == null ? "blue" : event.getColor().toLowerCase(Locale.ROOT);
        if (!VALID_COLORS.contains(color)) return false;
        event.setColor(color);

        return true;
    }
}
