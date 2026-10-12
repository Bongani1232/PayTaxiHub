-- Add account activation support to existing databases without failing on a fresh reset schema.
SET @has_is_active = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'User' AND COLUMN_NAME = 'is_active'
);
SET @add_is_active_sql = IF(
  @has_is_active = 0,
  'ALTER TABLE `User` ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
  'SELECT 1'
);
PREPARE add_is_active_stmt FROM @add_is_active_sql;
EXECUTE add_is_active_stmt;
DEALLOCATE PREPARE add_is_active_stmt;
