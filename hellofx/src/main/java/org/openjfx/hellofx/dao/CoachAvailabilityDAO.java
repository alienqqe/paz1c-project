package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CoachAvailabilityDAO {

    public void addAvailability(Long coachId, LocalDateTime start, LocalDateTime end, String note) throws SQLException {
        String sql = """
            INSERT INTO coach_availability (coach_id, startDate, endDate, note)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, coachId);
            stmt.setTimestamp(2, Timestamp.valueOf(start));
            stmt.setTimestamp(3, Timestamp.valueOf(end));
            stmt.setString(4, note);
            stmt.executeUpdate();
        }
    }
}
