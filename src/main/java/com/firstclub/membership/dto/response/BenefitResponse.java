package com.firstclub.membership.dto.response;

import com.firstclub.membership.model.enums.BenefitType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BenefitResponse {
    private Long id;
    private BenefitType benefitType;
    private BigDecimal value;
    private String description;
}
