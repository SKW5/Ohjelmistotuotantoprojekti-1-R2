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
    @Test
    void localizationCatalogsHaveMatchingKeysAndValidPatterns() throws Exception {
        var base = new java.util.Properties();
        try (var reader = new java.io.InputStreamReader(getClass().getResourceAsStream("/i18n/messages.properties"),
                java.nio.charset.StandardCharsets.UTF_8)) { base.load(reader); }
        for (String language : List.of("fi", "ar", "ja")) {
            var translated = new java.util.Properties();
            try (var reader = new java.io.InputStreamReader(getClass().getResourceAsStream("/i18n/messages_" + language + ".properties"),
                    java.nio.charset.StandardCharsets.UTF_8)) { translated.load(reader); }
            assertEquals(base.keySet(), translated.keySet(), language);
            for (String key : base.stringPropertyNames()) {
                assertFalse(translated.getProperty(key).isBlank(), language + ":" + key);
                var expected = new java.text.MessageFormat(base.getProperty(key));
                var actual = new java.text.MessageFormat(translated.getProperty(key));
                assertEquals(expected.getFormatsByArgumentIndex().length, actual.getFormatsByArgumentIndex().length, key);
            }
        }
    }

    @Test
    void selectorsUpdateExistingFormsAndMirrorLayoutWithoutLosingInput() throws Exception {
        FxTestSupport.onFxThread(() -> {
            var original = com.example.timetable.i18n.I18n.locale();
            Stage stage = new Stage();
            try {
                com.example.timetable.i18n.I18n.setLocale(com.example.timetable.i18n.I18n.ENGLISH);
                LoginView login = new LoginView(null, () -> {}, () -> {}, ignored -> {});
                TextField email = FxTestSupport.find(login, TextField.class, f -> "Email".equals(f.getPromptText()));
                email.setText("student@example.com");
                Button submit = FxTestSupport.find(login, Button.class, b -> "Log In".equals(b.getText()));
                ComboBox selector = FxTestSupport.find(login, ComboBox.class, c -> "language-selector".equals(c.getId()));
                assertEquals(4, selector.getItems().size());
                HeaderView header = new HeaderView(() -> {}, () -> {}, () -> {}, () -> {}, "Student", () -> {});
                var root = new BorderPane(login, header, null, null, null);
                stage.setScene(new javafx.scene.Scene(root, 1380, 820));
                stage.getScene().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                stage.show();
                for (var locale : List.of(com.example.timetable.i18n.I18n.ARABIC, com.example.timetable.i18n.I18n.JAPANESE,
                        com.example.timetable.i18n.I18n.FINNISH, com.example.timetable.i18n.I18n.ENGLISH)) {
                    selector.setValue(locale);
                    assertEquals(locale, com.example.timetable.i18n.I18n.locale());
                    assertEquals(com.example.timetable.i18n.I18n.text("auth.submit"), submit.getText());
                    assertEquals(com.example.timetable.i18n.I18n.text("auth.email"), email.getPromptText());
                    assertEquals("student@example.com", email.getText());
                    var expected = locale.getLanguage().equals("ar") ? javafx.geometry.NodeOrientation.RIGHT_TO_LEFT
                            : javafx.geometry.NodeOrientation.LEFT_TO_RIGHT;
                    assertEquals(expected, login.getEffectiveNodeOrientation());
                    assertEquals(expected, header.getEffectiveNodeOrientation());
                    root.applyCss();
                    root.layout();
                    var logo = header.getChildren().getFirst();
                    var avatar = header.getChildren().getLast();
                    double logoX = logo.localToScene(logo.getBoundsInLocal()).getMinX();
                    double avatarX = avatar.localToScene(avatar.getBoundsInLocal()).getMinX();
                    assertEquals(locale.getLanguage().equals("ar"), logoX > avatarX);
                    assertEquals(locale, FxTestSupport.find(header, ComboBox.class, c -> "language-selector".equals(c.getId())).getValue());
                    if (locale.getLanguage().equals("ar") || locale.getLanguage().equals("ja")) {
                        saveLocalizationPreview(root, "login-" + locale.getLanguage());
                    }
                }
            } finally {
                stage.close();
                com.example.timetable.i18n.I18n.setLocale(original);
            }
            return null;
        });
    }

    @Test
    void nonLatinLocalesUpdateCalendarDatesNumbersAndEventControls() throws Exception {
        FxTestSupport.onFxThread(() -> {
            var original = com.example.timetable.i18n.I18n.locale();
            Stage stage = new Stage();
            try {
                com.example.timetable.i18n.I18n.setLocale(com.example.timetable.i18n.I18n.ENGLISH);
                TimetableView timetable = new TimetableView(null, 0);
                AddEventDialog dialog = new AddEventDialog(null, 1);
                var method = AddEventDialog.class.getDeclaredMethod("createContent", Stage.class);
                method.setAccessible(true);
                VBox content = (VBox) method.invoke(dialog, stage);
                DatePicker date = FxTestSupport.find(content, DatePicker.class, d -> true);
                LocalDate value = LocalDate.of(2026, 11, 23);
                date.setValue(value);
                List<ComboBox> choices = FxTestSupport.findAll(content, ComboBox.class);
                Button add = FxTestSupport.find(content, Button.class, b -> "Add".equals(b.getText()));
                add.fire(); // Keep a validation message visible while switching languages.
                for (var locale : List.of(com.example.timetable.i18n.I18n.ARABIC, com.example.timetable.i18n.I18n.JAPANESE)) {
                    com.example.timetable.i18n.I18n.setLocale(locale);
                    assertEquals(value, date.getValue());
                    assertEquals(com.example.timetable.i18n.I18n.date(value), date.getEditor().getText());
                    assertEquals(value, date.getConverter().fromString(date.getEditor().getText()));
                    assertEquals("08:00", choices.get(0).getValue());
                    assertEquals(com.example.timetable.i18n.I18n.time(LocalTime.of(8, 0)), choices.get(0).getConverter().toString("08:00"));
                    assertEquals("Blue", choices.get(2).getValue());
                    assertEquals(com.example.timetable.i18n.I18n.text("color.Blue"), choices.get(2).getConverter().toString("Blue"));
                    assertNotNull(FxTestSupport.find(content, Label.class, l -> com.example.timetable.i18n.I18n.text("event.nameRequired").equals(l.getText())));
                    assertNotNull(FxTestSupport.find(timetable, Label.class, l -> com.example.timetable.i18n.I18n.text("calendar.title").equals(l.getText())));
                    String monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY).getDayOfWeek()
                            .getDisplayName(java.time.format.TextStyle.SHORT, locale);
                    ScrollPane scroll = FxTestSupport.find(timetable, ScrollPane.class, s -> true);
                    assertNotNull(FxTestSupport.find(scroll.getContent(), Label.class, l -> l.getText().contains(monday)));
                    String number = com.example.timetable.i18n.I18n.number(1234.5);
                    assertEquals(java.text.NumberFormat.getNumberInstance(locale).format(1234.5), number);
                    if (locale.getLanguage().equals("ar")) assertTrue(number.contains("١"));
                    assertTrue(com.example.timetable.i18n.I18n.text("event.nameLength", 120)
                            .contains(java.text.NumberFormat.getIntegerInstance(locale).format(120)));
                    if (stage.getScene() != null) stage.getScene().setRoot(new StackPane());
                    stage.setScene(new javafx.scene.Scene(timetable, 1380, 820));
                    stage.getScene().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                    stage.show();
                    timetable.applyCss();
                    timetable.layout();
                    saveLocalizationPreview(timetable, "timetable-" + locale.getLanguage());
                    stage.getScene().setRoot(new StackPane());
                    stage.setScene(new javafx.scene.Scene(content, 480, 760));
                    stage.getScene().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                    content.applyCss();
                    content.layout();
                    assertEquals(com.example.timetable.i18n.I18n.text("color.Blue"), choices.get(2).getButtonCell().getText());
                    assertEquals(com.example.timetable.i18n.I18n.time(LocalTime.of(8, 0)), choices.get(0).getButtonCell().getText());
                    saveLocalizationPreview(content, "event-" + locale.getLanguage());
                }
            } finally {
                stage.close();
                com.example.timetable.i18n.I18n.setLocale(original);
            }
            return null;
        });
    }

    private static void saveLocalizationPreview(javafx.scene.Parent root, String name) throws Exception {
        if (!Boolean.getBoolean("localization.screenshots")) return;
        var snapshot = root.snapshot(null, null);
        var pixels = snapshot.getPixelReader();
        var image = new java.awt.image.BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) image.setRGB(x, y, pixels.getArgb(x, y));
        }
        var directory = java.nio.file.Path.of("target", "localization-previews");
        java.nio.file.Files.createDirectories(directory);
        javax.imageio.ImageIO.write(image, "png", directory.resolve(name + ".png").toFile());
    }

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
