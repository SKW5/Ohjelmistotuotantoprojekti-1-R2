package com.example.timetable.ui;

import com.example.timetable.model.Event;
import com.example.timetable.service.AddEvent;

import javafx.application.Platform;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AddEventDialog {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] START_TIMES = {
            "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
            "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
            "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
    };
    private static final String[] END_TIMES = {
            "01:00", "02:00", "03:00", "04:00", "05:00", "06:00",
            "07:00", "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00",
            "19:00", "20:00", "21:00", "22:00", "23:00", "23:59"
    };
    private static final String[] COLORS = {
            "Blue",
            "Green",
            "Yellow",
            "Peach",
            "Purple"
    };

    private final AddEvent addEventService;
    private final int userId;
    private final Runnable onSaved;

    public AddEventDialog(AddEvent addEventService, int userId) {
        this(addEventService, userId, () -> { });
    }

    public AddEventDialog(AddEvent addEventService, int userId, Runnable onSaved) {
        this.addEventService = addEventService;
        this.userId = userId;
        this.onSaved = onSaved == null ? () -> { } : onSaved;
    }

    public void show() {
        show(null);
    }

    public void show(Event eventToEdit) {

        Stage dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(eventToEdit == null ? "Add event" : "Edit event");

        VBox box = createContent(dialog, eventToEdit);

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
        return createContent(dialog, null);
    }

    private VBox createContent(Stage dialog, Event eventToEdit) {

        VBox box = new VBox(14);
        boolean editMode = eventToEdit != null;

        box.setPadding(new Insets(26));
        box.setPrefWidth(390);

        box.getStyleClass().addAll("dialog", "event-dialog");

        Label title = new Label(editMode ? "Edit event" : "Add new event");
        title.getStyleClass().addAll("section-title", "card-heading");

        // -------------------------
        // EVENT NAME
        // -------------------------

        TextField name = new TextField();
        name.setPromptText("Event name");
        if (editMode) {
            name.setText(eventToEdit.getTitle());
        }

        // -------------------------
        // DATE
        // -------------------------

        DatePicker datePicker = new DatePicker();

        datePicker.setValue(
                editMode ? eventToEdit.getEvent_date() : LocalDate.now()
        );

        // -------------------------
        // START TIME
        // -------------------------

        ComboBox<String> startTime =
                new ComboBox<>(
                        FXCollections.observableArrayList(START_TIMES)
                );

        startTime.setValue("08:00");
        if (editMode && eventToEdit.getStart_time() != null) {
            startTime.setValue(eventToEdit.getStart_time().format(TIME_FORMAT));
        }

        // -------------------------
        // END TIME
        // -------------------------

        ComboBox<String> endTime =
                new ComboBox<>(
                        FXCollections.observableArrayList(END_TIMES)
                );

        endTime.setValue("09:00");
        if (editMode && eventToEdit.getEnd_time() != null) {
            endTime.setValue(eventToEdit.getEnd_time().format(TIME_FORMAT));
        }

        // -------------------------
        // LOCATION
        // -------------------------

        TextField room = new TextField();

        room.setPromptText(
                "Room / location"
        );
        if (editMode) {
            room.setText(eventToEdit.getLocation());
        }

        // -------------------------
        // COLOR
        // -------------------------

        ComboBox<String> color =
                new ComboBox<>(
                        FXCollections.observableArrayList(COLORS)
                );

        color.setValue("Blue");
        if (editMode) {
            color.setValue(displayColor(eventToEdit.getColor()));
        }

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

        Label message = new Label("");
        message.getStyleClass().add("status-error");
        message.setWrapText(true);

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
                new Button(editMode ? "Save" : "Add");

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

                message.setText("Event name is required.");
                return;
            }
            if (eventName.length() > AddEvent.MAX_TITLE_LENGTH) {
                name.setStyle("-fx-border-color: red;");
                message.setText("Event name must be at most " + AddEvent.MAX_TITLE_LENGTH + " characters.");
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

                message.setText("Date is required.");
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

                    message.setText("End time must be after start time.");
                    return;
                }

                endTime.setStyle("");

                String location = room.getText() == null ? "" : room.getText().trim();
                if (location.length() > AddEvent.MAX_LOCATION_LENGTH) {
                    room.setStyle("-fx-border-color: red;");
                    message.setText("Location must be at most " + AddEvent.MAX_LOCATION_LENGTH + " characters.");
                    return;
                }
                room.setStyle("");

                String selectedColor = color.getValue() == null
                        ? "blue"
                        : color.getValue().toLowerCase(Locale.ROOT);
                if (!AddEvent.VALID_COLORS.contains(selectedColor)) {
                    color.setStyle("-fx-border-color: red;");
                    message.setText("Choose a valid event color.");
                    return;
                }
                color.setStyle("");

                Event event = new Event(
                        editMode ? eventToEdit.getEvent_id() : 0,
                        eventName,
                        start,
                        end,
                        eventDate,
                        location,
                        selectedColor
                );

                save.setDisable(true);
                cancel.setDisable(true);
                message.setText("");

                Thread worker = new Thread(() -> {
                    boolean saved = editMode
                            ? addEventService.updateEvent(userId, event)
                            : addEventService.addEvent(userId, event);
                    Platform.runLater(() -> {
                        save.setDisable(false);
                        cancel.setDisable(false);
                        if (saved) {
                            onSaved.run();
                            dialog.close();
                        } else {
                            message.setText(editMode
                                    ? "Could not update this event. It may no longer exist or belongs to another user."
                                    : "Failed to save event. Check the details and try again.");
                        }
                    });
                }, editMode ? "event-update" : "event-save");
                worker.setDaemon(true);
                worker.start();

            } catch (Exception ex) {

                ex.printStackTrace();

                message.setText("Invalid event data.");
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
                message,
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

    private String displayColor(String color) {
        if (color == null || color.isBlank()) return "Blue";

        String lower = color.toLowerCase(Locale.ROOT);
        for (String option : COLORS) {
            if (option.toLowerCase(Locale.ROOT).equals(lower)) {
                return option;
            }
        }
        return "Blue";
    }
}
