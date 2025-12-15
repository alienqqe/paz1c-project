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
            if (isTen && remaining == null) {
                // lenient fallback for older data
                remaining = 10;
            }
            if (isTen && remaining <= 0) {
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
            // best effort rollback on the same connection if still open
            // (try-with-resources will close it regardless)
            throw e;
        }
    }
}
