package org.openjfx.hellofx.services;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.DiscountRuleDAO;
import org.openjfx.hellofx.entities.DiscountRule;

class DiscountRuleServiceTest {

    @Test
    void bestRuleForVisitsDelegatesToDao() {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            DiscountRuleDAO dao = mock(DiscountRuleDAO.class);
            mocked.when(DaoFactory::discountRules).thenReturn(dao);
            DiscountRule rule = new DiscountRule(1L, 10, 5);
            when(dao.bestRuleForVisits(12)).thenReturn(Optional.of(rule));

            DiscountRuleService service = new DiscountRuleService();
            Optional<DiscountRule> result = service.bestRuleForVisits(12);

            assertSame(rule, result.orElseThrow());
            verify(dao).bestRuleForVisits(12);
        }
    }

    @Test
    void findAllOrderedDelegatesToDao() {
        try (MockedStatic<DaoFactory> mocked = Mockito.mockStatic(DaoFactory.class)) {
            DiscountRuleDAO dao = mock(DiscountRuleDAO.class);
            mocked.when(DaoFactory::discountRules).thenReturn(dao);
            List<DiscountRule> rules = List.of(new DiscountRule(1L, 5, 2));
            when(dao.findAllOrdered()).thenReturn(rules);

            DiscountRuleService service = new DiscountRuleService();
            List<DiscountRule> result = service.findAllOrdered();

            assertSame(rules, result);
            verify(dao).findAllOrdered();
        }
    }

}
