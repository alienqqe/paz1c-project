package org.openjfx.hellofx.services;

import java.time.LocalDateTime;
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
import org.openjfx.hellofx.dao.VisitDAO;
import org.openjfx.hellofx.model.VisitRow;

class VisitServiceTest {

    @Test
    void getRecentVisitsForClientDelegatesToDao() {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            VisitDAO dao = mock(VisitDAO.class);
            mocked.when(DaoFactory::visits).thenReturn(dao);
            List<VisitRow> rows = List.of(new VisitRow(1L, "A", "a@test.com", "Monthly", LocalDateTime.now()));
            when(dao.getRecentVisitsForClient("john", 5)).thenReturn(rows);

            VisitService service = new VisitService();
            List<VisitRow> result = service.getRecentVisitsForClient("john", 5);

            assertSame(rows, result);
            verify(dao).getRecentVisitsForClient("john", 5);
        }
    }

    @Test
    void countVisitsForClientDelegatesToDao() throws Exception {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            VisitDAO dao = mock(VisitDAO.class);
            mocked.when(DaoFactory::visits).thenReturn(dao);
            when(dao.countVisitsForClient(2L)).thenReturn(7);

            VisitService service = new VisitService();
            int result = service.countVisitsForClient(2L);

            assertEquals(7, result);
            verify(dao).countVisitsForClient(2L);
        }
    }

}
