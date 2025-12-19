package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.model.AvailabilitySlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoachAvailabilityDaoTest extends TestContainers {

    private final CoachAvailabilityDAO dao = new CoachAvailabilityDAO();
    private final CoachDAO coachDao = new CoachDAO();

    @Test
    void addAndQueryAvailability() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "C A", "ca@mail.com", "999", null, null));
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2);

        dao.addAvailability(coachId, start, end, "note");

        List<AvailabilitySlot> slots = dao.getAvailabilityForDate(coachId, start.toLocalDate());
        assertEquals(1, slots.size());
        assertEquals("note", slots.get(0).note());
        assertTrue(dao.isWithinAvailability(coachId, start.plusMinutes(15), end.minusMinutes(15)));
    }

    @Test
    void deleteExpiredRemovesOldRecords() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Old", "old@mail.com", "000", null, null));
        LocalDateTime pastStart = LocalDateTime.now().minusDays(2);
        dao.addAvailability(coachId, pastStart, pastStart.plusHours(1), "old");

        dao.deleteExpired();

        List<AvailabilitySlot> slots = dao.getAvailabilityForDate(coachId, LocalDate.now().minusDays(2));
        assertTrue(slots.isEmpty());
    }
}
