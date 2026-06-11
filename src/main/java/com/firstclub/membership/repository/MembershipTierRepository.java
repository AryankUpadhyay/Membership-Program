package com.firstclub.membership.repository;

import com.firstclub.membership.model.MembershipTier;
import com.firstclub.membership.model.enums.TierLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {

    List<MembershipTier> findByActiveTrueOrderByLevelAsc();

    Optional<MembershipTier> findByLevel(TierLevel level);
}
