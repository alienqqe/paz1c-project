package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.model.AvailabilitySlot;
import org.openjfx.hellofx.utils.Database;
import org.springframework.jdbc.core.RowMapper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CoachAvailabilityDAO {

    private final RowMapper<AvailabilitySlot> mapper = (rs, rowNum) -> new AvailabilitySlot(
        rs.getTimestamp("startDate").toLocalDateTime(),
        rs.getTimestamp("endDate").toLocalDateTime(),
        rs.getString("note")
    );

    public void addAvailability(Long coachId, LocalDateTime start, LocalDateTime end, String note) throws SQLException {
        String sql = """
            INSERT INTO coach_availability (coach_id, startDate, endDate, note)
            VALUES (?, ?, ?, ?)
        """;

        Database.jdbc().update(sql, ps -> {
            ps.setLong(1, coachId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            ps.setString(4, note);
        });
    }

    /**
     * Returns true if there exists an availability window that fully contains the requested interval.
     */
    public boolean isWithinAvailability(Long coachId, LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM coach_availability
            WHERE coach_id = ?
              AND startDate <= ?
              AND endDate >= ?
        """;

        Long count = Database.jdbc().queryForObject(
            sql,
            new Object[]{coachId, Timestamp.valueOf(start), Timestamp.valueOf(end)},
            Long.class
        );
        return count != null && count > 0;
    }

    public List<AvailabilitySlot> getAvailabilityForDate(Long coachId, LocalDate date) throws SQLException {
        String sql = """
            SELECT startDate, endDate, COALESCE(note, 'Available') AS note
            FROM coach_availability
            WHERE coach_id = ?
              AND startDate >= ?
              AND startDate < ?
            ORDER BY startDate
        """;

        return Database.jdbc().query(
            sql,
            ps -> {
                ps.setLong(1, coachId);
                ps.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));
                ps.setTimestamp(3, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            },
            mapper
        );
    }

    public void deleteExpired() throws SQLException {
        String sql = "DELETE FROM coach_availability WHERE endDate < ?";
        Database.jdbc().update(sql, Timestamp.valueOf(LocalDateTime.now()));
    }
}
