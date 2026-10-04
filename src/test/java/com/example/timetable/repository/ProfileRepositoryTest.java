package com.example.timetable.repository;

import com.example.timetable.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ProfileRepositoryTest {

    @Test
    void loadsProfileByUserId() throws Exception {
        AtomicInteger queriedUserId = new AtomicInteger();
        profileRepository repository = new profileRepository(connectionForLoad(
                new UserProfile(7, "Ada", "ada@example.com", "Computer Science"),
                queriedUserId
        ));

        UserProfile profile = repository.findById(7);

        assertEquals(7, queriedUserId.get());
        assertNotNull(profile);
        assertEquals(7, profile.getUserId());
        assertEquals("Ada", profile.getUsername());
        assertEquals("ada@example.com", profile.getEmail());
        assertEquals("Computer Science", profile.getMajor());
    }

    @Test
    void returnsNullWhenProfileDoesNotExist() throws Exception {
        profileRepository repository = new profileRepository(connectionForLoad(null, new AtomicInteger()));

        assertNull(repository.findById(404));
    }

    @Test
    void updatesOnlyProfileForGivenUserIdAndDoesNotTouchPassword() throws Exception {
        Map<Integer, UserProfile> users = new HashMap<>();
        users.put(1, new UserProfile(1, "Ada", "ada@example.com", "Physics"));
        users.put(2, new UserProfile(2, "Grace", "grace@example.com", "Math"));
        AtomicReference<String> sql = new AtomicReference<>();

        profileRepository repository = new profileRepository(connectionForUpdate(users, sql, null));

        assertTrue(repository.updateProfile(new UserProfile(
                1,
                "Ada Lovelace",
                "ada.lovelace@example.com",
                "Computer Science"
        )));

        assertEquals("Ada Lovelace", users.get(1).getUsername());
        assertEquals("ada.lovelace@example.com", users.get(1).getEmail());
        assertEquals("Computer Science", users.get(1).getMajor());
        assertEquals("Grace", users.get(2).getUsername());
        assertEquals("grace@example.com", users.get(2).getEmail());
        assertEquals("Math", users.get(2).getMajor());
        assertTrue(sql.get().contains("WHERE user_id = ?"));
        assertFalse(sql.get().contains("password_hash"));
    }

    @Test
    void returnsFalseWhenUpdateDoesNotMatchCurrentUser() throws Exception {
        Map<Integer, UserProfile> users = new HashMap<>();
        users.put(2, new UserProfile(2, "Grace", "grace@example.com", "Math"));
        profileRepository repository = new profileRepository(connectionForUpdate(users, new AtomicReference<>(), null));

        assertFalse(repository.updateProfile(new UserProfile(1, "Ada", "ada@example.com", "Physics")));
        assertEquals("Grace", users.get(2).getUsername());
    }

    @Test
    void propagatesDuplicateUsernameOrEmailError() {
        SQLIntegrityConstraintViolationException duplicate =
                new SQLIntegrityConstraintViolationException("Duplicate entry", "23000", 1062);
        profileRepository repository = new profileRepository(connectionForUpdate(new HashMap<>(),
                new AtomicReference<>(), duplicate));

        assertThrows(SQLIntegrityConstraintViolationException.class,
                () -> repository.updateProfile(new UserProfile(1, "Ada", "ada@example.com", null)));
    }

    private static Connection connectionForLoad(UserProfile profile, AtomicInteger userId) {
        ResultSet resultSet = proxy(ResultSet.class, (method, args) -> switch (method) {
            case "next" -> profile != null;
            case "getInt" -> profile == null ? 0 : profile.getUserId();
            case "getString" -> {
                if (profile == null) yield null;
                yield switch ((String) args[0]) {
                    case "username" -> profile.getUsername();
                    case "email" -> profile.getEmail();
                    case "major" -> profile.getMajor();
                    default -> null;
                };
            }
            default -> defaultValue(method);
        });

        PreparedStatement statement = proxy(PreparedStatement.class, (method, args) -> {
            if (method.equals("setInt")) userId.set((Integer) args[1]);
            if (method.equals("executeQuery")) return resultSet;
            return defaultValue(method);
        });

        return proxy(Connection.class, (method, args) ->
                method.equals("prepareStatement") ? statement : defaultValue(method));
    }

    private static Connection connectionForUpdate(
            Map<Integer, UserProfile> users,
            AtomicReference<String> sql,
            SQLException failure
    ) {
        Map<Integer, Object> parameters = new HashMap<>();
        PreparedStatement statement = proxy(PreparedStatement.class, (method, args) -> {
            if (method.equals("setString") || method.equals("setInt")) {
                parameters.put((Integer) args[0], args[1]);
            }
            if (method.equals("executeUpdate")) {
                if (failure != null) throw failure;
                int userId = (Integer) parameters.get(4);
                if (!users.containsKey(userId)) return 0;
                users.put(userId, new UserProfile(
                        userId,
                        (String) parameters.get(1),
                        (String) parameters.get(2),
                        (String) parameters.get(3)
                ));
                return 1;
            }
            return defaultValue(method);
        });

        return proxy(Connection.class, (method, args) -> {
            if (method.equals("prepareStatement")) {
                sql.set((String) args[0]);
                return statement;
            }
            return defaultValue(method);
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
