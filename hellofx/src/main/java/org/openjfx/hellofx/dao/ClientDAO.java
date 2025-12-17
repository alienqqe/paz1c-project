package org.openjfx.hellofx.dao;

import java.sql.SQLException;
import java.util.List;

import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.utils.Database;
import org.springframework.jdbc.core.RowMapper;

public class ClientDAO {

    private final RowMapper<Client> mapper = (rs, i) -> new Client(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("phone_number")
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
}
