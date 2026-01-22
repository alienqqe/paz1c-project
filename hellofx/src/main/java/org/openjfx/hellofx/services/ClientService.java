package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.util.List;

import org.openjfx.hellofx.dao.ClientDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.entities.Client;

public class ClientService {
    private final ClientDAO clientDAO = DaoFactory.clients();

    public void addClient(Client client) throws SQLException {
        clientDAO.addClient(client);
    }

    public List<Client> searchClients(String query) throws SQLException {
        return clientDAO.searchClients(query);
    }
}
