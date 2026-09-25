package com.example.timetable.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HeaderView extends HBox {

    public HeaderView(
            Runnable onTimetable,
            Runnable onSettings,
            Runnable onLogin,
            Runnable onRegister
    ) {
        setSpacing(28);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(14, 22, 14, 22));
        getStyleClass().add("header");

        Label logo = new Label("Student Timetable");
        logo.getStyleClass().add("logo");

        Button timetable = navButton("Timetable");
        Button settings = navButton("Settings");
        Button avatar = createAvatarButton(onLogin, onRegister);

        setActiveTab(timetable, settings);

        timetable.setOnAction(e -> {
            setActiveTab(timetable, settings);
            onTimetable.run();
        });
        settings.setOnAction(e -> {
            setActiveTab(settings, timetable);
            onSettings.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(logo, timetable, settings, spacer, avatar);
    }

    private Button navButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("nav-button", "nav-tab");
        return button;
    }

    private void setActiveTab(Button active, Button inactive) {
        inactive.getStyleClass().remove("active");

        if (!active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }

    private Button createAvatarButton(Runnable onLogin, Runnable onRegister) {
        Button avatar = new Button("ST");
        avatar.getStyleClass().add("avatar-button");

        ContextMenu profileMenu = new ContextMenu();
        profileMenu.getStyleClass().add("profile-menu");
        profileMenu.setAutoHide(true);

        MenuItem login = new MenuItem("Log in");
        MenuItem register = new MenuItem("Register");
        login.getStyleClass().add("profile-menu-item");
        register.getStyleClass().add("profile-menu-item");

        login.setOnAction(e -> onLogin.run());
        register.setOnAction(e -> onRegister.run());
        profileMenu.getItems().addAll(login, register);

        avatar.setOnAction(e -> {
            if (profileMenu.isShowing()) {
                profileMenu.hide();
            } else {
                profileMenu.show(avatar, Side.BOTTOM, -112, 8);
            }
        });

        return avatar;
    }
}

