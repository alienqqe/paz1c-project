package org.openjfx.hellofx.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.hellofx.model.AvailabilitySlot;
import org.openjfx.hellofx.utils.Database;
import org.springframework.jdbc.core.RowMapper;

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

    // returns true if there is any overlapping availability for this coach
    public boolean hasOverlap(Long coachId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT COUNT(*) FROM coach_availability
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


   // returns true if there exists an availability window that fully contains the requested interval. 
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
            Long.class,
            coachId,
            Timestamp.valueOf(start),
            Timestamp.valueOf(end)
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

    // splits the avaliability slot after a booking
    // if the booking fully covers the slot, the slot is removed.
    // if it overlaps at the start or end, the slot is shortened.
    // if it sits in the middle, the slot is split into two.
    public void consumeAvailability(Long coachId, AvailabilitySlot slot, LocalDateTime bookStart, LocalDateTime bookEnd) throws SQLException {
        // must be within the slot
        if (bookStart.isBefore(slot.start()) || bookEnd.isAfter(slot.end())) {
            return;
        }

        // remove slot if booked fully
        if (!bookStart.isAfter(slot.start()) && !bookEnd.isBefore(slot.end())) {
            Database.jdbc().update(
                "DELETE FROM coach_availability WHERE coach_id = ? AND startDate = ? AND endDate = ?",
                coachId,
                Timestamp.valueOf(slot.start()),
                Timestamp.valueOf(slot.end())
            );
            return;
        }

        // consume from start
        if (bookStart.isEqual(slot.start())) {
            Database.jdbc().update(
                "UPDATE coach_availability SET startDate = ? WHERE coach_id = ? AND startDate = ? AND endDate = ?",
                Timestamp.valueOf(bookEnd),
                coachId,
                Timestamp.valueOf(slot.start()),
                Timestamp.valueOf(slot.end())
            );
            return;
        }

        // consume from end
        if (bookEnd.isEqual(slot.end())) {
            Database.jdbc().update(
                "UPDATE coach_availability SET endDate = ? WHERE coach_id = ? AND startDate = ? AND endDate = ?",
                Timestamp.valueOf(bookStart),
                coachId,
                Timestamp.valueOf(slot.start()),
                Timestamp.valueOf(slot.end())
            );
            return;
        }

        // booking is in the middle: split into two slots
        // shorten original to [start, bookStart]
        Database.jdbc().update(
            "UPDATE coach_availability SET endDate = ? WHERE coach_id = ? AND startDate = ? AND endDate = ?",
            Timestamp.valueOf(bookStart),
            coachId,
            Timestamp.valueOf(slot.start()),
            Timestamp.valueOf(slot.end())
        );
        // insert second part [bookEnd, oldEnd] carrying the same note
        Database.jdbc().update(
            "INSERT INTO coach_availability (coach_id, startDate, endDate, note) VALUES (?, ?, ?, ?)",
            coachId,
            Timestamp.valueOf(bookEnd),
            Timestamp.valueOf(slot.end()),
            slot.note()
        );
    }

   
    // restore the avalibility if the booking gets canceled
    public void restoreAvailability(Long coachId, LocalDateTime start, LocalDateTime end) throws SQLException {
        if (coachId == null || start == null || end == null || !end.isAfter(start)) {
            return;
        }
        Database.jdbc().update(
            "INSERT INTO coach_availability (coach_id, startDate, endDate, note) VALUES (?, ?, ?, ?)",
            coachId,
            Timestamp.valueOf(start),
            Timestamp.valueOf(end),
            "Available"
        );
        mergeAvailability(coachId);
    }

    
    // merge overlapping or adjacent (i.e 12:00 - 14:00, 14:00 - 16:00) slots
    public void mergeAvailability(Long coachId) throws SQLException {
        if (coachId == null) return;
        String selectSql = """
            SELECT startDate, endDate, COALESCE(note, 'Available') AS note
            FROM coach_availability
            WHERE coach_id = ?
            ORDER BY startDate
        """;
        List<AvailabilitySlot> slots = Database.jdbc().query(
            selectSql,
            ps -> ps.setLong(1, coachId),
            mapper
        );
        if (slots.isEmpty()) return;

        ArrayList<AvailabilitySlot> merged = new ArrayList<>();
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

        Database.jdbc().update("DELETE FROM coach_availability WHERE coach_id = ?", coachId);
        for (AvailabilitySlot s : merged) {
            Database.jdbc().update(
                "INSERT INTO coach_availability (coach_id, startDate, endDate, note) VALUES (?, ?, ?, ?)",
                coachId,
                Timestamp.valueOf(s.start()),
                Timestamp.valueOf(s.end()),
                s.note()
            );
        }
    }
}
