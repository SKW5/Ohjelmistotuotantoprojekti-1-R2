package com.example.timetable;

import com.example.timetable.service.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {
    @Test
    void hashesAndVerifiesPassword() {
        String hash = PasswordUtil.hashPassword("correct horse battery staple");

        assertNotEquals("correct horse battery staple", hash);
        assertTrue(PasswordUtil.verifyPassword("correct horse battery staple", hash));
        assertFalse(PasswordUtil.verifyPassword("wrong password", hash));
    }

    @Test
    void usesDifferentSaltForEachHash() {
        assertNotEquals(PasswordUtil.hashPassword("same"), PasswordUtil.hashPassword("same"));
    }
}
