-- Seed Shipping Zones
INSERT INTO shipping_zones (zone_id, country_code, postal_prefix, zone_name) VALUES
('ZONE_US_EAST', 'US', '10', 'US East Coast'),
('ZONE_US_WEST', 'US', '90', 'US West Coast'),
('ZONE_US_SOUTH', 'US', '30', 'US Southeast'),
('ZONE_US_CENTRAL', 'US', '60', 'US Central'),
('ZONE_CA_ONTARIO', 'CA', 'M5', 'Canada Ontario'),
('ZONE_BR_SP',   'BR', '01', 'São Paulo Metro'),
('ZONE_BR_RJ',   'BR', '20', 'Rio de Janeiro Metro'),
('ZONE_BR_SOUTH', 'BR', '90', 'Brazil South');

-- Seed Flat Rates
INSERT INTO flat_rates (rate_id, zone_id, base_rate, regional_surcharge) VALUES
('RATE_US_EAST', 'ZONE_US_EAST', 10.00, 2.50),
('RATE_US_WEST', 'ZONE_US_WEST', 15.00, 5.00),
('RATE_US_SOUTH', 'ZONE_US_SOUTH', 12.50, 3.25),
('RATE_US_CENTRAL', 'ZONE_US_CENTRAL', 11.00, 1.75),
('RATE_CA_ONTARIO', 'ZONE_CA_ONTARIO', 18.00, 4.00),
('RATE_BR_SP',   'ZONE_BR_SP',   20.00, 0.00),
('RATE_BR_RJ',   'ZONE_BR_RJ',   22.00, 2.00),
('RATE_BR_SOUTH', 'ZONE_BR_SOUTH', 25.00, 6.50);

-- Seed Carrier Surcharges (Fallback for remote zones)
INSERT INTO carrier_surcharges (surcharge_id, carrier_code, region, fee_amount) VALUES
('SUR_FEDEX_US', 'FEDEX', 'US', 8.50),
('SUR_UPS_US',   'UPS',   'US', 7.00),
('SUR_DHL_BR',   'DHL',   'BR', 12.00),
('SUR_FEDEX_BR', 'FEDEX', 'BR', 10.00),
('SUR_UPS_BR',   'UPS',   'BR', 9.00),
('SUR_DHL_US',   'DHL',   'US', 11.50),
('SUR_FEDEX_CA', 'FEDEX', 'CA', 6.50),
('SUR_UPS_CA',   'UPS',   'CA', 5.50);

-- Seed Dynamic Promo Rules
INSERT INTO promo_rules (rule_id, code, discount_type, discount_value, min_cart_subtotal, start_hour, end_hour, is_active) VALUES
('RULE_SUMMER5',    'SUMMER5',    'FLAT',                 5.0000, 0.00,   NULL, NULL, TRUE),
('RULE_NIGHTOWL10', 'NIGHTOWL10', 'PERCENTAGE_REMAINING', 0.1000, 0.00,   22,   4,    TRUE),
('RULE_VIP20',      'VIP20',      'PERCENTAGE',           0.2000, 50.00,  NULL, NULL, TRUE),
('RULE_WELCOME10',  'WELCOME10',  'PERCENTAGE',           0.1000, 25.00,  NULL, NULL, TRUE),
('RULE_FREESHIP10', 'FREESHIP10', 'FLAT',                10.0000, 75.00,  NULL, NULL, TRUE),
('RULE_EARLYBIRD5', 'EARLYBIRD5', 'FLAT',                5.0000, 0.00,    6,   12, TRUE),
('RULE_NIGHTOWL15', 'NIGHTOWL15', 'PERCENTAGE_REMAINING', 0.1500, 100.00, 22, 4, TRUE),
('RULE_EXPIRED10',  'EXPIRED10',  'PERCENTAGE',           0.1000, 0.00,  NULL, NULL, FALSE);
