package org.openjfx.hellofx.dao;

import java.util.List;
import java.util.Optional;

import org.openjfx.hellofx.entities.DiscountRule;
import org.openjfx.hellofx.utils.Database;
import org.springframework.jdbc.core.RowMapper;

public class DiscountRuleDAO {

    private final RowMapper<DiscountRule> mapper = (rs, rowNum) -> new DiscountRule(
        rs.getLong("id"),
        rs.getInt("visits_threshold"),
        rs.getInt("discount_percent")
    );

    public List<DiscountRule> findAllOrdered() {
        return Database.jdbc().query(
            "SELECT id, visits_threshold, discount_percent FROM discount_rules ORDER BY visits_threshold ASC",
            mapper
        );
    }

    public void replaceAll(List<DiscountRule> rules) {
        Database.jdbc().update("TRUNCATE TABLE discount_rules");
        if (rules.isEmpty()) {
            return;
        }
        Database.jdbc().batchUpdate(
            "INSERT INTO discount_rules (visits_threshold, discount_percent) VALUES (?, ?)",
            rules,
            rules.size(),
            (ps, rule) -> {
                ps.setInt(1, rule.visitsThreshold());
                ps.setInt(2, rule.discountPercent());
            }
        );
    }

    // get highest avaliable discount for visit count
    public Optional<DiscountRule> bestRuleForVisits(int visitCount) {
        List<DiscountRule> match = Database.jdbc().query(
            "SELECT id, visits_threshold, discount_percent FROM discount_rules " +
                "WHERE visits_threshold <= ? ORDER BY visits_threshold DESC LIMIT 1",
            ps -> ps.setInt(1, visitCount),
            mapper
        );
        return match.stream().findFirst();
    }

}
