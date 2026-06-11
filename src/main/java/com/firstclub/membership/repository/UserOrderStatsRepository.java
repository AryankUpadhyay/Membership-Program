package com.firstclub.membership.repository;

import com.firstclub.membership.model.UserOrderStats;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOrderStatsRepository extends JpaRepository<UserOrderStats, Long> {

    Optional<UserOrderStats> findByUserId(Long userId);

    /**
     * Pessimistic write lock for order stat updates to prevent lost count/value increments
     * under high concurrency for the same user.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM UserOrderStats s WHERE s.userId = :userId")
    Optional<UserOrderStats> findByUserIdForUpdate(@Param("userId") Long userId);

    /**
     * Bulk reset of monthly stats at the start of each month (called by scheduler).
     */
    @Modifying
    @Query("UPDATE UserOrderStats s SET s.orderCountThisMonth = 0, s.totalOrderValueThisMonth = 0")
    void resetMonthlyStats();
}
