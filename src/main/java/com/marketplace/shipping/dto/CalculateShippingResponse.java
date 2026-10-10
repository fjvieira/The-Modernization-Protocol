package com.marketplace.shipping.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalculateShippingResponse {

    private BigDecimal baseShippingRate;
    
    private BigDecimal totalSurcharges;
    
    private BigDecimal shippingDiscount;
    
    private BigDecimal totalDiscount;
    
    private BigDecimal finalShippingFee;
   
    @JsonProperty("isDiscountCapped")
    private boolean discountCapped;

    private Map<String, BigDecimal> appliedFees;
 
}