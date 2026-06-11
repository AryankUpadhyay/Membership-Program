package com.firstclub.membership.dto.response;

import com.firstclub.membership.model.enums.ChangeType;
import com.firstclub.membership.model.enums.MembershipStatus;
import com.firstclub.membership.model.enums.TierLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MembershipHistoryResponse {
    private Long id;
    private Long userId;
    private Long membershipId;
    private ChangeType changeType;
    private TierLevel previousTier;
    private TierLevel newTier;
    private MembershipStatus previousStatus;
    private MembershipStatus newStatus;
    private String reason;
    private LocalDateTime changedAt;
}
