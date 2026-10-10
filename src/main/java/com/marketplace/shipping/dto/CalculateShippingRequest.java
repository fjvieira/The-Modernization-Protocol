package com.marketplace.shipping.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculateShippingRequest {
    
    BigDecimal cartTotal;
    CustomerMetadata customerMetadata;
    Destination destination;
    Parcel parcel;
    OptionalContext optionalContext;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomerMetadata {
        String tier;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Destination {
        String countryCode;
        String zipCode;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Parcel {
        Double weight;
        Double length;
        Double width;
        Double height;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionalContext {
        String promoCode;
        OffsetDateTime orderTimestamp;
    }
}