package com.example.timetable.repository;

import java.sql.Connection;

public class loginRepository {

    private final Connection connection;

    public loginRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean loginUser(String username, String password) {
        return true;
    }


}
