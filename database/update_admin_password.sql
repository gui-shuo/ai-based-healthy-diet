-- 更新admin账号密码
-- 使用标准的BCrypt哈希（密码: password，cost=10）

USE nutriai;

-- 更新admin密码为 "password"
-- 这是password的标准BCrypt哈希
UPDATE `users` 
SET `password` = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE `username` = 'admin';

-- 验证更新
SELECT username, email, role, status, LEFT(password, 20) as password_prefix 
FROM users 
WHERE username = 'admin';
