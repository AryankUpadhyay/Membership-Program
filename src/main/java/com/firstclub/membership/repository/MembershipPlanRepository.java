package com.firstclub.membership.repository;

import com.firstclub.membership.model.MembershipPlan;
import com.firstclub.membership.model.enums.PlanDuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    List<MembershipPlan> findByActiveTrue();

    Optional<MembershipPlan> findByDurationAndActiveTrue(PlanDuration duration);
}
