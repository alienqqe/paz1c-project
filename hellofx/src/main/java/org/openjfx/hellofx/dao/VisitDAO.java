package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VisitDAO {

    public boolean checkInClient(Long clientId) throws SQLException {
        String sql = """
            INSERT INTO visits (client_id, membership_id, check_in)
            SELECT c.id, m.id, NOW()
            FROM clients c
            JOIN memberships m ON m.idOfHolder = c.id
            WHERE c.id = ? AND CURDATE() BETWEEN m.startDate AND m.expiresAt
            ORDER BY m.expiresAt DESC
            LIMIT 1;
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, clientId);
            return stmt.executeUpdate() > 0;
        }
    }
}
