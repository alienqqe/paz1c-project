package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.model.WeeklySession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimetableDaoTest extends TestContainers {

    private final TimetableDAO dao = new TimetableDAO();
    private final ClientDAO clientDao = new ClientDAO();
    private final CoachDAO coachDao = new CoachDAO();

    @Test
    void addAndListWeeklySessions() throws Exception {
        Long clientId = seedClient("tt-client", "tt@c.com");
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "TT Coach", "tt@coach.com", "000", null, null));

        LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDateTime start = weekStart.plusDays(1).atTime(10, 0);
        LocalDateTime end = start.plusHours(1);

        dao.addTrainingSession(clientId, coachId, start, end, "Session A");

        List<WeeklySession> sessions = dao.getWeeklySessions(weekStart);
        assertFalse(sessions.isEmpty());
        assertEquals("Session A", sessions.get(0).title());
    }

    @Test
    void detectsConflicts() throws Exception {
        Long clientId = seedClient("tt-client2", "tt2@c.com");
        Long coachId = coachDao.addCoach(new org.openjfx.hellofx.entities.Coach(null, "TT Coach2", "tt2@coach.com", "001", null, null));

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
        LocalDateTime end = start.plusHours(1);
        dao.addTrainingSession(clientId, coachId, start, end, "Session B");

        assertTrue(dao.hasConflictingSession(coachId, start.plusMinutes(15), end.minusMinutes(15)));
        assertFalse(dao.hasConflictingSession(coachId, end.plusHours(1), end.plusHours(2)));
    }

    private Long seedClient(String name, String email) throws Exception {
        clientDao.addClient(new org.openjfx.hellofx.entities.Client(null, name, email, "111"));
        return clientDao.searchClients(email).get(0).id();
    }
}
