package org.openjfx.hellofx.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.openjfx.hellofx.entities.Membership;
import org.openjfx.hellofx.utils.Database;

public class MembershipDAO {

    public int removeByHolderId(long holderId) throws SQLException {
        String sql = "DELETE FROM memberships WHERE idOfHolder = ?";
        return Database.jdbc().update(sql, holderId);
    }

    public void addMembership(Membership membership) throws SQLException {
        String sql = "INSERT INTO memberships (startDate, expiresAt, price, type, idOfHolder, visits_remaining) VALUES (?, ?, ?, ?, ?, ?)";

        Database.jdbc().update(sql, ps -> {
            if (membership.startDate() != null) {
                ps.setDate(1, java.sql.Date.valueOf(membership.startDate()));
            } else {
                ps.setNull(1, Types.DATE);
            }

            if (membership.expiresAt() != null) {
                ps.setDate(2, java.sql.Date.valueOf(membership.expiresAt()));
            } else {
                ps.setNull(2, Types.DATE);
            }

            ps.setDouble(3, membership.price());
            ps.setString(4, membership.type().name());
            if (membership.idOfHolder() != null) {
                ps.setLong(5, membership.idOfHolder());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            if (membership.type() == Membership.MembershipType.Ten) {
                ps.setInt(6, membership.visitsRemaining());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
        });
    }

    public String getCurrentMembershipType(Long clientId) throws SQLException {
        if (clientId == null) return null;
        String sql = """
            SELECT type, expiresAt, visits_remaining
            FROM memberships
            WHERE idOfHolder = ?
              AND (expiresAt IS NULL OR expiresAt >= ?)
              AND (type <> 'Ten' OR visits_remaining IS NULL OR visits_remaining > 0)
              AND startDate <= ?
            ORDER BY startDate DESC
            LIMIT 1
        """;

        java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());

        List<String> res = Database.jdbc().query(sql, ps -> {
            ps.setLong(1, clientId);
            ps.setDate(2, today);
            ps.setDate(3, today);
        }, (rs, i) -> {
            String type = rs.getString("type");
            Integer remaining = rs.getObject("visits_remaining", Integer.class);
            java.sql.Date expires = rs.getDate("expiresAt");
            String base = type;
            if ("Ten".equalsIgnoreCase(type) && remaining != null) {
                base = base + " (" + remaining + " left)";
            }
            if (expires == null) {
                return base + " (Permanent)";
            }
            return base;
        });
        return res.isEmpty() ? null : res.get(0);
    }

    public Integer getRemainingVisits(Long clientId) throws SQLException {
        if (clientId == null) return null;
        String sql = """
            SELECT visits_remaining
            FROM memberships
            WHERE idOfHolder = ?
              AND (expiresAt IS NULL OR expiresAt >= ?)
              AND (type <> 'Ten' OR visits_remaining IS NULL OR visits_remaining > 0)
              AND startDate <= ?
            ORDER BY startDate DESC
            LIMIT 1
        """;

        java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());

        List<Integer> res = Database.jdbc().query(sql, ps -> {
                ps.setLong(1, clientId);
                ps.setDate(2, today);
                ps.setDate(3, today);
            },
            (ResultSet rs, int rowNum) -> rs.getObject("visits_remaining", Integer.class));
        return res.isEmpty() ? null : res.get(0);
    }

    public boolean hasActiveMembership(Long clientId) throws SQLException {
        if (clientId == null) return false;
        String sql = """
            SELECT id
            FROM memberships
            WHERE idOfHolder = ?
              AND startDate <= ?
              AND expiresAt >= ?
              AND (type <> 'Ten' OR visits_remaining IS NULL OR visits_remaining > 0)
            ORDER BY expiresAt DESC
            LIMIT 1
        """;
        java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());

        List<Long> res = Database.jdbc().query(sql, ps -> {
                ps.setLong(1, clientId);
                ps.setDate(2, today);
                ps.setDate(3, today);
            },
            (ResultSet rs, int rowNum) -> rs.getLong("id"));
        return !res.isEmpty();
    }
}
