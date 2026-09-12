import com.example.timetable.model.Event;
import com.example.timetable.repository.eventRepository;
import com.example.timetable.service.AddEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class TestEvent {

    public static void main(String[] args) {

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mariadb://localhost:3306/student_timetable",
                    "student",
                    "student"
            );

            eventRepository repository =
                    new eventRepository(connection);

            AddEvent service =
                    new AddEvent(repository);

            // Hardcoded test event
            Event event = new Event(1,
                    "test event",
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    LocalDate.of(2024, 6, 15),
                    "Test Location",
                    1
            );

            boolean success = service.addEvent(event);

            if (success) {
                System.out.println("Event successfully added!");
            } else {
                System.out.println("Failed to add event.");
            }

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
