package org.openjfx.hellofx.dao;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.TestContainers;
import org.openjfx.hellofx.entities.Coach;

class CoachDaoTest extends TestContainers {

    private CoachDAO dao;
    private SpecializationDAO specializationDao;

    @BeforeEach
    void setUp() {
        dao = new CoachDAO();
        specializationDao = new SpecializationDAO();
    }

    @Test
    void addCoachAndResolveByName() throws Exception {
        Long id = dao.addCoach(new Coach(null, "Coach One", "coach1@mail.com", "555", Set.of()));

        assertNotNull(id);
        assertEquals(id, dao.findCoachIdForUser("Coach One"));
    }

    @Test
    void findCoachIdForUserReturnsNullWhenBlankOrMissing() throws Exception {
        assertNull(dao.findCoachIdForUser(null));
        assertNull(dao.findCoachIdForUser(" "));
        assertNull(dao.findCoachIdForUser("Missing Coach"));
    }

    @Test
    void searchCoachesFindsByNameOrEmailAndAttachesSpecializations() throws Exception {
        Long coachId = dao.addCoach(new Coach(null, "Spec Coach", "spec.coach@example.org", "777", Set.of()));
        specializationDao.setSpecializationsForCoach(coachId, Set.of("Yoga", "Pilates"));

        var byName = dao.searchCoaches("spec");
        assertEquals(1, byName.size());
        assertEquals("Spec Coach", byName.get(0).name());
        assertNotNull(byName.get(0).specializations());
        assertEquals(2, byName.get(0).specializations().size());

        var byEmail = dao.searchCoaches("EXAMPLE.ORG");
        assertEquals(1, byEmail.size());
        assertEquals("spec.coach@example.org", byEmail.get(0).email());
    }
}
