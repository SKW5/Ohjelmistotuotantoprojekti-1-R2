package com.example.timetable;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();

    private final String[] days = {"MON", "TUE", "WED", "THU", "FRI"};
    private final String[] times = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"};

    @Override
    public void start(Stage stage) {
        Font.loadFont(
                getClass().getResourceAsStream("/fonts/DMSerifDisplay-Regular.ttf"),
                20
        );
        showTimetable();
        root.getStyleClass().add("app");
        root.setTop(createHeader());
        root.setCenter(content);


        Scene scene = new Scene(root, 1180, 720);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        stage.setTitle("Student Timetable");
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox(28);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 22, 14, 22));
        header.getStyleClass().add("header");

        Label logo = new Label("Student Timetable");
        logo.getStyleClass().add("logo");

        Button timetable = navButton("Timetable");
        Button settings = navButton("Settings");
        Button login = new Button("Login");
        login.getStyleClass().add("dark-button");

        timetable.setOnAction(e -> showTimetable());
        settings.setOnAction(e -> showSettings());
        login.setOnAction(e -> showLoginPlaceholder());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(logo, timetable, settings, spacer, login);
        return header;
    }

    private Button navButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-button");
        return b;
    }

    private void showTimetable() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(28));

        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);

        Label title = new Label("Weekly timetable");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Your classes and events for this week");
        subtitle.getStyleClass().add("muted");

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button add = new Button("+ Add event");
        add.getStyleClass().add("dark-button");
        add.setOnAction(e -> showAddEventDialog());

        titleRow.getChildren().addAll(titleBox, spacer, add);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar");
        grid.setGridLinesVisible(true);

        ColumnConstraints timeColumn = new ColumnConstraints(70);
        grid.getColumnConstraints().add(timeColumn);

        for (int i = 0; i < days.length; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(18.6);
            grid.getColumnConstraints().add(column);
        }

        for (int row = 0; row <= times.length; row++) {
            grid.getRowConstraints().add(new RowConstraints(62));
        }

        Label empty = new Label("");
        empty.getStyleClass().add("day-header");
        grid.add(empty, 0, 0);

        for (int col = 0; col < days.length; col++) {
            Label day = new Label(days[col]);
            day.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            day.setAlignment(Pos.CENTER);
            day.getStyleClass().add("day-header");
            grid.add(day, col + 1, 0);
        }

        for (int row = 0; row < times.length; row++) {
            Label time = new Label(times[row]);
            time.setAlignment(Pos.TOP_CENTER);
            time.getStyleClass().add("time-label");
            grid.add(time, 0, row + 1);
        }

        addEvent(grid, 0, 1, "Math", "Room 201", "blue");
        addEvent(grid, 0, 3, "Programming", "Lab 2", "green");
        addEvent(grid, 1, 2, "English", "Room 105", "yellow");
        addEvent(grid, 2, 4, "Physics", "Room 302", "peach");
        addEvent(grid, 3, 1, "Project", "Library", "purple");
        addEvent(grid, 4, 5, "PE", "Gym", "blue");
        addEvent(grid, 2, 6, "Lunch", "Cafeteria", "yellow");

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("calendar-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        page.getChildren().addAll(titleRow, scroll);
        content.getChildren().setAll(page);
    }

    private void addEvent(
            GridPane grid,
            int day,
            int row,
            String name,
            String room,
            String style
    ) {
        VBox event = new VBox(2);
        event.setPadding(new Insets(8));
        event.getStyleClass().addAll("event", "event-" + style);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("event-name");

        Label roomLabel = new Label(room);
        roomLabel.getStyleClass().add("event-room");

        event.getChildren().addAll(nameLabel, roomLabel);

        grid.add(event, day + 1, row + 1);
    }

    private void showSettings() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(28));

        Label title = new Label("System settings");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label(
                "Manage your timetable and display preferences"
        );
        subtitle.getStyleClass().add("muted");

        HBox cards = new HBox(20);
        cards.setFillHeight(true);

        VBox profile = settingsCard(
                "About the profile",
                "Basic information used by the timetable.",
                field("Username", "student"),
                field("Email", "student@example.com"),
                field("Course", "INSINÖÖRI?")
        );

        VBox notifications = settingsCard(
                "Notification settings",
                "Choose how you want to be notified.",
                toggleRow("Email notifications", true),
                toggleRow("Schedule reminders", true)
        );

        cards.getChildren().addAll(profile, notifications);

        page.getChildren().addAll(title, subtitle, cards);
        content.getChildren().setAll(page);
    }

    private VBox settingsCard(String title, String subtitle, Node... nodes) {
        VBox card = new VBox(13);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));
        card.setPrefWidth(420);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("muted");

        card.getChildren().addAll(titleLabel, subtitleLabel);

        card.getChildren().addAll(nodes);

        return card;
    }

    private VBox field(String label, String value) {
        VBox box = new VBox(5);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("small-label");

        TextField field = new TextField(value);
        field.setPrefHeight(34);

        box.getChildren().addAll(labelNode, field);
        return box;
    }

    private HBox toggleRow(String text, boolean selected) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(text);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        CheckBox check = new CheckBox();
        check.setSelected(selected);

        row.getChildren().addAll(label, spacer, check);
        return row;
    }

    private void showAddEventDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add event");

        VBox box = new VBox(12);
        box.setPadding(new Insets(22));
        box.setPrefWidth(390);
        box.getStyleClass().add("dialog");

        Label title = new Label("Add new event");
        title.getStyleClass().add("section-title");

        TextField name = new TextField();
        name.setPromptText("Event name");

        ComboBox<String> day = new ComboBox<>();
        day.getItems().addAll(List.of(days));
        day.setValue("MON");

        ComboBox<String> time = new ComboBox<>();
        time.getItems().addAll(List.of(times));
        time.setValue("08:00");

        TextField room = new TextField();
        room.setPromptText("Room / location");

        ComboBox<String> color = new ComboBox<>();
        color.getItems().addAll("Blue", "Green", "Yellow", "Purple");
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

        Scene scene = new Scene(box);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private VBox labeled(String text, Control control) {
        Label label = new Label(text);
        label.getStyleClass().add("small-label");

        VBox box = new VBox(5, label, control);
        return box;
    }

    private void showLoginPlaceholder() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Login");
        alert.setHeaderText("Ei oo viel mitään, vaa placeholder");
        alert.setContentText(
                "Ku tehään database sitte saatte kirjautua"
        );

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
