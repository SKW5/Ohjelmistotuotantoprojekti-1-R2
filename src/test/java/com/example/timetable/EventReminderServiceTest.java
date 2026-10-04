package com.example.timetable;

import com.example.timetable.model.Event;
import com.example.timetable.repository.eventRepository;
import com.example.timetable.service.AddEvent;
import com.example.timetable.service.EventReminderService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventReminderServiceTest {

    @Test
    void notifiesOnceForAnEventStartingWithinFifteenMinutes() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 9, 0);
        Event upcoming = event(17, "Lecture", now.plusMinutes(10));
        Event past = event(18, "Past event", now.minusMinutes(1));
        Event tooFarAway = event(19, "Later event", now.plusMinutes(16));
        List<Event> notifications = new ArrayList<>();
        EventReminderService service = reminderService(
                List.of(upcoming, past, tooFarAway), notifications
        );

        service.checkUpcomingEvents(5, now);
        service.checkUpcomingEvents(5, now.plusSeconds(30));

        assertEquals(List.of(upcoming), notifications);
    }

    @Test
    void respectsReminderToggleAndIgnoresInvalidUsers() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 9, 0);
        Event upcoming = event(17, "Lecture", now.plusMinutes(10));
        List<Event> notifications = new ArrayList<>();
        EventReminderService service = reminderService(List.of(upcoming), notifications);

        service.setEnabled(false);
        service.checkUpcomingEvents(5, now);
        service.setEnabled(true);
        service.checkUpcomingEvents(0, now);
        service.checkUpcomingEvents(5, now);

        assertEquals(List.of(upcoming), notifications);
    }

    private EventReminderService reminderService(List<Event> events, List<Event> notifications) {
        eventRepository repository = new eventRepository(null) {
            @Override
            public List<Event> findEventsBetween(int userId, java.time.LocalDate start, java.time.LocalDate end) {
                return events;
            }
        };
        return new EventReminderService(new AddEvent(repository), notifications::add);
    }

    private Event event(int id, String title, LocalDateTime start) {
        return new Event(id, title, start.toLocalTime(), start.toLocalTime().plusHours(1),
                start.toLocalDate(), "Room 1");
    }
}