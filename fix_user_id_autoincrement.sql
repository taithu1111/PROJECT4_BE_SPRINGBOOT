-- Fix user table ID to have AUTO_INCREMENT
-- This script handles foreign key constraints properly

USE ecommerce_project4;

-- Step 1: Drop foreign key constraints that reference user.id
ALTER TABLE address DROP FOREIGN KEY IF EXISTS FKda8tuywtf0gb6sedwk7la1pgi;
ALTER TABLE cart DROP FOREIGN KEY IF EXISTS FKl70asp4l4w0jmbm1tqyofho4o;
ALTER TABLE cart_item DROP FOREIGN KEY IF EXISTS FK1uobyhgl1wvgt1jpccia8xxs3;
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FK32ql8ubntj5uh44ph9659tiih;
ALTER TABLE rating DROP FOREIGN KEY IF EXISTS FKg9uhjdu699cxmk8vj1u9o48al;
ALTER TABLE review DROP FOREIGN KEY IF EXISTS FKiyf57dy48lyiftdrf7y87rnxi;

-- Step 2: Modify user.id to have AUTO_INCREMENT
ALTER TABLE user MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- Step 3: Re-add foreign key constraints
ALTER TABLE address ADD CONSTRAINT FKda8tuywtf0gb6sedwk7la1pgi 
    FOREIGN KEY (user_id) REFERENCES user(id);
    
ALTER TABLE cart ADD CONSTRAINT FKl70asp4l4w0jmbm1tqyofho4o 
    FOREIGN KEY (user_id) REFERENCES user(id);
    
ALTER TABLE cart_item ADD CONSTRAINT FK1uobyhgl1wvgt1jpccia8xxs3 
    FOREIGN KEY (user_id) REFERENCES user(id);
    
ALTER TABLE orders ADD CONSTRAINT FK32ql8ubntj5uh44ph9659tiih 
    FOREIGN KEY (user_id) REFERENCES user(id);
    
ALTER TABLE rating ADD CONSTRAINT FKg9uhjdu699cxmk8vj1u9o48al 
    FOREIGN KEY (user_id) REFERENCES user(id);
    
ALTER TABLE review ADD CONSTRAINT FKiyf57dy48lyiftdrf7y87rnxi 
    FOREIGN KEY (user_id) REFERENCES user(id);

-- Verify the change
SHOW CREATE TABLE user;
