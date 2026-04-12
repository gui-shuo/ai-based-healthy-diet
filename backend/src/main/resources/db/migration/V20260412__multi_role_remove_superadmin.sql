-- Remove SUPER_ADMIN, support multi-role via comma-separated VARCHAR
-- Role column: USER, ADMIN, USER,NUTRITIONIST, ADMIN,NUTRITIONIST

-- 1. Change ENUM to VARCHAR to support multi-role
ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';

-- 2. Migrate SUPER_ADMIN → ADMIN
UPDATE users SET role = 'ADMIN' WHERE role = 'SUPER_ADMIN';

-- 3. Migrate standalone NUTRITIONIST → USER,NUTRITIONIST
UPDATE users SET role = 'USER,NUTRITIONIST' WHERE role = 'NUTRITIONIST';
