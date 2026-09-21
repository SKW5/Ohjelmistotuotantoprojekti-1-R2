package com.example.timetable.model;

public class User {

    private String username;
    private String email;
    private String password_hash;


    public User(String username, String email ,String password_hash) {
        this.username = username;
        this.email = email;
        this.password_hash = password_hash;
    }

    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword_hash() {
        return password_hash;
    }


}
