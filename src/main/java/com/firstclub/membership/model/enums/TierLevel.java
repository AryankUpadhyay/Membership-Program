package com.firstclub.membership.model.enums;

/**
 * Tier levels, ordered by rank (higher ordinal = higher tier).
 * Used for upgrade/downgrade comparisons.
 */
public enum TierLevel {
    SILVER,
    GOLD,
    PLATINUM;

    public boolean isHigherThan(TierLevel other) {
        return this.ordinal() > other.ordinal();
    }

    public boolean isLowerThan(TierLevel other) {
        return this.ordinal() < other.ordinal();
    }
}
