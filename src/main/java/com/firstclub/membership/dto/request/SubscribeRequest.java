package com.firstclub.membership.dto.request;

import com.firstclub.membership.model.enums.TierLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for subscribing to a membership plan.
 */
@Data
public class SubscribeRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotNull(message = "Tier level is required")
    private TierLevel tierLevel;
}
