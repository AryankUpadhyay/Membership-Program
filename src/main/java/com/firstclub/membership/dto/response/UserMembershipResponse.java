package com.firstclub.membership.dto.response;

import com.firstclub.membership.model.enums.MembershipStatus;
import com.firstclub.membership.model.enums.PlanDuration;
import com.firstclub.membership.model.enums.TierLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserMembershipResponse {
    private Long id;
    private Long userId;
    private Long planId;
    private String planName;
    private PlanDuration planDuration;
    private BigDecimal planPrice;
    private TierLevel tierLevel;
    private String tierDisplayName;
    private MembershipStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long daysRemaining;
    private boolean active;
    private List<BenefitResponse> activeBenefits;
}
