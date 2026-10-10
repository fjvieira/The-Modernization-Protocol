package com.marketplace.shipping.domain;

import java.math.BigDecimal;

import lombok.Getter;

public enum Tier {
    STANDARD(BigDecimal.ZERO),
    VIP(new BigDecimal("0.30")),
    GOLD(new BigDecimal("0.20")),
    SILVER(new BigDecimal("0.10"));

    @Getter
    private BigDecimal discountPercentage;

    private Tier(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

}
