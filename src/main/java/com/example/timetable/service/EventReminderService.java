package com.example.timetable.service;

import com.example.timetable.model.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EventReminderService {

    private static final long REMINDER_WINDOW_MINUTES = 15;

    private final AddEvent addEventService;
    private final Consumer<Event> reminderHandler;
    private final Set<String> remindedEvents = new HashSet<>();
    private boolean enabled = true;

    public EventReminderService(AddEvent addEventService, Consumer<Event> reminderHandler) {
        this.addEventService = addEventService;
        this.reminderHandler = reminderHandler;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void checkUpcomingEvents(int userId, LocalDateTime now) {
        if (!enabled || userId <= 0 || addEventService == null) return;

        LocalDate today = now.toLocalDate();
        for (Event event : addEventService.getEventsBetween(userId, today, today.plusDays(2))) {
            if (event == null || event.getEvent_date() == null || event.getStart_time() == null) continue;

            LocalDateTime start = LocalDateTime.of(event.getEvent_date(), event.getStart_time());
            if (!start.isAfter(now) || start.isAfter(now.plusMinutes(REMINDER_WINDOW_MINUTES))) continue;

            String eventKey = userId + "|" + event.getEvent_id() + "|" + event.getEvent_date() + "|"
                    + event.getStart_time() + "|" + event.getTitle();
            if (remindedEvents.add(eventKey)) {
                reminderHandler.accept(event);
            }
        }
    }
}