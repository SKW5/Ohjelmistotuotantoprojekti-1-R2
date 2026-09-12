package com.example.timetable.model;
import java.time.LocalDate;
import java.time.LocalTime;


public class Event {
    private int event_id;
    private String title;
    private LocalTime start_time;
    private LocalTime end_time;
    private LocalDate event_date;
    private String location;
    private int course_id;

    public Event(int event_id, String title, LocalTime start_time, LocalTime end_time, LocalDate event_date, String location, int course_id) {
        this.event_id = event_id;
        this.title = title;
        this.start_time = start_time;
        this.end_time = end_time;
        this.event_date = event_date;
        this.location = location;
        this.course_id = course_id;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public int getCourse_id() {
        return course_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalTime getStart_time() {
        return start_time;
    }

    public void setStart_time(LocalTime start_time) {
        this.start_time = start_time;
    }

    public LocalTime getEnd_time() {
        return end_time;
    }

    public void setEnd_time(LocalTime end_time) {
        this.end_time = end_time;
    }

    public LocalDate getEvent_date() {
        return event_date;
    }

    public void setEvent_date(LocalDate event_date) {
        this.event_date = event_date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
