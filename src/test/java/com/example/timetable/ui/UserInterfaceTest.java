package com.example.timetable.ui;

import com.example.timetable.model.Event;
import com.example.timetable.Main;
import com.example.timetable.repository.eventRepository;
import com.example.timetable.repository.loginRepository;
import com.example.timetable.service.AddEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class UserInterfaceTest {
    @BeforeAll
    static void startJavaFx() throws Exception {
        FxTestSupport.startToolkit();
    }

    @AfterAll
    static void stopJavaFx() throws Exception {
        FxTestSupport.shutdownToolkit();
    }

    @Test
    void landingButtonsInvokeTheirActions() throws Exception {
        AtomicInteger logins = new AtomicInteger();
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger skips = new AtomicInteger();

        FxTestSupport.onFxThread(() -> {
            LandingView view = new LandingView(logins::incrementAndGet,
                    registrations::incrementAndGet, skips::incrementAndGet);
            Label title = FxTestSupport.find(view, Label.class,
                    label -> "student time table".equals(label.getText()));
            assertNotNull(title);
            assertTrue(title.isWrapText());
            assertEquals(0, title.getMinWidth());
            List<Button> buttons = FxTestSupport.findAll(view, Button.class);
            buttons.stream().filter(button -> button.getText().equals("Log in")).findFirst().orElseThrow().fire();
            buttons.stream().filter(button -> button.getText().equals("Register")).findFirst().orElseThrow().fire();
            buttons.stream().filter(button -> button.getText().equals("✕")).findFirst().orElseThrow().fire();
            return null;
        });

        assertEquals(1, logins.get());
        assertEquals(1, registrations.get());
        assertEquals(1, skips.get());
    }

    @Test
    void headerNavigationUpdatesActiveTabAndCallsCallbacks() throws Exception {
        AtomicInteger timetable = new AtomicInteger();
        AtomicInteger settings = new AtomicInteger();
        AtomicReference<List<Boolean>> activeStates = new AtomicReference<>();

        FxTestSupport.onFxThread(() -> {
            HeaderView header = new HeaderView(timetable::incrementAndGet, settings::incrementAndGet,
                    () -> { }, () -> { }, "Ada", () -> { });
            Button timetableButton = FxTestSupport.find(header, Button.class,
                    button -> "Timetable".equals(button.getText()));
            Button settingsButton = FxTestSupport.find(header, Button.class,
                    button -> "Settings".equals(button.getText()));
            assertNotNull(timetableButton);
            assertNotNull(settingsButton);
            timetableButton.fire();
            settingsButton.fire();
            activeStates.set(List.of(timetableButton.getStyleClass().contains("active"),
                    settingsButton.getStyleClass().contains("active")));
            return null;
        });

        assertEquals(1, timetable.get());
        assertEquals(1, settings.get());
        assertEquals(List.of(false, true), activeStates.get());
    }

    @Test
    void settingsViewShowsProfileAndNotificationControls() throws Exception {
        AtomicReference<Boolean> remindersEnabled = new AtomicReference<>();
        FxTestSupport.onFxThread(() -> {
            SettingsView view = new SettingsView(null, 0, ignored -> { }, true, remindersEnabled::set);
            List<Label> labels = FxTestSupport.findAll(view, Label.class);
            List<TextField> fields = FxTestSupport.findAll(view, TextField.class);
            assertTrue(labels.stream().anyMatch(label -> "System settings".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Username".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Email".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Major".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Log in to edit and save your profile.".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Email notifications (not configured)".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Schedule reminders".equals(label.getText())));
            assertEquals(3, fields.size());
            assertTrue(fields.stream().allMatch(TextField::isDisabled));
            assertTrue(FxTestSupport.find(view, Button.class, button -> "Save".equals(button.getText())).isDisabled());
            List<CheckBox> checkboxes = FxTestSupport.findAll(view, CheckBox.class);
            assertEquals(2, checkboxes.size());
            assertTrue(checkboxes.get(0).isDisabled());
            assertTrue(checkboxes.get(1).isSelected());
            checkboxes.get(1).fire();
            assertFalse(labels.stream().anyMatch(label -> "Course".equals(label.getText())));
            return null;
        });
        assertEquals(Boolean.FALSE, remindersEnabled.get());
    }

    @Test
    void loginViewTrimsEmailAndInvokesSuccessForValidCredentials() throws Exception {
        AtomicReference<String> submittedEmail = new AtomicReference<>();
        loginRepository repository = new loginRepository(null) {
            @Override public boolean loginUser(String email, String password) {
                submittedEmail.set(email);
                return "ada@example.com".equals(email) && "secret".equals(password);
            }
        };
        AtomicInteger successes = new AtomicInteger();

        FxTestSupport.onFxThread(() -> {
            LoginView view = new LoginView(repository, () -> { }, () -> { }, email -> successes.incrementAndGet());
            TextField email = FxTestSupport.find(view, TextField.class, field -> "Email".equals(field.getPromptText()));
            PasswordField password = FxTestSupport.find(view, PasswordField.class,
                    field -> "Password".equals(field.getPromptText()));
            email.setText("  ada@example.com ");
            password.setText("secret");
            FxTestSupport.find(view, Button.class, button -> "Log In".equals(button.getText())).fire();
            return null;
        });

        assertEquals("ada@example.com", submittedEmail.get());
        assertEquals(1, successes.get());
    }

    @Test
    void registrationViewShowsFieldsAndInvokesNavigationActions() throws Exception {
        AtomicInteger login = new AtomicInteger();
        AtomicInteger skip = new AtomicInteger();

        FxTestSupport.onFxThread(() -> {
            RegisterView view = new RegisterView(null, login::incrementAndGet, skip::incrementAndGet);
            assertNotNull(FxTestSupport.find(view, TextField.class,
                    field -> "Username".equals(field.getPromptText())));
            assertNotNull(FxTestSupport.find(view, TextField.class,
                    field -> "Email".equals(field.getPromptText())));
            List<PasswordField> passwords = FxTestSupport.findAll(view, PasswordField.class);
            assertEquals(2, passwords.size());
            FxTestSupport.find(view, Button.class,
                    button -> button.getText().startsWith("Already have an account?")).fire();
            FxTestSupport.find(view, Button.class, button -> "✕".equals(button.getText())).fire();
            return null;
        });

        assertEquals(1, login.get());
        assertEquals(1, skip.get());
    }

    @Test
    void timetableRendersWeekAndEventAndDisablesAddForInvalidUser() throws Exception {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        Event event = new Event(8, "Review", LocalTime.of(9, 0), LocalTime.of(10, 0),
                monday.plusDays(1), "Room 2");
        AtomicInteger queryCount = new AtomicInteger();
        CountDownLatch queried = new CountDownLatch(1);
        AtomicReference<TimetableView> timetable = new AtomicReference<>();
        AtomicReference<TimetableView> signedOutTimetable = new AtomicReference<>();
        AddEvent service = new AddEvent(new eventRepository(null) {
            @Override public List<Event> findEventsBetween(int userId, LocalDate start, LocalDate end) {
                try {
                    queryCount.incrementAndGet();
                    assertEquals(monday, start);
                    assertEquals(monday.plusDays(7), end);
                    return List.of(event);
                } finally {
                    queried.countDown();
                }
            }
        });

        FxTestSupport.onFxThread(() -> {
            timetable.set(new TimetableView(service, 7));
            signedOutTimetable.set(new TimetableView(service, 0));
            return null;
        });

        assertTrue(queried.await(5, TimeUnit.SECONDS));
        for (int attempt = 0; attempt < 50; attempt++) {
            Boolean rendered = FxTestSupport.onFxThread(() -> {
                ScrollPane scroll = FxTestSupport.find(timetable.get(), ScrollPane.class, ignored -> true);
                GridPane grid = (GridPane) scroll.getContent();
                return FxTestSupport.find(grid, Label.class, label -> "Review".equals(label.getText())) != null;
            });
            if (rendered) break;
            Thread.sleep(50);
        }

        FxTestSupport.onFxThread(() -> {
            ScrollPane scroll = FxTestSupport.find(timetable.get(), ScrollPane.class, ignored -> true);
            GridPane grid = (GridPane) scroll.getContent();
            assertEquals(201, grid.getChildren().size());
            assertTrue(FxTestSupport.find(grid, Label.class, label -> "Review".equals(label.getText())) != null);
            assertTrue(FxTestSupport.find(grid, Label.class, label -> "Room 2".equals(label.getText())) != null);

            Button add = FxTestSupport.find(signedOutTimetable.get(), Button.class,
                    button -> "+ New event".equals(button.getText()));
            assertTrue(add.isDisabled());
            return null;
        });

        assertEquals(1, queryCount.get());
    }

    @Test
    void addEventDialogSavesValidInputAndCloses() throws Exception {
        AtomicReference<Event> savedEvent = new AtomicReference<>();
        CountDownLatch saved = new CountDownLatch(1);
        AddEvent service = new AddEvent(new eventRepository(null) {
            @Override public boolean saveEvent(int userId, Event event) {
                assertEquals(23, userId);
                savedEvent.set(event);
                saved.countDown();
                return true;
            }
        });
        LocalDate date = LocalDate.of(2026, 10, 12);

        FxTestSupport.onFxThread(() -> {
            AddEventDialog dialog = new AddEventDialog(service, 23);
            Stage stage = new Stage();
            var createContent = AddEventDialog.class.getDeclaredMethod("createContent", Stage.class);
            createContent.setAccessible(true);
            VBox content = (VBox) createContent.invoke(dialog, stage);
            TextField name = FxTestSupport.find(content, TextField.class,
                    field -> "Event name".equals(field.getPromptText()));
            TextField room = FxTestSupport.find(content, TextField.class,
                    field -> "Room / location".equals(field.getPromptText()));
            DatePicker datePicker = FxTestSupport.find(content, DatePicker.class, ignored -> true);
            List<ComboBox> combos = FxTestSupport.findAll(content, ComboBox.class);
            name.setText("Design review");
            room.setText("Room 5");
            datePicker.setValue(date);
            combos.get(0).setValue("09:00");
            combos.get(1).setValue("10:00");
            assertNull(FxTestSupport.find(content, Button.class, button -> "Delete".equals(button.getText())));
            FxTestSupport.find(content, Button.class, button -> "Add".equals(button.getText())).fire();
            return null;
        });

        assertTrue(saved.await(5, TimeUnit.SECONDS));
        assertNotNull(savedEvent.get());
        assertEquals("Design review", savedEvent.get().getTitle());
        assertEquals(LocalTime.of(9, 0), savedEvent.get().getStart_time());
        assertEquals(LocalTime.of(10, 0), savedEvent.get().getEnd_time());
        assertEquals(date, savedEvent.get().getEvent_date());
        assertEquals("Room 5", savedEvent.get().getLocation());
    }

    @Test
    void editEventDialogPrefillsAndUpdatesExistingEvent() throws Exception {
        AtomicReference<Event> updatedEvent = new AtomicReference<>();
        CountDownLatch updated = new CountDownLatch(1);
        AddEvent service = new AddEvent(new eventRepository(null) {
            @Override public boolean updateEvent(int userId, Event event) {
                assertEquals(23, userId);
                updatedEvent.set(event);
                updated.countDown();
                return true;
            }
        });
        Event existing = new Event(77, "Review", LocalTime.of(9, 0), LocalTime.of(10, 0),
                LocalDate.of(2026, 10, 12), "Room 5", "purple");

        FxTestSupport.onFxThread(() -> {
            AddEventDialog dialog = new AddEventDialog(service, 23);
            Stage stage = new Stage();
            var createContent = AddEventDialog.class.getDeclaredMethod("createContent", Stage.class, Event.class);
            createContent.setAccessible(true);
            VBox content = (VBox) createContent.invoke(dialog, stage, existing);

            assertTrue(FxTestSupport.findAll(content, Label.class).stream()
                    .anyMatch(label -> "Edit event".equals(label.getText())));
            TextField name = FxTestSupport.find(content, TextField.class,
                    field -> "Event name".equals(field.getPromptText()));
            TextField room = FxTestSupport.find(content, TextField.class,
                    field -> "Room / location".equals(field.getPromptText()));
            DatePicker datePicker = FxTestSupport.find(content, DatePicker.class, ignored -> true);
            List<ComboBox> combos = FxTestSupport.findAll(content, ComboBox.class);

            assertEquals("Review", name.getText());
            assertEquals("Room 5", room.getText());
            assertEquals(LocalDate.of(2026, 10, 12), datePicker.getValue());
            assertEquals("09:00", combos.get(0).getValue());
            assertEquals("10:00", combos.get(1).getValue());
            assertEquals("Purple", combos.get(2).getValue());
            assertNotNull(FxTestSupport.find(content, Button.class, button -> "Delete".equals(button.getText())));

            name.setText("Updated review");
            room.setText("Room 8");
            datePicker.setValue(LocalDate.of(2026, 10, 13));
            combos.get(0).setValue("11:00");
            combos.get(1).setValue("12:00");
            combos.get(2).setValue("Green");
            FxTestSupport.find(content, Button.class, button -> "Save".equals(button.getText())).fire();
            return null;
        });

        assertTrue(updated.await(5, TimeUnit.SECONDS));
        assertEquals(77, updatedEvent.get().getEvent_id());
        assertEquals("Updated review", updatedEvent.get().getTitle());
        assertEquals(LocalTime.of(11, 0), updatedEvent.get().getStart_time());
        assertEquals(LocalTime.of(12, 0), updatedEvent.get().getEnd_time());
        assertEquals(LocalDate.of(2026, 10, 13), updatedEvent.get().getEvent_date());
        assertEquals("Room 8", updatedEvent.get().getLocation());
        assertEquals("green", updatedEvent.get().getColor());
    }

    @Test
    void editEventDialogCancelDoesNotSaveChanges() throws Exception {
        AtomicInteger updateCalls = new AtomicInteger();
        AddEvent service = new AddEvent(new eventRepository(null) {
            @Override public boolean updateEvent(int userId, Event event) {
                updateCalls.incrementAndGet();
                return true;
            }
        });
        Event existing = new Event(77, "Review", LocalTime.of(9, 0), LocalTime.of(10, 0),
                LocalDate.of(2026, 10, 12), "Room 5", "blue");

        FxTestSupport.onFxThread(() -> {
            AddEventDialog dialog = new AddEventDialog(service, 23);
            Stage stage = new Stage();
            var createContent = AddEventDialog.class.getDeclaredMethod("createContent", Stage.class, Event.class);
            createContent.setAccessible(true);
            VBox content = (VBox) createContent.invoke(dialog, stage, existing);
            TextField name = FxTestSupport.find(content, TextField.class,
                    field -> "Event name".equals(field.getPromptText()));
            name.setText("Unsaved title");
            FxTestSupport.find(content, Button.class, button -> "Cancel".equals(button.getText())).fire();
            return null;
        });

        assertEquals(0, updateCalls.get());
        assertEquals("Review", existing.getTitle());
    }

    @Test
    void editEventDialogDeleteCancellationDoesNotDelete() throws Exception {
        AtomicInteger deleteCalls = new AtomicInteger();
        AddEvent service = new AddEvent(new eventRepository(null) {
            @Override public boolean deleteEvent(int userId, int eventId) {
                deleteCalls.incrementAndGet();
                return true;
            }
        });
        Event existing = new Event(77, "Review", LocalTime.of(9, 0), LocalTime.of(10, 0),
                LocalDate.of(2026, 10, 12), "Room 5", "blue");

        FxTestSupport.onFxThread(() -> {
            AddEventDialog dialog = new AddEventDialog(service, 23, () -> { }, event -> false);
            Stage stage = new Stage();
            var createContent = AddEventDialog.class.getDeclaredMethod("createContent", Stage.class, Event.class);
            createContent.setAccessible(true);
            VBox content = (VBox) createContent.invoke(dialog, stage, existing);

            FxTestSupport.find(content, Button.class, button -> "Delete".equals(button.getText())).fire();
            return null;
        });

        assertEquals(0, deleteCalls.get());
    }

    @Test
    void editEventDialogDeletesExistingEventAndRunsCallback() throws Exception {
        AtomicInteger savedCallbacks = new AtomicInteger();
        AtomicReference<Integer> deletedEventId = new AtomicReference<>();
        CountDownLatch deleted = new CountDownLatch(1);
        AddEvent service = new AddEvent(new eventRepository(null) {
            @Override public boolean deleteEvent(int userId, int eventId) {
                assertEquals(23, userId);
                deletedEventId.set(eventId);
                deleted.countDown();
                return true;
            }
        });
        Event existing = new Event(77, "Review", LocalTime.of(9, 0), LocalTime.of(10, 0),
                LocalDate.of(2026, 10, 12), "Room 5", "blue");

        FxTestSupport.onFxThread(() -> {
            AddEventDialog dialog = new AddEventDialog(service, 23, savedCallbacks::incrementAndGet, event -> true);
            Stage stage = new Stage();
            var createContent = AddEventDialog.class.getDeclaredMethod("createContent", Stage.class, Event.class);
            createContent.setAccessible(true);
            VBox content = (VBox) createContent.invoke(dialog, stage, existing);

            FxTestSupport.find(content, Button.class, button -> "Delete".equals(button.getText())).fire();
            return null;
        });

        assertTrue(deleted.await(5, TimeUnit.SECONDS));
        for (int attempt = 0; attempt < 50 && savedCallbacks.get() == 0; attempt++) {
            FxTestSupport.onFxThread(() -> null);
            Thread.sleep(50);
        }
        assertEquals(77, deletedEventId.get());
        assertEquals(1, savedCallbacks.get());
    }

    @Test
    void editEventDialogRestoresButtonsAndShowsMessageWhenDeleteFails() throws Exception {
        CountDownLatch deleteAttempted = new CountDownLatch(1);
        AtomicReference<VBox> contentRef = new AtomicReference<>();
        AddEvent service = new AddEvent(new eventRepository(null) {
            @Override public boolean deleteEvent(int userId, int eventId) {
                deleteAttempted.countDown();
                return false;
            }
        });
        Event existing = new Event(77, "Review", LocalTime.of(9, 0), LocalTime.of(10, 0),
                LocalDate.of(2026, 10, 12), "Room 5", "blue");

        FxTestSupport.onFxThread(() -> {
            AddEventDialog dialog = new AddEventDialog(service, 23, () -> { }, event -> true);
            Stage stage = new Stage();
            var createContent = AddEventDialog.class.getDeclaredMethod("createContent", Stage.class, Event.class);
            createContent.setAccessible(true);
            VBox content = (VBox) createContent.invoke(dialog, stage, existing);
            contentRef.set(content);

            FxTestSupport.find(content, Button.class, button -> "Delete".equals(button.getText())).fire();
            return null;
        });

        assertTrue(deleteAttempted.await(5, TimeUnit.SECONDS));
        for (int attempt = 0; attempt < 50; attempt++) {
            Boolean restored = FxTestSupport.onFxThread(() -> {
                VBox content = contentRef.get();
                Button delete = FxTestSupport.find(content, Button.class, button -> "Delete".equals(button.getText()));
                Button save = FxTestSupport.find(content, Button.class, button -> "Save".equals(button.getText()));
                return !delete.isDisabled() && !save.isDisabled()
                        && FxTestSupport.find(content, Label.class,
                        label -> label.getText().contains("Could not delete this event")) != null;
            });
            if (restored) return;
            Thread.sleep(50);
        }
        fail("Delete failure did not restore buttons and show an error message");
    }

    @Test
    void mainStartsWindowAndRoutesBetweenScreensWithoutDatabase() throws Exception {
        FxTestSupport.onFxThread(() -> {
            Main application = new Main() {
                @Override
                protected Connection openDatabaseConnection() throws SQLException {
                    throw new SQLException("Database intentionally unavailable in this test");
                }
            };
            Stage stage = new Stage();
            try {
                PrintStream previousError = System.err;
                ByteArrayOutputStream startupError = new ByteArrayOutputStream();
                try {
                    System.setErr(new PrintStream(startupError));
                    application.start(stage);
                } finally {
                    System.setErr(previousError);
                }
                assertTrue(startupError.toString().contains("Database intentionally unavailable"));
                assertEquals("Student Timetable", stage.getTitle());
                assertEquals(950, stage.getMinWidth());
                assertEquals(600, stage.getMinHeight());
                assertTrue(stage.isShowing());

                LandingView landing = (LandingView) stage.getScene().getRoot();
                FxTestSupport.find(landing, Button.class, button -> "Log in".equals(button.getText())).fire();
                assertTrue(stage.getScene().getRoot() instanceof LoginView);
                FxTestSupport.find(stage.getScene().getRoot(), Button.class,
                        button -> "✕".equals(button.getText())).fire();
                assertTrue(stage.getScene().getRoot() instanceof LandingView);

                landing = (LandingView) stage.getScene().getRoot();
                FxTestSupport.find(landing, Button.class,
                        button -> "Register".equals(button.getText())).fire();
                assertTrue(stage.getScene().getRoot() instanceof RegisterView);
                FxTestSupport.find(stage.getScene().getRoot(), Button.class,
                        button -> "✕".equals(button.getText())).fire();

                BorderPane applicationRoot = (BorderPane) stage.getScene().getRoot();
                HeaderView header = (HeaderView) applicationRoot.getTop();
                StackPane content = (StackPane) applicationRoot.getCenter();
                assertTrue(content.getChildren().get(0) instanceof TimetableView);

                FxTestSupport.find(header, Button.class,
                        button -> "Settings".equals(button.getText())).fire();
                assertTrue(content.getChildren().get(0) instanceof SettingsView);

                FxTestSupport.find(header, Button.class,
                        button -> "Timetable".equals(button.getText())).fire();
                assertTrue(content.getChildren().get(0) instanceof TimetableView);
                Button addEvent = FxTestSupport.find(content, Button.class,
                        button -> "+ New event".equals(button.getText()));
                assertTrue(addEvent.isDisabled());
            } finally {
                stage.close();
            }
            return null;
        });
    }
}
