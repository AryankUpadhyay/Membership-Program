package com.firstclub.membership.controller;

import com.firstclub.membership.dto.request.SubscribeRequest;
import com.firstclub.membership.dto.request.UpgradeDowngradeRequest;
import com.firstclub.membership.dto.response.ApiResponse;
import com.firstclub.membership.dto.response.MembershipHistoryResponse;
import com.firstclub.membership.dto.response.UserMembershipResponse;
import com.firstclub.membership.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Manage user membership subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Subscribe to a membership plan",
               description = "Creates a new membership for the user. Fails if the user already has an active subscription.")
    public ResponseEntity<ApiResponse<UserMembershipResponse>> subscribe(
            @Valid @RequestBody SubscribeRequest request) {
        UserMembershipResponse response = subscriptionService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription created successfully", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's active membership",
               description = "Retrieves the current active membership with tier, benefits, and expiry info")
    public ResponseEntity<ApiResponse<UserMembershipResponse>> getActiveMembership(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getActiveMembership(userId)));
    }

    @PutMapping("/{membershipId}/upgrade")
    @Operation(summary = "Upgrade membership tier",
               description = "Manually upgrades the user's tier to a higher level (Silver → Gold → Platinum)")
    public ResponseEntity<ApiResponse<UserMembershipResponse>> upgradeTier(
            @PathVariable Long membershipId,
            @Valid @RequestBody UpgradeDowngradeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Tier upgraded successfully",
                        subscriptionService.upgradeTier(membershipId, request)));
    }

    @PutMapping("/{membershipId}/downgrade")
    @Operation(summary = "Downgrade membership tier",
               description = "Manually downgrades the user's tier to a lower level (Platinum → Gold → Silver)")
    public ResponseEntity<ApiResponse<UserMembershipResponse>> downgradeTier(
            @PathVariable Long membershipId,
            @Valid @RequestBody UpgradeDowngradeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Tier downgraded successfully",
                        subscriptionService.downgradeTier(membershipId, request)));
    }

    @DeleteMapping("/{membershipId}")
    @Operation(summary = "Cancel membership",
               description = "Immediately cancels the membership. Access to benefits is revoked.")
    public ResponseEntity<ApiResponse<Void>> cancelMembership(@PathVariable Long membershipId) {
        subscriptionService.cancelMembership(membershipId);
        return ResponseEntity.ok(ApiResponse.success("Membership cancelled successfully", null));
    }

    @GetMapping("/user/{userId}/history")
    @Operation(summary = "Get membership history",
               description = "Returns full audit log of all membership changes for a user (subscribe, upgrade, cancel, auto-tier changes)")
    public ResponseEntity<ApiResponse<List<MembershipHistoryResponse>>> getMembershipHistory(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getMembershipHistory(userId)));
    }
}
