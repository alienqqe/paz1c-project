package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.TimetableDAO;
import org.openjfx.hellofx.model.WeeklySession;

public class TimetableService {
    private final TimetableDAO timetableDAO = DaoFactory.timetable();

    public List<WeeklySession> getWeeklySessions(LocalDate weekStart) throws SQLException {
        return timetableDAO.getWeeklySessions(weekStart);
    }

    public void deleteSessionAndRestoreAvailability(Long sessionId) throws SQLException {
        timetableDAO.deleteSessionAndRestoreAvailability(sessionId);
    }

    public boolean hasConflictingSession(Long coachId, LocalDateTime start, LocalDateTime end) throws SQLException {
        return timetableDAO.hasConflictingSession(coachId, start, end);
    }

    public void addTrainingSession(Long clientId, Long coachId, LocalDateTime start, LocalDateTime end, String title) throws SQLException {
        timetableDAO.addTrainingSession(clientId, coachId, start, end, title);
    }
}
