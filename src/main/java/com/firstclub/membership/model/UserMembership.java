package com.firstclub.membership.model;

import com.firstclub.membership.model.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * The active subscription record for a user.
 *
 * Concurrency safety:
 * - @Version enables optimistic locking — concurrent updates will throw
 *   ObjectOptimisticLockingFailureException, preventing lost updates.
 * - Service layer catches this and retries or surfaces a 409 Conflict.
 */
@Entity
@Table(name = "user_memberships",
        indexes = {
                @Index(name = "idx_user_membership_user_id", columnList = "user_id"),
                @Index(name = "idx_user_membership_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * External user identifier (from the shopping platform's user service).
     * Not a FK — we don't own user data.
     */
    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Optimistic lock version — prevents concurrent subscription mutations
     * from corrupting membership state.
     */
    @Version
    private Long version;

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE && LocalDateTime.now().isBefore(endDate);
    }
}
