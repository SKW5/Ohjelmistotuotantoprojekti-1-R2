package com.example.timetable.repository;

import com.example.timetable.model.UserProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class profileRepository {

    private final Connection connection;
    private final ConnectionProvider connectionProvider;

    public profileRepository(Connection connection) {
        this.connection = connection;
        this.connectionProvider = null;
    }

    public profileRepository(ConnectionProvider connectionProvider) {
        this.connection = null;
        this.connectionProvider = connectionProvider;
    }

    public UserProfile findById(int userId) throws SQLException {
        String sql = """
                SELECT user_id, username, email, major
                FROM student_timetable.users
                WHERE user_id = ?
                """;

        return withConnection(database -> {
            try (PreparedStatement statement = database.prepareStatement(sql)) {
                statement.setInt(1, userId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return null;
                    }
                    return new UserProfile(
                            result.getInt("user_id"),
                            result.getString("username"),
                            result.getString("email"),
                            result.getString("major")
                    );
                }
            }
        });
    }

    public boolean updateProfile(UserProfile profile) throws SQLException {
        String sql = """
                UPDATE student_timetable.users
                SET username = ?, email = ?, major = ?
                WHERE user_id = ?
                """;

        return withConnection(database -> {
            try (PreparedStatement statement = database.prepareStatement(sql)) {
                statement.setString(1, profile.getUsername());
                statement.setString(2, profile.getEmail());
                statement.setString(3, profile.getMajor());
                statement.setInt(4, profile.getUserId());
                return statement.executeUpdate() == 1;
            }
        });
    }

    private <T> T withConnection(SqlWork<T> work) throws SQLException {
        if (connection != null) {
            synchronized (connection) {
                return work.run(connection);
            }
        }

        try (Connection database = connectionProvider.open()) {
            return work.run(database);
        }
    }

    @FunctionalInterface
    public interface ConnectionProvider {
        Connection open() throws SQLException;
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }
}