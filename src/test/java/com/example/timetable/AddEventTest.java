package com.example.timetable;

import com.example.timetable.model.Event;
import com.example.timetable.repository.eventRepository;
import com.example.timetable.service.AddEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddEventTest {
    @Test
    void rejectsNullOrEmptyTitleWithoutSaving() {
        TrackingEventRepository repository = new TrackingEventRepository();
        AddEvent service = new AddEvent(repository);

        assertFalse(service.addEvent(3, event(null)));
        assertFalse(service.addEvent(3, event("")));
        assertEquals(0, repository.saveCalls);
    }

    @Test
    void rejectsMissingTimesWithoutSaving() {
        TrackingEventRepository repository = new TrackingEventRepository();
        AddEvent service = new AddEvent(repository);
        Event event = event("Lecture");
        event.setStart_time(null);

        assertFalse(service.addEvent(3, event));
        assertEquals(0, repository.saveCalls);
    }

    @Test
    void savesValidEventAndReturnsRepositoryResult() {
        TrackingEventRepository repository = new TrackingEventRepository();
        repository.saveResult = true;

        assertTrue(new AddEvent(repository).addEvent(3, event("Lecture")));
        assertEquals(1, repository.saveCalls);
    }

    @Test
    void returnsEventsFromRepository() {
        TrackingEventRepository repository = new TrackingEventRepository();
        List<Event> expected = List.of(event("Lecture"));
        repository.events = expected;

        assertSame(expected, new AddEvent(repository).getEventsBetween(3,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8)));
    }

    private static Event event(String title) {
        return new Event(0, title, LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalDate.of(2026, 1, 2), "Room 1", 1);
    }

    private static class TrackingEventRepository extends eventRepository {
        private int saveCalls;
        private boolean saveResult;
        private List<Event> events = List.of();

        TrackingEventRepository() { super(null); }
        @Override public boolean saveEvent(int userId, Event event) { saveCalls++; return saveResult; }
        @Override public List<Event> findEventsBetween(int userId, LocalDate start, LocalDate end) { return events; }
    }
}
