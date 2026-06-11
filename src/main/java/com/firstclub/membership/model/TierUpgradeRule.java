package com.firstclub.membership.model;

import com.firstclub.membership.model.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * A configurable rule that determines if a user qualifies for a specific tier.
 *
 * Rule evaluation is handled by the Strategy pattern (TierRuleEvaluator implementations).
 * Multiple rules per tier are ANDed together — a user must satisfy all rules for the tier.
 *
 * Example rules:
 * - Silver: ORDER_COUNT >= 3 (last 30 days)
 * - Gold: ORDER_COUNT >= 7, ORDER_VALUE >= 3000 (last 30 days)
 * - Platinum: ORDER_COUNT >= 15, ORDER_VALUE >= 7500 (last 30 days)
 */
@Entity
@Table(name = "tier_upgrade_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierUpgradeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    /**
     * Minimum threshold value for ORDER_COUNT or ORDER_VALUE rules.
     */
    @Column(name = "rule_threshold", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal threshold = BigDecimal.ZERO;

    /**
     * Evaluation window in days (default 30).
     * For COHORT rules this field is unused.
     */
    @Column(nullable = false)
    @Builder.Default
    private int evaluationWindowDays = 30;

    /**
     * For COHORT rules: the cohort identifier (e.g., "PREMIUM_BETA", "VIP_2025").
     */
    @Column(length = 100)
    private String cohortKey;

    @Column(length = 255)
    private String description;
}
