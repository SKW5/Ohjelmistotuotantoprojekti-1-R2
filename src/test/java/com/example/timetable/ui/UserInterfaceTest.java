package com.example.timetable.ui;

import com.example.timetable.model.Event;
import com.example.timetable.repository.eventRepository;
import com.example.timetable.repository.loginRepository;
import com.example.timetable.service.AddEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
        FxTestSupport.onFxThread(() -> {
            SettingsView view = new SettingsView();
            List<Label> labels = FxTestSupport.findAll(view, Label.class);
            assertTrue(labels.stream().anyMatch(label -> "System settings".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Username".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Email".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Email notifications".equals(label.getText())));
            assertTrue(labels.stream().anyMatch(label -> "Schedule reminders".equals(label.getText())));
            assertFalse(labels.stream().anyMatch(label -> "Course".equals(label.getText())));
            return null;
        });
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
        AddEvent service = new AddEvent(new eventRepository(null) {
            @Override public List<Event> findEventsBetween(int userId, LocalDate start, LocalDate end) {
                queryCount.incrementAndGet();
                assertEquals(monday, start);
                assertEquals(monday.plusDays(5), end);
                return List.of(event);
            }
        });

        FxTestSupport.onFxThread(() -> {
            TimetableView view = new TimetableView(service, 7);
            ScrollPane scroll = FxTestSupport.find(view, ScrollPane.class, ignored -> true);
            GridPane grid = (GridPane) scroll.getContent();
            assertEquals(73, grid.getChildren().size());
            assertTrue(FxTestSupport.find(grid, Label.class, label -> "Review".equals(label.getText())) != null);
            assertTrue(FxTestSupport.find(grid, Label.class, label -> "Room 2".equals(label.getText())) != null);

            TimetableView signedOut = new TimetableView(service, 0);
            Button add = FxTestSupport.find(signedOut, Button.class,
                    button -> "+ New event".equals(button.getText()));
            assertTrue(add.isDisabled());
            return null;
        });

        assertEquals(1, queryCount.get());
    }

    @Test
    void addEventDialogSavesValidInputAndCloses() throws Exception {
        AtomicReference<Event> savedEvent = new AtomicReference<>();
        AddEvent service = new AddEvent(new eventRepository(null) {
            @Override public boolean saveEvent(int userId, Event event) {
                assertEquals(23, userId);
                savedEvent.set(event);
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
            FxTestSupport.find(content, Button.class, button -> "Add".equals(button.getText())).fire();
            return null;
        });

        assertNotNull(savedEvent.get());
        assertEquals("Design review", savedEvent.get().getTitle());
        assertEquals(LocalTime.of(9, 0), savedEvent.get().getStart_time());
        assertEquals(LocalTime.of(10, 0), savedEvent.get().getEnd_time());
        assertEquals(date, savedEvent.get().getEvent_date());
        assertEquals("Room 5", savedEvent.get().getLocation());
    }
}
