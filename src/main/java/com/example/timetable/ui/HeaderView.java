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
    private final Label userStatus;
    private final Button avatar;

    public HeaderView(
            Runnable onTimetable,
            Runnable onSettings,
            Runnable onLogin,
            Runnable onRegister,
            String currentUserName,
            Runnable onLogout
    ) {
        setSpacing(28);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(14, 22, 14, 22));
        getStyleClass().add("header");

        Label logo = new Label("Student Timetable");
        logo.getStyleClass().add("logo");

        Button timetable = navButton("Timetable");
        Button settings = navButton("Settings");

        userStatus = new Label(
                currentUserName != null && !currentUserName.isBlank()
                        ? "Logged in: " + currentUserName
                        : "Not logged in"
        );
        userStatus.getStyleClass().add("user-status");

        avatar = createAvatarButton(currentUserName, onLogin, onRegister, onLogout);

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

        getChildren().addAll(logo, timetable, settings, spacer, userStatus, avatar);
    }

    public void updateCurrentUserName(String currentUserName) {
        userStatus.setText(
                currentUserName != null && !currentUserName.isBlank()
                        ? "Logged in: " + currentUserName
                        : "Not logged in"
        );
        avatar.setText(currentUserName != null && !currentUserName.isBlank()
                ? currentUserName.substring(0, 1).toUpperCase()
                : "ST");
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

    private Button createAvatarButton(String currentUserName, Runnable onLogin, Runnable onRegister, Runnable onLogout) {
        String avatarText = currentUserName != null && !currentUserName.isBlank()
                ? currentUserName.substring(0, 1).toUpperCase()
                : "ST";

        Button avatar = new Button(avatarText);
        avatar.getStyleClass().add("avatar-button");

        ContextMenu profileMenu = new ContextMenu();
        profileMenu.getStyleClass().add("profile-menu");
        profileMenu.setAutoHide(true);

        if (currentUserName != null && !currentUserName.isBlank()) {
            MenuItem logout = new MenuItem("Log out");
            logout.getStyleClass().add("profile-menu-item");
            logout.setOnAction(e -> onLogout.run());
            profileMenu.getItems().add(logout);
        } else {
            MenuItem login = new MenuItem("Log in");
            MenuItem register = new MenuItem("Register");
            login.getStyleClass().add("profile-menu-item");
            register.getStyleClass().add("profile-menu-item");

            login.setOnAction(e -> onLogin.run());
            register.setOnAction(e -> onRegister.run());
            profileMenu.getItems().addAll(login, register);
        }

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

