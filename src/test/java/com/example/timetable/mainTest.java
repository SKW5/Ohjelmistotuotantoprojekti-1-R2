package com.example.timetable;

import com.example.timetable.Main;
import com.example.timetable.ui.SettingsView;
import com.example.timetable.ui.TimetableView;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class mainTest {

    private static void runOnFxThread(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            
        }
    }

    @Test
    void start_shouldBuildSceneAndLoadTimetable() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                Main app = new Main();
                Stage stage = new Stage();

                app.start(stage);

                assertEquals("Student Timetable", stage.getTitle());
                assertEquals(950, stage.getMinWidth(), 0.0);
                assertEquals(600, stage.getMinHeight(), 0.0);
                assertNotNull(stage.getScene());

                BorderPane root = getPrivateField(app, "root", BorderPane.class);
                StackPane content = getPrivateField(app, "content", StackPane.class);

                assertEquals(root, stage.getScene().getRoot());
                assertEquals(content, root.getCenter());
                assertEquals(1, content.getChildren().size());
                assertTrue(content.getChildren().get(0) instanceof TimetableView);

                stage.close();

            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    void showTimetable_shouldDisplayTimetableView() throws Exception {
        Main app = new Main();
        StackPane content = getPrivateField(app, "content", StackPane.class);

        invokePrivate(app, "showTimetable");

        assertEquals(1, content.getChildren().size());
        assertTrue(content.getChildren().get(0) instanceof TimetableView);
    }

    @Test
    void showSettings_shouldDisplaySettingsView() throws Exception {
        Main app = new Main();
        StackPane content = getPrivateField(app, "content", StackPane.class);

        invokePrivate(app, "showSettings");

        assertEquals(1, content.getChildren().size());
        assertTrue(content.getChildren().get(0) instanceof SettingsView);
    }

    private static <T> T getPrivateField(Object instance, String fieldName, Class<T> type)
            throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(instance));
    }

    private static void invokePrivate(Object instance, String methodName) throws Exception {
        Method method = instance.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(instance);
    }
}