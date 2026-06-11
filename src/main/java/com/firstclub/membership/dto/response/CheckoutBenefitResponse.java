package com.firstclub.membership.dto.response;

import com.firstclub.membership.model.enums.TierLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Checkout benefit computation result.
 * Returned by POST /api/v1/benefits/checkout to inform the shopping cart
 * how much discount and delivery cost apply for a member.
 */
@Data
@Builder
public class CheckoutBenefitResponse {
    private Long userId;
    private TierLevel tierLevel;
    private BigDecimal originalTotal;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal finalTotal;
    private boolean freeDeliveryApplied;
    private BigDecimal deliverySaving;
    private boolean hasMembership;
    private List<String> appliedBenefits;
}
