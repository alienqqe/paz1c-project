package org.openjfx.hellofx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.openjfx.hellofx.entities.Membership;
import org.openjfx.hellofx.utils.Database;

public class MembershipDAO {

    public int removeByHolderId(long holderId) throws SQLException {
    String sql = "DELETE FROM memberships WHERE idOfHolder = ?";
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setLong(1, holderId);
        return stmt.executeUpdate(); 
    }
}

    public void addMembership(Membership membership) throws SQLException {
        String sql = "INSERT INTO memberships (startDate, expiresAt, price, type, idOfHolder, visits_remaining) VALUES (?, ?, ?, ?, ?, ?)";
    
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (membership.startDate() != null) {
                stmt.setDate(1, java.sql.Date.valueOf(membership.startDate()));
            } else {
                stmt.setNull(1, Types.DATE);
            }

            if (membership.expiresAt() != null) {
                stmt.setDate(2, java.sql.Date.valueOf(membership.expiresAt()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            stmt.setDouble(3, membership.price());
            stmt.setString(4, membership.type().name());
            if (membership.idOfHolder() != null) {
                stmt.setLong(5, membership.idOfHolder());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }

            if (membership.type() == Membership.MembershipType.Ten) {
                stmt.setInt(6, membership.visitsRemaining());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.executeUpdate();
        }
    }

    public String getCurrentMembershipType(Long clientId) throws SQLException {
        String sql = """
            SELECT type, expiresAt, visits_remaining
            FROM memberships
            WHERE idOfHolder = ?
              AND (expiresAt IS NULL OR expiresAt >= CURDATE())
            ORDER BY startDate DESC
            LIMIT 1
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (clientId == null) return null;
            stmt.setLong(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
                }
            }
        }
        return null;
    }

    public Integer getRemainingVisits(Long clientId) throws SQLException {
        String sql = """
            SELECT visits_remaining
            FROM memberships
            WHERE idOfHolder = ?
              AND (expiresAt IS NULL OR expiresAt >= CURDATE())
            ORDER BY startDate DESC
            LIMIT 1
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (clientId == null) return null;
            stmt.setLong(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject("visits_remaining", Integer.class);
                }
            }
        }
        return null;
    }

    public boolean hasActiveMembership(Long clientId) throws SQLException {
            if (clientId == null) return false;
            String sql = """
                SELECT id
                FROM memberships
                WHERE idOfHolder = ?
                AND CURDATE() BETWEEN startDate AND expiresAt
                ORDER BY expiresAt DESC
                LIMIT 1
            """;

            try(Connection con = Database.getConnection()){
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setLong(1, clientId);
                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()){
                        return true;
                    }
                }
            }
            return false;
    }
}
