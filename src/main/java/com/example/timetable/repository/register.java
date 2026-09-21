package com.example.timetable.repository;

import com.example.timetable.model.User;
import com.example.timetable.service.passwordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class register {

    private final Connection connection;

    public register(Connection connection) {
        this.connection = connection;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO student_timetable.users (username, email ,password_hash) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String hashedPassword = passwordUtil.hashPassword(user.getPassword_hash());
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, hashedPassword);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }

    }
}
