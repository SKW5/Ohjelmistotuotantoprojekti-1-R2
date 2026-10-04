package com.example.timetable.service;

import com.example.timetable.model.Event;
import com.example.timetable.repository.eventRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventReminderServiceTest {

    @Test
    void remindsOnceWhenEventStartsWithinFifteenMinutes() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 9, 0);
        Event event = new Event(12, "Lecture", LocalTime.of(9, 15), LocalTime.of(10, 0),
                now.toLocalDate(), "Room 2");
        AddEvent addEventService = new AddEvent(new eventRepository(null) {
            @Override
            public List<Event> findEventsBetween(int userId, LocalDate start, LocalDate end) {
                return List.of(event);
            }
        });
        AtomicInteger reminders = new AtomicInteger();
        EventReminderService service = new EventReminderService(addEventService, ignored -> reminders.incrementAndGet());

        service.checkUpcomingEvents(7, now);
        service.checkUpcomingEvents(7, now.plusMinutes(1));
        service.checkUpcomingEvents(8, now);

        assertEquals(2, reminders.get());
    }

    @Test
    void includesEventsStartingShortlyAfterMidnight() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 23, 50);
        Event event = new Event(13, "Morning lecture", LocalTime.of(0, 5), LocalTime.of(1, 0),
                now.toLocalDate().plusDays(1), "Room 4");
        AtomicInteger reminders = new AtomicInteger();
        AddEvent addEventService = new AddEvent(new eventRepository(null) {
            @Override
            public List<Event> findEventsBetween(int userId, LocalDate start, LocalDate end) {
                assertEquals(now.toLocalDate(), start);
                assertEquals(now.toLocalDate().plusDays(2), end);
                return List.of(event);
            }
        });
        EventReminderService service = new EventReminderService(addEventService, ignored -> reminders.incrementAndGet());

        service.checkUpcomingEvents(7, now);

        assertEquals(1, reminders.get());
    }

    @Test
    void doesNotQueryEventsForUnauthenticatedUser() {
        AtomicInteger queries = new AtomicInteger();
        AddEvent addEventService = new AddEvent(new eventRepository(null) {
            @Override
            public List<Event> findEventsBetween(int userId, LocalDate start, LocalDate end) {
                queries.incrementAndGet();
                return List.of();
            }
        });
        EventReminderService service = new EventReminderService(addEventService, ignored -> { });

        service.checkUpcomingEvents(0, LocalDateTime.of(2026, 10, 4, 9, 0));

        assertEquals(0, queries.get());
    }
}