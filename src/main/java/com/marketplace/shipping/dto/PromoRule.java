package com.marketplace.shipping.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoRule {

    public enum DiscountType {
        FLAT, PERCENTAGE, PERCENTAGE_REMAINING
    }

    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minCartSubtotal;
    private Integer startHour;
    private Integer endHour;
    private boolean active;

}