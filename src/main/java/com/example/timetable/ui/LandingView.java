package com.example.timetable.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

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
        center.setMaxWidth(760);
        center.getStyleClass().add("landing-panel");

        Label title = new Label("student time table");
        title.getStyleClass().add("brand-title");
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setWrapText(true);
        title.setMinWidth(0);
        title.setMaxWidth(Double.MAX_VALUE);
        title.prefWidthProperty().bind(center.widthProperty());

        Button login = new Button("Log in");
        login.getStyleClass().add("primary-button");
        login.setPrefWidth(390);
        login.setMaxWidth(390);
        login.setOnAction(e -> onLogin.run());

        Button register = new Button("Register");
        register.getStyleClass().add("secondary-button");
        register.setPrefWidth(390);
        register.setMaxWidth(390);
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
