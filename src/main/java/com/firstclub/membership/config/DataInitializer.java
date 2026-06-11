package com.firstclub.membership.config;

import com.firstclub.membership.model.*;
import com.firstclub.membership.model.enums.*;
import com.firstclub.membership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with initial plans, tiers, benefits, and upgrade rules on startup.
 *
 * All business configuration lives here (or in DB) — zero hardcoding in service layer.
 * To reconfigure: update this seeder or modify records directly in H2 console.
 *
 * Plans:    Monthly ₹99 / Quarterly ₹249 / Yearly ₹799
 * Tiers:    Silver / Gold / Platinum with escalating benefits and thresholds
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final TierBenefitRepository benefitRepository;
    private final TierUpgradeRuleRepository ruleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (planRepository.count() > 0) {
            log.info("Database already seeded, skipping initialization.");
            return;
        }

        seedPlans();
        seedTiers();
        log.info("✅ Database seeded successfully: 3 plans, 3 tiers with benefits and rules.");
    }

    // ── Plans ──────────────────────────────────────────────────────────────────

    private void seedPlans() {
        planRepository.saveAll(List.of(
                MembershipPlan.builder()
                        .name("Monthly")
                        .duration(PlanDuration.MONTHLY)
                        .durationDays(30)
                        .price(new BigDecimal("99.00"))
                        .description("Monthly membership — perfect for trying out FirstClub benefits.")
                        .active(true)
                        .build(),

                MembershipPlan.builder()
                        .name("Quarterly")
                        .duration(PlanDuration.QUARTERLY)
                        .durationDays(90)
                        .price(new BigDecimal("249.00"))
                        .description("Quarterly membership — great value for regular shoppers. Save 16% vs monthly.")
                        .active(true)
                        .build(),

                MembershipPlan.builder()
                        .name("Yearly")
                        .duration(PlanDuration.YEARLY)
                        .durationDays(365)
                        .price(new BigDecimal("799.00"))
                        .description("Annual membership — best value. Save 33% vs monthly. " +
                                     "Includes full year of tier benefits.")
                        .active(true)
                        .build()
        ));
        log.info("Seeded 3 membership plans.");
    }

    // ── Tiers ──────────────────────────────────────────────────────────────────

    private void seedTiers() {
        MembershipTier silver = tierRepository.save(
                MembershipTier.builder()
                        .level(TierLevel.SILVER)
                        .displayName("Silver Member")
                        .description("Entry-level tier with essential shopping benefits.")
                        .active(true)
                        .build());

        MembershipTier gold = tierRepository.save(
                MembershipTier.builder()
                        .level(TierLevel.GOLD)
                        .displayName("Gold Member")
                        .description("Enhanced benefits for frequent FirstClub shoppers.")
                        .active(true)
                        .build());

        MembershipTier platinum = tierRepository.save(
                MembershipTier.builder()
                        .level(TierLevel.PLATINUM)
                        .displayName("Platinum Member")
                        .description("Top-tier benefits for FirstClub's most loyal members.")
                        .active(true)
                        .build());

        seedSilverBenefitsAndRules(silver);
        seedGoldBenefitsAndRules(gold);
        seedPlatinumBenefitsAndRules(platinum);

        log.info("Seeded 3 membership tiers: SILVER, GOLD, PLATINUM.");
    }

    private void seedSilverBenefitsAndRules(MembershipTier tier) {
        benefitRepository.saveAll(List.of(
                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.FREE_DELIVERY)
                        .value(BigDecimal.ONE)
                        .description("Free delivery on all eligible orders")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.DISCOUNT_PERCENTAGE)
                        .value(new BigDecimal("5.00"))
                        .description("5% extra discount on selected items and categories")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.EXCLUSIVE_DEALS_ACCESS)
                        .value(BigDecimal.ONE)
                        .description("Access to exclusive Silver member deals")
                        .enabled(true).build()
        ));

        ruleRepository.saveAll(List.of(
                TierUpgradeRule.builder().tier(tier)
                        .ruleType(RuleType.ORDER_COUNT)
                        .threshold(new BigDecimal("3"))
                        .evaluationWindowDays(30)
                        .description("Minimum 3 orders in the last 30 days")
                        .build()
        ));
    }

    private void seedGoldBenefitsAndRules(MembershipTier tier) {
        benefitRepository.saveAll(List.of(
                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.FREE_DELIVERY)
                        .value(BigDecimal.ONE)
                        .description("Free delivery on all eligible orders")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.DISCOUNT_PERCENTAGE)
                        .value(new BigDecimal("10.00"))
                        .description("10% extra discount on selected items and categories")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.EXCLUSIVE_DEALS_ACCESS)
                        .value(BigDecimal.ONE)
                        .description("Access to exclusive Gold member deals")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.EARLY_SALE_ACCESS)
                        .value(BigDecimal.ONE)
                        .description("24-hour early access to all sales and flash deals")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.EXCLUSIVE_COUPONS)
                        .value(new BigDecimal("2"))
                        .description("2 exclusive Gold coupons per month")
                        .enabled(true).build()
        ));

        ruleRepository.saveAll(List.of(
                TierUpgradeRule.builder().tier(tier)
                        .ruleType(RuleType.ORDER_COUNT)
                        .threshold(new BigDecimal("7"))
                        .evaluationWindowDays(30)
                        .description("Minimum 7 orders in the last 30 days")
                        .build(),

                TierUpgradeRule.builder().tier(tier)
                        .ruleType(RuleType.ORDER_VALUE)
                        .threshold(new BigDecimal("3000.00"))
                        .evaluationWindowDays(30)
                        .description("Minimum ₹3,000 total order value in the last 30 days")
                        .build()
        ));
    }

    private void seedPlatinumBenefitsAndRules(MembershipTier tier) {
        benefitRepository.saveAll(List.of(
                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.FREE_DELIVERY)
                        .value(BigDecimal.ONE)
                        .description("Free delivery on all eligible orders")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.DISCOUNT_PERCENTAGE)
                        .value(new BigDecimal("15.00"))
                        .description("15% extra discount on selected items and categories")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.EXCLUSIVE_DEALS_ACCESS)
                        .value(BigDecimal.ONE)
                        .description("Access to exclusive Platinum member deals")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.EARLY_SALE_ACCESS)
                        .value(BigDecimal.ONE)
                        .description("48-hour early access to all sales and flash deals")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.PRIORITY_SUPPORT)
                        .value(BigDecimal.ONE)
                        .description("Dedicated priority customer support with < 2hr response")
                        .enabled(true).build(),

                TierBenefit.builder().tier(tier)
                        .benefitType(BenefitType.EXCLUSIVE_COUPONS)
                        .value(new BigDecimal("5"))
                        .description("5 exclusive Platinum coupons per month")
                        .enabled(true).build()
        ));

        ruleRepository.saveAll(List.of(
                TierUpgradeRule.builder().tier(tier)
                        .ruleType(RuleType.ORDER_COUNT)
                        .threshold(new BigDecimal("15"))
                        .evaluationWindowDays(30)
                        .description("Minimum 15 orders in the last 30 days")
                        .build(),

                TierUpgradeRule.builder().tier(tier)
                        .ruleType(RuleType.ORDER_VALUE)
                        .threshold(new BigDecimal("7500.00"))
                        .evaluationWindowDays(30)
                        .description("Minimum ₹7,500 total order value in the last 30 days")
                        .build()
        ));
    }
}
