package com.example.timetable.service;

import com.example.timetable.model.User;
import com.example.timetable.repository.register;

public class RegisterUser {

    private final register register;

    public RegisterUser(register register) {
        this.register = register;
    }

    public boolean registerUser(User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            System.err.println("Username cannot be empty.");
            return false;
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            System.err.println("Email cannot be empty.");
            return false;
        }
        if (user.getPassword_hash() == null || user.getPassword_hash().isEmpty()) {
            System.err.println("Password cannot be empty.");
            return false;
        }

        return register.registerUser(user);
    }

}
