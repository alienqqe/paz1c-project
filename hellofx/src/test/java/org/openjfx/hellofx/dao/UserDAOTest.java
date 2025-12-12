package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openjfx.hellofx.entities.User;
import org.openjfx.hellofx.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    private final UserDAO dao = new UserDAO();

    @Test
    void findByIdReturnsUser() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getLong("id")).thenReturn(42L);
            when(rs.getString("username")).thenReturn("findme");
            when(rs.getString("password_hash")).thenReturn("ph");
            when(rs.getString("role")).thenReturn("STAFF");
            when(rs.getObject("coach_id", Long.class)).thenReturn(null);

            Optional<User> result = dao.findById(42L);

            assertTrue(result.isPresent());
            assertEquals("findme", result.get().username());
            verify(stmt).setLong(1, 42L);
        }
    }

    @Test
    void findByUsernameReturnsUser() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getLong("id")).thenReturn(10L);
            when(rs.getString("username")).thenReturn("john");
            when(rs.getString("password_hash")).thenReturn("hash");
            when(rs.getString("role")).thenReturn("ADMIN");
            when(rs.getObject("coach_id", Long.class)).thenReturn(5L);

            Optional<User> result = dao.findByUsername("john");

            assertTrue(result.isPresent());
            User user = result.get();
            assertEquals(10L, user.id());
            assertEquals("john", user.username());
            assertEquals("hash", user.passwordHash());
            assertEquals("ADMIN", user.role());
            assertEquals(5L, user.coachId());
            verify(stmt).setString(1, "john");
        }
    }

    @Test
    void findByUsernameEmptyWhenNotFound() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            Optional<User> result = dao.findByUsername("missing");

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void countUsersReturnsValue() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getLong(1)).thenReturn(3L);

            long count = dao.countUsers();

            assertEquals(3L, count);
        }
    }

    @Test
    void createUserHashesPasswordAndPersists() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class);
             MockedStatic<BCrypt> mockedBcrypt = mockStatic(BCrypt.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            mockedBcrypt.when(() -> BCrypt.hashpw(anyString(), anyString())).thenReturn("hashed");
            mockedBcrypt.when(BCrypt::gensalt).thenReturn("salt");

            when(conn.prepareStatement(anyString())).thenReturn(stmt);

            dao.createUser("jane", "secret", "ADMIN", 2L);

            verify(stmt).setString(1, "jane");
            verify(stmt).setString(2, "hashed");
            verify(stmt).setString(3, "ADMIN");
            verify(stmt).setLong(4, 2L);
            verify(stmt).executeUpdate();
        }
    }

    @Test
    void createUserAllowsNullCoach() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class);
             MockedStatic<BCrypt> mockedBcrypt = mockStatic(BCrypt.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            mockedBcrypt.when(() -> BCrypt.hashpw(anyString(), anyString())).thenReturn("hashed");
            mockedBcrypt.when(BCrypt::gensalt).thenReturn("salt");

            when(conn.prepareStatement(anyString())).thenReturn(stmt);

            dao.createUser("jane", "secret", "ADMIN", null);

            verify(stmt).setNull(4, java.sql.Types.BIGINT);
            verify(stmt).executeUpdate();
        }
    }

    @Test
    void updatePasswordHashesNewValue() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class);
             MockedStatic<BCrypt> mockedBcrypt = mockStatic(BCrypt.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            mockedBcrypt.when(() -> BCrypt.hashpw(anyString(), anyString())).thenReturn("newHash");
            mockedBcrypt.when(BCrypt::gensalt).thenReturn("salt2");

            when(conn.prepareStatement(anyString())).thenReturn(stmt);

            dao.updatePassword(7L, "newPass");

            verify(stmt).setString(1, "newHash");
            verify(stmt).setLong(2, 7L);
            verify(stmt).executeUpdate();
        }
    }

    @Test
    void updateCoachIdHandlesNull() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);

            dao.updateCoachId(3L, null);

            verify(stmt).setNull(1, java.sql.Types.BIGINT);
            verify(stmt).setLong(2, 3L);
            verify(stmt).executeUpdate();
        }
    }
}
