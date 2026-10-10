package com.marketplace.shipping.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.marketplace.shipping.dto.PromoRule;
import com.marketplace.shipping.repository.PromoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PromoEvaluatorService {

    private final PromoRepository promoRepository;

    public PromoEvaluatorService(PromoRepository promoRepository) {
        this.promoRepository = promoRepository;
    }

    public BigDecimal evaluatePromoDiscount(String promoCode, BigDecimal cartTotal, 
                                            BigDecimal grossShippingFee, BigDecimal tierShippingDiscount, 
                                            OffsetDateTime orderTimestamp) {
        if (promoCode == null || promoCode.isBlank()) {
            log.info("No promo code supplied");
            return BigDecimal.ZERO;
        }

        // 1. Fetch active rule dynamically from DB/Cache
        Optional<PromoRule> ruleOpt = promoRepository.findActiveRule(promoCode.trim().toUpperCase());
        if (ruleOpt.isEmpty()) {
            log.warn("Promo code {} was not found or is inactive", promoCode);
            return BigDecimal.ZERO;
        }

        PromoRule rule = ruleOpt.get();

        // 2. Validate Cart Threshold Constraint
        if (rule.getMinCartSubtotal() != null && cartTotal.compareTo(rule.getMinCartSubtotal()) < 0) {
            log.info("Promo code {} rejected below cart minimum {}", promoCode, rule.getMinCartSubtotal());
            return BigDecimal.ZERO;
        }

        // 3. Validate Time Window Constraint (e.g. Night Owl)
        if (rule.getStartHour() != null && rule.getEndHour() != null) {
            if (orderTimestamp == null) {
                log.info("Promo code {} rejected because timestamp is missing", promoCode);
                return BigDecimal.ZERO;
            }
            int hour = orderTimestamp.getHour();
            boolean inWindow = rule.getStartHour() > rule.getEndHour()
                    ? (hour >= rule.getStartHour() || hour < rule.getEndHour()) // Overnight window (22 to 04)
                    : (hour >= rule.getStartHour() && hour < rule.getEndHour());  // Daytime window (09 to 17)

            if (!inWindow) {
                log.info("Promo code {} rejected outside time window", promoCode);
                return BigDecimal.ZERO;
            }
        }

        // 4. Calculate Discount Amount
        switch (rule.getDiscountType()) {
            case FLAT:
                log.info("Promo code {} applied flat discount {}", promoCode, rule.getDiscountValue());
                return rule.getDiscountValue();

            case PERCENTAGE:
                log.info("Promo code {} applied percentage discount", promoCode);
                return grossShippingFee.multiply(rule.getDiscountValue());

            case PERCENTAGE_REMAINING:
                BigDecimal remainingFee = grossShippingFee.subtract(tierShippingDiscount).max(BigDecimal.ZERO);
                log.info("Promo code {} applied remaining-fee percentage discount", promoCode);
                return remainingFee.multiply(rule.getDiscountValue());

            default:
                log.warn("Promo code {} has unsupported discount type", promoCode);
                return BigDecimal.ZERO;
        }
    }
}