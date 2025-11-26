-- Fix user table ID to have AUTO_INCREMENT (Safer version)
-- This script checks constraint names before dropping

USE ecommerce_project4;

-- First, let's see what foreign keys actually exist
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    information_schema.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_NAME = 'user'
    AND TABLE_SCHEMA = 'ecommerce_project4';

-- Now drop only the constraints that exist
-- Address table
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE 
                  WHERE CONSTRAINT_NAME = 'FKda8tuywtf0gb6sedwk7la1pgi' 
                  AND TABLE_SCHEMA = 'ecommerce_project4');
SET @sql = IF(@fk_exists > 0, 'ALTER TABLE address DROP FOREIGN KEY FKda8tuywtf0gb6sedwk7la1pgi', 'SELECT "FK not found for address"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Cart table
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE 
                  WHERE CONSTRAINT_NAME = 'FKl70asp4l4w0jmbm1tqyofho4o' 
                  AND TABLE_SCHEMA = 'ecommerce_project4');
SET @sql = IF(@fk_exists > 0, 'ALTER TABLE cart DROP FOREIGN KEY FKl70asp4l4w0jmbm1tqyofho4o', 'SELECT "FK not found for cart"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Review table
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE 
                  WHERE CONSTRAINT_NAME = 'FKiyf57dy48lyiftdrf7y87rnxi' 
                  AND TABLE_SCHEMA = 'ecommerce_project4');
SET @sql = IF(@fk_exists > 0, 'ALTER TABLE review DROP FOREIGN KEY FKiyf57dy48lyiftdrf7y87rnxi', 'SELECT "FK not found for review"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- IMPORTANT: Modify user.id to have AUTO_INCREMENT
ALTER TABLE user MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- Re-add foreign key constraints (only for tables that exist)
-- Address
SET @table_exists = (SELECT COUNT(*) FROM information_schema.TABLES 
                     WHERE TABLE_NAME = 'address' AND TABLE_SCHEMA = 'ecommerce_project4');
SET @sql = IF(@table_exists > 0, 
              'ALTER TABLE address ADD CONSTRAINT FKda8tuywtf0gb6sedwk7la1pgi FOREIGN KEY (user_id) REFERENCES user(id)',
              'SELECT "Address table not found"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Cart
SET @table_exists = (SELECT COUNT(*) FROM information_schema.TABLES 
                     WHERE TABLE_NAME = 'cart' AND TABLE_SCHEMA = 'ecommerce_project4');
SET @sql = IF(@table_exists > 0,
              'ALTER TABLE cart ADD CONSTRAINT FKl70asp4l4w0jmbm1tqyofho4o FOREIGN KEY (user_id) REFERENCES user(id)',
              'SELECT "Cart table not found"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Review
SET @table_exists = (SELECT COUNT(*) FROM information_schema.TABLES 
                     WHERE TABLE_NAME = 'review' AND TABLE_SCHEMA = 'ecommerce_project4');
SET @sql = IF(@table_exists > 0,
              'ALTER TABLE review ADD CONSTRAINT FKiyf57dy48lyiftdrf7y87rnxi FOREIGN KEY (user_id) REFERENCES user(id)',
              'SELECT "Review table not found"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify the change
SHOW CREATE TABLE user;
