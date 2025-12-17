package org.openjfx.hellofx.dao;

import org.mindrot.jbcrypt.BCrypt;
import org.openjfx.hellofx.entities.User;
import org.openjfx.hellofx.utils.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, coach_id FROM users WHERE username = ?";
        List<User> users = Database.jdbc().query(sql, ps -> ps.setString(1, username), this::mapUser);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findById(Long id) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, coach_id FROM users WHERE id = ?";
        List<User> users = Database.jdbc().query(sql, ps -> ps.setLong(1, id), this::mapUser);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public long countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        Long count = Database.jdbc().queryForObject(sql, Long.class);
        return count == null ? 0 : count;
    }

    public void createUser(String username, String rawPassword, String role, Long coachId) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, coach_id) VALUES (?, ?, ?, ?)";
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        Database.jdbc().update(sql, ps -> {
            ps.setString(1, username);
            ps.setString(2, hashed);
            ps.setString(3, role);
            if (coachId != null) {
                ps.setLong(4, coachId);
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }
        });
    }

    public void updatePassword(Long userId, String rawPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        Database.jdbc().update(sql, hashed, userId);
    }

    public void updateCoachId(Long userId, Long coachId) throws SQLException {
        String sql = "UPDATE users SET coach_id = ? WHERE id = ?";
        Database.jdbc().update(sql, ps -> {
            if (coachId != null) {
                ps.setLong(1, coachId);
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setLong(2, userId);
        });
    }

    private User mapUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getObject("coach_id", Long.class)
        );
    }
}
