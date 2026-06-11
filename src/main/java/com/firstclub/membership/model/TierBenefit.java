package com.firstclub.membership.model;

import com.firstclub.membership.model.enums.BenefitType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * A single configurable benefit attached to a membership tier.
 *
 * Design: benefits are polymorphic by BenefitType.
 * - FREE_DELIVERY: no value needed (value = 0)
 * - DISCOUNT_PERCENTAGE: value = discount % (e.g., 10.0 for 10%)
 * - EXCLUSIVE_DEALS_ACCESS, EARLY_SALE_ACCESS, PRIORITY_SUPPORT: boolean flags (value = 1)
 * - EXCLUSIVE_COUPONS: value = number of monthly coupons
 *
 * Adding a new benefit type: add the enum value + a new row in DB. Zero code changes.
 */
@Entity
@Table(name = "tier_benefits",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tier_id", "benefit_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type", nullable = false)
    private BenefitType benefitType;

    /**
     * Numeric value associated with the benefit (context-dependent by type).
     * For boolean benefits (FREE_DELIVERY, PRIORITY_SUPPORT) use 1.0.
     * Named 'benefit_value' to avoid SQL reserved word conflict with 'value'.
     */
    @Column(name = "benefit_value", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal value = BigDecimal.ZERO;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
