package com.firstclub.membership.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Order event posted by the shopping platform when a user completes an order.
 * Triggers tier re-evaluation for the user.
 */
@Data
public class OrderEventRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Order value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Order value must be positive")
    private BigDecimal orderValue;

    /** Optional cohort update for COHORT-based tier evaluation */
    private String cohortKey;
}
