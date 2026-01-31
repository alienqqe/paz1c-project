package org.openjfx.hellofx.dao;

import java.sql.SQLException;
import java.util.List;

import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.model.ClientWithMembershipStatus;
import org.openjfx.hellofx.utils.Database;
import org.springframework.jdbc.core.RowMapper;

public class ClientDAO {

    private final RowMapper<Client> mapper = (rs, i) -> new Client(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("phone_number")
    );

    private final RowMapper<ClientWithMembershipStatus> statusMapper = (rs, i) -> new ClientWithMembershipStatus(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("phone_number"),
        rs.getString("membership_type"),
        (Integer) rs.getObject("visits_remaining")
    );

    public void addClient(Client client) throws SQLException {
        String sql = "INSERT INTO clients (name, email, phone_number) VALUES (?, ?, ?)";
        Database.jdbc().update(sql, client.name(), client.email(), client.phoneNumber());
    }

    public List<Client> searchClients(String query) throws SQLException {
        String sql = "SELECT * FROM clients WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ?";
        String pattern = "%" + query.toLowerCase() + "%";
        return Database.jdbc().query(sql, ps -> {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
        }, mapper);
    }

    
    public List<ClientWithMembershipStatus> searchClientsWithStatus(String query) throws SQLException {
        String pattern = "%" + query.toLowerCase() + "%";
        String sql = """
            WITH active_membership AS (
                SELECT
                    m.idOfHolder,
                    m.type AS membership_type,
                    m.visits_remaining,
                    ROW_NUMBER() OVER (PARTITION BY m.idOfHolder ORDER BY m.startDate DESC) AS rn
                FROM memberships m
                WHERE m.startDate <= ?
                  AND (m.expiresAt IS NULL OR m.expiresAt >= ?)
                  AND (m.type <> 'Ten' OR m.visits_remaining > 0)
            )
            SELECT c.id,
                   c.name,
                   c.email,
                   c.phone_number,
                   am.membership_type,
                   am.visits_remaining
            FROM clients c
            LEFT JOIN active_membership am
              ON am.idOfHolder = c.id AND am.rn = 1
            WHERE LOWER(c.name) LIKE ? OR LOWER(c.email) LIKE ?
            ORDER BY c.name ASC
        """;

        java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
        return Database.jdbc().query(
            sql,
            ps -> {
                ps.setDate(1, today);
                ps.setDate(2, today);
                ps.setString(3, pattern);
                ps.setString(4, pattern);
            },
            statusMapper
        );
    }
}
