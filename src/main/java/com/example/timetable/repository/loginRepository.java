package com.example.timetable.repository;

import com.example.timetable.service.passwordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class loginRepository {

    private final Connection connection;

    public loginRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean loginUser(String email, String password) {
        String sql = "SELECT password_hash FROM student_timetable.users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        && passwordUtil.verifyPassword(password, result.getString("password_hash"));
            }
        } catch (SQLException e) {
            System.err.println("Error logging in: " + e.getMessage());
            return false;
        }
    }

    public String getUserNameByEmail(String email) {
        String sql = "SELECT username FROM student_timetable.users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("username");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching username: " + e.getMessage());
        }

        return null;
    }

    public int getUserIdByEmail(String email) {
        String sql = "SELECT user_id FROM student_timetable.users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt("user_id") : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user ID: " + e.getMessage());
            return 0;
        }
    }
}
