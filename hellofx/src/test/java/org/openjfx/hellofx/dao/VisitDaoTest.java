package org.openjfx.hellofx.dao;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.TestContainers;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.entities.Membership;
import org.openjfx.hellofx.model.VisitRow;
import org.openjfx.hellofx.utils.Database;

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

        List<VisitRow> visits = dao.getRecentVisits(5);
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

    @Test
    void checkInFailsWhenNoActiveMembership() throws Exception {
        clientDao.addClient(new Client(null, "NoMember", "nomember@example.org", "000"));
        Long noMemberId = clientDao.searchClients("nomember@example.org").get(0).id();

        assertFalse(dao.checkInClient(noMemberId));
        assertEquals(0, dao.countVisitsForClient(noMemberId));
    }

    @Test
    void checkInWithMonthlyMembershipSucceedsWithoutDecrementingVisits() throws Exception {
        clientDao.addClient(new Client(null, "Monthly", "monthly@example.org", "123"));
        Long monthlyId = clientDao.searchClients("monthly@example.org").get(0).id();

        membershipDao.addMembership(new Membership(
            null,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(30),
            40.0,
            Membership.MembershipType.Monthly,
            monthlyId,
            0
        ));

        assertTrue(dao.checkInClient(monthlyId));
        assertEquals(1, dao.countVisitsForClient(monthlyId));
        assertNull(membershipDao.getRemainingVisits(monthlyId));
    }

    @Test
    void recentVisitsForClientFiltersAndRespectsLimit() throws Exception {
        clientDao.addClient(new Client(null, "Alice", "alice.visits@example.org", "111"));
        Long aliceId = clientDao.searchClients("alice.visits@example.org").get(0).id();
        clientDao.addClient(new Client(null, "Bob", "bob.visits@example.org", "222"));
        Long bobId = clientDao.searchClients("bob.visits@example.org").get(0).id();

        Database.jdbc().update(
            "INSERT INTO visits (client_id, membership_id, check_in) VALUES (?, NULL, ?)",
            aliceId, Timestamp.valueOf(LocalDateTime.of(2025, 1, 1, 10, 0))
        );
        Database.jdbc().update(
            "INSERT INTO visits (client_id, membership_id, check_in) VALUES (?, NULL, ?)",
            aliceId, Timestamp.valueOf(LocalDateTime.of(2025, 1, 2, 10, 0))
        );
        Database.jdbc().update(
            "INSERT INTO visits (client_id, membership_id, check_in) VALUES (?, NULL, ?)",
            bobId, Timestamp.valueOf(LocalDateTime.of(2025, 1, 3, 10, 0))
        );

        List<VisitRow> got = dao.getRecentVisitsForClient("alice.visits", 1);
        assertEquals(1, got.size());
        assertEquals("Alice", got.get(0).clientName());

        List<VisitRow> global = dao.getRecentVisits(2);
        assertEquals(2, global.size());
        assertEquals("Bob", global.get(0).clientName());
    }
}
