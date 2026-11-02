package com.example.timetable.ui;

import com.example.timetable.i18n.I18n;

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
        I18n.orient(this);
        getStyleClass().add("auth-screen");
        var languages = I18n.languageSelector();
        StackPane.setAlignment(languages, Pos.TOP_LEFT);
        StackPane.setMargin(languages, new Insets(18, 22, 0, 22));
        getChildren().add(languages);

        Button close = closeButton(onClose);
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        StackPane.setMargin(close, new Insets(18, 22, 0, 0));

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(390);
        card.getStyleClass().addAll("card", "auth-card", "login-card");

        Label title = I18n.label("auth.login");
        title.getStyleClass().add("card-heading");

        TextField email = new TextField();
        I18n.bind(email.promptTextProperty(), "auth.email");
        email.getStyleClass().add("field-input");
        email.setMaxWidth(Double.MAX_VALUE);

        PasswordField password = new PasswordField();
        I18n.bind(password.promptTextProperty(), "auth.password");
        password.getStyleClass().add("field-input");
        password.setMaxWidth(Double.MAX_VALUE);

        Button submit = I18n.button("auth.submit");
        submit.getStyleClass().add("primary-button");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> {
            String emailValue = email.getText().trim();
            String passwordValue = password.getText();

            if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                showMessage(Alert.AlertType.WARNING, "auth.missing", "auth.enterCredentials");
                return;
            }

            if (loginRepository == null || !loginRepository.loginUser(emailValue, passwordValue)) {
                showMessage(Alert.AlertType.ERROR, "auth.failed", "auth.checkCredentials");
                return;
            }

            onLoginSuccess.accept(emailValue);
        });

        Button switchToRegister = I18n.button("auth.registerLink");
        switchToRegister.getStyleClass().add("link-button");
        switchToRegister.setOnAction(e -> onRegister.run());

        card.getChildren().addAll(
                title,
                labeledField("auth.email", email),
                labeledField("auth.password", password),
                submit,
                switchToRegister
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
