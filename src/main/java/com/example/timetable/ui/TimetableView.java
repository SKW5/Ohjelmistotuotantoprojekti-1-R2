package com.example.timetable.ui;

import com.example.timetable.service.AddEvent;
import com.example.timetable.model.Event;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimetableView extends VBox {

    private static final String[] DAYS = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
    private static final String[] TIMES = {
            "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00"
    };

    private final AddEvent addEventService;
    private final ScrollPane calendarScroll = new ScrollPane();
    private static final String[] EVENT_STYLES = {"blue", "green", "yellow", "peach", "purple"};

    public TimetableView(AddEvent addEventService) {
        this.addEventService = addEventService;

        setSpacing(24);
        setPadding(new Insets(32));

        HBox titleRow = createTitleRow();
        calendarScroll.setContent(createCalendar());
        calendarScroll.setFitToWidth(true);
        calendarScroll.setFitToHeight(true);
        calendarScroll.getStyleClass().add("calendar-scroll");
        VBox.setVgrow(calendarScroll, Priority.ALWAYS);

        getChildren().addAll(titleRow, calendarScroll);
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
        add.setOnAction(e -> {
            new AddEventDialog(addEventService).show();
            refreshCalendar();
        });
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
        addDatabaseEvents(grid);

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

    private void addDatabaseEvents(GridPane grid) {
        if (addEventService == null) return;
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        List<Event> events = addEventService.getEventsBetween(monday, monday.plusDays(5));
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        for (Event event : events) {
            int day = event.getEvent_date().getDayOfWeek().getValue() - 1;
            int startMinutes = event.getStart_time().getHour() * 60 + event.getStart_time().getMinute();
            int endMinutes = event.getEnd_time().getHour() * 60 + event.getEnd_time().getMinute();
            int firstSlot = (startMinutes - 8 * 60) / 60;
            int lastSlot = (endMinutes - 8 * 60 + 59) / 60;

            if (day < 0 || day >= DAYS.length || firstSlot < 0 || firstSlot >= TIMES.length) continue;
            int rowSpan = Math.max(1, Math.min(TIMES.length - firstSlot, lastSlot - firstSlot));
            addEvent(grid, day, firstSlot, rowSpan, event.getTitle(),
                    event.getStart_time().format(timeFormat) + "-" + event.getEnd_time().format(timeFormat),
                    event.getLocation(), EVENT_STYLES[Math.floorMod(event.getEvent_id(), EVENT_STYLES.length)]);
        }
    }

    private void addEvent(
            GridPane grid,
            int day,
            int row,
            int rowSpan,
            String name,
            String time,
            String room,
            String style
    ) {
        VBox event = new VBox(3);
        event.setPadding(new Insets(9, 10, 8, 10));
        event.getStyleClass().addAll("event", "event-block", "event-" + style);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("event-name");

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("event-time");

        Label roomLabel = new Label(room);
        roomLabel.getStyleClass().add("event-room");

        event.getChildren().addAll(nameLabel, timeLabel, roomLabel);
        grid.add(event, day + 1, row + 1, 1, rowSpan);
    }

    private void refreshCalendar() {
        calendarScroll.setContent(createCalendar());
    }

    private String spacedDayLabel(String day) {
        return String.join(" ", day.split(""));
    }
}
