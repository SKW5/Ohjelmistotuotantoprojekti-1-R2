package com.example.timetable;

import javafx.application.Application;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class mainTest {
    @Test
    void mainIsJavaFxApplicationEntryPoint() throws Exception {
        assertTrue(Application.class.isAssignableFrom(Main.class));
        Method main = Main.class.getMethod("main", String[].class);
        assertTrue(java.lang.reflect.Modifier.isStatic(main.getModifiers()));
    }
}
