package com.firstclub.membership.dto.response;

import com.firstclub.membership.model.enums.PlanDuration;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MembershipPlanResponse {
    private Long id;
    private String name;
    private PlanDuration duration;
    private int durationDays;
    private BigDecimal price;
    private String description;
    private List<MembershipTierResponse> availableTiers;
}
