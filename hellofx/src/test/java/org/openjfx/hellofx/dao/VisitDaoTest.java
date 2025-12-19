package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.entities.Membership;
import org.openjfx.hellofx.dao.VisitDAO.VisitView;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisitDaoTest extends TestContainers {

    private final VisitDAO dao = new VisitDAO();
    private final ClientDAO clientDao = new ClientDAO();
    private final MembershipDAO membershipDao = new MembershipDAO();
    private Long clientId;

    @BeforeEach
    void seedClientAndMembership() throws Exception {
        clientDao.addClient(new Client(null, "Visitor", "visitor@mail.com", "111"));
        clientId = clientDao.searchClients("visitor").get(0).id();
        Membership m = new Membership(
            null,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(5),
            15.0,
            Membership.MembershipType.Ten,
            clientId,
            2
        );
        membershipDao.addMembership(m);
    }

    @Test
    void checkInConsumesVisitAndLogsHistory() throws Exception {
        assertTrue(dao.checkInClient(clientId));

        List<VisitView> visits = dao.getRecentVisits(5);
        assertFalse(visits.isEmpty());
        assertEquals("Visitor", visits.get(0).clientName());

        Integer remaining = membershipDao.getRemainingVisits(clientId);
        assertEquals(1, remaining);
        assertEquals(1, dao.countVisitsForClient(clientId));
    }

    @Test
    void checkInFailsWhenNoVisitsLeft() throws Exception {
        // consume two visits
        assertTrue(dao.checkInClient(clientId));
        assertTrue(dao.checkInClient(clientId));
        // third should fail
        assertFalse(dao.checkInClient(clientId));
    }
}
