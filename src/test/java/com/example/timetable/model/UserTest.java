package com.example.timetable.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {
    @Test
    void exposesValuesPassedToConstructor() {
        User user = new User("Ada", "ada@example.com", "password-hash");

        assertEquals("Ada", user.getUsername());
        assertEquals("ada@example.com", user.getEmail());
        assertEquals("password-hash", user.getPassword_hash());
    }
}
