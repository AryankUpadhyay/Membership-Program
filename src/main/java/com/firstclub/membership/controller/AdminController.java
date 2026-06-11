package com.firstclub.membership.controller;

import com.firstclub.membership.dto.request.OrderEventRequest;
import com.firstclub.membership.dto.response.ApiResponse;
import com.firstclub.membership.dto.response.OrderStatsResponse;
import com.firstclub.membership.model.UserOrderStats;
import com.firstclub.membership.service.OrderStatsService;
import com.firstclub.membership.service.TierEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin operations: order events and tier management")
public class AdminController {

    private final OrderStatsService orderStatsService;
    private final TierEvaluationService tierEvaluationService;

    @PostMapping("/orders/event")
    @Operation(summary = "Record an order event",
               description = "Records a completed order for a user and triggers tier re-evaluation. " +
                             "This endpoint is called by the shopping platform's order service.")
    public ResponseEntity<ApiResponse<OrderStatsResponse>> recordOrderEvent(
            @Valid @RequestBody OrderEventRequest event) {
        UserOrderStats stats = orderStatsService.recordOrder(event);
        // Trigger tier evaluation immediately after order is recorded
        tierEvaluationService.evaluateAndUpdateTier(event.getUserId(), stats);
        return ResponseEntity.ok(ApiResponse.success(
                "Order recorded and tier evaluated",
                orderStatsService.getStatsResponse(event.getUserId())));
    }

    @PostMapping("/tiers/evaluate/{userId}")
    @Operation(summary = "Manually trigger tier evaluation",
               description = "Forces tier re-evaluation for a user based on their current order stats")
    public ResponseEntity<ApiResponse<String>> evaluateTier(@PathVariable Long userId) {
        UserOrderStats stats = orderStatsService.getStats(userId);
        tierEvaluationService.evaluateAndUpdateTier(userId, stats);
        return ResponseEntity.ok(ApiResponse.success("Tier evaluation completed for userId: " + userId));
    }

    @GetMapping("/stats/{userId}")
    @Operation(summary = "Get user order statistics",
               description = "Returns monthly and lifetime order count and value for tier evaluation context")
    public ResponseEntity<ApiResponse<OrderStatsResponse>> getUserStats(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(orderStatsService.getStatsResponse(userId)));
    }
}
