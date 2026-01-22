package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.DiscountRuleDAO;
import org.openjfx.hellofx.entities.DiscountRule;

public class DiscountRuleService {
    private final DiscountRuleDAO discountRuleDAO = DaoFactory.discountRules();

    public List<DiscountRule> findAllOrdered() {
        return discountRuleDAO.findAllOrdered();
    }

    public void replaceAll(List<DiscountRule> rules) {
        discountRuleDAO.replaceAll(rules);
    }

    public Optional<DiscountRule> bestRuleForVisits(int visitCount) {
        return discountRuleDAO.bestRuleForVisits(visitCount);
    }
}
