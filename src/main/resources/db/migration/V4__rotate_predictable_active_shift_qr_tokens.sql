-- Invalidate the old predictable ID-based QR payloads on upgrade and rotate active shifts.
-- Ended shifts are not verifiable; only active shifts need a new token for continued demo use.
UPDATE Shift
SET qr_token = CONCAT('PTH-SHIFT:', UUID())
WHERE shift_status = 'Active'
  AND (qr_token IS NULL OR qr_token NOT REGEXP '^PTH-SHIFT:[A-Fa-f0-9-]{36}$');
