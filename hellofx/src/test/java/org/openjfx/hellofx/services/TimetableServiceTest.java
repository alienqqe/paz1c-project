package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.TimetableDAO;
import org.openjfx.hellofx.model.WeeklySession;

class TimetableServiceTest {

    @Test
    void hasConflictingSessionDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            TimetableDAO dao = mock(TimetableDAO.class);
            mocked.when(DaoFactory::timetable).thenReturn(dao);
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusHours(1);
            when(dao.hasConflictingSession(2L, start, end)).thenReturn(false);

            TimetableService service = new TimetableService();
            boolean result = service.hasConflictingSession(2L, start, end);

            assertEquals(false, result);
            verify(dao).hasConflictingSession(2L, start, end);
        }
    }

    @Test
    void getWeeklySessionsDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            TimetableDAO dao = mock(TimetableDAO.class);
            mocked.when(DaoFactory::timetable).thenReturn(dao);
            LocalDate weekStart = LocalDate.now();
            List<WeeklySession> sessions = List.of(
                new WeeklySession(1L, "Coach", "Client", weekStart.getDayOfWeek(), LocalTime.NOON, LocalTime.NOON.plusHours(1), "Title")
            );
            when(dao.getWeeklySessions(weekStart)).thenReturn(sessions);

            TimetableService service = new TimetableService();
            List<WeeklySession> result = service.getWeeklySessions(weekStart);

            assertSame(sessions, result);
            verify(dao).getWeeklySessions(weekStart);
        }
    }

}
