package com.example.timetable.ui;

import com.example.timetable.i18n.I18n;

import javafx.scene.control.Alert;

public class LoginDialog {

    public void show() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        I18n.bind(alert.titleProperty(), "auth.placeholderTitle");
        I18n.bind(alert.headerTextProperty(), "auth.placeholderHeader");
        I18n.bind(alert.contentTextProperty(), "auth.placeholderBody");
        I18n.localizeAlert(alert);
        alert.showAndWait();
    }
}

