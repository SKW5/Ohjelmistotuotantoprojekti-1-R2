package com.example.timetable.model;

public class UserProfile {

    private final int userId;
    private final String username;
    private final String email;
    private final String major;

    public UserProfile(int userId, String username, String email, String major) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.major = major;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getMajor() {
        return major;
    }
}
