package com.example.timetable.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HeaderView extends HBox {

    public HeaderView(
            Runnable onTimetable,
            Runnable onSettings,
            Runnable onLogin
    ) {
        setSpacing(28);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(14, 22, 14, 22));
        getStyleClass().add("header");

        Label logo = new Label("Student Timetable");
        logo.getStyleClass().add("logo");

        Button timetable = navButton("Timetable");
        Button settings = navButton("Settings");
        Button login = new Button("Login");
        login.getStyleClass().add("dark-button");

        timetable.setOnAction(e -> onTimetable.run());
        settings.setOnAction(e -> onSettings.run());
        login.setOnAction(e -> onLogin.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(logo, timetable, settings, spacer, login);
    }

    private Button navButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        return button;
    }
}

