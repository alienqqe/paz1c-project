package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openjfx.hellofx.dao.ClientDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.entities.Client;

class ClientServiceTest {

    @Test
    void addClientDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            ClientDAO clientDAO = mock(ClientDAO.class);
            mocked.when(DaoFactory::clients).thenReturn(clientDAO);

            ClientService service = new ClientService();
            Client client = new Client(1L, "A", "a@test.com", "123");
            service.addClient(client);

            verify(clientDAO).addClient(client);
        }
    }

    @Test
    void searchClientsDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            ClientDAO clientDAO = mock(ClientDAO.class);
            mocked.when(DaoFactory::clients).thenReturn(clientDAO);
            List<Client> clients = List.of(new Client(1L, "A", "a@test.com", "123"));
            when(clientDAO.searchClients("a")).thenReturn(clients);

            ClientService service = new ClientService();
            List<Client> result = service.searchClients("a");

            assertSame(clients, result);
            verify(clientDAO).searchClients("a");
        }
    }

}
