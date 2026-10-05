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

        event.setStart_time(LocalTime.of(10, 0));
        event.setEnd_time(null);
        assertFalse(service.addEvent(3, event));
        assertEquals(0, repository.saveCalls);
    }

    @Test
    void rejectsInvalidUserIdsWithoutCallingRepository() {
        TrackingEventRepository repository = new TrackingEventRepository();
        AddEvent service = new AddEvent(repository);

        assertFalse(service.addEvent(0, event("Lecture")));
        assertFalse(service.addEvent(-1, event("Lecture")));
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
    void updatesValidEventWithoutChangingEventId() {
        TrackingEventRepository repository = new TrackingEventRepository();
        repository.updateResult = true;
        AddEvent service = new AddEvent(repository);
        Event event = event("  Workshop  ");
        event.setEvent_id(42);

        assertTrue(service.updateEvent(3, event));

        assertEquals(1, repository.updateCalls);
        assertEquals(42, repository.updatedEvent.getEvent_id());
        assertEquals("Workshop", repository.updatedEvent.getTitle());
    }

    @Test
    void updateReturnsFalseWhenRepositoryRejectsOwnership() {
        TrackingEventRepository repository = new TrackingEventRepository();
        repository.updateResult = false;
        Event event = event("Lecture");
        event.setEvent_id(42);

        assertFalse(new AddEvent(repository).updateEvent(9, event));
        assertEquals(1, repository.updateCalls);
    }

    @Test
    void rejectsInvalidUpdateTimeWithoutCallingRepository() {
        TrackingEventRepository repository = new TrackingEventRepository();
        Event event = event("Lecture");
        event.setEvent_id(42);
        event.setEnd_time(LocalTime.of(10, 0));

        assertFalse(new AddEvent(repository).updateEvent(3, event));
        assertEquals(0, repository.updateCalls);
    }

    @Test
    void deletesValidEvent() {
        TrackingEventRepository repository = new TrackingEventRepository();
        repository.deleteResult = true;

        assertTrue(new AddEvent(repository).deleteEvent(3, 42));

        assertEquals(1, repository.deleteCalls);
        assertEquals(3, repository.deletedUserId);
        assertEquals(42, repository.deletedEventId);
    }

    @Test
    void rejectsInvalidDeleteIdsWithoutCallingRepository() {
        TrackingEventRepository repository = new TrackingEventRepository();
        AddEvent service = new AddEvent(repository);

        assertFalse(service.deleteEvent(0, 42));
        assertFalse(service.deleteEvent(3, 0));
        assertFalse(service.deleteEvent(-1, 42));
        assertFalse(service.deleteEvent(3, -1));
        assertEquals(0, repository.deleteCalls);
    }

    @Test
    void deleteReturnsFalseWhenRepositoryRejectsOwnershipOrMissingEvent() {
        TrackingEventRepository repository = new TrackingEventRepository();
        repository.deleteResult = false;

        assertFalse(new AddEvent(repository).deleteEvent(9, 42));
        assertEquals(1, repository.deleteCalls);
    }

    @Test
    void returnsEventsFromRepository() {
        TrackingEventRepository repository = new TrackingEventRepository();
        List<Event> expected = List.of(event("Lecture"));
        repository.events = expected;

        assertSame(expected, new AddEvent(repository).getEventsBetween(3,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8)));
    }

    @Test
    void returnsNoEventsForInvalidUserWithoutCallingRepository() {
        TrackingEventRepository repository = new TrackingEventRepository();
        repository.events = List.of(event("Lecture"));

        assertTrue(new AddEvent(repository).getEventsBetween(0,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8)).isEmpty());
        assertEquals(0, repository.findCalls);
    }

    private static Event event(String title) {
        return new Event(0, title, LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalDate.of(2026, 1, 2), "Room 1");
    }

    private static class TrackingEventRepository extends eventRepository {
        private int saveCalls;
        private boolean saveResult;
        private int updateCalls;
        private boolean updateResult;
        private Event updatedEvent;
        private int deleteCalls;
        private boolean deleteResult;
        private int deletedUserId;
        private int deletedEventId;
        private int findCalls;
        private List<Event> events = List.of();

        TrackingEventRepository() { super(null); }
        @Override public boolean saveEvent(int userId, Event event) { saveCalls++; return saveResult; }
        @Override public boolean updateEvent(int userId, Event event) {
            updateCalls++;
            updatedEvent = event;
            return updateResult;
        }
        @Override public boolean deleteEvent(int userId, int eventId) {
            deleteCalls++;
            deletedUserId = userId;
            deletedEventId = eventId;
            return deleteResult;
        }
        @Override public List<Event> findEventsBetween(int userId, LocalDate start, LocalDate end) {
            findCalls++;
            return events;
        }
    }
}
