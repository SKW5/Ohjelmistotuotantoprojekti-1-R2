package com.example.timetable.repository;

import com.example.timetable.model.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            statement.setInt(2, event.getCourse_id());
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
}
