-- Flyway repair script
-- Run this in your PostgreSQL database to fix migration conflicts

-- Delete conflicting migration records
DELETE FROM flyway_schema_history WHERE version IN ('5', '6', '7');

-- Update V4 checksum if needed (you may need to adjust this)
UPDATE flyway_schema_history 
SET checksum = -1780042438 
WHERE version = '4';

-- Verify the schema history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;



