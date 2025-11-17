package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.entities.Membership;
import org.openjfx.hellofx.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

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
        String sql = "INSERT INTO memberships (startDate, expiresAt, price, type, idOfHolder) VALUES (?, ?, ?, ?, ?)";
    
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

            stmt.executeUpdate();
        }
    }

    public String getCurrentMembershipType(Long clientId) throws SQLException {
        String sql = """
            SELECT type, expiresAt
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
                    java.sql.Date expires = rs.getDate("expiresAt");
                    if (expires == null) {
                        return type + " (Permanent)";
                    }
                    return type;
                }
            }
        }
        return null;
    }
}
