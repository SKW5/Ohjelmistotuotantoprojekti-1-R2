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

    public boolean saveEvent(Event event) {
        String sql = """
                INSERT INTO student_timetable.timetable_events (user_id, course_id, title, start_time, end_time, event_date, location) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, 1); // Assuming user_id is 1 for now
            statement.setInt(2, 1);
            statement.setString(3, event.getTitle());
            statement.setTime(4, java.sql.Time.valueOf(event.getStart_time()));
            statement.setTime(5, java.sql.Time.valueOf(event.getEnd_time()));
            statement.setDate(6, java.sql.Date.valueOf(event.getEvent_date()));
            statement.setString(7, event.getLocation());

            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("Error saving event: " + e.getMessage());
            return false;
        }
    }

    public List<Event> findEventsBetween(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT event_id, title, start_time, end_time, event_date, location, course_id
                FROM student_timetable.timetable_events
                WHERE event_date >= ? AND event_date < ?
                ORDER BY event_date, start_time
                """;
        List<Event> events = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    events.add(new Event(
                            result.getInt("event_id"),
                            result.getString("title"),
                            result.getTime("start_time").toLocalTime(),
                            result.getTime("end_time").toLocalTime(),
                            result.getDate("event_date").toLocalDate(),
                            result.getString("location"),
                            result.getInt("course_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading events: " + e.getMessage());
        }
        return events;
    }
}
