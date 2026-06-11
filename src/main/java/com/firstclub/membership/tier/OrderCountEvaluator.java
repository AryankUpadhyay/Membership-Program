package com.firstclub.membership.tier;

import com.firstclub.membership.model.TierUpgradeRule;
import com.firstclub.membership.model.UserOrderStats;
import com.firstclub.membership.model.enums.RuleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Evaluates ORDER_COUNT rules.
 * A user qualifies if their monthly order count meets or exceeds the threshold.
 */
@Slf4j
@Component
public class OrderCountEvaluator implements TierRuleEvaluator {

    @Override
    public RuleType getSupportedRuleType() {
        return RuleType.ORDER_COUNT;
    }

    @Override
    public boolean evaluate(TierUpgradeRule rule, UserOrderStats stats) {
        int count = stats.getOrderCountThisMonth();
        int threshold = rule.getThreshold().intValue();
        boolean qualifies = count >= threshold;
        log.debug("ORDER_COUNT eval: userId={} count={} threshold={} qualifies={}",
                stats.getUserId(), count, threshold, qualifies);
        return qualifies;
    }
}
