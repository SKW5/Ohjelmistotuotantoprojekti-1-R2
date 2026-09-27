package com.example.timetable;

import com.example.timetable.service.passwordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {
    @Test
    void hashesAndVerifiesPassword() {
        String hash = passwordUtil.hashPassword("correct horse battery staple");

        assertNotEquals("correct horse battery staple", hash);
        assertTrue(passwordUtil.verifyPassword("correct horse battery staple", hash));
        assertFalse(passwordUtil.verifyPassword("wrong password", hash));
    }

    @Test
    void usesDifferentSaltForEachHash() {
        assertNotEquals(passwordUtil.hashPassword("same"), passwordUtil.hashPassword("same"));
    }
}
