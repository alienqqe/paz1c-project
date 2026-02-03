package org.openjfx.hellofx.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.TestContainers;
import org.openjfx.hellofx.entities.TrainingSession;
import org.openjfx.hellofx.model.AvailabilitySlot;
import org.openjfx.hellofx.model.WeeklySession;
import org.openjfx.hellofx.utils.Database;

class TimetableDaoTest extends TestContainers {

    private final TimetableDAO dao = new TimetableDAO();
    private final ClientDAO clientDao = new ClientDAO();
    private final CoachDAO coachDao = new CoachDAO();
    private final CoachAvailabilityDAO availabilityDao = new CoachAvailabilityDAO();

    @Test
    void addAndListWeeklySessions() throws Exception {
        Long clientId = seedClient("tt-client", "tt@c.com");
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "TT Coach", "tt@coach.com", "000", Set.of()));

        LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDateTime start = weekStart.plusDays(1).atTime(10, 0);
        LocalDateTime end = start.plusHours(1);

        dao.addTrainingSession(clientId, coachId, start, end, "Session A");

        List<WeeklySession> sessions = dao.getWeeklySessions(weekStart);
        assertFalse(sessions.isEmpty());

        WeeklySession s = sessions.get(0);
        assertEquals("TT Coach", s.coachName());
        assertEquals("tt-client", s.clientName());
        assertEquals(start.toLocalDate().getDayOfWeek(), s.day());
        assertEquals(start.toLocalTime(), s.start());
        assertEquals(end.toLocalTime(), s.end());
        assertEquals("Session A", s.title());
    }

    @Test
    void detectsConflicts() throws Exception {
        Long clientId = seedClient("tt-client2", "tt2@c.com");
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "TT Coach2", "tt2@coach.com", "001", Set.of()));

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        dao.addTrainingSession(clientId, coachId, start, end, "Session B");

        assertTrue(dao.hasConflictingSession(coachId, start.plusMinutes(15), end.minusMinutes(15)));
        assertFalse(dao.hasConflictingSession(coachId, end.plusHours(1), end.plusHours(2)));
    }

    @Test
    void findAndDeleteTrainingSession() throws Exception {
        Long clientId = seedClient("find-client", "find@c.com");
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Find Coach", "find@coach.com", "101", Set.of()));

        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        dao.addTrainingSession(clientId, coachId, start, end, "Find Me");

        Long sessionId = Database.jdbc().queryForObject("SELECT id FROM training_sessions LIMIT 1", Long.class);
        assertNotNull(sessionId);

        TrainingSession found = dao.findTrainingSession(sessionId);
        assertNotNull(found);
        assertEquals(coachId, found.coachId());
        assertEquals(start, found.startDate());
        assertEquals(end, found.endDate());

        dao.deleteTrainingSession(sessionId);
        assertNull(dao.findTrainingSession(sessionId));
    }

    @Test
    void deleteSessionAndRestoreAvailabilityMergesAdjacentSlots() throws Exception {
        Long clientId = seedClient("restore-client", "restore@c.com");
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "Restore Coach", "restore@coach.com", "202", Set.of()));

        LocalDate date = LocalDate.now().plusDays(3);
        LocalDateTime slotStart = date.atTime(10, 0);
        LocalDateTime slotMid1 = date.atTime(11, 0);
        LocalDateTime slotMid2 = date.atTime(12, 0);
        LocalDateTime slotEnd = date.atTime(13, 0);

        availabilityDao.addAvailability(coachId, slotStart, slotMid1, "Available");
        availabilityDao.addAvailability(coachId, slotMid2, slotEnd, "Available");

        dao.addTrainingSession(clientId, coachId, slotMid1, slotMid2, "Booked");
        Long sessionId = Database.jdbc().queryForObject("SELECT id FROM training_sessions LIMIT 1", Long.class);
        assertNotNull(sessionId);

        dao.deleteSessionAndRestoreAvailability(sessionId);
        assertNull(dao.findTrainingSession(sessionId));

        List<AvailabilitySlot> slots = availabilityDao.getAvailabilityForDate(coachId, date);
        assertEquals(1, slots.size());
        assertEquals(slotStart, slots.get(0).start());
        assertEquals(slotEnd, slots.get(0).end());
    }

    private Long seedClient(String name, String email) throws Exception {
        clientDao.addClient(new org.openjfx.hellofx.entities.Client(null, name, email, "111"));
        return clientDao.searchClients(email).get(0).id();
    }
}
