package com.firstclub.membership.controller;

import com.firstclub.membership.dto.request.CheckoutRequest;
import com.firstclub.membership.dto.response.ApiResponse;
import com.firstclub.membership.dto.response.BenefitResponse;
import com.firstclub.membership.dto.response.CheckoutBenefitResponse;
import com.firstclub.membership.service.BenefitEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/benefits")
@RequiredArgsConstructor
@Tag(name = "Benefits", description = "Query membership benefits and compute checkout savings")
public class BenefitController {

    private final BenefitEngine benefitEngine;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's active benefits",
               description = "Returns all benefits active for the user's current membership tier")
    public ResponseEntity<ApiResponse<List<BenefitResponse>>> getUserBenefits(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(benefitEngine.getUserBenefits(userId)));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Compute checkout benefits",
               description = "Calculates discount and delivery savings for a cart total. " +
                             "Returns breakdown of savings to integrate with the checkout flow.")
    public ResponseEntity<ApiResponse<CheckoutBenefitResponse>> computeCheckoutBenefits(
            @Valid @RequestBody CheckoutRequest request) {
        CheckoutBenefitResponse result = benefitEngine.computeCheckoutBenefits(
                request.getUserId(),
                request.getCartTotal(),
                request.isDeliveryEligible());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
