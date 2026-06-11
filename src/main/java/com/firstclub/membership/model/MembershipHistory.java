package com.firstclub.membership.model;

import com.firstclub.membership.model.enums.ChangeType;
import com.firstclub.membership.model.enums.MembershipStatus;
import com.firstclub.membership.model.enums.TierLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable audit log of every state change to a user's membership.
 * Enables full history view and debugging of tier transitions.
 * Records are append-only — never updated after insert.
 */
@Entity
@Table(name = "membership_history",
        indexes = @Index(name = "idx_history_user_id", columnList = "userId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long membershipId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeType changeType;

    @Enumerated(EnumType.STRING)
    private TierLevel previousTier;

    @Enumerated(EnumType.STRING)
    private TierLevel newTier;

    @Enumerated(EnumType.STRING)
    private MembershipStatus previousStatus;

    @Enumerated(EnumType.STRING)
    private MembershipStatus newStatus;

    @Column(length = 500)
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;
}
