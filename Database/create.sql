CREATE DATABASE student_timetable;

USE student_timetable;

CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE courses (
                         course_id INT AUTO_INCREMENT PRIMARY KEY,
                         course_name VARCHAR(100) NOT NULL,
                         course_code VARCHAR(20),
                         color VARCHAR(20) NOT NULL
);

CREATE TABLE user_courses (
                              user_id INT NOT NULL,
                              course_id INT NOT NULL,

                              PRIMARY KEY (user_id, course_id),

                              FOREIGN KEY (user_id)
                                  REFERENCES users(user_id)
                                  ON DELETE CASCADE,

                              FOREIGN KEY (course_id)
                                  REFERENCES courses(course_id)
                                  ON DELETE CASCADE
);

CREATE TABLE timetable_events (
                                  event_id INT AUTO_INCREMENT PRIMARY KEY,
                                  user_id INT NOT NULL,
                                  course_id INT NOT NULL,

                                  title VARCHAR(100) NOT NULL,
                                  event_date DATE NOT NULL,
                                  start_time TIME NOT NULL,
                                  end_time TIME NOT NULL,
                                  location VARCHAR(100),

                                  FOREIGN KEY (user_id)
                                      REFERENCES users(user_id)
                                      ON DELETE CASCADE,

                                  FOREIGN KEY (course_id)
                                      REFERENCES courses(course_id)
                                      ON DELETE CASCADE
);

CREATE TABLE reminders (
                           reminder_id INT AUTO_INCREMENT PRIMARY KEY,
                           user_id INT NOT NULL,
                           event_id INT NOT NULL,
                           reminder_time INT NOT NULL,

                           FOREIGN KEY (user_id)
                               REFERENCES users(user_id)
                               ON DELETE CASCADE,

                           FOREIGN KEY (event_id)
                               REFERENCES timetable_events(event_id)
                               ON DELETE CASCADE
);