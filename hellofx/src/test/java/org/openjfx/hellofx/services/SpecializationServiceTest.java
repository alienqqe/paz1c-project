package org.openjfx.hellofx.services;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.SpecializationDAO;
import org.openjfx.hellofx.entities.Specialization;

class SpecializationServiceTest {

    @Test
    void setForCoachDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            SpecializationDAO dao = mock(SpecializationDAO.class);
            mocked.when(DaoFactory::specializations).thenReturn(dao);
            Set<String> names = Set.of("Strength");

            SpecializationService service = new SpecializationService();
            service.setForCoach(3L, names);

            verify(dao).setSpecializationsForCoach(3L, names);
        }
    }

    @Test
    void getForCoachDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            SpecializationDAO dao = mock(SpecializationDAO.class);
            mocked.when(DaoFactory::specializations).thenReturn(dao);
            Set<Specialization> resultSet = Set.of(new Specialization(1L, "Strength"));
            when(dao.getSpecializationsForCoach(2L)).thenReturn(resultSet);

            SpecializationService service = new SpecializationService();
            Set<Specialization> result = service.getForCoach(2L);

            assertSame(resultSet, result);
            verify(dao).getSpecializationsForCoach(2L);
        }
    }
}
