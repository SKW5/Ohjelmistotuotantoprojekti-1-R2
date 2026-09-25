package com.example.timetable.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LandingView extends StackPane {

    public LandingView(
            Runnable onLogin,
            Runnable onRegister,
            Runnable onSkip
    ) {
        getStyleClass().add("auth-screen");

        Button close = closeButton(onSkip);
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        StackPane.setMargin(close, new Insets(18, 22, 0, 0));

        VBox center = new VBox(18);
        center.setAlignment(Pos.CENTER);
        center.setMaxWidth(360);

        Label title = new Label("Student timetable");
        title.getStyleClass().add("brand-title");

        Button login = new Button("Log in");
        login.getStyleClass().add("primary-button");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(e -> onLogin.run());

        Button register = new Button("Register");
        register.getStyleClass().add("secondary-button");
        register.setMaxWidth(Double.MAX_VALUE);
        register.setOnAction(e -> onRegister.run());

        center.getChildren().addAll(title, login, register);
        getChildren().addAll(center, close);
    }

    private Button closeButton(Runnable onSkip) {
        Button close = new Button("✕");
        close.getStyleClass().add("close-button");
        close.setOnAction(e -> onSkip.run());
        return close;
    }
}
