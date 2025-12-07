package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.hellofx.model.WeeklySession;

public class TimetableDAO {
    public List<WeeklySession> getWeeklySessions(LocalDate weekStart) throws SQLException {
        LocalDate weekEnd = weekStart.plusDays(7);
        String sql = """
             SELECT c.name AS coach_name,
                   cl.name AS client_name,
                   ts.startDate,
                   ts.endDate,
                   ts.title,
                   0 AS is_availability
            FROM training_sessions ts
            JOIN coaches c ON c.id = ts.coach_id
            JOIN clients cl ON cl.id = ts.client_id
            WHERE ts.startDate >= ? AND ts.startDate < ?
            UNION ALL
             SELECT c.name AS coach_name,
                   'Available' AS client_name,
                   ca.startDate,
                   ca.endDate,
                   COALESCE(ca.note, 'Available'),
                   1 AS is_availability
            FROM coach_availability ca
            JOIN coaches c ON c.id = ca.coach_id
            WHERE ca.startDate >= ? AND ca.startDate < ?
            ORDER BY coach_name, startDate
                """;

            List<WeeklySession> results = new ArrayList<>();

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setTimestamp(1, Timestamp.valueOf(weekStart.atStartOfDay()));
                stmt.setTimestamp(2, Timestamp.valueOf(weekEnd.atStartOfDay()));
                stmt.setTimestamp(3, Timestamp.valueOf(weekStart.atStartOfDay()));
                stmt.setTimestamp(4, Timestamp.valueOf(weekEnd.atStartOfDay()));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalDateTime startDt = rs.getTimestamp("startDate").toLocalDateTime();
                        LocalDateTime endDt = rs.getTimestamp("endDate").toLocalDateTime();
                        results.add(new WeeklySession(
                            rs.getString("coach_name"),
                            rs.getString("client_name"),
                            startDt.toLocalDate().getDayOfWeek(),
                            startDt.toLocalTime(),
                            endDt.toLocalTime(),
                            rs.getString("title")
                        ));
                    }
                }
            return results;
            }
    }

    public void addTrainingSession(Long clientId, Long coachId, LocalDateTime start, LocalDateTime end, String title) throws SQLException {
        String sql = """
            INSERT INTO training_sessions (client_id, coach_id, startDate, endDate, title)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, clientId);
            stmt.setLong(2, coachId);
            stmt.setTimestamp(3, Timestamp.valueOf(start));
            stmt.setTimestamp(4, Timestamp.valueOf(end));
            stmt.setString(5, title);
            stmt.executeUpdate();
        }
    }
}
