package com.example.timetable;

import com.example.timetable.repository.eventRepository;
import com.example.timetable.service.AddEvent;
import com.example.timetable.ui.HeaderView;
import com.example.timetable.ui.LandingView;
import com.example.timetable.ui.LoginView;
import com.example.timetable.ui.RegisterView;
import com.example.timetable.ui.SettingsView;
import com.example.timetable.ui.TimetableView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends Application {

    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();
    private AddEvent addEventService;
    private Scene scene;

    @Override
    public void start(Stage stage) {
        loadFonts();

        try {
        // Create database connection
        Connection connection = DriverManager.getConnection(
                "jdbc:mariadb://localhost:3306/student_timetable",
                "student",
                "student"
        );

            // Create repository
            eventRepository repository =
                    new eventRepository(connection);

            // Create service
            addEventService =
                    new AddEvent(repository);

        } catch (Exception e) {
            e.printStackTrace();
        }

        scene = new Scene(new StackPane(), 1180, 720);
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
                        this::showRegister,
                        this::showApplication
                )
        );
    }

    private void showRegister() {
        scene.setRoot(
                new RegisterView(
                        this::showLogin,
                        this::showApplication
                )
        );
    }

    private void showApplication() {
        HeaderView header = new HeaderView(
                this::showTimetable,
                this::showSettings,
                this::showLoginPlaceholder
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
        content.getChildren().setAll(new TimetableView(addEventService));
    }

    private void showSettings() {
        content.getChildren().setAll(new SettingsView());
    }

    private void showLoginPlaceholder() {
        showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
