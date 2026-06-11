package com.firstclub.membership.service;

import com.firstclub.membership.dto.response.BenefitResponse;
import com.firstclub.membership.dto.response.CheckoutBenefitResponse;
import com.firstclub.membership.model.TierBenefit;
import com.firstclub.membership.model.UserMembership;
import com.firstclub.membership.model.enums.BenefitType;
import com.firstclub.membership.model.enums.MembershipStatus;
import com.firstclub.membership.repository.TierBenefitRepository;
import com.firstclub.membership.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Benefit Engine — resolves and applies membership benefits.
 *
 * Implements Chain of Responsibility for benefit application:
 * benefits are applied in priority order (discount first, then delivery).
 *
 * This service is the integration point for the checkout pipeline.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BenefitEngine {

    private static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("49.00");

    private final UserMembershipRepository membershipRepository;
    private final TierBenefitRepository benefitRepository;

    /**
     * Returns the list of active benefits for a user's current tier.
     */
    public List<BenefitResponse> getUserBenefits(Long userId) {
        Optional<UserMembership> membershipOpt = membershipRepository
                .findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);

        if (membershipOpt.isEmpty() || !membershipOpt.get().isActive()) {
            return List.of();
        }

        UserMembership membership = membershipOpt.get();
        return benefitRepository.findActiveByTierLevel(membership.getTier().getLevel())
                .stream()
                .map(this::toBenefitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Computes checkout benefits for a given cart total.
     * Returns discount amounts, free delivery status, and final payable amount.
     *
     * Chain of responsibility:
     * 1. DISCOUNT_PERCENTAGE applied to cart total
     * 2. FREE_DELIVERY applied to delivery charge
     */
    public CheckoutBenefitResponse computeCheckoutBenefits(Long userId,
                                                            BigDecimal cartTotal,
                                                            boolean deliveryEligible) {
        Optional<UserMembership> membershipOpt = membershipRepository
                .findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);

        if (membershipOpt.isEmpty() || !membershipOpt.get().isActive()) {
            return CheckoutBenefitResponse.builder()
                    .userId(userId)
                    .originalTotal(cartTotal)
                    .discountAmount(BigDecimal.ZERO)
                    .discountPercentage(BigDecimal.ZERO)
                    .finalTotal(cartTotal)
                    .freeDeliveryApplied(false)
                    .deliverySaving(BigDecimal.ZERO)
                    .hasMembership(false)
                    .appliedBenefits(List.of())
                    .build();
        }

        UserMembership membership = membershipOpt.get();
        List<TierBenefit> benefits = benefitRepository.findActiveByTierLevel(
                membership.getTier().getLevel());

        BigDecimal discountPercentage = BigDecimal.ZERO;
        boolean hasFreeDelivery = false;
        List<String> appliedBenefits = new ArrayList<>();

        // Chain: evaluate each benefit in sequence
        for (TierBenefit benefit : benefits) {
            switch (benefit.getBenefitType()) {
                case DISCOUNT_PERCENTAGE -> {
                    discountPercentage = benefit.getValue();
                    appliedBenefits.add(benefit.getValue() + "% discount on cart");
                }
                case FREE_DELIVERY -> {
                    hasFreeDelivery = deliveryEligible;
                    if (deliveryEligible) appliedBenefits.add("Free delivery");
                }
                case EXCLUSIVE_DEALS_ACCESS -> appliedBenefits.add("Exclusive deals access");
                case EARLY_SALE_ACCESS -> appliedBenefits.add("Early sale access");
                case PRIORITY_SUPPORT -> appliedBenefits.add("Priority support");
                case EXCLUSIVE_COUPONS ->
                        appliedBenefits.add(benefit.getValue().intValue() + " exclusive coupons/month");
            }
        }

        BigDecimal discountAmount = cartTotal
                .multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal deliverySaving = hasFreeDelivery ? STANDARD_DELIVERY_FEE : BigDecimal.ZERO;
        BigDecimal finalTotal = cartTotal.subtract(discountAmount);

        log.info("Checkout benefit computed: userId={} tier={} discount={}% saving={} freeDelivery={}",
                userId, membership.getTier().getLevel(), discountPercentage, discountAmount, hasFreeDelivery);

        return CheckoutBenefitResponse.builder()
                .userId(userId)
                .tierLevel(membership.getTier().getLevel())
                .originalTotal(cartTotal)
                .discountPercentage(discountPercentage)
                .discountAmount(discountAmount)
                .finalTotal(finalTotal)
                .freeDeliveryApplied(hasFreeDelivery)
                .deliverySaving(deliverySaving)
                .hasMembership(true)
                .appliedBenefits(appliedBenefits)
                .build();
    }

    private BenefitResponse toBenefitResponse(TierBenefit b) {
        return BenefitResponse.builder()
                .id(b.getId())
                .benefitType(b.getBenefitType())
                .value(b.getValue())
                .description(b.getDescription())
                .build();
    }
}
