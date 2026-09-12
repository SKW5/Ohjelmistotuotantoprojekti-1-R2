package com.example.timetable;

import com.example.timetable.ui.HeaderView;
import com.example.timetable.ui.LoginDialog;
import com.example.timetable.ui.SettingsView;
import com.example.timetable.ui.TimetableView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();

    @Override
    public void start(Stage stage) {
        Font.loadFont(
                getClass().getResourceAsStream("/fonts/DMSerifDisplay-Regular.ttf"),
                20
        );

        HeaderView header = new HeaderView(
                this::showTimetable,
                this::showSettings,
                this::showLoginPlaceholder
        );

        root.getStyleClass().add("app");
        root.setTop(header);
        root.setCenter(content);

        showTimetable();

        Scene scene = new Scene(root, 1180, 720);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        stage.setTitle("Student Timetable");
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private void showTimetable() {
        content.getChildren().setAll(new TimetableView());
    }

    private void showSettings() {
        content.getChildren().setAll(new SettingsView());
    }

    private void showLoginPlaceholder() {
        new LoginDialog().show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
