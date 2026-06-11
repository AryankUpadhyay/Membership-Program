package com.firstclub.membership.repository;

import com.firstclub.membership.model.TierUpgradeRule;
import com.firstclub.membership.model.enums.TierLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TierUpgradeRuleRepository extends JpaRepository<TierUpgradeRule, Long> {

    @Query("SELECT r FROM TierUpgradeRule r WHERE r.tier.level = :level")
    List<TierUpgradeRule> findByTierLevel(@Param("level") TierLevel level);
}
