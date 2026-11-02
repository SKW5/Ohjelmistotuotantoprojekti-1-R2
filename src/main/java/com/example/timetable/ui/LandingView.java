package com.example.timetable.ui;

import com.example.timetable.i18n.I18n;

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
        I18n.orient(this);
        getStyleClass().add("auth-screen");
        var languages = I18n.languageSelector();
        StackPane.setAlignment(languages, Pos.TOP_LEFT);
        StackPane.setMargin(languages, new Insets(18, 22, 0, 22));
        getChildren().add(languages);

        Button close = closeButton(onSkip);
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        StackPane.setMargin(close, new Insets(18, 22, 0, 0));

        VBox center = new VBox(18);
        center.setAlignment(Pos.CENTER);
        center.setMaxWidth(760);
        center.getStyleClass().add("landing-panel");

        Label title = I18n.label("app.brand");
        title.getStyleClass().add("brand-title");
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setWrapText(true);
        title.setMinWidth(0);
        title.setMaxWidth(Double.MAX_VALUE);
        title.prefWidthProperty().bind(center.widthProperty());

        Button login = I18n.button("auth.login");
        login.getStyleClass().add("primary-button");
        login.setPrefWidth(390);
        login.setMaxWidth(390);
        login.setOnAction(e -> onLogin.run());

        Button register = I18n.button("auth.register");
        register.getStyleClass().add("secondary-button");
        register.setPrefWidth(390);
        register.setMaxWidth(390);
        register.setOnAction(e -> onRegister.run());

        center.getChildren().addAll(title, login, register);
        getChildren().addAll(center, close);
    }

    private Button closeButton(Runnable onSkip) {
        Button close = I18n.button("common.close");
        close.getStyleClass().add("close-button");
        I18n.bind(close.accessibleTextProperty(), "common.continueGuest");
        close.setOnAction(e -> onSkip.run());
        return close;
    }
}
