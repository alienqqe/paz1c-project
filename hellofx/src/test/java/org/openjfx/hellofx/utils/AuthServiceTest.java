package org.openjfx.hellofx.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.UserDAO;
import org.openjfx.hellofx.entities.User;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    static {
        // VS Code test runner does not always inherit Maven/Surefire JVM args.
        // Mockito-inline uses Byte Buddy which currently needs this flag on Java 25.
        System.setProperty("net.bytebuddy.experimental",
            System.getProperty("net.bytebuddy.experimental", "true"));
    }

    @BeforeEach
    void setUp() {
        AuthContext.clear();
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void ensureDefaultAdminCreatesUserWhenNoUsers() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            UserDAO userDAO = mock(UserDAO.class);
            mocked.when(DaoFactory::users).thenReturn(userDAO);
            mocked.when(DaoFactory::coaches).thenReturn(mock(CoachDAO.class));

            when(userDAO.countUsers()).thenReturn(0L);

            AuthService service = new AuthService();
            service.ensureDefaultAdmin();

            verify(userDAO).createUser("admin", "admin123", "ADMIN", null);
        }
    }

    @Test
    void ensureDefaultAdminDoesNothingWhenUsersExist() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            UserDAO userDAO = mock(UserDAO.class);
            mocked.when(DaoFactory::users).thenReturn(userDAO);
            mocked.when(DaoFactory::coaches).thenReturn(mock(CoachDAO.class));

            when(userDAO.countUsers()).thenReturn(2L);

            AuthService service = new AuthService();
            service.ensureDefaultAdmin();

            verify(userDAO, never()).createUser(anyString(), anyString(), anyString(), nullable(Long.class));
        }
    }

    @Test
    void loginReturnsFalseWhenUserMissing() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            UserDAO userDAO = mock(UserDAO.class);
            mocked.when(DaoFactory::users).thenReturn(userDAO);
            mocked.when(DaoFactory::coaches).thenReturn(mock(CoachDAO.class));

            when(userDAO.findByUsername("missing")).thenReturn(Optional.empty());

            AuthService service = new AuthService();
            assertFalse(service.login("missing", "pw"));
            assertFalse(AuthContext.isLoggedIn());
        }
    }

    @Test
    void loginReturnsTrueAndSetsContextWhenPasswordMatches() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            UserDAO userDAO = mock(UserDAO.class);
            CoachDAO coachDAO = mock(CoachDAO.class);
            mocked.when(DaoFactory::users).thenReturn(userDAO);
            mocked.when(DaoFactory::coaches).thenReturn(coachDAO);

            String hash = BCrypt.hashpw("secret", BCrypt.gensalt());
            User user = new User(1L, "admin", hash, "ADMIN", null);
            when(userDAO.findByUsername("admin")).thenReturn(Optional.of(user));

            AuthService service = new AuthService();
            assertTrue(service.login("admin", "secret"));
            assertEquals(user, AuthContext.getCurrentUser());

            verifyNoInteractions(coachDAO);
        }
    }

    @Test
    void loginCoachResolvesCoachIdAndPersists() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            UserDAO userDAO = mock(UserDAO.class);
            CoachDAO coachDAO = mock(CoachDAO.class);
            mocked.when(DaoFactory::users).thenReturn(userDAO);
            mocked.when(DaoFactory::coaches).thenReturn(coachDAO);

            String hash = BCrypt.hashpw("pw", BCrypt.gensalt());
            User coachUser = new User(5L, "coachUser", hash, "COACH", null);
            when(userDAO.findByUsername("coachUser")).thenReturn(Optional.of(coachUser));
            when(coachDAO.findCoachIdForUser("coachUser")).thenReturn(42L);

            AuthService service = new AuthService();
            assertTrue(service.login("coachUser", "pw"));

            verify(coachDAO).findCoachIdForUser("coachUser");
            verify(userDAO).updateCoachId(5L, 42L);
            assertEquals(42L, AuthContext.getCurrentUser().coachId());
        }
    }

    @Test
    void logoutClearsContext() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            mocked.when(DaoFactory::users).thenReturn(mock(UserDAO.class));
            mocked.when(DaoFactory::coaches).thenReturn(mock(CoachDAO.class));

            AuthContext.setCurrentUser(new User(1L, "u", "h", "ADMIN", null));
            assertTrue(AuthContext.isLoggedIn());

            AuthService service = new AuthService();
            service.logout();

            assertFalse(AuthContext.isLoggedIn());
        }
    }

    @Test
    void createUserDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            UserDAO userDAO = mock(UserDAO.class);
            mocked.when(DaoFactory::users).thenReturn(userDAO);
            mocked.when(DaoFactory::coaches).thenReturn(mock(CoachDAO.class));

            AuthService service = new AuthService();
            service.createUser("user", "pw", "ADMIN", null);

            verify(userDAO).createUser("user", "pw", "ADMIN", null);
        }
    }

    @Test
    void changePasswordReturnsFalseWhenUserMissing() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            UserDAO userDAO = mock(UserDAO.class);
            mocked.when(DaoFactory::users).thenReturn(userDAO);
            mocked.when(DaoFactory::coaches).thenReturn(mock(CoachDAO.class));

            when(userDAO.findById(1L)).thenReturn(Optional.empty());

            AuthService service = new AuthService();
            assertFalse(service.changePassword(1L, "old", "new"));
            verify(userDAO, never()).updatePassword(anyLong(), anyString());
        }
    }

    @Test
    void changePasswordReturnsFalseWhenCurrentPasswordWrong() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            UserDAO userDAO = mock(UserDAO.class);
            mocked.when(DaoFactory::users).thenReturn(userDAO);
            mocked.when(DaoFactory::coaches).thenReturn(mock(CoachDAO.class));

            String hash = BCrypt.hashpw("correct", BCrypt.gensalt());
            when(userDAO.findById(1L)).thenReturn(Optional.of(new User(1L, "user", hash, "ADMIN", null)));

            AuthService service = new AuthService();
            assertFalse(service.changePassword(1L, "wrong", "new"));
            verify(userDAO, never()).updatePassword(anyLong(), anyString());
        }
    }

    @Test
    void changePasswordUpdatesPasswordAndRefreshesContext() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            UserDAO userDAO = mock(UserDAO.class);
            mocked.when(DaoFactory::users).thenReturn(userDAO);
            mocked.when(DaoFactory::coaches).thenReturn(mock(CoachDAO.class));

            String oldHash = BCrypt.hashpw("old", BCrypt.gensalt());
            User existing = new User(1L, "user", oldHash, "ADMIN", null);

            String newHash = BCrypt.hashpw("new", BCrypt.gensalt());
            User refreshed = new User(1L, "user", newHash, "ADMIN", null);

            when(userDAO.findById(1L)).thenReturn(Optional.of(existing), Optional.of(refreshed));

            AuthService service = new AuthService();
            assertTrue(service.changePassword(1L, "old", "new"));

            verify(userDAO).updatePassword(1L, "new");
            assertEquals(refreshed, AuthContext.getCurrentUser());
        }
    }
}
