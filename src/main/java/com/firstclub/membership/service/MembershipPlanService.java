package com.firstclub.membership.service;

import com.firstclub.membership.dto.response.BenefitResponse;
import com.firstclub.membership.dto.response.MembershipPlanResponse;
import com.firstclub.membership.dto.response.MembershipTierResponse;
import com.firstclub.membership.dto.response.TierRuleResponse;
import com.firstclub.membership.exception.MembershipNotFoundException;
import com.firstclub.membership.model.MembershipPlan;
import com.firstclub.membership.model.MembershipTier;
import com.firstclub.membership.repository.MembershipPlanRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipPlanService {

    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;

    /**
     * Returns all active plans, each decorated with the full list of available tiers.
     * This is the primary "catalog" endpoint for the front-end selection UI.
     */
    public List<MembershipPlanResponse> getAllActivePlans() {
        List<MembershipTierResponse> tiers = getAllActiveTiers();
        return planRepository.findByActiveTrue()
                .stream()
                .map(plan -> toResponse(plan, tiers))
                .collect(Collectors.toList());
    }

    public MembershipPlanResponse getPlanById(Long id) {
        MembershipPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException("Plan not found with id: " + id));
        return toResponse(plan, getAllActiveTiers());
    }

    public List<MembershipTierResponse> getAllActiveTiers() {
        return tierRepository.findByActiveTrueOrderByLevelAsc()
                .stream()
                .map(this::toTierResponse)
                .collect(Collectors.toList());
    }

    public MembershipTier getTierEntityById(Long id) {
        return tierRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException("Tier not found with id: " + id));
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private MembershipPlanResponse toResponse(MembershipPlan plan, List<MembershipTierResponse> tiers) {
        return MembershipPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .duration(plan.getDuration())
                .durationDays(plan.getDurationDays())
                .price(plan.getPrice())
                .description(plan.getDescription())
                .availableTiers(tiers)
                .build();
    }

    public MembershipTierResponse toTierResponse(MembershipTier tier) {
        List<BenefitResponse> benefits = tier.getBenefits() == null ? List.of() :
                tier.getBenefits().stream()
                        .filter(b -> b.isEnabled())
                        .map(b -> BenefitResponse.builder()
                                .id(b.getId())
                                .benefitType(b.getBenefitType())
                                .value(b.getValue())
                                .description(b.getDescription())
                                .build())
                        .collect(Collectors.toList());

        List<TierRuleResponse> rules = tier.getUpgradeRules() == null ? List.of() :
                tier.getUpgradeRules().stream()
                        .map(r -> TierRuleResponse.builder()
                                .ruleType(r.getRuleType())
                                .threshold(r.getThreshold())
                                .evaluationWindowDays(r.getEvaluationWindowDays())
                                .cohortKey(r.getCohortKey())
                                .description(r.getDescription())
                                .build())
                        .collect(Collectors.toList());

        return MembershipTierResponse.builder()
                .id(tier.getId())
                .level(tier.getLevel())
                .displayName(tier.getDisplayName())
                .description(tier.getDescription())
                .benefits(benefits)
                .upgradeRules(rules)
                .build();
    }
}
