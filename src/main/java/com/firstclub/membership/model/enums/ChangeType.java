package com.firstclub.membership.model.enums;

/**
 * Type of change recorded in MembershipHistory for full audit trail.
 */
public enum ChangeType {
    SUBSCRIBED,
    UPGRADED,
    DOWNGRADED,
    CANCELLED,
    EXPIRED,
    RENEWED,
    TIER_AUTO_PROMOTED,
    TIER_AUTO_DEMOTED
}
