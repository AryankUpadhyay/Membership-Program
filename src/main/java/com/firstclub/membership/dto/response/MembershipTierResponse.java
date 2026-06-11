package com.firstclub.membership.dto.response;

import com.firstclub.membership.model.enums.TierLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MembershipTierResponse {
    private Long id;
    private TierLevel level;
    private String displayName;
    private String description;
    private List<BenefitResponse> benefits;
    private List<TierRuleResponse> upgradeRules;
}
