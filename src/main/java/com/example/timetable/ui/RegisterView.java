package com.example.timetable.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RegisterView extends StackPane {

    public RegisterView(
            Runnable onLogin,
            Runnable onSkip
    ) {
        getStyleClass().add("auth-screen");

        Button close = closeButton(onSkip);
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        StackPane.setMargin(close, new Insets(18, 22, 0, 0));

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(390);
        card.getStyleClass().addAll("card", "auth-card", "login-card");

        Label title = new Label("Register");
        title.getStyleClass().add("card-heading");

        TextField fullName = new TextField();
        fullName.setPromptText("Full name");
        fullName.getStyleClass().add("field-input");
        fullName.setMaxWidth(Double.MAX_VALUE);

        TextField email = new TextField();
        email.setPromptText("Email");
        email.getStyleClass().add("field-input");
        email.setMaxWidth(Double.MAX_VALUE);

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.getStyleClass().add("field-input");
        password.setMaxWidth(Double.MAX_VALUE);

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm password");
        confirmPassword.getStyleClass().add("field-input");
        confirmPassword.setMaxWidth(Double.MAX_VALUE);

        Button submit = new Button("Register");
        submit.getStyleClass().add("primary-button");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> System.out.println("Register placeholder"));

        Button switchToLogin = new Button("Already have an account? Log in");
        switchToLogin.getStyleClass().add("link-button");
        switchToLogin.setOnAction(e -> onLogin.run());

        card.getChildren().addAll(
                title,
                labeledField("Full name", fullName),
                labeledField("Email", email),
                labeledField("Password", password),
                labeledField("Confirm password", confirmPassword),
                submit,
                switchToLogin
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
