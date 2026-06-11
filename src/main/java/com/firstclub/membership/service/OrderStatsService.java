package com.firstclub.membership.service;

import com.firstclub.membership.dto.request.OrderEventRequest;
import com.firstclub.membership.dto.response.OrderStatsResponse;
import com.firstclub.membership.model.UserOrderStats;
import com.firstclub.membership.repository.UserOrderStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatsService {

    private final UserOrderStatsRepository statsRepository;

    /**
     * Records an order event for a user.
     * Uses PESSIMISTIC_WRITE lock to prevent concurrent lost updates on the same user's stats.
     * Returns the updated stats for immediate tier re-evaluation.
     */
    @Transactional
    public UserOrderStats recordOrder(OrderEventRequest event) {
        // Lock the row before modifying to prevent concurrent counter corruption
        UserOrderStats stats = statsRepository.findByUserIdForUpdate(event.getUserId())
                .orElseGet(() -> UserOrderStats.builder()
                        .userId(event.getUserId())
                        .build());

        stats.setOrderCountThisMonth(stats.getOrderCountThisMonth() + 1);
        stats.setTotalOrderValueThisMonth(
                stats.getTotalOrderValueThisMonth().add(event.getOrderValue()));
        stats.setLifetimeOrderCount(stats.getLifetimeOrderCount() + 1);
        stats.setLifetimeTotalOrderValue(
                stats.getLifetimeTotalOrderValue().add(event.getOrderValue()));

        // Update cohort if provided
        if (event.getCohortKey() != null && !event.getCohortKey().isBlank()) {
            stats.setCohortKey(event.getCohortKey());
        }

        UserOrderStats saved = statsRepository.save(stats);
        log.info("Recorded order: userId={} orderId={} value={} | monthlyCount={} monthlyValue={}",
                event.getUserId(), event.getOrderId(), event.getOrderValue(),
                saved.getOrderCountThisMonth(), saved.getTotalOrderValueThisMonth());
        return saved;
    }

    @Transactional(readOnly = true)
    public UserOrderStats getStats(Long userId) {
        return statsRepository.findByUserId(userId)
                .orElseGet(() -> UserOrderStats.builder()
                        .userId(userId)
                        .build());
    }

    @Transactional(readOnly = true)
    public OrderStatsResponse getStatsResponse(Long userId) {
        UserOrderStats stats = getStats(userId);
        return OrderStatsResponse.builder()
                .userId(stats.getUserId())
                .cohortKey(stats.getCohortKey())
                .orderCountThisMonth(stats.getOrderCountThisMonth())
                .totalOrderValueThisMonth(stats.getTotalOrderValueThisMonth())
                .lifetimeOrderCount(stats.getLifetimeOrderCount())
                .lifetimeTotalOrderValue(stats.getLifetimeTotalOrderValue())
                .build();
    }

    /**
     * Called by the monthly reset scheduler. Resets per-month counters for all users.
     */
    @Transactional
    public void resetMonthlyStats() {
        statsRepository.resetMonthlyStats();
        log.info("Monthly order stats reset completed for all users.");
    }
}
