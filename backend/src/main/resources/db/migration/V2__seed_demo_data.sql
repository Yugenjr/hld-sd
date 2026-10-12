-- V2: Seed Data for Smart EV Charging Platform

-- Demo Users
-- Passwords:
-- demo@smartev.com -> Password123! ($2a$10$wN16lqZc7eQ8r8kXU.4XUe6wUf5hLq0d21aTsqV4PfxwG5E6o9rKG)
-- admin@smartev.com -> AdminPassword123! ($2a$10$wN16lqZc7eQ8r8kXU.4XUe6wUf5hLq0d21aTsqV4PfxwG5E6o9rKG)
INSERT INTO users (name, email, password_hash, role, created_at)
VALUES 
('Alex Morgan', 'demo@smartev.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'ROLE_USER', CURRENT_TIMESTAMP),
('System Administrator', 'admin@smartev.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'ROLE_ADMIN', CURRENT_TIMESTAMP);

-- Charging Stations
INSERT INTO charging_stations (name, address, latitude, longitude, operational_status, amenities, created_at, updated_at)
VALUES
('Downtown GreenCharge Hub', '101 Market St, Financial District, San Francisco, CA', 37.7937, -122.3965, 'OPERATIONAL', 'WiFi, Coffee Shop, 24/7 Security, Restrooms', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Silicon Valley Supercharge Center', '3200 Hillview Ave, Palo Alto, CA', 37.4124, -122.1487, 'OPERATIONAL', 'Restrooms, Covered Canopy, EV Lounge, Vending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Marina Bay FastCharge Plaza', '850 Marina Blvd, San Francisco, CA', 37.8055, -122.4385, 'OPERATIONAL', 'Scenic View, Cafe, Restrooms, Bike Sharing', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('East Bay Transit Charging Hub', '1900 Broadway, Oakland, CA', 37.8080, -122.2690, 'OPERATIONAL', 'Transit Access, Restrooms, 24/7 Lighting', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SFO Airport Rapid Depot', '780 N McDonnell Rd, San Francisco, CA', 37.6213, -122.3790, 'MAINTENANCE', 'Airport Shuttle, Restrooms, Lounge, Food Court', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Chargers for Downtown GreenCharge Hub (Station 1)
INSERT INTO chargers (station_id, connector_type, max_power_kw, status, price_per_kwh, version)
VALUES
(1, 'CCS2', 150.0, 'AVAILABLE', 0.38, 0),
(1, 'NACS', 250.0, 'AVAILABLE', 0.42, 0),
(1, 'CCS2', 350.0, 'AVAILABLE', 0.45, 0),
(1, 'TYPE2', 22.0, 'AVAILABLE', 0.28, 0);

-- Chargers for Silicon Valley Supercharge Center (Station 2)
INSERT INTO chargers (station_id, connector_type, max_power_kw, status, price_per_kwh, version)
VALUES
(2, 'NACS', 250.0, 'AVAILABLE', 0.40, 0),
(2, 'CCS2', 150.0, 'AVAILABLE', 0.36, 0),
(2, 'CHADEMO', 50.0, 'AVAILABLE', 0.32, 0);

-- Chargers for Marina Bay FastCharge Plaza (Station 3)
INSERT INTO chargers (station_id, connector_type, max_power_kw, status, price_per_kwh, version)
VALUES
(3, 'CCS2', 150.0, 'AVAILABLE', 0.39, 0),
(3, 'NACS', 250.0, 'AVAILABLE', 0.44, 0);

-- Chargers for East Bay Transit Charging Hub (Station 4)
INSERT INTO chargers (station_id, connector_type, max_power_kw, status, price_per_kwh, version)
VALUES
(4, 'CCS2', 100.0, 'AVAILABLE', 0.34, 0),
(4, 'TYPE2', 22.0, 'AVAILABLE', 0.25, 0);

-- Chargers for SFO Airport Rapid Depot (Station 5 - In Maintenance)
INSERT INTO chargers (station_id, connector_type, max_power_kw, status, price_per_kwh, version)
VALUES
(5, 'CCS2', 350.0, 'OUT_OF_SERVICE', 0.48, 0);
