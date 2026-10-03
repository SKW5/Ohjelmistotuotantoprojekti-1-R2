package com.example.timetable.service;
import com.example.timetable.model.Event;
import com.example.timetable.repository.eventRepository;

import java.time.LocalDate;
import java.util.List;

public class AddEvent {

    private final eventRepository eventRepository;


    public AddEvent(eventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public boolean addEvent(int userId, Event event) {
        if (userId <= 0) return false;
        if (event.getTitle() == null || event.getTitle().isEmpty()) {
            System.err.println("Event title cannot be empty.");
            return false;
        }
        if (event.getStart_time() == null || event.getEnd_time() == null) {
            return false;
        }

        return eventRepository.saveEvent(userId, event);
    }

    public List<Event> getEventsBetween(int userId, LocalDate startDate, LocalDate endDate) {
        if (userId <= 0) return List.of();
        return eventRepository.findEventsBetween(userId, startDate, endDate);
    }
}
