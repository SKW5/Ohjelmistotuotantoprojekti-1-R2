package com.example.timetable.repository;

import com.example.timetable.model.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class eventRepository {

    private final Connection connection;

    public eventRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean saveEvent(int userId, Event event) {
        String sql = """
                INSERT INTO student_timetable.timetable_events (user_id, title, start_time, end_time, event_date, location, color)
                VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            synchronized (connection) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {

                    statement.setInt(1, userId);
                    statement.setString(2, event.getTitle());
                    statement.setTime(3, java.sql.Time.valueOf(event.getStart_time()));
                    statement.setTime(4, java.sql.Time.valueOf(event.getEnd_time()));
                    statement.setDate(5, java.sql.Date.valueOf(event.getEvent_date()));
                    statement.setString(6, event.getLocation());
                    statement.setString(7, event.getColor());

                    statement.executeUpdate();
                }
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error saving event: " + e.getMessage());
            return false;
        }
    }

    public boolean updateEvent(int userId, Event event) {
        String sql = """
                UPDATE student_timetable.timetable_events
                SET title = ?, start_time = ?, end_time = ?, event_date = ?, location = ?, color = ?
                WHERE event_id = ? AND user_id = ?
                """;

        try {
            synchronized (connection) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, event.getTitle());
                    statement.setTime(2, java.sql.Time.valueOf(event.getStart_time()));
                    statement.setTime(3, java.sql.Time.valueOf(event.getEnd_time()));
                    statement.setDate(4, java.sql.Date.valueOf(event.getEvent_date()));
                    statement.setString(5, event.getLocation());
                    statement.setString(6, event.getColor());
                    statement.setInt(7, event.getEvent_id());
                    statement.setInt(8, userId);

                    return statement.executeUpdate() == 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteEvent(int userId, int eventId) {
        String sql = """
                DELETE FROM student_timetable.timetable_events
                WHERE event_id = ? AND user_id = ?
                """;

        try {
            synchronized (connection) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, eventId);
                    statement.setInt(2, userId);

                    return statement.executeUpdate() == 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
            return false;
        }
    }

    public List<Event> findEventsBetween(int userId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT event_id, title, start_time, end_time, event_date, location, color
                FROM student_timetable.timetable_events
                WHERE user_id = ? AND event_date >= ? AND event_date < ?
                ORDER BY event_date, start_time
                """;
        List<Event> events = new ArrayList<>();

        try {
            synchronized (connection) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, userId);
                    statement.setDate(2, java.sql.Date.valueOf(startDate));
                    statement.setDate(3, java.sql.Date.valueOf(endDate));
                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            events.add(new Event(
                                    result.getInt("event_id"),
                                    result.getString("title"),
                                    result.getTime("start_time").toLocalTime(),
                                    result.getTime("end_time").toLocalTime(),
                                    result.getDate("event_date").toLocalDate(),
                                    result.getString("location"),
                                    result.getString("color")
                            ));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading events: " + e.getMessage());
        }
        return events;
    }
}
