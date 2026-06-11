package com.firstclub.membership.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Input to the checkout benefit calculation endpoint.
 */
@Data
public class CheckoutRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Cart total is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cart total must be positive")
    private BigDecimal cartTotal;

    /** Whether the order qualifies for delivery benefit */
    private boolean deliveryEligible = true;
}
