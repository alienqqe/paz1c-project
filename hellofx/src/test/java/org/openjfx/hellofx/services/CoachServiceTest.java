package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.entities.Coach;
import org.openjfx.hellofx.entities.Specialization;

class CoachServiceTest {

    @Test
    void findCoachIdForUserDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            CoachDAO coachDAO = mock(CoachDAO.class);
            mocked.when(DaoFactory::coaches).thenReturn(coachDAO);
            when(coachDAO.findCoachIdForUser("user1")).thenReturn(42L);

            CoachService service = new CoachService();
            Long result = service.findCoachIdForUser("user1");

            assertEquals(42L, result);
            verify(coachDAO).findCoachIdForUser("user1");
        }
    }

    @Test
    void searchCoachesDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            CoachDAO coachDAO = mock(CoachDAO.class);
            mocked.when(DaoFactory::coaches).thenReturn(coachDAO);
            List<Coach> coaches = List.of(
                new Coach(1L, "Coach", "c@test.com", "111", Set.of(new Specialization(1L, "Strength")))
            );
            when(coachDAO.searchCoaches("c")).thenReturn(coaches);

            CoachService service = new CoachService();
            List<Coach> result = service.searchCoaches("c");

            assertSame(coaches, result);
            verify(coachDAO).searchCoaches("c");
        }
    }

}
