package com.example.timetable;

import com.example.timetable.repository.eventRepository;
import com.example.timetable.repository.loginRepository;
import com.example.timetable.repository.profileRepository;
import com.example.timetable.repository.register;
import com.example.timetable.service.AddEvent;
import com.example.timetable.service.EventReminderService;
import com.example.timetable.service.RegisterUser;
import com.example.timetable.model.Event;
import com.example.timetable.model.UserProfile;
import com.example.timetable.ui.HeaderView;
import com.example.timetable.ui.LandingView;
import com.example.timetable.ui.LoginView;
import com.example.timetable.ui.RegisterView;
import com.example.timetable.ui.SettingsView;
import com.example.timetable.ui.TimetableView;
import javafx.application.Application;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Main extends Application {

    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();
    private AddEvent addEventService;
    private EventReminderService eventReminderService;
    private Timeline reminderTimeline;
    private RegisterUser registerUserService;
    private loginRepository loginRepository;
    private profileRepository profileRepository;
    private HeaderView header;
    private String loggedInUserName;
    private String loggedInUserEmail;
    private UserProfile loggedInUserProfile;
    private int loggedInUserId;
    private boolean scheduleRemindersEnabled = true;
    private Scene scene;

    @Override
    public void start(Stage stage) {
        loadFonts();
        profileRepository = new profileRepository(this::openDatabaseConnection);

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

        } catch (Exception e) {
            e.printStackTrace();
        }

        eventReminderService = new EventReminderService(addEventService, this::showEventReminder);
        reminderTimeline = new Timeline(new KeyFrame(Duration.seconds(30), event ->
            eventReminderService.checkUpcomingEvents(loggedInUserId, LocalDateTime.now())
        ));
        reminderTimeline.setCycleCount(Animation.INDEFINITE);
        reminderTimeline.play();

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
    }

    protected Connection openDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(
                System.getenv().getOrDefault("DB_URL", "jdbc:mariadb://localhost:3306/student_timetable"),
                System.getenv().getOrDefault("DB_USER", "student"),
                System.getenv().getOrDefault("DB_PASSWORD", "student")
        );
    }

    @Override
    public void stop() {
        if (reminderTimeline != null) {
            reminderTimeline.stop();
        }
    }

    private void showEventReminder(Event event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Upcoming event");
        alert.setHeaderText(event.getTitle());

        String location = event.getLocation();
        String content = "Starts at " + event.getStart_time();
        if (location != null && !location.isBlank()) {
            content += "\nLocation: " + location;
        }
        alert.setContentText(content);
        alert.show();
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
        loggedInUserEmail = email;
        if (loginRepository != null) {
            loggedInUserId = loginRepository.getUserIdByEmail(email);
            String username = loginRepository.getUserNameByEmail(email);
            loggedInUserName = username != null && !username.isBlank() ? username : email;
            loggedInUserProfile = new UserProfile(loggedInUserId, loggedInUserName, email, null);
        } else {
            loggedInUserName = email;
            loggedInUserProfile = null;
        }
        showApplication();
    }

    private void logout() {
        loggedInUserName = null;
        loggedInUserEmail = null;
        loggedInUserProfile = null;
        loggedInUserId = 0;
        showLanding();
    }

    private void showApplication() {
        header = new HeaderView(
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
    }

    private void showTimetable() {
        content.getChildren().setAll(new TimetableView(addEventService, loggedInUserId));
    }

    private void showSettings() {
        content.getChildren().setAll(new SettingsView(
                profileRepository,
                loggedInUserId,
                this::handleProfileSaved,
                scheduleRemindersEnabled,
                enabled -> {
                    scheduleRemindersEnabled = enabled;
                    eventReminderService.setEnabled(enabled);
                }
        ));
    }

    private void handleProfileSaved(UserProfile profile) {
        loggedInUserId = profile.getUserId();
        loggedInUserName = profile.getUsername();
        loggedInUserEmail = profile.getEmail();
        loggedInUserProfile = profile;
        if (header != null) {
            header.updateCurrentUserName(loggedInUserName);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
