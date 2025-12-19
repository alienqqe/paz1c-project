package org.openjfx.hellofx.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.openjfx.hellofx.entities.TrainingSession;
import org.openjfx.hellofx.model.AvailabilitySlot;
import org.openjfx.hellofx.model.WeeklySession;
import org.openjfx.hellofx.utils.Database;
import org.springframework.jdbc.core.RowMapper;

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

    public TrainingSession findTrainingSession(Long id) throws SQLException {
        String sql = """
            SELECT id, coach_id, startDate, endDate
            FROM training_sessions
            WHERE id = ?
            LIMIT 1
        """;
        List<TrainingSession> rows = Database.jdbc().query(
            sql,
            ps -> ps.setLong(1, id),
            (rs, rowNum) -> new TrainingSession(
                rs.getLong("id"),
                null,
                rs.getLong("coach_id"),
                rs.getTimestamp("startDate").toLocalDateTime(),
                rs.getTimestamp("endDate").toLocalDateTime(),
                null
            )
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    
    // Deletes a session and restores availability in one transaction.
    // we use plain connection, because its the simplest solution
    public void deleteSessionAndRestoreAvailability(Long id) throws SQLException {
        if (id == null) return;
        var ds = Database.getDataSource();
        try (var conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            TrainingSession win = null;
            try (var ps = conn.prepareStatement(
                "SELECT id, coach_id, startDate, endDate FROM training_sessions WHERE id = ? LIMIT 1")) {
                ps.setLong(1, id);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        win = new TrainingSession(
                            rs.getLong("id"),
                            null,
                            rs.getLong("coach_id"),
                            rs.getTimestamp("startDate").toLocalDateTime(),
                            rs.getTimestamp("endDate").toLocalDateTime(),
                            null
                        );
                    }
                }
            }
            if (win == null) {
                conn.rollback();
                return;
            }

            try (var ps = conn.prepareStatement("DELETE FROM training_sessions WHERE id = ?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            try (var ps = conn.prepareStatement(
                "INSERT INTO coach_availability (coach_id, startDate, endDate, note) VALUES (?, ?, ?, ?)")) {
                ps.setLong(1, win.coachId());
                ps.setTimestamp(2, Timestamp.valueOf(win.startDate()));
                ps.setTimestamp(3, Timestamp.valueOf(win.endDate()));
                ps.setString(4, "Available");
                ps.executeUpdate();
            }

            mergeAvailability(conn, win.coachId());
            conn.commit();
        }
    }

    // merges overlapping/adjacent availability for a coach using the provided connection
    private void mergeAvailability(java.sql.Connection conn, Long coachId) throws SQLException {
        if (coachId == null) return;
        List<AvailabilitySlot> slots;
        try (var ps = conn.prepareStatement(
            """
            SELECT startDate, endDate, COALESCE(note, 'Available') AS note
            FROM coach_availability
            WHERE coach_id = ?
            ORDER BY startDate
            """
        )) {
            ps.setLong(1, coachId);
            try (var rs = ps.executeQuery()) {
                var list = new java.util.ArrayList<AvailabilitySlot>();
                while (rs.next()) {
                    list.add(new AvailabilitySlot(
                        rs.getTimestamp("startDate").toLocalDateTime(),
                        rs.getTimestamp("endDate").toLocalDateTime(),
                        rs.getString("note")
                    ));
                }
                slots = list;
            }
        }
        if (slots.isEmpty()) return;

        var merged = new java.util.ArrayList<AvailabilitySlot>();
        AvailabilitySlot current = slots.get(0);
        for (int i = 1; i < slots.size(); i++) {
            AvailabilitySlot next = slots.get(i);
            boolean overlapsOrTouches = !next.start().isAfter(current.end());
            if (overlapsOrTouches) {
                LocalDateTime newEnd = next.end().isAfter(current.end()) ? next.end() : current.end();
                current = new AvailabilitySlot(current.start(), newEnd, current.note());
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        try (var ps = conn.prepareStatement("DELETE FROM coach_availability WHERE coach_id = ?")) {
            ps.setLong(1, coachId);
            ps.executeUpdate();
        }
        try (var ps = conn.prepareStatement(
            "INSERT INTO coach_availability (coach_id, startDate, endDate, note) VALUES (?, ?, ?, ?)")) {
            for (AvailabilitySlot s : merged) {
                ps.setLong(1, coachId);
                ps.setTimestamp(2, Timestamp.valueOf(s.start()));
                ps.setTimestamp(3, Timestamp.valueOf(s.end()));
                ps.setString(4, s.note());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    
   // returns true if the coach has any overlapping session in the given interval.
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
