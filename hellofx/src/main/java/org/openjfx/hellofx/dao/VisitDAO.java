package org.openjfx.hellofx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openjfx.hellofx.utils.Database;

public class VisitDAO {

    public boolean checkInClient(Long clientId) throws SQLException {
        String selectSql = """
            SELECT id, type, visits_remaining
            FROM memberships
            WHERE idOfHolder = ?
              AND CURDATE() BETWEEN startDate AND expiresAt
            ORDER BY expiresAt DESC
            LIMIT 1
            FOR UPDATE
        """;
        String insertSql = "INSERT INTO visits (client_id, membership_id, check_in) VALUES (?, ?, NOW())";
        String decSql = "UPDATE memberships SET visits_remaining = visits_remaining - 1 WHERE id = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            Long membershipId = null;
            String type = null;
            Integer remaining = null;

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setLong(1, clientId);
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

    public ResultSet getRecentVisits(Connection conn, int limit) throws SQLException {
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
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, limit);
        return ps.executeQuery();
    }

    public ResultSet getRecentVisitsForClient(Connection conn, String filter, int limit) throws SQLException {
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
        PreparedStatement ps = conn.prepareStatement(sql);
        String pattern = "%" + filter.toLowerCase() + "%";
        ps.setString(1, pattern);
        ps.setString(2, pattern);
        ps.setInt(3, limit);
        return ps.executeQuery();
    }
}
