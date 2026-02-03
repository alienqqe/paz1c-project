package org.openjfx.hellofx.dao;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.openjfx.hellofx.TestContainers;
import org.openjfx.hellofx.entities.DiscountRule;

class DiscountRuleDaoTest extends TestContainers {

    private final DiscountRuleDAO dao = new DiscountRuleDAO();

    @Test
    void replaceAllAndFindOrdered() {
        List<DiscountRule> rules = List.of(
            new DiscountRule(0L, 5, 10),
            new DiscountRule(0L, 10, 20)
        );
        dao.replaceAll(rules);

        List<DiscountRule> stored = dao.findAllOrdered();
        assertEquals(2, stored.size());
        assertEquals(5, stored.get(0).visitsThreshold());
        assertEquals(10, stored.get(1).visitsThreshold());

        Optional<DiscountRule> best = dao.bestRuleForVisits(9);
        assertTrue(best.isPresent());
        assertEquals(10, best.get().discountPercent());
    }

    @Test
    void bestRuleForVisitsEmptyWhenNoRuleMatches() {
        dao.replaceAll(List.of(new DiscountRule(0L, 5, 10)));

        assertTrue(dao.bestRuleForVisits(0).isEmpty());
    }

    @Test
    void replaceAllWithEmptyClearsTable() {
        dao.replaceAll(List.of(new DiscountRule(0L, 5, 10)));
        assertFalse(dao.findAllOrdered().isEmpty());

        dao.replaceAll(List.of());
        assertTrue(dao.findAllOrdered().isEmpty());
    }
}
