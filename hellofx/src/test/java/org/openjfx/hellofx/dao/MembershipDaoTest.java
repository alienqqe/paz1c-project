package org.openjfx.hellofx.dao;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.TestContainers;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.entities.Membership;

class MembershipDaoTest extends TestContainers {

    private final MembershipDAO dao = new MembershipDAO();
    private final ClientDAO clientDao = new ClientDAO();
    private Long clientId;

    @BeforeEach
    void seedClient() throws Exception {
        clientDao.addClient(new Client(null, "Member", "member@test.com", "111"));
        clientId = clientDao.searchClients("member").get(0).id();
    }

    @Test
    void addMembershipAndResolveCurrentType() throws Exception {
        Membership m = new Membership(
            null,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(30),
            49.9,
            Membership.MembershipType.Monthly,
            clientId,
            0
        );
        dao.addMembership(m);

        assertEquals("Monthly", dao.getCurrentMembershipType(clientId));
        assertTrue(dao.hasActiveMembership(clientId));
    }

    @Test
    void tenVisitMembershipTracksRemaining() throws Exception {
        Membership m = new Membership(
            null,
            LocalDate.now(),
            LocalDate.now().plusDays(10),
            20.0,
            Membership.MembershipType.Ten,
            clientId,
            5
        );
        dao.addMembership(m);

        Integer remaining = dao.getRemainingVisits(clientId);
        assertEquals(5, remaining);
    }

    @Test
    void returnsNullWhenNoMembership() throws Exception {
        assertNull(dao.getCurrentMembershipType(clientId));
        assertNull(dao.getRemainingVisits(clientId));
        assertFalse(dao.hasActiveMembership(clientId));
    }

    @Test
    void tenMembershipWithZeroVisitsIsNotActive() throws Exception {
        dao.addMembership(new Membership(
            null,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(10),
            20.0,
            Membership.MembershipType.Ten,
            clientId,
            0
        ));

        assertNull(dao.getCurrentMembershipType(clientId));
        assertNull(dao.getRemainingVisits(clientId));
        assertFalse(dao.hasActiveMembership(clientId));
    }

    @Test
    void getCurrentMembershipTypeChoosesLatestStartDate() throws Exception {
        dao.addMembership(new Membership(
            null,
            LocalDate.now().minusDays(10),
            LocalDate.now().plusDays(10),
            30.0,
            Membership.MembershipType.Monthly,
            clientId,
            0
        ));
        dao.addMembership(new Membership(
            null,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(5),
            15.0,
            Membership.MembershipType.Weekly,
            clientId,
            0
        ));

        assertEquals("Weekly", dao.getCurrentMembershipType(clientId));
    }

    @Test
    void removeByHolderIdDeletesMemberships() throws Exception {
        dao.addMembership(new Membership(
            null,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(30),
            49.9,
            Membership.MembershipType.Monthly,
            clientId,
            0
        ));

        assertNotNull(dao.getCurrentMembershipType(clientId));

        int removed = dao.removeByHolderId(clientId);
        assertEquals(1, removed);
        assertNull(dao.getCurrentMembershipType(clientId));
    }

    @Test
    void membershipIsNotActiveWhenStartsInFutureOrExpired() throws Exception {
        dao.addMembership(new Membership(
            null,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(10),
            10.0,
            Membership.MembershipType.Weekly,
            clientId,
            0
        ));
        assertFalse(dao.hasActiveMembership(clientId));

        dao.removeByHolderId(clientId);
        dao.addMembership(new Membership(
            null,
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            10.0,
            Membership.MembershipType.Weekly,
            clientId,
            0
        ));
        assertFalse(dao.hasActiveMembership(clientId));
    }
}
