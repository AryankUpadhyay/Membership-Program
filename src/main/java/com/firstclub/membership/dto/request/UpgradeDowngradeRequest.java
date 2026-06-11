package com.firstclub.membership.dto.request;

import com.firstclub.membership.model.enums.TierLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for manually upgrading or downgrading a membership tier.
 */
@Data
public class UpgradeDowngradeRequest {

    @NotNull(message = "Target tier level is required")
    private TierLevel targetTierLevel;

    private String reason;
}
