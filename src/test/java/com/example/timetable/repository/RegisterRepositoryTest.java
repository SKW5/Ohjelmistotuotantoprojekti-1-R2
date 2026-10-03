package com.example.timetable.repository;

import com.example.timetable.model.User;
import com.example.timetable.service.passwordUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRepositoryTest {
    @Test
    void storesUserFieldsAndHashedPassword() throws Exception {
        Map<Integer, Object> parameters = new HashMap<>();
        register repository = new register(connectionCapturing(parameters));

        assertTrue(repository.registerUser(new User("Ada", "ada@example.com", "secret")));
        assertEquals("Ada", parameters.get(1));
        assertEquals("ada@example.com", parameters.get(2));
        String storedHash = (String) parameters.get(3);
        assertNotEquals("secret", storedHash);
        assertTrue(passwordUtil.verifyPassword("secret", storedHash));
    }

    @Test
    void propagatesDatabaseFailure() {
        Connection connection = proxy(Connection.class, (method, args) -> {
            if (method.equals("prepareStatement")) throw new SQLException("connection failed");
            return null;
        });

        assertThrows(SQLException.class, () -> new register(connection)
                .registerUser(new User("Ada", "ada@example.com", "secret")));
    }

    private static Connection connectionCapturing(Map<Integer, Object> parameters) {
        PreparedStatement statement = proxy(PreparedStatement.class, (method, args) -> {
            if (method.equals("setString")) parameters.put((Integer) args[0], args[1]);
            if (method.equals("executeUpdate")) return 1;
            return null;
        });
        return proxy(Connection.class, (method, args) ->
                method.equals("prepareStatement") ? statement : null);
    }

    private interface Call { Object invoke(String method, Object[] args) throws Throwable; }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Call call) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> call.invoke(method.getName(), args));
    }
}
