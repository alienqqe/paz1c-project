package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.entities.Client;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientDAOTest extends TestContainers {

    private final ClientDAO dao = new ClientDAO();

    @Test
    void addClientPersistsAndSearches() throws Exception {
        dao.addClient(new Client(null, "Alice", "alice@test.com", "123"));

        List<Client> found = dao.searchClients("alice");
        assertEquals(1, found.size());
        Client c = found.get(0);
        assertEquals("Alice", c.name());
        assertEquals("alice@test.com", c.email());
    }

    @Test
    void searchClientsEmptyWhenNoRows() throws Exception {
        List<Client> found = dao.searchClients("missing");
        assertTrue(found.isEmpty());
    }
}
