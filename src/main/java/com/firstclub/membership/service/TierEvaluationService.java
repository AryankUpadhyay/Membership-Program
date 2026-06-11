package com.firstclub.membership.service;

import com.firstclub.membership.model.MembershipHistory;
import com.firstclub.membership.model.MembershipTier;
import com.firstclub.membership.model.TierUpgradeRule;
import com.firstclub.membership.model.UserMembership;
import com.firstclub.membership.model.UserOrderStats;
import com.firstclub.membership.model.enums.ChangeType;
import com.firstclub.membership.model.enums.MembershipStatus;
import com.firstclub.membership.model.enums.RuleType;
import com.firstclub.membership.model.enums.TierLevel;
import com.firstclub.membership.repository.MembershipHistoryRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.UserMembershipRepository;
import com.firstclub.membership.tier.TierRuleEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Core tier evaluation service.
 *
 * Uses the Strategy pattern — all TierRuleEvaluator beans are auto-discovered
 * and dispatched by RuleType. Adding a new criteria type requires only a new
 * @Component implementing TierRuleEvaluator.
 *
 * Evaluation logic:
 * - Tiers are evaluated from highest (PLATINUM) down to lowest (SILVER).
 * - A user is promoted to the highest tier whose rules they ALL satisfy (AND semantics).
 * - If no tier is satisfied, the user keeps their current tier.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TierEvaluationService {

    private final MembershipTierRepository tierRepository;
    private final UserMembershipRepository membershipRepository;
    private final MembershipHistoryRepository historyRepository;
    private final List<TierRuleEvaluator> evaluators;

    /**
     * Evaluates what tier a user qualifies for based on their order stats.
     * Returns the best (highest) tier the user qualifies for, or empty if none.
     */
    public Optional<MembershipTier> evaluateBestTier(UserOrderStats stats) {
        Map<RuleType, TierRuleEvaluator> evaluatorMap = evaluators.stream()
                .collect(Collectors.toMap(TierRuleEvaluator::getSupportedRuleType, Function.identity()));

        // Tiers sorted highest to lowest (PLATINUM, GOLD, SILVER)
        List<MembershipTier> tiersDescending = tierRepository.findByActiveTrueOrderByLevelAsc()
                .stream()
                .sorted((a, b) -> b.getLevel().ordinal() - a.getLevel().ordinal())
                .collect(Collectors.toList());

        for (MembershipTier tier : tiersDescending) {
            List<TierUpgradeRule> rules = tier.getUpgradeRules();
            if (rules == null || rules.isEmpty()) continue;

            boolean allRulesMet = rules.stream().allMatch(rule -> {
                TierRuleEvaluator evaluator = evaluatorMap.get(rule.getRuleType());
                if (evaluator == null) {
                    log.warn("No evaluator found for rule type: {}", rule.getRuleType());
                    return false;
                }
                return evaluator.evaluate(rule, stats);
            });

            if (allRulesMet) {
                log.debug("User {} qualifies for tier {}", stats.getUserId(), tier.getLevel());
                return Optional.of(tier);
            }
        }

        return Optional.empty();
    }

    /**
     * Re-evaluates and potentially promotes/demotes a user's active membership tier
     * based on their current order stats. Called after each order event and by scheduler.
     */
    @Transactional
    public void evaluateAndUpdateTier(Long userId, UserOrderStats stats) {
        Optional<UserMembership> membershipOpt = membershipRepository.findByUserIdAndStatus(
                userId, MembershipStatus.ACTIVE);

        if (membershipOpt.isEmpty()) {
            log.debug("No active membership for userId={}, skipping tier evaluation", userId);
            return;
        }

        UserMembership membership = membershipOpt.get();
        Optional<MembershipTier> bestTierOpt = evaluateBestTier(stats);

        if (bestTierOpt.isEmpty()) {
            log.debug("userId={} does not qualify for any tier upgrade", userId);
            return;
        }

        MembershipTier bestTier = bestTierOpt.get();
        TierLevel currentLevel = membership.getTier().getLevel();
        TierLevel newLevel = bestTier.getLevel();

        if (newLevel == currentLevel) {
            log.debug("userId={} already at correct tier {}", userId, currentLevel);
            return;
        }

        ChangeType changeType = newLevel.isHigherThan(currentLevel)
                ? ChangeType.TIER_AUTO_PROMOTED
                : ChangeType.TIER_AUTO_DEMOTED;

        membership.setTier(bestTier);
        membershipRepository.save(membership);

        historyRepository.save(MembershipHistory.builder()
                .userId(userId)
                .membershipId(membership.getId())
                .changeType(changeType)
                .previousTier(currentLevel)
                .newTier(newLevel)
                .previousStatus(membership.getStatus())
                .newStatus(membership.getStatus())
                .reason("Auto-evaluated based on order activity")
                .build());

        log.info("Tier auto-{}: userId={} {} → {}",
                changeType == ChangeType.TIER_AUTO_PROMOTED ? "promoted" : "demoted",
                userId, currentLevel, newLevel);
    }
}
