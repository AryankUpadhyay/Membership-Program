package com.firstclub.membership.controller;

import com.firstclub.membership.dto.response.ApiResponse;
import com.firstclub.membership.dto.response.MembershipPlanResponse;
import com.firstclub.membership.dto.response.MembershipTierResponse;
import com.firstclub.membership.service.MembershipPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Membership Plans", description = "Browse available membership plans and tiers")
public class MembershipPlanController {

    private final MembershipPlanService planService;

    @GetMapping("/plans")
    @Operation(summary = "Get all active membership plans",
               description = "Returns all active plans (Monthly, Quarterly, Yearly) with available tiers and their benefits")
    public ResponseEntity<ApiResponse<List<MembershipPlanResponse>>> getAllPlans() {
        return ResponseEntity.ok(ApiResponse.success(planService.getAllActivePlans()));
    }

    @GetMapping("/plans/{id}")
    @Operation(summary = "Get a specific membership plan by ID")
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(planService.getPlanById(id)));
    }

    @GetMapping("/tiers")
    @Operation(summary = "Get all membership tiers",
               description = "Returns all tiers (Silver, Gold, Platinum) with their benefits and upgrade criteria")
    public ResponseEntity<ApiResponse<List<MembershipTierResponse>>> getAllTiers() {
        return ResponseEntity.ok(ApiResponse.success(planService.getAllActiveTiers()));
    }
}
