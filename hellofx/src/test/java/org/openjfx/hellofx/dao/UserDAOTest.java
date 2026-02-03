package org.openjfx.hellofx.dao;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.openjfx.hellofx.TestContainers;
import org.openjfx.hellofx.entities.Coach;
import org.openjfx.hellofx.entities.User;

class UserDAOTest extends TestContainers {

    private final UserDAO dao = new UserDAO();
    private final CoachDAO coachDao = new CoachDAO();

    @Test
    void createAndFindUser() throws Exception {
        dao.createUser("john", "secret", "ADMIN", null);

        Optional<User> found = dao.findByUsername("john");
        assertTrue(found.isPresent());
        assertEquals("ADMIN", found.get().role());
        assertTrue(BCrypt.checkpw("secret", found.get().passwordHash()));
    }

    @Test
    void findByUsernameAndFindByIdReturnEmptyWhenMissing() throws Exception {
        assertTrue(dao.findByUsername("missing").isEmpty());
        assertTrue(dao.findById(99999L).isEmpty());
    }

    @Test
    void countUsersReflectsInsertedRows() throws Exception {
        long before = dao.countUsers();
        dao.createUser("user1", "p1", "STAFF", null);
        dao.createUser("user2", "p2", "STAFF", null);
        assertEquals(before + 2, dao.countUsers());
    }

    @Test
    void updatePasswordChangesHash() throws Exception {
        dao.createUser("mary", "old", "STAFF", null);
        Long id = dao.findByUsername("mary").orElseThrow().id();

        dao.updatePassword(id, "new");

        User changed = dao.findById(id).orElseThrow();
        assertTrue(BCrypt.checkpw("new", changed.passwordHash()));
    }

    @Test
    void updateCoachIdNullsValue() throws Exception {
        Long coachId = coachDao.addCoach(new Coach(null, "Coachy", "coachy@mail.com", "999", Set.of()));
        dao.createUser("coachy", "p", "COACH", coachId);
        Long id = dao.findByUsername("coachy").orElseThrow().id();

        dao.updateCoachId(id, null);

        assertNull(dao.findById(id).orElseThrow().coachId());
    }
}
