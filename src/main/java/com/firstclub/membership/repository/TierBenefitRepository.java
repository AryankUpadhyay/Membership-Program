package com.firstclub.membership.repository;

import com.firstclub.membership.model.TierBenefit;
import com.firstclub.membership.model.enums.BenefitType;
import com.firstclub.membership.model.enums.TierLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TierBenefitRepository extends JpaRepository<TierBenefit, Long> {

    List<TierBenefit> findByTierIdAndEnabledTrue(Long tierId);

    @Query("SELECT tb FROM TierBenefit tb WHERE tb.tier.level = :level AND tb.enabled = true")
    List<TierBenefit> findActiveByTierLevel(@Param("level") TierLevel level);

    @Query("SELECT tb FROM TierBenefit tb WHERE tb.tier.level = :level AND tb.benefitType = :type AND tb.enabled = true")
    Optional<TierBenefit> findByTierLevelAndBenefitType(@Param("level") TierLevel level,
                                                         @Param("type") BenefitType type);
}
