package com.example.timetable.repository;

import com.example.timetable.service.passwordUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class LoginRepositoryTest {
    @Test
    void loginChecksPasswordForFoundAccount() {
        String hash = passwordUtil.hashPassword("secret");
        AtomicReference<String> queriedEmail = new AtomicReference<>();
        loginRepository repository = new loginRepository(connectionReturning(
                resultSet(true, hash, null), queriedEmail, null));

        assertTrue(repository.loginUser("ada@example.com", "secret"));
        assertEquals("ada@example.com", queriedEmail.get());
    }

    @Test
    void loginRejectsWrongPasswordAndMissingAccount() {
        loginRepository wrongPassword = new loginRepository(connectionReturning(
                resultSet(true, passwordUtil.hashPassword("secret"), null), new AtomicReference<>(), null));
        loginRepository missingUser = new loginRepository(connectionReturning(
                resultSet(false, null, null), new AtomicReference<>(), null));

        assertFalse(wrongPassword.loginUser("ada@example.com", "incorrect"));
        assertFalse(missingUser.loginUser("missing@example.com", "secret"));
    }

    @Test
    void loginReturnsFalseWhenDatabaseThrows() {
        loginRepository repository = new loginRepository(connectionReturning(null, new AtomicReference<>(),
                new SQLException("connection failed")));

        assertFalse(repository.loginUser("ada@example.com", "secret"));
    }

    @Test
    void usernameLookupReturnsNameOrNull() {
        AtomicReference<String> queriedEmail = new AtomicReference<>();
        loginRepository found = new loginRepository(connectionReturning(
                resultSet(true, null, "Ada"), queriedEmail, null));
        loginRepository missing = new loginRepository(connectionReturning(
                resultSet(false, null, null), new AtomicReference<>(), null));

        assertEquals("Ada", found.getUserNameByEmail("ada@example.com"));
        assertEquals("ada@example.com", queriedEmail.get());
        assertNull(missing.getUserNameByEmail("missing@example.com"));
    }

    private static Connection connectionReturning(ResultSet results, AtomicReference<String> email,
                                                  SQLException failure) {
        PreparedStatement statement = proxy(PreparedStatement.class, (method, args) -> {
            if (method.equals("setString")) email.set((String) args[1]);
            if (method.equals("executeQuery")) return results;
            return defaultValue(method);
        });
        return proxy(Connection.class, (method, args) -> {
            if (method.equals("prepareStatement")) {
                if (failure != null) throw failure;
                return statement;
            }
            return defaultValue(method);
        });
    }

    private static ResultSet resultSet(boolean hasRow, String hash, String username) {
        return proxy(ResultSet.class, (method, args) -> switch (method) {
            case "next" -> hasRow;
            case "getString" -> "password_hash".equals(args[0]) ? hash : username;
            default -> defaultValue(method);
        });
    }

    private interface Call { Object invoke(String method, Object[] args) throws Throwable; }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Call call) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> call.invoke(method.getName(), args));
    }

    private static Object defaultValue(String method) {
        return switch (method) {
            case "executeUpdate" -> 1;
            case "isClosed", "isWrapperFor" -> false;
            case "unwrap" -> null;
            default -> null;
        };
    }
}
