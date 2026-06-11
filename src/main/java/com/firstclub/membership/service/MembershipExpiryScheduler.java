package com.firstclub.membership.service;

import com.firstclub.membership.model.UserMembership;
import com.firstclub.membership.model.UserOrderStats;
import com.firstclub.membership.model.enums.ChangeType;
import com.firstclub.membership.model.enums.MembershipStatus;
import com.firstclub.membership.repository.MembershipHistoryRepository;
import com.firstclub.membership.repository.UserMembershipRepository;
import com.firstclub.membership.repository.UserOrderStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled background jobs for membership lifecycle management.
 *
 * Jobs run on a dedicated Spring TaskScheduler thread pool (see SchedulingConfig).
 * Isolated from request threads — failures here do not affect live traffic.
 *
 * Cron expressions are configurable via application.yml.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipExpiryScheduler {

    private final UserMembershipRepository membershipRepository;
    private final MembershipHistoryRepository historyRepository;
    private final UserOrderStatsRepository statsRepository;
    private final TierEvaluationService tierEvaluationService;
    private final OrderStatsService orderStatsService;

    /**
     * Daily job: expires memberships whose end date has passed.
     * Runs at midnight every day.
     */
    @Scheduled(cron = "${membership.scheduler.expiry-check-cron}")
    @Transactional
    public void expireMemberships() {
        log.info("Running membership expiry check...");
        List<UserMembership> expired = membershipRepository.findExpiredMemberships(LocalDateTime.now());

        for (UserMembership membership : expired) {
            MembershipStatus prevStatus = membership.getStatus();
            membership.setStatus(MembershipStatus.EXPIRED);
            membershipRepository.save(membership);

            historyRepository.save(com.firstclub.membership.model.MembershipHistory.builder()
                    .userId(membership.getUserId())
                    .membershipId(membership.getId())
                    .changeType(ChangeType.EXPIRED)
                    .previousTier(membership.getTier().getLevel())
                    .newTier(membership.getTier().getLevel())
                    .previousStatus(prevStatus)
                    .newStatus(MembershipStatus.EXPIRED)
                    .reason("Membership period ended")
                    .build());
        }

        if (!expired.isEmpty()) {
            log.info("Expired {} memberships", expired.size());
        }
    }

    /**
     * Daily job (1 AM): re-evaluates all active member tiers based on order stats.
     * Ensures tier state stays consistent even if order events were missed.
     */
    @Scheduled(cron = "${membership.scheduler.tier-eval-cron}")
    @Transactional
    public void reEvaluateAllTiers() {
        log.info("Running scheduled tier re-evaluation for all active members...");

        List<UserMembership> activeMembers = membershipRepository
                .findAll()
                .stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .toList();

        for (UserMembership membership : activeMembers) {
            try {
                UserOrderStats stats = orderStatsService.getStats(membership.getUserId());
                tierEvaluationService.evaluateAndUpdateTier(membership.getUserId(), stats);
            } catch (Exception e) {
                log.error("Error re-evaluating tier for userId={}: {}",
                        membership.getUserId(), e.getMessage());
            }
        }

        log.info("Tier re-evaluation completed for {} members.", activeMembers.size());
    }

    /**
     * Monthly job: resets per-month order counters on the 1st at 2 AM.
     * Ensures tier evaluation uses a fresh monthly window.
     */
    @Scheduled(cron = "0 0 2 1 * *")
    @Transactional
    public void resetMonthlyStats() {
        log.info("Running monthly order stats reset...");
        orderStatsService.resetMonthlyStats();
    }
}
