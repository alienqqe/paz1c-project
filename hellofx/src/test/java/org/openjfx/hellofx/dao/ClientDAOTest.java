package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientDAOTest {

    private final ClientDAO dao = new ClientDAO();

    @Test
    void addClientSendsCorrectParams() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);

            Client client = new Client(null, "Alice", "alice@test.com", "123");
            dao.addClient(client);

            verify(stmt).setString(1, "Alice");
            verify(stmt).setString(2, "alice@test.com");
            verify(stmt).setString(3, "123");
            verify(stmt).executeUpdate();
        }
    }

    @Test
    void searchClientsReturnsMatches() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("id")).thenReturn(5L);
            when(rs.getString("name")).thenReturn("Alice");
            when(rs.getString("email")).thenReturn("alice@test.com");
            when(rs.getString("phone_number")).thenReturn("123");

            List<Client> found = dao.searchClients("alice");

            assertEquals(1, found.size());
            Client c = found.get(0);
            assertEquals(5L, c.id());
            assertEquals("Alice", c.name());
            assertEquals("alice@test.com", c.email());
            assertEquals("123", c.phoneNumber());
            verify(stmt).setString(1, "%alice%");
            verify(stmt).setString(2, "%alice%");
        }
    }

    @Test
    void searchClientsEmptyWhenNoRows() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            List<Client> found = dao.searchClients("missing");

            assertTrue(found.isEmpty());
        }
    }
}
