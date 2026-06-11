package com.firstclub.membership.service;

import com.firstclub.membership.dto.request.SubscribeRequest;
import com.firstclub.membership.dto.request.UpgradeDowngradeRequest;
import com.firstclub.membership.dto.response.BenefitResponse;
import com.firstclub.membership.dto.response.MembershipHistoryResponse;
import com.firstclub.membership.dto.response.UserMembershipResponse;
import com.firstclub.membership.exception.DuplicateMembershipException;
import com.firstclub.membership.exception.InvalidOperationException;
import com.firstclub.membership.exception.MembershipNotFoundException;
import com.firstclub.membership.model.*;
import com.firstclub.membership.model.enums.ChangeType;
import com.firstclub.membership.model.enums.MembershipStatus;
import com.firstclub.membership.model.enums.TierLevel;
import com.firstclub.membership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core subscription lifecycle service.
 *
 * Concurrency:
 * - Subscribe uses REPEATABLE_READ isolation + PESSIMISTIC_WRITE lock to
 *   prevent duplicate active memberships for the same user.
 * - Upgrade/downgrade uses @Version optimistic locking on UserMembership.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserMembershipRepository membershipRepository;
    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final MembershipHistoryRepository historyRepository;
    private final BenefitEngine benefitEngine;

    /**
     * Subscribes a user to a plan + tier combination.
     *
     * Guards:
     * 1. Prevents duplicate active memberships (PESSIMISTIC_WRITE check).
     * 2. Validates plan and tier exist.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public UserMembershipResponse subscribe(SubscribeRequest request) {
        // Pessimistic lock to prevent race on concurrent subscribe calls for same user
        if (membershipRepository.findActiveByUserIdForUpdate(request.getUserId()).isPresent()) {
            throw new DuplicateMembershipException(
                    "User " + request.getUserId() + " already has an active membership. " +
                    "Cancel the existing one before subscribing to a new plan.");
        }

        MembershipPlan plan = planRepository.findById(request.getPlanId())
                .filter(MembershipPlan::isActive)
                .orElseThrow(() -> new MembershipNotFoundException(
                        "Active plan not found with id: " + request.getPlanId()));

        MembershipTier tier = tierRepository.findByLevel(request.getTierLevel())
                .filter(MembershipTier::isActive)
                .orElseThrow(() -> new MembershipNotFoundException(
                        "Active tier not found: " + request.getTierLevel()));

        LocalDateTime now = LocalDateTime.now();
        UserMembership membership = UserMembership.builder()
                .userId(request.getUserId())
                .plan(plan)
                .tier(tier)
                .status(MembershipStatus.ACTIVE)
                .startDate(now)
                .endDate(now.plusDays(plan.getDurationDays()))
                .build();

        membership = membershipRepository.save(membership);

        historyRepository.save(MembershipHistory.builder()
                .userId(request.getUserId())
                .membershipId(membership.getId())
                .changeType(ChangeType.SUBSCRIBED)
                .newTier(tier.getLevel())
                .newStatus(MembershipStatus.ACTIVE)
                .reason("New subscription")
                .build());

        log.info("User {} subscribed to plan={} tier={} until {}",
                request.getUserId(), plan.getName(), tier.getLevel(), membership.getEndDate());

        return toResponse(membership);
    }

    /**
     * Returns the user's active membership with current benefits.
     */
    @Transactional(readOnly = true)
    public UserMembershipResponse getActiveMembership(Long userId) {
        UserMembership membership = membershipRepository
                .findByUserIdAndStatus(userId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new MembershipNotFoundException(
                        "No active membership found for user: " + userId));
        return toResponse(membership);
    }

    /**
     * Upgrades a user's tier to a higher tier.
     * Optimistic locking (@Version) ensures concurrent calls are safe.
     */
    @Transactional
    public UserMembershipResponse upgradeTier(Long membershipId, UpgradeDowngradeRequest request) {
        UserMembership membership = getActiveMembershipById(membershipId);

        TierLevel currentLevel = membership.getTier().getLevel();
        TierLevel targetLevel = request.getTargetTierLevel();

        if (!targetLevel.isHigherThan(currentLevel)) {
            throw new InvalidOperationException(
                    "Target tier " + targetLevel + " is not higher than current tier " + currentLevel +
                    ". Use /downgrade for tier reduction.");
        }

        MembershipTier newTier = tierRepository.findByLevel(targetLevel)
                .filter(MembershipTier::isActive)
                .orElseThrow(() -> new MembershipNotFoundException("Tier not found: " + targetLevel));

        membership.setTier(newTier);
        membership = membershipRepository.save(membership);

        historyRepository.save(MembershipHistory.builder()
                .userId(membership.getUserId())
                .membershipId(membership.getId())
                .changeType(ChangeType.UPGRADED)
                .previousTier(currentLevel)
                .newTier(targetLevel)
                .previousStatus(MembershipStatus.ACTIVE)
                .newStatus(MembershipStatus.ACTIVE)
                .reason(request.getReason() != null ? request.getReason() : "Manual upgrade")
                .build());

        log.info("Membership {} upgraded: {} → {}", membershipId, currentLevel, targetLevel);
        return toResponse(membership);
    }

    /**
     * Downgrades a user's tier to a lower tier.
     */
    @Transactional
    public UserMembershipResponse downgradeTier(Long membershipId, UpgradeDowngradeRequest request) {
        UserMembership membership = getActiveMembershipById(membershipId);

        TierLevel currentLevel = membership.getTier().getLevel();
        TierLevel targetLevel = request.getTargetTierLevel();

        if (!targetLevel.isLowerThan(currentLevel)) {
            throw new InvalidOperationException(
                    "Target tier " + targetLevel + " is not lower than current tier " + currentLevel +
                    ". Use /upgrade for tier promotion.");
        }

        MembershipTier newTier = tierRepository.findByLevel(targetLevel)
                .filter(MembershipTier::isActive)
                .orElseThrow(() -> new MembershipNotFoundException("Tier not found: " + targetLevel));

        membership.setTier(newTier);
        membership = membershipRepository.save(membership);

        historyRepository.save(MembershipHistory.builder()
                .userId(membership.getUserId())
                .membershipId(membership.getId())
                .changeType(ChangeType.DOWNGRADED)
                .previousTier(currentLevel)
                .newTier(targetLevel)
                .previousStatus(MembershipStatus.ACTIVE)
                .newStatus(MembershipStatus.ACTIVE)
                .reason(request.getReason() != null ? request.getReason() : "Manual downgrade")
                .build());

        log.info("Membership {} downgraded: {} → {}", membershipId, currentLevel, targetLevel);
        return toResponse(membership);
    }

    /**
     * Cancels a user's active membership immediately.
     */
    @Transactional
    public void cancelMembership(Long membershipId) {
        UserMembership membership = getActiveMembershipById(membershipId);

        MembershipStatus previousStatus = membership.getStatus();
        TierLevel previousTier = membership.getTier().getLevel();

        membership.setStatus(MembershipStatus.CANCELLED);
        membership.setCancelledAt(LocalDateTime.now());
        membershipRepository.save(membership);

        historyRepository.save(MembershipHistory.builder()
                .userId(membership.getUserId())
                .membershipId(membership.getId())
                .changeType(ChangeType.CANCELLED)
                .previousTier(previousTier)
                .newTier(previousTier)
                .previousStatus(previousStatus)
                .newStatus(MembershipStatus.CANCELLED)
                .reason("User cancelled membership")
                .build());

        log.info("Membership {} cancelled for userId={}", membershipId, membership.getUserId());
    }

    /**
     * Returns complete membership history for a user (audit log).
     */
    @Transactional(readOnly = true)
    public List<MembershipHistoryResponse> getMembershipHistory(Long userId) {
        return historyRepository.findByUserIdOrderByChangedAtDesc(userId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    private UserMembership getActiveMembershipById(Long membershipId) {
        UserMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new MembershipNotFoundException(
                        "Membership not found with id: " + membershipId));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "Membership " + membershipId + " is not active (status: " + membership.getStatus() + ")");
        }
        return membership;
    }

    private UserMembershipResponse toResponse(UserMembership membership) {
        List<BenefitResponse> benefits = benefitEngine.getUserBenefits(membership.getUserId());
        long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), membership.getEndDate());

        return UserMembershipResponse.builder()
                .id(membership.getId())
                .userId(membership.getUserId())
                .planId(membership.getPlan().getId())
                .planName(membership.getPlan().getName())
                .planDuration(membership.getPlan().getDuration())
                .planPrice(membership.getPlan().getPrice())
                .tierLevel(membership.getTier().getLevel())
                .tierDisplayName(membership.getTier().getDisplayName())
                .status(membership.getStatus())
                .startDate(membership.getStartDate())
                .endDate(membership.getEndDate())
                .daysRemaining(Math.max(0, daysRemaining))
                .active(membership.isActive())
                .activeBenefits(benefits)
                .build();
    }

    private MembershipHistoryResponse toHistoryResponse(MembershipHistory history) {
        return MembershipHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUserId())
                .membershipId(history.getMembershipId())
                .changeType(history.getChangeType())
                .previousTier(history.getPreviousTier())
                .newTier(history.getNewTier())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .reason(history.getReason())
                .changedAt(history.getChangedAt())
                .build();
    }
}
