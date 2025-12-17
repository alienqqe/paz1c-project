package org.openjfx.hellofx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.openjfx.hellofx.utils.Database;
import org.springframework.jdbc.core.RowMapper;

public class VisitDAO {

    public record VisitView(Long id, String clientName, String clientEmail, String membershipType, LocalDateTime checkIn) {}

    private final RowMapper<VisitView> historyMapper = (rs, rowNum) -> new VisitView(
        rs.getLong("id"),
        rs.getString("client_name"),
        rs.getString("client_email"),
        rs.getString("membership_type"),
        rs.getTimestamp("check_in").toLocalDateTime()
    );

    public boolean checkInClient(Long clientId) throws SQLException {
        String selectSql = """
            SELECT id, type, visits_remaining
            FROM memberships
            WHERE idOfHolder = ?
              AND startDate <= ?
              AND expiresAt >= ?
            ORDER BY expiresAt DESC
            LIMIT 1
            FOR UPDATE
        """;
        String insertSql = "INSERT INTO visits (client_id, membership_id, check_in) VALUES (?, ?, NOW())";
        String decSql = "UPDATE memberships SET visits_remaining = visits_remaining - 1 WHERE id = ?";

        java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
        try (Connection conn = Database.getDataSource().getConnection()) {
            conn.setAutoCommit(false);

            Long membershipId = null;
            String type = null;
            Integer remaining = null;

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setLong(1, clientId);
                ps.setDate(2, today);
                ps.setDate(3, today);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        membershipId = rs.getLong("id");
                        type = rs.getString("type");
                        remaining = rs.getObject("visits_remaining", Integer.class);
                    }
                }
            }

            if (membershipId == null) {
                conn.rollback();
                return false;
            }

            boolean isTen = "Ten".equalsIgnoreCase(type);
            if (isTen && (remaining == null || remaining <= 0)) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setLong(1, clientId);
                ps.setLong(2, membershipId);
                ps.executeUpdate();
            }

            if (isTen) {
                try (PreparedStatement ps = conn.prepareStatement(decSql)) {
                    ps.setLong(1, membershipId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                // attempt rollback on the same connection if still open
                // note: conn is auto-closed by try-with-resources
            } catch (Exception ignore) {
                // ignore
            }
            throw e;
        }
    }

    public List<VisitView> getRecentVisits(int limit) {
        String sql = """
            SELECT v.id,
                   c.name AS client_name,
                   c.email AS client_email,
                   m.type AS membership_type,
                   v.check_in
            FROM visits v
            JOIN clients c ON c.id = v.client_id
            LEFT JOIN memberships m ON m.id = v.membership_id
            ORDER BY v.check_in DESC
            LIMIT ?
        """;
        return Database.jdbc().query(sql, ps -> ps.setInt(1, limit), historyMapper);
    }

    public List<VisitView> getRecentVisitsForClient(String filter, int limit) {
        String sql = """
            SELECT v.id,
                   c.name AS client_name,
                   c.email AS client_email,
                   m.type AS membership_type,
                   v.check_in
            FROM visits v
            JOIN clients c ON c.id = v.client_id
            LEFT JOIN memberships m ON m.id = v.membership_id
            WHERE LOWER(c.name) LIKE ? OR LOWER(c.email) LIKE ?
            ORDER BY v.check_in DESC
            LIMIT ?
        """;
        String pattern = "%" + filter.toLowerCase() + "%";
        return Database.jdbc().query(
            sql,
            ps -> {
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setInt(3, limit);
            },
            historyMapper
        );
    }

    public int countVisitsForClient(Long clientId) {
        String sql = "SELECT COUNT(*) FROM visits WHERE client_id = ?";
        Integer count = Database.jdbc().queryForObject(sql, Integer.class, clientId);
        return count != null ? count : 0;
    }

}
