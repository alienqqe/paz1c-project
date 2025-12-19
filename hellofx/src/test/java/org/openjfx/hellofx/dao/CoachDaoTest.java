package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.entities.Coach;

import static org.junit.jupiter.api.Assertions.*;

class CoachDaoTest extends TestContainers {

    private final CoachDAO dao = new CoachDAO();

    @Test
    void addCoachAndResolveByEmailOrName() throws Exception {
        Long id = dao.addCoach(new Coach(null, "Coach One", "coach1@mail.com", "555", null, null));

        assertNotNull(id);
        assertEquals(id, dao.findCoachIdForUser("coach1@mail.com"));
        assertEquals(id, dao.findCoachIdForUser("Coach One"));
    }
}
