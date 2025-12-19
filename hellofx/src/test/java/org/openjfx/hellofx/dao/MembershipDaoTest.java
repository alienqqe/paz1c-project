package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.entities.Membership;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

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
}
