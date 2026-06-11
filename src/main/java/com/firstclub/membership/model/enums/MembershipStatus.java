package com.firstclub.membership.model.enums;

/**
 * Lifecycle states of a user's membership subscription.
 */
public enum MembershipStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED,
    PENDING  // Reserved for async payment flows
}
