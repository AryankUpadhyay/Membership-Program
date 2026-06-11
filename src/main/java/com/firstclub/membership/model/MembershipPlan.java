package com.firstclub.membership.model;

import com.firstclub.membership.model.enums.PlanDuration;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a purchasable membership plan (e.g., Monthly ₹99, Quarterly ₹249, Yearly ₹799).
 * Plans define duration and pricing; tier is chosen separately at subscribe time.
 */
@Entity
@Table(name = "membership_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;  // e.g., "Monthly", "Quarterly", "Yearly"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanDuration duration;

    @Column(nullable = false)
    private int durationDays;  // 30, 90, 365

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
