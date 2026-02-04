package org.openjfx.hellofx.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.TestContainers;
import org.openjfx.hellofx.model.AvailabilitySlot;
import org.openjfx.hellofx.model.CoachAvailabilityRow;

class CoachAvailabilityDaoTest extends TestContainers {

    private final CoachAvailabilityDAO dao = new CoachAvailabilityDAO();
    private final CoachDAO coachDao = new CoachDAO();

    @Test
    void addAndQueryAvailability() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "C A", "ca@mail.com", "999", Set.of()));
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        dao.addAvailability(coachId, start, end, "note");

        List<AvailabilitySlot> slots = dao.getAvailabilityForDate(coachId, start.toLocalDate());
        assertEquals(1, slots.size());
        assertEquals("note", slots.get(0).note());
        assertTrue(dao.isWithinAvailability(coachId, start.plusMinutes(15), end.minusMinutes(15)));
    }

    @Test
    void deleteExpiredRemovesOldRecords() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Old", "old@mail.com", "000", Set.of()));
        LocalDateTime pastStart = LocalDateTime.now().minusDays(2);
        dao.addAvailability(coachId, pastStart, pastStart.plusHours(1), "old");

        dao.deleteExpired();

        List<AvailabilitySlot> slots = dao.getAvailabilityForDate(coachId, LocalDate.now().minusDays(2));
        assertTrue(slots.isEmpty());
    }

    @Test
    void hasOverlapTreatsTouchingAsNonOverlapping() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Overlap", "overlap@mail.com", "111", Set.of()));
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);
        dao.addAvailability(coachId, start, end, "slot");

        assertTrue(dao.hasOverlap(coachId, start.plusMinutes(30), start.plusMinutes(90)));
        assertFalse(dao.hasOverlap(coachId, end, end.plusHours(1)));
    }

    @Test
    void listUpcomingAndDeleteAvailabilityById() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Upcoming", "up@mail.com", "222", Set.of()));
        LocalDateTime now = LocalDateTime.now();

        dao.addAvailability(coachId, now.minusDays(2), now.minusDays(2).plusHours(1), "past");

        LocalDateTime s1 = now.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime s2 = now.plusDays(1).withHour(13).withMinute(0).withSecond(0).withNano(0);
        dao.addAvailability(coachId, s2, s2.plusHours(1), "b");
        dao.addAvailability(coachId, s1, s1.plusHours(1), "a");

        List<CoachAvailabilityRow> upcoming = dao.listUpcomingForCoach(coachId);
        assertEquals(2, upcoming.size());
        assertEquals(s1, upcoming.get(0).start());
        assertEquals(s2, upcoming.get(1).start());

        Long firstId = upcoming.get(0).id();
        assertEquals(1, dao.deleteAvailability(coachId, firstId));
        assertEquals(1, dao.listUpcomingForCoach(coachId).size());
        Long remainingId = dao.listUpcomingForCoach(coachId).get(0).id();
        assertEquals(0, dao.deleteAvailability(coachId + 999, remainingId));
    }

    @Test
    void consumeAvailabilitySplitsSlotWhenBookingInMiddle() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Consume", "consume@mail.com", "333", Set.of()));
        LocalDate date = LocalDate.now().plusDays(2);
        LocalDateTime start = date.atTime(10, 0);
        LocalDateTime end = date.atTime(14, 0);

        dao.addAvailability(coachId, start, end, "Available");

        LocalDateTime bookStart = start.plusHours(1);
        LocalDateTime bookEnd = start.plusHours(2);
        dao.consumeAvailability(coachId, new AvailabilitySlot(start, end, "Available"), bookStart, bookEnd);

        List<AvailabilitySlot> slots = dao.getAvailabilityForDate(coachId, date);
        assertEquals(2, slots.size());
        assertEquals(start, slots.get(0).start());
        assertEquals(bookStart, slots.get(0).end());
        assertEquals(bookEnd, slots.get(1).start());
        assertEquals(end, slots.get(1).end());
    }

    @Test
    void restoreAvailabilityMergesAdjacentSlots() throws Exception {
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Restore", "restore@mail.com", "444", Set.of()));
        LocalDate date = LocalDate.now().plusDays(4);
        LocalDateTime s1 = date.atTime(10, 0);
        LocalDateTime s2 = date.atTime(11, 0);
        LocalDateTime s3 = date.atTime(12, 0);
        LocalDateTime s4 = date.atTime(13, 0);

        dao.addAvailability(coachId, s1, s2, "Available");
        dao.addAvailability(coachId, s3, s4, "Available");

        dao.restoreAvailability(coachId, s2, s3);

        List<AvailabilitySlot> slots = dao.getAvailabilityForDate(coachId, date);
        assertEquals(1, slots.size());
        assertEquals(s1, slots.get(0).start());
        assertEquals(s4, slots.get(0).end());
    }
}
