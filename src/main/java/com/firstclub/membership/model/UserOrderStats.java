package com.firstclub.membership.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Aggregated order statistics for a user — used by tier evaluation rules.
 *
 * Concurrency:
 * - Uses @Version for optimistic locking on order stat updates.
 * - Service uses @Lock(PESSIMISTIC_WRITE) when incrementing counts to avoid
 *   lost updates under high order throughput for the same user.
 *
 * Reset Strategy:
 * - orderCountThisMonth and totalOrderValueThisMonth are reset at the start
 *   of each calendar month by the MembershipExpiryScheduler.
 * - Lifetime stats are never reset.
 */
@Entity
@Table(name = "user_order_stats",
        indexes = @Index(name = "idx_order_stats_user_id", columnList = "user_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    /** User's cohort / segment for COHORT-based tier rules */
    @Column(length = 100)
    private String cohortKey;

    /** Orders placed in the current evaluation window (this month) */
    @Column(nullable = false)
    @Builder.Default
    private int orderCountThisMonth = 0;

    /** Total order value in the current evaluation window (this month) */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalOrderValueThisMonth = BigDecimal.ZERO;

    /** All-time order count */
    @Column(nullable = false)
    @Builder.Default
    private int lifetimeOrderCount = 0;

    /** All-time total order value */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal lifetimeTotalOrderValue = BigDecimal.ZERO;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @Version
    private Long version;
}
