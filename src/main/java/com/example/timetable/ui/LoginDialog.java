package com.example.timetable.ui;

import javafx.scene.control.Alert;

public class LoginDialog {

    public void show() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Login");
        alert.setHeaderText("Ei oo viel mitään, vaa placeholder");
        alert.setContentText(
                "Ku tehään database sitte saatte kirjautua"
        );

        alert.showAndWait();
    }
}

