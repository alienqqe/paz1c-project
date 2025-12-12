package org.openjfx.hellofx.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.dao.UserDAO;
import org.openjfx.hellofx.entities.User;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private CoachDAO coachDAO;

    private AuthService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new AuthService();
        inject(service, "userDAO", userDAO);
        inject(service, "coachDAO", coachDAO);
        AuthContext.clear();
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void ensureDefaultAdminCreatesWhenEmpty() throws Exception {
        when(userDAO.countUsers()).thenReturn(0L);

        service.ensureDefaultAdmin();

        verify(userDAO).createUser("admin", "admin123", "ADMIN", null);
    }

    @Test
    void ensureDefaultAdminSkipsWhenUsersExist() throws Exception {
        when(userDAO.countUsers()).thenReturn(2L);

        service.ensureDefaultAdmin();

        verify(userDAO, never()).createUser(anyString(), anyString(), anyString(), any());
    }

    @Test
    void loginSuccessSetsContextAndResolvesCoachId() throws Exception {
        User user = new User(1L, "coach1", "storedHash", "COACH", null);
        when(userDAO.findByUsername("coach1")).thenReturn(Optional.of(user));
        when(coachDAO.findCoachIdForUser("coach1")).thenReturn(99L);

        try (MockedStatic<BCrypt> mockedBcrypt = mockStatic(BCrypt.class)) {
            mockedBcrypt.when(() -> BCrypt.checkpw("secret", "storedHash")).thenReturn(true);

            boolean loggedIn = service.login("coach1", "secret");

            assertTrue(loggedIn);
            assertNotNull(AuthContext.getCurrentUser());
            assertEquals(99L, AuthContext.getCurrentUser().coachId());
            verify(userDAO).updateCoachId(1L, 99L);
        }
    }

    @Test
    void loginFailsWhenPasswordWrong() throws Exception {
        User user = new User(2L, "john", "hash", "ADMIN", null);
        when(userDAO.findByUsername("john")).thenReturn(Optional.of(user));

        try (MockedStatic<BCrypt> mockedBcrypt = mockStatic(BCrypt.class)) {
            mockedBcrypt.when(() -> BCrypt.checkpw("bad", "hash")).thenReturn(false);

            boolean loggedIn = service.login("john", "bad");

            assertFalse(loggedIn);
            assertFalse(AuthContext.isLoggedIn());
        }
    }

    @Test
    void loginFailsWhenUserMissing() throws Exception {
        when(userDAO.findByUsername("nouser")).thenReturn(Optional.empty());

        boolean loggedIn = service.login("nouser", "pwd");

        assertFalse(loggedIn);
        assertFalse(AuthContext.isLoggedIn());
    }

    @Test
    void changePasswordUpdatesHashAndContext() throws Exception {
        User existing = new User(5L, "mary", "oldHash", "STAFF", null);
        User updated = new User(5L, "mary", "newHash", "STAFF", null);
        when(userDAO.findById(5L)).thenReturn(Optional.of(existing), Optional.of(updated));

        try (MockedStatic<BCrypt> mockedBcrypt = mockStatic(BCrypt.class)) {
            mockedBcrypt.when(() -> BCrypt.checkpw("current", "oldHash")).thenReturn(true);
            mockedBcrypt.when(() -> BCrypt.hashpw("newPass", "salt")).thenReturn("newHash");
            mockedBcrypt.when(BCrypt::gensalt).thenReturn("salt");

            boolean changed = service.changePassword(5L, "current", "newPass");

            assertTrue(changed);
            verify(userDAO).updatePassword(5L, "newPass");
            assertEquals("newHash", AuthContext.getCurrentUser().passwordHash());
        }
    }

    @Test
    void changePasswordFailsOnWrongCurrent() throws Exception {
        User existing = new User(6L, "kate", "oldHash", "STAFF", null);
        when(userDAO.findById(6L)).thenReturn(Optional.of(existing));

        try (MockedStatic<BCrypt> mockedBcrypt = mockStatic(BCrypt.class)) {
            mockedBcrypt.when(() -> BCrypt.checkpw("wrong", "oldHash")).thenReturn(false);

            boolean changed = service.changePassword(6L, "wrong", "newPass");

            assertFalse(changed);
            verify(userDAO, never()).updatePassword(anyLong(), anyString());
            assertFalse(AuthContext.isLoggedIn());
        }
    }

    @Test
    void logoutClearsContext() {
        AuthContext.setCurrentUser(new User(7L, "tmp", "h", "ADMIN", null));
        assertTrue(AuthContext.isLoggedIn());

        service.logout();

        assertFalse(AuthContext.isLoggedIn());
        assertNull(AuthContext.getCurrentUser());
    }

    private void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
