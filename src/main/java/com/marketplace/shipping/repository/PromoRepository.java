package com.marketplace.shipping.repository;

import com.marketplace.shipping.dto.PromoRule;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PromoRepository {

    private final JdbcTemplate jdbcTemplate;

    public PromoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<PromoRule> findActiveRule(String promoCode) {
        String sql = "SELECT code, discount_type, discount_value, min_cart_subtotal, start_hour, end_hour, is_active " +
                     "FROM promo_rules " +
                     "WHERE UPPER(code) = UPPER(?) AND is_active = TRUE LIMIT 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PromoRule(
                rs.getString("code"),
                PromoRule.DiscountType.valueOf(rs.getString("discount_type")),
                rs.getBigDecimal("discount_value"),
                rs.getBigDecimal("min_cart_subtotal"),
                rs.getObject("start_hour") != null ? rs.getInt("start_hour") : null,
                rs.getObject("end_hour") != null ? rs.getInt("end_hour") : null,
                rs.getBoolean("is_active")
            ), promoCode));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}