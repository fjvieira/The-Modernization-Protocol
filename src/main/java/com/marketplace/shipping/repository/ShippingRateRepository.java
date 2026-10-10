package com.marketplace.shipping.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class ShippingRateRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShippingRateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Value
    public static class ZoneRate {

        String zoneId;

        String zoneName;

        BigDecimal baseRate;

        BigDecimal regionalSurcharge;
    }

    public Optional<ShippingRateRepository.ZoneRate> findZoneRate(String countryCode, String zipCode) {
        String sql = "SELECT z.zone_id, z.zone_name, f.base_rate, f.regional_surcharge " +
                "FROM shipping_zones z " +
                "JOIN flat_rates f ON z.zone_id = f.zone_id " +
                "WHERE z.country_code = ? " +
                "AND ? LIKE CONCAT(z.postal_prefix, '%') " +
                "ORDER BY LENGTH(z.postal_prefix) DESC LIMIT 1";
        try {
            List<ZoneRate> rates = jdbcTemplate.query(sql, (rs, rowNum) -> new ZoneRate(
                    rs.getString("zone_id"),
                    rs.getString("zone_name"),
                    rs.getBigDecimal("base_rate"),
                    rs.getBigDecimal("regional_surcharge")), countryCode, zipCode);

            return rates.isEmpty() ? Optional.empty() : Optional.of(rates.get(0));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<BigDecimal> findCarrierSurcharge(String carrierCode, String region) {
        String sql = "SELECT fee_amount FROM carrier_surcharges WHERE carrier_code = ? AND region = ? LIMIT 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, BigDecimal.class, carrierCode, region));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}