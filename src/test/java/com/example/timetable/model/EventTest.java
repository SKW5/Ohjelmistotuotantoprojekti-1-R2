package com.example.timetable.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventTest {
    @Test
    void exposesConstructorValuesAndUpdatesMutableFields() {
        Event event = new Event(4, "Lecture", LocalTime.of(9, 0), LocalTime.of(10, 30),
                LocalDate.of(2026, 9, 27), "Room A");

        assertEquals(4, event.getEvent_id());
        assertEquals("Lecture", event.getTitle());
        assertEquals(LocalTime.of(9, 0), event.getStart_time());
        assertEquals(LocalTime.of(10, 30), event.getEnd_time());
        assertEquals(LocalDate.of(2026, 9, 27), event.getEvent_date());
        assertEquals("Room A", event.getLocation());
        event.setEvent_id(5);
        event.setTitle("Seminar");
        event.setStart_time(LocalTime.NOON);
        event.setEnd_time(LocalTime.of(13, 0));
        event.setEvent_date(LocalDate.of(2026, 9, 28));
        event.setLocation("Room B");

        assertEquals(5, event.getEvent_id());
        assertEquals("Seminar", event.getTitle());
        assertEquals(LocalTime.NOON, event.getStart_time());
        assertEquals(LocalTime.of(13, 0), event.getEnd_time());
        assertEquals(LocalDate.of(2026, 9, 28), event.getEvent_date());
        assertEquals("Room B", event.getLocation());
    }
}
