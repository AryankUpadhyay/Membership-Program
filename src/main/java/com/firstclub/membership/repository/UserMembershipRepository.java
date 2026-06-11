package com.firstclub.membership.repository;

import com.firstclub.membership.model.UserMembership;
import com.firstclub.membership.model.enums.MembershipStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    /**
     * Find the active membership for a user. A user can only have one active membership at a time.
     */
    Optional<UserMembership> findByUserIdAndStatus(Long userId, MembershipStatus status);

    /**
     * Pessimistic write lock — prevents concurrent subscribe calls for the same user
     * from both proceeding past the existence check.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM UserMembership m WHERE m.userId = :userId AND m.status = 'ACTIVE'")
    Optional<UserMembership> findActiveByUserIdForUpdate(@Param("userId") Long userId);

    /**
     * Fetch all memberships expiring before a given timestamp (for the expiry scheduler).
     */
    @Query("SELECT m FROM UserMembership m WHERE m.status = 'ACTIVE' AND m.endDate <= :threshold")
    List<UserMembership> findExpiredMemberships(@Param("threshold") LocalDateTime threshold);

    List<UserMembership> findByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, MembershipStatus status);
}
