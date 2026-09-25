package com.example.timetable.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView extends StackPane {

    public LoginView(
            Runnable onRegister,
            Runnable onSkip
    ) {
        getStyleClass().add("auth-screen");

        Button close = closeButton(onSkip);
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        StackPane.setMargin(close, new Insets(18, 22, 0, 0));

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(390);
        card.getStyleClass().addAll("card", "auth-card");

        Label title = new Label("Log in");
        title.getStyleClass().add("card-heading");

        TextField email = new TextField();
        email.setPromptText("Email");
        email.getStyleClass().add("field-input");
        email.setMaxWidth(Double.MAX_VALUE);

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.getStyleClass().add("field-input");
        password.setMaxWidth(Double.MAX_VALUE);

        Button submit = new Button("Log In");
        submit.getStyleClass().add("primary-button");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> System.out.println("Login placeholder"));

        Button switchToRegister = new Button("Don't have an account? Register");
        switchToRegister.getStyleClass().add("link-button");
        switchToRegister.setOnAction(e -> onRegister.run());

        card.getChildren().addAll(
                title,
                labeledField("Email", email),
                labeledField("Password", password),
                submit,
                switchToRegister
        );

        getChildren().addAll(card, close);
    }

    private VBox labeledField(String labelText, TextField input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        return new VBox(5, label, input);
    }

    private Button closeButton(Runnable onSkip) {
        Button close = new Button("✕");
        close.getStyleClass().add("close-button");
        close.setOnAction(e -> onSkip.run());
        return close;
    }
}
