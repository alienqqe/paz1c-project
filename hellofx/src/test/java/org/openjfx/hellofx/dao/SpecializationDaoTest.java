package org.openjfx.hellofx.dao;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.entities.Specialization;

import static org.junit.jupiter.api.Assertions.*;

class SpecializationDaoTest extends TestContainers {

    private final SpecializationDAO dao = new SpecializationDAO();
    private final CoachDAO coachDao = new CoachDAO();

    @Test
    void ensureAndAssignSpecializations() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Spec Coach", "spec@mail.com", "777", Set.of()));

        Long specId = dao.ensureSpecialization("Yoga");
        assertNotNull(specId);

        dao.setSpecializationsForCoach(coachId, Set.of("Yoga", "Pilates"));

        Set<Specialization> specs = dao.getSpecializationsForCoach(coachId);
        assertEquals(2, specs.size());
        assertTrue(specs.stream().anyMatch(s -> s.name().equals("Yoga")));
        assertTrue(specs.stream().anyMatch(s -> s.name().equals("Pilates")));
    }
}
