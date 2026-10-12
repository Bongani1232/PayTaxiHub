-- Query indexes for the Phase 4 schema. Kept separate from V1 so each is created once.
CREATE INDEX idx_trip_driver_time ON Trip(driver_id,start_time);
CREATE INDEX idx_trip_commuter_time ON Trip(commuter_id,start_time);
CREATE INDEX idx_transaction_trip ON `Transaction`(trip_id);
CREATE INDEX idx_shift_driver_status ON Shift(driver_id,shift_status);
