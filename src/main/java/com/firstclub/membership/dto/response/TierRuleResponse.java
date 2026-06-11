package com.firstclub.membership.dto.response;

import com.firstclub.membership.model.enums.RuleType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TierRuleResponse {
    private RuleType ruleType;
    private BigDecimal threshold;
    private int evaluationWindowDays;
    private String cohortKey;
    private String description;
}
