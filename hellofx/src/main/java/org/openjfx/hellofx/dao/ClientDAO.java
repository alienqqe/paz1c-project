package org.openjfx.hellofx.dao;

import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.utils.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public void addClient(Client client) throws SQLException {
        String sql = "INSERT INTO clients (name, email, phone_number, is_active_member, is_in_gym) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.name());
            stmt.setString(2, client.email());
            stmt.setString(3, client.phoneNumber());
            stmt.setBoolean(4, client.isActiveMember());
            stmt.setBoolean(5, client.isInTheGym());
            stmt.executeUpdate();
        }
    }

    public List<Client> searchClients(String query) throws SQLException {
        String sql = "SELECT * FROM clients WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ?";
        List<Client> results = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query.toLowerCase() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Client(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getBoolean("is_active_member"),
                        rs.getBoolean("is_in_gym"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        new LocalDate[0]
                    ));
                }
            }
        }

        return results;
    }
}
