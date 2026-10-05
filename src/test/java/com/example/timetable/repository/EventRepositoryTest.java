package com.example.timetable.repository;

import com.example.timetable.model.Event;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class EventRepositoryTest {
    @Test
    void savesEventWithExpectedParameters() {
        Map<Integer, Object> parameters = new HashMap<>();
        AtomicReference<String> sql = new AtomicReference<>();
        eventRepository repository = new eventRepository(connectionForSave(sql, parameters, false));
        Event event = event("Review", LocalDate.of(2026, 10, 2));

        assertTrue(repository.saveEvent(3, event));
        assertTrue(sql.get().contains("INSERT INTO student_timetable.timetable_events"));
        assertEquals(3, parameters.get(1));
        assertEquals("Review", parameters.get(2));
        assertEquals(Time.valueOf(LocalTime.of(9, 30)), parameters.get(3));
        assertEquals(Time.valueOf(LocalTime.of(10, 30)), parameters.get(4));
        assertEquals(Date.valueOf(LocalDate.of(2026, 10, 2)), parameters.get(5));
        assertEquals("Room 3", parameters.get(6));
        assertEquals("blue", parameters.get(7));
    }

    @Test
    void saveReturnsFalseWhenDatabaseFails() {
        eventRepository repository = new eventRepository(connectionForSave(new AtomicReference<>(),
                new HashMap<>(), true));

        assertFalse(repository.saveEvent(3, event("Review", LocalDate.of(2026, 10, 2))));
    }

    @Test
    void updatesEventWithExpectedParametersAndOwnerCondition() {
        Map<Integer, Object> parameters = new HashMap<>();
        AtomicReference<String> sql = new AtomicReference<>();
        eventRepository repository = new eventRepository(connectionForUpdate(sql, parameters, 1, false));
        Event event = event("Updated review", LocalDate.of(2026, 10, 3));
        event.setEvent_id(55);
        event.setColor("purple");

        assertTrue(repository.updateEvent(7, event));

        assertTrue(sql.get().contains("UPDATE student_timetable.timetable_events"));
        assertTrue(sql.get().contains("WHERE event_id = ? AND user_id = ?"));
        assertEquals("Updated review", parameters.get(1));
        assertEquals(Time.valueOf(LocalTime.of(9, 30)), parameters.get(2));
        assertEquals(Time.valueOf(LocalTime.of(10, 30)), parameters.get(3));
        assertEquals(Date.valueOf(LocalDate.of(2026, 10, 3)), parameters.get(4));
        assertEquals("Room 3", parameters.get(5));
        assertEquals("purple", parameters.get(6));
        assertEquals(55, parameters.get(7));
        assertEquals(7, parameters.get(8));
    }

    @Test
    void updateReturnsFalseWhenNoRowsWereUpdated() {
        eventRepository repository = new eventRepository(connectionForUpdate(new AtomicReference<>(),
                new HashMap<>(), 0, false));
        Event event = event("Review", LocalDate.of(2026, 10, 2));
        event.setEvent_id(55);

        assertFalse(repository.updateEvent(99, event));
    }

    @Test
    void updateReturnsFalseWhenDatabaseFails() {
        eventRepository repository = new eventRepository(connectionForUpdate(new AtomicReference<>(),
                new HashMap<>(), 1, true));
        Event event = event("Review", LocalDate.of(2026, 10, 2));
        event.setEvent_id(55);

        assertFalse(repository.updateEvent(3, event));
    }

    @Test
    void deletesEventWithExpectedParametersAndOwnerCondition() {
        Map<Integer, Object> parameters = new HashMap<>();
        AtomicReference<String> sql = new AtomicReference<>();
        eventRepository repository = new eventRepository(connectionForUpdate(sql, parameters, 1, false));

        assertTrue(repository.deleteEvent(7, 55));

        assertTrue(sql.get().contains("DELETE FROM student_timetable.timetable_events"));
        assertTrue(sql.get().contains("WHERE event_id = ? AND user_id = ?"));
        assertEquals(55, parameters.get(1));
        assertEquals(7, parameters.get(2));
    }

    @Test
    void deleteReturnsFalseWhenNoRowsWereDeleted() {
        eventRepository repository = new eventRepository(connectionForUpdate(new AtomicReference<>(),
                new HashMap<>(), 0, false));

        assertFalse(repository.deleteEvent(99, 55));
    }

    @Test
    void deleteReturnsFalseWhenDatabaseFails() {
        eventRepository repository = new eventRepository(connectionForUpdate(new AtomicReference<>(),
                new HashMap<>(), 1, true));

        assertFalse(repository.deleteEvent(3, 55));
    }

    @Test
    void loadsRowsAndUsesHalfOpenDateRange() {
        Map<Integer, Object> parameters = new HashMap<>();
        List<Map<String, Object>> rows = List.of(row(7, "Review", LocalDate.of(2026, 10, 2)));
        eventRepository repository = new eventRepository(connectionForQuery(parameters, rows, false));

        List<Event> events = repository.findEventsBetween(3,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 8));

        assertEquals(3, parameters.get(1));
        assertEquals(Date.valueOf(LocalDate.of(2026, 10, 1)), parameters.get(2));
        assertEquals(Date.valueOf(LocalDate.of(2026, 10, 8)), parameters.get(3));
        assertEquals(1, events.size());
        Event event = events.get(0);
        assertEquals(7, event.getEvent_id());
        assertEquals("Review", event.getTitle());
        assertEquals(LocalTime.of(9, 30), event.getStart_time());
        assertEquals(LocalTime.of(10, 30), event.getEnd_time());
        assertEquals(LocalDate.of(2026, 10, 2), event.getEvent_date());
        assertEquals("Room 3", event.getLocation());
    }

    @Test
    void queryFailureReturnsEmptyList() {
        eventRepository repository = new eventRepository(connectionForQuery(new HashMap<>(), List.of(), true));

        assertTrue(repository.findEventsBetween(3, LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 8)).isEmpty());
    }

    private static Event event(String title, LocalDate date) {
        return new Event(0, title, LocalTime.of(9, 30), LocalTime.of(10, 30), date, "Room 3");
    }

    private static Map<String, Object> row(int id, String title, LocalDate date) {
        return Map.of("event_id", id, "title", title, "start_time", Time.valueOf(LocalTime.of(9, 30)),
                "end_time", Time.valueOf(LocalTime.of(10, 30)), "event_date", Date.valueOf(date),
                "location", "Room 3");
    }

    private static Connection connectionForSave(AtomicReference<String> sql, Map<Integer, Object> parameters,
                                                boolean fail) {
        PreparedStatement statement = proxy(PreparedStatement.class, (method, args) -> {
            if (method.startsWith("set")) parameters.put((Integer) args[0], args[1]);
            if (method.equals("executeUpdate") && fail) throw new SQLException("write failed");
            return method.equals("executeUpdate") ? 1 : null;
        });
        return proxy(Connection.class, (method, args) -> {
            if (method.equals("prepareStatement")) {
                sql.set((String) args[0]);
                if (fail) throw new SQLException("prepare failed");
                return statement;
            }
            return null;
        });
    }

    private static Connection connectionForUpdate(AtomicReference<String> sql, Map<Integer, Object> parameters,
                                                  int updatedRows, boolean fail) {
        PreparedStatement statement = proxy(PreparedStatement.class, (method, args) -> {
            if (method.startsWith("set")) parameters.put((Integer) args[0], args[1]);
            if (method.equals("executeUpdate") && fail) throw new SQLException("write failed");
            return method.equals("executeUpdate") ? updatedRows : null;
        });
        return proxy(Connection.class, (method, args) -> {
            if (method.equals("prepareStatement")) {
                sql.set((String) args[0]);
                if (fail) throw new SQLException("prepare failed");
                return statement;
            }
            return null;
        });
    }

    private static Connection connectionForQuery(Map<Integer, Object> parameters,
                                                 List<Map<String, Object>> rows, boolean fail) {
        AtomicReference<Integer> index = new AtomicReference<>(-1);
        ResultSet result = proxy(ResultSet.class, (method, args) -> {
            if (method.equals("next")) {
                int next = index.get() + 1;
                index.set(next);
                return next < rows.size();
            }
            if (method.startsWith("get")) {
                Object value = rows.get(index.get()).get(args[0]);
                if (method.equals("getInt")) return value;
                return value;
            }
            return null;
        });
        PreparedStatement statement = proxy(PreparedStatement.class, (method, args) -> {
            if (method.startsWith("set")) parameters.put((Integer) args[0], args[1]);
            if (method.equals("executeQuery")) {
                if (fail) throw new SQLException("query failed");
                return result;
            }
            return null;
        });
        return proxy(Connection.class, (method, args) -> {
            if (method.equals("prepareStatement")) {
                if (fail) throw new SQLException("prepare failed");
                return statement;
            }
            return null;
        });
    }

    private interface Call { Object invoke(String method, Object[] args) throws Throwable; }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Call call) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> call.invoke(method.getName(), args));
    }
}
