package org.openjfx.hellofx.dao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.TestContainers;
import org.openjfx.hellofx.entities.Specialization;

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

    @Test
    void ensureSpecializationIsIdempotent() throws Exception {
        Long first = dao.ensureSpecialization("CrossFit");
        Long second = dao.ensureSpecialization("CrossFit");

        assertNotNull(first);
        assertEquals(first, second);
    }

    @Test
    void setSpecializationsReplacesAndTrimsAndCanClear() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Trim Coach", "trim@mail.com", "123", Set.of()));

        dao.setSpecializationsForCoach(coachId, new HashSet<>(Arrays.asList("  Boxing  ", " ", "", null, "Boxing")));
        Set<Specialization> specs = dao.getSpecializationsForCoach(coachId);
        assertEquals(1, specs.size());
        assertTrue(specs.stream().anyMatch(s -> "Boxing".equals(s.name())));

        dao.setSpecializationsForCoach(coachId, Set.of("Yoga"));
        Set<Specialization> replaced = dao.getSpecializationsForCoach(coachId);
        assertEquals(1, replaced.size());
        assertTrue(replaced.stream().anyMatch(s -> "Yoga".equals(s.name())));

        dao.setSpecializationsForCoach(coachId, Set.of());
        assertTrue(dao.getSpecializationsForCoach(coachId).isEmpty());
    }
}
