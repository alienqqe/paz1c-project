package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.openjfx.hellofx.model.AvailabilitySlot;

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

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, coachId);
            stmt.setTimestamp(2, Timestamp.valueOf(start));
            stmt.setTimestamp(3, Timestamp.valueOf(end));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("cnt") > 0;
                }
            }
        }
        return false;
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

        List<AvailabilitySlot> slots = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, coachId);
            stmt.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));
            stmt.setTimestamp(3, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    slots.add(new AvailabilitySlot(
                        rs.getTimestamp("startDate").toLocalDateTime(),
                        rs.getTimestamp("endDate").toLocalDateTime(),
                        rs.getString("note")
                    ));
                }
            }
        }

        return slots;
    }

    public void deleteExpired() throws SQLException {
        String sql = "DELETE FROM coach_availability WHERE endDate < ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }
}
