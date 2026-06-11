package com.firstclub.membership.tier;

import com.firstclub.membership.model.TierUpgradeRule;
import com.firstclub.membership.model.UserOrderStats;
import com.firstclub.membership.model.enums.RuleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Evaluates ORDER_VALUE rules.
 * A user qualifies if their total monthly order value meets or exceeds the threshold.
 */
@Slf4j
@Component
public class OrderValueEvaluator implements TierRuleEvaluator {

    @Override
    public RuleType getSupportedRuleType() {
        return RuleType.ORDER_VALUE;
    }

    @Override
    public boolean evaluate(TierUpgradeRule rule, UserOrderStats stats) {
        BigDecimal value = stats.getTotalOrderValueThisMonth();
        BigDecimal threshold = rule.getThreshold();
        boolean qualifies = value.compareTo(threshold) >= 0;
        log.debug("ORDER_VALUE eval: userId={} value={} threshold={} qualifies={}",
                stats.getUserId(), value, threshold, qualifies);
        return qualifies;
    }
}
