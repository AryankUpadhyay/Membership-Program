package com.firstclub.membership.model;

import com.firstclub.membership.model.enums.TierLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a membership tier (Silver, Gold, Platinum).
 * Each tier has a set of configurable benefits and upgrade rules.
 * Adding a new tier requires only a new DB row — no code changes.
 */
@Entity
@Table(name = "membership_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TierLevel level;

    @Column(nullable = false, length = 50)
    private String displayName;  // e.g., "Silver Member"

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * All benefits associated with this tier (loaded eagerly for benefit resolution).
     */
    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private List<TierBenefit> benefits = new ArrayList<>();

    /**
     * Rules that must ALL be satisfied for a user to qualify for this tier.
     * Using AND semantics by default; OR semantics can be supported via ruleLogic field.
     */
    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private List<TierUpgradeRule> upgradeRules = new ArrayList<>();
}
