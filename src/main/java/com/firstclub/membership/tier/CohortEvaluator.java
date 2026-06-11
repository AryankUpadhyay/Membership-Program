package com.firstclub.membership.tier;

import com.firstclub.membership.model.TierUpgradeRule;
import com.firstclub.membership.model.UserOrderStats;
import com.firstclub.membership.model.enums.RuleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Evaluates COHORT rules.
 * A user qualifies if their cohort key matches the rule's configured cohort key.
 * Useful for VIP segments, beta groups, or manually assigned cohorts.
 */
@Slf4j
@Component
public class CohortEvaluator implements TierRuleEvaluator {

    @Override
    public RuleType getSupportedRuleType() {
        return RuleType.COHORT;
    }

    @Override
    public boolean evaluate(TierUpgradeRule rule, UserOrderStats stats) {
        String userCohort = stats.getCohortKey();
        String requiredCohort = rule.getCohortKey();
        boolean qualifies = requiredCohort != null && requiredCohort.equals(userCohort);
        log.debug("COHORT eval: userId={} userCohort={} requiredCohort={} qualifies={}",
                stats.getUserId(), userCohort, requiredCohort, qualifies);
        return qualifies;
    }
}
