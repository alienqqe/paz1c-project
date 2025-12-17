package org.openjfx.hellofx.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.openjfx.hellofx.entities.Coach;
import org.openjfx.hellofx.entities.Specialization;
import org.openjfx.hellofx.utils.Database;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class CoachDAO {

    private final RowMapper<Coach> mapper = (rs, i) -> new Coach(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("phone_number"),
        null,
        null
    );

    public Long addCoach(Coach coach) throws SQLException {
        String sql = "INSERT INTO coaches (name, email, phone_number) VALUES (?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        Database.jdbc().update(con -> {
            var ps = con.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, coach.name());
            ps.setString(2, coach.email());
            ps.setString(3, coach.phoneNumber());
            return ps;
        }, kh);
        if (kh.getKey() != null) {
            return kh.getKey().longValue();
        }
        return null;
    }

    public List<Coach> searchCoaches(String query) throws SQLException {
        String sql = "SELECT * FROM coaches WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ?";
        String pattern = "%" + query.toLowerCase() + "%";
        List<Coach> base = Database.jdbc().query(sql, ps -> {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
        }, mapper);
        return attachSpecializations(base);
    }

    public Long findCoachIdForUser(String username) throws SQLException {
        if (username == null || username.isBlank()) {
            return null;
        }
        // try email match
        String sql = "SELECT id FROM coaches WHERE email = ? LIMIT 1";
        List<Long> byEmail = Database.jdbc().query(sql, ps -> ps.setString(1, username),
            (ResultSet rs, int rowNum) -> rs.getLong("id"));
        if (!byEmail.isEmpty()) {
            return byEmail.get(0);
        }

        // try exact name match
        String nameSql = "SELECT id FROM coaches WHERE LOWER(name) = LOWER(?) LIMIT 1";
        List<Long> byName = Database.jdbc().query(nameSql, ps -> ps.setString(1, username),
            (ResultSet rs, int rowNum) -> rs.getLong("id"));
        return byName.isEmpty() ? null : byName.get(0);
    }

    private List<Coach> attachSpecializations(List<Coach> coaches) throws SQLException {
        if (coaches == null || coaches.isEmpty()) return coaches;
        var specDao = DaoFactory.specializations();
        return coaches.stream().map(c -> {
            try {
                Set<Specialization> specs = specDao.getSpecializationsForCoach(c.id());
                return new Coach(c.id(), c.name(), c.email(), c.phoneNumber(), c.upcomingTrainings(), specs);
            } catch (SQLException e) {
                return c;
            }
        }).toList();
    }
}
