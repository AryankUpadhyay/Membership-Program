package com.firstclub.membership.repository;

import com.firstclub.membership.model.MembershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipHistoryRepository extends JpaRepository<MembershipHistory, Long> {

    List<MembershipHistory> findByUserIdOrderByChangedAtDesc(Long userId);

    List<MembershipHistory> findByMembershipIdOrderByChangedAtDesc(Long membershipId);
}
