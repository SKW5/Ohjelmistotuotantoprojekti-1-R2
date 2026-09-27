package com.example.timetable;

import com.example.timetable.model.User;
import com.example.timetable.repository.register;
import com.example.timetable.service.RegisterUser;
import com.example.timetable.service.passwordUtil;

import java.sql.Connection;
import java.sql.DriverManager;

public class registerTest {
    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mariadb://localhost:3306/student_timetable",
                    "student",
                    "student"
            );

            register register = new register(connection);
            RegisterUser service = new RegisterUser(register);

            User user = new User("testi", "al@al", "salasana");
            boolean success = service.registerUser(user);

            if(success) {
                System.out.println("User successfully registered!");
            } else {
                System.out.println("Failed to register user.");
            }

             connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

