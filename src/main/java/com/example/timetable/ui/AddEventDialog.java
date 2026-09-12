package com.example.timetable.ui;

import com.example.timetable.service.AddEvent;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import javafx.stage.Stage;

public class AddEventDialog {

    private static final String[] TIMES = {
            "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00",
            "16:00", "17:00", "18:00", "19:00",
            "20:00", "21:00"
    };


    public void show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add event");

        VBox box = createContent(dialog);

        Scene scene = new Scene(box);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private VBox createContent(Stage dialog) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(22));
        box.setPrefWidth(390);
        box.getStyleClass().add("dialog");

        Label title = new Label("Add new event");
        title.getStyleClass().add("section-title");

        TextField name = new TextField();
        name.setPromptText("Event name");

        ComboBox<String> day = new ComboBox<>(
                FXCollections.observableArrayList()
        );
        day.setValue("MON");

        ComboBox<String> time = new ComboBox<>(
                FXCollections.observableArrayList(TIMES)
        );
        time.setValue("08:00");

        TextField room = new TextField();
        room.setPromptText("Room / location");

        ComboBox<String> color = new ComboBox<>(
                FXCollections.observableArrayList(
                        "Blue", "Green", "Yellow", "Purple"
                )
        );
        color.setValue("Blue");

        VBox nameBox = labeled("Event", name);
        VBox dayBox = labeled("Day", day);
        VBox timeBox = labeled("Time", time);
        VBox roomBox = labeled("Location", room);
        VBox colorBox = labeled("Color", color);

        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button cancel = new Button("Cancel");
        Button save = new Button("Add");
        save.getStyleClass().add("dark-button");

        cancel.setOnAction(e -> dialog.close());

        // Frontend-only: no database/backend yet.
        save.setOnAction(e -> dialog.close());

        buttons.getChildren().addAll(cancel, save);

        box.getChildren().addAll(
                title,
                nameBox,
                dayBox,
                timeBox,
                roomBox,
                colorBox,
                buttons
        );

        return box;
    }

    private VBox labeled(String text, Control control) {
        Label label = new Label(text);
        label.getStyleClass().add("small-label");

        return new VBox(5, label, control);
    }
}
