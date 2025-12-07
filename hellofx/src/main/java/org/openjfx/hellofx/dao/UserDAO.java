package org.openjfx.hellofx.dao;

import org.mindrot.jbcrypt.BCrypt;
import org.openjfx.hellofx.entities.User;
import org.openjfx.hellofx.utils.Database;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, coach_id FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, coach_id FROM users WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public long countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    public void createUser(String username, String rawPassword, String role, Long coachId) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, coach_id) VALUES (?, ?, ?, ?)";
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashed);
            stmt.setString(3, role);
            if (coachId != null) {
                stmt.setLong(4, coachId);
            } else {
                stmt.setNull(4, java.sql.Types.BIGINT);
            }
            stmt.executeUpdate();
        }
    }

    public void updatePassword(Long userId, String rawPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashed);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
    }

    public void updateCoachId(Long userId, Long coachId) throws SQLException {
        String sql = "UPDATE users SET coach_id = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (coachId != null) {
                stmt.setLong(1, coachId);
            } else {
                stmt.setNull(1, java.sql.Types.BIGINT);
            }
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getObject("coach_id", Long.class)
        );
    }
}
