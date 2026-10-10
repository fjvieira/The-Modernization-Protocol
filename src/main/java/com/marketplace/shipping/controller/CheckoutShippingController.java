package com.marketplace.shipping.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.shipping.dto.CalculateShippingRequest;
import com.marketplace.shipping.dto.CalculateShippingResponse;
import com.marketplace.shipping.service.LoyaltyShippingService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v2/checkout")
@Slf4j
public class CheckoutShippingController {

    private final LoyaltyShippingService shippingService;

    public CheckoutShippingController(LoyaltyShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping("/calculate-shipping")
    public ResponseEntity<CalculateShippingResponse> calculateShipping(
            @RequestBody CalculateShippingRequest request) {
        log.info("Shipping calculation requested");
        CalculateShippingResponse response = shippingService.calculateShipping(request);
        log.info("Shipping calculation completed with final fee {}", response.getFinalShippingFee());
        return ResponseEntity.ok(response);
    }
}
