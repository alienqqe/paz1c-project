package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.model.WeeklySession;
import org.openjfx.hellofx.utils.Database;
import org.springframework.jdbc.core.RowMapper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TimetableDAO {

    private final RowMapper<WeeklySession> mapper = (rs, rowNum) -> {
        LocalDateTime startDt = rs.getTimestamp("startDate").toLocalDateTime();
        LocalDateTime endDt = rs.getTimestamp("endDate").toLocalDateTime();
        return new WeeklySession(
            rs.getLong("id"),
            rs.getString("coach_name"),
            rs.getString("client_name"),
            startDt.toLocalDate().getDayOfWeek(),
            startDt.toLocalTime(),
            endDt.toLocalTime(),
            rs.getString("title")
        );
    };

    public List<WeeklySession> getWeeklySessions(LocalDate weekStart) throws SQLException {
        LocalDate weekEnd = weekStart.plusDays(7);
        String sql = """
             SELECT c.name AS coach_name,
                   ts.id AS id,
                   cl.name AS client_name,
                   ts.startDate,
                   ts.endDate,
                   ts.title
            FROM training_sessions ts
            JOIN coaches c ON c.id = ts.coach_id
            JOIN clients cl ON cl.id = ts.client_id
            WHERE ts.startDate >= ? AND ts.startDate < ?
            ORDER BY coach_name, startDate
                """;
        return Database.jdbc().query(
            sql,
            ps -> {
                ps.setTimestamp(1, Timestamp.valueOf(weekStart.atStartOfDay()));
                ps.setTimestamp(2, Timestamp.valueOf(weekEnd.atStartOfDay()));
            },
            mapper
        );
    }

    public void addTrainingSession(Long clientId, Long coachId, LocalDateTime start, LocalDateTime end, String title) throws SQLException {
        String sql = """
            INSERT INTO training_sessions (client_id, coach_id, startDate, endDate, title)
            VALUES (?, ?, ?, ?, ?)
        """;

        Database.jdbc().update(sql, ps -> {
            ps.setLong(1, clientId);
            ps.setLong(2, coachId);
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(end));
            ps.setString(5, title);
        });
    }

    public void deleteTrainingSession(Long id) throws SQLException {
        String sql = "DELETE FROM training_sessions WHERE id = ?";
        Database.jdbc().update(sql, id);
    }

    /**
     * Returns true if the coach has any overlapping session in the given interval.
     */
    public boolean hasConflictingSession(Long coachId, LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM training_sessions
            WHERE coach_id = ?
              AND NOT (endDate <= ? OR startDate >= ?)
        """;

        Long count = Database.jdbc().queryForObject(
            sql,
            Long.class,
            coachId,
            Timestamp.valueOf(start),
            Timestamp.valueOf(end)
        );
        return count != null && count > 0;
    }
}
