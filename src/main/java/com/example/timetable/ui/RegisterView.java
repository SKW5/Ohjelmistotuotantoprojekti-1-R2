package com.example.timetable.ui;

import com.example.timetable.i18n.I18n;

import com.example.timetable.model.User;
import com.example.timetable.service.RegisterUser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.sql.SQLException;

public class RegisterView extends StackPane {

    public RegisterView(
            RegisterUser registerUserService,
            Runnable onLogin,
            Runnable onSkip
    ) {
        I18n.orient(this);
        getStyleClass().add("auth-screen");
        var languages = I18n.languageSelector();
        StackPane.setAlignment(languages, Pos.TOP_LEFT);
        StackPane.setMargin(languages, new Insets(18, 22, 0, 22));
        getChildren().add(languages);

        Button close = closeButton(onSkip);
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        StackPane.setMargin(close, new Insets(18, 22, 0, 0));

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(390);
        card.getStyleClass().addAll("card", "auth-card", "login-card");

        Label title = I18n.label("auth.register");
        title.getStyleClass().add("card-heading");

        TextField fullName = new TextField();
        I18n.bind(fullName.promptTextProperty(), "auth.username");
        fullName.getStyleClass().add("field-input");
        fullName.setMaxWidth(Double.MAX_VALUE);

        TextField email = new TextField();
        I18n.bind(email.promptTextProperty(), "auth.email");
        email.getStyleClass().add("field-input");
        email.setMaxWidth(Double.MAX_VALUE);

        PasswordField password = new PasswordField();
        I18n.bind(password.promptTextProperty(), "auth.password");
        password.getStyleClass().add("field-input");
        password.setMaxWidth(Double.MAX_VALUE);

        PasswordField confirmPassword = new PasswordField();
        I18n.bind(confirmPassword.promptTextProperty(), "auth.confirm");
        confirmPassword.getStyleClass().add("field-input");
        confirmPassword.setMaxWidth(Double.MAX_VALUE);

        Button submit = I18n.button("auth.register");
        submit.getStyleClass().add("primary-button");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> {
            String username = fullName.getText().trim();
            String emailValue = email.getText().trim();
            String passwordValue = password.getText();

            if (username.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty()) {
                showMessage(Alert.AlertType.WARNING, "auth.missing", "auth.completeFields");
                return;
            }

            if (!passwordValue.equals(confirmPassword.getText())) {
                showMessage(Alert.AlertType.WARNING, "auth.passwordMismatch", "auth.repeatPassword");
                return;
            }

            if (registerUserService == null) {
                showMessage(Alert.AlertType.ERROR, "auth.registrationFailed", "auth.databaseUnavailable");
                return;
            }

            User user = new User(username, emailValue, passwordValue);
            try {
                if (!registerUserService.registerUser(user)) {
                    showMessage(Alert.AlertType.ERROR, "auth.registrationFailed", "auth.enterRegistration");
                    return;
                }
            } catch (SQLException error) {
                String message = error.getErrorCode() == 1062
                        ? "auth.duplicate"
                        : "auth.registrationError";
                showMessage(Alert.AlertType.ERROR, "auth.registrationFailed", message);
                return;
            }

            showMessage(Alert.AlertType.INFORMATION, "auth.registrationComplete", "auth.accountCreated");
            onSkip.run();
        });

        Button switchToLogin = I18n.button("auth.loginLink");
        switchToLogin.getStyleClass().add("link-button");
        switchToLogin.setOnAction(e -> onLogin.run());

        card.getChildren().addAll(
                title,
                labeledField("auth.username", fullName),
                labeledField("auth.email", email),
                labeledField("auth.password", password),
                labeledField("auth.confirm", confirmPassword),
                submit,
                switchToLogin
        );

        getChildren().addAll(card, close);
    }

    private VBox labeledField(String labelText, TextField input) {
        Label label = I18n.label(labelText);
        label.getStyleClass().add("field-label");

        return new VBox(5, label, input);
    }

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        I18n.bind(alert.titleProperty(), title);
        alert.setHeaderText(null);
        I18n.bind(alert.contentTextProperty(), message);
        I18n.localizeAlert(alert);
        alert.showAndWait();
    }

    private Button closeButton(Runnable onSkip) {
        Button close = I18n.button("common.close");
        close.getStyleClass().add("close-button");
        I18n.bind(close.accessibleTextProperty(), "common.continueGuest");
        close.setOnAction(e -> onSkip.run());
        return close;
    }
}
