package com.example.timetable.ui;

import com.example.timetable.repository.loginRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class LoginView extends StackPane {

    public LoginView(
            loginRepository loginRepository,
            Runnable onRegister,
            Runnable onClose,
            Consumer<String> onLoginSuccess
    ) {
        getStyleClass().add("auth-screen");

        Button close = closeButton(onClose);
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        StackPane.setMargin(close, new Insets(18, 22, 0, 0));

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(390);
        card.getStyleClass().addAll("card", "auth-card", "login-card");

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
        submit.setOnAction(e -> {
            String emailValue = email.getText().trim();
            String passwordValue = password.getText();

            if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                showMessage(Alert.AlertType.WARNING, "Missing details", "Enter your email and password.");
                return;
            }

            if (loginRepository == null || !loginRepository.loginUser(emailValue, passwordValue)) {
                showMessage(Alert.AlertType.ERROR, "Login failed", "Check your email, password, and MariaDB connection.");
                return;
            }

            onLoginSuccess.accept(emailValue);
        });

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

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Button closeButton(Runnable onSkip) {
        Button close = new Button("✕");
        close.getStyleClass().add("close-button");
        close.setOnAction(e -> onSkip.run());
        return close;
    }
}
