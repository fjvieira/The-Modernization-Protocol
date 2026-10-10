DROP TABLE IF EXISTS promo_rules;
DROP TABLE IF EXISTS carrier_surcharges;
DROP TABLE IF EXISTS flat_rates;
DROP TABLE IF EXISTS shipping_zones;

-- 1. Shipping Zones Table
CREATE TABLE shipping_zones (
    zone_id VARCHAR(50) PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL,
    postal_prefix VARCHAR(10) NOT NULL,
    zone_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for postal prefix wildcard matching
CREATE INDEX idx_zones_country_prefix ON shipping_zones(country_code, postal_prefix);

-- 2. Flat Rates Table
CREATE TABLE flat_rates (
    rate_id VARCHAR(50) PRIMARY KEY,
    zone_id VARCHAR(50) NOT NULL,
    base_rate DECIMAL(10, 2) NOT NULL CHECK (base_rate >= 0),
    regional_surcharge DECIMAL(10, 2) NOT NULL DEFAULT 0.00 CHECK (regional_surcharge >= 0),
    FOREIGN KEY (zone_id) REFERENCES shipping_zones(zone_id) ON DELETE CASCADE
);

-- 3. Carrier Surcharges Table
CREATE TABLE carrier_surcharges (
    surcharge_id VARCHAR(50) PRIMARY KEY,
    carrier_code VARCHAR(30) NOT NULL,
    region VARCHAR(10) NOT NULL,
    fee_amount DECIMAL(10, 2) NOT NULL CHECK (fee_amount >= 0),
    CONSTRAINT uk_carrier_region UNIQUE (carrier_code, region)
);

-- 4. Dynamic Promo Rules Table
CREATE TABLE promo_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(30) NOT NULL,
    discount_value DECIMAL(10, 4) NOT NULL,
    min_cart_subtotal DECIMAL(10, 2) DEFAULT 0.00,
    start_hour INT CHECK (start_hour BETWEEN 0 AND 23),
    end_hour INT CHECK (end_hour BETWEEN 0 AND 23),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);