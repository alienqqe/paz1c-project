package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.utils.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openjfx.hellofx.entities.Specialization;

/**
 * DAO for coach specializations.
 */
public class SpecializationDAO {

    /**
     * Ensures the specialization exists and returns its id.
     */
    public Long ensureSpecialization(String name) throws SQLException {
        String insertSql = "INSERT INTO specializations (name) VALUES (?)";
        try {
            Database.jdbc().update(insertSql, name);
        } catch (Exception ignore) {
            // likely duplicate, fall through to lookup
        }
        String selectSql = "SELECT id FROM specializations WHERE name = ? LIMIT 1";
        List<Long> ids = Database.jdbc().query(selectSql, ps -> ps.setString(1, name),
            (ResultSet rs, int rowNum) -> rs.getLong("id"));
        return ids.isEmpty() ? null : ids.get(0);
    }

    public Set<Specialization> getSpecializationsForCoach(Long coachId) throws SQLException {
        if (coachId == null) return Set.of();
        String sql = """
            SELECT s.id, s.name
            FROM coach_specializations cs
            JOIN specializations s ON s.id = cs.specialization_id
            WHERE cs.coach_id = ?
        """;
        List<Specialization> specs = Database.jdbc().query(sql, ps -> ps.setLong(1, coachId),
            (ResultSet rs, int rowNum) -> new Specialization(rs.getLong("id"), rs.getString("name")));
        return new HashSet<>(specs);
    }

    /**
     * Replaces all specializations for a coach with the provided set.
     */
    public void setSpecializationsForCoach(Long coachId, Set<String> names) throws SQLException {
        if (coachId == null) return;
        Database.jdbc().update("DELETE FROM coach_specializations WHERE coach_id = ?", coachId);
        if (names == null || names.isEmpty()) {
            return;
        }
        for (String raw : names) {
            if (raw == null || raw.isBlank()) continue;
            String name = raw.trim();
            Long specId = ensureSpecialization(name);
            if (specId != null) {
                Database.jdbc().update(
                    "INSERT IGNORE INTO coach_specializations (coach_id, specialization_id) VALUES (?, ?)",
                    coachId, specId
                );
            }
        }
    }
}
