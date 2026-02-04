package org.openjfx.hellofx.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openjfx.hellofx.dao.CoachAvailabilityDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.model.AvailabilitySlot;

class CoachAvailabilityServiceTest {

    @Test
    void deleteExpiredDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            CoachAvailabilityDAO dao = mock(CoachAvailabilityDAO.class);
            mocked.when(DaoFactory::coachAvailability).thenReturn(dao);

            CoachAvailabilityService service = new CoachAvailabilityService();
            service.deleteExpired();

            verify(dao).deleteExpired();
        }
    }

    @Test
    void hasOverlapDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            CoachAvailabilityDAO dao = mock(CoachAvailabilityDAO.class);
            mocked.when(DaoFactory::coachAvailability).thenReturn(dao);
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusHours(1);
            when(dao.hasOverlap(1L, start, end)).thenReturn(true);

            CoachAvailabilityService service = new CoachAvailabilityService();
            boolean result = service.hasOverlap(1L, start, end);

            assertEquals(true, result);
            verify(dao).hasOverlap(1L, start, end);
        }
    }

    @Test
    void addAvailabilityDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            CoachAvailabilityDAO dao = mock(CoachAvailabilityDAO.class);
            mocked.when(DaoFactory::coachAvailability).thenReturn(dao);
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusHours(1);

            CoachAvailabilityService service = new CoachAvailabilityService();
            service.addAvailability(2L, start, end, "note");

            verify(dao).addAvailability(2L, start, end, "note");
        }
    }

    @Test
    void isWithinAvailabilityDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            CoachAvailabilityDAO dao = mock(CoachAvailabilityDAO.class);
            mocked.when(DaoFactory::coachAvailability).thenReturn(dao);
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusHours(1);
            when(dao.isWithinAvailability(3L, start, end)).thenReturn(false);

            CoachAvailabilityService service = new CoachAvailabilityService();
            boolean result = service.isWithinAvailability(3L, start, end);

            assertEquals(false, result);
            verify(dao).isWithinAvailability(3L, start, end);
        }
    }

    @Test
    void consumeAvailabilityDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            CoachAvailabilityDAO dao = mock(CoachAvailabilityDAO.class);
            mocked.when(DaoFactory::coachAvailability).thenReturn(dao);
            AvailabilitySlot slot = new AvailabilitySlot(
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "note"
            );
            LocalDateTime start = slot.start().plusMinutes(10);
            LocalDateTime end = slot.end().minusMinutes(10);

            CoachAvailabilityService service = new CoachAvailabilityService();
            service.consumeAvailability(4L, slot, start, end);

            verify(dao).consumeAvailability(4L, slot, start, end);
        }
    }

}
