package com.firstclub.membership.model.enums;

/**
 * Criteria type used by TierUpgradeRule to evaluate if a user qualifies for a tier.
 */
public enum RuleType {
    ORDER_COUNT,      // Minimum number of orders in the evaluation window
    ORDER_VALUE,      // Minimum total order value in the evaluation window
    COHORT            // User belongs to a specific cohort / segment
}
