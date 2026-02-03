package org.openjfx.hellofx.dao;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.TestContainers;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.entities.Membership;
import org.openjfx.hellofx.model.ClientWithMembershipStatus;

class ClientDAOTest extends TestContainers {

    private final ClientDAO dao = new ClientDAO();
    private final MembershipDAO membershipDao = new MembershipDAO();

    @Test
    void addClientPersistsAndSearches() throws Exception {
        dao.addClient(new Client(null, "Alice", "alice@example.com", "123"));

        List<Client> found = dao.searchClients("alice");
        assertEquals(1, found.size());
        Client c = found.get(0);
        assertNotNull(c.id());
        assertEquals("Alice", c.name());
        assertEquals("alice@example.com", c.email());
        assertEquals("123", c.phoneNumber());
    }

    @Test
    void searchClientsEmptyWhenNoRows() throws Exception {
        List<Client> found = dao.searchClients("missing");
        assertTrue(found.isEmpty());
    }

    @Test
    void searchClientsFindsByNameOrEmailCaseInsensitive() throws Exception {
        dao.addClient(new Client(null, "Bob Marley", "bob.marley@example.org", "555"));

        assertEquals(1, dao.searchClients("bob").size());
        assertEquals(1, dao.searchClients("MARLEY").size());
        assertEquals(1, dao.searchClients("EXAMPLE.ORG").size());
    }

    @Test
    void searchClientsWithStatusReturnsMembershipTypeWhenActive() throws Exception {
        dao.addClient(new Client(null, "Zoe", "zoe@example.org", "001"));
        Long zoeId = dao.searchClients("zoe@example.org").get(0).id();

        // Ten membership should expose remaining visits.
        membershipDao.addMembership(new Membership(
            null,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(10),
            20.0,
            Membership.MembershipType.Ten,
            zoeId,
            3
        ));

        List<ClientWithMembershipStatus> got = dao.searchClientsWithStatus("zoe@example.org");
        assertEquals(1, got.size());
        assertEquals("Zoe", got.get(0).name());
        assertEquals("Ten", got.get(0).membershipType());
        assertEquals(3, got.get(0).remainingVisits());
    }

    @Test
    void searchClientsWithStatusReturnsNullMembershipWhenMissingOrInactive() throws Exception {
        dao.addClient(new Client(null, "No Membership", "nomem@example.org", "002"));

        List<ClientWithMembershipStatus> noMem = dao.searchClientsWithStatus("nomem@example.org");
        assertEquals(1, noMem.size());
        assertNull(noMem.get(0).membershipType());
        assertNull(noMem.get(0).remainingVisits());

        dao.addClient(new Client(null, "Zero Ten", "zero.ten@example.org", "003"));
        Long zeroId = dao.searchClients("zero.ten@example.org").get(0).id();
        membershipDao.addMembership(new Membership(
            null,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(10),
            20.0,
            Membership.MembershipType.Ten,
            zeroId,
            0
        ));

        List<ClientWithMembershipStatus> zeroTen = dao.searchClientsWithStatus("zero.ten@example.org");
        assertEquals(1, zeroTen.size());
        assertNull(zeroTen.get(0).membershipType());
        assertNull(zeroTen.get(0).remainingVisits());
    }
}
