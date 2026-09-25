package com.example.timetable.ui;

import com.example.timetable.model.Event;
import com.example.timetable.service.AddEvent;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalTime;

public class AddEventDialog {

    private static final String[] TIMES = {
            "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00",
            "16:00", "17:00", "18:00", "19:00",
            "20:00", "21:00"
    };

    private final AddEvent addEventService;

    public AddEventDialog(AddEvent addEventService) {
        this.addEventService = addEventService;
    }

    public void show() {

        Stage dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add event");

        VBox box = createContent(dialog);

        Scene scene = new Scene(box);

        var stylesheet =
                getClass().getResource("/styles.css");

        if (stylesheet != null) {
            scene.getStylesheets().add(
                    stylesheet.toExternalForm()
            );
        }

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private VBox createContent(Stage dialog) {

        VBox box = new VBox(14);

        box.setPadding(new Insets(26));
        box.setPrefWidth(390);

        box.getStyleClass().addAll("dialog", "event-dialog");

        Label title = new Label("Add new event");
        title.getStyleClass().addAll("section-title", "card-heading");

        // -------------------------
        // EVENT NAME
        // -------------------------

        TextField name = new TextField();
        name.setPromptText("Event name");

        // -------------------------
        // DATE
        // -------------------------

        DatePicker datePicker = new DatePicker();

        datePicker.setValue(
                java.time.LocalDate.now()
        );

        // -------------------------
        // START TIME
        // -------------------------

        ComboBox<String> startTime =
                new ComboBox<>(
                        FXCollections.observableArrayList(TIMES)
                );

        startTime.setValue("08:00");

        // -------------------------
        // END TIME
        // -------------------------

        ComboBox<String> endTime =
                new ComboBox<>(
                        FXCollections.observableArrayList(TIMES)
                );

        endTime.setValue("09:00");

        // -------------------------
        // LOCATION
        // -------------------------

        TextField room = new TextField();

        room.setPromptText(
                "Room / location"
        );

        // -------------------------
        // COLOR
        // -------------------------

        ComboBox<String> color =
                new ComboBox<>(
                        FXCollections.observableArrayList(
                                "Blue",
                                "Green",
                                "Yellow",
                                "Purple"
                        )
                );

        color.setValue("Blue");

        // -------------------------
        // LABELLED CONTROLS
        // -------------------------

        VBox nameBox =
                labeled("Event", name);

        VBox dateBox =
                labeled("Date", datePicker);

        VBox startBox =
                labeled("Start time", startTime);

        VBox endBox =
                labeled("End time", endTime);

        VBox roomBox =
                labeled("Location", room);

        VBox colorBox =
                labeled("Color", color);

        // -------------------------
        // BUTTONS
        // -------------------------

        HBox buttons = new HBox(8);

        buttons.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button cancel =
                new Button("Cancel");
        cancel.getStyleClass().add("secondary-button");

        Button save =
                new Button("Add");

        save.getStyleClass().add(
                "primary-button"
        );

        cancel.setOnAction(
                e -> dialog.close()
        );

        // -------------------------
        // SAVE EVENT
        // -------------------------

        save.setOnAction(e -> {

            String eventName =
                    name.getText().trim();

            if (eventName.isEmpty()) {

                name.setStyle(
                        "-fx-border-color: red;"
                );

                return;
            }

            name.setStyle("");

            // Get date
            var eventDate =
                    datePicker.getValue();

            if (eventDate == null) {

                datePicker.setStyle(
                        "-fx-border-color: red;"
                );

                return;
            }

            datePicker.setStyle("");

            try {

                // Convert strings to LocalTime
                LocalTime start =
                        LocalTime.parse(
                                startTime.getValue()
                        );

                LocalTime end =
                        LocalTime.parse(
                                endTime.getValue()
                        );

                // Make sure end is after start
                if (!end.isAfter(start)) {

                    endTime.setStyle(
                            "-fx-border-color: red;"
                    );

                    System.err.println(
                            "End time must be after start time."
                    );

                    return;
                }

                endTime.setStyle("");

                /*
                 * Your Event constructor:
                 *
                 * Event(
                 *     int event_id,
                 *     String title,
                 *     LocalTime start_time,
                 *     LocalTime end_time,
                 *     LocalDate event_date,
                 *     String location,
                 *     int course_id
                 * )
                 */

                Event event = new Event(
                        0,
                        eventName,
                        start,
                        end,
                        eventDate,
                        room.getText().trim(),
                        0
                );

                // Send event to backend
                boolean saved =
                        addEventService.addEvent(event);

                if (saved) {

                    System.out.println(
                            "Event saved successfully!"
                    );

                    dialog.close();

                } else {

                    System.err.println(
                            "Failed to save event."
                    );
                }

            } catch (Exception ex) {

                ex.printStackTrace();

                System.err.println(
                        "Invalid event data."
                );
            }
        });

        buttons.getChildren().addAll(
                cancel,
                save
        );

        box.getChildren().addAll(
                title,
                nameBox,
                dateBox,
                startBox,
                endBox,
                roomBox,
                colorBox,
                buttons
        );

        return box;
    }

    private VBox labeled(
            String text,
            Control control
    ) {

        Label label =
                new Label(text);

        label.getStyleClass().add(
                "small-label"
        );
        label.getStyleClass().add("field-label");

        if (!control.getStyleClass().contains("field-input")) {
            control.getStyleClass().add("field-input");
        }

        return new VBox(
                6,
                label,
                control
        );
    }
}
