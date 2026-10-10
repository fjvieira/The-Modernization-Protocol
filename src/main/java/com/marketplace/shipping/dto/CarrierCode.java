package com.marketplace.shipping.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum CarrierCode {
    MOCK_CARRIER,
    FEDEX,
    UPS,
    DHL,
    @JsonEnumDefaultValue
    UNKNOWN
}