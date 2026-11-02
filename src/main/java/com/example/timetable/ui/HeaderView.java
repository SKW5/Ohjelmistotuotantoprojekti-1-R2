package com.example.timetable.ui;

import com.example.timetable.i18n.I18n;

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
        I18n.orient(this);
        setSpacing(14);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(14, 22, 14, 22));
        getStyleClass().add("header");

        Label logo = I18n.label("app.title");
        logo.getStyleClass().add("logo");

        Button timetable = navButton("nav.timetable");
        Button settings = navButton("nav.settings");

        userStatus = new Label();
        bindUserStatus(currentUserName);
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

        getChildren().addAll(logo, timetable, settings, spacer, I18n.languageSelector(), userStatus, avatar);
    }

    public void updateCurrentUserName(String currentUserName) {
        bindUserStatus(currentUserName);
        I18n.bind(avatar.textProperty(), () -> currentUserName != null && !currentUserName.isBlank()
                ? currentUserName.substring(0, 1).toUpperCase()
                : I18n.text("auth.avatar"));
    }

    private void bindUserStatus(String name) {
        I18n.bind(userStatus.textProperty(), () -> name != null && !name.isBlank()
                ? I18n.text("auth.loggedIn", name) : I18n.text("auth.guest"));
    }

    private Button navButton(String text) {
        Button button = I18n.button(text);
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
                : I18n.text("auth.avatar");

        Button avatar = new Button(avatarText);
        I18n.bind(avatar.textProperty(), () -> currentUserName != null && !currentUserName.isBlank() ? currentUserName.substring(0, 1).toUpperCase(I18n.locale()) : I18n.text("auth.avatar"));
        avatar.getStyleClass().add("avatar-button");

        ContextMenu profileMenu = new ContextMenu();
        profileMenu.getStyleClass().add("profile-menu");
        profileMenu.setAutoHide(true);

        if (currentUserName != null && !currentUserName.isBlank()) {
            MenuItem logout = I18n.menuitem("auth.logout");
            logout.getStyleClass().add("profile-menu-item");
            logout.setOnAction(e -> onLogout.run());
            profileMenu.getItems().add(logout);
        } else {
            MenuItem login = I18n.menuitem("auth.login");
            MenuItem register = I18n.menuitem("auth.register");
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

