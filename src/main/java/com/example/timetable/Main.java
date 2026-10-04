package com.example.timetable;

import com.example.timetable.repository.eventRepository;
import com.example.timetable.repository.loginRepository;
import com.example.timetable.repository.register;
import com.example.timetable.service.AddEvent;
import com.example.timetable.service.EventReminderService;
import com.example.timetable.service.RegisterUser;
import com.example.timetable.ui.HeaderView;
import com.example.timetable.ui.LandingView;
import com.example.timetable.ui.LoginView;
import com.example.timetable.ui.RegisterView;
import com.example.timetable.ui.SettingsView;
import com.example.timetable.ui.TimetableView;
import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main extends Application {

    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();
    private AddEvent addEventService;
    private EventReminderService eventReminderService;
    private Timeline reminderTimeline;
    private boolean scheduleRemindersEnabled = true;
    private RegisterUser registerUserService;
    private loginRepository loginRepository;
    private String loggedInUserName;
    private int loggedInUserId;
    private Scene scene;

    @Override
    public void start(Stage stage) {
        loadFonts();

        try {
            // Create database connection
            Connection connection = openDatabaseConnection();

            // Create repository
            eventRepository repository =
                    new eventRepository(connection);

            loginRepository = new loginRepository(connection);
            registerUserService = new RegisterUser(new register(connection));

            // Create service
            addEventService =
                    new AddEvent(repository);
                eventReminderService = new EventReminderService(addEventService, this::showEventReminder);

        } catch (Exception e) {
            e.printStackTrace();
        }

        scene = new Scene(new StackPane(), 1380, 820);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        stage.setTitle("Student Timetable");
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.setScene(scene);

        showLanding();
        stage.show();
        reminderTimeline = new Timeline(new KeyFrame(Duration.minutes(1), event -> checkEventReminders()));
        reminderTimeline.setCycleCount(Timeline.INDEFINITE);
        reminderTimeline.play();
        stage.setOnHidden(event -> reminderTimeline.stop());
    }

    protected Connection openDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(
                System.getenv().getOrDefault("DB_URL", "jdbc:mariadb://localhost:3307/student_timetable"),
                System.getenv().getOrDefault("DB_USER", "student"),
                System.getenv().getOrDefault("DB_PASSWORD", "student")
        );
    }

    private void loadFonts() {
        Font.loadFont(
                getClass().getResourceAsStream("/Fonts/DMSerifDisplay-Regular.ttf"),
                20
        );
    }

    private void showLanding() {
        scene.setRoot(
                new LandingView(
                        this::showLogin,
                        this::showRegister,
                        this::showApplication
                )
        );
    }

    private void showLogin() {
        scene.setRoot(
                new LoginView(
                        loginRepository,
                        this::showRegister,
                        this::showLanding,
                        this::handleLoginSuccess
                )
        );
    }

    private void showRegister() {
        scene.setRoot(
                new RegisterView(
                    registerUserService,
                        this::showLogin,
                        this::showApplication
                )
        );
    }

    private void handleLoginSuccess(String email) {
        if (loginRepository != null) {
            loggedInUserId = loginRepository.getUserIdByEmail(email);
            String username = loginRepository.getUserNameByEmail(email);
            loggedInUserName = username != null && !username.isBlank() ? username : email;
        } else {
            loggedInUserName = email;
        }
        showApplication();
    }

    private void logout() {
        loggedInUserName = null;
        loggedInUserId = 0;
        showLanding();
    }

    private void showApplication() {
        HeaderView header = new HeaderView(
                this::showTimetable,
                this::showSettings,
                this::showLogin,
                this::showRegister,
                loggedInUserName,
                this::logout
        );

        if (!root.getStyleClass().contains("app")) {
            root.getStyleClass().add("app");
        }
        root.setTop(header);
        root.setCenter(content);

        showTimetable();
        scene.setRoot(root);
        checkEventReminders();
    }

    private void showTimetable() {
        content.getChildren().setAll(new TimetableView(addEventService, loggedInUserId));
    }

    private void showSettings() {
        content.getChildren().setAll(new SettingsView(scheduleRemindersEnabled, enabled -> {
            scheduleRemindersEnabled = enabled;
            checkEventReminders();
        }));
    }

    private void checkEventReminders() {
        if (scheduleRemindersEnabled && eventReminderService != null) {
            eventReminderService.checkUpcomingEvents(loggedInUserId, LocalDateTime.now());
        }
    }

    private void showEventReminder(com.example.timetable.model.Event event) {
        Alert reminder = new Alert(Alert.AlertType.INFORMATION);
        reminder.setTitle("Upcoming event");
        reminder.setHeaderText(event.getTitle());
        reminder.setContentText("Starts at " + event.getStart_time().format(DateTimeFormatter.ofPattern("HH:mm"))
                + (event.getLocation() == null || event.getLocation().isBlank()
                ? "" : "\nLocation: " + event.getLocation()));
        reminder.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
