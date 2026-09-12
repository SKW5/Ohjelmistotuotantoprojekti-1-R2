package com.example.timetable.service;
import com.example.timetable.model.Event;
import com.example.timetable.repository.eventRepository;

public class AddEvent {

    private final eventRepository eventRepository;


    public AddEvent(eventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public boolean addEvent(Event event) {
        if (event.getTitle() == null || event.getTitle().isEmpty()) {
            System.err.println("Event title cannot be empty.");
            return false;
        }
        if (event.getStart_time() == null || event.getEnd_time() == null) {
            return false;
        }

        return eventRepository.saveEvent(event);
    }
}