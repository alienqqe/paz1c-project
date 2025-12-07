package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.entities.Coach;
import org.openjfx.hellofx.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CoachDAO {

    public Long addCoach(Coach coach) throws SQLException {
        String sql = "INSERT INTO coaches (name, email, phone_number) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, coach.name());
            if (coach.email() == null || coach.email().isBlank()) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, coach.email());
            }
            stmt.setString(3, coach.phoneNumber());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public List<Coach> searchCoaches(String query) throws SQLException {
        String sql = "SELECT * FROM coaches WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ?";
        List<Coach> results = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query.toLowerCase() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Coach(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        null,
                        null
                    ));
                }
            }
        }

        return results;
    }

    public Long findCoachIdForUser(String username) throws SQLException {
        if (username == null || username.isBlank()) {
            return null;
        }

        // try email match
        String sql = "SELECT id FROM coaches WHERE email = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        // try pattern coach<id>
        if (username.startsWith("coach")) {
            String suffix = username.substring(5);
            try {
                return Long.parseLong(suffix);
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }

        // try exact name match
        String nameSql = "SELECT id FROM coaches WHERE LOWER(name) = LOWER(?) LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(nameSql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        return null;
    }
}
