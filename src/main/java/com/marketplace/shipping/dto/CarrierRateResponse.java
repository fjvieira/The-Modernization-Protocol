package com.marketplace.shipping.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarrierRateResponse {

    BigDecimal baseRate;

    Boolean surcharge;

    CarrierCode carrierCode;

    String estimatedDeliveryDays;
    
}
