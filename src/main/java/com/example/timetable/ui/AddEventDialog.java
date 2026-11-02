package com.example.timetable.ui;

import com.example.timetable.i18n.I18n;

import com.example.timetable.model.Event;
import com.example.timetable.service.AddEvent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

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
    private final DeleteConfirmation deleteConfirmation;

    public AddEventDialog(AddEvent addEventService, int userId) {
        this(addEventService, userId, () -> { });
    }

    public AddEventDialog(AddEvent addEventService, int userId, Runnable onSaved) {
        this(addEventService, userId, onSaved, AddEventDialog::confirmDeleteWithAlert);
    }

    AddEventDialog(AddEvent addEventService, int userId, Runnable onSaved, DeleteConfirmation deleteConfirmation) {
        this.addEventService = addEventService;
        this.userId = userId;
        this.onSaved = onSaved == null ? () -> { } : onSaved;
        this.deleteConfirmation = deleteConfirmation == null
                ? AddEventDialog::confirmDeleteWithAlert
                : deleteConfirmation;
    }

    public void show() {
        show(null);
    }

    public void show(Event eventToEdit) {

        Stage dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL);
        I18n.bind(dialog.titleProperty(), eventToEdit == null ? "event.addTitle" : "event.editTitle");

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
        I18n.orient(box);
        boolean editMode = eventToEdit != null;

        box.setPadding(new Insets(26));
        box.setPrefWidth(390);

        box.getStyleClass().addAll("dialog", "event-dialog");

        Label title = I18n.label(editMode ? "event.editTitle" : "event.newTitle");
        title.getStyleClass().addAll("section-title", "card-heading");

        // EVENT NAME

        TextField name = new TextField();
        I18n.bind(name.promptTextProperty(), "event.namePrompt");
        if (editMode) {
            name.setText(eventToEdit.getTitle());
        }

        // DATE

        DatePicker datePicker = new DatePicker();
        I18n.localize(datePicker);

        datePicker.setValue(
                editMode ? eventToEdit.getEvent_date() : LocalDate.now()
        );

        ComboBox<String> startTime =
                new ComboBox<>(
                        FXCollections.observableArrayList(START_TIMES)
                );

        I18n.localizeTimes(startTime);
        startTime.setValue("08:00");
        if (editMode && eventToEdit.getStart_time() != null) {
            startTime.setValue(eventToEdit.getStart_time().format(TIME_FORMAT));
        }


        // END TIME


        ComboBox<String> endTime =
                new ComboBox<>(
                        FXCollections.observableArrayList(END_TIMES)
                );

        I18n.localizeTimes(endTime);
        endTime.setValue("09:00");
        if (editMode && eventToEdit.getEnd_time() != null) {
            endTime.setValue(eventToEdit.getEnd_time().format(TIME_FORMAT));
        }


        // LOCATION


        TextField room = new TextField();

        I18n.bind(room.promptTextProperty(), "event.locationPrompt");
        if (editMode) {
            room.setText(eventToEdit.getLocation());
        }

        // COLOR

        ComboBox<String> color =
                new ComboBox<>(
                        FXCollections.observableArrayList(COLORS)
                );

        I18n.localizeColors(color);
        color.setValue("Blue");
        if (editMode) {
            color.setValue(displayColor(eventToEdit.getColor()));
        }


        // LABELLED CONTROLS

        VBox nameBox =
                labeled("event.name", name);

        VBox dateBox =
                labeled("event.date", datePicker);

        VBox startBox =
                labeled("event.start", startTime);

        VBox endBox =
                labeled("event.end", endTime);

        VBox roomBox =
                labeled("event.location", room);

        VBox colorBox =
                labeled("event.color", color);

        Label message = new Label("");
        message.getStyleClass().add("status-error");
        message.setWrapText(true);


        // BUTTONS


        HBox buttons = new HBox(8);

        buttons.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button cancel =
                I18n.button("common.cancel");
        cancel.getStyleClass().add("secondary-button");

        Button save =
                I18n.button(editMode ? "common.save" : "common.add");

        save.getStyleClass().add(
                "primary-button"
        );

        Button delete =
                I18n.button("common.delete");
        delete.getStyleClass().addAll("secondary-button", "danger-button");
        delete.setVisible(editMode);
        delete.setManaged(editMode);
        delete.setDisable(userId <= 0);

        cancel.setOnAction(
                e -> dialog.close()
        );


        // SAVE EVENT


        save.setOnAction(e -> {

            String eventName =
                    name.getText().trim();

            if (eventName.isEmpty()) {

                name.setStyle(
                        "-fx-border-color: red;"
                );

                I18n.bind(message.textProperty(), "event.nameRequired");
                return;
            }
            if (eventName.length() > AddEvent.MAX_TITLE_LENGTH) {
                name.setStyle("-fx-border-color: red;");
                I18n.bind(message.textProperty(), "event.nameLength", AddEvent.MAX_TITLE_LENGTH);
                return;
            }

            name.setStyle("");

            // Commit manually typed dates using the selected locale before validation.
            try {
                datePicker.commitValue();
            } catch (RuntimeException invalidDate) {
                I18n.bind(message.textProperty(), "event.invalid");
                return;
            }
            // Get date
            var eventDate =
                    datePicker.getValue();

            if (eventDate == null) {

                datePicker.setStyle(
                        "-fx-border-color: red;"
                );

                I18n.bind(message.textProperty(), "event.dateRequired");
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
                            "event.endAfterStart"
                    );

                    I18n.bind(message.textProperty(), "event.endAfterStart");
                    return;
                }

                endTime.setStyle("");

                String location = room.getText() == null ? "" : room.getText().trim();
                if (location.length() > AddEvent.MAX_LOCATION_LENGTH) {
                    room.setStyle("-fx-border-color: red;");
                    I18n.bind(message.textProperty(), "event.locationLength", AddEvent.MAX_LOCATION_LENGTH);
                    return;
                }
                room.setStyle("");

                String selectedColor = color.getValue() == null
                        ? "blue"
                        : color.getValue().toLowerCase(Locale.ROOT);
                if (!AddEvent.VALID_COLORS.contains(selectedColor)) {
                    color.setStyle("-fx-border-color: red;");
                    I18n.bind(message.textProperty(), "event.invalidColor");
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
                delete.setDisable(true);
                I18n.bind(message.textProperty(), "");

                Thread worker = new Thread(() -> {
                    boolean saved = editMode
                            ? addEventService.updateEvent(userId, event)
                            : addEventService.addEvent(userId, event);
                    Platform.runLater(() -> {
                        save.setDisable(false);
                        cancel.setDisable(false);
                        delete.setDisable(userId <= 0);
                        if (saved) {
                            onSaved.run();
                            dialog.close();
                        } else {
                            I18n.bind(message.textProperty(), editMode
                                    ? "event.updateFailed"
                                    : "event.saveFailed");
                        }
                    });
                }, editMode ? "event-update" : "event-save");
                worker.setDaemon(true);
                worker.start();

            } catch (Exception ex) {

                ex.printStackTrace();

                I18n.bind(message.textProperty(), "event.invalid");
            }
        });

        delete.setOnAction(e -> {
            if (!editMode || userId <= 0) {
                I18n.bind(message.textProperty(), "event.loginToDelete");
                return;
            }
            if (!deleteConfirmation.confirm(eventToEdit)) {
                return;
            }

            save.setDisable(true);
            cancel.setDisable(true);
            delete.setDisable(true);
            I18n.bind(message.textProperty(), "");

            Thread worker = new Thread(() -> {
                boolean deleted;
                try {
                    deleted = addEventService.deleteEvent(userId, eventToEdit.getEvent_id());
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                    deleted = false;
                }

                boolean deletionSucceeded = deleted;
                Platform.runLater(() -> {
                    save.setDisable(false);
                    cancel.setDisable(false);
                    delete.setDisable(false);
                    if (deletionSucceeded) {
                        onSaved.run();
                        dialog.close();
                    } else {
                        I18n.bind(message.textProperty(), "event.deleteFailed");
                    }
                });
            }, "event-delete");
            worker.setDaemon(true);
            worker.start();
        });

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        if (editMode) {
            buttons.getChildren().addAll(
                    delete,
                    buttonSpacer,
                    cancel,
                    save
            );
        } else {
            buttons.getChildren().addAll(
                    cancel,
                    save
            );
        }

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
                I18n.label(text);

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

    private static boolean confirmDeleteWithAlert(Event event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        I18n.orient(confirmation.getDialogPane());
        I18n.bind(confirmation.titleProperty(), "event.deleteTitle");
        I18n.bind(confirmation.headerTextProperty(), "event.deleteConfirm");
        confirmation.setContentText(event == null ? "" : event.getTitle());

        ButtonType cancel = new ButtonType(I18n.text("common.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType delete = new ButtonType(I18n.text("common.delete"), ButtonBar.ButtonData.OK_DONE);
        confirmation.getButtonTypes().setAll(cancel, delete);

        Optional<ButtonType> result = confirmation.showAndWait();
        return result.isPresent() && result.get() == delete;
    }

    @FunctionalInterface
    interface DeleteConfirmation {
        boolean confirm(Event event);
    }
}
