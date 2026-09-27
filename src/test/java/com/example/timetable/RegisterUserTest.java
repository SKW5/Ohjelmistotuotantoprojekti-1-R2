package com.example.timetable;

import com.example.timetable.model.User;
import com.example.timetable.repository.register;
import com.example.timetable.service.RegisterUser;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class RegisterUserTest {
    @Test
    void rejectsMissingRequiredFieldsWithoutCallingRepository() throws SQLException {
        TrackingRegister repository = new TrackingRegister();
        RegisterUser service = new RegisterUser(repository);

        assertFalse(service.registerUser(new User(null, "a@b.com", "secret")));
        assertFalse(service.registerUser(new User("name", "", "secret")));
        assertFalse(service.registerUser(new User("name", "a@b.com", null)));
        assertEquals(0, repository.calls);
    }

    @Test
    void delegatesValidUserAndReturnsRepositoryResult() throws SQLException {
        TrackingRegister repository = new TrackingRegister();
        repository.result = true;

        assertTrue(new RegisterUser(repository).registerUser(new User("Ada", "ada@example.com", "secret")));
        assertEquals(1, repository.calls);
    }

    private static class TrackingRegister extends register {
        private int calls;
        private boolean result;
        TrackingRegister() { super(null); }
        @Override public boolean registerUser(User user) { calls++; return result; }
    }
}
