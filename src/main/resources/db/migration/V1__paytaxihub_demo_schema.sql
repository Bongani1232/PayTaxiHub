-- Fresh-install schema for the PayTaxiHub educational demo (MySQL 8+).
-- Ten core domain tables matching the current JPA entities.
CREATE TABLE IF NOT EXISTS `User` (
 user_id INT AUTO_INCREMENT PRIMARY KEY, full_name VARCHAR(100) NOT NULL, email VARCHAR(254) NOT NULL UNIQUE,
 phone_number VARCHAR(20) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL,
 role VARCHAR(20) NOT NULL, created_at DATETIME NOT NULL
);
CREATE TABLE IF NOT EXISTS FleetOwner (
 fleet_owner_id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL UNIQUE, company_name VARCHAR(150) NOT NULL,
 CONSTRAINT fk_owner_user FOREIGN KEY (user_id) REFERENCES `User`(user_id)
);
CREATE TABLE IF NOT EXISTS Driver (
 driver_id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL UNIQUE, sa_id_number CHAR(13) NOT NULL UNIQUE,
 prdp_number VARCHAR(50) NOT NULL UNIQUE, kyc_status VARCHAR(20) NOT NULL DEFAULT 'Pending', fleet_owner_id INT NULL,
 CONSTRAINT fk_driver_user FOREIGN KEY (user_id) REFERENCES `User`(user_id),
 CONSTRAINT fk_driver_owner FOREIGN KEY (fleet_owner_id) REFERENCES FleetOwner(fleet_owner_id),
 CONSTRAINT chk_sa_id_13 CHECK (sa_id_number REGEXP '^[0-9]{13}$')
);
CREATE TABLE IF NOT EXISTS Vehicle (
 vehicle_id INT AUTO_INCREMENT PRIMARY KEY, driver_id INT NOT NULL, fleet_owner_id INT NULL,
 registration_number VARCHAR(30) NOT NULL UNIQUE, vehicle_model VARCHAR(100) NOT NULL, vehicle_status VARCHAR(20) NOT NULL,
 CONSTRAINT fk_vehicle_driver FOREIGN KEY (driver_id) REFERENCES Driver(driver_id),
 CONSTRAINT fk_vehicle_owner FOREIGN KEY (fleet_owner_id) REFERENCES FleetOwner(fleet_owner_id)
);
CREATE TABLE IF NOT EXISTS Wallet (
 wallet_id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL UNIQUE, balance DECIMAL(12,2) NOT NULL DEFAULT 0,
 wallet_status VARCHAR(20) NOT NULL DEFAULT 'Active', created_at DATETIME NOT NULL,
 CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES `User`(user_id), CONSTRAINT chk_wallet_nonnegative CHECK (balance >= 0)
);
CREATE TABLE IF NOT EXISTS Route (
 route_id INT AUTO_INCREMENT PRIMARY KEY, route_name VARCHAR(150) NOT NULL, start_location VARCHAR(200) NOT NULL,
 destination VARCHAR(200) NOT NULL, fare_type VARCHAR(20) NOT NULL, fare_rate DECIMAL(10,2) NOT NULL, estimated_distance_km DECIMAL(8,2) NULL,
 is_active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS Trip (
 trip_id INT AUTO_INCREMENT PRIMARY KEY, driver_id INT NOT NULL, commuter_id INT NOT NULL, vehicle_id INT NOT NULL, route_id INT NULL,
 start_time DATETIME NULL, end_time DATETIME NULL, start_latitude DECIMAL(10,7) NULL, start_longitude DECIMAL(10,7) NULL,
 end_latitude DECIMAL(10,7) NULL, end_longitude DECIMAL(10,7) NULL, distance_km DECIMAL(8,2) NOT NULL DEFAULT 0,
 fare_amount DECIMAL(12,2) NOT NULL DEFAULT 0, trip_status VARCHAR(20) NOT NULL,
 CONSTRAINT fk_trip_driver FOREIGN KEY (driver_id) REFERENCES Driver(driver_id), CONSTRAINT fk_trip_commuter FOREIGN KEY (commuter_id) REFERENCES `User`(user_id),
 CONSTRAINT fk_trip_vehicle FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id), CONSTRAINT fk_trip_route FOREIGN KEY (route_id) REFERENCES Route(route_id)
);
CREATE TABLE IF NOT EXISTS `Transaction` (
 transaction_id INT AUTO_INCREMENT PRIMARY KEY, trip_id INT NULL, wallet_id INT NULL, from_wallet_id INT NULL, to_wallet_id INT NULL,
 driver_id INT NULL, amount DECIMAL(12,2) NOT NULL, transaction_type VARCHAR(20) NOT NULL,
 transaction_status VARCHAR(20) NOT NULL, transaction_reference VARCHAR(100) NOT NULL UNIQUE, created_at DATETIME NOT NULL,
 CONSTRAINT fk_tx_trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id), CONSTRAINT fk_tx_wallet FOREIGN KEY (wallet_id) REFERENCES Wallet(wallet_id),
 CONSTRAINT fk_tx_from_wallet FOREIGN KEY (from_wallet_id) REFERENCES Wallet(wallet_id), CONSTRAINT fk_tx_to_wallet FOREIGN KEY (to_wallet_id) REFERENCES Wallet(wallet_id),
 CONSTRAINT fk_tx_driver FOREIGN KEY (driver_id) REFERENCES Driver(driver_id), CONSTRAINT chk_tx_amount CHECK (amount > 0)
);
CREATE TABLE IF NOT EXISTS Shift (
 shift_id INT AUTO_INCREMENT PRIMARY KEY, driver_id INT NOT NULL, vehicle_id INT NOT NULL, start_time DATETIME NOT NULL, end_time DATETIME NULL,
 total_earnings DECIMAL(12,2) NOT NULL DEFAULT 0, owner_target DECIMAL(12,2) NULL, qr_token VARCHAR(100) NULL UNIQUE, shift_status VARCHAR(20) NOT NULL,
 CONSTRAINT fk_shift_driver FOREIGN KEY (driver_id) REFERENCES Driver(driver_id), CONSTRAINT fk_shift_vehicle FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id)
);
CREATE TABLE IF NOT EXISTS SOSEvent (
 sos_id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, trip_id INT NULL, latitude DECIMAL(10,7) NULL, longitude DECIMAL(10,7) NULL,
 sos_status VARCHAR(20) NOT NULL, created_at DATETIME NOT NULL,
 CONSTRAINT fk_sos_user FOREIGN KEY (user_id) REFERENCES `User`(user_id), CONSTRAINT fk_sos_trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id)
);
