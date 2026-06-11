package com.firstclub.membership.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderStatsResponse {
    private Long userId;
    private String cohortKey;
    private int orderCountThisMonth;
    private BigDecimal totalOrderValueThisMonth;
    private int lifetimeOrderCount;
    private BigDecimal lifetimeTotalOrderValue;
}
