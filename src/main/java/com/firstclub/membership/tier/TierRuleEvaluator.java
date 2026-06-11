package com.firstclub.membership.tier;

import com.firstclub.membership.model.TierUpgradeRule;
import com.firstclub.membership.model.UserOrderStats;
import com.firstclub.membership.model.enums.RuleType;

/**
 * Strategy interface for tier upgrade rule evaluation.
 *
 * Each implementation handles a specific RuleType.
 * New criteria can be added by implementing this interface and registering
 * the new bean — no changes to TierEvaluationService required.
 *
 * Implementations are Spring beans and auto-discovered via @Component.
 */
public interface TierRuleEvaluator {

    /**
     * The RuleType this evaluator handles.
     */
    RuleType getSupportedRuleType();

    /**
     * Evaluates whether the given user stats satisfy the given rule.
     *
     * @param rule  The rule to evaluate
     * @param stats The user's current order statistics
     * @return true if the user meets the rule's threshold
     */
    boolean evaluate(TierUpgradeRule rule, UserOrderStats stats);
}
