package com.example.timetable.ui;

import com.example.timetable.service.AddEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class TimetableView extends VBox {

    private static final String[] DAYS = {"MON", "TUE", "WED", "THU", "FRI"};
    private static final String[] TIMES = {
            "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00"
    };

    private final AddEvent addEventService;

    public TimetableView(AddEvent addEventService) {
        this.addEventService = addEventService;

        setSpacing(24);
        setPadding(new Insets(32));

        HBox titleRow = createTitleRow();
        GridPane grid = createCalendar();

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("calendar-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(titleRow, scroll);
    }

    private HBox createTitleRow() {
        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);

        Label title = new Label("Weekly timetable");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Your classes and events for this week");
        subtitle.getStyleClass().add("muted");

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button add = new Button("+ New event");
        add.getStyleClass().addAll("dark-button", "primary-button", "new-event-button");
        add.setOnAction(e -> new AddEventDialog(addEventService).show());
        titleRow.getChildren().addAll(titleBox, spacer, add);
        return titleRow;
    }

    private GridPane createCalendar() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar");
        grid.setGridLinesVisible(false);

        createColumns(grid);
        createRows(grid);
        createHeaders(grid);
        createTimeLabels(grid);
        createCalendarCells(grid);
        addSampleEvents(grid);

        return grid;
    }

    private void createColumns(GridPane grid) {
        grid.getColumnConstraints().add(new ColumnConstraints(70));

        for (int i = 0; i < DAYS.length; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(18.6);
            grid.getColumnConstraints().add(column);
        }
    }

    private void createRows(GridPane grid) {
        for (int row = 0; row <= TIMES.length; row++) {
            grid.getRowConstraints().add(new RowConstraints(68));
        }
    }

    private void createHeaders(GridPane grid) {
        Label empty = new Label("");
        empty.getStyleClass().addAll("day-header", "corner-header");
        grid.add(empty, 0, 0);

        for (int col = 0; col < DAYS.length; col++) {
            Label day = new Label(spacedDayLabel(DAYS[col]));
            day.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            day.setAlignment(Pos.CENTER);
            day.getStyleClass().addAll("day-header", "day-short");
            grid.add(day, col + 1, 0);
        }
    }

    private void createTimeLabels(GridPane grid) {
        for (int row = 0; row < TIMES.length; row++) {
            Label time = new Label(TIMES[row]);
            time.setAlignment(Pos.TOP_CENTER);
            time.getStyleClass().add("time-label");
            grid.add(time, 0, row + 1);
        }
    }

    private void createCalendarCells(GridPane grid) {
        for (int row = 0; row < TIMES.length; row++) {
            for (int day = 0; day < DAYS.length; day++) {
                Region cell = new Region();
                cell.getStyleClass().add("calendar-cell");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                grid.add(cell, day + 1, row + 1);
            }
        }
    }

    private void addSampleEvents(GridPane grid) {
        addEvent(grid, 0, 1, "Math", "Room 201", "blue");
        addEvent(grid, 0, 3, "Programming", "Lab 2", "green");
        addEvent(grid, 1, 2, "English", "Room 105", "yellow");
        addEvent(grid, 2, 4, "Physics", "Room 302", "peach");
        addEvent(grid, 3, 1, "Project", "Library", "purple");
        addEvent(grid, 4, 5, "PE", "Gym", "blue");
        addEvent(grid, 2, 6, "Lunch", "Cafeteria", "yellow");
    }

    private void addEvent(
            GridPane grid,
            int day,
            int row,
            String name,
            String room,
            String style
    ) {
        VBox event = new VBox(3);
        event.setPadding(new Insets(9, 10, 8, 10));
        event.getStyleClass().addAll("event", "event-block", "event-" + style);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("event-name");

        Label timeLabel = new Label(timeRange(row));
        timeLabel.getStyleClass().add("event-time");

        Label roomLabel = new Label(room);
        roomLabel.getStyleClass().add("event-room");

        event.getChildren().addAll(nameLabel, timeLabel, roomLabel);
        grid.add(event, day + 1, row + 1);
    }

    private String timeRange(int row) {
        int startIndex = Math.max(0, Math.min(row, TIMES.length - 1));
        int endIndex = Math.max(0, Math.min(row + 1, TIMES.length - 1));

        return TIMES[startIndex] + "-" + TIMES[endIndex];
    }

    private String spacedDayLabel(String day) {
        return String.join(" ", day.split(""));
    }
}
