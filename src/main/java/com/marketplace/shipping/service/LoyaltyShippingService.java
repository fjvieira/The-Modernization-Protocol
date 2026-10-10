package com.marketplace.shipping.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.marketplace.shipping.domain.Tier;
import com.marketplace.shipping.dto.CalculateShippingRequest;
import com.marketplace.shipping.dto.CalculateShippingResponse;
import com.marketplace.shipping.dto.CarrierCode;
import com.marketplace.shipping.dto.CarrierRateResponse;
import com.marketplace.shipping.repository.ShippingRateRepository;
import com.marketplace.shipping.repository.ShippingRateRepository.ZoneRate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoyaltyShippingService {

    private static final BigDecimal USD_50_THRESHOLD = new BigDecimal("50.00");

    private final ShippingRateRepository rateRepository;
    private final CarrierRateAPIService carrierAPIService;
    private final PromoEvaluatorService promoEvaluatorService;

    public LoyaltyShippingService(ShippingRateRepository rateRepository,
            CarrierRateAPIService carrierAPIService,
            PromoEvaluatorService promoEvaluatorService) {
        this.rateRepository = rateRepository;
        this.carrierAPIService = carrierAPIService;
        this.promoEvaluatorService = promoEvaluatorService;
    }

    public CalculateShippingResponse calculateShipping(CalculateShippingRequest request) {
        BigDecimal cartTotal = request.getCartTotal() != null ? request.getCartTotal() : BigDecimal.ZERO;

        String countryCode = request.getDestination() != null ? request.getDestination().getCountryCode() : null;
        String zipCode = request.getDestination() != null ? request.getDestination().getZipCode() : null;

        if (countryCode == null) {
            throw new IllegalArgumentException("Shipping is not available for country: " + countryCode);
        }

        BigDecimal baseRate = BigDecimal.ZERO;
        BigDecimal regionalSurcharge = BigDecimal.ZERO;
        BigDecimal carrierSurchargeFee = BigDecimal.ZERO;

        Map<String, BigDecimal> appliedFees = new LinkedHashMap<>();
        log.info("Calculating shipping for country={}, zip={}, cartTotal={}",
            countryCode, zipCode, cartTotal);

        // 1. Fetch Base Shipping Rates in USD (Local DB vs External Carrier API
        // Fallback)
        Optional<ZoneRate> zoneRateOpt = rateRepository.findZoneRate(countryCode, zipCode);

        if (zoneRateOpt.isPresent()) {
            ZoneRate zoneData = zoneRateOpt.get();
            baseRate = zoneData.getBaseRate();
            regionalSurcharge = zoneData.getRegionalSurcharge();
                log.info("Using database rate baseRate={}, regionalSurcharge={}",
                    baseRate, regionalSurcharge);

            if (regionalSurcharge.compareTo(BigDecimal.ZERO) > 0) {
                appliedFees.put("REGIONAL_SURCHARGE", round(regionalSurcharge));
            }
        } else {
            log.warn("No database rate found for country={}, zip={}; using carrier fallback",
                    countryCode, zipCode);
                Optional<CarrierRateResponse> carrierResponse = carrierAPIService.fetchCarrierRate(
                    countryCode, zipCode, request.getParcel());
                if (carrierResponse.isEmpty()) {
                throw new IllegalArgumentException(
                    "Shipping is not available for country: " + countryCode);
                }
                CarrierRateResponse carrierResp = carrierResponse.get();
            baseRate = carrierResp.getBaseRate();

            if (Boolean.TRUE.equals(carrierResp.getSurcharge())
                    && carrierResp.getCarrierCode() != CarrierCode.UNKNOWN) {
                carrierSurchargeFee = rateRepository
                        .findCarrierSurcharge(carrierResp.getCarrierCode().name(), countryCode)
                        .orElse(BigDecimal.ZERO);
            }
            
            if (carrierSurchargeFee.compareTo(BigDecimal.ZERO) > 0) {
                appliedFees.put("CARRIER_SURCHARGE", round(carrierSurchargeFee));
            }
            log.info("Using carrier rate baseRate={}, carrierSurcharge={}",
                    baseRate, carrierSurchargeFee);
        }

        BigDecimal grossFreight = baseRate.add(regionalSurcharge).add(carrierSurchargeFee);

        // 2. Shipping Tier Discounts
        String tier = "STANDARD";
        if (request.getCustomerMetadata() != null && request.getCustomerMetadata().getTier() != null) {
            tier = request.getCustomerMetadata().getTier();
        }
        BigDecimal tierShippingDiscount = cartTotal.multiply(
            Tier.valueOf(tier.toUpperCase()).getDiscountPercentage());
        log.info("Applied tier={} discount={}", tier, tierShippingDiscount);

        // 3. Shipping Promo Code Discounts
        BigDecimal promoDiscount = promoEvaluatorService.evaluatePromoDiscount(
                request.getOptionalContext() != null ? request.getOptionalContext().getPromoCode() : null,
                cartTotal,
                grossFreight,
                tierShippingDiscount,
                request.getOptionalContext() != null ? request.getOptionalContext()
                        .getOrderTimestamp() : null);
                log.info("Applied promo discount={}", promoDiscount);

        // 4. Discount Capping (Max 50% of USD Cart Total) & Zero-Floor Check
        BigDecimal maxAllowedDiscount = cartTotal.multiply(new BigDecimal("0.50"));
        BigDecimal proposedTotalDiscount = tierShippingDiscount.add(promoDiscount);

        boolean isDiscountCapped = proposedTotalDiscount.compareTo(maxAllowedDiscount) > 0;
        BigDecimal finalTotalDiscount = isDiscountCapped ? maxAllowedDiscount : proposedTotalDiscount;

        // Net Freight in USD
        BigDecimal netFreight = grossFreight.subtract(finalTotalDiscount).max(BigDecimal.ZERO);
        log.info("Discount total={} capped={} netFreight={}",
            finalTotalDiscount, isDiscountCapped, netFreight);

        // 5. Taxes
        BigDecimal importTax = BigDecimal.ZERO;
        BigDecimal icmsTax = BigDecimal.ZERO;

        if ("BR".equalsIgnoreCase(countryCode)) {
            // Statutory $50.00 USD Remessa Conforme Threshold
            if (cartTotal.add(netFreight).compareTo(USD_50_THRESHOLD) <= 0) {
                // Under or equal to $50 USD: 20% Import Tax on net freight
                importTax = cartTotal.add(netFreight).multiply(new BigDecimal("0.20"));
            } else {
                // Over $50 USD: 60% Import Tax minus $20 USD credit
                BigDecimal grossImportTax = cartTotal.add(netFreight).multiply(new BigDecimal("0.60"));
                importTax = grossImportTax.subtract(new BigDecimal("20.00"))
                        .max(BigDecimal.ZERO);
            }

            // ICMS Gross-Up Tax Base: (Net Freight + Import Tax) / (1 - 0.17)
            BigDecimal icmsBase = netFreight.add(importTax)
                    .divide(new BigDecimal("0.83"), 4, RoundingMode.HALF_UP);

            icmsTax = icmsBase.multiply(new BigDecimal("0.17"));
            log.info("Calculated Brazil taxes importTax={}, icmsTax={}", importTax, icmsTax);

            if (importTax.compareTo(BigDecimal.ZERO) > 0) {
                appliedFees.put("BR_IMPORT_TAX", round(importTax));
            }
            if (icmsTax.compareTo(BigDecimal.ZERO) > 0) {
                appliedFees.put("BR_ICMS_TAX", round(icmsTax));
            }
        }

        // 6. Final USD Surcharges & Shipping Fee Assembly
        BigDecimal totalSurcharges = importTax.add(icmsTax);
        BigDecimal finalShippingFee = netFreight.add(totalSurcharges);
        log.info("Shipping calculation result grossFreight={}, totalSurcharges={}, finalFee={}",
            grossFreight, totalSurcharges, finalShippingFee);

        return CalculateShippingResponse.builder()
                .baseShippingRate(round(baseRate))
                .totalDiscount(round(finalTotalDiscount))
                .totalSurcharges(round(totalSurcharges))
                .shippingDiscount(round(tierShippingDiscount))
                .finalShippingFee(round(finalShippingFee))
                .discountCapped(isDiscountCapped)
                .appliedFees(appliedFees)
                .build();

    }

    private BigDecimal round(BigDecimal originalValue) {
        return originalValue.setScale(2, RoundingMode.HALF_UP);
    }

}