package org.openjfx.hellofx.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.MembershipDAO;

class MembershipServiceTest {

    @Test
    void hasActiveMembershipDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            MembershipDAO dao = mock(MembershipDAO.class);
            mocked.when(DaoFactory::memberships).thenReturn(dao);
            when(dao.hasActiveMembership(7L)).thenReturn(true);

            MembershipService service = new MembershipService();
            boolean result = service.hasActiveMembership(7L);

            assertEquals(true, result);
            verify(dao).hasActiveMembership(7L);
        }
    }

    @Test
    void getCurrentMembershipTypeDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            MembershipDAO dao = mock(MembershipDAO.class);
            mocked.when(DaoFactory::memberships).thenReturn(dao);
            when(dao.getCurrentMembershipType(5L)).thenReturn("Monthly");

            MembershipService service = new MembershipService();
            String result = service.getCurrentMembershipType(5L);

            assertEquals("Monthly", result);
            verify(dao).getCurrentMembershipType(5L);
        }
    }

    @Test
    void removeByHolderIdDelegatesToDao() throws SQLException {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            MembershipDAO dao = mock(MembershipDAO.class);
            mocked.when(DaoFactory::memberships).thenReturn(dao);

            MembershipService service = new MembershipService();
            service.removeByHolderId(7L);

            verify(dao).removeByHolderId(7L);
        }
    }
}
